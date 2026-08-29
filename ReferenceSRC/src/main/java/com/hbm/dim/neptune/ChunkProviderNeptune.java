package com.hbm.dim.neptune;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.GasGiantChunkUtil;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.List;

public class ChunkProviderNeptune extends ChunkProviderCelestial {

	public ChunkProviderNeptune(World world, long seed, boolean hasMapFeatures) {
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

				// Neptune's high clouds are dark, methane-rich and wind-sheared.
				double band =
					Math.sin(worldX * 0.007D) * 7.0D +
					Math.cos(worldZ * 0.006D) * 6.0D +
					Math.sin((worldX + worldZ) * 0.018D) * 3.0D;

				int cloudTop =
					184 + (int)band;

				double darkSpot =
					Math.sin(worldX * 0.024D + worldZ * 0.006D) *
					Math.cos(worldZ * 0.028D);

				int columnIndexBase =
					(bx * 16 + bz) * 256;

				for(int y = 0; y < 256; y++) {

					int index =
						columnIndexBase + y;

					if(y > cloudTop + 38) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 20)
								? ModBlocks.neptune_cloud_thin
								: Blocks.air;
					}

					// Thin upper methane haze.
					else if(y > cloudTop + 12) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 4)
								? ModBlocks.neptune_cloud_thin
								: Blocks.air;
					}

					// Main methane cloud deck.
					else if(y > cloudTop - 15) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 5)
								? ModBlocks.neptune_cloud
								: ModBlocks.neptune_cloud_dense;
					}

					// Deep high-speed storm layer.
					else if(y > 100) {

						boolean storm =
							darkSpot > 0.62D
								|| GasGiantChunkUtil.chance(worldX, y, worldZ, 13);

						buffer.blocks[index] =
							storm
								? ModBlocks.neptune_storm
								: ModBlocks.neptune_atmosphere_dense;
					}

					// Supercritical water/ammonia/methane mantle.
					else if(y > 35) {

						buffer.blocks[index] =
							ModBlocks.supercritical_water;
					}

					// Compressed exotic ice mantle.
					else if(y > 6) {

						buffer.blocks[index] =
							Blocks.packed_ice;
					}

					else {

						buffer.blocks[index] =
							Blocks.bedrock;
					}
				}

				if(GasGiantChunkUtil.chance(worldX, 23, worldZ, 150)) {

					int stormHeight =
						GasGiantChunkUtil.range(worldX, 29, worldZ, 25, 45);

					for(int sy = 0; sy < stormHeight; sy++) {

						int y =
							cloudTop - 8 + sy;

						if(y >= 256)
							break;

						if(y > 0) {
							buffer.blocks[columnIndexBase + y] =
								ModBlocks.neptune_storm;
						}
					}
				}
			}
		}

		return buffer;
	}
}
