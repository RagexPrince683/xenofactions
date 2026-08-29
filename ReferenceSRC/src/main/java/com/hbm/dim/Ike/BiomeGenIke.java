package com.hbm.dim.Ike;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenIke extends BiomeGenBaseCelestial {

	public static final Height height =
		new Height(0.0F, 0.0F);

	public BiomeGenIke(int id) {

		super(id);

		this.setBiomeName("Ike");
		this.setDisableRain();

		BiomeDecoratorCelestial decorator =
			new BiomeDecoratorCelestial(
				ModBlocks.ike_stone);

		// No lakes on Phobos
		decorator.lakeChancePerChunk = 0;
		this.theBiomeDecorator = decorator;
		this.theBiomeDecorator.generateLakes = false;

		this.setHeight(height);

		this.topBlock = ModBlocks.ike_regolith;
		this.fillerBlock = ModBlocks.ike_regolith;
	}

	@Override
	public void genTerrainBlocks(
		World world,
		Random rand,
		Block[] blocks,
		byte[] meta,
		int x,
		int z,
		double noise) {

		int localX = x & 15;
		int localZ = z & 15;
		int height = blocks.length / 256;

		for(int y = 255; y >= 0; y--) {

			int index =
				(localZ * 16 + localX)
					* height + y;

			Block current = blocks[index];

			if(current == null ||
				current.getMaterial()
					== Material.air) {
				continue;
			}

			if(current == ModBlocks.ike_stone) {

				// realistic varying regolith depth
				int regolithDepth =
					2 + rand.nextInt(5);

				for(int d = 0;
					d < regolithDepth;
					d++) {

					int replace =
						index - d;

					if(replace < 0)
						break;

					if(blocks[replace]
						== ModBlocks.ike_stone) {

						blocks[replace] =
							ModBlocks
								.ike_regolith;
					}
				}

				break; // only modify top surface
			}
		}
	}
}
