package com.hfr.command;

import com.hfr.tdm.TDMManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class CommandTDM extends CommandBase {

    @Override
    public String getCommandName() {
        return "tdm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tdm <vote <map>|maps|toggle|friendlyfire <on|off>|autobalance <on|off|now>|addspawn <red|blue>|map <create|delete|select|addspawn|clearspawns|list> ...|setteam <player> <red|blue>|clear>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        World world = sender.getEntityWorld();

        if (args[0].equalsIgnoreCase("maps") || args[0].equalsIgnoreCase("listmaps")) {
            sendMapList(sender, world);
            return;
        }

        if (args[0].equalsIgnoreCase("vote")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm vote <map>"));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            String votedMap = TDMManager.voteForMap(world, player.getCommandSenderName(), args[1]);
            if (votedMap == null) {
                sender.addChatMessage(new ChatComponentText("Unable to vote. A TDM map vote must be active and the map must exist."));
                return;
            }

            sender.addChatMessage(new ChatComponentText("Voted for TDM map " + votedMap + "."));
            sendVoteCounts(sender, world);
            return;
        }

        if (!isAdmin(sender)) {
            sender.addChatMessage(new ChatComponentText("You can use /tdm maps or /tdm vote <map>."));
            return;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            boolean enabled = TDMManager.toggle(world);
            sender.addChatMessage(new ChatComponentText("TDM: " + enabled));
            return;
        }

        if (args[0].equalsIgnoreCase("friendlyfire")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Friendly fire is " + TDMManager.isFriendlyFireEnabled(world) + ". Usage: /tdm friendlyfire <on|off>"));
                return;
            }

            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm friendlyfire <on|off>"));
                return;
            }

            TDMManager.setFriendlyFireEnabled(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("TDM friendly fire damage: " + (enabled.booleanValue() ? "on" : "off")));
            return;
        }

        if (args[0].equalsIgnoreCase("autobalance")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Auto balance is " + TDMManager.isAutoBalanceEnabled(world) + ". Usage: /tdm autobalance <on|off|now>"));
                return;
            }

            if (args[1].equalsIgnoreCase("now")) {
                int moved = TDMManager.balanceTeams(world);
                sender.addChatMessage(new ChatComponentText("TDM team balance complete. Players moved: " + moved));
                return;
            }

            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm autobalance <on|off|now>"));
                return;
            }

            TDMManager.setAutoBalanceEnabled(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("TDM auto balance: " + (enabled.booleanValue() ? "on" : "off")));
            return;
        }

        if (args[0].equalsIgnoreCase("map")) {
            processMapCommand(sender, args, world);
            return;
        }

        if (args[0].equalsIgnoreCase("addspawn")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm addspawn <red|blue>"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[1]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[1]));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            TDMManager.addSpawn(
                    world,
                    team,
                    player.dimension,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ
            );

            sender.addChatMessage(new ChatComponentText(
                    "Legacy spawn added for " + team.name + ". Total: " + TDMManager.getSpawnCount(world)
                            + " (red: " + TDMManager.getSpawnCount(world, TDMManager.Team.RED)
                            + ", blue: " + TDMManager.getSpawnCount(world, TDMManager.Team.BLUE) + ")"
            ));
            return;
        }

        if (args[0].equalsIgnoreCase("setteam")) {
            if (args.length < 3) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm setteam <player> <red|blue>"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[2]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[2]));
                return;
            }

            TDMManager.setPlayerTeam(world, args[1], team);
            sender.addChatMessage(new ChatComponentText(args[1] + " assigned to " + team.name));
            return;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            TDMManager.clearSpawns(world);
            sender.addChatMessage(new ChatComponentText("Legacy TDM spawns cleared"));
            return;
        }

        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
    }

    private void processMapCommand(ICommandSender sender, String[] args, World world) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm map <create|delete|select|addspawn|clearspawns|list>"));
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            sendMapList(sender, world);
            return;
        }

        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm map " + args[1] + " <map>"));
            return;
        }

        String mapName = TDMManager.normalizeMapName(args[2]);
        if (mapName.length() == 0) {
            sender.addChatMessage(new ChatComponentText("Map name cannot be empty."));
            return;
        }

        if (args[1].equalsIgnoreCase("create")) {
            if (!TDMManager.createMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("TDM map already exists or has an invalid name: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Created TDM map: " + mapName));
            return;
        }

        if (args[1].equalsIgnoreCase("delete")) {
            if (!TDMManager.deleteMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Deleted TDM map: " + mapName));
            return;
        }

        if (args[1].equalsIgnoreCase("select")) {
            if (!TDMManager.selectMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Selected TDM map: " + mapName));
            return;
        }

        if (args[1].equalsIgnoreCase("clearspawns")) {
            if (!TDMManager.clearMapSpawns(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Cleared spawns for TDM map: " + mapName));
            return;
        }

        if (args[1].equalsIgnoreCase("addspawn")) {
            if (args.length < 4) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map addspawn <map> <red|blue>"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[3]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[3]));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            TDMManager.addMapSpawn(world, mapName, team, player.dimension, (int) player.posX, (int) player.posY, (int) player.posZ);
            sender.addChatMessage(new ChatComponentText(
                    "Spawn added for " + team.name + " on map " + mapName + ". Total: " + TDMManager.getMapSpawnCount(world, mapName)
                            + " (red: " + TDMManager.getMapSpawnCount(world, mapName, TDMManager.Team.RED)
                            + ", blue: " + TDMManager.getMapSpawnCount(world, mapName, TDMManager.Team.BLUE) + ")"
            ));
            return;
        }

        sender.addChatMessage(new ChatComponentText("Usage: /tdm map <create|delete|select|addspawn|clearspawns|list>"));
    }

    private void sendMapList(ICommandSender sender, World world) {
        List<String> maps = TDMManager.getMapNames(world);
        if (maps.isEmpty()) {
            sender.addChatMessage(new ChatComponentText("No TDM maps defined. Admins can use /tdm map create <map>."));
            return;
        }

        String selected = TDMManager.getSelectedMap(world);
        sender.addChatMessage(new ChatComponentText("TDM maps: " + join(maps) + ". Selected: " + (selected.length() == 0 ? "none" : selected)));
        sendVoteCounts(sender, world);
    }

    private void sendVoteCounts(ICommandSender sender, World world) {
        Map<String, Integer> votes = TDMManager.getVoteCounts(world);
        if (votes.isEmpty()) {
            return;
        }

        String message = "Votes: ";
        boolean first = true;
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (!first) {
                message += ", ";
            }
            message += entry.getKey() + "=" + entry.getValue();
            first = false;
        }
        sender.addChatMessage(new ChatComponentText(message));
    }

    private String join(List<String> values) {
        String joined = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                joined += ", ";
            }
            joined += values.get(i);
        }
        return joined;
    }

    private Boolean parseToggle(String value) {
        if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("enabled")) {
            return Boolean.TRUE;
        }

        if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("disabled")) {
            return Boolean.FALSE;
        }

        return null;
    }

    private boolean isAdmin(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(4, getCommandName());
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
