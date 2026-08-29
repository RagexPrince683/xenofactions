package com.hbm.dim.uranus.biome;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.dim.saturn.biome.BiomeGenSaturn;

public class BiomeGenBaseUranus {

	public static final BiomeGenBaseCelestial uranusCore =
		(BiomeGenBaseCelestial)new BiomeGenUranus(
			SpaceConfig.uranusBiome
		).setBiomeName("Uranus");
}
