package com.hbm.dim.moho;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.moho.genlayer.GenLayerMohoBiomes;

import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

import java.util.Random;

public class WorldProviderMoho extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public void updateWeather() {
		super.updateWeather();

		// Apply radiation effect to players on mercury
		if (!worldObj.isRemote) {
			Random rand = new Random();

			//todo summon a really wholesome creature here bc it's such a NICE environment.

			for (Object obj : worldObj.playerEntities) {
				if (obj instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) obj;

					// Check if the player can see the sky
					if (worldObj.canBlockSeeTheSky((int) player.posX, (int) player.posY, (int) player.posZ)) {
						// Apply radiation effect with a random chance
						if (isDaytime()) {
							if (rand.nextInt(15) == 0) {
								player.addPotionEffect(new PotionEffect(HbmPotion.radiation.id, 20, 1));
							}
						} else {
							if (rand.nextInt(20) == 0) {
								player.addPotionEffect(new PotionEffect(HbmPotion.radiation.id, 20, 1));
							}
						}

					}
				}
			}
		}
	}

	@Override
	public String getDimensionName() {
		return "Moho";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderMoho(this.worldObj, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.moho_stone;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer biomes = new GenLayerMohoBiomes(seed);

		biomes = new GenLayerFuzzyZoom(2000L, biomes);
		biomes = new GenLayerZoom(2001L, biomes);
		// biomes = new GenLayerZoom(1000L, biomes);
		// biomes = new GenLayerZoom(1003L, biomes);
		biomes = new GenLayerSmooth(700L, biomes);
		biomes = new GenLayerZoom(1006L, biomes);

		GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

		return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
	}

}
