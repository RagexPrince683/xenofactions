package com.hbm.dim.saturn.biome;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.dim.jupiter.biome.BiomeGenJupiter;

public class BiomeGenBaseSaturn {

	public static final BiomeGenBaseCelestial saturnCore =
		(BiomeGenBaseCelestial)new BiomeGenSaturn(
			SpaceConfig.saturnBiome
		).setBiomeName("Saturn Core");
}
