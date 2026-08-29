package com.hbm.dim.laythe.biome;

import net.minecraft.init.Blocks;

public class BiomeGenEuropaPlains extends BiomeGenBaseLaythe {

	public static final Height height =
		new Height(-0.05F, 0.008F);

	public BiomeGenEuropaPlains(int id) {
		super(id);

		this.setBiomeName("Europa Plains");
		this.setHeight(height);

		this.topBlock = Blocks.packed_ice;
		this.fillerBlock = Blocks.packed_ice;
	}
}
