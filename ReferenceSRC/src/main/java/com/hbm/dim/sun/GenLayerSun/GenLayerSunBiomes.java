package com.hbm.dim.sun.GenLayerSun;

import com.hbm.dim.sun.biome.BiomeGenBaseSun;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerSunBiomes extends GenLayer {

	public GenLayerSunBiomes(long seed) {
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
					BiomeGenBaseSun.sunCore.biomeID;
			}
		}

		return dest;
	}
}
