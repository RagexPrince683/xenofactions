package com.hbm.dim.neptune.biome;

import com.hbm.dim.BiomeGenBaseCelestial;
import net.minecraft.world.World;

import java.util.Random;

public class BiomeGenNeptune extends BiomeGenBaseCelestial {

	public BiomeGenNeptune(int id) {
		super(id);

		this.rootHeight = 8.0F;
		this.heightVariation = 0.0F;

		this.rainfall = 0.0F;
		this.temperature = 0.0F;

		this.enableRain = false;

		theBiomeDecorator.generateLakes = false;
		this.theBiomeDecorator.treesPerChunk = 0;
		this.theBiomeDecorator.flowersPerChunk = 0;
		this.theBiomeDecorator.grassPerChunk = 0;
		this.theBiomeDecorator.deadBushPerChunk = 0;
		this.theBiomeDecorator.mushroomsPerChunk = 0;
		this.theBiomeDecorator.reedsPerChunk = 0;
		this.theBiomeDecorator.cactiPerChunk = 0;
		this.theBiomeDecorator.sandPerChunk = 0;
		this.theBiomeDecorator.sandPerChunk2 = 0;
		this.theBiomeDecorator.clayPerChunk = 0;
		this.theBiomeDecorator.bigMushroomsPerChunk = 0;

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
		// no vanilla surface features in a gas giant atmosphere
	}
}
