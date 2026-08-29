package com.hbm.dim.laythe.GenLayerLaythe;

import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerDiversifyLaythe extends GenLayer {

	public GenLayerDiversifyLaythe(long seed,
								   GenLayer parent) {

		super(seed);
		this.parent = parent;
	}

	@Override
	public int[] getInts(
		int x,
		int z,
		int width,
		int depth) {

		int[] input =
			this.parent.getInts(
				x,
				z,
				width,
				depth);

		int[] output =
			IntCache.getIntCache(
				width * depth);

		for(int zOut = 0;
			zOut < depth;
			zOut++) {

			for(int xOut = 0;
				xOut < width;
				xOut++) {

				int index =
					xOut + zOut * width;

				int worldX =
					x + xOut;

				int worldZ =
					z + zOut;

				double fractureNoise =
					Math.abs(
						Math.sin(
							worldX * 0.004
								+ worldZ * 0.0015));

				double chaosNoise =
					Math.abs(
						Math.sin(
							worldX * 0.0012
								- worldZ * 0.003));

				output[index] =
					BiomeGenBaseLaythe
						.europaPlains
						.biomeID;

				// fracture belts
				//if(fractureNoise > 0.985) {
//
				//	output[index] =
				//		BiomeGenBaseLaythe
				//			.europaFracture
				//			.biomeID;
				//}

				// chaos blobs
				if(chaosNoise > 0.996) {

					output[index] =
						BiomeGenBaseLaythe
							.europaChaos
							.biomeID;
				}
			}
		}

		return output;
	}
}
