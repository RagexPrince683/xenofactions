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
    // structure: each player keyed by UUID string -> list of compounds {name, url}
    private NBTTagCompound storage = new NBTTagCompound();

    public CustomImageStorage() { super(IDENT); }
    public CustomImageStorage(String id) { super(id); }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        storage = nbt.getCompoundTag("storage");
        if (storage == null) storage = new NBTTagCompound();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setCompoundTag("storage", storage);
        return nbt;
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
        NBTTagList list = storage.getTagList(key, 10); // compound list
        if (list == null) list = new NBTTagList();
        if (list.tagCount() >= 5) return false;
        NBTTagCompound ent = new NBTTagCompound();
        ent.setString("name", name);
        ent.setString("url", url);
        list.appendTag(ent);
        storage.setTag(key, list);
        setDirty(true);
        return true;
    }

    public boolean deleteImage(UUID uuid, int index) {
        String key = uuid.toString();
        NBTTagList list = storage.getTagList(key, 10);
        if (list == null || index < 0 || index >= list.tagCount()) return false;
        list.removeTag(index);
        storage.setTag(key, list);
        setDirty(true);
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