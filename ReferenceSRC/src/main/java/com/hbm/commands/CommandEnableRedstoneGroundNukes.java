package com.hbm.commands;

import java.util.Locale;

import com.hbm.config.GeneralConfig;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/** Controls whether placed nuclear bombs respond to redstone power. */
public class CommandEnableRedstoneGroundNukes extends CommandBase {

	@Override
	public String getCommandName() {
		return "ntmenableredstonegroundnukes";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ntmenableredstonegroundnukes <true|false>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length != 1) {
			sendUsage(sender);
			return;
		}

		switch(args[0].toLowerCase(Locale.US)) {
			case "true":
				GeneralConfig.enableRedstoneGroundNukes = true;
				sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.GREEN + "Redstone detonation of ground nukes enabled."));
				return;
			case "false":
				GeneralConfig.enableRedstoneGroundNukes = false;
				sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "Redstone detonation of ground nukes disabled."));
				return;
			default:
				sendUsage(sender);
		}
	}

	private void sendUsage(ICommandSender sender) {
		sender.addChatMessage(new ChatComponentText(
			EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
	}
}
