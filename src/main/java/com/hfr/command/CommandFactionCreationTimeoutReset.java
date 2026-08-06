package com.hfr.command;

import java.util.List;

import com.hfr.clowder.FactionCreationCooldownData;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandFactionCreationTimeoutReset extends CommandBase {

    @Override
    public String getCommandName() {
        return "xcfactiontimeoutcreationreset";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/xcfactiontimeoutcreationreset <playername>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(args.length != 1 || !args[0].matches("[A-Za-z0-9_]{1,16}"))
            throw new WrongUsageException(getCommandUsage(sender));

        FactionCreationCooldownData.ClearResult result = FactionCreationCooldownData.clearPlayerName(args[0]);
        if(result.ambiguous) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Player name is ambiguous; no faction creation cooldown was reset."));
            return;
        }
        if(!result.resolved) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Could not resolve player '" + args[0] + "'."));
            return;
        }

        String name = result.name == null || result.name.isEmpty() ? args[0] : result.name;
        if(result.removedEntries == 0) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + name + " has no active faction creation cooldown."));
            return;
        }

        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Reset faction creation cooldown for " + name + "."));
        EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().func_152612_a(name);
        if(target != null)
            target.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "An admin reset your faction creation cooldown."));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if(args.length == 1)
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        return null;
    }
}
