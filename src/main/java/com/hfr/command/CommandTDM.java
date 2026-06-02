package com.hfr.command;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMMenuDataPacket;
import com.hfr.tdm.TDMKitManager;
import com.hfr.tdm.TDMManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
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
        return "/tdm help";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("man") || args[0].equalsIgnoreCase("?")) {
            sendHelp(sender);
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
            if (args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("openmenu")) {
                openMenu(sender);
                return;
            }

            if (args[0].equalsIgnoreCase("teamchange") || args[0].equalsIgnoreCase("team") || args[0].equalsIgnoreCase("switchteam")) {
                processTeamChangeCommand(sender);
                return;
            }

            sender.addChatMessage(new ChatComponentText("Unknown or admin-only TDM command: " + args[0]));
            sender.addChatMessage(new ChatComponentText("Use /tdm help for player commands."));
            return;
        }

        if (args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("openmenu")) {
            openMenu(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("teamchange") || args[0].equalsIgnoreCase("team") || args[0].equalsIgnoreCase("switchteam")) {
            processTeamChangeCommand(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("kits") || args[0].equalsIgnoreCase("listkits")) {
            processKitCommand(sender, prependArg("list", args));
            return;
        }

        if (args[0].equalsIgnoreCase("kit")) {
            processKitCommand(sender, args);
            return;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            boolean enabled = TDMManager.toggle(world);
            sender.addChatMessage(new ChatComponentText("TDM: " + enabled));
            return;
        }

        if (args[0].equalsIgnoreCase("forcemapvote") || args[0].equalsIgnoreCase("forcevote")) {
            if (!TDMManager.isEnabled(world)) {
                sender.addChatMessage(new ChatComponentText("TDM must be enabled before forcing a map vote."));
                return;
            }

            if (TDMManager.getMapNames(world).isEmpty()) {
                sender.addChatMessage(new ChatComponentText("No TDM maps defined. Use /tdm map create <map> first."));
                return;
            }

            if (TDMManager.isMapVoteActive(world)) {
                sender.addChatMessage(new ChatComponentText("A TDM map vote is already active."));
                return;
            }

            TDMManager.startMapVote(world);
            sender.addChatMessage(new ChatComponentText("Forced a 30 second TDM map vote."));
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

        sender.addChatMessage(new ChatComponentText("Unknown TDM command: " + args[0]));
        sender.addChatMessage(new ChatComponentText("Use /tdm help for available commands and examples."));
    }

    private void sendHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(HELP + "/tdm [command] <args...>"));
        sender.addChatMessage(new ChatComponentText(INFO + "TDM commands:"));
        sender.addChatMessage(new ChatComponentText(TITLE + "Player commands"));
        sender.addChatMessage(new ChatComponentText(COMMAND + "-maps" + TITLE + " - Lists playable maps and current votes"));
        sender.addChatMessage(new ChatComponentText(COMMAND + "-vote <map>" + TITLE + " - Votes during an active map vote"));
        sender.addChatMessage(new ChatComponentText(COMMAND + "-menu" + TITLE + " - Opens the TDM menu to swap teams"));
        sender.addChatMessage(new ChatComponentText(COMMAND + "-teamchange" + TITLE + " - Fallback team swap with a 120s cooldown"));
        if (!isAdmin(sender)) {
            sender.addChatMessage(new ChatComponentText(INFO + "Tip: open the TDM menu from the HUD/keybind to change teams."));
            return;
        }

        sender.addChatMessage(new ChatComponentText(TITLE + "Admin setup"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-toggle" + TITLE + " - Enables or disables TDM"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-map create <map>" + TITLE + " - Creates a playable map"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-map delete <map>" + TITLE + " - Deletes a playable map"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-map select <map>" + TITLE + " - Selects the current map"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-map addspawn <map> <red|blue>" + TITLE + " - Adds your position as a map spawn"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-kit add <red|blue> [map|global]" + TITLE + " - Saves your inventory as a kit"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-kit list [map|global]" + TITLE + " - Lists saved kits"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-kit remove <red|blue> <number> [map|global]" + TITLE + " - Deletes a kit"));
        sender.addChatMessage(new ChatComponentText(TITLE + "Match controls"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-forcemapvote" + TITLE + " - Starts a 30 second map vote"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-friendlyfire <on|off>" + TITLE + " - Toggles friendly fire"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-autobalance <on|off|now>" + TITLE + " - Configures or runs auto balance"));
        sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-setteam <player> <red|blue>" + TITLE + " - Moves a player to a team"));
        sender.addChatMessage(new ChatComponentText(INFO + "Legacy spawn tools: /tdm addspawn <red|blue>, /tdm clear"));
    }

    private void openMenu(ICommandSender sender) {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        if (!TDMManager.isEnabled(player.worldObj)) {
            sender.addChatMessage(new ChatComponentText("TDM is not enabled."));
            return;
        }

        if (!(player instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("Only players can open the TDM menu."));
            return;
        }

        PacketDispatcher.wrapper.sendTo(new TDMMenuDataPacket((EntityPlayerMP) player, TDMManager.getTeamChangeCooldownSeconds(player)), (EntityPlayerMP) player);
    }

    private String[] prependArg(String first, String[] args) {
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = args[0];
        newArgs[1] = first;
        for (int i = 1; i < args.length; i++) {
            newArgs[i + 1] = args[i];
        }
        return newArgs;
    }

    private void processTeamChangeCommand(ICommandSender sender) {
        TDMManager.changePlayerTeamWithCooldown(getCommandSenderAsPlayer(sender));
    }

    private void processKitCommand(ICommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm kit <list|add|remove> ..."));
            sender.addChatMessage(new ChatComponentText("  /tdm kit list [map|global]"));
            sender.addChatMessage(new ChatComponentText("  /tdm kit add <red|blue> [map|global]"));
            sender.addChatMessage(new ChatComponentText("  /tdm kit remove <red|blue> <number> [map|global]"));
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            String mapName = args.length >= 3 ? normalizeKitMap(args[2]) : TDMManager.getSelectedMap(sender.getEntityWorld());
            sendKitList(sender, mapName);
            return;
        }

        if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("save")) {
            if (args.length < 3) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm kit add <blue|red> [map|global]"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[2]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[2]));
                sender.addChatMessage(new ChatComponentText("Usage: /tdm kit add <blue|red> [map|global]"));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            String mapName = args.length >= 4 ? normalizeKitMap(args[3]) : TDMManager.getSelectedMap(sender.getEntityWorld());
            int kitCount = TDMKitManager.addKit(mapName, team, player);
            String mapText = mapName.length() > 0 ? " for map " + mapName : " as a global fallback";
            sender.addChatMessage(new ChatComponentText("Saved " + team.name + " kit #" + kitCount + mapText + " from your inventory to tdm_kits.txt"));
            return;
        }

        if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete")) {
            removeKit(sender, args);
            return;
        }

        sender.addChatMessage(new ChatComponentText("Unknown TDM kit command: " + args[1]));
        sender.addChatMessage(new ChatComponentText("Usage: /tdm kit <list|add|remove> ..."));
    }

    private void removeKit(ICommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm kit remove <blue|red> <number> [map|global]"));
            return;
        }

        TDMManager.Team team = TDMManager.Team.fromName(args[2]);
        if (team == null) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[2]));
            sender.addChatMessage(new ChatComponentText("Usage: /tdm kit remove <blue|red> <number> [map|global]"));
            return;
        }

        int kitNumber;
        try {
            kitNumber = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText("Kit number must be a number from /tdm kit list."));
            return;
        }

        String mapName = args.length >= 5 ? normalizeKitMap(args[4]) : TDMManager.getSelectedMap(sender.getEntityWorld());
        if (!TDMKitManager.removeKit(mapName, team, kitNumber - 1)) {
            sender.addChatMessage(new ChatComponentText("No " + team.name + " kit #" + kitNumber + " exists for " + getMapDisplayName(mapName) + ". Use /tdm kit list " + getMapDisplayName(mapName) + " to list kits."));
            return;
        }

        sender.addChatMessage(new ChatComponentText("Removed " + team.name + " kit #" + kitNumber + " from " + getMapDisplayName(mapName) + "."));
    }

    private void sendKitList(ICommandSender sender, String mapName) {
        String displayMap = getMapDisplayName(mapName);
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

    private String getMapDisplayName(String mapName) {
        return mapName.length() == 0 ? "global" : mapName;
    }

    private void processMapCommand(ICommandSender sender, String[] args, World world) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm map <create|delete|select|addspawn|clearspawns|list>"));
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            sendMapList(sender, world);
            return;
        }

        if (!action.equals("create") && !action.equals("delete") && !action.equals("select") && !action.equals("addspawn") && !action.equals("clearspawns")) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM map command: " + args[1]));
            sender.addChatMessage(new ChatComponentText("Usage: /tdm map <create|delete|select|addspawn|clearspawns|list>"));
            return;
        }

        if (args.length < 3) {
            if (action.equals("addspawn")) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map addspawn <map> <red|blue>"));
            } else {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map " + args[1] + " <map>"));
            }
            return;
        }

        String mapName = TDMManager.normalizeMapName(args[2]);
        if (mapName.length() == 0) {
            sender.addChatMessage(new ChatComponentText("Map name cannot be empty."));
            return;
        }

        if (action.equals("create")) {
            if (!TDMManager.createMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("TDM map already exists or has an invalid name: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Created TDM map: " + mapName));
            return;
        }

        if (action.equals("delete")) {
            if (!TDMManager.deleteMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Deleted TDM map: " + mapName));
            return;
        }

        if (action.equals("select")) {
            if (!TDMManager.selectMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Selected TDM map: " + mapName));
            return;
        }

        if (action.equals("clearspawns")) {
            if (!TDMManager.clearMapSpawns(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Cleared spawns for TDM map: " + mapName));
            return;
        }

        if (action.equals("addspawn")) {
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

    public static final String ERROR = EnumChatFormatting.RED.toString();
    public static final String TITLE = EnumChatFormatting.GOLD.toString();
    public static final String HELP = EnumChatFormatting.DARK_GREEN.toString();
    public static final String INFO = EnumChatFormatting.GREEN.toString();
    public static final String COMMAND = EnumChatFormatting.RED.toString();
    public static final String COMMAND_ADMIN = EnumChatFormatting.DARK_PURPLE.toString();
}
