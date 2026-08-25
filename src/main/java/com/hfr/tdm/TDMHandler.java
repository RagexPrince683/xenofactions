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
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TDMHandler {

    private static final int RESPAWN_RETRY_TICKS = 20;
    private static final int AUTO_BALANCE_INTERVAL_TICKS = 100;
    private static final int TEAM_CHANGE_REMINDER_INTERVAL_TICKS = 8 * 60 * 20;
    private long lastAutoBalanceTick = -1;
    private long lastRoundTick = -1;
    private final Map<String, Integer> pendingRespawns = new HashMap<String, Integer>();
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

        if (TDMBombManager.isEliminated(event.original) && TDMManager.isHardcoreRespawns(event.entityPlayer.worldObj)) TDMSpectatorManager.observe(event.entityPlayer);
        else queueRespawn(event.entityPlayer);
    }

    @SubscribeEvent
    public void onRespawn(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
        if (TDMBombManager.isEliminated(event.player) && TDMManager.isHardcoreRespawns(event.player.worldObj)) TDMSpectatorManager.observe(event.player);
        else queueRespawn(event.player);
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
        Integer ticksLeft = pendingRespawns.get(playerName);
        if (ticksLeft == null) return;

        if (TDMManager.respawnPlayer(event.player, random)) {
            pendingRespawns.remove(playerName);
            TDMManager.promptForKit(event.player);
        } else if (ticksLeft <= 1) {
            pendingRespawns.remove(playerName);
            TDMManager.promptForKit(event.player);
        } else {
            pendingRespawns.put(playerName, ticksLeft - 1);
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
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        if (!TDMManager.isEnabled(event.entityLiving.worldObj)) return;
        if (TDMManager.isBombMode(event.entityLiving.worldObj) && !TDMBombManager.isRoundActive()) { event.setCanceled(true); return; }
        if (TDMManager.isFriendlyFireEnabled(event.entityLiving.worldObj)) return;

        EntityPlayer victim = (EntityPlayer) event.entityLiving;
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
    public void restrictPlace(BlockEvent.PlaceEvent event) {
        if (event.world.isRemote) return;
        if (TDMSpectatorManager.isObserving(event.player)
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
        if (TDMSpectatorManager.isObserving(player)
                || TDMBombManager.shouldRestrictWorldInteraction(event.world)) {
            event.setCanceled(true);
        } else if (TDMBombManager.isTrackedBomb(event.world, event.x, event.y, event.z)) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (TDMSpectatorManager.isObserving(player)) {
            event.setCanceled(true);
            return;
        }
        if (isWorldBorderAdminWand(player)) return;
        if (TDMBombManager.shouldRestrictWorldInteraction(player.worldObj)) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void restrictPickup(EntityItemPickupEvent event){if(TDMSpectatorManager.isObserving(event.entityPlayer))event.setCanceled(true);}

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
        if (TDMManager.isHardcoreRespawns(player.worldObj) && TDMBombManager.isEliminated(player)) { TDMSpectatorManager.observe(player); return; }

        pendingRespawns.put(getKey(player), RESPAWN_RETRY_TICKS);
    }

    private String getKey(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }
}
