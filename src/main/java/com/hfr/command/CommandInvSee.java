package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandInvSee extends CommandBase {

    @Override
    public String getCommandName() {
        return "invsee";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/invsee <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP level
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length != 1)
            throw new WrongUsageException(getCommandUsage(sender));

        EntityPlayerMP senderPlayer = getCommandSenderAsPlayer(sender);
        EntityPlayerMP target =
                MinecraftServer.getServer().getConfigurationManager()
                        .func_152612_a(args[0]); // get player by name

        if (target == null)
            throw new PlayerNotFoundException();

        senderPlayer.displayGUIChest(target.inventory);
    }
}