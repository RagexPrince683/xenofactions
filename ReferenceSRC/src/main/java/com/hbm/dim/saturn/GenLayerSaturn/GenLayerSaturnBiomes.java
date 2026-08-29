package com.hbm.dim.saturn.GenLayerSaturn;

import com.hbm.dim.saturn.biome.BiomeGenBaseSaturn;
import net.minecraft.world.gen.layer.GenLayer;

public class GenLayerSaturnBiomes extends GenLayer {

	public GenLayerSaturnBiomes(long seed) {
		super(seed);
	}

	@Override
	public int[] getInts(int x, int z, int width, int depth) {

		//where evil ass hexagon?

		int[] dest = new int[width * depth];

		for(int k = 0; k < depth; ++k) {
			for(int i = 0; i < width; ++i) {

				initChunkSeed(x + i, z + k);

				// one biome forever
				dest[i + k * width] =
					BiomeGenBaseSaturn.saturnCore.biomeID;
			}
		}

		return dest;
	}


}
