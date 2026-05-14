package com.hfr.tdm;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Random;

public class TDMHandler {

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!event.wasDeath) return;

        respawn(event.entityPlayer);
    }

    @SubscribeEvent
    public void onRespawn(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
        respawn(event.player);
    }

    private void respawn(EntityPlayer player) {
        if (!TDMManager.isEnabled(player.worldObj)) return;

        TDMManager.respawnPlayer(player, new Random());
    }
}
