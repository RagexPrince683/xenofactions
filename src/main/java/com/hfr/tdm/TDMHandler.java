package com.hfr.tdm;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Random;

public class TDMHandler {

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!event.wasDeath) return;

        EntityPlayer newPlayer = event.entityPlayer;
        if (!TDMManager.isEnabled(newPlayer.worldObj)) return;

        TDMManager.SpawnPoint spawn = TDMManager.getRandomSpawn(newPlayer, new Random());
        if (spawn == null) return;

        TDMManager.respawnPlayer(newPlayer, new Random());
    }
}
