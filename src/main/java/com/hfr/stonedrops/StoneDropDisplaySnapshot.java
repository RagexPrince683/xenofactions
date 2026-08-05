package com.hfr.stonedrops;

import com.hfr.main.MainRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StoneDropDisplaySnapshot {
    public static final int MAX_ENTRIES = 1024;
    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_NBT_CHARS = 8192;
    private static volatile List<StoneDropDisplayEntry> clientSnapshot = Collections.emptyList();

    private StoneDropDisplaySnapshot() { }

    public static List<StoneDropDisplayEntry> fromRuntime() {
        Map<String, StoneDropDisplayEntry> unique = new LinkedHashMap<String, StoneDropDisplayEntry>();
        int size = Math.min(Math.min(MainRegistry.customDrops.size(), MainRegistry.customDropChances.size()),
                Math.min(MainRegistry.customDropMinYs.size(), MainRegistry.customDropMaxYs.size()));
        for (int i = 0; i < size; i++) add(unique, fromStack(MainRegistry.customDrops.get(i), MainRegistry.customDropChances.get(i), MainRegistry.customDropMinYs.get(i), MainRegistry.customDropMaxYs.get(i)));
        List<StoneDropDisplayEntry> out = new ArrayList<StoneDropDisplayEntry>(unique.values());
        Collections.sort(out, comparator());
        return out;
    }

    public static List<StoneDropDisplayEntry> getClientSnapshot() {
        return new ArrayList<StoneDropDisplayEntry>(clientSnapshot);
    }

    public static void replaceClientSnapshot(List<StoneDropDisplayEntry> entries) {
        clientSnapshot = Collections.unmodifiableList(new ArrayList<StoneDropDisplayEntry>(entries));
    }

    public static void clearClientSnapshot() { clientSnapshot = Collections.emptyList(); }

    public static StoneDropDisplayEntry fromStack(ItemStack stack, Double chance, Integer minY, Integer maxY) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0 || chance == null || Double.isNaN(chance.doubleValue()) || Double.isInfinite(chance.doubleValue())) {
            logInvalid("invalid stone-drop display entry");
            return null;
        }
        String name = Item.itemRegistry.getNameForObject(stack.getItem());
        if (name == null || name.length() == 0 || name.length() > MAX_NAME_LENGTH) { logInvalid("invalid stone-drop registry name: " + name); return null; }
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : null;
        if (tag != null && tag.toString().length() > MAX_NBT_CHARS) { logInvalid("stone-drop NBT too large for NEI display: " + name); return null; }
        return new StoneDropDisplayEntry(name, stack.getItemDamage(), stack.stackSize, chance.doubleValue(), minY, maxY, tag);
    }

    public static boolean isValid(StoneDropDisplayEntry entry) {
        if (entry == null || entry.itemName == null || entry.itemName.length() == 0 || entry.itemName.length() > MAX_NAME_LENGTH) return false;
        if (entry.stackSize <= 0 || entry.stackSize > 64 || Double.isNaN(entry.chance) || Double.isInfinite(entry.chance)) return false;
        if (entry.nbt != null && entry.nbt.toString().length() > MAX_NBT_CHARS) return false;
        return Item.itemRegistry.getObject(entry.itemName) != null;
    }

    private static void add(Map<String, StoneDropDisplayEntry> unique, StoneDropDisplayEntry entry) { if (entry != null && isValid(entry)) unique.put(entry.key(), entry); }
    private static Comparator<StoneDropDisplayEntry> comparator() { return new Comparator<StoneDropDisplayEntry>() { public int compare(StoneDropDisplayEntry a, StoneDropDisplayEntry b) { int c=a.itemName.compareTo(b.itemName); if(c!=0)return c; c=a.metadata-b.metadata; if(c!=0)return c; c=val(a.minY)-val(b.minY); if(c!=0)return c; c=val(a.maxY)-val(b.maxY); if(c!=0)return c; return Double.compare(a.chance,b.chance); }}; }
    private static int val(Integer i) { return i == null ? Integer.MIN_VALUE : i.intValue(); }
    private static void logInvalid(String s) { if (MainRegistry.logger != null) MainRegistry.logger.warn("[XF] " + s); }
}
