package com.hbm.dim.laythe;

import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.laythe.GenLayerLaythe.GenLayerDiversifyLaythe;
import com.hbm.dim.laythe.GenLayerLaythe.GenLayerLaytheBiomes;
import com.hbm.dim.laythe.GenLayerLaythe.GenLayerLaythePolar;

import com.hbm.entity.mob.EntityFRIEND;
import com.hbm.potion.HbmPotion;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraftforge.client.IRenderHandler;

import java.util.Random;

public class WorldProviderLaythe extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}


	private static final double RADIATION_MULTIPLIER_EUROPA = 14210.53;


	@Override
	public String getDimensionName() {
		return "Laythe";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderLaythe(this.worldObj, this.getSeed(), false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderLaytheSunset();
	}

	@Override
	public boolean hasLife() {
		return true;
	}

	@Override
	public int getWaterOpacity() {
		return 1;
	}

	@Override
	public boolean updateLightmap(int[] lightmap) {
		for(int i = 0; i < 256; i++) {
			float sun = getSunBrightness(1.0F);
			float sky = lightBrightnessTable[i / 16];
			float jool = Math.max(sky - sun, 0);

			int[] color = unpackColor(lightmap[i]);

			color[1] += jool * 60;
			if(color[1] > 255) color[1] = 255;

			lightmap[i] = packColor(color);
		}
		return true;
	}


	@Override
	public void updateWeather() {
		super.updateWeather();

		if (!worldObj.isRemote) {
			for (Object obj : worldObj.playerEntities) {
				if (obj instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) obj;

					if (worldObj.canBlockSeeTheSky((int) player.posX, (int) player.posY, (int) player.posZ)) {

						// For ultra-high radiation, just apply the effect every tick
						int baseDuration = 60; // 3 seconds


						// Scale the amplifier logarithmically so it's not instantly fatal
						//int amplifier = Math.min(220, (int)(Math.log10(RADIATION_MULTIPLIER_EUROPA)));
						//no enjoy your game should have worn a suit dumbass
						int amplifier = 220;


						//was 4 upping to 220, might try 127 if too much bc minecraft lim

						//we're gonna need a better suit.

						// Scale duration linearly to make it stack or persist
						//int duration = baseDuration * amplifier;
						int duration = 20;

						player.addPotionEffect(new PotionEffect(HbmPotion.radiation.id, duration, amplifier));
					}
				}
			}
		}
	}


	private static BiomeGenLayers createBiomeGenerators(long seed) {

		GenLayer biomes =
			new GenLayerLaytheBiomes(seed);

		// make biome regions larger
		biomes =
			GenLayerZoom.magnify(
				1000L,
				biomes,
				3);

		// add diversification
		biomes =
			new GenLayerDiversifyLaythe(
				2000L,
				biomes);

		// polar shaping
		biomes =
			new GenLayerLaythePolar(
				3000L,
				biomes);

		// soften edges
		biomes =
			new GenLayerSmooth(
				700L,
				biomes);

		GenLayer voronoi =
			new GenLayerVoronoiZoom(
				10L,
				biomes);

		return new BiomeGenLayers(
			biomes,
			voronoi,
			seed
		);
	}

}
