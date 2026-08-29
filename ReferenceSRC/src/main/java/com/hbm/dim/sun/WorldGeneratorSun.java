package com.hbm.dim.sun;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorSun implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
						 IChunkProvider chunkGenerator,
						 IChunkProvider chunkProvider) {

		return;
	}

	//shit caused a game crash - cascading death for pcs

	//private void generateSun(World world, Random rand, int i, int j) {
//
	//	for(int x = 0; x < 16; x++) {
	//		for(int z = 0; z < 16; z++) {
//
	//			int ox = i + x;
	//			int oz = j + z;
//
	//			// slight height variation so it's not perfectly flat
	//			int surface = 220 + rand.nextInt(24);
//
	//			for(int y = 0; y < 256; y++) {
//
	//				// deep interior = fully molten
	//				if(y < surface - 20) {
//
	//					// occasional denser solar material pockets
	//					if(rand.nextInt(120) == 0) {
	//						world.setBlock(ox, y, oz, ModBlocks.basalt);
	//					} else {
	//						world.setBlock(ox, y, oz, Blocks.lava);
	//					}
//
	//				}
	//				// convection / unstable layer
	//				else if(y < surface) {
//
	//					int r = rand.nextInt(100);
//
	//					if(r < 70) {
	//						world.setBlock(ox, y, oz, Blocks.lava);
	//					}
	//					else if(r < 92) {
	//						world.setBlock(ox, y, oz, Blocks.lava);
	//					}
	//					else {
	//						world.setBlock(ox, y, oz, Blocks.fire);
	//					}
//
	//				}
	//				// upper plasma "surface"
	//				else {
//
	//					// mostly empty but constantly burning
	//					if(rand.nextInt(4) == 0) {
	//						world.setBlock(ox, y, oz, Blocks.fire);
	//					} else {
	//						world.setBlock(ox, y, oz, Blocks.air);
	//					}
	//				}
	//			}
//
	//			// giant lava plume / solar flare columns
	//			if(rand.nextInt(12) == 0) {
//
	//				int flareHeight = 40 + rand.nextInt(80);
//
	//				for(int y = surface; y < Math.min(255, surface + flareHeight); y++) {
//
	//					if(rand.nextBoolean()) {
	//						world.setBlock(ox, y, oz, Blocks.lava);
	//					}
//
	//					// branching flare
	//					if(rand.nextInt(8) == 0) {
	//						world.setBlock(
	//							ox + rand.nextInt(3) - 1,
	//							y,
	//							oz + rand.nextInt(3) - 1,
	//							Blocks.fire
	//						);
	//					}
	//				}
	//			}
	//		}
	//	}
	//}
}
