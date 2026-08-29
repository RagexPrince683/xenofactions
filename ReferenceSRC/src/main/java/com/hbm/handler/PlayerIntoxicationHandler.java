package com.hbm.handler;

import com.hbm.config.VersatileConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public class PlayerIntoxicationHandler {

	@SubscribeEvent
	public void onPlayerTick(LivingUpdateEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;

			// Check intoxication level and decrease it gradually
			if (VersatileConfig.getIntoxicationLevel(player) > 0) {
				if (player.ticksExisted % 200 == 0) { // Every 10 seconds
					VersatileConfig.decreaseIntoxication(player, 1);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerSleep(PlayerSleepInBedEvent event) {
		EntityPlayer player = event.entityPlayer;

		// Reduce or reset intoxication when sleeping
		if (VersatileConfig.getIntoxicationLevel(player) > 0) {
			VersatileConfig.decreaseIntoxication(player, VersatileConfig.getIntoxicationLevel(player)); // Full reset
		}
	}

}
