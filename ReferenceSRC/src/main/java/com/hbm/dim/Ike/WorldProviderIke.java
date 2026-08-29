package com.hbm.dim.Ike;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.WorldProviderCelestial;

import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.biome.*;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderIke extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(new BiomeGenIke(SpaceConfig.ikeBiome), dimensionId);
	}

	@Override
	public String getDimensionName() {
		return "Ike";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderIke(this.worldObj, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.ike_stone;
	}

	@Override
	public boolean updateLightmap(int[] lightmap) {
		for(int i = 0; i < 256; i++) {
			float sun = getSunBrightness(1.0F) - 0.1F;
			float sky = lightBrightnessTable[i / 16];
			float duna = Math.max(sky - sun, 0);

			int[] color = unpackColor(lightmap[i]);

			color[0] += duna * 20;
			if(color[0] > 255) color[0] = 255;

			lightmap[i] = packColor(color);
		}
		return true;
	}

	@Override
	public void updateWeather() {
		super.updateWeather();

		// Apply radiation effect to players on phobos 50% less than moon
		if (!worldObj.isRemote) {
			Random rand = new Random();

			for (Object obj : worldObj.playerEntities) {
				if (obj instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) obj;

					// Check if the player can see the sky
					if (worldObj.canBlockSeeTheSky((int) player.posX, (int) player.posY, (int) player.posZ)) {
						// Apply radiation effect with a random chance
						if (rand.nextInt(150) == 0) {
							player.addPotionEffect(new PotionEffect(HbmPotion.radiation.id, 20, 0));
						}
					}
				}
			}
		}
	}

	private static ArrayList<WeightedRandomFishable> plushie;

	private ArrayList<WeightedRandomFishable> getPlushie() {
		if(plushie == null) {
			plushie = new ArrayList<>();
			plushie.add(new WeightedRandomFishable(new ItemStack(Blocks.air, 1, 1), 100));
			//DIE
		}
//
		return plushie;
	}

	/// FISH ///
	public ArrayList<WeightedRandomFishable> getFish() {
		return getPlushie();
	}
//
	public ArrayList<WeightedRandomFishable> getJunk() {
		return getPlushie();
	}
//
	public ArrayList<WeightedRandomFishable> getTreasure() {
		return getPlushie();
	}

	//public ArrayList<WeightedRandomFishable> getFish() {
	//	return new ArrayList<>();
	//}
//
	//public ArrayList<WeightedRandomFishable> getJunk() {
	//	return new ArrayList<>();
	//}
//
	//public ArrayList<WeightedRandomFishable> getTreasure() {
	//	return new ArrayList<>();
	//}
	//forge 1.7.10 moment
	//actually probably a java moment

	//HAHAHA DUDE THESE ANIMALS ARE SO FUCKING FUNNY THEY MAKE ME WANT TO MERGE WITHOUT LOOKING

	/// FISH ///

}
