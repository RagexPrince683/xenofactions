package com.hfr.stonedrops;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class StoneDropDisplayEntry {
    public final String itemName;
    public final int metadata;
    public final int stackSize;
    public final double chance;
    public final Integer minY;
    public final Integer maxY;
    public final NBTTagCompound nbt;

    public StoneDropDisplayEntry(String itemName, int metadata, int stackSize, double chance, Integer minY, Integer maxY, NBTTagCompound nbt) {
        this.itemName = itemName;
        this.metadata = metadata;
        this.stackSize = stackSize;
        this.chance = chance;
        this.minY = minY;
        this.maxY = maxY;
        this.nbt = nbt == null ? null : (NBTTagCompound) nbt.copy();
    }

    public ItemStack toStack() {
        Item item = (Item) Item.itemRegistry.getObject(itemName);
        if (item == null || stackSize <= 0) return null;
        ItemStack stack = new ItemStack(item, stackSize, metadata);
        if (nbt != null) stack.setTagCompound((NBTTagCompound) nbt.copy());
        return stack;
    }

    public String key() {
        return itemName + "|" + metadata + "|" + stackSize + "|" + chance + "|" + minY + "|" + maxY + "|" + (nbt == null ? "" : nbt.toString());
    }
}
