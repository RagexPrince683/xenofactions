package com.hbm.dim.neptune;

import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.neptune.GenLayerNeptune.GenLayerNeptuneBiomes;
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

public class WorldProviderNeptune extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {

		this.worldChunkMgr =
			new WorldChunkManagerCelestial(
				createNeptune(worldObj.getSeed())
			);
	}

	@Override
	public String getDimensionName() {
		return "Neptune";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderNeptune(
			this.worldObj,
			this.getSeed(),
			false
		);
	}

	@Override
	public void updateWeather() {

		// Neptune has no surface rain, but it does have persistent deep storms.
		this.worldObj.rainingStrength = 0.0F;
		this.worldObj.thunderingStrength = 0.7F;

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

			int amp;

			// exposed upper methane haze
			if(player.posY > 220) {

				amp = 12;
			}

			// cold cloud tops and charged storm bands
			else if(player.posY > 170) {

				amp = 6;
			}

			// deeper atmosphere provides more shielding
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

				if(player.posY <= 35) {

					player.attackEntityFrom(DamageSource.generic, 9.0F);
				}

				else if(player.posY <= 100) {

					player.attackEntityFrom(DamageSource.generic, 3.0F);
				}

				else if(player.posY <= 170) {

					player.attackEntityFrom(DamageSource.drown, 1.0F);
				}
			}
		}
	}

	private static WorldChunkManagerCelestial.BiomeGenLayers createNeptune(long seed) {

		// One planet-wide cold methane/hydrogen atmosphere biome.
		GenLayer genlayerBiomes =
			new GenLayerNeptuneBiomes(seed);

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
			0.06,
			0.18,
			0.48
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float celestialAngle, float partialTicks) {

		return Vec3.createVectorHelper(
			0.02,
			0.07,
			0.18
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float partialTicks) {

		// About thirty astronomical units from the Sun.
		return 0.002F;
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {

		// Neptune rotates in about 16.1 Earth hours.
		long dayLength =
			16100L;

		long time =
			worldTime % dayLength;

		return ((float)time + partialTicks)
			/ (float)dayLength - 0.25F;
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
