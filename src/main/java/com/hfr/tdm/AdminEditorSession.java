package com.hfr.tdm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/** Per-player editor context and GUI action lease; stale panels cannot mutate replaced targets. */
public final class AdminEditorSession {
    private static final Map<UUID, Session> ACTIVE = new HashMap<UUID, Session>();
    private static final String CONTEXT = "xfAdminEditorContext";
    private AdminEditorSession() { }
    public static final class Context {
        public String map, kitName;
        public TDMManager.Team team;
        public int kitIndex, page, mode, spawnIndex, spawnType, rewardIndex;
        private Context() { }
    }
    private static NBTTagCompound saved(EntityPlayer player) {
        return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag(CONTEXT);
    }
    private static void save(EntityPlayer player, Context context) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("map", context.map); tag.setString("team", context.team.name);
        tag.setInteger("kitIndex", context.kitIndex); tag.setString("kitName", context.kitName);
        tag.setInteger("page", context.page); tag.setInteger("mode", context.mode);
        tag.setInteger("spawnIndex", context.spawnIndex); tag.setInteger("spawnType", context.spawnType);
        tag.setInteger("rewardIndex", context.rewardIndex);
        NBTTagCompound persisted = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persisted.setTag(CONTEXT, tag);
        player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
    }
    public static void copyContext(EntityPlayer source, EntityPlayer target) {
        NBTTagCompound context = saved(source);
        if (!context.hasKey("team")) return;
        NBTTagCompound persisted = target.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persisted.setTag(CONTEXT, context.copy());
        target.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
    }
    private static int clamp(int value, int max) { return Math.max(0, Math.min(value, max)); }
    public static Context openContext(EntityPlayerMP player, String requestedMap, TDMManager.Team requestedTeam, int requestedKit) {
        NBTTagCompound tag = saved(player);
        boolean previous = tag.hasKey("team");
        Context context = new Context();
        context.map = previous ? tag.getString("map") : TDMManager.getSelectedMap(player.worldObj);
        context.team = previous ? TDMManager.Team.fromName(tag.getString("team")) : TDMManager.Team.RED;
        context.kitIndex = previous ? tag.getInteger("kitIndex") : 0;
        context.kitName = previous ? tag.getString("kitName") : "";
        context.page = previous ? clamp(tag.getInteger("page"), 4) : 0;
        context.mode = previous ? clamp(tag.getInteger("mode"), 2) : -1;
        context.spawnIndex = previous ? Math.max(0, tag.getInteger("spawnIndex")) : 0;
        context.spawnType = previous ? clamp(tag.getInteger("spawnType"), 2) : 0;
        context.rewardIndex = previous ? clamp(tag.getInteger("rewardIndex"), 5) : 0;
        if (requestedMap != null) {
            context.map = "@global".equalsIgnoreCase(requestedMap) ? "" : TDMManager.normalizeMapName(requestedMap);
            context.team = requestedTeam;
            context.kitIndex = requestedKit;
            context.kitName = "";
        }
        if (context.team == null) context.team = TDMManager.Team.RED;
        if (context.map.length() > 0 && !TDMManager.hasMap(player.worldObj, context.map)) {
            String previousMap = tag.getString("map");
            if (previousMap.length() > 0 && TDMManager.hasMap(player.worldObj, previousMap)) {
                context.map = previousMap;
                context.team = TDMManager.Team.fromName(tag.getString("team"));
                if (context.team == null) context.team = TDMManager.Team.RED;
                context.kitIndex = tag.getInteger("kitIndex"); context.kitName = tag.getString("kitName");
            } else context.map = TDMManager.getSelectedMap(player.worldObj);
            if (context.map.length() > 0 && !TDMManager.hasMap(player.worldObj, context.map)) context.map = "";
            if (context.map.length() == 0) {
                java.util.List<String> names = TDMManager.getMapNames(player.worldObj);
                if (!names.isEmpty()) context.map = names.get(0);
            }
            if (!context.map.equals(previousMap)) { context.kitIndex = 0; context.kitName = ""; context.spawnIndex = 0; }
        }
        String[] names = TDMKitManager.getDirectKitNames(context.map, context.team);
        if (context.kitName.length() > 0) {
            int found = context.kitIndex >= 0 && context.kitIndex < names.length
                    && context.kitName.equals(names[context.kitIndex]) ? context.kitIndex : -1;
            if (found < 0) for (int i = 0; i < names.length; i++)
                if (context.kitName.equals(names[i])) { found = i; break; }
            context.kitIndex = found < 0 ? 0 : found;
        }
        context.kitIndex = clamp(context.kitIndex, Math.max(0, names.length - 1));
        context.kitName = names.length == 0 ? "" : names[context.kitIndex];
        TDMManager.TDMMap map = TDMManager.getMap(player.worldObj, context.map);
        if (context.mode < 0) context.mode = TDMManager.getGameMode(player.worldObj).ordinal();
        if (map == null) context.spawnIndex = 0;
        else context.spawnIndex = clamp(context.spawnIndex, Math.max(0, map.spawns(TDMManager.TDMGameMode.values()[context.mode]).size() - 1));
        if (context.mode == 2) context.spawnType = 2;
        else if (context.spawnType == 2) context.spawnType = 0;
        save(player, context);
        return context;
    }
    public static boolean updateContext(EntityPlayerMP player, String token, int page, int mode, int spawnIndex, int spawnType, int rewardIndex) {
        Session session = ACTIVE.get(player.getUniqueID());
        if (session == null || !session.token.toString().equals(token) || player.dimension != session.dimension
                || System.currentTimeMillis() - session.created > 300000L || !player.canCommandSenderUseCommand(4, "tdm")
                || page < 0 || page > 4 || mode < 0 || mode > 2 || spawnIndex < 0 || spawnType < 0 || spawnType > 2
                || rewardIndex < 0 || rewardIndex > 5) return false;
        NBTTagCompound tag = saved(player);
        if (!session.map.equals(tag.getString("map")) || !session.team.name.equals(tag.getString("team"))) return false;
        tag.setInteger("page", page); tag.setInteger("mode", mode); tag.setInteger("spawnIndex", spawnIndex);
        tag.setInteger("spawnType", spawnType); tag.setInteger("rewardIndex", rewardIndex);
        NBTTagCompound persisted = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persisted.setTag(CONTEXT, tag); player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        return true;
    }
    private static final class Session {
        final UUID token = UUID.randomUUID();
        final int dimension;
        final String map, mapRevision, kitRevision, selectedMap;
        final TDMManager.TDMGameMode selectedMode;
        final TDMManager.Team team;
        final int kitIndex;
        final Object mapIdentity, kitIdentity;
        final long created = System.currentTimeMillis();
        Session(EntityPlayerMP player, String map, TDMManager.Team team, int kitIndex) {
            this.dimension = player.dimension; this.map = map; this.team = team; this.kitIndex = kitIndex;
            this.selectedMap = TDMManager.getSelectedMap(player.worldObj);
            this.selectedMode = TDMManager.getGameMode(player.worldObj);
            this.mapIdentity = map.length() == 0 ? null : TDMManager.getMap(player.worldObj, map);
            this.mapRevision = revision((TDMManager.TDMMap)mapIdentity);
            this.kitIdentity = TDMKitManager.getDirectKitIdentity(map, team, kitIndex);
            this.kitRevision = TDMKitManager.getDirectKitRevision(map, team, kitIndex);
        }
    }
    public static String open(EntityPlayerMP player, String map, TDMManager.Team team, int kitIndex) {
        Session session = new Session(player, map, team, kitIndex);
        ACTIVE.put(player.getUniqueID(), session);
        return session.token.toString();
    }
    public static void clear(EntityPlayerMP player) { if (player != null) ACTIVE.remove(player.getUniqueID()); }
    public static boolean has(EntityPlayerMP player) { return player != null && ACTIVE.containsKey(player.getUniqueID()); }
    public static void clearAll() { ACTIVE.clear(); }
    public static boolean consume(EntityPlayerMP player, String token, String map, TDMManager.Team team, int kitIndex, boolean kitAction) {
        Session session = ACTIVE.remove(player.getUniqueID());
        if (session == null || !session.token.toString().equals(token) || System.currentTimeMillis() - session.created > 300000L
                || player.dimension != session.dimension || !session.map.equals(map) || session.team != team || session.kitIndex != kitIndex
                || !session.selectedMap.equals(TDMManager.getSelectedMap(player.worldObj))
                || session.selectedMode != TDMManager.getGameMode(player.worldObj)
                || session.mapIdentity != (map.length() == 0 ? null : TDMManager.getMap(player.worldObj, map))
                || !session.mapRevision.equals(revision((TDMManager.TDMMap)session.mapIdentity))) return false;
        return !kitAction || session.kitIdentity == TDMKitManager.getDirectKitIdentity(map, team, kitIndex)
                && same(session.kitRevision, TDMKitManager.getDirectKitRevision(map, team, kitIndex));
    }
    private static boolean same(String a, String b) { return a == null ? b == null : a.equals(b); }
    private static String revision(TDMManager.TDMMap map) {
        if (map == null) return "";
        StringBuilder s = new StringBuilder();
        s.append(map.mode).append('|').append(map.votingEnabled).append('|').append(map.terroristTeam).append('|').append(map.hardcoreRespawns).append('|')
                .append(map.buyScoreEnabled).append('|').append(map.killstreaksEnabled).append('|')
                .append(map.scoreLimitOverride).append('|').append(map.roundTicksOverride).append('|')
                .append(map.bombScoreLimitOverride).append('|').append(map.bombRoundTicksOverride).append('|')
                .append(map.killScoreReward).append('|').append(map.roundLossBuyScoreReward).append('|')
                .append(map.killBuyScoreReward).append('|').append(map.roundWinBuyScoreReward).append('|')
                .append(map.bombPlantBuyScoreReward).append('|').append(map.bombDefuseBuyScoreReward).append('|');
        area(s, map.bounds); area(s, map.bombsiteA); area(s, map.bombsiteB);
        s.append(map.mapBorderEnabled).append('|');
        for (TDMManager.TDMGameMode mode : TDMManager.TDMGameMode.values()) {
            s.append(mode).append(':').append(map.supportedModes.contains(mode)).append(':').append(map.spawnFallbacks.get(mode)).append('|');
            for (TDMManager.SpawnPoint p : map.spawns(mode)) s.append(p.team).append(':').append(p.dim).append(':').append(p.x).append(':').append(p.y).append(':').append(p.z).append(':').append(p.hasRotation).append(':').append(p.yaw).append(':').append(p.pitch).append(';');
        }
        return s.toString();
    }
    private static void area(StringBuilder s, TDMManager.Bombsite a) {
        s.append(a.hasPos1).append(':').append(a.hasPos2).append(':').append(a.dimension).append(':')
                .append(a.x1).append(':').append(a.y1).append(':').append(a.z1).append(':')
                .append(a.x2).append(':').append(a.y2).append(':').append(a.z2).append('|');
    }
}
