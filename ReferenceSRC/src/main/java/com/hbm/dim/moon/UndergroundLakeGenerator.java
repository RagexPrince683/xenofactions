package com.hbm.dim.moon;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class UndergroundLakeGenerator {
	public boolean generate(World world, Random rand, int x, int z) {
		int centerX = x + rand.nextInt(16);
		int centerZ = z + rand.nextInt(16);
		int centerY = 20 + rand.nextInt(20); // Depth between 20 and 40 blocks below surface

		int radius = 4 + rand.nextInt(4); // Random radius between 4 and 8 blocks
		int depth = 2 + rand.nextInt(3);  // Random depth between 2 and 5 blocks

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				for (int dy = -depth; dy <= 0; dy++) {
					double distance = Math.sqrt(dx * dx + dz * dz + dy * dy * 0.5); // Elliptical shape
					if (distance <= radius - rand.nextDouble()) {
						int blockX = centerX + dx;
						int blockY = centerY + dy;
						int blockZ = centerZ + dz;

						// Replace Moon rock with now ice/packed ice was water
						if (world.getBlock(blockX, blockY, blockZ) == ModBlocks.moon_rock) {
							if (rand.nextBoolean()) {
								world.setBlock(blockX, blockY, blockZ, Blocks.ice, 0, 2);
							} else {
								world.setBlock(blockX, blockY, blockZ, Blocks.packed_ice, 0, 2);
							}
						}
					}
				}
			}
		}
		return true;
	}
}
