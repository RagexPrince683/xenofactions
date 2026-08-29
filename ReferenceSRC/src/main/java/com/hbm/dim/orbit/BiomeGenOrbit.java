package com.hbm.dim.orbit;

import java.util.Random;

import com.hbm.dim.BiomeGenBaseCelestial;

import com.hbm.entity.mob.EntityFRIEND;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenOrbit extends BiomeGenBaseCelestial {

	public BiomeGenOrbit(int id) {
		super(id);
		this.setBiomeName("Space");
		this.setDisableRain();
		this.monsters.add(new BiomeGenBase.SpawnListEntry(EntityFRIEND.class, 50, 1, 1));
		//why won't you spawn
	}

	//@Override
	//public float getSpawningChance() {
	//	return 8F;
	//}
	//not needed?

	@Override
	public void genTerrainBlocks(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		// NOTHING
	}

	public void decorate(World world, Random rand, int x, int z) {
		// EVEN LESS
	}

}
