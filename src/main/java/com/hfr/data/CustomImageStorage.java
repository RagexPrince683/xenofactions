package com.hfr.data;


import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomImageStorage extends WorldSavedData {
    private static final String IDENT = "yourmod_custom_images";
    private NBTTagCompound storage = new NBTTagCompound();

    public CustomImageStorage() { super(IDENT); }
    public CustomImageStorage(String id) { super(id); }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            storage = new NBTTagCompound();
            return;
        }
        if (nbt.hasKey("storage")) {
            storage = nbt.getCompoundTag("storage");
            if (storage == null) storage = new NBTTagCompound();
        } else {
            storage = new NBTTagCompound();
        }
    }

    // NOTE: void return type required by 1.7.10 WorldSavedData
    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        // use setTag (takes an NBTBase) in 1.7.10
        nbt.setTag("storage", storage);
    }

    public static CustomImageStorage get(World world) {
        WorldSavedData d = world.perWorldStorage.loadData(CustomImageStorage.class, IDENT);
        if (d == null) {
            d = new CustomImageStorage();
            world.perWorldStorage.setData(IDENT, d);
        }
        return (CustomImageStorage) d;
    }

    // add an image for uuid, return true if added, false if limit reached
    public boolean addImage(UUID uuid, String name, String url) {
        String key = uuid.toString();
        NBTTagList list = storage.getTagList(key, 10); // 10 = compound
        if (list == null) list = new NBTTagList();
        if (list.tagCount() >= 5) return false;
        NBTTagCompound ent = new NBTTagCompound();
        ent.setString("name", name);
        ent.setString("url", url);
        list.appendTag(ent);
        storage.setTag(key, list);
        markDirty(); // ensure WorldSavedData is marked dirty so it saves
        return true;
    }

    public boolean deleteImage(UUID uuid, int index) {
        String key = uuid.toString();
        NBTTagList list = storage.getTagList(key, 10);
        if (list == null || index < 0 || index >= list.tagCount()) return false;
        list.removeTag(index);
        storage.setTag(key, list);
        markDirty();
        return true;
    }

    public List<NBTTagCompound> getList(UUID uuid) {
        List<NBTTagCompound> out = new ArrayList<NBTTagCompound>();
        String key = uuid.toString();
        NBTTagList list = storage.getTagList(key, 10);
        if (list == null) return out;
        for (int i = 0; i < list.tagCount(); i++) {
            out.add(list.getCompoundTagAt(i));
        }
        return out;
    }
}