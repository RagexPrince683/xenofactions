package com.hfr.command;

import com.hfr.tdm.TDMKitManager;
import com.hfr.tdm.TDMManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandKits extends CommandBase {

    @Override
    public String getCommandName() {
        return "kits";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/kits [map|global]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String mapName = args.length >= 1 ? normalizeKitMap(args[0]) : TDMManager.getSelectedMap(sender.getEntityWorld());
        String displayMap = mapName.length() == 0 ? "global" : mapName;

        sender.addChatMessage(new ChatComponentText("TDM kits for " + displayMap + ":"));
        sendTeamKits(sender, mapName, TDMManager.Team.RED);
        sendTeamKits(sender, mapName, TDMManager.Team.BLUE);

        if (mapName.length() > 0) {
            sender.addChatMessage(new ChatComponentText("Global fallback kits:"));
            sendTeamKits(sender, "", TDMManager.Team.RED);
            sendTeamKits(sender, "", TDMManager.Team.BLUE);
        }
    }

    private void sendTeamKits(ICommandSender sender, String mapName, TDMManager.Team team) {
        String[] names = TDMKitManager.getDirectKitNames(mapName, team);
        if (names.length == 0) {
            sender.addChatMessage(new ChatComponentText("  " + team.name + ": none"));
            return;
        }

        String message = "  " + team.name + ": ";
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                message += ", ";
            }
            message += (i + 1) + "=" + names[i];
        }
        sender.addChatMessage(new ChatComponentText(message));
    }

    private String normalizeKitMap(String mapName) {
        String normalized = TDMManager.normalizeMapName(mapName);
        return normalized.equals("global") ? "" : normalized;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
