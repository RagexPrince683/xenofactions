package com.hbm.dim.laythe;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.world.feature.OilBubble;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorLaythe implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if(world.provider.dimensionId == SpaceConfig.laytheDimension) {
            generateLaythe(world, random, chunkX * 16, chunkZ * 16);
        }
    }

	private void generateLaythe(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

		if(WorldConfig.laytheOilSpawn > 0 && rand.nextInt(WorldConfig.laytheOilSpawn) == 0) {
			int randPosX = i + rand.nextInt(16);
			int randPosY = rand.nextInt(25);
			int randPosZ = j + rand.nextInt(16);

			OilBubble.spawnOil(world, randPosX, randPosY, randPosZ, 10 + rand.nextInt(7), ModBlocks.ore_oil, meta, Blocks.stone);
		}

		//table salt
		DungeonToolbox.generateOre(world, rand, i, j, 12, 4, 16, 8, Blocks.netherrack, 0);

		//clay
		DungeonToolbox.generateOre(world, rand, i, j, 20, 12, 32, 0, Blocks.clay, 0);

		//sulfur
		DungeonToolbox.generateOre(world, rand, i, j, 18, 10, 28, 0, ModBlocks.ore_sulfur, meta);


		//pre existing
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn, 4, 16, 16, ModBlocks.ore_asbestos, meta);
    }

}
