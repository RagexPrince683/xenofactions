package com.hbm.commands;

import com.hbm.config.MiningConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandBedrockDrop extends CommandBase {

	@Override
	public String getCommandName() {
		return "hbmbedrockdrop";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/hbmbedrockdrop <add/remove/list/clear>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		if(args.length == 0) {
			sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
			return;
		}

		String sub = args[0].toLowerCase();

		try {

			// LIST
			if(sub.equals("list")) {

				if(MiningConfig.excavatorBedrockDrops.isEmpty()) {
					sender.addChatMessage(
						new ChatComponentText("No excavator bedrock drops.")
					);
					return;
				}

				sender.addChatMessage(
					new ChatComponentText("=== Excavator Bedrock Drops ===")
				);

				for(int i = 0; i < MiningConfig.excavatorBedrockDrops.size(); i++) {

					sender.addChatMessage(
						new ChatComponentText(
							i + ": " +
								MiningConfig.excavatorBedrockDrops.get(i)
						)
					);
				}

				return;
			}

			// CLEAR
			if(sub.equals("clear")) {

				MiningConfig.excavatorBedrockDrops.clear();

				sender.addChatMessage(
					new ChatComponentText(
						"Cleared all excavator bedrock drops."
					)
				);

				return;
			}

			// REMOVE
			if(sub.equals("remove")) {

				if(args.length != 2) {
					sender.addChatMessage(
						new ChatComponentText(
							"/hbmbedrockdrop remove <index>"
						)
					);
					return;
				}

				int index = Integer.parseInt(args[1]);

				if(index < 0 ||
					index >= MiningConfig.excavatorBedrockDrops.size()) {

					sender.addChatMessage(
						new ChatComponentText("Invalid index.")
					);
					return;
				}

				String removed =
					MiningConfig.excavatorBedrockDrops.remove(index);

				sender.addChatMessage(
					new ChatComponentText(
						"Removed: " + removed
					)
				);

				return;
			}

			// ADD
			if(sub.equals("add")) {

				if(args.length != 5) {
					sender.addChatMessage(
						new ChatComponentText(
							"/hbmbedrockdrop add <registry> <meta> <min> <max>"
						)
					);
					return;
				}

				String registry = args[1];
				int meta = Integer.parseInt(args[2]);
				int min = Integer.parseInt(args[3]);
				int max = Integer.parseInt(args[4]);

				String entry =
					registry + " " +
						meta + " " +
						min + " " +
						max;

				MiningConfig.excavatorBedrockDrops.add(entry);

				sender.addChatMessage(
					new ChatComponentText(
						"Added: " + entry
					)
				);

				return;
			}

			sender.addChatMessage(
				new ChatComponentText(
					"Unknown subcommand."
				)
			);

		} catch(Exception ex) {

			sender.addChatMessage(
				new ChatComponentText(
					"Invalid command arguments."
				)
			);
		}
	}
}
