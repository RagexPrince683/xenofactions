package com.hfr.command;

import com.hfr.tdm.TDMManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class CommandTDM extends CommandBase {

    @Override
    public String getCommandName() {
        return "tdm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tdm <toggle|addspawn|clear>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length == 0) return;

        if (args[0].equalsIgnoreCase("toggle")) {
            TDMManager.tdmEnabled = !TDMManager.tdmEnabled;
            sender.addChatMessage(new ChatComponentText("TDM: " + TDMManager.tdmEnabled));
        }

        if (args[0].equalsIgnoreCase("addspawn")) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);

            TDMManager.spawns.add(new TDMManager.SpawnPoint(
                    player.dimension,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ
            ));

            sender.addChatMessage(new ChatComponentText("Spawn added. Total: " + TDMManager.spawns.size()));
        }

        if (args[0].equalsIgnoreCase("clear")) {
            TDMManager.spawns.clear();
            sender.addChatMessage(new ChatComponentText("Spawns cleared"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
