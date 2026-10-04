package com.hfr.saveddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private Map<Integer, RegionIndex> regionIndex;

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
        regionIndex = null;
        markDirty();
        return true;
    }
    /** Adds a region without exposing its persistence key to administrators. */
    public Region addRegion(int dimension, int x1, int z1, int x2, int z2) {
        int suffix = 1;
        String key;
        do { key = "exemption_" + suffix++; } while (getRegion(key) != null);
        Region region = new Region(key, dimension, Math.min(x1, x2), Math.max(x1, x2), Math.min(z1, z2), Math.max(z1, z2));
        regions.add(region);
        regionIndex = null;
        markDirty();
        return region;
    }
    /** Removes all exemption rectangles while leaving the runtime enable override untouched. */
    public int clearRegions() {
        int removed = regions.size();
        if (removed > 0) {
            regions.clear();
            regionIndex = null;
            markDirty();
        }
        return removed;
    }
    public boolean removeRegion(String name) {
        Region found = getRegion(name);
        if (found == null) return false;
        regions.remove(found);
        regionIndex = null;
        markDirty();
        return true;
    }
    public static String normalizeName(String name) { return name == null ? "" : name.toLowerCase(Locale.ROOT); }
    public static boolean isValidName(String name) { return name != null && name.matches("[A-Za-z0-9_-]+"); }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        hasRuntimeEnabled = nbt.hasKey("runtimeEnabled");
        runtimeEnabled = nbt.getBoolean("runtimeEnabled");
        regions.clear();
        regionIndex = null;
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
            return this.dimension == dimension && x >= minX && x < (double) maxX + 1
                    && z >= minZ && z < (double) maxZ + 1;
        }
    }

    /** Point/footprint lookup without scanning every saved region on every entity tick. */
    public boolean intersectsRegion(int dimension, double minX, double maxX, double minZ, double maxZ) {
        if (regionIndex == null) {
            regionIndex = new HashMap<Integer, RegionIndex>();
            Map<Integer, List<Region>> byDimension = new HashMap<Integer, List<Region>>();
            for (Region region : regions) {
                List<Region> entries = byDimension.get(region.dimension);
                if (entries == null) {
                    entries = new ArrayList<Region>();
                    byDimension.put(region.dimension, entries);
                }
                entries.add(region);
            }
            for (Map.Entry<Integer, List<Region>> entry : byDimension.entrySet())
                regionIndex.put(entry.getKey(), new RegionIndex(entry.getValue(), 0));
        }
        RegionIndex index = regionIndex.get(dimension);
        return index != null && index.intersects(minX, maxX, minZ, maxZ);
    }

    /** Balanced spatial tree, rebuilt only when saved rectangles change. Bounds are half open. */
    private static final class RegionIndex {
        private final double minX, maxX, minZ, maxZ;
        private final RegionIndex left, right;

        private RegionIndex(List<Region> entries, final int depth) {
            double x1 = Double.POSITIVE_INFINITY, x2 = Double.NEGATIVE_INFINITY;
            double z1 = Double.POSITIVE_INFINITY, z2 = Double.NEGATIVE_INFINITY;
            for (Region region : entries) {
                x1 = Math.min(x1, region.minX); x2 = Math.max(x2, (double) region.maxX + 1);
                z1 = Math.min(z1, region.minZ); z2 = Math.max(z2, (double) region.maxZ + 1);
            }
            minX = x1; maxX = x2; minZ = z1; maxZ = z2;
            if (entries.size() == 1) {
                left = right = null;
            } else {
                Collections.sort(entries, new Comparator<Region>() {
                    @Override public int compare(Region a, Region b) {
                        return Double.compare(depth % 2 == 0 ? (double) a.minX + a.maxX : (double) a.minZ + a.maxZ,
                                depth % 2 == 0 ? (double) b.minX + b.maxX : (double) b.minZ + b.maxZ);
                    }
                });
                int mid = entries.size() / 2;
                left = new RegionIndex(entries.subList(0, mid), depth + 1);
                right = new RegionIndex(entries.subList(mid, entries.size()), depth + 1);
            }
        }

        private boolean intersects(double x1, double x2, double z1, double z2) {
            if (x1 >= maxX || z1 >= maxZ || (x1 == x2 ? x2 < minX : x2 <= minX)
                    || (z1 == z2 ? z2 < minZ : z2 <= minZ)) return false;
            return left == null || left.intersects(x1, x2, z1, z2) || right.intersects(x1, x2, z1, z2);
        }
    }
}
