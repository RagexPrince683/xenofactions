package com.hfr.command;

import com.hfr.tdm.TDMKitManager;
import com.hfr.tdm.TDMManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class CommandKit extends CommandBase {

    @Override
    public String getCommandName() {
        return "kit";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kit <blue|red>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        TDMManager.Team team = TDMManager.Team.fromName(args[0]);
        if (team == null) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[0]));
            return;
        }

        EntityPlayer player = getCommandSenderAsPlayer(sender);
        int kitCount = TDMKitManager.addKit(team, player);
        sender.addChatMessage(new ChatComponentText("Saved " + team.name + " kit #" + kitCount + " from your inventory to tdm_kits.txt"));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
