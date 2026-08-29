package com.hbm.dim.uranus;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.GasGiantChunkUtil;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.List;

public class ChunkProviderUranus extends ChunkProviderCelestial {

	public ChunkProviderUranus(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		stoneBlock = Blocks.packed_ice;
		seaBlock = Blocks.ice;
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

				int columnIndexBase =
					(bx * 16 + bz) * 256;

				// Uranus has muted, high-altitude methane bands and weak storms.
				double bandNoise =
					Math.sin(worldZ * 0.0025D) * 0.6D +
					Math.cos(worldX * 0.002D) * 0.4D +
					Math.sin(worldX * 0.0008D) * 0.2D;

				int cloudTop =
					165 + (int)(bandNoise * 8D);

				int upperCloudTop =
					cloudTop + 22;

				int hazeTop =
					cloudTop + 48;

				for(int y = 0; y < 256; y++) {

					int index =
						columnIndexBase + y;

					if(y > hazeTop) {

						buffer.blocks[index] =
							Blocks.air;
					}

					// Pale methane haze.
					else if(y > upperCloudTop) {

						double hazeBand =
							Math.sin(worldZ * 0.008D + y * 0.12D);

						buffer.blocks[index] =
							hazeBand > 0.65D
								? ModBlocks.uranus_cloud
								: Blocks.air;
					}

					// Smooth visible methane cloud deck.
					else if(y > cloudTop - 10) {

						double cloudBand =
							Math.sin(worldZ * 0.015D) +
							Math.cos(worldX * 0.008D);

						if(cloudBand > 0.2D) {

							buffer.blocks[index] =
								ModBlocks.uranus_cloud_dense;
						}

						else if(cloudBand > -0.4D) {

							buffer.blocks[index] =
								ModBlocks.uranus_cloud;
						}

						else {

							buffer.blocks[index] =
								Blocks.air;
						}
					}

					// Dense hydrogen/helium/methane atmosphere.
					else if(y > 90) {

						buffer.blocks[index] =
							ModBlocks.uranus_atmosphere;
					}

					// Ice giant mantle: water, ammonia and methane slurry.
					else if(y > 45) {

						buffer.blocks[index] =
							ModBlocks.ammonia_water;
					}

					// Supercritical water/ammonia ocean.
					else if(y > 18) {

						buffer.blocks[index] =
							ModBlocks.supercritical_water;
					}

					// Sparse diamond-rain inclusions in the compressed methane zone.
					else if(y > 8) {

						buffer.blocks[index] =
							GasGiantChunkUtil.chance(worldX, y, worldZ, 90)
								? Blocks.diamond_ore
								: ModBlocks.supercritical_water;
					}

					else {

						buffer.blocks[index] =
							Blocks.obsidian;
					}
				}

				if(GasGiantChunkUtil.chance(worldX, 5, worldZ, 1024)) {

					int stormHeight =
						GasGiantChunkUtil.range(worldX, 7, worldZ, 10, 16);

					for(int sy = 0; sy < stormHeight; sy++) {

						int y =
							cloudTop + sy;

						if(y >= 256)
							break;

						if(y > 0) {
							buffer.blocks[columnIndexBase + y] =
								ModBlocks.uranus_cloud_dense;
						}
					}
				}
			}
		}

		return buffer;
	}
}
