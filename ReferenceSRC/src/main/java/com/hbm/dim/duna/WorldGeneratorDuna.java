package com.hbm.dim.duna;

import java.util.Random;

import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.WorldTypeTeleport;
import com.hbm.dim.moon.UndergroundLakeGenerator;
import com.hbm.main.ResourceManager;
import com.hbm.world.feature.OilBubble;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorDuna implements IWorldGenerator {

	private final UndergroundLakeGenerator lakeGenerator = new UndergroundLakeGenerator();

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.dunaDimension) {
			generateDuna(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateDuna(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

		//if(WorldConfig.dunaOilSpawn > 0 && rand.nextInt(WorldConfig.dunaOilSpawn) == 0) {
		//	int randPosX = i + rand.nextInt(16);
		//	int randPosY = rand.nextInt(25);
		//	int randPosZ = j + rand.nextInt(16);
//
		//	OilBubble.spawnOil(world, randPosX, randPosY, randPosZ, 10 + rand.nextInt(7), ModBlocks.ore_oil, meta, ModBlocks.duna_rock);
		//}
		//fake


		//iron
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.nickelSpawn, 8, 1, 43, ModBlocks.ore_iron, meta, ModBlocks.duna_rock);
		//zinc
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumSpawn, 9, 4, 27, ModBlocks.ore_zinc, meta, ModBlocks.duna_rock);
		//titanium
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumSpawn, 9, 4, 27, ModBlocks.ore_titanium, meta, ModBlocks.duna_rock);
		//Aluminum
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.aluminiumSpawn, 8, 4, 27, ModBlocks.ore_aluminium, meta, ModBlocks.duna_rock);
		//Copper
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn, 8, 4, 27, ModBlocks.ore_copper, meta, ModBlocks.duna_rock);
		//Cobalt
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cobaltSpawn, 8, 4, 17, ModBlocks.ore_cobalt, meta, ModBlocks.duna_rock);
		//Lithium
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.lithiumSpawn, 2, 60, 17, ModBlocks.ore_lithium, meta, ModBlocks.duna_rock);
		//Gold
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cobaltSpawn, 2, 4, 12, ModBlocks.ore_gold, meta, ModBlocks.duna_rock);

		//Hematite
		DungeonToolbox.generateOre(world, rand, i, j, 16, 12, 25, 30, ModBlocks.stone_resource, BlockEnums.EnumStoneType.HEMATITE.ordinal(), ModBlocks.duna_rock);

		if (rand.nextInt(10) < 2) { // Adjust frequency here
			lakeGenerator.generate(world, rand, i, j);
		}

		// Basalt rich in minerals, but only in basaltic caves!
		//THERES NO FUCKING FLUORITE OR ASBESTOS ON MARS YOU OAF
		DungeonToolbox.generateOre(world, rand, i, j, 12, 6, 0, 16, ModBlocks.ore_basalt, 0, ModBlocks.basalt);
		//DungeonToolbox.generateOre(world, rand, i, j, 8, 8, 0, 16, ModBlocks.ore_basalt, 1, ModBlocks.basalt);
		//DungeonToolbox.generateOre(world, rand, i, j, 8, 9, 0, 16, ModBlocks.ore_basalt, 2, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 2, 4, 0, 16, ModBlocks.ore_basalt, 3, ModBlocks.basalt);
		DungeonToolbox.generateOre(world, rand, i, j, 6, 10, 0, 16, ModBlocks.ore_basalt, 4, ModBlocks.basalt);


		if(i == 0 && j == 0 && world.getWorldInfo().getTerrainType() == WorldTypeTeleport.martian) {
			int x = 0;
			int z = 0;
			int y = world.getHeightValue(x, z) - 1;

			ResourceManager.martian.build(world, x, y, z);
		}
	}

}
