package com.hfr.tdm;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMKitGuiPacket;
import com.hfr.packet.effect.TDMMapVoteGuiPacket;
import com.hfr.packet.effect.TDMStatusPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TDMManager {

    public static boolean tdmEnabled = false;
    public static final int ROUND_TICKS = 20 * 60 * 20;
    public static final int MAP_VOTE_TICKS = 30 * 20;
    public static final int SCORE_LIMIT = 10000;
    public static final int POINTS_PER_KILL = 100;
    private static final Set<String> pendingKitSelection = new HashSet<String>();

    public enum Team {
        RED("red"),
        BLUE("blue");

        public final String name;

        Team(String name) {
            this.name = name;
        }

        public static Team fromName(String name) {
            if (name == null) {
                return null;
            }

            for (Team team : values()) {
                if (team.name.equalsIgnoreCase(name)) {
                    return team;
                }
            }

            return null;
        }
    }

    public static class TDMMap {
        public final String name;
        public final List<SpawnPoint> spawns = new ArrayList<SpawnPoint>();

        public TDMMap(String name) {
            this.name = normalizeMapName(name);
        }
    }

    public static class SpawnPoint {
        public final Team team;
        public final int dim;
        public final int x, y, z;

        public SpawnPoint(Team team, int dim, int x, int y, int z) {
            this.team = team;
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static void init() {
        tdmEnabled = false;
        pendingKitSelection.clear();
    }

    public static boolean isEnabled(World world) {
        TDMData data = TDMData.get(world);
        tdmEnabled = data.enabled;
        return data.enabled;
    }

    public static boolean toggle(World world) {
        TDMData data = TDMData.get(world);
        data.enabled = !data.enabled;
        tdmEnabled = data.enabled;
        if (data.enabled) {
            startRound(world, false);
        } else {
            data.roundEndTick = 0;
            data.mapVoteActive = false;
            data.mapVoteEndTick = 0;
            data.mapVotes.clear();
        }
        data.markDirty();
        sendStatusToAll(world);
        return data.enabled;
    }

    public static boolean isFriendlyFireEnabled(World world) {
        return TDMData.get(world).friendlyFireEnabled;
    }

    public static void setFriendlyFireEnabled(World world, boolean enabled) {
        TDMData data = TDMData.get(world);
        data.friendlyFireEnabled = enabled;
        data.markDirty();
    }

    public static boolean isAutoBalanceEnabled(World world) {
        return TDMData.get(world).autoBalanceEnabled;
    }

    public static void setAutoBalanceEnabled(World world, boolean enabled) {
        TDMData data = TDMData.get(world);
        data.autoBalanceEnabled = enabled;
        data.markDirty();
    }


    public static boolean createMap(World world, String mapName) {
        String normalized = normalizeMapName(mapName);
        if (normalized.length() == 0) {
            return false;
        }

        TDMData data = TDMData.get(world);
        if (data.maps.containsKey(normalized)) {
            return false;
        }

        data.maps.put(normalized, new TDMMap(normalized));
        if (data.selectedMap.length() == 0) {
            data.selectedMap = normalized;
        }
        data.markDirty();
        return true;
    }

    public static boolean deleteMap(World world, String mapName) {
        String normalized = normalizeMapName(mapName);
        TDMData data = TDMData.get(world);
        if (data.maps.remove(normalized) == null) {
            return false;
        }

        if (data.selectedMap.equals(normalized)) {
            data.selectedMap = "";
        }

        List<String> playersToClear = new ArrayList<String>();
        for (Map.Entry<String, String> entry : data.mapVotes.entrySet()) {
            if (entry.getValue().equals(normalized)) {
                playersToClear.add(entry.getKey());
            }
        }
        for (String player : playersToClear) {
            data.mapVotes.remove(player);
        }

        data.markDirty();
        return true;
    }

    public static boolean selectMap(World world, String mapName) {
        String normalized = normalizeMapName(mapName);
        TDMData data = TDMData.get(world);
        if (!data.maps.containsKey(normalized)) {
            return false;
        }

        data.selectedMap = normalized;
        data.markDirty();
        return true;
    }

    public static String getSelectedMap(World world) {
        return TDMData.get(world).selectedMap;
    }

    public static boolean hasMap(World world, String mapName) {
        return TDMData.get(world).maps.containsKey(normalizeMapName(mapName));
    }

    public static List<String> getMapNames(World world) {
        return new ArrayList<String>(TDMData.get(world).maps.keySet());
    }

    public static void addMapSpawn(World world, String mapName, Team team, int dim, int x, int y, int z) {
        TDMData data = TDMData.get(world);
        String normalized = normalizeMapName(mapName);
        TDMMap map = data.maps.get(normalized);
        if (map == null) {
            map = new TDMMap(normalized);
            data.maps.put(normalized, map);
        }

        map.spawns.add(new SpawnPoint(team, dim, x, y, z));
        if (data.selectedMap.length() == 0) {
            data.selectedMap = normalized;
        }
        data.markDirty();
    }

    public static boolean clearMapSpawns(World world, String mapName) {
        TDMMap map = TDMData.get(world).maps.get(normalizeMapName(mapName));
        if (map == null) {
            return false;
        }

        map.spawns.clear();
        TDMData.get(world).markDirty();
        return true;
    }

    public static int getMapSpawnCount(World world, String mapName) {
        TDMMap map = TDMData.get(world).maps.get(normalizeMapName(mapName));
        return map == null ? 0 : map.spawns.size();
    }

    public static int getMapSpawnCount(World world, String mapName, Team team) {
        TDMMap map = TDMData.get(world).maps.get(normalizeMapName(mapName));
        if (map == null) {
            return 0;
        }

        int count = 0;
        for (SpawnPoint spawn : map.spawns) {
            if (spawn.team == team) {
                count++;
            }
        }
        return count;
    }

    public static String voteForMap(World world, String playerName, String mapName) {
        String normalized = normalizeMapName(mapName);
        TDMData data = TDMData.get(world);
        if (!data.enabled || !data.mapVoteActive || !data.maps.containsKey(normalized)) {
            return null;
        }

        data.mapVotes.put(playerName.toLowerCase(), normalized);
        data.markDirty();
        return normalized;
    }


    public static void recordKill(World world, String playerName) {
        updateStat(TDMData.get(world).playerKills, playerName);
        TDMData.get(world).markDirty();
    }

    public static void recordDeath(World world, String playerName) {
        updateStat(TDMData.get(world).playerDeaths, playerName);
        TDMData.get(world).markDirty();
    }

    private static void updateStat(Map<String, Integer> map, String playerName) {
        if (playerName == null) return;
        String key = playerName.toLowerCase();
        Integer old = map.get(key);
        map.put(key, Integer.valueOf(old == null ? 1 : old.intValue() + 1));
    }

    public static int getKills(World world, String playerName) {
        if (playerName == null) return 0;
        Integer v = TDMData.get(world).playerKills.get(playerName.toLowerCase());
        return v == null ? 0 : v.intValue();
    }

    public static int getDeaths(World world, String playerName) {
        if (playerName == null) return 0;
        Integer v = TDMData.get(world).playerDeaths.get(playerName.toLowerCase());
        return v == null ? 0 : v.intValue();
    }

    public static Map<String, Integer> getVoteCounts(World world) {
        TDMData data = TDMData.get(world);
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (String mapName : data.maps.keySet()) {
            counts.put(mapName, Integer.valueOf(0));
        }
        for (String mapName : data.mapVotes.values()) {
            if (counts.containsKey(mapName)) {
                counts.put(mapName, Integer.valueOf(counts.get(mapName).intValue() + 1));
            }
        }
        return counts;
    }

    private static String getWinningMap(TDMData data) {
        String winner = null;
        int winnerVotes = -1;
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (String mapName : data.maps.keySet()) {
            counts.put(mapName, Integer.valueOf(0));
        }
        for (String mapName : data.mapVotes.values()) {
            if (counts.containsKey(mapName)) {
                counts.put(mapName, Integer.valueOf(counts.get(mapName).intValue() + 1));
            }
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue().intValue() > winnerVotes) {
                winner = entry.getKey();
                winnerVotes = entry.getValue().intValue();
            }
        }
        return winner;
    }

    public static String normalizeMapName(String mapName) {
        if (mapName == null) {
            return "";
        }
        return mapName.trim().toLowerCase();
    }


    public static void tickRound(World world) {
        TDMData data = TDMData.get(world);
        if (!data.enabled) {
            return;
        }

        long now = world.getTotalWorldTime();
        if (data.mapVoteActive) {
            if (data.mapVoteEndTick <= 0) {
                data.mapVoteEndTick = now + MAP_VOTE_TICKS;
                data.markDirty();
            }
            if (now >= data.mapVoteEndTick) {
                finishMapVote(world);
            } else {
                sendStatusToAll(world);
            }
            return;
        }

        if (data.roundEndTick <= 0 || now > data.roundEndTick + MAP_VOTE_TICKS) {
            startRound(world, false);
            return;
        }

        if (now >= data.roundEndTick || data.redScore >= SCORE_LIMIT || data.blueScore >= SCORE_LIMIT) {
            startMapVote(world);
            return;
        }

        if (now % 20 == 0) {
            sendStatusToAll(world);
        }
    }

    public static void startRound(World world, boolean resetVotes) {
        TDMData data = TDMData.get(world);
        data.redScore = 0;
        data.blueScore = 0;
        data.playerKills.clear();
        data.playerDeaths.clear();
        data.roundEndTick = world.getTotalWorldTime() + ROUND_TICKS;
        data.mapVoteActive = false;
        data.mapVoteEndTick = 0;
        if (resetVotes) {
            data.mapVotes.clear();
        }
        data.markDirty();
        sendStatusToAll(world);
    }

    public static void addKillScore(World world, Team scoringTeam) {
        TDMData data = TDMData.get(world);
        if (!data.enabled || data.mapVoteActive || scoringTeam == null) {
            return;
        }

        if (scoringTeam == Team.RED) {
            data.redScore += POINTS_PER_KILL;
        } else if (scoringTeam == Team.BLUE) {
            data.blueScore += POINTS_PER_KILL;
        }
        data.markDirty();

        if (data.redScore >= SCORE_LIMIT || data.blueScore >= SCORE_LIMIT) {
            startMapVote(world);
        } else {
            sendStatusToAll(world);
        }
    }

    public static void startMapVote(World world) {
        TDMData data = TDMData.get(world);
        if (data.mapVoteActive) {
            return;
        }

        data.mapVoteActive = true;
        data.mapVoteEndTick = world.getTotalWorldTime() + MAP_VOTE_TICKS;
        data.mapVotes.clear();
        data.markDirty();
        sendMapVoteGuiToAll(world);
        sendStatusToAll(world);
    }

    public static void finishMapVote(World world) {
        TDMData data = TDMData.get(world);
        String winner = getWinningMap(data);
        if (winner != null && data.maps.containsKey(winner)) {
            data.selectedMap = winner;
        }
        data.mapVoteActive = false;
        data.mapVoteEndTick = 0;
        data.mapVotes.clear();
        data.markDirty();
        startRound(world, false);
        teleportAllPlayersToSelectedMap(world);
    }

    public static int getRemainingRoundSeconds(World world) {
        TDMData data = TDMData.get(world);
        if (!data.enabled || data.roundEndTick <= 0) {
            return 0;
        }
        return Math.max(0, (int) ((data.roundEndTick - world.getTotalWorldTime() + 19) / 20));
    }

    public static int getRemainingVoteSeconds(World world) {
        TDMData data = TDMData.get(world);
        if (!data.enabled || !data.mapVoteActive || data.mapVoteEndTick <= 0) {
            return 0;
        }
        return Math.max(0, (int) ((data.mapVoteEndTick - world.getTotalWorldTime() + 19) / 20));
    }

    public static boolean isMapVoteActive(World world) {
        return TDMData.get(world).mapVoteActive;
    }

    public static int getScore(World world, Team team) {
        TDMData data = TDMData.get(world);
        return team == Team.RED ? data.redScore : data.blueScore;
    }

    public static void sendStatusToAll(World world) {
        TDMData data = TDMData.get(world);
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (player.worldObj.provider.dimensionId == world.provider.dimensionId) {
                PacketDispatcher.wrapper.sendTo(new TDMStatusPacket(
                        data.enabled,
                        data.mapVoteActive,
                        getRemainingRoundSeconds(world),
                        getRemainingVoteSeconds(world),
                        data.redScore,
                        data.blueScore,
                        data.selectedMap
                ), player);
            }
        }
    }

    private static void sendMapVoteGuiToAll(World world) {
        List<String> mapNames = getMapNames(world);
        if (mapNames.isEmpty()) {
            return;
        }

        String[] maps = mapNames.toArray(new String[mapNames.size()]);
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (player.worldObj.provider.dimensionId == world.provider.dimensionId) {
                PacketDispatcher.wrapper.sendTo(new TDMMapVoteGuiPacket(maps, MAP_VOTE_TICKS / 20), player);
            }
        }
    }

    private static void teleportAllPlayersToSelectedMap(World world) {
        Random rand = new Random();
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (player.worldObj.provider.dimensionId != world.provider.dimensionId) {
                continue;
            }

            if (respawnPlayer(player, rand)) {
                pendingKitSelection.remove(getPlayerKey(player));
                promptForKit(player);
            }
        }
    }

    public static void addSpawn(World world, Team team, int dim, int x, int y, int z) {
        TDMData data = TDMData.get(world);
        data.spawns.add(new SpawnPoint(team, dim, x, y, z));
        data.markDirty();
    }

    public static void clearSpawns(World world) {
        TDMData data = TDMData.get(world);
        data.spawns.clear();
        data.markDirty();
    }

    public static int getSpawnCount(World world) {
        return TDMData.get(world).spawns.size();
    }

    public static int getSpawnCount(World world, Team team) {
        int count = 0;
        for (SpawnPoint spawn : TDMData.get(world).spawns) {
            if (spawn.team == team) {
                count++;
            }
        }
        return count;
    }

    public static void setPlayerTeam(World world, String playerName, Team team) {
        TDMData data = TDMData.get(world);
        data.playerTeams.put(playerName.toLowerCase(), team);
        data.markDirty();
    }

    public static Team getPlayerTeam(World world, String playerName) {
        return TDMData.get(world).playerTeams.get(playerName.toLowerCase());
    }

    public static Team getOrAssignPlayerTeam(EntityPlayer player) {
        TDMData data = TDMData.get(player.worldObj);
        String playerName = player.getCommandSenderName().toLowerCase();
        Team team = data.playerTeams.get(playerName);

        if (team != null) {
            return team;
        }

        team = getSmallestTeam(data);
        data.playerTeams.put(playerName, team);
        data.markDirty();
        return team;
    }

    private static Team getSmallestTeam(TDMData data) {
        int red = 0;
        int blue = 0;
        List<EntityPlayerMP> onlinePlayers = getOnlinePlayers();

        if (!onlinePlayers.isEmpty()) {
            for (EntityPlayerMP player : onlinePlayers) {
                Team team = data.playerTeams.get(player.getCommandSenderName().toLowerCase());
                if (team == Team.RED) {
                    red++;
                } else if (team == Team.BLUE) {
                    blue++;
                }
            }
        } else {
            for (Team team : data.playerTeams.values()) {
                if (team == Team.RED) {
                    red++;
                } else if (team == Team.BLUE) {
                    blue++;
                }
            }
        }

        return red <= blue ? Team.RED : Team.BLUE;
    }

    public static int balanceTeams(World world) {
        TDMData data = TDMData.get(world);
        if (!data.enabled) {
            return 0;
        }

        List<EntityPlayerMP> onlinePlayers = getOnlinePlayers();
        if (onlinePlayers.size() < 2) {
            return 0;
        }

        boolean assigned = false;
        for (EntityPlayerMP player : onlinePlayers) {
            String playerName = player.getCommandSenderName().toLowerCase();
            if (!data.playerTeams.containsKey(playerName)) {
                data.playerTeams.put(playerName, getSmallestTeam(data));
                assigned = true;
            }
        }

        int moved = 0;
        while (Math.abs(getOnlineTeamCount(data, Team.RED) - getOnlineTeamCount(data, Team.BLUE)) > 1) {
            Team larger = getOnlineTeamCount(data, Team.RED) > getOnlineTeamCount(data, Team.BLUE) ? Team.RED : Team.BLUE;
            Team smaller = larger == Team.RED ? Team.BLUE : Team.RED;
            EntityPlayerMP playerToMove = getLastOnlinePlayerOnTeam(data, larger);
            if (playerToMove == null) {
                break;
            }

            data.playerTeams.put(playerToMove.getCommandSenderName().toLowerCase(), smaller);
            pendingKitSelection.remove(getPlayerKey(playerToMove));
            playerToMove.addChatMessage(new ChatComponentText("You were moved to " + smaller.name + " to balance TDM teams."));
            promptForKit(playerToMove);
            moved++;
        }

        if (assigned || moved > 0) {
            data.markDirty();
        }

        return moved;
    }

    private static int getOnlineTeamCount(TDMData data, Team team) {
        int count = 0;
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (data.playerTeams.get(player.getCommandSenderName().toLowerCase()) == team) {
                count++;
            }
        }
        return count;
    }

    private static EntityPlayerMP getLastOnlinePlayerOnTeam(TDMData data, Team team) {
        List<EntityPlayerMP> onlinePlayers = getOnlinePlayers();
        for (int i = onlinePlayers.size() - 1; i >= 0; i--) {
            EntityPlayerMP player = onlinePlayers.get(i);
            if (data.playerTeams.get(player.getCommandSenderName().toLowerCase()) == team) {
                return player;
            }
        }
        return null;
    }

    private static List<EntityPlayerMP> getOnlinePlayers() {
        List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) {
            return players;
        }

        for (Object player : server.getConfigurationManager().playerEntityList) {
            if (player instanceof EntityPlayerMP) {
                players.add((EntityPlayerMP) player);
            }
        }
        return players;
    }

    public static void promptForKit(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        Team team = getOrAssignPlayerTeam(player);
        String mapName = getSelectedMap(player.worldObj);
        if (TDMKitManager.getKitCount(mapName, team) <= 0) {
            player.addChatMessage(new net.minecraft.util.ChatComponentText("No TDM kits have been saved for " + team.name + " on map " + (mapName.length() == 0 ? "global" : mapName) + ". Ask an admin to use /tdm kit add " + team.name + " [map]."));
            return;
        }

        pendingKitSelection.add(getPlayerKey(player));
        PacketDispatcher.wrapper.sendTo(new TDMKitGuiPacket(team.name, TDMKitManager.getKitNames(mapName, team)), (EntityPlayerMP) player);
    }

    public static void tickKitSelection(EntityPlayer player) {
        if (!pendingKitSelection.contains(getPlayerKey(player))) {
            return;
        }

        if (!isEnabled(player.worldObj)) {
            pendingKitSelection.remove(getPlayerKey(player));
            return;
        }

        player.addPotionEffect(new PotionEffect(Potion.invisibility.id, 40, 4, true));
        player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 40, 4, true));
        player.addPotionEffect(new PotionEffect(Potion.resistance.id, 40, 4, true));
        player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 40, 4, true));
    }

    public static boolean selectKit(EntityPlayer player, int kitIndex) {
        if (!pendingKitSelection.contains(getPlayerKey(player))) {
            return false;
        }

        if (!isEnabled(player.worldObj)) {
            pendingKitSelection.remove(getPlayerKey(player));
            return false;
        }

        Team team = getOrAssignPlayerTeam(player);
        if (!TDMKitManager.applyKit(getSelectedMap(player.worldObj), team, kitIndex, player)) {
            return false;
        }

        pendingKitSelection.remove(getPlayerKey(player));
        player.removePotionEffect(Potion.resistance.id);
        player.removePotionEffect(Potion.regeneration.id);
        return true;
    }

    private static String getPlayerKey(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }

    public static boolean respawnPlayer(EntityPlayer player, Random rand) {
        SpawnPoint spawn = getRandomSpawn(player, rand);
        if (spawn == null) {
            return false;
        }

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            playerMP.playerNetServerHandler.setPlayerLocation(
                    spawn.x + 0.5,
                    spawn.y,
                    spawn.z + 0.5,
                    playerMP.rotationYaw,
                    playerMP.rotationPitch
            );
        } else {
            player.setPositionAndUpdate(
                    spawn.x + 0.5,
                    spawn.y,
                    spawn.z + 0.5
            );
        }

        return true;
    }

    public static SpawnPoint getRandomSpawn(EntityPlayer player, Random rand) {
        Team team = getOrAssignPlayerTeam(player);
        return getRandomSpawn(player.worldObj, team, rand);
    }

    public static SpawnPoint getRandomSpawn(World world, Team team, Random rand) {
        TDMData data = TDMData.get(world);
        List<SpawnPoint> valid = new ArrayList<SpawnPoint>();
        TDMMap selected = data.maps.get(data.selectedMap);

        if (selected != null) {
            addValidSpawns(valid, selected.spawns, team, world.provider.dimensionId);
        }

        if (valid.isEmpty()) {
            addValidSpawns(valid, data.spawns, team, world.provider.dimensionId);
        }

        if (valid.isEmpty()) {
            return null;
        }

        return valid.get(rand.nextInt(valid.size()));
    }

    private static void addValidSpawns(List<SpawnPoint> valid, List<SpawnPoint> spawns, Team team, int dim) {
        for (SpawnPoint spawn : spawns) {
            if (spawn.team == team && spawn.dim == dim) {
                valid.add(spawn);
            }
        }
    }
}
