package com.hbm.explosion;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class NukeEnvironmentalEffect {

	static Random rand = new Random();

	/**
	 * Area of effect radiation effect. j > 0 for jagged edges of the spherical area. Args: world, x, y, z, radius, outer radius with random chance.
	 */
	@Deprecated //does not use scorched uranium, implementation is garbage anyway
	public static void applyStandardAOE(World world, int x, int y, int z, int r, int j) {

		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22 + rand.nextInt(j)) {
						applyStandardEffect(world, X, Y, Z);
					}
				}
			}
		}
	}

	public static void applyStandardEffect(World world, int x, int y, int z) {

		int chance = 100;
		Block b = null;
		int meta = 0;

		Block in = world.getBlock(x, y, z);
		int inMeta = world.getBlockMetadata(x, y, z);

		if(in == Blocks.air)
			return;

		// Sand -> Trinitite
		if(in == Blocks.sand) {

			if(inMeta == 1)
				b = ModBlocks.waste_trinitite_red;
			else
				b = ModBlocks.waste_trinitite;

			chance = 250;

			// Grass / dirt contamination
		} else if(in == Blocks.grass || in == Blocks.dirt) {

			b = ModBlocks.waste_earth;
			chance = 400;

			// Mycelium death
		} else if(in == Blocks.mycelium) {

			b = ModBlocks.waste_mycelium;
			chance = 500;

			// Trees carbonize
		} else if(in == Blocks.log || in == Blocks.log2) {

			b = ModBlocks.waste_log;
			chance = 600;

			// Wooden structures char/burn
		} else if(in == Blocks.planks) {

			b = ModBlocks.waste_planks;
			chance = 700;

			// Clay hardens from heat
		} else if(in == Blocks.clay) {

			b = Blocks.hardened_clay;
			chance = 500;

			// Stone scorches/cracks
		} else if(
			in == Blocks.stone ||
				in == Blocks.cobblestone ||
				in == Blocks.mossy_cobblestone
		) {

			b = ModBlocks.scorched_stone;
			chance = 200;

			// Ores become irradiated/scorched
		} else if(
			in == Blocks.coal_ore ||
				in == Blocks.iron_ore ||
				in == Blocks.gold_ore ||
				in == Blocks.redstone_ore ||
				in == Blocks.lapis_ore ||
				in == Blocks.emerald_ore ||
				in == Blocks.diamond_ore ||
				in == ModBlocks.ore_uranium ||
				in == ModBlocks.ore_plutonium
		) {

			b = ModBlocks.ore_uranium_scorched;
			chance = 50;

			// Mushroom caps burn away
		} else if(
			(in == Blocks.brown_mushroom_block && inMeta == 10) ||
				(in == Blocks.red_mushroom_block && inMeta == 10)
		) {

			b = Blocks.air;
			chance = 800;

			// General flammables ignite
		} else if(in.getMaterial().getCanBurn()) {

			b = Blocks.fire;
			chance = 850;
		}

		if(b != null && rand.nextInt(1000) < chance)
			world.setBlock(x, y, z, b, meta, 2);
	}

}
