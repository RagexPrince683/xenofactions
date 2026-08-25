package com.hfr.tdm;

import com.hfr.compat.HbmCsgoChargeIntegration;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Server-authoritative lifecycle for the short combat rounds on a bomb map. */
public final class TDMBombManager {
    public enum BombRoundState { DISABLED, PRE_ROUND, LIVE, BOMB_PLANTED, ROUND_END }
    public enum BombRoundWinReason { TERRORISTS_ELIMINATED, COUNTER_TERRORISTS_ELIMINATED, BOMB_DETONATED, BOMB_DEFUSED, TIME_EXPIRED }
    public static final int BUY_TIME_TICKS = TDMManager.BUY_TIME_TICKS;
    private static final int INTERMISSION_TICKS = 5 * 20;
    private static BombRoundState state = BombRoundState.DISABLED;
    private static long stateEndTick;
    private static final Set<String> eliminated = new HashSet<String>();
    private static TrackedBomb bomb;
    private static PendingPlant pendingPlant;

    private TDMBombManager() { }
    public static BombRoundState getState(){return state;}
    public static boolean isRoundActive(){return state==BombRoundState.LIVE||state==BombRoundState.BOMB_PLANTED;}
    public static String getPlantedSite(){return bomb==null?"":bomb.site;}
    public static int getRemainingSeconds(World w){return (state==BombRoundState.LIVE||state==BombRoundState.PRE_ROUND)?(int)Math.max(0,(stateEndTick-w.getTotalWorldTime()+19)/20):0;}
    public static boolean isEliminated(EntityPlayer p){return eliminated.contains(key(p));}

    public static void startMatch(World world){
        TDMData d=TDMData.get(world); d.redBombWins=d.blueBombWins=d.redBombLosses=d.blueBombLosses=0; d.markDirty();
        cleanup(world,true); beginBuyTime(world);
        if(!HbmCsgoChargeIntegration.isAvailable()) broadcast("Bomb mode requires HBM's hbm:tile.charge_c4csgo block.");
    }
    public static void tick(World world){
        if(!TDMManager.isEnabled(world)||!TDMManager.isBombMode(world)){cleanup(world,true);return;}
        long now=world.getTotalWorldTime();
        if(pendingPlant!=null&&now>pendingPlant.tick){PendingPlant p=pendingPlant;pendingPlant=null;if(p.player.worldObj==world&&HbmCsgoChargeIntegration.isCsgoCharge(world.getBlock(p.x,p.y,p.z)))acceptPlant(p.player,p.x,p.y,p.z);}
        if(state==BombRoundState.DISABLED)beginBuyTime(world);
        if(state==BombRoundState.PRE_ROUND&&now>=stateEndTick) startRound(world);
        else if(state==BombRoundState.LIVE&&now>=stateEndTick) completeRound(world,TDMManager.getCounterTerroristTeam(world),BombRoundWinReason.TIME_EXPIRED);
        else if(state==BombRoundState.ROUND_END&&now>=stateEndTick) beginBuyTime(world);
        TDMSpectatorManager.tick(world);
    }
    public static void beginBuyTime(World world){
        state=BombRoundState.PRE_ROUND;stateEndTick=world.getTotalWorldTime()+BUY_TIME_TICKS;eliminated.clear();bomb=null;TDMSpectatorManager.restoreAll();Random r=new Random();
        for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj.provider.dimensionId==world.provider.dimensionId){TDMManager.respawnPlayer(p,r);p.inventory.clearInventory(null,-1);for(int i=0;i<4;i++)p.inventory.armorInventory[i]=null;TDMManager.clearKitSelection(p);TDMManager.promptForKit(p);}
        TDMManager.sendStatusToAll(world);
    }
    public static void startRound(World world){
        if(world.isRemote||state!=BombRoundState.PRE_ROUND||TDMManager.isMapVoteActive(world))return;
        TDMManager.TDMMap map=TDMManager.getSelectedMapData(world);if(map==null||map.mode!=TDMManager.TDMGameMode.BOMB)return;
        int red=0,blue=0;for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj.provider.dimensionId==world.provider.dimensionId&&!TDMSpectatorManager.isObserving(p)&&!isEliminated(p)){TDMManager.Team t=TDMManager.getOrAssignPlayerTeam(p);if(t==TDMManager.Team.RED)red++;else if(t==TDMManager.Team.BLUE)blue++;}
        if(red==0||blue==0){beginBuyTime(world);return;}
        for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj.provider.dimensionId==world.provider.dimensionId){if(!TDMManager.hasSelectedKit(p)){TDMManager.Team t=TDMManager.getOrAssignPlayerTeam(p);int[] costs=TDMKitManager.getKitCosts(map.name,t);int fallback=-1;for(int i=0;i<costs.length;i++)if(!map.buyScoreEnabled||costs[i]==0){fallback=i;break;}if(fallback>=0)TDMManager.selectKit(p,fallback);else{p.addChatMessage(new ChatComponentText("No free kit is available for this round."));System.err.println("TDM bomb buy: no free kit for map "+map.name+" team "+t.name);}}com.hfr.packet.PacketDispatcher.wrapper.sendTo(new com.hfr.packet.effect.TDMKitGuiPacket("",new String[0]),p);}
        state=BombRoundState.LIVE;stateEndTick=world.getTotalWorldTime()+TDMManager.getEffectiveBombRoundTicks(world,map.name);assignBombToRandomTerrorist(world);TDMManager.sendStatusToAll(world);
    }
    private static boolean assignBombToRandomTerrorist(World world){
        ItemStack available=HbmCsgoChargeIntegration.createCsgoChargeStack();
        if(available==null){System.err.println("TDM bomb round: cannot assign hbm:tile.charge_c4csgo because its inventory item is unavailable.");return false;}
        TDMManager.Team terrorists=TDMManager.getTerroristTeam(world);List<EntityPlayerMP> eligible=new ArrayList<EntityPlayerMP>();
        for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p.worldObj.provider.dimensionId==world.provider.dimensionId&&TDMManager.getOrAssignPlayerTeam(p)==terrorists&&!TDMSpectatorManager.isObserving(p)&&!isEliminated(p))eligible.add(p);
        if(eligible.isEmpty()){System.err.println("TDM bomb round: cannot assign the CSGO bomb because no eligible Terrorist is online in dimension "+world.provider.dimensionId+".");return false;}
        Collections.shuffle(eligible,new Random());
        for(EntityPlayerMP p:eligible){ItemStack stack=HbmCsgoChargeIntegration.createCsgoChargeStack();if(stack!=null&&p.inventory.addItemStackToInventory(stack)){p.inventory.markDirty();p.inventoryContainer.detectAndSendChanges();p.addChatMessage(new ChatComponentText("You have the bomb."));return true;}}
        System.err.println("TDM bomb round: no eligible Terrorist has inventory space for the CSGO bomb in dimension "+world.provider.dimensionId+".");return false;
    }
    /** Called at LOWEST priority, after an uncancelled Forge placement has been accepted. */
    public static void recordAcceptedPlant(EntityPlayer player,Block block,int x,int y,int z){
        if(canPlant(player,block,x,y,z,false))pendingPlant=new PendingPlant(player,x,y,z,player.worldObj.getTotalWorldTime());
    }
    private static void acceptPlant(EntityPlayer player,int x,int y,int z){String site=TDMManager.getBombsiteAt(player.worldObj,player.dimension,x,y,z);
        bomb=new TrackedBomb(player.dimension,x,y,z,site,player.getCommandSenderName(),TDMManager.getOrAssignPlayerTeam(player),TDMManager.getBombRole(player));
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
    public static synchronized boolean tryDefuse(EntityPlayer player,int x,int y,int z){
        if(state!=BombRoundState.BOMB_PLANTED||bomb==null||player.dimension!=bomb.dim||x!=bomb.x||y!=bomb.y||z!=bomb.z||!TDMManager.isCounterTerrorist(player)||isEliminated(player)||TDMSpectatorManager.isObserving(player))return false;
        if(!HbmCsgoChargeIntegration.isCsgoCharge(player.worldObj.getBlock(x,y,z)))return false;
        TDMManager.awardDefuseBuyScore(player);player.worldObj.setBlockToAir(x,y,z);bomb=null;completeRound(player.worldObj,TDMManager.getCounterTerroristTeam(player.worldObj),BombRoundWinReason.BOMB_DEFUSED);return true;
    }
    public static void eliminate(EntityPlayer p){
        if(!TDMManager.isHardcoreRespawns(p.worldObj))return; eliminated.add(key(p));
        if(!TDMManager.isBombMode(p.worldObj)||!isRoundActive())return;
        TDMManager.Team ct=TDMManager.getCounterTerroristTeam(p.worldObj),tt=TDMManager.getTerroristTeam(p.worldObj);
        if(living(p.worldObj,ct)==0)completeRound(p.worldObj,tt,BombRoundWinReason.COUNTER_TERRORISTS_ELIMINATED);
        else if(state!=BombRoundState.BOMB_PLANTED&&living(p.worldObj,tt)==0)completeRound(p.worldObj,ct,BombRoundWinReason.TERRORISTS_ELIMINATED);
    }
    private static int living(World w,TDMManager.Team team){int n=0;for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(TDMManager.getOrAssignPlayerTeam(p)==team&&!isEliminated(p)&&!TDMSpectatorManager.isObserving(p))n++;return n;}
    public static synchronized void completeRound(World world,TDMManager.Team winner,BombRoundWinReason reason){
        if(state==BombRoundState.ROUND_END||state==BombRoundState.DISABLED)return; TDMData d=TDMData.get(world);
        if(winner==TDMManager.Team.RED){d.redBombWins++;d.blueBombLosses++;}else{d.blueBombWins++;d.redBombLosses++;} d.markDirty(); state=BombRoundState.ROUND_END;stateEndTick=world.getTotalWorldTime()+INTERMISSION_TICKS;
        TDMManager.BombRole role=TDMManager.getBombRole(world,winner); broadcast((role==TDMManager.BombRole.TERRORIST?"Terrorists":"Counter-Terrorists")+" win: "+reasonText(reason)+".");
        if((winner==TDMManager.Team.RED?d.redBombWins:d.blueBombWins)>=TDMManager.getEffectiveBombScoreLimit(world,TDMManager.getSelectedMap(world))){cleanup(world,true);TDMManager.startMapVote(world);}
        else TDMManager.sendStatusToAll(world);
    }
    private static String reasonText(BombRoundWinReason r){switch(r){case BOMB_DEFUSED:return "bomb defused";case BOMB_DETONATED:return "bomb detonated";case TERRORISTS_ELIMINATED:return "Terrorists eliminated";case COUNTER_TERRORISTS_ELIMINATED:return "Counter-Terrorists eliminated";default:return "time expired";}}
    public static void cleanup(World world,boolean removeTracked){if(removeTracked&&bomb!=null&&world.provider.dimensionId==bomb.dim&&HbmCsgoChargeIntegration.isCsgoCharge(world.getBlock(bomb.x,bomb.y,bomb.z)))world.setBlockToAir(bomb.x,bomb.y,bomb.z);bomb=null;pendingPlant=null;eliminated.clear();state=BombRoundState.DISABLED;stateEndTick=0;}
    private static void broadcast(String s){for(EntityPlayerMP p:TDMManager.getOnlinePlayers())p.addChatMessage(new ChatComponentText(s));}
    private static String key(EntityPlayer p){return p.getCommandSenderName().toLowerCase();}
    private static final class TrackedBomb {final int dim,x,y,z;final String site,planter;final TDMManager.Team team;final TDMManager.BombRole role;TrackedBomb(int d,int x,int y,int z,String s,String p,TDMManager.Team t,TDMManager.BombRole r){dim=d;this.x=x;this.y=y;this.z=z;site=s;planter=p;team=t;role=r;}}
    private static final class PendingPlant{final EntityPlayer player;final int x,y,z;final long tick;PendingPlant(EntityPlayer p,int x,int y,int z,long t){player=p;this.x=x;this.y=y;this.z=z;tick=t;}}
}
