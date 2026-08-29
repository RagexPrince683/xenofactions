package com.hbm.dim.jupiter.GenLayerJupiter;

import com.hbm.dim.jupiter.biome.BiomeGenBaseJupiter;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerJupiterBiomes extends GenLayer {

	public GenLayerJupiterBiomes(long seed) {
		super(seed);
	}

	@Override
	public int[] getInts(int x, int z, int width, int depth) {

		int[] dest = IntCache.getIntCache(width * depth);

		for(int k = 0; k < depth; ++k) {
			for(int i = 0; i < width; ++i) {

				initChunkSeed(x + i, z + k);

				// one biome forever
				dest[i + k * width] =
					BiomeGenBaseJupiter.jupiterCore.biomeID;
			}
		}

		return dest;
	}

}
