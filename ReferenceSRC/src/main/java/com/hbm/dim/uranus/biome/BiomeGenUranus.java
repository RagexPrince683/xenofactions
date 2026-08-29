package com.hbm.dim.uranus.biome;

import com.hbm.dim.BiomeGenBaseCelestial;
import net.minecraft.world.World;

import java.util.Random;

public class BiomeGenUranus extends BiomeGenBaseCelestial {

	public BiomeGenUranus(int id) {
		super(id);

		this.rootHeight = 8.0F;
		this.heightVariation = 0.0F;

		this.rainfall = 0.0F;
		this.temperature = 0.0F;

		this.enableRain = false;


		this.theBiomeDecorator.sandPerChunk = 0;
		this.theBiomeDecorator.sandPerChunk2 = 0;
		this.theBiomeDecorator.clayPerChunk = 0;

		theBiomeDecorator.generateLakes = false;
		theBiomeDecorator.treesPerChunk = 0;
		theBiomeDecorator.flowersPerChunk = 0;
		theBiomeDecorator.grassPerChunk = 0;
		theBiomeDecorator.mushroomsPerChunk = 0;
		theBiomeDecorator.deadBushPerChunk = 0;
		theBiomeDecorator.reedsPerChunk = 0;
		theBiomeDecorator.cactiPerChunk = 0;
		theBiomeDecorator.bigMushroomsPerChunk = 0;

		this.spawnableCreatureList.clear();
		this.spawnableMonsterList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();

	}

	@Override
	public void decorate(
		World world,
		Random rand,
		int x,
		int z
	) {
		// no vanilla surface features
	}

}
