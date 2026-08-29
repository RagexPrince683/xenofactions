package com.hbm.dim.dres;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorDres implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.dresDimension) {
			generateDres(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateDres(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

		//Phyllosilicates
		DungeonToolbox.generateOre(world, rand, i, j, 18, 12, 40, 20, Blocks.clay,0, ModBlocks.dres_rock);
		//Carbonates
		DungeonToolbox.generateOre(world, rand, i, j, 10, 8, 20, 30, ModBlocks.block_graphite,0, ModBlocks.dres_rock);

		//Ammonium salts
		//god dammit we don't have table salt in NTM
		DungeonToolbox.generateOre(world, rand, i, j, 6, 6, 10, 10, Blocks.netherrack, 0, ModBlocks.dres_rock);
		//fuck this you're getting netherrack and a trade for pam's table salt at spawn if needed
		// too lazy to add another fucking ore to this 3k commit behind rendering shit hole

		//water ice
		DungeonToolbox.generateOre(world, rand, i, j, 12, 4, 5, 15, Blocks.packed_ice, 0, ModBlocks.dres_rock);

		//magnetite (iron)
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn, 18, 2, 27, ModBlocks.ore_iron, meta, ModBlocks.dres_rock);



		//other random shit that james probably added for random progression...?
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cobaltSpawn, 4, 3, 22, ModBlocks.ore_cobalt, meta, ModBlocks.dres_rock);
		DungeonToolbox.generateOre(world, rand, i, j, 12,  8, 1, 33, ModBlocks.ore_niobium, meta, ModBlocks.dres_rock);
		DungeonToolbox.generateOre(world, rand, i, j, GeneralConfig.coltanRate, 4, 15, 40, ModBlocks.ore_coltan, meta, ModBlocks.dres_rock);
		DungeonToolbox.generateOre(world, rand, i, j, 1, 6, 4, 64, ModBlocks.ore_lanthanium, meta, ModBlocks.dres_rock);

        DungeonToolbox.generateOre(world, rand, i, j, 1, 12, 8, 32, ModBlocks.ore_shale, meta, ModBlocks.dres_rock);
	}
}
