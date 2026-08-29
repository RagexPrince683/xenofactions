package com.hbm.dim.eve;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockVolcano;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.eve.GenLayerEve.WorldGenElectricVolcano;
import com.hbm.dim.eve.GenLayerEve.WorldGenEveSpike;
import com.hbm.dim.eve.biome.BiomeGenBaseEve;
import com.hbm.world.feature.OilBubble;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorEve implements IWorldGenerator {

	WorldGenElectricVolcano volcano = new WorldGenElectricVolcano(30, 22, ModBlocks.eve_silt, ModBlocks.eve_rock);

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.eveDimension) {
			generateEve(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	//venus
	private void generateEve(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);




		//iron bearing
		DungeonToolbox.generateOre(world, rand, i, j, 20, 12, 16, 64, ModBlocks.ore_iron, meta, ModBlocks.eve_rock);

		//basalts
		DungeonToolbox.generateOre(world, rand, i, j, 18, 32, 0, 128, ModBlocks.basalt, 0, ModBlocks.eve_rock);

		DungeonToolbox.generateOre(world, rand, i, j, 14, 6, 16, 64, ModBlocks.ore_basalt, 0, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 18, 8, 8, 32, ModBlocks.ore_basalt, 1, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 10, 9, 8, 48, ModBlocks.ore_basalt, 2, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 8, 4, 0, 24, ModBlocks.ore_basalt, 3, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 12, 10, 16, 64, ModBlocks.ore_basalt, 4, ModBlocks.basalt);


		//pre existing:
		DungeonToolbox.generateOre(world, rand, i, j, 12,  8, 1, 33, ModBlocks.ore_niobium, meta, ModBlocks.eve_rock);
		DungeonToolbox.generateOre(world, rand, i, j, 8,  4, 5, 48, ModBlocks.ore_iodine, meta, ModBlocks.eve_rock);

		if(WorldConfig.eveGasSpawn > 0 && rand.nextInt(WorldConfig.eveGasSpawn) == 0) {
			int randPosX = i + rand.nextInt(16);
			int randPosY = rand.nextInt(25);
			int randPosZ = j + rand.nextInt(16);

			OilBubble.spawnOil(world, randPosX, randPosY, randPosZ, 10 + rand.nextInt(7), ModBlocks.ore_gas, meta, ModBlocks.eve_rock);
		}

		int x = i + rand.nextInt(16) + 8;
		int z = j + rand.nextInt(16) + 8;
		int y = world.getHeightValue(x, z);

		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		if(biome == BiomeGenBaseEve.eveSeismicPlains) {
			new WorldGenEveSpike().generate(world, rand, x, y, z);
		}

		if(rand.nextInt(100) == 0) {
			volcano.generate(world, rand, x, y, z);

		}


		for(int k = 0; k < 2; k++){
			int d = 16 + rand.nextInt(96);

			for(y = d - 5; y <= d; y++) {
				Block b = world.getBlock(x, y, z);
				if(world.getBlock(x, y + 1, z) == Blocks.air && (b == ModBlocks.eve_rock || b == ModBlocks.eve_silt)) {
					world.setBlock(x, y, z, ModBlocks.geysir_nether);
					world.setBlock(x+1, y, z, Blocks.netherrack);
					world.setBlock(x-1, y, z, Blocks.netherrack);
					world.setBlock(x, y, z+1, Blocks.netherrack);
					world.setBlock(x, y, z-1, Blocks.netherrack);
					world.setBlock(x+1, y-1, z, Blocks.netherrack);
					world.setBlock(x-1, y-1, z, Blocks.netherrack);
					world.setBlock(x, y-1, z+1, Blocks.netherrack);
					world.setBlock(x, y-1, z-1, Blocks.netherrack);
				}
			}
		}

		// Kick the volcanoes into action, and fix SOME floating lava
		// a full fix for floating lava would cause infinite cascades so we uh, don't

		//todone volcano implem example here
		for(x = 0; x < 16; x++) {
			for(z = 0; z < 16; z++) {
				for(y = 32; y < 128; y++) {
					int ox = i + x;
					int oz = j + z;
					Block b = world.getBlock(ox, y, oz);

					if(b == Blocks.lava && world.getBlock(ox, y - 1, oz) == Blocks.air) {
						world.setBlock(ox, y - 1, oz, Blocks.lava, 0, 0);
						world.markBlockForUpdate(ox, y - 1, oz);
					} else if(b == ModBlocks.volcano_core) {
						world.setBlock(ox, y, oz, ModBlocks.volcano_core, BlockVolcano.META_STATIC_EXTINGUISHING, 0);
						world.markBlockForUpdate(ox, y, oz);
					}
				}
			}
		}

	}

}
