package com.hbm.dim.sun;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class ChunkProviderSun extends ChunkProviderCelestial {

	public ChunkProviderSun(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		stoneBlock = Blocks.lava;
		seaBlock = Blocks.lava;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {

		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);

		for(int bx = 0; bx < 16; bx++) {
			for(int bz = 0; bz < 16; bz++) {

				int worldX = x * 16 + bx;
				int worldZ = z * 16 + bz;

				int surface =
					220 +
						(int)(Math.sin(worldX * 0.04D) * 8) +
						(int)(Math.cos(worldZ * 0.04D) * 8);

				int columnIndexBase = (bx * 16 + bz) * 256;

				// ==========================================
				// SUN LAYER MODEL (DISTANCE FROM SURFACE)
				// ==========================================

				for(int y = 0; y < 256; y++) {

					int index = columnIndexBase + y;
					int dist = y - surface;

					// =========================
					// CORE (fusion region)
					// =========================
					if(dist < -25) {
						buffer.blocks[index] = ModBlocks.basalt;
					}

					// =========================
					// RADIATIVE ZONE
					// =========================
					else if(dist < -10) {
						buffer.blocks[index] = ModBlocks.basalt;
					}

					// =========================
					// CONVECTIVE ZONE
					// =========================
					else if(dist < 5) {

						if(rand.nextInt(10) == 0) {
							buffer.blocks[index] = ModBlocks.solar_plasma;
						} else {
							buffer.blocks[index] = ModBlocks.basalt;
						}
					}

					// =========================
					// PHOTOSPHERE (visible surface)
					// =========================
					else if(dist < 12) {

						if(rand.nextInt(3) == 0) {
							buffer.blocks[index] = ModBlocks.basalt;
						} else {
							buffer.blocks[index] = ModBlocks.solar_plasma;
						}
					}

					// =========================
					// CHROMOSPHERE
					// =========================
					else if(dist < 30) {

						if(rand.nextInt(2) == 0) {
							buffer.blocks[index] = ModBlocks.solar_plasma;
						} else {
							buffer.blocks[index] = Blocks.air;
						}
					}

					// =========================
					// CORONA
					// =========================
					else {
						buffer.blocks[index] = Blocks.air;
					}
				}

				// ==========================================
				// FLARE SYSTEM (ONLY FROM PHOTOSPHERE)
				// ==========================================

				int flareRoll = rand.nextInt(55);

				if(flareRoll == 0) {

					int flareHeight = 10 + rand.nextInt(25);

					for(int fy = 0; fy < flareHeight; fy++) {

						int y = surface + fy;
						if(y >= 256) break;

						int index = columnIndexBase + y;

						buffer.blocks[index] = ModBlocks.solar_plasma;

						// reduced branching (coronal loops)
						if(fy > 2 && rand.nextInt(8) == 0) {

							int bxOff = bx + rand.nextInt(3) - 1;
							int bzOff = bz + rand.nextInt(3) - 1;

							if(bxOff >= 0 && bxOff < 16 && bzOff >= 0 && bzOff < 16) {
								int sideIndex = (bxOff * 16 + bzOff) * 256 + y;
								buffer.blocks[sideIndex] = ModBlocks.solar_plasma;
							}
						}
					}
				}
			}
		}

		return buffer;
	}
}
