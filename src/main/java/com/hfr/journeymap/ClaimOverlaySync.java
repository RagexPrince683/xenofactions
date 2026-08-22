package com.hfr.journeymap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hfr.clowder.ClaimOverlayData;
import com.hfr.clowder.ClaimOverlayData.Claim;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.ClaimOverlayPacket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/** Debounced server-to-client JourneyMap claim snapshot synchronization. */
public class ClaimOverlaySync {
	private static final Set<Integer> DIRTY = new HashSet<Integer>();
	private static boolean allDirty;
	private static int debounce, generation;
	public static synchronized void markAllDirty() { allDirty = true; debounce = 20; }
	public static synchronized void markDirty(int dimension) { DIRTY.add(Integer.valueOf(dimension)); debounce = 20; }
	public static synchronized void resetWorldState() { DIRTY.clear(); allDirty = true; debounce = 0; generation = 0; }

	@SubscribeEvent public void login(PlayerLoggedInEvent event) { if(event.player instanceof EntityPlayerMP) send((EntityPlayerMP)event.player); }
	@SubscribeEvent public void changed(PlayerChangedDimensionEvent event) { if(event.player instanceof EntityPlayerMP) send((EntityPlayerMP)event.player); }
	@SubscribeEvent public void respawn(PlayerRespawnEvent event) { if(event.player instanceof EntityPlayerMP) send((EntityPlayerMP)event.player); }
	@SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
		if(event.phase != TickEvent.Phase.END || debounce <= 0 || --debounce > 0) return;
		MinecraftServer server = MinecraftServer.getServer(); if(server == null) return;
		for(Object object : server.getConfigurationManager().playerEntityList) {
			EntityPlayerMP player = (EntityPlayerMP)object;
			if(allDirty || DIRTY.contains(Integer.valueOf(player.dimension))) send(player);
		}
		DIRTY.clear(); allDirty = false;
	}
	private static void send(EntityPlayerMP player) {
		List<Claim> claims = ClaimOverlayData.snapshot(player.dimension);
		int parts = Math.max(1, (claims.size() + ClaimOverlayPacket.MAX_CLAIMS - 1) / ClaimOverlayPacket.MAX_CLAIMS);
		int id = ++generation;
		for(int part = 0; part < parts; part++) {
			int from = part * ClaimOverlayPacket.MAX_CLAIMS, to = Math.min(claims.size(), from + ClaimOverlayPacket.MAX_CLAIMS);
			PacketDispatcher.wrapper.sendTo(new ClaimOverlayPacket(player.dimension, id, part, parts, claims.subList(from, to)), player);
		}
	}
}
