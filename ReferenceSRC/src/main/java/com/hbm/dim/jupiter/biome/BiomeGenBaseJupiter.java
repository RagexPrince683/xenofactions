package com.hbm.dim.jupiter.biome;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.dim.sun.biome.BiomeGenSun;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenBaseJupiter {

	//public static BiomeGenBase jupiterCore;

	//public static void registerBiomes() {

		//jupiterCore =
		//	(BiomeGenBaseCelestial)new BiomeGenJupiter(
		//		SpaceConfig.jupiterDimension
		//	).setBiomeName("jupiter_core");

		public static final BiomeGenBaseCelestial jupiterCore =
			(BiomeGenBaseCelestial)new BiomeGenJupiter(
				SpaceConfig.jupiterBiome
			).setBiomeName("Jupiter Core");

	//}
}
