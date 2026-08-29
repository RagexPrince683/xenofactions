package com.hbm.dim.jupiter;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.GasGiantChunkUtil;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.List;

public class ChunkProviderJupiter extends ChunkProviderCelestial {

	public ChunkProviderJupiter(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		stoneBlock = Blocks.hardened_clay;
		seaBlock = Blocks.air;
	}

	@Override
	public List getPossibleCreatures(EnumCreatureType creatureType, int x, int y, int z) {
		return null;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {

		BlockMetaBuffer buffer =
			new BlockMetaBuffer();

		this.biomesForGeneration =
			this.worldObj.getWorldChunkManager().loadBlockGeneratorData(
				this.biomesForGeneration,
				x * 16,
				z * 16,
				16,
				16
			);

		for(int bx = 0; bx < 16; bx++) {
			for(int bz = 0; bz < 16; bz++) {

				int worldX = x * 16 + bx;
				int worldZ = z * 16 + bz;

				// Jupiter has strong east-west belts with high turbulent relief.
				double belt =
					Math.sin(worldZ * 0.010D) * 13.0D +
					Math.sin(worldZ * 0.031D) * 4.0D +
					Math.cos(worldX * 0.006D) * 5.0D;

				int cloudTop =
					190 + (int)belt;

				double ovalStorm =
					Math.sin(worldX * 0.018D + worldZ * 0.004D) *
					Math.cos(worldZ * 0.022D);

				int columnIndexBase =
					(bx * 16 + bz) * 256;

				for(int y = 0; y < 256; y++) {

					int index =
						columnIndexBase + y;

					if(y > cloudTop + 42) {

						buffer.blocks[index] =
							Blocks.air;
					}

					// Thin ammonia haze at the very top.
					else if(y > cloudTop + 16) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 4)
								? ModBlocks.cloud
								: Blocks.air;
					}

					// Bright ammonia cloud deck.
					else if(y > cloudTop - 12) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 5)
								? ModBlocks.cloud
								: ModBlocks.cloud_dense;
					}

					// Darker ammonium/sulfide cloud and storm belt region.
					else if(y > 118) {

						boolean storm =
							ovalStorm > 0.58D
								|| GasGiantChunkUtil.chance(worldX, y, worldZ, 11);

						buffer.blocks[index] =
							storm
								? ModBlocks.jupiter_storm
								: ModBlocks.cloud_dense;
					}

					// Molecular hydrogen/helium becomes liquid-like under pressure.
					else if(y > 48) {

						buffer.blocks[index] =
							ModBlocks.supercritical_hydrogen;
					}

					// Metallic hydrogen layer: no solid surface.
					else {

						buffer.blocks[index] =
							ModBlocks.metallic_hydrogen;
					}
				}

				if(GasGiantChunkUtil.chance(worldX, 31, worldZ, 72)) {

					int stormHeight =
						GasGiantChunkUtil.range(worldX, 37, worldZ, 24, 42);

					for(int sy = 0; sy < stormHeight; sy++) {

						int y =
							cloudTop - 8 + sy;

						if(y >= 256)
							break;

						if(y > 0) {
							buffer.blocks[columnIndexBase + y] =
								ModBlocks.jupiter_storm;
						}
					}
				}
			}
		}

		return buffer;
	}
}
