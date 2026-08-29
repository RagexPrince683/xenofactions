package com.hbm.dim.neptune.GenLayerNeptune;

import com.hbm.dim.neptune.biome.BiomeGenBaseNeptune;
import net.minecraft.world.gen.layer.GenLayer;

public class GenLayerNeptuneBiomes extends GenLayer {

	public GenLayerNeptuneBiomes(long seed) {
		super(seed);
	}

	@Override
	public int[] getInts(int x, int z, int width, int depth) {
		int[] dest = new int[width * depth];

		for(int k = 0; k < depth; ++k) {
			for(int i = 0; i < width; ++i) {

				initChunkSeed(x + i, z + k);

				// one biome forever?
				dest[i + k * width] =
					BiomeGenBaseNeptune.neptune.biomeID;
			}
		}

		return dest;
	}




}
