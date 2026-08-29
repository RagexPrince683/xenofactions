package com.hbm.dim.minmus.biome;

import com.hbm.blocks.ModBlocks;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenMinmusBasin extends BiomeGenBaseMinmus {

	//todo figure out where the skybox for this planet is, move it way way way further away from the solar system
	// , turn it into a alien hellscape

    public static final BiomeGenBase.Height height = new BiomeGenBase.Height(-1F, 0.02F);

	public BiomeGenMinmusBasin(int id) {
		super(id);
		this.setBiomeName("Minmus Basins");

        this.setHeight(height);
        this.topBlock = ModBlocks.minmus_smooth;
        this.fillerBlock = ModBlocks.minmus_regolith;
	}

}
