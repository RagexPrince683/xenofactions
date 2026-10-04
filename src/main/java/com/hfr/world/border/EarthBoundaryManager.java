package com.hfr.world.border;

import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.hfr.saveddata.EarthBoundarySavedData;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/** Central access to the effective state and exemptions of the existing border handler. */
public final class EarthBoundaryManager {
    // Observe actual positions across boundary checks; player packets and mod entities can reset prevPos.
    private static final Map<Entity, Position> positions = new WeakHashMap<Entity, Position>();
    private EarthBoundaryManager() { }
    public static boolean isBoundaryEnabled(World world) {
        if (world == null || world.isRemote) return false;
        EarthBoundarySavedData data = EarthBoundarySavedData.get(world);
        return data.hasRuntimeEnabled() ? data.getRuntimeEnabled() : XFConfig.earthBoundaryEnabled;
    }
    public static boolean isPositionExempt(World world, double x, double z) {
        if (world == null || world.isRemote) return false;
        return finite(x) && finite(z) && EarthBoundarySavedData.get(world)
                .intersectsRegion(world.provider.dimensionId, x, x, z, z);
    }

    /** Decide before any destination calculation, chunk lookup, or relocation. Server thread only. */
    public static boolean isEntityExempt(Entity entity) {
        if (entity == null) return false;
        World world = entity.worldObj;
        if (world == null || world.isRemote || world.provider.dimensionId != 0 || entity.isDead
                || !finite(entity.posX) || !finite(entity.posZ)) {
            positions.remove(entity);
            return false;
        }
        Position previous = positions.get(entity);
        double fromX = previous != null && previous.world == world ? previous.x : entity.posX;
        double fromZ = previous != null && previous.world == world ? previous.z : entity.posZ;
        if (!finite(fromX) || !finite(fromZ)) { fromX = entity.posX; fromZ = entity.posZ; }
        rememberPosition(entity);

        EarthBoundarySavedData data = EarthBoundarySavedData.get(world);
        AxisAlignedBB box = entity.boundingBox;
        double minX = box == null ? 0 : box.minX - entity.posX;
        double maxX = box == null ? 0 : box.maxX - entity.posX;
        double minZ = box == null ? 0 : box.minZ - entity.posZ;
        double maxZ = box == null ? 0 : box.maxZ - entity.posZ;
        if (!finite(minX) || !finite(maxX) || !finite(minZ) || !finite(maxZ)
                || minX > maxX || minZ > maxZ) minX = maxX = minZ = maxZ = 0;
        if (data.intersectsRegion(0, entity.posX + minX, entity.posX + maxX,
                entity.posZ + minZ, entity.posZ + maxZ)) return true;
        if (isInsideBoundary(world, entity.posX, entity.posZ)) return false;

        // An open boundary section continues outward along its normal, but never widens along the edge.
        // This also prevents a fast entity from being wrapped one tick after crossing a thin exemption.
        if (entity.posX < MainRegistry.borderNegX || entity.posX > MainRegistry.borderPosX) {
            double edge = entity.posX < MainRegistry.borderNegX ? MainRegistry.borderNegX : MainRegistry.borderPosX;
            if (!isFaceExempt(data, true, edge, fromX, entity.posX, minX, maxX,
                    fromZ, entity.posZ, minZ, maxZ)) return false;
        }
        if (entity.posZ < MainRegistry.borderNegZ || entity.posZ > MainRegistry.borderPosZ) {
            double edge = entity.posZ < MainRegistry.borderNegZ ? MainRegistry.borderNegZ : MainRegistry.borderPosZ;
            if (!isFaceExempt(data, false, edge, fromZ, entity.posZ, minZ, maxZ,
                    fromX, entity.posX, minX, maxX)) return false;
        }
        return true;
    }

    private static boolean isFaceExempt(EarthBoundarySavedData data, boolean xFace, double edge,
            double fromNormal, double toNormal, double minNormal, double maxNormal,
            double fromTangent, double toTangent, double minTangent, double maxTangent) {
        double low = toTangent + minTangent, high = toTangent + maxTangent;
        double movement = toNormal - fromNormal;
        if (movement != 0) {
            // Clip the swept AABB to the boundary plane, including contact before/after the origin crosses.
            double a = (edge - maxNormal - fromNormal) / movement;
            double b = (edge - minNormal - fromNormal) / movement;
            double enter = Math.max(0, Math.min(a, b)), exit = Math.min(1, Math.max(a, b));
            if (enter <= exit) {
                double first = fromTangent + enter * (toTangent - fromTangent);
                double last = fromTangent + exit * (toTangent - fromTangent);
                low = Math.min(first, last) + minTangent;
                high = Math.max(first, last) + maxTangent;
            }
        }
        return xFace ? data.intersectsRegion(0, edge, edge, low, high)
                : data.intersectsRegion(0, low, high, edge, edge);
    }

    /** Reset movement history after an intentional teleport; do not mistake it for a crossing. */
    public static void rememberPosition(Entity entity) {
        Position position = positions.get(entity);
        if (position == null || position.world != entity.worldObj) {
            positions.put(entity, new Position(entity));
        } else {
            position.x = entity.posX; position.z = entity.posZ;
        }
    }
    public static void forget(Entity entity) { positions.remove(entity); }
    public static void clearWorld(World world) {
        java.util.Iterator<Position> iterator = positions.values().iterator();
        while (iterator.hasNext()) if (iterator.next().world == world) iterator.remove();
    }
    private static boolean finite(double value) { return !Double.isNaN(value) && !Double.isInfinite(value); }
    private static final class Position {
        private final World world;
        private double x, z;
        private Position(Entity entity) { world = entity.worldObj; x = entity.posX; z = entity.posZ; }
    }
    /** The authoritative inclusive rectangle used by the legacy wrap implementation. */
    public static boolean isInsideBoundary(World world, double x, double z) {
        return world != null && x >= MainRegistry.borderNegX && x <= MainRegistry.borderPosX
                && z >= MainRegistry.borderNegZ && z <= MainRegistry.borderPosZ;
    }
}
