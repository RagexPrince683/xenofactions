package com.hbm.dim.laythe.biome;

import com.hbm.blocks.ModBlocks;
import net.minecraft.init.Blocks;

public class BiomeGenEuropaChaos extends BiomeGenBaseLaythe {

	public static final Height height =
		new Height(0.02F, 0.04F);

	public BiomeGenEuropaChaos(int id) {
		super(id);

		this.setBiomeName("Chaos Terrain");
		this.setHeight(height);

		this.topBlock = ModBlocks.laythe_silt;
		this.fillerBlock = Blocks.packed_ice;
	}
}
