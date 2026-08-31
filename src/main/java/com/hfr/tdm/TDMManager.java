package com.hfr.tdm;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMKitGuiPacket;
import com.hfr.packet.effect.TDMKitSelectResultPacket;
import com.hfr.packet.effect.TDMMapVoteGuiPacket;
import com.hfr.packet.effect.TDMStatusPacket;
import com.hfr.packet.effect.TDMSurvivorChoiceGuiPacket;
import com.hfr.packet.effect.TDMSoundPacket;
import com.hfr.compat.HbmCsgoChargeIntegration;
import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.*;

public class TDMManager {

    public static boolean tdmEnabled = false;
    public static final int ROUND_TICKS = 20 * 60 * 20;
    public static final int MAP_VOTE_TICKS = 30 * 20;
    public static final int SKIP_VOTE_TICKS = 60 * 20;
    public static final int SCORE_LIMIT = 10000;
    public static final int POINTS_PER_KILL = 100;
    /** BOMB maps are first-team-to-N round wins; maps may override this default of 13. */
    public static final int BOMB_SCORE_LIMIT = 13;
    public static final int BOMB_ROUND_TICKS = 120 * 20;
    public static final int BUY_TIME_TICKS = 20 * 20;
    public static final int TEAM_CHANGE_COOLDOWN_TICKS = 120 * 20;
    private static final Set<String> pendingKitSelection = new HashSet<String>();
    private static final Set<String> buyProtectionActive = new HashSet<String>();
    private static final Set<String> respawnLockProtectionActive = new HashSet<String>();
    private static final Map<String, KitSelectionContext> kitSelectionContexts = new HashMap<String, KitSelectionContext>();
    private static final Map<String, String> pendingKitMaps = new HashMap<String, String>();
    private static final Map<String, FreezeAnchor> freezeAnchors = new HashMap<String, FreezeAnchor>();
    private static final Map<String, Long> nextTeamChangeTick = new HashMap<String, Long>();
    private static boolean bombTestMode;
    private static final Set<String> teamlessPlayers = new HashSet<String>();
    /** Explicit server-owned state for participants who have no life in the current round. */
    private static final Set<String> roundWaitingPlayers = new HashSet<String>();
    private static final Map<String, FreezeAnchor> roundWaitingAnchors = new HashMap<String, FreezeAnchor>();
    /** Last successfully purchased kit; eligibility is granted separately at round completion. */
    private static final Map<String, SelectedKit> selectedKits = new HashMap<String, SelectedKit>();
    private static final Set<String> survivorChoicePending = new HashSet<String>();
    private static final Set<String> ffaEliminated = new HashSet<String>();
    /** Eliminated players owned exclusively by an active hardcore DEATHMATCH round. */
    private static final Set<String> deathmatchEliminated = new HashSet<String>();
    private static long ffaNextRoundTick;
    private static String ffaRoundWinner;
    /** Names captured when the current non-BOMB competitive round starts. */
    private static final Set<String> activeRoundParticipants = new HashSet<String>();
    /** Guards the authoritative non-BOMB round-end path against death/tick re-entry. */
    private static boolean nonBombRoundEnding;

    public static void makePlayerTeamless(EntityPlayer player) {
        if (player == null) return;
        TDMData data = TDMData.get(player.worldObj);
        data.playerTeams.remove(getPlayerKey(player));
        data.playerKillScores.remove(getPlayerKey(player));
        data.playerPointScores.remove(getPlayerKey(player));
        TDMPurchasableManager.clearPending(player);
        teamlessPlayers.add(getPlayerKey(player));
        cancelKitSelection(player);
        clearRoundPlayerState(player);
        TDMSpectatorManager.restore(player);
        data.markDirty();
        sendStatusToAll(player.worldObj);
    }

    public static String playConfiguredSound(World world, String eventType, String[] variants, Team onlyTeam) {
        return playConfiguredSound(world, eventType, variants, onlyTeam, null);
    }

    public static String playConfiguredSound(World world, String eventType, String[] variants, Team onlyTeam, EntityPlayerMP onlyPlayer) {
        if (world == null || variants == null || world.isRemote) return null;
        List<String> valid = new ArrayList<String>();
        for (String sound : variants) {
            String normalized = normalizeSoundEventId(sound);
            if (normalized != null) valid.add(normalized);
        }
        if (valid.isEmpty()) return null;
        String sound = valid.get(world.rand.nextInt(valid.size()));
        int recipients = 0;
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (onlyPlayer != null && player != onlyPlayer) continue;
            if (player.worldObj != world || (onlyPlayer == null && TDMSpectatorManager.isObserving(player))) continue;
            Team team = getPlayerTeam(world, player.getCommandSenderName());
            if (onlyPlayer == null && (team == null || (onlyTeam != null && team != onlyTeam))) continue;
            PacketDispatcher.wrapper.sendTo(new TDMSoundPacket(sound), player);
            recipients++;
        }
        if (XFConfig.enableDebugLogging && MainRegistry.logger != null)
            MainRegistry.logger.info("TDM SOUND dispatch: type={}, configured={}, event={}, recipients={}, route=explicit-player-packet", eventType, Arrays.toString(variants), sound, recipients);
        return sound;
    }

    /** Config values are event IDs. Unqualified IDs use this mod's namespace; explicit namespaces are preserved. */
    public static String normalizeSoundEventId(String configured) {
        if (configured == null) return null;
        String value = configured.trim();
        if (value.length() == 0) return null;
        return value.indexOf(':') < 0 ? "hfr:" + value : value;
    }

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

    public enum TDMGameMode { DEATHMATCH, BOMB, FFA }
    public enum BombRole { TERRORIST, COUNTER_TERRORIST }
    public enum KitSelectionResult { SUCCESS, INSUFFICIENT_FUNDS, INVALID_SELECTION, BUY_PHASE_ENDED, ALREADY_SELECTED }
    /** BUY_PHASE is competitive BOMB economy state; LOADOUT_SELECTION is economy-free DM/FFA state. */
    public enum KitSelectionContext { NONE, BUY_PHASE, RESPAWN_LOCK, LOADOUT_SELECTION }

    private static final class SelectedKit {
        final Team pool;
        final int index;

        SelectedKit(Team pool, int index) {
            this.pool = pool;
            this.index = index;
        }
    }
    private static final class FreezeAnchor { final int dimension; final double x,y,z; FreezeAnchor(EntityPlayer p){dimension=p.dimension;x=p.posX;y=p.posY;z=p.posZ;} }

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
        public boolean buyScoreEnabled = true;
        /** Killstreak currency is opt-in and intended for respawn-based modes. */
        public boolean killstreaksEnabled;
        public int killScoreReward = 1;
        public int roundLossBuyScoreReward = 1;
        public int killBuyScoreReward = 2;
        public int roundWinBuyScoreReward = 3;
        public int bombDefuseBuyScoreReward;
        public int bombPlantBuyScoreReward = 1;
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
    public static BombRole getBombRole(World world, Team team) { return team == null ? null : (team == getTerroristTeam(world) ? BombRole.TERRORIST : BombRole.COUNTER_TERRORIST); }
    public static BombRole getBombRole(EntityPlayer player) { return getBombRole(player.worldObj, getOrAssignPlayerTeam(player)); }
    public static boolean isTerrorist(EntityPlayer player) { return getBombRole(player) == BombRole.TERRORIST; }
    public static boolean isCounterTerrorist(EntityPlayer player) { return getBombRole(player) == BombRole.COUNTER_TERRORIST; }
    public static boolean isHardcoreRespawns(World world) {
        TDMMap map = getSelectedMapData(world);
        return map != null && map.hardcoreRespawns;
    }
    public static boolean isBombMode(World world) { return getGameMode(world) == TDMGameMode.BOMB; }
    public static boolean isFfaMode(World world) { return getGameMode(world) == TDMGameMode.FFA; }

    /** Returns the live TDM label consumed by Xenofactions' existing global-chat prefix pipeline. */
    public static String getChatPrefix(EntityPlayer player) {
        if (player == null || player.worldObj == null || !isEnabled(player.worldObj) || !isCompetitivePlayer(player)) return null;
        if (isFfaMode(player.worldObj)) return EnumChatFormatting.GOLD + "[FFA]";
        Team team = getPlayerTeam(player.worldObj, player.getCommandSenderName());
        if (team == null) return null;
        if (isBombMode(player.worldObj)) {
            BombRole role = getBombRole(player.worldObj, team);
            return role == BombRole.TERRORIST
                    ? EnumChatFormatting.RED + "[T]"
                    : EnumChatFormatting.BLUE + "[CT]";
        }
        return team == Team.BLUE
                ? EnumChatFormatting.BLUE + "[BLUE]"
                : EnumChatFormatting.RED + "[RED]";
    }

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

        boolean changingSelectedActiveMap = data.enabled && data.selectedMap.equals(map.name);
        if (changingSelectedActiveMap) {
            stopSelectedMapLifecycle(world);
        }

        map.mode = mode;
        data.markDirty();
        if (!changingSelectedActiveMap) return true;

        // The old lifecycle is fully stopped before the new mode can create player state.
        data.roundEndTick = 0;
        startRound(world, false);
        return true;
    }

    /** Stops ownership belonging to the selected mode before its map data is changed. */
    private static void stopSelectedMapLifecycle(World world) {
        TDMBombManager.cleanup(world, true);
        clearAllModeEliminationState();
        resetTDMTransientPlayerState(world);
    }

    public static boolean isBombTestMode() { return bombTestMode; }

    public static void setBombTestMode(World world, boolean enabled) {
        if (bombTestMode == enabled) return;
        bombTestMode = enabled;
        if (isEnabled(world) && isBombMode(world)) TDMBombManager.onTestModeChanged(world, enabled);
        sendStatusToAll(world);
    }

    /** Cancels all active selections and their TDM-owned effects as one lifecycle operation. */
    public static void clearPendingKitSelections() { cancelAllKitSelections(); }

    public static void cancelAllKitSelections() {
        for (EntityPlayerMP player : getOnlinePlayers()) {
            String playerKey = getPlayerKey(player);
            if (pendingKitSelection.contains(playerKey)
                    || buyProtectionActive.contains(playerKey)
                    || respawnLockProtectionActive.contains(playerKey)
                    || freezeAnchors.containsKey(playerKey)) {
                cancelKitSelection(player);
            }
        }
        pendingKitSelection.clear();
        buyProtectionActive.clear();
        respawnLockProtectionActive.clear();
        kitSelectionContexts.clear();
        pendingKitMaps.clear();
        freezeAnchors.clear();
    }

    public static void cancelKitSelections(World world) {
        if (world == null) return;
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (player.worldObj.provider.dimensionId == world.provider.dimensionId) cancelKitSelection(player);
        }
    }

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
        buyProtectionActive.clear();
        respawnLockProtectionActive.clear();
        kitSelectionContexts.clear();
        pendingKitMaps.clear();
        freezeAnchors.clear();
        nextTeamChangeTick.clear();
        teamlessPlayers.clear();
        roundWaitingPlayers.clear(); roundWaitingAnchors.clear(); selectedKits.clear(); survivorChoicePending.clear();
        ffaEliminated.clear();
        deathmatchEliminated.clear();
        ffaNextRoundTick=0L; ffaRoundWinner=null; activeRoundParticipants.clear();
        nonBombRoundEnding=false;
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
            clearAllModeEliminationState();
            resetTDMTransientPlayerState(world);
            data.roundEndTick = 0;
            data.mapVoteActive = false;
            data.mapVoteEndTick = 0;
            clearSkipVote(data);
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

        stopSelectedMapLifecycle(world);
        data.playerBuyScores.clear();
        clearSkipVote(data);
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
        if (!data.enabled || !data.mapVoteActive || !data.maps.containsKey(normalized)
                || normalized.equals(normalizeMapName(data.selectedMap))) {
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
            if (mapName.equals(normalizeMapName(data.selectedMap))) continue;
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
        if (data.skipVoteActive) tickSkipVote(world);
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

        if (isFfaMode(world) && ffaNextRoundTick > 0L) {
            if (now >= ffaNextRoundTick) startFfaRound(world);
            else if (now % 20 == 0) sendStatusToAll(world);
            return;
        }

        if (data.roundEndTick <= 0 || now > data.roundEndTick + MAP_VOTE_TICKS) {
            startRound(world, false);
            return;
        }

        int scoreLimit = getEffectiveScoreLimit(world);
        if (now >= data.roundEndTick || data.redPointScore >= scoreLimit || data.bluePointScore >= scoreLimit) {
            finishNonBombRound(world);
            return;
        }

        if (now % 20 == 0) {
            sendStatusToAll(world);
        }
    }

    /**
     * Starts a full match only after the selected map can own participant spawning.
     * An enabled TDM round must never fall through to vanilla worldspawn when a
     * selected-map spawn is available.
     */
    public static void startRound(World world, boolean resetVotes) {
        if (!hasUsableSelectedMapSpawns(world)) {
            stopInvalidMatchStart(world);
            return;
        }

        startMatch(world, resetVotes);
    }

    /** Starts a validated full map match; bomb combat rounds own their own placement lifecycle. */
    private static void startMatch(World world, boolean resetVotes) {
        clearAllModeEliminationState();
        for(EntityPlayerMP player:getOnlinePlayers())clearRoundPlayerState(player);
        TDMData data = TDMData.get(world);
        data.redPointScore = 0;
        data.bluePointScore = 0;
        data.playerKills.clear();
        data.playerDeaths.clear();
        data.playerBuyScores.clear();
        data.playerKillScores.clear();
        data.playerPointScores.clear();
        TDMPurchasableManager.clearAllPending();
        nonBombRoundEnding = false;
        data.roundEndTick = world.getTotalWorldTime() + getEffectiveRoundTicks(world);
        data.mapVoteActive = false;
        data.mapVoteEndTick = 0;
        clearSkipVote(data);
        if (resetVotes) {
            data.mapVotes.clear();
        }
        data.markDirty();
        TDMSpectatorManager.restoreAll();
        activeRoundParticipants.clear();
        if (isBombMode(world)) {
            TDMBombManager.startMatch(world);
        } else {
            TDMBombManager.cleanup(world, true);
            if(isFfaMode(world)){ffaEliminated.clear();ffaNextRoundTick=0L;ffaRoundWinner=null;}
            placeAllPlayersAtSelectedMap(world, KitSelectionContext.LOADOUT_SELECTION);
            captureActiveRoundParticipants(world, activeRoundParticipants);
        }
        sendStatusToAll(world);
    }

    /** Adds non-spendable DEATHMATCH point score; this is never killstreak currency. */
    public static void addTeamPointScore(World world, Team scoringTeam) {
        TDMData data = TDMData.get(world);
        if (!data.enabled || data.mapVoteActive || scoringTeam == null) {
            return;
        }

        if (isBombMode(world)) { sendStatusToAll(world); return; }
        if (scoringTeam == Team.RED) {
            data.redPointScore = addPoint(data.redPointScore);
        } else if (scoringTeam == Team.BLUE) {
            data.bluePointScore = addPoint(data.bluePointScore);
        }
        data.markDirty();

        int scoreLimit = getEffectiveScoreLimit(world);
        if (data.redPointScore >= scoreLimit || data.bluePointScore >= scoreLimit) {
            finishNonBombRound(world);
        } else {
            sendStatusToAll(world);
        }
    }

    private static int addPoint(int score) {
        return score > Integer.MAX_VALUE - POINTS_PER_KILL ? Integer.MAX_VALUE : score + POINTS_PER_KILL;
    }

    /** Awards both independent kill-derived resources after the death handler validates the kill. */
    public static void awardValidKillResources(EntityPlayer attacker) {
        if (attacker == null || !isCompetitivePlayer(attacker) || isBombMode(attacker.worldObj)) return;
        TDMMap map = getSelectedMapData(attacker.worldObj);
        if (map == null) return;
        if (map.killstreaksEnabled && map.killScoreReward > 0) addPlayerKillScore(attacker, map.killScoreReward);
        // Point score is last because reaching the threshold may synchronously end/reset the match.
        if (map.mode == TDMGameMode.FFA) addPlayerPointScore(attacker, POINTS_PER_KILL);
    }

    public static int getPlayerPointScore(EntityPlayer player) { return player == null ? 0 : getPlayerPointScore(player.worldObj, getPlayerKey(player)); }
    public static int getPlayerPointScore(World world, String playerName) { Integer value=TDMData.get(world).playerPointScores.get(playerName==null?"":playerName.toLowerCase());return value==null?0:Math.max(0,value.intValue()); }
    public static int getPlayerKillScore(EntityPlayer player) { Integer value=player==null?null:TDMData.get(player.worldObj).playerKillScores.get(getPlayerKey(player));return value==null?0:Math.max(0,value.intValue()); }
    private static void addPlayerPointScore(EntityPlayer player,int amount){if(amount<=0||nonBombRoundEnding)return;TDMData data=TDMData.get(player.worldObj);String key=getPlayerKey(player);int old=getPlayerPointScore(player);int next=old>Integer.MAX_VALUE-amount?Integer.MAX_VALUE:old+amount;data.playerPointScores.put(key,Integer.valueOf(next));data.markDirty();if(next>=getEffectiveScoreLimit(player.worldObj))finishNonBombRound(player.worldObj);else sendStatusToAll(player.worldObj);}
    private static void addPlayerKillScore(EntityPlayer player,int amount){if(amount<=0)return;TDMData data=TDMData.get(player.worldObj);String key=getPlayerKey(player);int old=getPlayerKillScore(player);int next=old>Integer.MAX_VALUE-amount?Integer.MAX_VALUE:old+amount;data.playerKillScores.put(key,Integer.valueOf(next));data.markDirty();sendStatusToAll(player.worldObj);}
    public static boolean spendPlayerKillScore(EntityPlayer player,int cost){if(player==null||cost<0)return false;TDMMap map=getSelectedMapData(player.worldObj);if(map==null||!map.killstreaksEnabled||(map.mode!=TDMGameMode.DEATHMATCH&&map.mode!=TDMGameMode.FFA))return false;TDMData data=TDMData.get(player.worldObj);int balance=getPlayerKillScore(player);if(balance<cost)return false;data.playerKillScores.put(getPlayerKey(player),Integer.valueOf(balance-cost));data.markDirty();sendStatusToAll(player.worldObj);return true;}

    private static void finishNonBombRound(World world) {
        if (nonBombRoundEnding || isBombMode(world) || !isEnabled(world)) return;
        nonBombRoundEnding = true;
        String result = getNonBombRoundResult(world);
        for (EntityPlayerMP player : getOnlinePlayers()) if (player.worldObj == world) player.addChatMessage(new ChatComponentText(result));
        sendStatusToAll(world);
        if (hasAlternativeMap(TDMData.get(world))) startMapVote(world); else startRound(world, true);
    }

    private static String getNonBombRoundResult(World world) {
        TDMData data=TDMData.get(world);
        if(getGameMode(world)==TDMGameMode.DEATHMATCH){if(data.redPointScore==data.bluePointScore)return "Deathmatch ended in a draw at "+data.redPointScore+" points.";Team winner=data.redPointScore>data.bluePointScore?Team.RED:Team.BLUE;return winner.name.toUpperCase()+" wins Deathmatch "+Math.max(data.redPointScore,data.bluePointScore)+" to "+Math.min(data.redPointScore,data.bluePointScore)+".";}
        String winner=null;int best=-1;boolean tied=false;for(Map.Entry<String,Integer> entry:data.playerPointScores.entrySet()){int points=Math.max(0,entry.getValue().intValue());if(points>best){winner=entry.getKey();best=points;tied=false;}else if(points==best){tied=true;}}
        return winner==null||tied?"FFA ended in a draw at "+Math.max(0,best)+" points.":winner+" wins FFA with "+best+" points.";
    }

    /** Handles automatic DEATHMATCH rotation without changing manual skip-vote behavior. */
    private static void rotateAfterDeathmatch(World world) {
        TDMData data = TDMData.get(world);
        if (hasAlternativeMap(data)) {
            startMapVote(world);
            return;
        }

        // A same-map restart is a complete new match, not a map vote or timer extension.
        // resetVotes also removes any stale persisted votes without changing selectedMap.
        startRound(world, true);
    }

    private static boolean hasAlternativeMap(TDMData data) {
        return !getAlternativeMapNames(data).isEmpty();
    }

    public static void startMapVote(World world) {
        TDMData data = TDMData.get(world);
        if (data.mapVoteActive) {
            return;
        }

        clearSkipVote(data);
        if (getAlternativeMapNames(data).isEmpty()) {
            data.roundEndTick = world.getTotalWorldTime() + getEffectiveRoundTicks(world);
            data.markDirty();
            broadcastTDM(world, "TDM map vote not started: no alternative to the current map is available.");
            sendStatusToAll(world);
            return;
        }

        TDMBombManager.cleanup(world, true);
        resetTDMTransientPlayerState(world);
        data.playerBuyScores.clear();

        data.mapVoteActive = true;
        data.mapVoteEndTick = world.getTotalWorldTime() + MAP_VOTE_TICKS;
        data.mapVotes.clear();
        data.markDirty();
        // Voting changes combat eligibility, not spatial ownership of the selected map.
        placeAllPlayersAtSelectedMap(world, KitSelectionContext.NONE);
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
        // selectedMap is final before startRound resolves any player spawn.
        startRound(world, false);
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
        return team == Team.RED ? data.redPointScore : data.bluePointScore;
    }

    public static void sendStatusToAll(World world) {
        TDMData data = TDMData.get(world);
        for (EntityPlayerMP player : getOnlinePlayers()) {
            PacketDispatcher.wrapper.sendTo(new TDMStatusPacket(
                        data.enabled,
                        data.mapVoteActive,
                        getRemainingRoundSeconds(world),
                        getRemainingVoteSeconds(world),
                        data.redPointScore,
                        data.bluePointScore, data.selectedMap,
                        getGameMode(world).name(), TDMBombManager.getState().name(),
                        data.redBombWins, data.blueBombWins, getTerroristTeam(world).name,
                        TDMBombManager.getRemainingSeconds(world), TDMBombManager.getPlantedSite(),
                        getSelectedMapData(world)!=null&&getSelectedMapData(world).buyScoreEnabled, getBuyScore(player),
                        TDMBombManager.getPlayerCount(world, Team.RED), TDMBombManager.getPlayerCount(world, Team.BLUE)
                ), player);
        }
    }

    private static void sendMapVoteGuiToAll(World world) {
        List<String> mapNames = getMapNames(world);
        if (mapNames.isEmpty()) {
            return;
        }

        String[] maps = mapNames.toArray(new String[mapNames.size()]);
        for (EntityPlayerMP player : getOnlinePlayers()) {
            PacketDispatcher.wrapper.sendTo(new TDMMapVoteGuiPacket(maps, MAP_VOTE_TICKS / 20, getSelectedMap(world)), player);
        }
    }

    private static List<String> getAlternativeMapNames(TDMData data) {
        List<String> alternatives = new ArrayList<String>();
        String current = normalizeMapName(data.selectedMap);
        for (String map : data.maps.keySet()) if (!map.equals(current)) alternatives.add(map);
        return alternatives;
    }

    public static String castSkipVote(World world, EntityPlayer player, boolean yes) {
        TDMData data = TDMData.get(world);
        if (!data.enabled || data.mapVoteActive) return "A skip vote is unavailable right now.";
        String key = getPlayerKey(player);
        if (!isEligibleSkipVoter(data, player)) return "Only active TDM competitors may vote to skip.";
        if (!data.skipVoteActive) {
            if (getAlternativeMapNames(data).isEmpty()) return "The current map cannot be skipped because no alternative map is available.";
            data.skipVoteActive = true;
            data.skipVoteEndTick = world.getTotalWorldTime() + SKIP_VOTE_TICKS;
            data.skipVoteInitiator = player.getCommandSenderName();
            data.skipVotes.clear();
            broadcastTDM(world, player.getCommandSenderName() + " started a vote to skip the current map.");
            broadcastTDM(world, "Vote with /tdm skip yes or /tdm skip no.");
            broadcastTDM(world, "Votes needed to pass: " + requiredSkipVotes(countEligibleSkipVoters(data)) + ".");
        }
        if (data.skipVotes.containsKey(key)) return "You have already voted in this skip vote.";
        data.skipVotes.put(key, Boolean.valueOf(yes));
        data.markDirty();
        int required = requiredSkipVotes(countEligibleSkipVoters(data));
        int yesVotes = countSkipYesVotes(data);
        broadcastTDM(world, player.getCommandSenderName() + " voted " + (yes ? "YES" : "NO")
                + ". [" + yesVotes + "/" + required + " YES votes required]");
        evaluateSkipVote(world);
        return null;
    }

    public static String getSkipVoteStatus(World world) {
        TDMData data = TDMData.get(world);
        if (!data.skipVoteActive) return "No skip-map vote is active.";
        int eligible = countEligibleSkipVoters(data);
        int yes = countSkipYesVotes(data);
        return "The vote to skip the current map has " + yes + " YES " + (yes == 1 ? "vote" : "votes")
                + "; " + requiredSkipVotes(eligible) + " YES votes are required to pass.";
    }

    public static void onPlayerDisconnected(World world, EntityPlayer player) {
        TDMData data = TDMData.get(world);
        if (!data.skipVoteActive) return;
        data.skipVotes.remove(getPlayerKey(player));
        evaluateSkipVote(world);
    }

    private static void tickSkipVote(World world) {
        TDMData data = TDMData.get(world);
        if (world.getTotalWorldTime() >= data.skipVoteEndTick) {
            broadcastTDM(world, "The vote to skip the current map expired.");
            clearSkipVote(data);
            data.markDirty();
            return;
        }
        evaluateSkipVote(world);
    }

    private static void evaluateSkipVote(World world) {
        TDMData data = TDMData.get(world);
        if (!data.skipVoteActive) return;
        Set<String> eligible = new HashSet<String>();
        for (EntityPlayerMP player : getOnlinePlayers()) if (isEligibleSkipVoter(data, player)) eligible.add(getPlayerKey(player));
        data.skipVotes.keySet().retainAll(eligible);
        int required = requiredSkipVotes(eligible.size()), yes = 0;
        for (Boolean vote : data.skipVotes.values()) if (vote.booleanValue()) yes++;
        if (yes >= required && required > 0) {
            broadcastTDM(world, "The vote passed. Skipping the current map...");
            clearSkipVote(data);
            data.markDirty();
            startMapVote(world); // Owns objective, combat, freeze, kit, buy-phase, and transition cleanup; awards no result.
        } else if (yes + (eligible.size() - data.skipVotes.size()) < required) {
            broadcastTDM(world, "The vote to skip the current map failed.");
            clearSkipVote(data);
            data.markDirty();
        }
    }

    private static boolean isEligibleSkipVoter(TDMData data, EntityPlayer player) {
        return isCompetitivePlayer(player);
    }

    private static int countEligibleSkipVoters(TDMData data) {
        int count = 0;
        for (EntityPlayerMP player : getOnlinePlayers()) if (isEligibleSkipVoter(data, player)) count++;
        return count;
    }

    private static int requiredSkipVotes(int eligible) { return eligible / 2 + 1; }

    private static int countSkipYesVotes(TDMData data) {
        int yes = 0;
        for (Boolean vote : data.skipVotes.values()) if (vote.booleanValue()) yes++;
        return yes;
    }

    private static void clearSkipVote(TDMData data) {
        data.skipVoteActive = false; data.skipVoteEndTick = 0; data.skipVoteInitiator = ""; data.skipVotes.clear();
    }

    private static void broadcastTDM(World world, String message) {
        for (EntityPlayerMP player : getOnlinePlayers()) player.addChatMessage(new ChatComponentText(message));
    }

    public static void placeAllPlayersAtSelectedMap(World world, KitSelectionContext context) {
        Random random = new Random();
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (!canPlaceAtTdmSpawn(player)) {
                continue;
            }

            cancelKitSelection(player);
            if (respawnPlayer(player, random)) {
                if (!TDMSpectatorManager.isObserving(player) && context != KitSelectionContext.NONE) {
                    promptForKit(player, context);
                }
            }
        }
    }

    private static boolean hasUsableSelectedMapSpawns(World world) {
        TDMData data = TDMData.get(world);
        TDMMap map = data.maps.get(data.selectedMap);
        if (map == null) {
            logInvalidSpawn(null, data.selectedMap, null, world.provider.dimensionId);
            return false;
        }

        if(map.mode==TDMGameMode.FFA){
            boolean available=hasSpawnForTeam(data,map,null);
            if(!available)logInvalidSpawn(null,map.name,null,world.provider.dimensionId);
            return available;
        }
        boolean redAvailable = hasSpawnForTeam(data, map, Team.RED);
        boolean blueAvailable = hasSpawnForTeam(data, map, Team.BLUE);
        if (!redAvailable) {
            logMissingTeamSpawnForParticipants(data, map.name, Team.RED, world.provider.dimensionId);
        }
        if (!blueAvailable) {
            logMissingTeamSpawnForParticipants(data, map.name, Team.BLUE, world.provider.dimensionId);
        }
        return redAvailable && blueAvailable;
    }

    private static boolean hasSpawnForTeam(TDMData data, TDMMap map, Team team) {
        List<SpawnPoint> source = map.spawns.isEmpty() ? data.spawns : map.spawns;
        for (SpawnPoint spawn : source) {
            if (spawn.team == team
                    && net.minecraftforge.common.DimensionManager.isDimensionRegistered(spawn.dim)) {
                return true;
            }
        }
        return false;
    }

    private static void logMissingTeamSpawnForParticipants(
            TDMData data,
            String mapName,
            Team team,
            int dimension
    ) {
        boolean foundParticipant = false;
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (data.playerTeams.get(getPlayerKey(player)) == team) {
                logInvalidSpawn(player, mapName, team, player.dimension);
                foundParticipant = true;
            }
        }
        if (!foundParticipant) {
            logInvalidSpawn(null, mapName, team, dimension);
        }
    }

    private static void stopInvalidMatchStart(World world) {
        TDMData data = TDMData.get(world);
        data.roundEndTick = 0;
        data.mapVoteActive = false;
        data.mapVoteEndTick = 0;
        clearSkipVote(data);
        data.markDirty();
        TDMBombManager.cleanup(world, true);
        sendStatusToAll(world);
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
        if(isFfaMode(player.worldObj)){player.addChatMessage(new ChatComponentText("FFA has no teams."));return false;}
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
        cancelKitSelection(player);
        clearRoundPlayerState(player);
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
        teamlessPlayers.remove(playerName.toLowerCase());
        data.playerTeams.put(playerName.toLowerCase(), team);
        data.markDirty();
    }

    public static Team getPlayerTeam(World world, String playerName) {
        return TDMData.get(world).playerTeams.get(playerName.toLowerCase());
    }

    public static Team getOrAssignPlayerTeam(EntityPlayer player) {
        TDMData data = TDMData.get(player.worldObj);
        String playerName = player.getCommandSenderName().toLowerCase();
        if(isFfaMode(player.worldObj)) return null;
        Team team = data.playerTeams.get(playerName);

        if (team != null) {
            return team;
        }
        if (teamlessPlayers.contains(playerName)) return null;

        team = getSmallestTeam(data);
        data.playerTeams.put(playerName, team);
        data.markDirty();
        return team;
    }

    public static boolean hasPlayerTeam(EntityPlayer player) {
        return isCompetitivePlayer(player);
    }

    public static boolean isCompetitivePlayer(EntityPlayer player){
        if(player==null||teamlessPlayers.contains(getPlayerKey(player))||TDMSpectatorManager.isObserving(player))return false;
        return isFfaMode(player.worldObj)||getPlayerTeam(player.worldObj,player.getCommandSenderName())!=null;
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
            if (!teamlessPlayers.contains(playerName) && !data.playerTeams.containsKey(playerName)) {
                data.playerTeams.put(playerName, getSmallestTeam(data));
                refreshPlayerPlacementAfterTeamChange(player);
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

            setPlayerTeam(world, playerToMove.getCommandSenderName(), smaller);
            refreshPlayerPlacementAfterTeamChange(playerToMove);
            playerToMove.addChatMessage(new ChatComponentText("You were moved to " + smaller.name + " to balance TDM teams."));
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
    public static boolean spendBuyScore(EntityPlayer player,int amount){if(player==null||amount<0)return false;TDMMap map=getSelectedMapData(player.worldObj);if(map==null||map.mode!=TDMGameMode.BOMB||!map.buyScoreEnabled||!isGlobalBombBuyPeriod(player))return false;int balance=getBuyScore(player);if(balance<amount)return false;TDMData data=TDMData.get(player.worldObj);data.playerBuyScores.put(getPlayerKey(player),Integer.valueOf(balance-amount));data.markDirty();sendStatusToAll(player.worldObj);return true;}
    public static void addBuyScore(EntityPlayer player,int amount){TDMMap map=getSelectedMapData(player.worldObj);if(map==null||map.mode!=TDMGameMode.BOMB||!map.buyScoreEnabled||amount<=0)return;TDMData d=TDMData.get(player.worldObj);int old=getBuyScore(player);int next=old>Integer.MAX_VALUE-amount?Integer.MAX_VALUE:old+amount;d.playerBuyScores.put(getPlayerKey(player),Integer.valueOf(next));d.markDirty();sendStatusToAll(player.worldObj);}
    public static void awardKillBuyScore(EntityPlayer player){TDMMap map=getSelectedMapData(player.worldObj);if(map!=null&&map.mode==TDMGameMode.BOMB&&map.buyScoreEnabled&&isCompetitivePlayer(player)&&TDMBombManager.isRoundActive())addBuyScore(player,map.killBuyScoreReward);}
    public static void awardRoundWinBuyScore(World world,Team team,EntityPlayer individual){TDMMap map=getSelectedMapData(world);if(map==null)return;if(individual!=null){if(isCompetitivePlayer(individual))addBuyScore(individual,map.roundWinBuyScoreReward);return;}for(EntityPlayerMP p:getOnlinePlayers())if(p.worldObj==world&&isCompetitivePlayer(p)&&getPlayerTeam(world,p.getCommandSenderName())==team)addBuyScore(p,map.roundWinBuyScoreReward);}
    /** Finalizes competitive BOMB winner and loser economy once the authoritative result is known. */
    public static void awardRoundResultBuyScore(World world, Team winningTeam, String individualWinner, Set<String> participants) {
        TDMMap map=getSelectedMapData(world); if(map==null||map.mode!=TDMGameMode.BOMB||!map.buyScoreEnabled||participants==null)return;
        TDMData data=TDMData.get(world);
        for(String key:participants){
            if(teamlessPlayers.contains(key))continue;
            boolean won=individualWinner!=null?individualWinner.equals(key):(winningTeam!=null&&data.playerTeams.get(key)==winningTeam);
            int amount=won?map.roundWinBuyScoreReward:map.roundLossBuyScoreReward;
            if(amount<=0)continue;
            Integer value=data.playerBuyScores.get(key);int old=value==null?0:Math.max(0,value.intValue());
            data.playerBuyScores.put(key,Integer.valueOf(old>Integer.MAX_VALUE-amount?Integer.MAX_VALUE:old+amount));
        }
        data.markDirty();sendStatusToAll(world);
    }
    public static void awardDefuseBuyScore(EntityPlayer player){TDMMap map=getSelectedMapData(player.worldObj);if(map!=null&&map.buyScoreEnabled)addBuyScore(player,map.bombDefuseBuyScoreReward);}
    public static void awardBombPlantBuyScore(EntityPlayer player){TDMMap map=getSelectedMapData(player.worldObj);if(map!=null&&map.buyScoreEnabled&&isCompetitivePlayer(player))addBuyScore(player,map.bombPlantBuyScoreReward);}
    public static void captureActiveRoundParticipants(World world, Set<String> target){target.clear();for(EntityPlayerMP p:getOnlinePlayers())if(p.worldObj==world&&isCompetitivePlayer(p))target.add(getPlayerKey(p));}
    public static void clearKitSelection(EntityPlayer player) {
        cancelKitSelection(player);
    }
    public static boolean hasSelectedKit(EntityPlayer player){return !pendingKitSelection.contains(getPlayerKey(player));}
    public static KitSelectionContext getKitSelectionContext(EntityPlayer player){KitSelectionContext c=player==null?null:kitSelectionContexts.get(getPlayerKey(player));return c==null?KitSelectionContext.NONE:c;}

    public static boolean isAliveForTDM(EntityPlayer player) {
        return player != null && player.worldObj != null && !player.isDead && player.getHealth() > 0.0F
                && !roundWaitingPlayers.contains(getPlayerKey(player));
    }
    /** Spatial eligibility deliberately ignores combat-phase and round-waiting state. */
    public static boolean canPlaceAtTdmSpawn(EntityPlayer player){return player!=null&&player.worldObj!=null&&!player.isDead&&player.getHealth()>0.0F;}

    public static boolean isFfaEliminated(EntityPlayer player) {
        return player != null
                && isFfaMode(player.worldObj)
                && ffaEliminated.contains(getPlayerKey(player));
    }
    public static void markLateJoinerFfaEliminated(EntityPlayer player) {
        if (player != null && isFfaMode(player.worldObj)) {
            ffaEliminated.add(getPlayerKey(player));
        }
    }

    public static void eliminateFfaPlayer(EntityPlayer victim) {
        if (victim == null
                || !isFfaMode(victim.worldObj)
                || ffaNextRoundTick > 0L
                || !isCompetitivePlayer(victim)) {
            return;
        }
        ffaEliminated.add(getPlayerKey(victim));
        List<EntityPlayerMP> alive = new ArrayList<EntityPlayerMP>();
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (player.worldObj == victim.worldObj
                    && isCompetitivePlayer(player)
                    && !ffaEliminated.contains(getPlayerKey(player))) {
                alive.add(player);
            }
        }
        if (alive.size() <= 1) {
            ffaRoundWinner = alive.isEmpty() ? null : getPlayerKey(alive.get(0));
            awardRoundResultBuyScore(victim.worldObj, null, ffaRoundWinner,
                    activeRoundParticipants);
            ffaNextRoundTick = victim.worldObj.getTotalWorldTime() + 100L;
            sendStatusToAll(victim.worldObj);
        }
    }

    public static boolean isDeathmatchEliminated(EntityPlayer player) {
        return player != null
                && getGameMode(player.worldObj) == TDMGameMode.DEATHMATCH
                && isHardcoreRespawns(player.worldObj)
                && deathmatchEliminated.contains(getPlayerKey(player));
    }

    public static void eliminateDeathmatchPlayer(EntityPlayer player) {
        if (player == null
                || getGameMode(player.worldObj) != TDMGameMode.DEATHMATCH
                || !isHardcoreRespawns(player.worldObj)
                || !isCompetitivePlayer(player)) {
            return;
        }
        deathmatchEliminated.add(getPlayerKey(player));
    }

    public static void markLateJoinerDeathmatchEliminated(EntityPlayer player) {
        if (player != null
                && getGameMode(player.worldObj) == TDMGameMode.DEATHMATCH
                && isHardcoreRespawns(player.worldObj)) {
            deathmatchEliminated.add(getPlayerKey(player));
        }
    }

    /** Resolves elimination using only the state owned by the selected mode. */
    public static boolean isCurrentModeEliminated(EntityPlayer player) {
        if (player == null || player.worldObj == null || !isEnabled(player.worldObj)) {
            return false;
        }

        TDMGameMode mode = getGameMode(player.worldObj);
        if (mode == TDMGameMode.BOMB) {
            return isHardcoreRespawns(player.worldObj)
                    && TDMBombManager.isEliminated(player);
        }
        if (mode == TDMGameMode.FFA) {
            return isFfaEliminated(player);
        }
        if (mode == TDMGameMode.DEATHMATCH) {
            return isDeathmatchEliminated(player);
        }
        return false;
    }

    private static void clearAllModeEliminationState() {
        TDMBombManager.clearEliminatedPlayers();
        ffaEliminated.clear();
        deathmatchEliminated.clear();
    }
    private static void startFfaRound(World world) {
        ffaEliminated.clear();
        ffaRoundWinner = null;
        ffaNextRoundTick = 0L;
        releaseAllRoundWaiting();
        placeAllPlayersAtSelectedMap(world, KitSelectionContext.LOADOUT_SELECTION);
        captureActiveRoundParticipants(world, activeRoundParticipants);
    }

    public static boolean isRoundWaiting(EntityPlayer player) {
        return player != null
                && roundWaitingPlayers.contains(getPlayerKey(player))
                && isCurrentModeEliminated(player);
    }

    public static void putInRoundWaiting(EntityPlayer player) {
        if (player == null
                || !isCurrentModeEliminated(player)
                || (!hasPlayerTeam(player) && !isFfaMode(player.worldObj))) {
            return;
        }
        String key = getPlayerKey(player);
        cancelKitSelection(player);
        releaseGlobalBuyProtection(player);
        clearKitSelectionProtection(player);
        roundWaitingPlayers.add(key);
        roundWaitingAnchors.put(key, new FreezeAnchor(player));
        applyOwnedProtectionEffects(player);
        enforceFreeze(player, roundWaitingAnchors.get(key));
    }
    public static void releaseRoundWaiting(EntityPlayer player) {
        if(player==null)return; String key=getPlayerKey(player); roundWaitingPlayers.remove(key);roundWaitingAnchors.remove(key);
        if(!hasKitSelectionProtection(player))removeOwnedProtectionEffects(player);
    }
    public static void tickRoundWaiting(EntityPlayer player) {
        if (player == null || !roundWaitingPlayers.contains(getPlayerKey(player))) {
            return;
        }
        if (!isEnabled(player.worldObj)
                || (!hasPlayerTeam(player) && !isFfaMode(player.worldObj))
                || !isCurrentModeEliminated(player)) {
            releaseRoundWaiting(player);
            return;
        }
        applyOwnedProtectionEffects(player);enforceFreeze(player,roundWaitingAnchors.get(getPlayerKey(player)));
    }
    public static void clearRoundPlayerState(EntityPlayer player){if(player==null)return;releaseRoundWaiting(player);survivorChoicePending.remove(getPlayerKey(player));selectedKits.remove(getPlayerKey(player));}
    public static void releaseAllRoundWaiting(){for(EntityPlayerMP p:getOnlinePlayers())releaseRoundWaiting(p);roundWaitingPlayers.clear();roundWaitingAnchors.clear();}

    public static boolean hasValidSelectedKit(EntityPlayer player) {
        SelectedKit selected = selectedKits.get(getPlayerKey(player));
        Team team = getPlayerTeam(player.worldObj, player.getCommandSenderName());
        return selected != null && team != null && selected.pool == team
                && TDMKitManager.getKitCost(getSelectedMap(player.worldObj), team, selected.index) >= 0;
    }
    public static void offerSurvivorChoice(EntityPlayer player){if(!(player instanceof EntityPlayerMP)||!hasValidSelectedKit(player))return;survivorChoicePending.add(getPlayerKey(player));PacketDispatcher.wrapper.sendTo(new TDMSurvivorChoiceGuiPacket(),(EntityPlayerMP)player);}
    public static boolean handleSurvivorChoice(EntityPlayer player,boolean keep){String key=getPlayerKey(player);if(!survivorChoicePending.remove(key)||!isGlobalBombBuyPeriod(player)||!hasValidSelectedKit(player))return false;if(!keep){promptForKit(player,KitSelectionContext.BUY_PHASE);return true;}Team team=getPlayerTeam(player.worldObj,player.getCommandSenderName());SelectedKit selected = selectedKits.get(key);boolean applied=TDMKitManager.applyKit(getSelectedMap(player.worldObj),team,selected.index,player);if(applied)closeKitGui(player);return applied;}
    public static void clearSurvivorChoice(EntityPlayer player){if(player!=null)survivorChoicePending.remove(getPlayerKey(player));}
    public static void resolvePendingSurvivorChoice(EntityPlayer player){if(player!=null&&survivorChoicePending.contains(getPlayerKey(player)))handleSurvivorChoice(player,true);}

    /** One authoritative gate shared by the key binding and menu button. */
    public static boolean requestBuyMenu(EntityPlayer player){
        if (player == null || !isGlobalBombBuyPeriod(player) || !hasPlayerTeam(player)
                || isRoundWaiting(player) || !isNearTeamSpawn(player, 4.0D)) {
            return false;
        }
        survivorChoicePending.remove(getPlayerKey(player));promptForKit(player,KitSelectionContext.BUY_PHASE);return true;
    }
    private static boolean isNearTeamSpawn(EntityPlayer player,double radius){TDMMap map=getSelectedMapData(player.worldObj);if(map==null)return false;List<SpawnPoint> source=map.spawns.isEmpty()?TDMData.get(player.worldObj).spawns:map.spawns;Team team=getPlayerTeam(player.worldObj,player.getCommandSenderName());double max=radius*radius;for(SpawnPoint s:source)if(s.team==team&&s.dim==player.dimension&&player.getDistanceSq(s.x+.5D,s.y,s.z+.5D)<=max)return true;return false;}

    /** True for living participants in the selected map dimension throughout hardcore PRE_ROUND. */
    public static boolean isGlobalBombBuyPeriod(EntityPlayer player) {
        if (!isAliveForTDM(player) || !isEnabled(player.worldObj) || isMapVoteActive(player.worldObj)
                || !isBombMode(player.worldObj) || !isHardcoreRespawns(player.worldObj)
                || TDMBombManager.getState() != TDMBombManager.BombRoundState.PRE_ROUND
                || TDMSpectatorManager.isObserving(player)) return false;
        TDMMap map = getSelectedMapData(player.worldObj);
        if (map == null) return false;
        List<SpawnPoint> source = map.spawns.isEmpty() ? TDMData.get(player.worldObj).spawns : map.spawns;
        for (SpawnPoint spawn : source) if (spawn.dim == player.dimension) return true;
        return false;
    }

    /**
     * Must be called after respawnPlayer so the anchor is the team spawn.
     * Buy protection is server-side gameplay protection and must never hide the player.
     */
    public static void enrollGlobalBuyProtection(EntityPlayer player) {
        if (!isGlobalBombBuyPeriod(player)) return;
        String key = getPlayerKey(player);
        respawnLockProtectionActive.remove(key);
        buyProtectionActive.add(key);
        if (!freezeAnchors.containsKey(key)) {
            freezeAnchors.put(key, new FreezeAnchor(player));
            if (XFConfig.tdmBombLifecycleDebug && MainRegistry.logger != null)
                MainRegistry.logger.info("TDM BUY: {} freeze anchor=({}, {}, {})", player.getCommandSenderName(), player.posX, player.posY, player.posZ);
        }
        applyOwnedProtectionEffects(player);
        enforceFreeze(player, freezeAnchors.get(key));
    }

    /** Per-player RESPawn lock protection; global buy protection has separate ownership. */
    public static void applyKitSelectionProtection(EntityPlayer player) {
        if (!isAliveForTDM(player)) return;
        String key = getPlayerKey(player);
        respawnLockProtectionActive.add(key);
        applyOwnedProtectionEffects(player);
        if (!freezeAnchors.containsKey(key)) freezeAnchors.put(key, new FreezeAnchor(player));
    }

    private static void applyOwnedProtectionEffects(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 40, 4, true));
        player.addPotionEffect(new PotionEffect(Potion.resistance.id, 40, 4, true));
        player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 40, 4, true));
    }

    private static void removeOwnedProtectionEffects(EntityPlayer player) {
        // TDM no longer applies invisibility, so an active invisibility effect belongs elsewhere.
        player.removePotionEffect(Potion.moveSlowdown.id);
        player.removePotionEffect(Potion.resistance.id);
        player.removePotionEffect(Potion.regeneration.id);
    }

    public static void clearKitSelectionProtection(EntityPlayer player) {
        if (player == null) return;
        String key = getPlayerKey(player);
        respawnLockProtectionActive.remove(key);
        if (!buyProtectionActive.contains(key)) { freezeAnchors.remove(key); removeOwnedProtectionEffects(player); }
    }

    public static void releaseGlobalBuyProtection(EntityPlayer player) {
        if (player == null) return;
        String key = getPlayerKey(player);
        buyProtectionActive.remove(key);
        if (!respawnLockProtectionActive.contains(key)) { freezeAnchors.remove(key); removeOwnedProtectionEffects(player); }
    }

    public static boolean hasKitSelectionProtection(EntityPlayer player) {
        return player != null && (buyProtectionActive.contains(getPlayerKey(player)) || respawnLockProtectionActive.contains(getPlayerKey(player)));
    }

    public static boolean hasRespawnLockProtection(EntityPlayer player) {
        return player != null && respawnLockProtectionActive.contains(getPlayerKey(player));
    }

    public static void closeKitGui(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) PacketDispatcher.wrapper.sendTo(new TDMKitGuiPacket("", new String[0]), (EntityPlayerMP) player);
    }

    public static void cancelKitSelection(EntityPlayer player) {
        if (player == null) return;
        pendingKitSelection.remove(getPlayerKey(player));
        kitSelectionContexts.remove(getPlayerKey(player));
        pendingKitMaps.remove(getPlayerKey(player));
        clearKitSelectionProtection(player);
        releaseGlobalBuyProtection(player);
        closeKitGui(player);
    }

    /** Clears only transient state owned by TDM; spectator flags and kit potions remain independent. */
    public static void resetTDMTransientPlayerState(EntityPlayer player) {
        cancelKitSelection(player);
        TDMPurchasableManager.clearPending(player);
        releaseRoundWaiting(player);
        survivorChoicePending.remove(getPlayerKey(player));
        TDMSpectatorManager.restore(player);
    }

    public static void resetTDMTransientPlayerState(World world) {
        for (EntityPlayerMP player : getOnlinePlayers()) if (world == null || player.worldObj.provider.dimensionId == world.provider.dimensionId) resetTDMTransientPlayerState(player);
    }

    public static void promptForKit(EntityPlayer player) {
        KitSelectionContext context;
        if (isFfaMode(player.worldObj)) {
            context = KitSelectionContext.LOADOUT_SELECTION;
        } else if (isGlobalBombBuyPeriod(player)) {
            context = KitSelectionContext.BUY_PHASE;
        } else {
            context = KitSelectionContext.RESPAWN_LOCK;
        }
        promptForKit(player, context);
    }

    public static void promptForKit(EntityPlayer player, KitSelectionContext context) {
        if (!(player instanceof EntityPlayerMP) || !isAliveForTDM(player)
                || !isEnabled(player.worldObj) || isMapVoteActive(player.worldObj)
                || TDMSpectatorManager.isObserving(player) || context == KitSelectionContext.NONE) {
            return;
        }
        if (context == KitSelectionContext.BUY_PHASE && !isGlobalBombBuyPeriod(player)) {
            return;
        }
        if (context == KitSelectionContext.RESPAWN_LOCK
                && (!isBombMode(player.worldObj) || isHardcoreRespawns(player.worldObj))) {
            return;
        }
        if (context == KitSelectionContext.LOADOUT_SELECTION && isBombMode(player.worldObj)) {
            return;
        }

        String mapName = getSelectedMap(player.worldObj);
        boolean ffa = isFfaMode(player.worldObj);
        Team team = ffa ? null : getOrAssignPlayerTeam(player);
        int redCount = TDMKitManager.getKitCount(mapName, Team.RED);
        int blueCount = TDMKitManager.getKitCount(mapName, Team.BLUE);
        if ((!ffa && (team == null || TDMKitManager.getKitCount(mapName, team) == 0))
                || (ffa && redCount == 0 && blueCount == 0)) {
            String message = "No usable TDM kits are configured for map " + mapName + ".";
            player.addChatMessage(new ChatComponentText(message + " Ask an admin to add RED or BLUE kits."));
            if (MainRegistry.logger != null) {
                MainRegistry.logger.error(message + " Mandatory kit selection was not started for "
                        + player.getCommandSenderName() + ".");
            }
            cancelKitSelection(player);
            return;
        }

        String playerKey = getPlayerKey(player);
        pendingKitSelection.add(playerKey);
        kitSelectionContexts.put(playerKey, context);
        pendingKitMaps.put(playerKey, mapName);
        if (context == KitSelectionContext.RESPAWN_LOCK
                || context == KitSelectionContext.LOADOUT_SELECTION) {
            applyKitSelectionProtection(player);
        }

        TDMMap map = getSelectedMapData(player.worldObj);
        boolean economy = context != KitSelectionContext.LOADOUT_SELECTION
                && map != null && map.mode == TDMGameMode.BOMB && map.buyScoreEnabled;
        if (economy && !hasAffordableKit(player, mapName, ffa, team)) {
            String message = "No configured TDM kit is affordable for your new life.";
            player.addChatMessage(new ChatComponentText(message));
            if (MainRegistry.logger != null) {
                MainRegistry.logger.error("TDM map {} cannot complete mandatory kit selection for {}: "
                        + "no eligible kit is affordable", mapName,
                        player.getCommandSenderName());
            }
            cancelKitSelection(player);
            return;
        }
        Team primaryPool = ffa ? (redCount > 0 ? Team.RED : Team.BLUE) : team;
        sendKitGui((EntityPlayerMP) player, mapName, primaryPool, ffa, economy,
                context == KitSelectionContext.BUY_PHASE);
    }

    private static boolean hasAffordableKit(EntityPlayer player, String mapName,
            boolean ffa, Team team) {
        int balance = getBuyScore(player);
        Team[] pools = ffa ? new Team[] { Team.RED, Team.BLUE } : new Team[] { team };
        for (Team pool : pools) {
            int[] costs = TDMKitManager.getKitCosts(mapName, pool);
            for (int cost : costs) {
                if (cost <= balance) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void sendKitGui(EntityPlayerMP player, String mapName, Team primaryPool,
            boolean ffa, boolean economy, boolean buying) {
        String[] redNames = ffa ? TDMKitManager.getKitNames(mapName, Team.RED)
                : TDMKitManager.getKitNames(mapName, primaryPool);
        int[] redCosts = ffa ? TDMKitManager.getKitCosts(mapName, Team.RED)
                : TDMKitManager.getKitCosts(mapName, primaryPool);
        ItemStack[][] redPreviews = ffa ? TDMKitManager.getKitPreviews(mapName, Team.RED)
                : TDMKitManager.getKitPreviews(mapName, primaryPool);
        String[] blueNames = ffa ? TDMKitManager.getKitNames(mapName, Team.BLUE) : new String[0];
        int[] blueCosts = ffa ? TDMKitManager.getKitCosts(mapName, Team.BLUE) : new int[0];
        ItemStack[][] bluePreviews = ffa ? TDMKitManager.getKitPreviews(mapName, Team.BLUE)
                : new ItemStack[0][40];
        if (!economy) {
            Arrays.fill(redCosts, 0);
            Arrays.fill(blueCosts, 0);
        }
        PacketDispatcher.wrapper.sendTo(new TDMKitGuiPacket(primaryPool.name, redNames, redCosts,
                redPreviews, blueNames, blueCosts, bluePreviews, ffa, economy,
                getBuyScore(player), buying ? TDMBombManager.getRemainingSeconds(player.worldObj) : 0,
                buying, true), player);
    }

    public static void tickKitSelection(EntityPlayer player) {
        if (player == null || player.worldObj == null) {
            return;
        }
        String key = getPlayerKey(player);
        if (!isEnabled(player.worldObj) || isMapVoteActive(player.worldObj)) {
            cancelKitSelection(player);
            return;
        }
        KitSelectionContext context = getKitSelectionContext(player);
        if (isGlobalBombBuyPeriod(player)) {
            enrollGlobalBuyProtection(player);
            return;
        }
        releaseGlobalBuyProtection(player);
        if (context == KitSelectionContext.BUY_PHASE
                || (context == KitSelectionContext.RESPAWN_LOCK && isHardcoreRespawns(player.worldObj))
                || (context == KitSelectionContext.LOADOUT_SELECTION && isBombMode(player.worldObj))) {
            cancelKitSelection(player);
            return;
        }
        boolean protectedContext = context == KitSelectionContext.RESPAWN_LOCK
                || context == KitSelectionContext.LOADOUT_SELECTION;
        if (protectedContext && pendingKitSelection.contains(key) && isAliveForTDM(player)
                && !TDMSpectatorManager.isObserving(player)) {
            applyKitSelectionProtection(player);
            enforceFreeze(player, freezeAnchors.get(key));
        } else {
            clearKitSelectionProtection(player);
        }
    }

    private static void enforceFreeze(EntityPlayer player, FreezeAnchor anchor) {
        if (anchor == null || !(player instanceof EntityPlayerMP)) {
            return;
        }
        player.motionX = 0;
        player.motionY = 0;
        player.motionZ = 0;
        player.fallDistance = 0;
        player.setSprinting(false);
        if (anchor.dimension == player.dimension
                && (Math.abs(player.posX - anchor.x) > .001
                    || Math.abs(player.posY - anchor.y) > .001
                    || Math.abs(player.posZ - anchor.z) > .001)) {
            ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(anchor.x, anchor.y,
                    anchor.z, player.rotationYaw, player.rotationPitch);
        }
    }

    public static KitSelectionResult selectKit(EntityPlayer player, int kitIndex, Team requestedPool) {
        if (!isAliveForTDM(player) || TDMSpectatorManager.isObserving(player)
                || !isEnabled(player.worldObj) || isMapVoteActive(player.worldObj)) {
            return expireKitSelection(player);
        }
        String playerKey = getPlayerKey(player);
        if (!pendingKitSelection.contains(playerKey)) {
            return KitSelectionResult.ALREADY_SELECTED;
        }
        KitSelectionContext context = getKitSelectionContext(player);
        String pendingMap = pendingKitMaps.get(playerKey);
        if (pendingMap == null || !pendingMap.equals(getSelectedMap(player.worldObj))) {
            return expireKitSelection(player);
        }
        boolean ffa = isFfaMode(player.worldObj);
        if (context == KitSelectionContext.BUY_PHASE && !isGlobalBombBuyPeriod(player)) {
            return expireKitSelection(player);
        }
        if (context == KitSelectionContext.RESPAWN_LOCK && isHardcoreRespawns(player.worldObj)) {
            return expireKitSelection(player);
        }
        if (context == KitSelectionContext.LOADOUT_SELECTION && isBombMode(player.worldObj)) {
            return expireKitSelection(player);
        }

        Team pool;
        if (ffa) {
            if (context != KitSelectionContext.LOADOUT_SELECTION || requestedPool == null) {
                return KitSelectionResult.INVALID_SELECTION;
            }
            pool = requestedPool;
        } else {
            pool = getOrAssignPlayerTeam(player);
            if (pool == null || (requestedPool != null && requestedPool != pool)) {
                return KitSelectionResult.INVALID_SELECTION;
            }
        }
        String mapName = getSelectedMap(player.worldObj);
        int savedCost = TDMKitManager.getKitCost(mapName, pool, kitIndex);
        if (savedCost < 0) {
            return KitSelectionResult.INVALID_SELECTION;
        }
        TDMMap map = getSelectedMapData(player.worldObj);
        int effectiveCost = context != KitSelectionContext.LOADOUT_SELECTION
                && map != null && map.mode == TDMGameMode.BOMB && map.buyScoreEnabled ? savedCost : 0;
        if (getBuyScore(player) < effectiveCost) {
            return KitSelectionResult.INSUFFICIENT_FUNDS;
        }
        if (!TDMKitManager.applyKit(mapName, pool, kitIndex, player)) {
            return KitSelectionResult.INVALID_SELECTION;
        }
        TDMPurchasableManager.applyPendingKillstreakRewards(player);
        // Survivor-kit state belongs exclusively to competitive BOMB purchases.
        if (context != KitSelectionContext.LOADOUT_SELECTION && isBombMode(player.worldObj)) {
            selectedKits.put(playerKey, new SelectedKit(pool, kitIndex));
        }
        if (effectiveCost > 0) {
            TDMData data = TDMData.get(player.worldObj);
            data.playerBuyScores.put(playerKey, Integer.valueOf(getBuyScore(player) - effectiveCost));
            data.markDirty();
        }
        pendingKitSelection.remove(playerKey);
        kitSelectionContexts.remove(playerKey);
        pendingKitMaps.remove(playerKey);
        if (context != KitSelectionContext.BUY_PHASE) {
            clearKitSelectionProtection(player);
        }
        closeKitGui(player);
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            playerMP.inventory.markDirty();
            playerMP.inventoryContainer.detectAndSendChanges();
            sendStatusToAll(player.worldObj);
            PacketDispatcher.wrapper.sendTo(new TDMKitSelectResultPacket(KitSelectionResult.SUCCESS), playerMP);
        }
        if (context == KitSelectionContext.RESPAWN_LOCK && isBombMode(player.worldObj)) {
            TDMBombManager.ensureLiveRoundBombAssigned(player.worldObj);
        }
        return KitSelectionResult.SUCCESS;
    }

    public static KitSelectionResult selectKit(EntityPlayer player, int kitIndex) {
        return selectKit(player, kitIndex, null);
    }

    private static KitSelectionResult expireKitSelection(EntityPlayer player) {
        if (player != null) {
            cancelKitSelection(player);
            if (XFConfig.tdmBombLifecycleDebug && MainRegistry.logger != null) MainRegistry.logger.info("TDM kit selection expired for {}", player.getCommandSenderName());
        }
        return KitSelectionResult.BUY_PHASE_ENDED;
    }

    public static String getPlayerKey(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }

    /** Re-resolves spatial state after a team mutation; no selected spawn or freeze anchor survives the old team. */
    public static void refreshPlayerPlacementAfterTeamChange(EntityPlayer player) {
        if(player==null)return;
        boolean waiting=isRoundWaiting(player);
        cancelKitSelection(player); selectedKits.remove(getPlayerKey(player)); survivorChoicePending.remove(getPlayerKey(player));
        releaseRoundWaiting(player); releaseGlobalBuyProtection(player); clearKitSelectionProtection(player);
        boolean placed=placePlayerAtSelectedMapSpawn(player,new Random());
        if(waiting&&placed)putInRoundWaiting(player);
        else if(placed&&isGlobalBombBuyPeriod(player)){enrollGlobalBuyProtection(player);promptForKit(player,KitSelectionContext.BUY_PHASE);}
        else if(placed&&!isHardcoreRespawns(player.worldObj))promptForKit(player,KitSelectionContext.RESPAWN_LOCK);
        TDMHandler.refreshPendingPlacementAfterTeamChange(player);
    }

    /** Authoritative server-side placement path for login, respawn, and round transitions. */
    public static boolean placePlayerAtSelectedMapSpawn(EntityPlayer player, Random random) {
        if (!canPlaceAtTdmSpawn(player)) {
            return false;
        }

        Team team = isFfaMode(player.worldObj) ? null : getPlayerTeam(player.worldObj, player.getCommandSenderName());
        if (!isFfaMode(player.worldObj) && team == null) return false;
        String mapName = getSelectedMap(player.worldObj);
        SpawnPoint spawn = getRandomSpawn(player.worldObj, team, random);
        if (spawn == null) {
            logInvalidSpawn(player, mapName, team, player.dimension);
            return false;
        }

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            playerMP.mountEntity(null);
            if (playerMP.dimension != spawn.dim) {
                // Forge's existing transfer path establishes the destination world before final placement.
                playerMP.travelToDimension(spawn.dim);
            }
            playerMP.playerNetServerHandler.setPlayerLocation(
                    spawn.x + 0.5D,
                    spawn.y,
                    spawn.z + 0.5D,
                    playerMP.rotationYaw,
                    playerMP.rotationPitch
            );
        } else if (player.dimension == spawn.dim) {
            player.setPositionAndUpdate(spawn.x + 0.5D, spawn.y, spawn.z + 0.5D);
        } else {
            logInvalidSpawn(player, mapName, team, player.dimension);
            return false;
        }

        return true;
    }

    /**
     * Restores the TDM-owned new-life state after authoritative team-spawn placement.
     * This helper is only for round starts and genuine respawns, never live players.
     */
    public static void restorePlayerForRound(EntityPlayer player) {
        if (player == null || !isAliveForTDM(player)) {
            return;
        }

        player.setHealth(player.getMaxHealth());
        NBTTagCompound foodState = new NBTTagCompound();
        // MCP exposes the FoodStats setters in the merged development environment, but they
        // are not common-side methods on Forge 1.7.10 dedicated servers. Use common-side NBT.
        player.getFoodStats().writeNBT(foodState);
        foodState.setInteger("foodLevel", 20);
        foodState.setFloat("foodSaturationLevel", 20.0F);
        foodState.setFloat("foodExhaustionLevel", 0.0F);
        player.getFoodStats().readNBT(foodState);
        player.extinguish();
        player.fallDistance = 0.0F;
    }

    /** Places and fully restores a player for a genuine TDM new-life transition. */
    public static boolean respawnPlayer(EntityPlayer player, Random random) {
        if (!placePlayerAtSelectedMapSpawn(player, random)) {
            return false;
        }

        restorePlayerForRound(player);
        return true;
    }

    private static void logInvalidSpawn(EntityPlayer player, String mapName, Team team, int dimension) {
        String playerName = player == null ? "<round-start>" : player.getCommandSenderName();
        String teamName = team == null ? "<unassigned>" : team.name;
        String selectedMap = mapName == null || mapName.length() == 0 ? "<none>" : mapName;
        String message = "TDM spawn error: player=" + playerName
                + ", selectedMap=" + selectedMap
                + ", team=" + teamName
                + ", dimension=" + dimension
                + ". No usable " + (team==null?"FFA":"team") + " spawn is available; refusing vanilla worldspawn fallback.";
        if (MainRegistry.logger != null) {
            MainRegistry.logger.error(message);
        } else {
            System.err.println(message);
        }
    }

    public static SpawnPoint getRandomSpawn(EntityPlayer player, Random rand) {
        Team team = isFfaMode(player.worldObj)?null:getOrAssignPlayerTeam(player);
        return getRandomSpawn(player.worldObj, team, rand);
    }

    public static SpawnPoint getRandomSpawn(World world, Team team, Random rand) {
        TDMData data = TDMData.get(world);
        List<SpawnPoint> valid = new ArrayList<SpawnPoint>();
        TDMMap selected = data.maps.get(data.selectedMap);

        if (selected != null && !selected.spawns.isEmpty()) {
            addValidSpawns(valid, selected.spawns, team);
        } else {
            // Legacy global spawns remain compatible only for maps with no map-specific data.
            addValidSpawns(valid, data.spawns, team);
        }

        if (valid.isEmpty()) {
            return null;
        }

        return valid.get(rand.nextInt(valid.size()));
    }

    private static void addValidSpawns(List<SpawnPoint> valid, List<SpawnPoint> spawns, Team team) {
        for (SpawnPoint spawn : spawns) {
            if (spawn.team == team
                    && net.minecraftforge.common.DimensionManager.isDimensionRegistered(spawn.dim)) {
                valid.add(spawn);
            }
        }
    }
}
