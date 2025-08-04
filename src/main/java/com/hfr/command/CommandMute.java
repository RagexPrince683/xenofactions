package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandMute extends CommandBase {

    @Override
    public String getCommandName() {
        return "xmute";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/xmute <player> <duration> [reason]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        String playerName = args[0];
        String duration = args[1];
        String reason = args.length > 2 ? args[2] : "No reason provided";

        // Here you would implement the logic to mute the player
        // For example, you could store the mute information in a database or a file

        sender.addChatMessage(new ChatComponentText("Player " + playerName + " has been muted for " + duration + " seconds. Reason: " + reason));
    }


}
