package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.util.UUID;

public class CommandIgnore extends CommandBase {

    @Override
    public String getCommandName() {
        return "xignore";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/xignore <player>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        // Anyone can use this command; no permission required
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        EntityPlayerMP senderPlayer;
        EntityPlayerMP target;

        try {
            senderPlayer = getCommandSenderAsPlayer(sender);
            target = getPlayer(sender, args[0]);
        } catch (PlayerNotFoundException e) {
            sender.addChatMessage(new ChatComponentText("Player not found."));
            return;
        }

        UUID senderUUID = senderPlayer.getUniqueID();
        UUID targetUUID = target.getUniqueID();

        IgnoreManager.toggleIgnore(senderUUID, targetUUID);

        if (IgnoreManager.isIgnoring(senderUUID, targetUUID)) {
            sender.addChatMessage(new ChatComponentText("Now ignoring " + target.getCommandSenderName()));
        } else {
            sender.addChatMessage(new ChatComponentText("No longer ignoring " + target.getCommandSenderName()));
        }
    }
}
