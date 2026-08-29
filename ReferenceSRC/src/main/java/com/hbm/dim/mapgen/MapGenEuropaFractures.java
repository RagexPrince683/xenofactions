package com.hbm.dim.mapgen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenRavine;

public class MapGenEuropaFractures extends MapGenRavine {

	public Block crackBlock =
		Blocks.packed_ice;

	public MapGenEuropaFractures() {

		this.range = 8;
	}

	@Override
	protected void func_151540_a(
		long seed,
		int chunkX,
		int chunkZ,
		Block[] blocks,
		double x,
		double y,
		double z,
		float width,
		float yaw,
		float pitch,
		int startStep,
		int endStep,
		double verticalScale) {

		/*
		 * Vanilla ravines work.
		 * Just widen them.
		 */

		width *= 2.2F;

		//TODO test
		//pitch *= 0.35F;
		//verticalScale *= 0.7D;

		super.func_151540_a(
			seed,
			chunkX,
			chunkZ,
			blocks,
			x,
			y,
			z,
			width,
			yaw,
			pitch,
			startStep,
			endStep,
			verticalScale
		);
	}

	@Override
	protected void func_151538_a(
		World world,
		int originChunkX,
		int originChunkZ,
		int chunkX,
		int chunkZ,
		Block[] blocks) {

		/*
		 * Stable regional fracture belts
		 *
		 * Europa-style bands where
		 * ravines become extremely common.
		 */
		double fractureField =
			Math.sin(originChunkX * 0.025D)
				+ Math.cos(originChunkZ * 0.025D);

		/*
		 * Vanilla rarity = 1/50
		 */
		int chance = 50;

		/*
		 * Fracture zone
		 */
		if(fractureField > 0.4D) {

			chance = 14;
		}

		/*
		 * Dense fracture core
		 */
		if(fractureField > 1.0D) {

			chance = 5;
		}

		if(this.rand.nextInt(chance) != 0)
			return;

		double x =
			(originChunkX * 16)
				+ this.rand.nextInt(16);

		double y =
			this.rand.nextInt(
				this.rand.nextInt(40) + 8
			) + 20;

		double z =
			(originChunkZ * 16)
				+ this.rand.nextInt(16);

		/*
		 * Multiple overlapping ravines
		 * in fracture regions.
		 */
		int ravines = 1;

		if(fractureField > 0.4D)
			ravines = 3;

		if(fractureField > 1.0D)
			ravines = 6;

		float baseYaw =
			this.rand.nextFloat()
				* (float)Math.PI * 2F;

		for(int i = 0; i < ravines; i++) {

			float yaw =
				baseYaw
					+ (this.rand.nextFloat() - 0.5F)
					* 0.45F;

			float pitch =
				(this.rand.nextFloat() - 0.5F)
					* 0.12F;

			float width =
				(this.rand.nextFloat()
					* 2.0F
					+ this.rand.nextFloat())
					* 2.0F;

			func_151540_a(
				this.rand.nextLong(),
				chunkX,
				chunkZ,
				blocks,
				x + this.rand.nextInt(12) - 6,
				y,
				z + this.rand.nextInt(12) - 6,
				width,
				yaw,
				pitch,
				0,
				0,
				3.0D
			);
		}
	}

}
