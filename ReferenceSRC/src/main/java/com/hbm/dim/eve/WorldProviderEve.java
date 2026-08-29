package com.hbm.dim.eve;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.eve.GenLayerEve.GenLayerEveBiomes;
import com.hbm.dim.eve.GenLayerEve.GenLayerEveRiverMix;

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

public class WorldProviderEve extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Eve";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderEve(this.worldObj, this.getSeed(), false);
	}


	private int chargetime;
	private float flashd;

	@Override
	public void updateWeather() {
		super.updateWeather();

		//no rads too THICC

		if(!worldObj.isRemote) {
			if (chargetime <= 0 || chargetime <= 800) {
				chargetime += 1;
			} else if (chargetime >= 800) {
				chargetime = 0;
			}
		} else {
			if (chargetime >= 800) {
				flashd = 0;
			} else if (chargetime >= 100) {
				if (flashd <= 1) {
					Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.rumble", 10F, 1F);
				}
				flashd += 0.1f;
				flashd = Math.min(100.0f, flashd + 0.1f * (100.0f - flashd) * 0.15f);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("chargetime", chargetime);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		chargetime = nbt.getInteger("chargetime");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(chargetime);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		chargetime = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		Vec3 base = super.getSkyColor(camera, partialTicks);

		// Compute flash factor from 0 → 1
		float flash = Math.min(1.0F, flashd / 100F);

		// Target bright yellow (you can tweak these to taste)
		double targetR = 1.0D;
		double targetG = 1.0D;
		double targetB = 0.0D;

		// Interpolate toward yellow: base*(1-flash) + target*flash
		double r = base.xCoord * (1.0D - flash) + targetR * flash;
		double g = base.yCoord * (1.0D - flash) + targetG * flash;
		double b = base.zCoord * (1.0D - flash) + targetB * flash;

		return Vec3.createVectorHelper(r, g, b);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float base = super.getSunBrightness(par1);
		float flash = Math.min(1.0F, flashd / 100F);

		// Brighten slightly more during flash
		return base + flash * 0.5F;
	}


	@Override
	public Block getStone() {
		return ModBlocks.eve_rock;
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

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer genlayerBiomes = new GenLayerEveBiomes(seed); // Your custom biome layer

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
		GenLayerEveRiverMix genlayerrivermix = new GenLayerEveRiverMix(100L, genlayersmooth1, genlayersmooth);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, genlayerrivermix);

		return new BiomeGenLayers(genlayerrivermix, genlayervoronoizoom, seed);
	}

}
