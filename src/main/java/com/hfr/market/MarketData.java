package com.hfr.market;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MarketData {
    private static final String FILE_NAME = "marketdata.json";
    private static final Gson GSON = new Gson();
    private static Map<String, Integer> marketItems = new HashMap<String, Integer>();

    // Load market data from file
    public static void load() {
        try {
            File file = new File(MinecraftServer.getServer().getFile(FILE_NAME));
            if (!file.exists()) {
                save(); // Create default file if missing
            }
            FileReader reader = new FileReader(file);
            Type type = new TypeToken<Map<String, Integer>>() {}.getType();
            marketItems = GSON.fromJson(reader, type);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save market data to file
    public static void save() {
        try {
            File file = new File(MinecraftServer.getServer().getFile(FILE_NAME));
            FileWriter writer = new FileWriter(file);
            GSON.toJson(marketItems, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get item price (manual fallback for Java 6 compatibility)
    public static int getItemPrice(String item) {
        if (marketItems.containsKey(item)) {
            return marketItems.get(item);
        }
        return -1; // Default if item not found
    }

    // Add or update item
    public static void setItem(String item, int price) {
        marketItems.put(item, price);
        save();
    }

    // Get all market data
    public static Map<String, Integer> getMarketItems() {
        return marketItems;
    }
}