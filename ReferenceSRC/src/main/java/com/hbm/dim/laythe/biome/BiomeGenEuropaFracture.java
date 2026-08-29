package com.hbm.dim.laythe.biome;

import net.minecraft.init.Blocks;

public class BiomeGenEuropaFracture extends BiomeGenBaseLaythe {

	public static final Height height =
		new Height(-0.02F, 0.015F);

	public BiomeGenEuropaFracture(int id) {
		super(id);

		this.setBiomeName("Fracture Zone");
		this.setHeight(height);

		this.topBlock = Blocks.packed_ice;
		this.fillerBlock = Blocks.packed_ice;
	}
}
