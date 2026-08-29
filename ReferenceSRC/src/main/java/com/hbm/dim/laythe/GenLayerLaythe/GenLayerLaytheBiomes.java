package com.hbm.dim.laythe.GenLayerLaythe;

import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerLaytheBiomes extends GenLayer {

	public GenLayerLaytheBiomes(long seed) {
		super(seed);
	}

	@Override
	public int[] getInts(int x, int z, int width, int depth) {

		int[] dest =
			IntCache.getIntCache(
				width * depth);

		for(int dz = 0;
			dz < depth;
			dz++) {

			for(int dx = 0;
				dx < width;
				dx++) {

				dest[dx + dz * width] =
					BiomeGenBaseLaythe
						.europaPlains
						.biomeID;
			}
		}

		return dest;
	}
}
