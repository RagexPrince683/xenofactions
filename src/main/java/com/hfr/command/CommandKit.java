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
        return "/kit <blue|red> [map]";
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
        String mapName = args.length >= 2 ? TDMManager.normalizeMapName(args[1]) : TDMManager.getSelectedMap(sender.getEntityWorld());
        int kitCount = TDMKitManager.addKit(mapName, team, player);
        String mapText = mapName.length() > 0 ? " for map " + mapName : " as a global fallback";
        sender.addChatMessage(new ChatComponentText("Saved " + team.name + " kit #" + kitCount + mapText + " from your inventory to tdm_kits.txt"));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
