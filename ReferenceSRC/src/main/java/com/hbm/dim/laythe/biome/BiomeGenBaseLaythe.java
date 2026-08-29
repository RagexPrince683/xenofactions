/*******************************************************************************
 * Copyright 2015 SteveKunG - More Planets Mod
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package com.hbm.dim.laythe.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.entity.mob.EntityFRIEND;
import com.hbm.entity.mob.EntityScutterfish;

import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public abstract class BiomeGenBaseLaythe extends BiomeGenBaseCelestial {


	//REPLACE WITH:
	//europaIcePlains
	//europaChaosTerrain
	//europaFractureZone
	//europaCryovolcanicRegion
	//europaImpactField
	//europaPolarFrost

	public static final BiomeGenBase europaPlains =
		new BiomeGenEuropaPlains(SpaceConfig.laytheBiome);

	public static final BiomeGenBase europaChaos =
		new BiomeGenEuropaChaos(SpaceConfig.laytheOceanBiome);

	//public static final BiomeGenBase europaFracture =
	//	new BiomeGenEuropaFracture(SpaceConfig.laytheFractureBiome);

	public static final BiomeGenBase europaPolar =
		new BiomeGenLaythePolar(SpaceConfig.laythePolarBiome);
	public static final BiomeGenBase laythePolar = new BiomeGenLaythePolar(SpaceConfig.laythePolarBiome).setTemperatureRainfall(0.2F, 0.2F);

	public BiomeGenBaseLaythe(int id) {
		super(id);
		this.waterColorMultiplier = 0x5b009a;

		//annoying

		//BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(Blocks.packed_ice);
		//decorator.waterPlantsPerChunk = 32;
		//this.theBiomeDecorator = decorator;
		this.theBiomeDecorator.generateLakes = false;

		this.topBlock = Blocks.packed_ice;
		this.fillerBlock = Blocks.packed_ice;
		BiomeDictionary.registerBiomeType(this, BiomeDictionary.Type.COLD, BiomeDictionary.Type.WET, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.SPOOKY);
	}
}
