package com.hfr.tdm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

/** GUI action lease: a stale panel cannot mutate a replacement map, kit, or spawn list. */
public final class AdminEditorSession {
    private static final Map<UUID, Session> ACTIVE = new HashMap<UUID, Session>();
    private AdminEditorSession() { }
    private static final class Session {
        final UUID token = UUID.randomUUID();
        final int dimension;
        final String map, mapRevision, kitRevision, selectedMap;
        final TDMManager.Team team;
        final int kitIndex;
        final Object mapIdentity, kitIdentity;
        final long created = System.currentTimeMillis();
        Session(EntityPlayerMP player, String map, TDMManager.Team team, int kitIndex) {
            this.dimension = player.dimension; this.map = map; this.team = team; this.kitIndex = kitIndex;
            this.selectedMap = TDMManager.getSelectedMap(player.worldObj);
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
                || session.mapIdentity != (map.length() == 0 ? null : TDMManager.getMap(player.worldObj, map))
                || !session.mapRevision.equals(revision((TDMManager.TDMMap)session.mapIdentity))) return false;
        return !kitAction || session.kitIdentity == TDMKitManager.getDirectKitIdentity(map, team, kitIndex)
                && same(session.kitRevision, TDMKitManager.getDirectKitRevision(map, team, kitIndex));
    }
    private static boolean same(String a, String b) { return a == null ? b == null : a.equals(b); }
    private static String revision(TDMManager.TDMMap map) {
        if (map == null) return "";
        StringBuilder s = new StringBuilder();
        s.append(map.mode).append('|').append(map.terroristTeam).append('|').append(map.hardcoreRespawns).append('|')
                .append(map.buyScoreEnabled).append('|').append(map.killstreaksEnabled).append('|')
                .append(map.scoreLimitOverride).append('|').append(map.roundTicksOverride).append('|')
                .append(map.bombScoreLimitOverride).append('|').append(map.bombRoundTicksOverride).append('|')
                .append(map.killScoreReward).append('|').append(map.roundLossBuyScoreReward).append('|')
                .append(map.killBuyScoreReward).append('|').append(map.roundWinBuyScoreReward).append('|')
                .append(map.bombPlantBuyScoreReward).append('|').append(map.bombDefuseBuyScoreReward).append('|');
        area(s, map.bounds); area(s, map.bombsiteA); area(s, map.bombsiteB);
        s.append(map.mapBorderEnabled).append('|');
        for (TDMManager.SpawnPoint p : map.spawns) s.append(p.team).append(':').append(p.dim).append(':').append(p.x).append(':').append(p.y).append(':').append(p.z).append(':').append(p.hasRotation).append(':').append(p.yaw).append(':').append(p.pitch).append(';');
        return s.toString();
    }
    private static void area(StringBuilder s, TDMManager.Bombsite a) {
        s.append(a.hasPos1).append(':').append(a.hasPos2).append(':').append(a.dimension).append(':')
                .append(a.x1).append(':').append(a.y1).append(':').append(a.z1).append(':')
                .append(a.x2).append(':').append(a.y2).append(':').append(a.z2).append('|');
    }
}
