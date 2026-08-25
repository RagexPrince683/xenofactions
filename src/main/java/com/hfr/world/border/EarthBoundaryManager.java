package com.hfr.world.border;

import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.hfr.saveddata.EarthBoundarySavedData;
import com.hfr.saveddata.EarthBoundarySavedData.Region;
import net.minecraft.world.World;

/** Central access to the effective state and exemptions of the existing border handler. */
public final class EarthBoundaryManager {
    private EarthBoundaryManager() { }
    public static boolean isBoundaryEnabled(World world) {
        if (world == null || world.isRemote) return false;
        EarthBoundarySavedData data = EarthBoundarySavedData.get(world);
        return data.hasRuntimeEnabled() ? data.getRuntimeEnabled() : XFConfig.earthBoundaryEnabled;
    }
    public static boolean isPositionExempt(World world, double x, double z) {
        if (world == null || world.isRemote) return false;
        int dimension = world.provider.dimensionId;
        for (Region region : EarthBoundarySavedData.get(world).getRegions())
            if (region.contains(dimension, x, z)) return true;
        return false;
    }
    /** The authoritative inclusive rectangle used by the legacy wrap implementation. */
    public static boolean isInsideBoundary(World world, double x, double z) {
        return world != null && x >= MainRegistry.borderNegX && x <= MainRegistry.borderPosX
                && z >= MainRegistry.borderNegZ && z <= MainRegistry.borderPosZ;
    }
}
