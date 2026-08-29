package com.hbm.commands;

import com.hbm.config.GeneralConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import java.util.Locale;

public class CommandEnableNukes extends CommandBase {

	//TODOne? this command but you can set a custom DATE and TIME for when nukes will be enabled,
	// and it will automatically enable/disable them at the specified time.
	// ALSO fix the fact this command says an unknown error occurred when you run it without true or false
	@Override
	public String getCommandName() {
		return "ntmenablenukes";
	}

	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "true/false";
	}



	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		if(args.length == 0) {
			sender.addChatMessage(new net.minecraft.util.ChatComponentText(
				EnumChatFormatting.RED +
					"Usage: /ntmenablenukes true/false OR /ntmenablenukes schedule true/false yyyy-MM-dd HH:mm"
			));
			return;
		}

		// normal instant toggle
		if(args.length == 1) {

			switch(args[0].toLowerCase(Locale.US)) {

				case "true":
					GeneralConfig.enableNuking = true;
					sender.addChatMessage(new net.minecraft.util.ChatComponentText(
						EnumChatFormatting.GREEN + "Nukes enabled."
					));
					return;

				case "false":
					GeneralConfig.enableNuking = false;
					sender.addChatMessage(new net.minecraft.util.ChatComponentText(
						EnumChatFormatting.RED + "Nukes disabled."
					));
					return;
			}
		}

		// scheduled toggle
		if(args.length == 4 && args[0].equalsIgnoreCase("schedule")) {

			try {

				boolean enable = Boolean.parseBoolean(args[1]);

				String dateTime = args[2] + " " + args[3];

				java.text.SimpleDateFormat format =
					new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");

				long timestamp = format.parse(dateTime).getTime();

				GeneralConfig.scheduledNukeTime = timestamp;
				GeneralConfig.scheduledNukeValue = enable;

				sender.addChatMessage(new net.minecraft.util.ChatComponentText(
					EnumChatFormatting.YELLOW +
						"Nukes scheduled to be " +
						(enable ? "ENABLED" : "DISABLED") +
						" at " + dateTime
				));

			} catch(Exception ex) {

				sender.addChatMessage(new net.minecraft.util.ChatComponentText(
					EnumChatFormatting.RED +
						"Invalid format. Example: /ntmenablenukes schedule true 2026-05-30 18:30"
				));
			}

			return;
		}

		sender.addChatMessage(new net.minecraft.util.ChatComponentText(
			EnumChatFormatting.RED + "Invalid command usage."
		));
	}


}
