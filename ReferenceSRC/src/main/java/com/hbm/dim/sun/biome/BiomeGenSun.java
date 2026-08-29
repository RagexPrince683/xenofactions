package com.hbm.dim.sun.biome;

import com.hbm.dim.BiomeGenBaseCelestial;

public class BiomeGenSun extends BiomeGenBaseCelestial {

	public BiomeGenSun(int id) {
		super(id);

		this.rootHeight = 2.0F;
		this.heightVariation = 0.0F;

		this.enableRain = false;
		this.temperature = 999F;
		this.rainfall = 0F;

		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
	}
}
