package com.hfr.data;

import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class OutOfBoundsData {

    private static final List<int[]> regions = new ArrayList<int[]>();
    private static File saveFile;
    private static boolean loaded = false;

    public static List<int[]> getRegions(World world) {
        load(world);
        return regions;
    }

    public static void addRegion(World world, int dim, int x1, int z1, int x2, int z2) {
        load(world);

        int xa = Math.min(x1, x2);
        int xb = Math.max(x1, x2);
        int za = Math.min(z1, z2);
        int zb = Math.max(z1, z2);
        regions.add(new int[] { dim, xa, za, xb, zb });
        save(world);
    }

    public static boolean isOutOfBounds(World world, int dim, int x, int z) {
        load(world);

        for (int[] region : regions) {
            if (region[0] == dim && x >= region[1] && z >= region[2] && x <= region[3] && z <= region[4]) {
                return true;
            }
        }

        return false;
    }

    private static void load(World world) {
        File file = getSaveFile(world);
        if (loaded && file != null && file.equals(saveFile)) {
            return;
        }

        regions.clear();
        saveFile = file;
        loaded = true;

        if (saveFile == null || !saveFile.exists()) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(saveFile));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0 || line.startsWith("#")) {
                        continue;
                    }

                    String[] parts = line.split("[,\\s]+");
                    if (parts.length != 5) {
                        continue;
                    }

                    addLoadedRegion(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4])
                    );
                }
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addLoadedRegion(int dim, int x1, int z1, int x2, int z2) {
        int xa = Math.min(x1, x2);
        int xb = Math.max(x1, x2);
        int za = Math.min(z1, z2);
        int zb = Math.max(z1, z2);
        regions.add(new int[] { dim, xa, za, xb, zb });
    }

    private static void save(World world) {
        File file = getSaveFile(world);
        if (file == null) {
            return;
        }

        saveFile = file;
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            try {
                writer.println("# dim,x1,z1,x2,z2 - Y is ignored");
                for (int[] region : regions) {
                    writer.println(region[0] + "," + region[1] + "," + region[2] + "," + region[3] + "," + region[4]);
                }
            } finally {
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File getSaveFile(World world) {
        if (world == null || world.getSaveHandler() == null) {
            return null;
        }
        return new File(world.getSaveHandler().getWorldDirectory(), "out_of_bounds_regions.txt");
    }
}
