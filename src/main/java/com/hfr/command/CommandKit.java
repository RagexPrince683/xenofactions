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
        return "/kit <blue|red> [map|global] | /kit remove <blue|red> <number> [map|global]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            removeKit(sender, args);
            return;
        }

        TDMManager.Team team = TDMManager.Team.fromName(args[0]);
        if (team == null) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[0]));
            return;
        }

        EntityPlayer player = getCommandSenderAsPlayer(sender);
        String mapName = args.length >= 2 ? normalizeKitMap(args[1]) : TDMManager.getSelectedMap(sender.getEntityWorld());
        int kitCount = TDMKitManager.addKit(mapName, team, player);
        String mapText = mapName.length() > 0 ? " for map " + mapName : " as a global fallback";
        sender.addChatMessage(new ChatComponentText("Saved " + team.name + " kit #" + kitCount + mapText + " from your inventory to tdm_kits.txt"));
    }

    private void removeKit(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("Usage: /kit remove <blue|red> <number> [map|global]"));
            return;
        }

        TDMManager.Team team = TDMManager.Team.fromName(args[1]);
        if (team == null) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[1]));
            return;
        }

        int kitNumber;
        try {
            kitNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText("Kit number must be a number from /kits."));
            return;
        }

        String mapName = args.length >= 4 ? normalizeKitMap(args[3]) : TDMManager.getSelectedMap(sender.getEntityWorld());
        if (!TDMKitManager.removeKit(mapName, team, kitNumber - 1)) {
            sender.addChatMessage(new ChatComponentText("No " + team.name + " kit #" + kitNumber + " exists for " + getMapDisplayName(mapName) + ". Use /kits " + getMapDisplayName(mapName) + " to list kits."));
            return;
        }

        sender.addChatMessage(new ChatComponentText("Removed " + team.name + " kit #" + kitNumber + " from " + getMapDisplayName(mapName) + "."));
    }

    private String normalizeKitMap(String mapName) {
        String normalized = TDMManager.normalizeMapName(mapName);
        return normalized.equals("global") ? "" : normalized;
    }

    private String getMapDisplayName(String mapName) {
        return mapName.length() == 0 ? "global" : mapName;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
