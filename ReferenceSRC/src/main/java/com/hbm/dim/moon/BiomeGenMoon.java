package com.hbm.dim.moon;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeGenBaseCelestial;

import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenMoon extends BiomeGenBaseCelestial {

	public static final BiomeGenBase.Height height = new BiomeGenBase.Height(0.125F, 0.05F);

	public BiomeGenMoon(int id) {
		super(id);
		this.setBiomeName("Mun");
		this.setDisableRain();

        //this.creatures.add(new BiomeGenBase.SpawnListEntry(EntityMoonCow.class, 10, 4, 4));
		//Now I miss her, she is still with me,
		// but I can only see her sometimes and this ship is very hard to control.
		// I'm a bad driver, I can crash if the way is too much for me,
		// I can lose her if I can't continue and it's so hard to continue,
		// I can lose all. I don't want to crash, I don't want to lose,
		// but it's so dark out there and I can't see the future,
		// I'm afraid to look and realize she is not there anymore

		this.theBiomeDecorator.generateLakes = false;

		this.setHeight(height);

		this.topBlock = ModBlocks.moon_turf;
		this.fillerBlock = ModBlocks.moon_rock;
	}

	@Override
	public float getSpawningChance() {
        return 0.008F;
    }





	@Override
	public void genTerrainBlocks(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		// boolean flag = true;
		Block block = this.topBlock;
		byte b0 = (byte) (this.field_150604_aj & 255);
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int i1 = x & 15;
		int j1 = z & 15;
		int k1 = blocks.length / 256;

		for (int l1 = 255; l1 >= 0; --l1) {
			int i2 = (j1 * 16 + i1) * k1 + l1;

			if (l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.bedrock;
			} else {
				Block block2 = blocks[i2];

				if (block2 != null && block2.getMaterial() != Material.air) {
					if (block2 == ModBlocks.moon_rock) {
						if (k == -1) {
							if (l <= 0) {
								block = null;
								b0 = 0;
								block1 = ModBlocks.moon_rock;
							} else if (l1 >= 59 && l1 <= 64) {
								block = this.topBlock;
								b0 = (byte) (this.field_150604_aj & 255);
								block1 = this.fillerBlock;
							}

							if (l1 < 63 && (block == null || block.getMaterial() == Material.air)) {
								if (this.getFloatTemperature(x, l1, z) < 0.15F) {
									block = this.topBlock;
									b0 = 0;
								} else {
									block = this.topBlock;
									b0 = 0;
								}
							}

							k = l;

							if (l1 >= 62) {
								blocks[i2] = block;
								meta[i2] = b0;
							} else if (l1 < 56 - l) {
								block = null;
								block1 = ModBlocks.moon_rock;
								blocks[i2] = Blocks.gravel;
							} else {
								blocks[i2] = block1;
							}
						} else if (k > 0) {
							--k;
							blocks[i2] = block1;

							if (k == 0 && block1 == Blocks.sand) {
								k = rand.nextInt(4) + Math.max(0, l1 - 63);
								block1 = Blocks.sandstone;
							}
						}
					}
				} else {
					k = -1;
				}
			}
		}
	}

}
