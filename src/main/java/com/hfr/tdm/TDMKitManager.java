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
import java.util.List;
import java.util.Map;

public class TDMKitManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAIN_INVENTORY_SIZE = 36;
    private static final int ARMOR_INVENTORY_SIZE = 4;
    private static final Map<TDMManager.Team, List<KitEntry>> kits = new EnumMap<TDMManager.Team, List<KitEntry>>(TDMManager.Team.class);
    private static File saveFile;

    static {
        for (TDMManager.Team team : TDMManager.Team.values()) {
            kits.put(team, new ArrayList<KitEntry>());
        }
    }

    public static void init() {
        File worldDir = MinecraftServer.getServer().getEntityWorld().getSaveHandler().getWorldDirectory();
        saveFile = new File(worldDir, "tdm_kits.txt");
        load();
    }

    public static int addKit(TDMManager.Team team, EntityPlayer player) {
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

        List<KitEntry> teamKits = kits.get(team);
        KitEntry kit = new KitEntry(team.name.substring(0, 1).toUpperCase() + team.name.substring(1) + " Kit " + (teamKits.size() + 1), items);
        teamKits.add(kit);
        save();
        return teamKits.size();
    }

    public static int getKitCount(TDMManager.Team team) {
        return kits.get(team).size();
    }

    public static String[] getKitNames(TDMManager.Team team) {
        List<KitEntry> teamKits = kits.get(team);
        String[] names = new String[teamKits.size()];
        for (int i = 0; i < teamKits.size(); i++) {
            names[i] = teamKits.get(i).name;
        }
        return names;
    }

    public static boolean applyKit(TDMManager.Team team, int kitIndex, EntityPlayer player) {
        List<KitEntry> teamKits = kits.get(team);
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

    private static void save() {
        if (saveFile == null) return;

        SaveData data = new SaveData();
        data.red = kits.get(TDMManager.Team.RED);
        data.blue = kits.get(TDMManager.Team.BLUE);

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
        for (TDMManager.Team team : TDMManager.Team.values()) {
            kits.get(team).clear();
        }

        if (saveFile == null || !saveFile.exists()) return;

        try {
            Reader reader = new FileReader(saveFile);
            try {
                Type type = new TypeToken<SaveData>() {}.getType();
                SaveData data = GSON.fromJson(reader, type);
                if (data != null) {
                    if (data.red != null) kits.get(TDMManager.Team.RED).addAll(data.red);
                    if (data.blue != null) kits.get(TDMManager.Team.BLUE).addAll(data.blue);
                }
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SaveData {
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
