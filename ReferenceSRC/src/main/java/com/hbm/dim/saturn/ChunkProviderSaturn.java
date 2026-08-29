package com.hbm.dim.saturn;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.GasGiantChunkUtil;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.List;

public class ChunkProviderSaturn extends ChunkProviderCelestial {

	public ChunkProviderSaturn(World world, long seed, boolean hasMapFeatures) {
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

				int worldX =
					x * 16 + bx;

				int worldZ =
					z * 16 + bz;

				// Saturn is paler and smoother, but still organized into jet bands.
				double band =
					Math.sin(worldZ * 0.006D) * 7.0D +
					Math.sin(worldZ * 0.018D) * 2.5D +
					Math.cos(worldX * 0.004D) * 3.5D;

				int cloudTop =
					184 + (int)band;

				double polarRadius =
					Math.sqrt(worldX * worldX + worldZ * worldZ);

				double polarAngle =
					Math.atan2(worldZ, worldX);

				boolean hexagonWall =
					polarRadius < 190.0D
						&& polarRadius > 120.0D
						&& Math.cos(polarAngle * 6.0D) > 0.72D;

				int columnIndexBase =
					(bx * 16 + bz) * 256;

				for(int y = 0; y < 256; y++) {

					int index =
						columnIndexBase + y;

					if(y > cloudTop + 40) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 24)
								? ModBlocks.cloud
								: Blocks.air;
					}

					// Thin ammonia haze.
					else if(y > cloudTop + 12) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 5)
								? ModBlocks.cloud
								: Blocks.air;
					}

					// Broad pale cloud deck.
					else if(y > cloudTop - 16) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 7)
								? ModBlocks.cloud
								: ModBlocks.cloud_dense;
					}

					// Hydrogen/helium storm layer with rare lightning cells.
					else if(y > 118) {

						boolean storm =
							hexagonWall
								|| GasGiantChunkUtil.chance(worldX, y, worldZ, 18);

						buffer.blocks[index] =
							storm
								? ModBlocks.saturn_storm
								: ModBlocks.cloud_dense;
					}

					// Helium rain / supercritical hydrogen interior.
					else if(y > 58) {

						buffer.blocks[index] =
							ModBlocks.supercritical_hydrogen;
					}

					// Deep metallic hydrogen.
					else {

						buffer.blocks[index] =
							ModBlocks.metallic_hydrogen;
					}
				}

				if(GasGiantChunkUtil.chance(worldX, 13, worldZ, 260)) {

					int stormHeight =
						GasGiantChunkUtil.range(worldX, 17, worldZ, 14, 28);

					for(int sy = 0; sy < stormHeight; sy++) {

						int y =
							cloudTop - 4 + sy;

						if(y >= 256)
							break;

						if(y > 0) {
							buffer.blocks[columnIndexBase + y] =
								ModBlocks.saturn_storm;
						}
					}
				}
			}
		}

		return buffer;
	}
}
