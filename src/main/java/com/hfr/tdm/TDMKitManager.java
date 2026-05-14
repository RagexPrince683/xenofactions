package com.hfr.tdm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TDMKitManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAIN_INVENTORY_SIZE = 36;
    private static final int ARMOR_INVENTORY_SIZE = 4;
    private static final String GLOBAL_MAP = "";
    private static final Map<String, Map<TDMManager.Team, List<KitEntry>>> kits = new LinkedHashMap<String, Map<TDMManager.Team, List<KitEntry>>>();
    private static File saveFile;

    static {
        getOrCreateMapKits(GLOBAL_MAP);
    }

    public static void init() {
        File worldDir = MinecraftServer.getServer().getEntityWorld().getSaveHandler().getWorldDirectory();
        saveFile = new File(worldDir, "tdm_kits.txt");
        load();
    }

    public static int addKit(TDMManager.Team team, EntityPlayer player) {
        return addKit(GLOBAL_MAP, team, player);
    }

    public static int addKit(String mapName, TDMManager.Team team, EntityPlayer player) {
        String normalizedMap = TDMManager.normalizeMapName(mapName);
        List<ItemEntry> items = new ArrayList<ItemEntry>();

        for (int i = 0; i < MAIN_INVENTORY_SIZE; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null) {
                items.add(new ItemEntry(i, stack));
            }
        }

        for (int i = 0; i < ARMOR_INVENTORY_SIZE; i++) {
            ItemStack stack = player.inventory.armorInventory[i];
            if (stack != null) {
                items.add(new ItemEntry(MAIN_INVENTORY_SIZE + i, stack));
            }
        }

        List<KitEntry> teamKits = getOrCreateTeamKits(normalizedMap, team);
        String mapPrefix = normalizedMap.length() > 0 ? normalizedMap + " " : "";
        KitEntry kit = new KitEntry(mapPrefix + team.name.substring(0, 1).toUpperCase() + team.name.substring(1) + " Kit " + (teamKits.size() + 1), items);
        teamKits.add(kit);
        save();
        return teamKits.size();
    }

    public static int getKitCount(TDMManager.Team team) {
        return getKitCount(GLOBAL_MAP, team);
    }

    public static int getKitCount(String mapName, TDMManager.Team team) {
        return getTeamKits(mapName, team).size();
    }

    public static String[] getKitNames(TDMManager.Team team) {
        return getKitNames(GLOBAL_MAP, team);
    }

    public static String[] getKitNames(String mapName, TDMManager.Team team) {
        List<KitEntry> teamKits = getTeamKits(mapName, team);
        String[] names = new String[teamKits.size()];
        for (int i = 0; i < teamKits.size(); i++) {
            names[i] = teamKits.get(i).name;
        }
        return names;
    }

    public static boolean applyKit(TDMManager.Team team, int kitIndex, EntityPlayer player) {
        return applyKit(GLOBAL_MAP, team, kitIndex, player);
    }

    public static boolean applyKit(String mapName, TDMManager.Team team, int kitIndex, EntityPlayer player) {
        List<KitEntry> teamKits = getTeamKits(mapName, team);
        if (kitIndex < 0 || kitIndex >= teamKits.size()) {
            return false;
        }

        player.inventory.clearInventory(null, -1);
        for (int i = 0; i < ARMOR_INVENTORY_SIZE; i++) {
            player.inventory.armorInventory[i] = null;
        }

        KitEntry kit = teamKits.get(kitIndex);
        for (ItemEntry item : kit.items) {
            ItemStack stack = item.toItemStack();
            if (stack == null) {
                continue;
            }

            if (item.slot >= 0 && item.slot < MAIN_INVENTORY_SIZE) {
                player.inventory.mainInventory[item.slot] = stack;
            } else if (item.slot >= MAIN_INVENTORY_SIZE && item.slot < MAIN_INVENTORY_SIZE + ARMOR_INVENTORY_SIZE) {
                player.inventory.armorInventory[item.slot - MAIN_INVENTORY_SIZE] = stack;
            }
        }

        player.inventory.markDirty();
        player.inventoryContainer.detectAndSendChanges();
        return true;
    }

    private static List<KitEntry> getTeamKits(String mapName, TDMManager.Team team) {
        String normalizedMap = TDMManager.normalizeMapName(mapName);
        Map<TDMManager.Team, List<KitEntry>> mapKits = kits.get(normalizedMap);
        if (mapKits != null && mapKits.get(team) != null && !mapKits.get(team).isEmpty()) {
            return mapKits.get(team);
        }

        Map<TDMManager.Team, List<KitEntry>> globalKits = kits.get(GLOBAL_MAP);
        if (globalKits == null || globalKits.get(team) == null) {
            return new ArrayList<KitEntry>();
        }
        return globalKits.get(team);
    }

    private static List<KitEntry> getOrCreateTeamKits(String mapName, TDMManager.Team team) {
        return getOrCreateMapKits(mapName).get(team);
    }

    private static Map<TDMManager.Team, List<KitEntry>> getOrCreateMapKits(String mapName) {
        String normalizedMap = TDMManager.normalizeMapName(mapName);
        Map<TDMManager.Team, List<KitEntry>> mapKits = kits.get(normalizedMap);
        if (mapKits == null) {
            mapKits = new EnumMap<TDMManager.Team, List<KitEntry>>(TDMManager.Team.class);
            for (TDMManager.Team team : TDMManager.Team.values()) {
                mapKits.put(team, new ArrayList<KitEntry>());
            }
            kits.put(normalizedMap, mapKits);
        }
        return mapKits;
    }

    private static void save() {
        if (saveFile == null) return;

        SaveData data = new SaveData();
        Map<TDMManager.Team, List<KitEntry>> globalKits = getOrCreateMapKits(GLOBAL_MAP);
        data.red = globalKits.get(TDMManager.Team.RED);
        data.blue = globalKits.get(TDMManager.Team.BLUE);
        for (Map.Entry<String, Map<TDMManager.Team, List<KitEntry>>> entry : kits.entrySet()) {
            if (entry.getKey().length() == 0) {
                continue;
            }

            TeamKitData mapData = new TeamKitData();
            mapData.red = entry.getValue().get(TDMManager.Team.RED);
            mapData.blue = entry.getValue().get(TDMManager.Team.BLUE);
            data.maps.put(entry.getKey(), mapData);
        }

        try {
            Writer writer = new FileWriter(saveFile);
            try {
                GSON.toJson(data, writer);
            } finally {
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        kits.clear();
        getOrCreateMapKits(GLOBAL_MAP);

        if (saveFile == null || !saveFile.exists()) return;

        try {
            Reader reader = new FileReader(saveFile);
            try {
                Type type = new TypeToken<SaveData>() {}.getType();
                SaveData data = GSON.fromJson(reader, type);
                if (data != null) {
                    Map<TDMManager.Team, List<KitEntry>> globalKits = getOrCreateMapKits(GLOBAL_MAP);
                    if (data.red != null) globalKits.get(TDMManager.Team.RED).addAll(data.red);
                    if (data.blue != null) globalKits.get(TDMManager.Team.BLUE).addAll(data.blue);
                    if (data.maps != null) {
                        for (Map.Entry<String, TeamKitData> entry : data.maps.entrySet()) {
                            String mapName = TDMManager.normalizeMapName(entry.getKey());
                            if (mapName.length() == 0 || entry.getValue() == null) {
                                continue;
                            }

                            Map<TDMManager.Team, List<KitEntry>> mapKits = getOrCreateMapKits(mapName);
                            if (entry.getValue().red != null) mapKits.get(TDMManager.Team.RED).addAll(entry.getValue().red);
                            if (entry.getValue().blue != null) mapKits.get(TDMManager.Team.BLUE).addAll(entry.getValue().blue);
                        }
                    }
                }
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SaveData extends TeamKitData {
        Map<String, TeamKitData> maps = new LinkedHashMap<String, TeamKitData>();
    }

    private static class TeamKitData {
        List<KitEntry> red = new ArrayList<KitEntry>();
        List<KitEntry> blue = new ArrayList<KitEntry>();
    }

    private static class KitEntry {
        String name;
        List<ItemEntry> items = new ArrayList<ItemEntry>();

        KitEntry() { }

        KitEntry(String name, List<ItemEntry> items) {
            this.name = name;
            this.items = items;
        }
    }

    private static class ItemEntry {
        int slot;
        String itemName;
        int count;
        int metadata;
        String nbtData;

        ItemEntry() { }

        ItemEntry(int slot, ItemStack stack) {
            this.slot = slot;
            this.itemName = Item.itemRegistry.getNameForObject(stack.getItem());
            this.count = stack.stackSize;
            this.metadata = stack.getItemDamage();
            this.nbtData = stack.hasTagCompound() ? stack.getTagCompound().toString() : null;
        }

        ItemStack toItemStack() {
            Item item = (Item) Item.itemRegistry.getObject(itemName);
            if (item == null) return null;

            ItemStack stack = new ItemStack(item, count, metadata);
            if (nbtData != null) {
                try {
                    stack.setTagCompound((NBTTagCompound) JsonToNBT.func_150315_a(nbtData));
                } catch (Exception e) {
                    System.err.println("Failed to parse TDM kit NBT for item: " + itemName);
                }
            }
            return stack;
        }
    }
}
