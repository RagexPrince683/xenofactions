package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.util.UUID;

public class CommandMute extends CommandBase {

    @Override
    public String getCommandName() {
        return "xmute";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/xmute <player> <seconds|perm> [reason]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        EntityPlayerMP target;
        try {
            target = getPlayer(sender, args[0]);
        } catch (PlayerNotFoundException e) {
            sender.addChatMessage(new ChatComponentText("Player not found."));
            return;
        }

        String durationArg = args[1];
        int durationSeconds;

        if (durationArg.equalsIgnoreCase("perm")) {
            durationSeconds = -1;
        } else {
            try {
                durationSeconds = Integer.parseInt(durationArg);
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText("Invalid duration."));
                return;
            }
        }

        // Join full reason (supports spaces)
        String reason = "No reason provided";
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            reason = sb.toString().trim();
        }

        UUID uuid = target.getUniqueID();
        MuteManager.mute(uuid, durationSeconds, reason);

        if (durationSeconds < 0) {
            sender.addChatMessage(new ChatComponentText(
                    "Player " + target.getCommandSenderName() + " has been permanently muted."
            ));
        } else {
            sender.addChatMessage(new ChatComponentText(
                    "Player " + target.getCommandSenderName() + " has been muted for " + durationSeconds + " seconds."
            ));
        }

        target.addChatMessage(new ChatComponentText(
                "You have been muted. Reason: " + reason
        ));
    }
}
