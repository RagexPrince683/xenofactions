package com.hbm.dim.neptune.biome;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeGenBaseCelestial;

public class BiomeGenBaseNeptune {

	public static final BiomeGenBaseCelestial neptune =
		(BiomeGenBaseCelestial)new BiomeGenNeptune(
			SpaceConfig.neptuneBiome
		).setBiomeName("Neptune");
}
