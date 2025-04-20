package com.hfr.market;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarketData {
    private static final String FILE_NAME = "marketdata.json";
    private static final Gson GSON = new Gson();
    private static Map<String, Integer> marketItems = new HashMap<String, Integer>();

    public static void load() {
        File file = new File(MinecraftServer.getServer().getFile(FILE_NAME));
        if (!file.exists()) {
            save(); // Create default data
        }
        try (FileReader reader = new FileReader(file)) {
            marketItems = GSON.fromJson(reader, marketItems.getClass());
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        File file = new File(MinecraftServer.getServer().getFile(FILE_NAME));
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(marketItems, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getItemPrice(String item) {
        if (marketItems.containsKey(item)) {
            return marketItems.get(item);
        }
        return -1;
    }

    public static void setItem(String item, int price) {
        marketItems.put(item, price);
        save();
    }

    public static Map<String, Integer> getMarketItems() {
        return marketItems;
    }

    // Implemented loadFromJson
    public static void loadFromJson(String jsonData) {
        try {
            marketItems = GSON.fromJson(jsonData, marketItems.getClass());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }
}