package com.hfr.tdm;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMKitGuiPacket;
import com.hfr.packet.effect.TDMKitSelectResultPacket;
import com.hfr.packet.effect.TDMMapVoteGuiPacket;
import com.hfr.packet.effect.TDMStatusPacket;
import com.hfr.compat.HbmCsgoChargeIntegration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.*;

public class TDMManager {

    public static boolean tdmEnabled = false;
    public static final int ROUND_TICKS = 20 * 60 * 20;
    public static final int MAP_VOTE_TICKS = 30 * 20;
    public static final int SCORE_LIMIT = 10000;
    public static final int POINTS_PER_KILL = 100;
    public static final int BOMB_SCORE_LIMIT = 13;
    public static final int BOMB_ROUND_TICKS = 120 * 20;
    public static final int BUY_TIME_TICKS = 20 * 20;
    public static final int TEAM_CHANGE_COOLDOWN_TICKS = 120 * 20;
    private static final Set<String> pendingKitSelection = new HashSet<String>();
    private static final Map<String, Long> nextTeamChangeTick = new HashMap<String, Long>();
    private static boolean bombTestMode;

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

    public enum TDMGameMode { DEATHMATCH, BOMB }
    public enum BombRole { TERRORIST, COUNTER_TERRORIST }
    public enum KitSelectionResult { SUCCESS, INSUFFICIENT_FUNDS, INVALID_SELECTION, BUY_PHASE_ENDED, ALREADY_SELECTED }

    public static class Bombsite {
        public int dimension;
        public boolean hasPos1, hasPos2;
        public int x1, y1, z1, x2, y2, z2;

        public boolean isComplete() { return hasPos1 && hasPos2; }
        public boolean contains(int dim, int x, int y, int z) {
            return isComplete() && dimension == dim
                    && x >= Math.min(x1, x2) && x <= Math.max(x1, x2)
                    && y >= Math.min(y1, y2) && y <= Math.max(y1, y2)
                    && z >= Math.min(z1, z2) && z <= Math.max(z1, z2);
        }
        public void clear() { hasPos1 = hasPos2 = false; }
    }

    public static class TDMMap {
        public final String name;
        public final List<SpawnPoint> spawns = new ArrayList<SpawnPoint>();
        /** Zero means inherit the global setting. */
        public int scoreLimitOverride;
        /** Zero means inherit the global setting; positive values are ticks. */
        public int roundTicksOverride;
        public TDMGameMode mode = TDMGameMode.DEATHMATCH;
        public Team terroristTeam = Team.RED;
        public boolean hardcoreRespawns;
        public int bombScoreLimitOverride;
        public int bombRoundTicksOverride;
        public boolean buyScoreEnabled;
        public int killBuyScoreReward;
        public int bombDefuseBuyScoreReward;
        public final Bombsite bombsiteA = new Bombsite();
        public final Bombsite bombsiteB = new Bombsite();

        public TDMMap(String name) {
            this.name = normalizeMapName(name);
        }
    }

    public static TDMMap getMap(World world, String name) {
        return TDMData.get(world).maps.get(normalizeMapName(name));
    }
    public static TDMMap getSelectedMapData(World world) { return getMap(world, getSelectedMap(world)); }
    public static TDMGameMode getGameMode(World world) { TDMMap m=getSelectedMapData(world); return m == null ? TDMGameMode.DEATHMATCH : m.mode; }
    public static Team getTerroristTeam(World world) { TDMMap m=getSelectedMapData(world); return m == null ? Team.RED : m.terroristTeam; }
    public static Team getCounterTerroristTeam(World world) { return getTerroristTeam(world) == Team.RED ? Team.BLUE : Team.RED; }
    public static BombRole getBombRole(World world, Team team) { return team == getTerroristTeam(world) ? BombRole.TERRORIST : BombRole.COUNTER_TERRORIST; }
    public static BombRole getBombRole(EntityPlayer player) { return getBombRole(player.worldObj, getOrAssignPlayerTeam(player)); }
    public static boolean isTerrorist(EntityPlayer player) { return getBombRole(player) == BombRole.TERRORIST; }
    public static boolean isCounterTerrorist(EntityPlayer player) { return getBombRole(player) == BombRole.COUNTER_TERRORIST; }
    public static boolean isHardcoreRespawns(World world) { TDMMap m=getSelectedMapData(world); return m != null && m.hardcoreRespawns; }
    public static boolean isBombMode(World world) { return getGameMode(world) == TDMGameMode.BOMB; }

    public static boolean configureMap(World world, String name, TDMGameMode mode, Team terrorists, Boolean hardcore) {
        TDMMap map=getMap(world,name); if(map==null)return false;
        if(mode!=null&&!setMapMode(world,name,mode))return false; if(terrorists!=null)map.terroristTeam=terrorists; if(hardcore!=null)map.hardcoreRespawns=hardcore.booleanValue();
        TDMData.get(world).markDirty(); return true;
    }

    /** Sets map mode and, only for the selected live map, starts a clean match in that mode. */
    public static boolean setMapMode(World world, String name, TDMGameMode mode) {
        TDMData data = TDMData.get(world);
        TDMMap map = data.maps.get(normalizeMapName(name));
        if (map == null || mode == null) return false;
        TDMGameMode oldMode = map.mode;
        if (oldMode == mode) return true;
        map.mode = mode;
        data.markDirty();
        if (!data.enabled || !data.selectedMap.equals(map.name)) return true;

        // startMatch owns score, timer, spectator, vote, economy, and bomb lifecycle reset.
        pendingKitSelection.clear();
        closeBombBuyGuis();
        data.roundEndTick = 0;
        startMatch(world, false);
        return true;
    }

    public static boolean isBombTestMode() { return bombTestMode; }

    public static void setBombTestMode(World world, boolean enabled) {
        if (bombTestMode == enabled) return;
        bombTestMode = enabled;
        if (isEnabled(world) && isBombMode(world)) TDMBombManager.onTestModeChanged(world, enabled);
        sendStatusToAll(world);
    }

    public static void clearPendingKitSelections() { pendingKitSelection.clear(); }

    public static void closeBombBuyGuis() {
        for (EntityPlayerMP player : getOnlinePlayers()) {
            PacketDispatcher.wrapper.sendTo(new TDMKitGuiPacket("", new String[0]), player);
        }
    }
    public static boolean setBombsite(World world,String name,boolean siteA,int corner,int dim,int x,int y,int z) {
        TDMMap map=getMap(world,name); if(map==null)return false; Bombsite site=siteA?map.bombsiteA:map.bombsiteB;
        if ((site.hasPos1 || site.hasPos2) && site.dimension != dim) return false;
        site.dimension=dim; if(corner==1){site.x1=x;site.y1=y;site.z1=z;site.hasPos1=true;}else{site.x2=x;site.y2=y;site.z2=z;site.hasPos2=true;}
        TDMData.get(world).markDirty(); return true;
    }
    public static boolean clearBombsite(World world,String name,boolean siteA){TDMMap m=getMap(world,name);if(m==null)return false;(siteA?m.bombsiteA:m.bombsiteB).clear();TDMData.get(world).markDirty();return true;}
    public static String getBombsiteAt(World world,int dim,int x,int y,int z){TDMMap m=getSelectedMapData(world);if(m==null)return null;if(m.bombsiteA.contains(dim,x,y,z))return "A";if(m.bombsiteB.contains(dim,x,y,z))return "B";return null;}
    public static int getEffectiveBombScoreLimit(World world,String name){TDMMap m=getMap(world,name);return m!=null&&m.bombScoreLimitOverride>0?m.bombScoreLimitOverride:BOMB_SCORE_LIMIT;}
    public static int getEffectiveBombRoundTicks(World world,String name){TDMMap m=getMap(world,name);return m!=null&&m.bombRoundTicksOverride>0?m.bombRoundTicksOverride:BOMB_ROUND_TICKS;}

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
        bombTestMode = false;
        pendingKitSelection.clear();
        nextTeamChangeTick.clear();
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
            TDMBombManager.cleanup(world, true);
            TDMSpectatorManager.restoreAll();
            data.roundEndTick = 0;
            data.mapVoteActive = false;
            data.mapVoteEndTick = 0;
            data.mapVotes.clear();
            data.playerBuyScores.clear();
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

        TDMBombManager.cleanup(world,true);TDMSpectatorManager.restoreAll();data.playerBuyScores.clear();data.selectedMap = normalized;
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

    public static int getEffectiveScoreLimit(World world) {
        TDMData data = TDMData.get(world);
        return getEffectiveScoreLimit(data.maps.get(data.selectedMap));
    }

    public static int getEffectiveScoreLimit(World world, String mapName) {
        return getEffectiveScoreLimit(TDMData.get(world).maps.get(normalizeMapName(mapName)));
    }

    private static int getEffectiveScoreLimit(TDMMap map) {
        return map != null && map.scoreLimitOverride > 0 ? map.scoreLimitOverride : SCORE_LIMIT;
    }

    public static int getEffectiveRoundTicks(World world) {
        TDMData data = TDMData.get(world);
        return getEffectiveRoundTicks(data.maps.get(data.selectedMap));
    }

    public static int getEffectiveRoundTicks(World world, String mapName) {
        return getEffectiveRoundTicks(TDMData.get(world).maps.get(normalizeMapName(mapName)));
    }

    private static int getEffectiveRoundTicks(TDMMap map) {
        return map != null && map.roundTicksOverride > 0 ? map.roundTicksOverride : ROUND_TICKS;
    }

    public static int getScoreLimitOverride(World world, String mapName) {
        TDMMap map = TDMData.get(world).maps.get(normalizeMapName(mapName));
        return map == null ? 0 : map.scoreLimitOverride;
    }

    public static int getRoundTicksOverride(World world, String mapName) {
        TDMMap map = TDMData.get(world).maps.get(normalizeMapName(mapName));
        return map == null ? 0 : map.roundTicksOverride;
    }

    public static boolean setMapScoreLimit(World world, String mapName, int scoreLimit) {
        TDMData data = TDMData.get(world);
        TDMMap map = data.maps.get(normalizeMapName(mapName));
        if (map == null || scoreLimit < 0) return false;
        if (map.mode == TDMGameMode.BOMB) map.bombScoreLimitOverride = scoreLimit;
        else map.scoreLimitOverride = scoreLimit;
        data.markDirty();
        return true;
    }

    public static boolean setMapRoundTicks(World world, String mapName, int roundTicks) {
        TDMData data = TDMData.get(world);
        TDMMap map = data.maps.get(normalizeMapName(mapName));
        if (map == null || roundTicks < 0) return false;
        if (map.mode == TDMGameMode.BOMB) map.bombRoundTicksOverride = roundTicks;
        else map.roundTicksOverride = roundTicks;
        data.markDirty();
        return true;
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

        if (isBombMode(world)) {
            TDMBombManager.tick(world);
            if (now % 20 == 0) sendStatusToAll(world);
            return;
        }

        if (data.roundEndTick <= 0 || now > data.roundEndTick + MAP_VOTE_TICKS) {
            startRound(world, false);
            return;
        }

        int scoreLimit = getEffectiveScoreLimit(world);
        if (now >= data.roundEndTick || data.redScore >= scoreLimit || data.blueScore >= scoreLimit) {
            startMapVote(world);
            return;
        }

        if (now % 20 == 0) {
            sendStatusToAll(world);
        }
    }

    public static void startRound(World world, boolean resetVotes) {
        startMatch(world, resetVotes);
    }

    /** Starts a full map match; bomb combat rounds have their own lifecycle. */
    public static void startMatch(World world, boolean resetVotes) {
        TDMData data = TDMData.get(world);
        data.redScore = 0;
        data.blueScore = 0;
        data.playerKills.clear();
        data.playerDeaths.clear();
        data.playerBuyScores.clear();
        data.roundEndTick = world.getTotalWorldTime() + getEffectiveRoundTicks(world);
        data.mapVoteActive = false;
        data.mapVoteEndTick = 0;
        if (resetVotes) {
            data.mapVotes.clear();
        }
        data.markDirty();
        TDMSpectatorManager.restoreAll();
        if (isBombMode(world)) TDMBombManager.startMatch(world);
        else TDMBombManager.cleanup(world, true);
        sendStatusToAll(world);
    }

    public static void addKillScore(World world, Team scoringTeam) {
        TDMData data = TDMData.get(world);
        if (!data.enabled || data.mapVoteActive || scoringTeam == null) {
            return;
        }

        if (isBombMode(world)) { sendStatusToAll(world); return; }
        if (scoringTeam == Team.RED) {
            data.redScore = addScore(data.redScore);
        } else if (scoringTeam == Team.BLUE) {
            data.blueScore = addScore(data.blueScore);
        }
        data.markDirty();

        int scoreLimit = getEffectiveScoreLimit(world);
        if (data.redScore >= scoreLimit || data.blueScore >= scoreLimit) {
            startMapVote(world);
        } else {
            sendStatusToAll(world);
        }
    }

    private static int addScore(int score) {
        return score > Integer.MAX_VALUE - POINTS_PER_KILL ? Integer.MAX_VALUE : score + POINTS_PER_KILL;
    }

    public static void startMapVote(World world) {
        TDMData data = TDMData.get(world);
        if (data.mapVoteActive) {
            return;
        }

        TDMBombManager.cleanup(world, true);
        TDMSpectatorManager.restoreAll();
        data.playerBuyScores.clear();

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
                        data.blueScore, data.selectedMap,
                        getGameMode(world).name(), TDMBombManager.getState().name(),
                        data.redBombWins, data.blueBombWins, getTerroristTeam(world).name,
                        TDMBombManager.getRemainingSeconds(world), TDMBombManager.getPlantedSite(),
                        getSelectedMapData(world)!=null&&getSelectedMapData(world).buyScoreEnabled, getBuyScore(player),
                        TDMBombManager.getPlayerCount(world, Team.RED), TDMBombManager.getPlayerCount(world, Team.BLUE)
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


    public static boolean changePlayerTeamWithCooldown(EntityPlayer player) {
        if (!isEnabled(player.worldObj)) {
            player.addChatMessage(new ChatComponentText("TDM is not enabled."));
            return false;
        }
        if (TDMBombManager.isRoundActive() || TDMSpectatorManager.isObserving(player)) {
            player.addChatMessage(new ChatComponentText("Team changes are unavailable during an active bomb round."));
            return false;
        }

        int secondsLeft = getTeamChangeCooldownSeconds(player);
        if (secondsLeft > 0) {
            player.addChatMessage(new ChatComponentText("You can change teams again in " + secondsLeft + " seconds."));
            return false;
        }

        Team currentTeam = getOrAssignPlayerTeam(player);
        Team newTeam = currentTeam == Team.RED ? Team.BLUE : Team.RED;
        setPlayerTeam(player.worldObj, player.getCommandSenderName(), newTeam);
        nextTeamChangeTick.put(getPlayerKey(player), Long.valueOf(player.worldObj.getTotalWorldTime() + TEAM_CHANGE_COOLDOWN_TICKS));
        pendingKitSelection.remove(getPlayerKey(player));
        player.addChatMessage(new ChatComponentText("You changed to the " + newTeam.name + " TDM team."));

        if (!respawnPlayer(player, new Random())) {
            player.addChatMessage(new ChatComponentText("No spawn is available for your new team on this map."));
        }
        promptForKit(player);
        return true;
    }

    public static int getTeamChangeCooldownSeconds(EntityPlayer player) {
        Long nextAllowedTick = nextTeamChangeTick.get(getPlayerKey(player));
        if (nextAllowedTick == null) {
            return 0;
        }

        long ticksLeft = nextAllowedTick.longValue() - player.worldObj.getTotalWorldTime();
        if (ticksLeft <= 0) {
            return 0;
        }

        return (int) ((ticksLeft + 19) / 20);
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
        if (TDMBombManager.isRoundActive()) return 0;

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

    public static List<EntityPlayerMP> getOnlinePlayers() {
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

    public static int getBuyScore(EntityPlayer player){Integer v=TDMData.get(player.worldObj).playerBuyScores.get(getPlayerKey(player));return v==null?0:Math.max(0,v.intValue());}
    public static void addBuyScore(EntityPlayer player,int amount){TDMMap map=getSelectedMapData(player.worldObj);if(map==null||!map.buyScoreEnabled||amount<=0)return;TDMData d=TDMData.get(player.worldObj);int old=getBuyScore(player);int next=old>Integer.MAX_VALUE-amount?Integer.MAX_VALUE:old+amount;d.playerBuyScores.put(getPlayerKey(player),Integer.valueOf(next));d.markDirty();sendStatusToAll(player.worldObj);}
    public static void awardKillBuyScore(EntityPlayer player){TDMMap map=getSelectedMapData(player.worldObj);if(map!=null&&map.mode==TDMGameMode.BOMB&&map.buyScoreEnabled&&TDMBombManager.isRoundActive())addBuyScore(player,map.killBuyScoreReward);}
    public static void awardDefuseBuyScore(EntityPlayer player){TDMMap map=getSelectedMapData(player.worldObj);if(map!=null&&map.buyScoreEnabled)addBuyScore(player,map.bombDefuseBuyScoreReward);}
    public static void clearKitSelection(EntityPlayer player){pendingKitSelection.remove(getPlayerKey(player));}
    public static boolean hasSelectedKit(EntityPlayer player){return !pendingKitSelection.contains(getPlayerKey(player));}

    public static void promptForKit(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        if (TDMSpectatorManager.isObserving(player)) return;

        Team team = getOrAssignPlayerTeam(player);
        String mapName = getSelectedMap(player.worldObj);
        if (TDMKitManager.getKitCount(mapName, team) <= 0) {
            player.addChatMessage(new net.minecraft.util.ChatComponentText("No TDM kits have been saved for " + team.name + " on map " + (mapName.length() == 0 ? "global" : mapName) + ". Ask an admin to use /tdm kit add " + team.name + " [map]."));
            return;
        }

        pendingKitSelection.add(getPlayerKey(player));
        int[] costs=TDMKitManager.getKitCosts(mapName,team); boolean buying=isBombMode(player.worldObj)&&TDMBombManager.getState()==TDMBombManager.BombRoundState.PRE_ROUND; TDMMap map=getSelectedMapData(player.worldObj); boolean economy=buying&&map!=null&&map.buyScoreEnabled; if(!economy)java.util.Arrays.fill(costs,0);
        PacketDispatcher.wrapper.sendTo(new TDMKitGuiPacket(team.name, TDMKitManager.getKitNames(mapName, team),costs,economy,getBuyScore(player),buying?TDMBombManager.getRemainingSeconds(player.worldObj):0,buying), (EntityPlayerMP) player);
    }

    public static void tickKitSelection(EntityPlayer player) {
        boolean bombBuyRestricted=isEnabled(player.worldObj)&&isBombMode(player.worldObj)&&TDMBombManager.getState()==TDMBombManager.BombRoundState.PRE_ROUND&&!TDMSpectatorManager.isObserving(player);
        if (!bombBuyRestricted&&!pendingKitSelection.contains(getPlayerKey(player))) {
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

    public static KitSelectionResult selectKit(EntityPlayer player, int kitIndex) {
        if (TDMSpectatorManager.isObserving(player)) return KitSelectionResult.BUY_PHASE_ENDED;
        if (!pendingKitSelection.contains(getPlayerKey(player))) {
            return KitSelectionResult.ALREADY_SELECTED;
        }

        if (!isEnabled(player.worldObj) || isMapVoteActive(player.worldObj)) {
            pendingKitSelection.remove(getPlayerKey(player));
            return KitSelectionResult.BUY_PHASE_ENDED;
        }

        Team team = getOrAssignPlayerTeam(player); String mapName=getSelectedMap(player.worldObj);
        boolean buying=isBombMode(player.worldObj); if(buying&&TDMBombManager.getState()!=TDMBombManager.BombRoundState.PRE_ROUND)return KitSelectionResult.BUY_PHASE_ENDED;
        int savedCost=TDMKitManager.getKitCost(mapName,team,kitIndex); if(savedCost<0)return KitSelectionResult.INVALID_SELECTION;
        TDMMap map=getSelectedMapData(player.worldObj);int effectiveCost=map!=null&&map.buyScoreEnabled?savedCost:0;if(getBuyScore(player)<effectiveCost)return KitSelectionResult.INSUFFICIENT_FUNDS;
        if (!TDMKitManager.applyKit(mapName, team, kitIndex, player)) return KitSelectionResult.INVALID_SELECTION;
        if(effectiveCost>0){TDMData d=TDMData.get(player.worldObj);d.playerBuyScores.put(getPlayerKey(player),Integer.valueOf(getBuyScore(player)-effectiveCost));d.markDirty();}
        pendingKitSelection.remove(getPlayerKey(player));
        player.removePotionEffect(Potion.resistance.id);
        player.removePotionEffect(Potion.regeneration.id);
        if(player instanceof EntityPlayerMP){EntityPlayerMP mp=(EntityPlayerMP)player;mp.inventory.markDirty();mp.inventoryContainer.detectAndSendChanges();sendStatusToAll(player.worldObj);PacketDispatcher.wrapper.sendTo(new TDMKitSelectResultPacket(KitSelectionResult.SUCCESS),(EntityPlayerMP)player);}
        return KitSelectionResult.SUCCESS;
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
