package com.hfr.clowder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hfr.main.MainRegistry;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class FactionCreationCooldownData {

    public static final long LEADER_COOLDOWN_MS = 7L * 24L * 60L * 60L * 1000L;
    public static final long MEMBER_COOLDOWN_MS = 3L * 24L * 60L * 60L * 1000L;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static Map<String, CooldownEntry> DATA = new HashMap<String, CooldownEntry>();
    private static Map<String, CooldownEntry> FALLBACKS = new HashMap<String, CooldownEntry>();

    public static class CooldownEntry {
        public long expiresAt;
        public String lastKnownName;
        public CooldownEntry() { }
        public CooldownEntry(long expiresAt, String lastKnownName) {
            this.expiresAt = expiresAt;
            this.lastKnownName = lastKnownName == null ? "" : lastKnownName;
        }
    }

    private static class Store {
        Map<String, CooldownEntry> uuids = new HashMap<String, CooldownEntry>();
        Map<String, CooldownEntry> nameFallbacks = new HashMap<String, CooldownEntry>();
    }

    private static File file() {
        return MinecraftServer.getServer().getFile("clowder_faction_creation_cooldowns.json");
    }

    public static void load() {
        try {
            File f = file();
            if(!f.exists()) { save(); return; }
            Reader reader = new InputStreamReader(new FileInputStream(f), UTF_8);
            Store store = GSON.fromJson(reader, new TypeToken<Store>(){}.getType());
            reader.close();
            DATA = store == null || store.uuids == null ? new HashMap<String, CooldownEntry>() : store.uuids;
            FALLBACKS = store == null || store.nameFallbacks == null ? new HashMap<String, CooldownEntry>() : store.nameFallbacks;
            cleanExpired(System.currentTimeMillis());
        } catch(Exception e) {
            log("Failed to load faction creation cooldowns", e);
            DATA = new HashMap<String, CooldownEntry>();
            FALLBACKS = new HashMap<String, CooldownEntry>();
        }
    }

    public static void save() {
        try {
            Store store = new Store();
            store.uuids = DATA;
            store.nameFallbacks = FALLBACKS;
            File f = file();
            File tmp = new File(f.getAbsolutePath() + ".tmp");
            Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), UTF_8);
            GSON.toJson(store, writer);
            writer.flush();
            writer.close();
            if(f.exists() && !f.delete())
                throw new RuntimeException("Could not replace " + f.getAbsolutePath());
            if(!tmp.renameTo(f))
                throw new RuntimeException("Could not move temporary cooldown file into place");
        } catch(Exception e) {
            log("Failed to save faction creation cooldowns", e);
        }
    }

    public static long getCooldownUntil(UUID uuid) {
        cleanExpired(System.currentTimeMillis());
        CooldownEntry entry = uuid == null ? null : DATA.get(uuid.toString());
        return entry == null ? 0L : entry.expiresAt;
    }

    public static void migrateFallback(EntityPlayer player) {
        if(player == null || player.getUniqueID() == null)
            return;
        String key = normalize(player.getDisplayName());
        CooldownEntry fallback = FALLBACKS.remove(key);
        if(fallback != null) {
            putUuid(player.getUniqueID(), player.getDisplayName(), fallback.expiresAt);
            save();
        }
    }

    public static Map<String, String> snapshotFactionMembers(Clowder clowder, World world) {
        Map<String, String> snapshot = new HashMap<String, String>();
        if(clowder == null)
            return null;
        for(FactionMemberRecord member : clowder.memberRecords.values())
            snapshot.put(member.lastKnownName, member.playerUuid.toString());
        return snapshot;
    }

    public static void applyDisbandCooldowns(Map<String, String> snapshot, String leaderName) {
        long now = System.currentTimeMillis();
        for(Map.Entry<String, String> entry : snapshot.entrySet()) {
            String name = entry.getKey();
            long until = now + (normalize(name).equals(normalize(leaderName)) ? LEADER_COOLDOWN_MS : MEMBER_COOLDOWN_MS);
            String uuid = entry.getValue();
            if(uuid == null || uuid.isEmpty()) {
                putFallback(name, until);
            } else {
                try { putUuid(UUID.fromString(uuid), name, until); }
                catch(Exception e) { putFallback(name, until); }
            }
        }
        save();
    }

    private static void putUuid(UUID uuid, String name, long until) {
        String key = uuid.toString();
        CooldownEntry old = DATA.get(key);
        if(old == null || old.expiresAt < until)
            DATA.put(key, new CooldownEntry(until, name));
    }

    private static void putFallback(String name, long until) {
        String key = normalize(name);
        CooldownEntry old = FALLBACKS.get(key);
        if(old == null || old.expiresAt < until)
            FALLBACKS.put(key, new CooldownEntry(until, name));
        log("Using name fallback for faction creation cooldown: " + name, null);
    }

    private static String resolveUuid(String name, World world) {
        if(name == null || name.isEmpty())
            return null;
        EntityPlayer online = world == null ? null : world.getPlayerEntityByName(name);
        if(online != null && online.getUniqueID() != null)
            return online.getUniqueID().toString();
        try {
            Object cache = MinecraftServer.getServer().func_152358_ax();
            Method m = cache.getClass().getMethod("func_152655_a", String.class);
            Object profile = m.invoke(cache, name);
            if(profile instanceof GameProfile && ((GameProfile)profile).getId() != null)
                return ((GameProfile)profile).getId().toString();
        } catch(Exception e) {
            log("Profile lookup failed for faction cooldown member " + name, e);
        }
        return null;
    }

    private static void cleanExpired(long now) {
        clean(DATA, now);
        clean(FALLBACKS, now);
    }

    private static void clean(Map<String, CooldownEntry> map, long now) {
        Map<String, CooldownEntry> keep = new HashMap<String, CooldownEntry>();
        for(Map.Entry<String, CooldownEntry> entry : map.entrySet())
            if(entry.getValue() != null && entry.getValue().expiresAt > now)
                keep.put(entry.getKey(), entry.getValue());
        map.clear();
        map.putAll(keep);
    }

    public static String formatRemaining(long ms) {
        long minutes = Math.max(1L, ms / (60L * 1000L));
        long days = minutes / (24L * 60L);
        minutes %= 24L * 60L;
        long hours = minutes / 60L;
        minutes %= 60L;
        return days + "d " + hours + "h " + minutes + "m";
    }

    private static String normalize(String name) { return name == null ? "" : name.trim().toLowerCase(); }

    private static void log(String msg, Throwable t) {
        if(MainRegistry.logger != null) {
            if(t == null) MainRegistry.logger.warn(msg); else MainRegistry.logger.warn(msg, t);
        }
    }
}
