package com.hbm.dim.sun;

import java.util.ArrayList;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;

import com.hbm.dim.moho.genlayer.GenLayerMohoBiomes;
import com.hbm.dim.sun.GenLayerSun.GenLayerSunBiomes;
import com.hbm.potion.HbmPotion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.*;

public class WorldProviderSun extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {

		this.worldChunkMgr =
			new WorldChunkManagerCelestial(
				createHell(worldObj.getSeed())
			);
	}

	@Override
	public String getDimensionName() {
		return "Sun";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderSun(this.worldObj, this.getSeed(), false);
	}

	@Override
	public void updateWeather() {

		this.worldObj.rainingStrength = 0.0F;
		this.worldObj.thunderingStrength = 0.0F;
		this.worldObj.getWorldInfo().setRaining(false);
		this.worldObj.getWorldInfo().setThundering(false);

		if(!worldObj.isRemote) {
			//the key idea is you shouldn't come here

			for(Object obj : worldObj.playerEntities) {

				if(obj instanceof EntityPlayer) {

					EntityPlayer player =
						(EntityPlayer)obj;

					// permanent burning
					player.setFire(200);

					// if exposed to the sky:
					if(worldObj.canBlockSeeTheSky(
						(int)player.posX,
						(int)player.posY,
						(int)player.posZ)) {

						// background radiation, the real danger is literally just exposure
						player.addPotionEffect(
							new PotionEffect(
								HbmPotion.radiation.id,
								40,
								1
							)
						);

						// heatstroke
						player.addPotionEffect(
							new PotionEffect(
								Potion.hunger.id,
								60,
								200
							)
						);

						player.addPotionEffect(
							new PotionEffect(
								Potion.weakness.id,
								40,
								100
							)
						);

						//mining fatigue
						player.addPotionEffect(
							new PotionEffect(
								Potion.digSlowdown.id,
								40,
								100
							)
						);
						//slowness from being literally smelted alive
						player.addPotionEffect(
							new PotionEffect(
								Potion.moveSlowdown.id,
								40,
								100
							)
						);
						// blindness
						player.addPotionEffect(
							new PotionEffect(
								Potion.blindness.id,
								80,
								100
							)
						);
					}
				}
			}
		}
	}

	private static BiomeGenLayers createHell(long seed) {

		// one biome everywhere
		GenLayer genlayerBiomes =
			new GenLayerSunBiomes(seed);

		// vanilla smoothing for biome borders
		GenLayerVoronoiZoom genlayerVoronoi =
			new GenLayerVoronoiZoom(
				10L,
				genlayerBiomes
			);

		return new BiomeGenLayers(
			genlayerBiomes,
			genlayerVoronoi,
			seed
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {

		// violent white/yellow glow
		return Vec3.createVectorHelper(
			2.5D,
			1.8D,
			0.4D
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float celestialAngle, float partialTicks) {

		// burning plasma haze
		return Vec3.createVectorHelper(
			2.0D,
			0.8D,
			0.1D
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float partialTicks) {

		// retina destroyer
		return 100.0F;
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {

		// no day/night cycle
		return 0.0F;
	}

	@Override
	public boolean isSurfaceWorld() {
		return false;
	}

	@Override
	public boolean canRespawnHere() {
		return false;
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z) {
		return false;
	}

	@Override
	public Block getStone() {

		// fallback terrain block
		return Blocks.lava;
	}

	@Override
	public double getMovementFactor() {
		return 1.0D;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean doesXZShowFog(int x, int z) {

		// always foggy / blinding
		return true;
	}

	private static ArrayList<WeightedRandomFishable> death;

	private ArrayList<WeightedRandomFishable> getDeath() {

		if(death == null) {
			death = new ArrayList<>();

			// you caught: nothing
			death.add(new WeightedRandomFishable(
				new ItemStack(Blocks.fire),
				100
			));
		}

		return death;
	}

	/// FISH ///
	@Override
	public ArrayList<WeightedRandomFishable> getFish() {
		return getDeath();
	}

	@Override
	public ArrayList<WeightedRandomFishable> getJunk() {
		return getDeath();
	}

	@Override
	public ArrayList<WeightedRandomFishable> getTreasure() {
		return getDeath();
	}
}
