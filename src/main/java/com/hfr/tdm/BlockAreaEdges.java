package com.hfr.tdm;

/** Horizontal render edges for an area stored as two inclusive block corners. */
public final class BlockAreaEdges {
    public final long minX, minZ, maxXExclusive, maxZExclusive;

    private BlockAreaEdges(int x1, int z1, int x2, int z2) {
        minX = Math.min(x1, x2);
        minZ = Math.min(z1, z2);
        maxXExclusive = (long)Math.max(x1, x2) + 1L;
        maxZExclusive = (long)Math.max(z1, z2) + 1L;
    }

    public static BlockAreaEdges of(int x1, int z1, int x2, int z2) {
        return new BlockAreaEdges(x1, z1, x2, z2);
    }

    public boolean containsPlayer(double x, double z, double halfWidth) {
        return x >= minX + halfWidth && x <= maxXExclusive - halfWidth
                && z >= minZ + halfWidth && z <= maxZExclusive - halfWidth;
    }

    // These checks run once when the shared conversion loads, including on dedicated servers.
    static {
        check(-1, -1, -1, -1, -1, 0, -1, 0);
        check(-17, -16, -1, -1, -17, 0, -16, 0);
        check(-16, -17, -16, -17, -16, -15, -17, -16);
        check(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0,
                Integer.MAX_VALUE, (long)Integer.MAX_VALUE + 1L, 0, 1);
        BlockAreaEdges negative = of(-17, -16, -1, -1);
        if (!negative.containsPlayer(-16.5D, -15.5D, 0.3D)
                || negative.containsPlayer(0D, -15.5D, 0.3D))
            throw new AssertionError("Inclusive map containment changed");
    }

    private static void check(int x1, int z1, int x2, int z2,
            long expectedMinX, long expectedMaxX, long expectedMinZ, long expectedMaxZ) {
        BlockAreaEdges actual = of(x1, z1, x2, z2);
        if (actual.minX != expectedMinX || actual.maxXExclusive != expectedMaxX
                || actual.minZ != expectedMinZ || actual.maxZExclusive != expectedMaxZ)
            throw new AssertionError("Inclusive block area edge conversion changed");
    }
}
