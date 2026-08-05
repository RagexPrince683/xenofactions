package com.hfr.stonedrops;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.StoneDropSnapshotPacket;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public final class StoneDropSnapshotSync {
    private StoneDropSnapshotSync() { }
    @SubscribeEvent public void login(PlayerLoggedInEvent event) { if (event.player instanceof EntityPlayerMP) send((EntityPlayerMP) event.player); }
    @SubscribeEvent public void changed(PlayerChangedDimensionEvent event) { if (event.player instanceof EntityPlayerMP) send((EntityPlayerMP) event.player); }
    public static void send(EntityPlayerMP player) { PacketDispatcher.wrapper.sendTo(new StoneDropSnapshotPacket(StoneDropDisplaySnapshot.fromRuntime()), player); }
    public static void sendToAll() { MinecraftServer s = MinecraftServer.getServer(); if (s == null || s.getConfigurationManager() == null) return; for (Object o : s.getConfigurationManager().playerEntityList) if (o instanceof EntityPlayerMP) send((EntityPlayerMP) o); }
}
