package com.hfr.tdm;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TDMHandler {

    private static final int RESPAWN_RETRY_TICKS = 20;
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

        String playerName = getKey(event.player);
        Integer ticksLeft = pendingRespawns.get(playerName);
        if (ticksLeft == null) return;

        if (!TDMManager.isEnabled(event.player.worldObj)) {
            pendingRespawns.remove(playerName);
            return;
        }

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

    private void queueRespawn(EntityPlayer player) {
        if (player.worldObj.isRemote) return;
        if (!TDMManager.isEnabled(player.worldObj)) return;

        pendingRespawns.put(getKey(player), RESPAWN_RETRY_TICKS);
    }

    private String getKey(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }
}
