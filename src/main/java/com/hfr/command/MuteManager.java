package com.hfr.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager {

    private static final Map<UUID, Mute> mutedPlayers = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File saveFile;

    public static void init() {
        File worldDir = MinecraftServer.getServer().getEntityWorld().getSaveHandler().getWorldDirectory();
        saveFile = new File(worldDir, "mutelist.json");
        load();
    }

    public static void mute(UUID uuid, int seconds, String reason) {
        long expiry = (seconds < 0)
                ? Long.MAX_VALUE
                : System.currentTimeMillis() + (seconds * 1000L);

        mutedPlayers.put(uuid, new Mute(uuid, expiry, reason));
        save();
    }

    public static void unmute(UUID uuid) {
        mutedPlayers.remove(uuid);
        save();
    }

    public static boolean isMuted(UUID uuid) {
        Mute mute = mutedPlayers.get(uuid);
        if (mute == null) return false;

        if (mute.isExpired()) {
            mutedPlayers.remove(uuid);
            save();
            return false;
        }

        return true;
    }

    public static Mute getMute(UUID uuid) {
        return mutedPlayers.get(uuid);
    }

    private static void save() {
        try (Writer writer = new FileWriter(saveFile)) {
            GSON.toJson(mutedPlayers, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        if (!saveFile.exists()) return;

        try (Reader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<Map<UUID, Mute>>(){}.getType();
            Map<UUID, Mute> data = GSON.fromJson(reader, type);
            if (data != null) {
                mutedPlayers.clear();
                mutedPlayers.putAll(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
