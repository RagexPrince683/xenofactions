package com.hfr.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class IgnoreManager {

    private static final Map<UUID, Set<UUID>> ignoreMap = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File saveFile;

    public static void init() {
        File worldDir = MinecraftServer.getServer()
                .getEntityWorld()
                .getSaveHandler()
                .getWorldDirectory();

        saveFile = new File(worldDir, "ignorelist.json");
        load();
    }

    public static void toggleIgnore(UUID player, UUID target) {

        Set<UUID> ignored = ignoreMap.computeIfAbsent(player, k -> new HashSet<>());

        if (ignored.contains(target)) {
            ignored.remove(target);
        } else {
            ignored.add(target);
        }

        save();
    }

    public static boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored != null && ignored.contains(target);
    }

    private static void save() {
        try (Writer writer = new FileWriter(saveFile)) {
            GSON.toJson(ignoreMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        if (!saveFile.exists()) return;

        try (Reader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<Map<UUID, Set<UUID>>>(){}.getType();
            Map<UUID, Set<UUID>> data = GSON.fromJson(reader, type);
            if (data != null) {
                ignoreMap.clear();
                ignoreMap.putAll(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}