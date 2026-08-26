package com.hfr.tdm;

import com.hfr.compat.HbmCsgoChargeIntegration;
import com.hfr.items.ModItems;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TDMHandler {

    private static final int RESPAWN_RETRY_TICKS = 2;
    private static final int AUTO_BALANCE_INTERVAL_TICKS = 100;
    private static final int TEAM_CHANGE_REMINDER_INTERVAL_TICKS = 8 * 60 * 20;
    private long lastAutoBalanceTick = -1;
    private long lastRoundTick = -1;
    private static final class PendingRespawn { int ticks; TDMManager.KitSelectionContext context; PendingRespawn(int ticks,TDMManager.KitSelectionContext context){this.ticks=ticks;this.context=context;} }
    private final Map<String, PendingRespawn> pendingRespawns = new HashMap<String, PendingRespawn>();
    private final Random random = new Random();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            HbmCsgoChargeIntegration.pollBombResults();
            TDMServerTaskQueue.runScheduledTasks();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && !event.world.isRemote) {
            TDMBombManager.onWorldUnload(event.world);
        }
        if (event.world != null && !event.world.isRemote
                && event.world.provider.dimensionId == 0) {
            TDMServerTaskQueue.clear();
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!event.wasDeath) return;

        TDMManager.cancelKitSelection(event.entityPlayer);

        if (TDMBombManager.isEliminated(event.original) && TDMManager.isHardcoreRespawns(event.entityPlayer.worldObj)) TDMSpectatorManager.observe(event.entityPlayer);
        // PlayerRespawnEvent owns non-hardcore per-life setup; Clone is too early for a gameplay GUI.
    }

    @SubscribeEvent
    public void onRespawn(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
        TDMManager.cancelKitSelection(event.player);
        if (TDMManager.isEnabled(event.player.worldObj) && TDMManager.isBombMode(event.player.worldObj)
                && TDMManager.isHardcoreRespawns(event.player.worldObj)
                && TDMBombManager.getState() == TDMBombManager.BombRoundState.PRE_ROUND) {
            TDMSpectatorManager.restore(event.player);
            if (TDMManager.respawnPlayer(event.player, random)) {
                TDMManager.enrollGlobalBuyProtection(event.player);
                TDMManager.promptForKit(event.player, TDMManager.KitSelectionContext.BUY_PHASE);
            } else queueRespawn(event.player);
            return;
        }
        if (TDMBombManager.isEliminated(event.player) && TDMManager.isHardcoreRespawns(event.player.worldObj)) TDMSpectatorManager.observe(event.player);
        else queueRespawn(event.player);
    }

    @SubscribeEvent
    public void onLogout(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        pendingRespawns.remove(getKey(event.player));
        TDMSpectatorManager.forget(event.player);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.worldObj.isRemote) return;

        TDMManager.tickKitSelection(event.player);

        if (!TDMManager.isEnabled(event.player.worldObj)) {
            pendingRespawns.remove(getKey(event.player));
            TDMSpectatorManager.restore(event.player);
            return;
        }

        runRoundTimer(event.player);
        sendTeamChangeReminder(event.player);
        runAutoBalance(event.player);

        String playerName = getKey(event.player);
        PendingRespawn pending = pendingRespawns.get(playerName);
        if (pending == null) return;

        if (!TDMManager.isAliveForTDM(event.player)) return;
        if (TDMManager.respawnPlayer(event.player, random)) {
            pendingRespawns.remove(playerName);
            if(pending.context==TDMManager.KitSelectionContext.BUY_PHASE)TDMManager.enrollGlobalBuyProtection(event.player);
            TDMManager.promptForKit(event.player, pending.context);
        } else if (pending.ticks <= 1) {
            pendingRespawns.remove(playerName);
            if(pending.context==TDMManager.KitSelectionContext.BUY_PHASE)TDMManager.enrollGlobalBuyProtection(event.player);
            TDMManager.promptForKit(event.player, pending.context);
        } else {
            pending.ticks--;
        }
    }


    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.entityLiving.worldObj.isRemote) return;
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        if (!TDMManager.isEnabled(event.entityLiving.worldObj)) return;
        if (TDMManager.isMapVoteActive(event.entityLiving.worldObj)) return;

        EntityPlayer victim = (EntityPlayer) event.entityLiving;
        EntityPlayer attacker = getAttackingPlayer(event.source);
        if (attacker != null && attacker != victim) {
            TDMManager.Team victimTeam = TDMManager.getOrAssignPlayerTeam(victim);
            TDMManager.Team attackerTeam = TDMManager.getOrAssignPlayerTeam(attacker);
            if (attackerTeam != null && attackerTeam != victimTeam) {
                TDMManager.addKillScore(victim.worldObj, attackerTeam);
                TDMManager.recordKill(victim.worldObj, attacker.getCommandSenderName());
                TDMManager.recordDeath(victim.worldObj, victim.getCommandSenderName());
                TDMManager.awardKillBuyScore(attacker);
            }
        }
        TDMBombManager.eliminate(victim);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.entityLiving.worldObj.isRemote) return;
        EntityPlayer protectedAttacker=getAttackingPlayer(event.source);
        if(protectedAttacker!=null&&(TDMManager.isGlobalBombBuyPeriod(protectedAttacker)||TDMManager.hasKitSelectionProtection(protectedAttacker)||TDMSpectatorManager.isObserving(protectedAttacker))){event.setCanceled(true);return;}
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        if (!TDMManager.isEnabled(event.entityLiving.worldObj)) return;
        EntityPlayer victim = (EntityPlayer) event.entityLiving;
        if(TDMManager.hasKitSelectionProtection(victim)){event.setCanceled(true);return;}
        if (TDMManager.isBombMode(event.entityLiving.worldObj) && !TDMBombManager.isRoundActive()) { event.setCanceled(true); return; }
        if (TDMManager.isFriendlyFireEnabled(event.entityLiving.worldObj)) return;
        EntityPlayer attacker = getAttackingPlayer(event.source);
        if (TDMSpectatorManager.isObserving(victim) || (attacker != null && TDMSpectatorManager.isObserving(attacker))) { event.setCanceled(true); return; }
        if (attacker == null || attacker == victim) return;

        TDMManager.Team victimTeam = TDMManager.getPlayerTeam(victim.worldObj, victim.getCommandSenderName());
        TDMManager.Team attackerTeam = TDMManager.getPlayerTeam(victim.worldObj, attacker.getCommandSenderName());
        if (victimTeam != null && victimTeam == attackerTeam) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictAttack(AttackEntityEvent event){if(TDMManager.isGlobalBombBuyPeriod(event.entityPlayer)||TDMManager.hasRespawnLockProtection(event.entityPlayer)||TDMSpectatorManager.isObserving(event.entityPlayer))event.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictEntityInteract(EntityInteractEvent event){if(TDMManager.isGlobalBombBuyPeriod(event.entityPlayer)||TDMManager.hasRespawnLockProtection(event.entityPlayer)||TDMSpectatorManager.isObserving(event.entityPlayer))event.setCanceled(true);}

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictPlace(BlockEvent.PlaceEvent event) {
        if (event.world.isRemote) return;
        if (TDMManager.hasKitSelectionProtection(event.player)||TDMSpectatorManager.isObserving(event.player)
                || TDMBombManager.shouldRestrictWorldInteraction(event.world)
                || !TDMBombManager.canPlant(event.player, event.placedBlock, event.x, event.y, event.z, true)) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=false)
    public void acceptedPlace(BlockEvent.PlaceEvent event){if(!event.world.isRemote)TDMBombManager.recordAcceptedPlant(event.player,event.placedBlock,event.x,event.y,event.z);}
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (isWorldBorderAdminWand(player)) return;
        if (TDMManager.hasKitSelectionProtection(player)||TDMSpectatorManager.isObserving(player)
                || TDMBombManager.shouldRestrictWorldInteraction(event.world)) {
            event.setCanceled(true);
        } else if (TDMBombManager.isTrackedBomb(event.world, event.x, event.y, event.z)) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (TDMManager.hasKitSelectionProtection(player)||TDMSpectatorManager.isObserving(player)) {
            event.setCanceled(true);
            return;
        }
        if (isWorldBorderAdminWand(player)) return;
        if (TDMBombManager.shouldRestrictWorldInteraction(player.worldObj)) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictPickup(EntityItemPickupEvent event){if(TDMManager.isGlobalBombBuyPeriod(event.entityPlayer)||TDMManager.hasKitSelectionProtection(event.entityPlayer)){event.setCanceled(true);return;}if(TDMBombManager.isBombStack(event.item.getEntityItem())&&(!TDMManager.isTerrorist(event.entityPlayer)||TDMBombManager.getState()!=TDMBombManager.BombRoundState.LIVE)){event.setCanceled(true);if(TDMBombManager.getState()!=TDMBombManager.BombRoundState.LIVE)event.item.setDead();return;}if(TDMSpectatorManager.isObserving(event.entityPlayer))event.setCanceled(true);}

    private boolean isWorldBorderAdminWand(EntityPlayer player) {
        return player != null
                && player.getHeldItem() != null
                && player.getHeldItem().getItem() == ModItems.world_border_wand
                && player.canCommandSenderUseCommand(3, "xclowder");
    }


    private void runRoundTimer(EntityPlayer player) {
        long worldTime = player.worldObj.getTotalWorldTime();
        if (lastRoundTick == worldTime) {
            return;
        }

        lastRoundTick = worldTime;
        TDMManager.tickRound(player.worldObj);
    }

    private void sendTeamChangeReminder(EntityPlayer player) {
        long worldTime = player.worldObj.getTotalWorldTime();
        if (worldTime > 0 && worldTime % TEAM_CHANGE_REMINDER_INTERVAL_TICKS == 0) {
            player.addChatMessage(new net.minecraft.util.ChatComponentText("Open the TDM menu from the HUD button to change teams."));
        }
    }

    private void runAutoBalance(EntityPlayer player) {
        if (!TDMManager.isAutoBalanceEnabled(player.worldObj)) {
            return;
        }

        long worldTime = player.worldObj.getTotalWorldTime();
        if (lastAutoBalanceTick == worldTime || worldTime % AUTO_BALANCE_INTERVAL_TICKS != 0) {
            return;
        }

        lastAutoBalanceTick = worldTime;
        TDMManager.balanceTeams(player.worldObj);
    }

    private EntityPlayer getAttackingPlayer(DamageSource source) {
        if (source == null) {
            return null;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof EntityPlayer) {
            return (EntityPlayer) attacker;
        }

        attacker = source.getSourceOfDamage();
        if (attacker instanceof EntityPlayer) {
            return (EntityPlayer) attacker;
        }

        return null;
    }

    private void queueRespawn(EntityPlayer player) {
        if (player.worldObj.isRemote) return;
        if (!TDMManager.isEnabled(player.worldObj)) return;
        if (!TDMManager.isAliveForTDM(player)) return;
        if (TDMManager.isHardcoreRespawns(player.worldObj) && TDMBombManager.isEliminated(player)) { TDMSpectatorManager.observe(player); return; }

        TDMManager.KitSelectionContext context=TDMManager.isBombMode(player.worldObj)&&TDMManager.isHardcoreRespawns(player.worldObj)&&TDMBombManager.getState()==TDMBombManager.BombRoundState.PRE_ROUND?TDMManager.KitSelectionContext.BUY_PHASE:TDMManager.KitSelectionContext.RESPAWN_LOCK;
        pendingRespawns.put(getKey(player),new PendingRespawn(RESPAWN_RETRY_TICKS,context));
    }

    private String getKey(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }
}
