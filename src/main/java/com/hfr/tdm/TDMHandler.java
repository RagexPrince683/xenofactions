package com.hfr.tdm;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TDMHandler {

    private static final int RESPAWN_RETRY_TICKS = 20;
    private static final int AUTO_BALANCE_INTERVAL_TICKS = 100;
    private long lastAutoBalanceTick = -1;
    private long lastRoundTick = -1;
    private final Map<String, Integer> pendingRespawns = new HashMap<String, Integer>();
    private final Random random = new Random();

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!event.wasDeath) return;

        queueRespawn(event.entityPlayer);
    }

    @SubscribeEvent
    public void onRespawn(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
        queueRespawn(event.player);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.worldObj.isRemote) return;

        TDMManager.tickKitSelection(event.player);

        if (!TDMManager.isEnabled(event.player.worldObj)) {
            pendingRespawns.remove(getKey(event.player));
            return;
        }

        runRoundTimer(event.player);
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
        if (attacker == null || attacker == victim) return;

        TDMManager.Team victimTeam = TDMManager.getOrAssignPlayerTeam(victim);
        TDMManager.Team attackerTeam = TDMManager.getOrAssignPlayerTeam(attacker);
        if (attackerTeam != null && attackerTeam != victimTeam) {
            TDMManager.addKillScore(victim.worldObj, attackerTeam);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.entityLiving.worldObj.isRemote) return;
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        if (!TDMManager.isEnabled(event.entityLiving.worldObj)) return;
        if (TDMManager.isFriendlyFireEnabled(event.entityLiving.worldObj)) return;

        EntityPlayer victim = (EntityPlayer) event.entityLiving;
        EntityPlayer attacker = getAttackingPlayer(event.source);
        if (attacker == null || attacker == victim) return;

        TDMManager.Team victimTeam = TDMManager.getPlayerTeam(victim.worldObj, victim.getCommandSenderName());
        TDMManager.Team attackerTeam = TDMManager.getPlayerTeam(victim.worldObj, attacker.getCommandSenderName());
        if (victimTeam != null && victimTeam == attackerTeam) {
            event.setCanceled(true);
        }
    }


    private void runRoundTimer(EntityPlayer player) {
        long worldTime = player.worldObj.getTotalWorldTime();
        if (lastRoundTick == worldTime) {
            return;
        }

        lastRoundTick = worldTime;
        TDMManager.tickRound(player.worldObj);
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

        pendingRespawns.put(getKey(player), RESPAWN_RETRY_TICKS);
    }

    private String getKey(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }
}
