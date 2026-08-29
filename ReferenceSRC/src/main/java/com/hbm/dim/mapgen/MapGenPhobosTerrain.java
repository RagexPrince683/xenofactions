package com.hbm.dim.mapgen;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.noise.DoublePerlinNoiseSampler;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;

import java.util.Random;

public class MapGenPhobosTerrain extends MapGenBase {

	private DoublePerlinNoiseSampler terrainNoise;
	private DoublePerlinNoiseSampler detailNoise;

	public Block stone = ModBlocks.ike_stone;
	public Block regolith = ModBlocks.ike_regolith;

	public int seaLevel = 72;

	@Override
	public void func_151539_a(
		IChunkProvider provider,
		World world,
		int chunkX,
		int chunkZ,
		Block[] blocks) {

		this.worldObj = world;

		if(terrainNoise == null) {

			Random seedRand =
				new Random(world.getSeed());

			terrainNoise =
				DoublePerlinNoiseSampler.create(
					seedRand,
					-6,
					1.0D,
					2.0D
				);

			detailNoise =
				DoublePerlinNoiseSampler.create(
					seedRand,
					-2,
					1.0D,
					2.0D
				);
		}

		generateTerrain(chunkX, chunkZ, blocks);
	}

	private void generateTerrain(
		int chunkX,
		int chunkZ,
		Block[] blocks) {

		Random craterRand = new Random(
			//crashed here
			worldObj.getSeed()
				^ (chunkX * 341873128712L)
				^ (chunkZ * 132897987541L)
		);

		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {

				int regolithDepth =
					2 + craterRand.nextInt(4);

				int worldX = chunkX * 16 + x;
				int worldZ = chunkZ * 16 + z;

				double base =
					terrainNoise.sample(
						worldX * 0.01,
						0,
						worldZ * 0.01
					) * 12;

				double detail =
					detailNoise.sample(
						worldX * 0.05,
						0,
						worldZ * 0.05
					) * 3;

				int height =
					seaLevel + (int)(base + detail);

				height = Math.max(8,
								  Math.min(255, height));

				for(int y = 0; y <= height; y++) {

					int index =
						(x * 16 + z) * 256 + y;



					if(y >= height - regolithDepth) {
						blocks[index] = regolith;
					} else {
						blocks[index] = stone;
					}
				}
			}
		}

		int craterCount = craterRand.nextInt(3);

		for(int i = 0; i < craterCount; i++) {

			carveCrater(
				blocks,
				craterRand.nextInt(48) - 16,
				craterRand.nextInt(48) - 16,
				4 + craterRand.nextInt(12)
			);
		}
	}

	private void carveCrater(
		Block[] blocks,
		int craterX,
		int craterZ,
		int radius) {

		for(int x = -radius; x <= radius; x++) {
			for(int z = -radius; z <= radius; z++) {

				double dist = Math.sqrt(x * x + z * z);

				if(dist > radius)
					continue;

				int localX = craterX + x;
				int localZ = craterZ + z;

				if(localX < 0 || localX > 15 ||
					localZ < 0 || localZ > 15)
					continue;

				int depth =
					(int)((1.0 - dist / radius) * radius * 0.6);

				for(int y = 255; y > 0; y--) {

					int index =
						(localX * 16 + localZ) * 256 + y;

					if(blocks[index] != null) {

						for(int d = 0; d < depth; d++) {

							int removeIndex =
								(localX * 16 + localZ)
									* 256
									+ (y - d);

							if(removeIndex >= 0)
								blocks[removeIndex] = null;
						}

						break;
					}
				}
			}
		}
	}

}
