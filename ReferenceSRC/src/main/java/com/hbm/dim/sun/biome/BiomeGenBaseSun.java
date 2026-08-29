package com.hbm.dim.sun.biome;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeGenBaseCelestial;
import net.minecraftforge.common.BiomeDictionary;

public abstract class BiomeGenBaseSun {

	public static final BiomeGenBaseCelestial sunCore =
		(BiomeGenBaseCelestial)new BiomeGenSun(
			SpaceConfig.sunBiome
		).setBiomeName("Sun");


	//public BiomeGenBaseSun (int id){
	//	super(id);
	//	BiomeDictionary.registerBiomeType(
	//		this,
	//		BiomeDictionary.Type.HOT,
	//		BiomeDictionary.Type.DEAD,
	//		BiomeDictionary.Type.WASTELAND
	//	);
	//}

}
