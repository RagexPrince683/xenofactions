package com.hfr.clowder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.MinecraftServer;

public class PlayerProtectionData {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final File FILE = new File(
            MinecraftServer.getServer().getFile("clowder_player_protection.json").getAbsolutePath()
    );

    public static class ProtectionEntry {

        public long pvpGraceUntil;
        public long keepInvUntil;

        public ProtectionEntry() {
        }

        public ProtectionEntry(long pvpGraceUntil, long keepInvUntil) {
            this.pvpGraceUntil = pvpGraceUntil;
            this.keepInvUntil = keepInvUntil;
        }
    }

    private static Map<String, ProtectionEntry> DATA = new HashMap<String, ProtectionEntry>();

    public static void load() {

        try {

            if(!FILE.exists()) {
                save();
                return;
            }

            FileReader reader = new FileReader(FILE);

            DATA = GSON.fromJson(
                    reader,
                    new TypeToken<HashMap<String, ProtectionEntry>>(){}.getType()
            );

            reader.close();

            if(DATA == null)
                DATA = new HashMap<String, ProtectionEntry>();

        } catch(Exception e) {
            e.printStackTrace();
            DATA = new HashMap<String, ProtectionEntry>();
        }
    }

    public static void save() {

        try {

            FileWriter writer = new FileWriter(FILE);

            GSON.toJson(DATA, writer);

            writer.flush();
            writer.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ProtectionEntry get(UUID uuid) {
        return DATA.get(uuid.toString());
    }

    public static Map<String, ProtectionEntry> getAll() {
        return DATA;
    }

    public static void set(UUID uuid, ProtectionEntry entry) {

        DATA.put(uuid.toString(), entry);

        save();
    }

}