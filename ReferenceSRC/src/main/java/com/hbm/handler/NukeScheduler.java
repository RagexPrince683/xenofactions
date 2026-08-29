package com.hbm.handler;

import com.hbm.config.GeneralConfig;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class NukeScheduler {

	private int tickTimer = 0;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {

		if(event.phase != TickEvent.Phase.END)
			return;

		// Check every 5 seconds
		tickTimer++;

		if(tickTimer < 100)
			return;

		tickTimer = 0;

		if(GeneralConfig.scheduledNukeTime == null)
			return;

		long now = System.currentTimeMillis();

		if(now >= GeneralConfig.scheduledNukeTime) {

			GeneralConfig.enableNuking =
				GeneralConfig.scheduledNukeValue;

			boolean enabled = GeneralConfig.enableNuking;

			MinecraftServer server =
				MinecraftServer.getServer();

			if(server != null) {

				// Broadcast message
				server.getConfigurationManager()
					.sendChatMsg(new ChatComponentText(
						(enabled
							? EnumChatFormatting.DARK_RED + "☢ WARNING ☢ "
							: EnumChatFormatting.GREEN + "☢ NOTICE ☢ ")
							+ EnumChatFormatting.GOLD
							+ "Nukes have been "
							+ (enabled
							? EnumChatFormatting.RED + "ENABLED"
							: EnumChatFormatting.GREEN + "DISABLED")
					));

				// Play sound for everyone
				server.getConfigurationManager()
					.playerEntityList
					.forEach(obj -> {

						net.minecraft.entity.player.EntityPlayerMP player =
							(net.minecraft.entity.player.EntityPlayerMP)obj;

						player.worldObj.playSoundAtEntity(
							player,
							enabled
								? "hbm:alarm.airRaid"
								: "mob.wither.death",
							50.0F, // volume
							0.8F   // pitch
						);
					});
			}

			System.out.println(
				"[HBM] Nukes automatically "
					+ (enabled ? "enabled" : "disabled")
			);

			// Clear schedule
			GeneralConfig.scheduledNukeTime = null;
			GeneralConfig.scheduledNukeValue = null;
		}
	}
}
