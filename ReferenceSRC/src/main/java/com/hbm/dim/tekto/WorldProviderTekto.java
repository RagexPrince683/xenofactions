package com.hbm.dim.tekto;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.eve.GenLayerEve.GenLayerEveBiomes;
import com.hbm.dim.eve.GenLayerEve.GenLayerEveRiverMix;
import com.hbm.dim.tekto.GenLayerTekto.GenLayerTektoRiverMix;
import com.hbm.dim.tekto.GenLayerTekto.GenlayerTektoBiomes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerRiver;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

import java.util.ArrayList;

public class WorldProviderTekto extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Tekto";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderTekto(this.worldObj, this.getSeed(), false);
	}


	@Override
	public void updateWeather() {
		super.updateWeather();
	}


	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		Vec3 ohshit = super.getSkyColor(camera, partialTicks);

		return Vec3.createVectorHelper(ohshit.xCoord , ohshit.yCoord, ohshit.zCoord);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float imsuper = super.getSunBrightness(par1);

		return imsuper * 0.7F;
	}

	@Override
	public Block getStone() {
		return ModBlocks.basalt; //temp
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer genlayerBiomes = new GenlayerTektoBiomes(seed); // Your custom biome layer

		genlayerBiomes = new GenLayerZoom(1000L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1001L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1002L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1003L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1004L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1005L, genlayerBiomes);

		GenLayer genlayerRiverZoom = new GenLayerZoom(1000L, genlayerBiomes);
		GenLayer genlayerRiver = new GenLayerRiver(1001L, genlayerRiverZoom); // Your custom river layer
		GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerRiver);

		GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerBiomes);
		GenLayerTektoRiverMix genlayerrivermix = new GenLayerTektoRiverMix(100L, genlayersmooth1, genlayersmooth);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, genlayerrivermix);

		return new BiomeGenLayers(genlayerrivermix, genlayervoronoizoom, seed);
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



}
