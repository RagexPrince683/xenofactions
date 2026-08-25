package com.hfr.saveddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;

/** Persistent, world-scoped runtime state for the existing Earth boundary. */
public final class EarthBoundarySavedData extends WorldSavedData {
    public static final String DATA_NAME = "xenofactions_earth_boundary";
    private boolean hasRuntimeEnabled;
    private boolean runtimeEnabled;
    private final List<Region> regions = new ArrayList<Region>();

    public EarthBoundarySavedData() { this(DATA_NAME); }
    public EarthBoundarySavedData(String name) { super(name); }

    public static EarthBoundarySavedData get(World world) {
        World root = DimensionManager.getWorld(0);
        if (root == null) root = world;
        MapStorage storage = root.mapStorage;
        EarthBoundarySavedData data = (EarthBoundarySavedData) storage.loadData(EarthBoundarySavedData.class, DATA_NAME);
        if (data == null) {
            data = new EarthBoundarySavedData(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public boolean hasRuntimeEnabled() { return hasRuntimeEnabled; }
    public boolean getRuntimeEnabled() { return runtimeEnabled; }
    public void setRuntimeEnabled(boolean enabled) {
        hasRuntimeEnabled = true;
        runtimeEnabled = enabled;
        markDirty();
    }
    public List<Region> getRegions() { return Collections.unmodifiableList(regions); }
    public Region getRegion(String name) {
        String key = normalizeName(name);
        for (Region region : regions) if (region.name.equals(key)) return region;
        return null;
    }
    public boolean addRegion(String name, int dimension, int x1, int z1, int x2, int z2) {
        String key = normalizeName(name);
        if (!isValidName(key) || getRegion(key) != null) return false;
        regions.add(new Region(key, dimension, Math.min(x1, x2), Math.max(x1, x2), Math.min(z1, z2), Math.max(z1, z2)));
        markDirty();
        return true;
    }
    public boolean removeRegion(String name) {
        Region found = getRegion(name);
        if (found == null) return false;
        regions.remove(found);
        markDirty();
        return true;
    }
    public static String normalizeName(String name) { return name == null ? "" : name.toLowerCase(Locale.ROOT); }
    public static boolean isValidName(String name) { return name != null && name.matches("[A-Za-z0-9_-]+"); }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        hasRuntimeEnabled = nbt.hasKey("runtimeEnabled");
        runtimeEnabled = nbt.getBoolean("runtimeEnabled");
        regions.clear();
        NBTTagList list = nbt.getTagList("regions", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            String name = normalizeName(tag.getString("name"));
            if (!isValidName(name) || getRegion(name) != null || !tag.hasKey("dimension") || !tag.hasKey("minX") || !tag.hasKey("maxX") || !tag.hasKey("minZ") || !tag.hasKey("maxZ")) continue;
            int minX = tag.getInteger("minX"), maxX = tag.getInteger("maxX");
            int minZ = tag.getInteger("minZ"), maxZ = tag.getInteger("maxZ");
            if (minX > maxX || minZ > maxZ) continue;
            regions.add(new Region(name, tag.getInteger("dimension"), minX, maxX, minZ, maxZ));
        }
    }
    @Override public void writeToNBT(NBTTagCompound nbt) {
        if (hasRuntimeEnabled) nbt.setBoolean("runtimeEnabled", runtimeEnabled);
        NBTTagList list = new NBTTagList();
        for (Region region : regions) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", region.name); tag.setInteger("dimension", region.dimension);
            tag.setInteger("minX", region.minX); tag.setInteger("maxX", region.maxX);
            tag.setInteger("minZ", region.minZ); tag.setInteger("maxZ", region.maxZ);
            list.appendTag(tag);
        }
        nbt.setTag("regions", list);
    }

    public static final class Region {
        public final String name; public final int dimension, minX, maxX, minZ, maxZ;
        private Region(String name, int dimension, int minX, int maxX, int minZ, int maxZ) {
            this.name=name; this.dimension=dimension; this.minX=minX; this.maxX=maxX; this.minZ=minZ; this.maxZ=maxZ;
        }
        public boolean contains(int dimension, double x, double z) {
            return this.dimension == dimension && x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }
}
