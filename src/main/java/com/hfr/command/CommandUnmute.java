package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandUnmute extends CommandBase {

    public String getCommandName() {
        return "xunmute";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/xunmute <player>";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: " + getCommandUsage(sender));
            return;
        }

        String playerName = args[0];
        // Logic to unmute the player
        MuteManager.unmute(playerName);
        sender.addChatMessage(new ChatComponentText("Player " + playerName + " has been unmuted."));
    }
}
