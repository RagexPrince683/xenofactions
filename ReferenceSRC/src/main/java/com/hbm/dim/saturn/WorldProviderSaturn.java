package com.hbm.dim.saturn;

import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.saturn.GenLayerSaturn.GenLayerSaturnBiomes;
import com.hbm.potion.HbmPotion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

public class WorldProviderSaturn extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {

		this.worldChunkMgr =
			new WorldChunkManagerCelestial(
				createGigaHell2(worldObj.getSeed())
			);
	}
	@Override
	public String getDimensionName() {
		return "Saturn";
	}
	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderSaturn(this.worldObj, this.getSeed(), false);
	}

	@Override
	public void updateWeather() {

		this.worldObj.rainingStrength = 0.0F;
		this.worldObj.thunderingStrength = 0.3F;

		this.worldObj
			.getWorldInfo()
			.setRaining(false);

		this.worldObj
			.getWorldInfo()
			.setThundering(true);

		if(worldObj.isRemote)
			return;

		for(Object obj :
			worldObj.playerEntities) {

			if(!(obj instanceof EntityPlayer))
				continue;

			EntityPlayer player =
				(EntityPlayer)obj;

			int amp = 0;

			// upper atmosphere
			if(player.posY > 220) {

				amp = 18;
			}

			// cloud deck
			else if(player.posY > 170) {

				amp = 7;
			}

			// dense atmosphere
			else if(player.posY > 100) {

				amp = 2;
			}

			else {

				amp = 0;
			}

			if(amp > 0 && player.ticksExisted % 20 == 0) {

				player.addPotionEffect(
					new PotionEffect(
						HbmPotion.radiation.id,
						40,
						amp
					)
				);
			}

			if(player.ticksExisted % 20 == 0) {

				if(player.posY <= 58) {

					player.attackEntityFrom(DamageSource.generic, 8.0F);
				}

				else if(player.posY <= 118) {

					player.attackEntityFrom(DamageSource.generic, 3.0F);
				}

				else if(player.posY <= 170) {

					player.attackEntityFrom(DamageSource.drown, 1.0F);
				}
			}
		}
	}

	private static WorldChunkManagerCelestial.BiomeGenLayers createGigaHell2(long seed) {

		GenLayer genlayerBiomes =
			new GenLayerSaturnBiomes(seed);

		GenLayerVoronoiZoom genlayerVoronoi =
			new GenLayerVoronoiZoom(
				10L,
				genlayerBiomes
			);

		return new WorldChunkManagerCelestial.BiomeGenLayers(
			genlayerBiomes,
			genlayerVoronoi,
			seed
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		return Vec3.createVectorHelper(
			0.93,
			0.86,
			0.68
		);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float celestialAngle, float partialTicks) {
		return Vec3.createVectorHelper(
			0.84,
			0.78,
			0.63
		);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float partialTicks) {
		return 0.014F;
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {

		long dayLength = 10700L;

		long time =
			worldTime % dayLength;

		return (
			(float)time + partialTicks
		) / (float)dayLength - 0.25F;
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


}
