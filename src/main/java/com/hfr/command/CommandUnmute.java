package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.util.UUID;

public class CommandUnmute extends CommandBase {

    @Override
    public String getCommandName() {
        return "xunmute";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/xunmute <player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length < 1) {
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

        UUID uuid = target.getUniqueID();
        MuteManager.unmute(uuid);

        sender.addChatMessage(new ChatComponentText(
                "Player " + target.getCommandSenderName() + " has been unmuted."
        ));

        target.addChatMessage(new ChatComponentText(
                "You have been unmuted."
        ));
    }
}
