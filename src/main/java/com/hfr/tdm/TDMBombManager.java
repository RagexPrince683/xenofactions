package com.hfr.tdm;

import com.hfr.compat.HbmCsgoChargeIntegration;
import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Server-authoritative lifecycle for the short combat rounds on a bomb map. */
public final class TDMBombManager {
    public enum BombRoundState { DISABLED, WAITING_FOR_TEAMS, PRE_ROUND, LIVE, BOMB_PLANTED, OBJECTIVE_ERROR, ROUND_END }
    public enum BombRoundWinReason { TERRORISTS_ELIMINATED, COUNTER_TERRORISTS_ELIMINATED, BOMB_DETONATED, BOMB_DEFUSED, TIME_EXPIRED, ADMIN_FORCED }
    public static final int BUY_TIME_TICKS = TDMManager.BUY_TIME_TICKS;
    private static final int INTERMISSION_TICKS = 5 * 20;
    private static final int MISSING_RESULT_GRACE_TICKS = 5;
    /** HBM decrements once per tick and explodes at zero; five leaves a small multi-tick margin beyond XF's <= 1 detonation latch. */
    private static final int UNKNOWN_REMOVAL_DEFUSE_MIN_TIMER = 5;
    private static BombRoundState state = BombRoundState.DISABLED;
    private static long stateEndTick;
    private static final Set<String> eliminated = new HashSet<String>();
    private static TrackedBomb bomb;
    private static PendingPlant pendingPlant;
    private static long missingBombSince = -1L;
    private static boolean missingBombWarningLogged;

    private TDMBombManager() { }
    public static BombRoundState getState(){return state;}
    public static boolean isRoundActive(){return state==BombRoundState.LIVE||state==BombRoundState.BOMB_PLANTED;}
    /** True only for BOMB phases which intentionally freeze ordinary world editing. */
    public static boolean shouldRestrictWorldInteraction(World world){
        return world!=null
                && TDMManager.isEnabled(world)
                && TDMManager.isBombMode(world)
                && !TDMManager.isMapVoteActive(world)
                && (state==BombRoundState.PRE_ROUND||state==BombRoundState.ROUND_END);
    }
    public static String getPlantedSite(){return bomb==null?"":bomb.site;}
    public static int getRemainingSeconds(World w){return (state==BombRoundState.LIVE||state==BombRoundState.PRE_ROUND)?(int)Math.max(0,(stateEndTick-w.getTotalWorldTime()+19)/20):0;}
    public static boolean isEliminated(EntityPlayer player) {
        return eliminated.contains(key(player));
    }

    /** Records a late hardcore join without evaluating an already-running round again. */
    public static void markLateJoinerEliminated(EntityPlayer player) {
        if (player != null && TDMManager.isHardcoreRespawns(player.worldObj)) {
            eliminated.add(key(player));
        }
    }

    public static void startMatch(World world) {
        TDMData data = TDMData.get(world);
        data.redBombWins = 0;
        data.blueBombWins = 0;
        data.redBombLosses = 0;
        data.blueBombLosses = 0;
        data.markDirty();

        cleanup(world, true);
        if (hasEnoughPlayersForBombRound(world)) {
            beginNextBombRound(world);
        } else {
            waitForTeams(world);
            TDMManager.placeAllPlayersAtSelectedMap(world, TDMManager.KitSelectionContext.NONE);
        }
        if (!HbmCsgoChargeIntegration.isAvailable()) {
            broadcast("Bomb mode requires HBM's hbm:tile.charge_c4csgo block.");
        }
    }
    public static void tick(World world){
        if(!TDMManager.isEnabled(world)||!TDMManager.isBombMode(world)){cleanup(world,true);return;}
        long now=world.getTotalWorldTime();
        if(pendingPlant!=null&&now>pendingPlant.tick){PendingPlant p=pendingPlant;pendingPlant=null;if(p.player.worldObj==world&&HbmCsgoChargeIntegration.isCsgoCharge(world.getBlock(p.x,p.y,p.z)))acceptPlant(p.player,p.x,p.y,p.z);}
        if(state==BombRoundState.BOMB_PLANTED)watchForMissingTrackedBomb(world,now);
        if(state==BombRoundState.DISABLED){if(hasEnoughPlayersForBombRound(world))beginNextBombRound(world);else waitForTeams(world);}
        else if(state==BombRoundState.WAITING_FOR_TEAMS&&hasEnoughPlayersForBombRound(world))beginNextBombRound(world);
        if(state==BombRoundState.PRE_ROUND&&now>=stateEndTick) startRound(world);
        else if(state==BombRoundState.LIVE&&now>=stateEndTick) completeRound(world,TDMManager.getCounterTerroristTeam(world),BombRoundWinReason.TIME_EXPIRED);
        else if(state==BombRoundState.ROUND_END&&now>=stateEndTick){if(hasEnoughPlayersForBombRound(world))beginNextBombRound(world);else waitForTeams(world);}
        if(state==BombRoundState.LIVE&&!TDMManager.isHardcoreRespawns(world))ensureLiveRoundBombAssigned(world);
        sanitizeBombInventories(world);
        TDMSpectatorManager.tick();
    }
    public static void beginBuyTime(World world) {
        cleanupTransientState(world);
        state = BombRoundState.PRE_ROUND;
        stateEndTick = world.getTotalWorldTime() + BUY_TIME_TICKS;
        Random random = new Random();
        if (XFConfig.tdmBombLifecycleDebug && MainRegistry.logger != null) {
            MainRegistry.logger.info("TDM BUY: begin map={}", TDMManager.getSelectedMap(world));
        }

        for (EntityPlayerMP player : TDMManager.getOnlinePlayers()) {
            if (!TDMManager.isAliveForTDM(player)) {
                continue;
            }
            if (!TDMManager.placePlayerAtSelectedMapSpawn(player, random)) {
                continue;
            }

            // Round initialization restores health and hunger at the final team spawn.
            TDMManager.restorePlayerForRound(player);
            TDMManager.resetTDMTransientPlayerState(player);
            player.inventory.clearInventory(null, -1);
            for (int slot = 0; slot < player.inventory.armorInventory.length; slot++) {
                player.inventory.armorInventory[slot] = null;
            }
            TDMManager.clearKitSelection(player);
            // Freeze anchors are intentionally created only after authoritative placement.
            TDMManager.enrollGlobalBuyProtection(player);
            TDMManager.promptForKit(player, TDMManager.KitSelectionContext.BUY_PHASE);
        }
        TDMManager.sendStatusToAll(world);
    }

    public static void beginNextBombRound(World world){if(TDMManager.isHardcoreRespawns(world))beginBuyTime(world);else startRoundWithoutBuyTime(world);}
    public static void startRoundWithoutBuyTime(World world) {
        if (world == null || world.isRemote || TDMManager.isMapVoteActive(world)) {
            return;
        }
        TDMManager.TDMMap map = TDMManager.getSelectedMapData(world);
        if (map == null || map.mode != TDMManager.TDMGameMode.BOMB
                || TDMManager.isHardcoreRespawns(world)) {
            return;
        }
        if (!hasEnoughPlayersForBombRound(world)) {
            waitForTeams(world);
            return;
        }

        cleanupTransientState(world);
        purgeBombObjectiveItems(world);
        eliminated.clear();
        Random random = new Random();
        List<EntityPlayerMP> placedPlayers = new ArrayList<EntityPlayerMP>();
        for (EntityPlayerMP player : TDMManager.getOnlinePlayers()) {
            if (!TDMManager.isAliveForTDM(player)) {
                continue;
            }
            if (!TDMManager.placePlayerAtSelectedMapSpawn(player, random)) {
                continue;
            }

            // Restore the new life before protection makes this live round playable.
            TDMManager.restorePlayerForRound(player);
            TDMManager.resetTDMTransientPlayerState(player);
            player.inventory.clearInventory(null, -1);
            for (int slot = 0; slot < player.inventory.armorInventory.length; slot++) {
                player.inventory.armorInventory[slot] = null;
            }
            placedPlayers.add(player);
        }

        state = BombRoundState.LIVE;
        stateEndTick = world.getTotalWorldTime()
                + TDMManager.getEffectiveBombRoundTicks(world, map.name);
        for (EntityPlayerMP player : placedPlayers) {
            TDMManager.promptForKit(player, TDMManager.KitSelectionContext.RESPAWN_LOCK);
        }
        ensureLiveRoundBombAssigned(world);
        TDMManager.sendStatusToAll(world);
    }

    public static void startRound(World world){
        if(world.isRemote||state!=BombRoundState.PRE_ROUND||TDMManager.isMapVoteActive(world))return;
        TDMManager.TDMMap map=TDMManager.getSelectedMapData(world);if(map==null||map.mode!=TDMManager.TDMGameMode.BOMB)return;
        if(!hasEnoughPlayersForBombRound(world)){waitForTeams(world);return;}
        for(EntityPlayerMP p:TDMManager.getOnlinePlayers()){if(TDMManager.isAliveForTDM(p)&&!TDMManager.hasSelectedKit(p)){TDMManager.Team t=TDMManager.getOrAssignPlayerTeam(p);int[] costs=TDMKitManager.getKitCosts(map.name,t);int fallback=-1;for(int i=0;i<costs.length;i++)if(!map.buyScoreEnabled||costs[i]==0){fallback=i;break;}if(fallback>=0)TDMManager.selectKit(p,fallback);else{p.addChatMessage(new ChatComponentText("No free kit is available for this round."));System.err.println("TDM bomb buy: no free kit for map "+map.name+" team "+t.name);}}boolean protectedByBuy=TDMManager.hasKitSelectionProtection(p);TDMManager.cancelKitSelection(p);if(protectedByBuy&&XFConfig.tdmBombLifecycleDebug&&MainRegistry.logger!=null)MainRegistry.logger.info("TDM kit protection cleared for {} entering LIVE",p.getCommandSenderName());p.inventory.markDirty();p.inventoryContainer.detectAndSendChanges();}
        purgeBombObjectiveItems(world);state=BombRoundState.LIVE;stateEndTick=world.getTotalWorldTime()+TDMManager.getEffectiveBombRoundTicks(world,map.name);if(!assignBombToRandomTerrorist(world)){waitForTeams(world);return;}TDMManager.sendStatusToAll(world);
    }
    private static boolean assignBombToRandomTerrorist(World world){
        ItemStack available=HbmCsgoChargeIntegration.createCsgoChargeStack();
        if(available==null){System.err.println("TDM bomb round: cannot assign hbm:tile.charge_c4csgo because its inventory item is unavailable.");return false;}
        TDMManager.Team terrorists=TDMManager.getTerroristTeam(world);List<EntityPlayerMP> eligible=new ArrayList<EntityPlayerMP>();
        for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj.provider.dimensionId==world.provider.dimensionId&&TDMManager.getOrAssignPlayerTeam(p)==terrorists&&!TDMSpectatorManager.isObserving(p)&&!isEliminated(p)&&TDMManager.getKitSelectionContext(p)!=TDMManager.KitSelectionContext.RESPAWN_LOCK)eligible.add(p);
        if(eligible.isEmpty()){System.err.println("TDM bomb round: cannot assign the CSGO bomb because no eligible Terrorist is online in dimension "+world.provider.dimensionId+".");return false;}
        Collections.shuffle(eligible,new Random());
        for(EntityPlayerMP p:eligible){if(p.inventory.addItemStackToInventory(available)){p.inventory.markDirty();p.inventoryContainer.detectAndSendChanges();p.addChatMessage(new ChatComponentText("You have the bomb."));return true;}}
        EntityPlayerMP carrier=eligible.get(0);carrier.entityDropItem(available,0.0F);return true;
    }
    public static synchronized void ensureLiveRoundBombAssigned(World world){if(world==null||state!=BombRoundState.LIVE||bomb!=null||hasWorldBomb(world))return;for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj==world&&TDMManager.isTerrorist(p))for(ItemStack stack:p.inventory.mainInventory)if(isBombStack(stack))return;assignBombToRandomTerrorist(world);}
    public static int getPlayerCount(World world,TDMManager.Team wanted){int count=0;for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(!TDMSpectatorManager.isObserving(p)&&TDMManager.getOrAssignPlayerTeam(p)==wanted)count++;return count;}
    public static boolean hasBothTeams(World world){return getPlayerCount(world,TDMManager.Team.RED)>0&&getPlayerCount(world,TDMManager.Team.BLUE)>0;}
    public static boolean hasEnoughPlayersForBombRound(World world){return hasBothTeams(world)||(TDMManager.isBombTestMode()&&(getPlayerCount(world,TDMManager.Team.RED)+getPlayerCount(world,TDMManager.Team.BLUE)>0));}
    private static void waitForTeams(World world){cleanupTransientState(world);state=BombRoundState.WAITING_FOR_TEAMS;TDMManager.sendStatusToAll(world);}
    public static void onTestModeChanged(World world,boolean enabled){if(enabled){if(state==BombRoundState.WAITING_FOR_TEAMS&&hasEnoughPlayersForBombRound(world))beginNextBombRound(world);}else if(!hasBothTeams(world)){cleanupTransientState(world);state=BombRoundState.WAITING_FOR_TEAMS;TDMManager.sendStatusToAll(world);}}
    /** Called at LOWEST priority, after an uncancelled Forge placement has been accepted. */
    public static void recordAcceptedPlant(EntityPlayer player,Block block,int x,int y,int z){
        if(canPlant(player,block,x,y,z,false))pendingPlant=new PendingPlant(player,x,y,z,player.worldObj.getTotalWorldTime());
    }
    private static void acceptPlant(EntityPlayer player,int x,int y,int z){String site=TDMManager.getBombsiteAt(player.worldObj,player.dimension,x,y,z);if(!"A".equals(site)&&!"B".equals(site))return;
        bomb=new TrackedBomb(player.worldObj,player.dimension,x,y,z,site,player.getCommandSenderName(),TDMManager.getOrAssignPlayerTeam(player),TDMManager.getBombRole(player),player.worldObj.getBlock(x,y,z),player.worldObj.getBlockMetadata(x,y,z),player.worldObj.getTotalWorldTime());bomb.observe();HbmCsgoChargeIntegration.describeImplementation(player.worldObj,x,y,z);missingBombSince=-1L;missingBombWarningLogged=false;
        state=BombRoundState.BOMB_PLANTED; TDMManager.sendStatusToAll(player.worldObj);
    }
    public static boolean canPlant(EntityPlayer p,Block block,int x,int y,int z,boolean explain){
        String reason=null;
        if(!TDMManager.isEnabled(p.worldObj)||!TDMManager.isBombMode(p.worldObj)||TDMManager.isMapVoteActive(p.worldObj))return true;
        if(!HbmCsgoChargeIntegration.isCsgoCharge(block))return true;
        if(state!=BombRoundState.LIVE)reason="The bomb round is not live."; else if(TDMSpectatorManager.isObserving(p)||isEliminated(p))reason="Eliminated players cannot plant.";
        else if(!TDMManager.isTerrorist(p))reason="Only Terrorists can plant."; else if(bomb!=null)reason="A bomb is already active.";
        else if(TDMManager.getBombsiteAt(p.worldObj,p.dimension,x,y,z)==null)reason="Plant inside bombsite A or B.";
        if(reason!=null&&explain)p.addChatMessage(new ChatComponentText(reason)); return reason==null;
    }
    public static synchronized boolean isTrackedBomb(World world,int x,int y,int z){return state==BombRoundState.BOMB_PLANTED&&bomb!=null&&world==bomb.world&&world.provider.dimensionId==bomb.dim&&x==bomb.x&&y==bomb.y&&z==bomb.z;}
    public static synchronized boolean handleHbmBombResult(World world,String result,int dimension,int x,int y,int z,String playerUuid,String playerName){
        if(world==null||!TDMManager.isEnabled(world)||!TDMManager.isBombMode(world)||state!=BombRoundState.BOMB_PLANTED||bomb==null)return false;
        if(world!=bomb.world||world.provider.dimensionId!=bomb.dim||dimension!=bomb.dim||x!=bomb.x||y!=bomb.y||z!=bomb.z)return false;
        if(!"A".equals(bomb.site)&&!"B".equals(bomb.site))return false;
        if(!"DEFUSED".equals(result)&&!"DETONATED".equals(result))return false;
        bomb.runtimeResult=result;bomb.runtimePlayerUuid=playerUuid;bomb.runtimePlayerName=playerName;
        finishBombResult(world,"DEFUSED".equals(result));return true;
    }
    private static void finishBombResult(World world,boolean defused){TrackedBomb finished=bomb;if(defused&&finished!=null)purgeBombDropsNear(finished);invalidateTrackedBomb();if(defused){EntityPlayerMP player=resolveDefuser(world,finished==null?null:finished.runtimePlayerUuid,finished==null?null:finished.runtimePlayerName);if(player!=null)TDMManager.awardDefuseBuyScore(player);completeRound(world,TDMManager.getCounterTerroristTeam(world),BombRoundWinReason.BOMB_DEFUSED);}else completeRound(world,TDMManager.getTerroristTeam(world),BombRoundWinReason.BOMB_DETONATED);}
    private static EntityPlayerMP resolveDefuser(World world,String playerUuid,String playerName){
        UUID uuid=null;if(playerUuid!=null&&!playerUuid.isEmpty())try{uuid=UUID.fromString(playerUuid);}catch(IllegalArgumentException ignored){}
        for(EntityPlayerMP player:TDMManager.getOnlinePlayers())if(player.worldObj==world&&((uuid!=null&&uuid.equals(player.getUniqueID()))||(uuid==null&&playerName!=null&&playerName.equals(player.getCommandSenderName()))))return player;
        return null;
    }
    private static synchronized void watchForMissingTrackedBomb(World world,long now){
        if(state!=BombRoundState.BOMB_PLANTED||bomb==null||world==null||world!=bomb.world||world.provider.dimensionId!=bomb.dim||bomb.site==null)return;
        if(!"A".equals(bomb.site)&&!"B".equals(bomb.site))return;
        if(HbmCsgoChargeIntegration.isCsgoCharge(world.getBlock(bomb.x,bomb.y,bomb.z))){bomb.observe();missingBombSince=-1L;missingBombWarningLogged=false;return;}
        if(missingBombSince<0){missingBombSince=now;return;}
        if(now-missingBombSince>=MISSING_RESULT_GRACE_TICKS){
            TDMManager.TDMMap map=TDMManager.getSelectedMapData(world);String mapName=map==null?"<unknown>":map.name;
            if("DEFUSED".equals(bomb.runtimeResult)){finishBombResult(world,true);return;}
            if("DETONATED".equals(bomb.runtimeResult)){finishBombResult(world,false);return;}
            if(bomb.detonationObserved){finishBombResult(world,false);return;}
            if(bomb.disarmObserved){finishBombResult(world,true);return;}
            boolean supportValid=isTrackedBombSupportStillValid(bomb);
            boolean fallback=state==BombRoundState.BOMB_PLANTED&&!bomb.invalidated&&bomb.currentTimer!=null&&bomb.currentTimer.intValue()>=UNKNOWN_REMOVAL_DEFUSE_MIN_TIMER&&supportValid&&XFConfig.tdmBombUnknownRemovalAsDefuse;
            if(fallback){if(MainRegistry.logger!=null)MainRegistry.logger.warn("HBM CSGO TDM: classifying unknown removal as CT defuse by compatibility fallback. map={}, site={}, dimension={}, coordinates=({},{},{}), timer={}, started={}, supportValid={}",mapName,bomb.site,bomb.dim,bomb.x,bomb.y,bomb.z,bomb.currentTimer,bomb.currentStarted,supportValid);finishBombResult(world,true);return;}
            if(MainRegistry.logger!=null)MainRegistry.logger.error("HBM CSGO TDM unknown removal: map={}, site={}, dimension={}, coordinates=({}, {}, {}), round={}, block={}, tile={}, previousStarted={}, currentStarted={}, previousTimer={}, currentTimer={}, detonationObserved={}, disarmObserved={}, runtimeResult={}, attachmentMetadata={}, attachmentDirection={}, supportCoordinates=({}, {}, {}), supportValid={}, compatibilityFallbackEnabled={}",mapName,bomb.site,bomb.dim,bomb.x,bomb.y,bomb.z,bomb.roundIdentity,bomb.block.getClass().getName(),bomb.tileClass,bomb.previousStarted,bomb.currentStarted,bomb.previousTimer,bomb.currentTimer,bomb.detonationObserved,bomb.disarmObserved,bomb.runtimeResult,bomb.chargeMetadata,bomb.attachmentDirection,bomb.supportX,bomb.supportY,bomb.supportZ,supportValid,XFConfig.tdmBombUnknownRemovalAsDefuse);
            if(!supportValid&&MainRegistry.logger!=null)MainRegistry.logger.error("HBM CSGO TDM objective disappeared because its supporting block became invalid; refusing CT defuse fallback.");
            state=BombRoundState.OBJECTIVE_ERROR;TDMManager.sendStatusToAll(world);
        }
    }
    private static boolean isTrackedBombSupportStillValid(TrackedBomb b){return b!=null&&b.attachmentDirection!=ForgeDirection.UNKNOWN&&b.world.isSideSolid(b.supportX,b.supportY,b.supportZ,b.attachmentDirection);}
    public static void eliminate(EntityPlayer p){
        if(!TDMManager.isHardcoreRespawns(p.worldObj))return; eliminated.add(key(p));
        if(!TDMManager.isBombMode(p.worldObj)||!isRoundActive())return;
        if(TDMManager.isBombTestMode()&&!hasBothTeams(p.worldObj))return;
        TDMManager.Team ct=TDMManager.getCounterTerroristTeam(p.worldObj),tt=TDMManager.getTerroristTeam(p.worldObj);
        if(living(p.worldObj,ct)==0)completeRound(p.worldObj,tt,BombRoundWinReason.COUNTER_TERRORISTS_ELIMINATED);
        else if(state!=BombRoundState.BOMB_PLANTED&&living(p.worldObj,tt)==0)completeRound(p.worldObj,ct,BombRoundWinReason.TERRORISTS_ELIMINATED);
    }
    private static int living(World w,TDMManager.Team team){int n=0;for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(TDMManager.getOrAssignPlayerTeam(p)==team&&!isEliminated(p)&&!TDMSpectatorManager.isObserving(p))n++;return n;}
    public static synchronized void completeRound(World world,TDMManager.Team winner,BombRoundWinReason reason){
        if(!isRoundActive())return; purgeBombObjectiveItems(world);TDMManager.cancelKitSelections(world);state=BombRoundState.ROUND_END; TDMData d=TDMData.get(world);
        if(winner==TDMManager.Team.RED){d.redBombWins++;d.blueBombLosses++;}else{d.blueBombWins++;d.redBombLosses++;} d.markDirty(); stateEndTick=world.getTotalWorldTime()+INTERMISSION_TICKS;
        TDMManager.BombRole role=TDMManager.getBombRole(world,winner); broadcast((role==TDMManager.BombRole.TERRORIST?"Terrorists":"Counter-Terrorists")+" win: "+reasonText(reason)+".");
        if((winner==TDMManager.Team.RED?d.redBombWins:d.blueBombWins)>=TDMManager.getEffectiveBombScoreLimit(world,TDMManager.getSelectedMap(world))){cleanup(world,true);TDMManager.startMapVote(world);}
        else TDMManager.sendStatusToAll(world);
    }
    public static synchronized boolean forceRoundEnd(World world,TDMManager.Team winner,boolean abort){if(world==null||!TDMManager.isEnabled(world)||!TDMManager.isBombMode(world)||(!isRoundActive()&&state!=BombRoundState.PRE_ROUND&&state!=BombRoundState.OBJECTIVE_ERROR))return false;cleanupTransientState(world);state=BombRoundState.ROUND_END;stateEndTick=world.getTotalWorldTime()+INTERMISSION_TICKS;if(!abort){state=BombRoundState.LIVE;completeRound(world,winner,BombRoundWinReason.ADMIN_FORCED);}else TDMManager.sendStatusToAll(world);return true;}
    private static String reasonText(BombRoundWinReason r){switch(r){case BOMB_DEFUSED:return "bomb defused";case BOMB_DETONATED:return "bomb detonated";case TERRORISTS_ELIMINATED:return "Terrorists eliminated";case COUNTER_TERRORISTS_ELIMINATED:return "Counter-Terrorists eliminated";case ADMIN_FORCED:return "administrative decision";default:return "time expired";}}
    public static boolean isBombStack(ItemStack stack){return HbmCsgoChargeIntegration.isCsgoChargeStack(stack);}
    private static void sanitizeBombInventories(World world){for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj==world&&(TDMSpectatorManager.isObserving(p)||!TDMManager.isTerrorist(p)))for(int i=0;i<p.inventory.mainInventory.length;i++)if(isBombStack(p.inventory.mainInventory[i])){ItemStack removed=p.inventory.mainInventory[i];p.inventory.mainInventory[i]=null;if(state==BombRoundState.LIVE&&!hasWorldBomb(world))p.entityDropItem(removed,0);p.inventory.markDirty();p.inventoryContainer.detectAndSendChanges();}}
    private static boolean hasWorldBomb(World world){for(Object o:world.loadedEntityList)if(o instanceof EntityItem&&isBombStack(((EntityItem)o).getEntityItem()))return true;return bomb!=null;}
    public static void purgeBombObjectiveItems(World world){if(world==null)return;for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj==world)for(int i=0;i<p.inventory.mainInventory.length;i++)if(isBombStack(p.inventory.mainInventory[i]))p.inventory.mainInventory[i]=null;for(Object o:new ArrayList<Object>(world.loadedEntityList))if(o instanceof EntityItem&&isBombStack(((EntityItem)o).getEntityItem()))((EntityItem)o).setDead();}
    private static void purgeBombDropsNear(TrackedBomb b){for(Object o:new ArrayList<Object>(b.world.loadedEntityList))if(o instanceof EntityItem){EntityItem e=(EntityItem)o;if(isBombStack(e.getEntityItem())&&e.getDistanceSq(b.x+.5,b.y+.5,b.z+.5)<=16)e.setDead();}}
    private static void invalidateTrackedBomb(){if(bomb!=null)bomb.invalidated=true;bomb=null;pendingPlant=null;missingBombSince=-1L;missingBombWarningLogged=false;}
    private static void cleanupTransientState(World world){TrackedBomb tracked=bomb;invalidateTrackedBomb();if(tracked!=null&&world==tracked.world&&world.provider.dimensionId==tracked.dim&&HbmCsgoChargeIntegration.isCsgoCharge(world.getBlock(tracked.x,tracked.y,tracked.z)))world.setBlockToAir(tracked.x,tracked.y,tracked.z);eliminated.clear();stateEndTick=0;TDMSpectatorManager.restoreAll();TDMManager.clearPendingKitSelections();TDMManager.closeBombBuyGuis();}
    public static void cleanup(World world,boolean removeTracked){if(removeTracked)cleanupTransientState(world);else{bomb=null;pendingPlant=null;missingBombSince=-1L;missingBombWarningLogged=false;eliminated.clear();stateEndTick=0;}state=BombRoundState.DISABLED;}
    public static synchronized void onWorldUnload(World world){if(world!=null){TDMManager.resetTDMTransientPlayerState(world);if(bomb!=null&&bomb.world==world)cleanup(world,false);}}
    private static void broadcast(String s){for(EntityPlayerMP p:TDMManager.getOnlinePlayers())p.addChatMessage(new ChatComponentText(s));}
    private static String key(EntityPlayer p){return p.getCommandSenderName().toLowerCase();}
    private static final class TrackedBomb {final World world;final int dim,x,y,z,chargeMetadata,supportX,supportY,supportZ;final ForgeDirection attachmentDirection;final String site,planter;final TDMManager.Team team;final TDMManager.BombRole role;final Block block;final long plantTick,roundIdentity;Class<?> tileClass;Boolean previousStarted,currentStarted;Integer previousTimer,currentTimer;boolean disarmObserved,detonationObserved,invalidated;String runtimeResult,runtimePlayerUuid,runtimePlayerName;TrackedBomb(World w,int d,int x,int y,int z,String s,String p,TDMManager.Team t,TDMManager.BombRole r,Block b,int metadata,long tick){world=w;dim=d;this.x=x;this.y=y;this.z=z;site=s;planter=p;team=t;role=r;block=b;chargeMetadata=metadata;attachmentDirection=ForgeDirection.getOrientation(metadata);supportX=x-attachmentDirection.offsetX;supportY=y-attachmentDirection.offsetY;supportZ=z-attachmentDirection.offsetZ;plantTick=tick;roundIdentity=tick;}void observe(){HbmCsgoChargeIntegration.Snapshot snapshot=HbmCsgoChargeIntegration.snapshot(world,x,y,z);previousStarted=currentStarted;previousTimer=currentTimer;currentStarted=snapshot.started;currentTimer=snapshot.timer;if(snapshot.tileClass!=null)tileClass=snapshot.tileClass;if(XFConfig.tdmBombLifecycleDebug&&MainRegistry.logger!=null)MainRegistry.logger.info("HBM CSGO TDM lifecycle: coordinates=({}, {}, {}), started={} -> {}, timer={} -> {}",x,y,z,previousStarted,currentStarted,previousTimer,currentTimer);if(Boolean.TRUE.equals(previousStarted)&&Boolean.FALSE.equals(currentStarted)&&currentTimer!=null&&currentTimer.intValue()>0){disarmObserved=true;if(XFConfig.tdmBombLifecycleDebug&&MainRegistry.logger!=null)MainRegistry.logger.info("HBM CSGO TDM: started true -> false; disarm latched");}if(Boolean.TRUE.equals(currentStarted)&&currentTimer!=null&&currentTimer.intValue()<=1){detonationObserved=true;if(XFConfig.tdmBombLifecycleDebug&&MainRegistry.logger!=null)MainRegistry.logger.info("HBM CSGO TDM: detonation latched at timer={}",currentTimer);}}}
    private static final class PendingPlant{final EntityPlayer player;final int x,y,z;final long tick;PendingPlant(EntityPlayer p,int x,int y,int z,long t){player=p;this.x=x;this.y=y;this.z=z;tick=t;}}
}
