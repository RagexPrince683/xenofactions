package com.hbm.dim.uranus.GenLayerUranus;

import com.hbm.dim.saturn.biome.BiomeGenBaseSaturn;
import com.hbm.dim.uranus.biome.BiomeGenBaseUranus;
import net.minecraft.world.gen.layer.GenLayer;

public class GenLayerUranusBiomes extends GenLayer {
	public GenLayerUranusBiomes(long seed) {
		super(seed);
	}

	@Override
	public int[] getInts(int x, int z, int width, int depth) {

		int[] dest = new int[width * depth];

		for(int k = 0; k < depth; ++k) {
			for(int i = 0; i < width; ++i) {

				initChunkSeed(x + i, z + k);

				// one biome forever
				dest[i + k * width] =
					BiomeGenBaseUranus.uranusCore.biomeID;
			}
		}

		return dest;
	}

}
