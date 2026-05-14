package com.hfr.tdm;

import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TDMManager {

    public static boolean tdmEnabled = false;

    // simple list of positions
    public static final List<SpawnPoint> spawns = new ArrayList<>();

    public static void init() {
        tdmEnabled = false;
        spawns.clear();
    }

    public static class SpawnPoint {
        public final int dim;
        public final int x, y, z;

        public SpawnPoint(int dim, int x, int y, int z) {
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static SpawnPoint getRandomSpawn(World world, Random rand) {
        if (spawns.isEmpty()) return null;

        // filter by dimension (optional but recommended)
        List<SpawnPoint> valid = new ArrayList<>();
        for (SpawnPoint s : spawns) {
            if (s.dim == world.provider.dimensionId) valid.add(s);
        }

        if (valid.isEmpty()) return null;

        return valid.get(rand.nextInt(valid.size()));
    }
}
