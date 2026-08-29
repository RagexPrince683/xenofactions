package com.hbm.world.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.hbm.dim.WorldProviderCelestial;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.WorldEvent;

public class OreLayer3D {

	private static int counter = 0;
	private final int id;

	long lastSeed;
	NoiseGeneratorPerlin noiseX;
	NoiseGeneratorPerlin noiseY;
	NoiseGeneratorPerlin noiseZ;
	double[][] cacheX;
	double[][] cacheZ;

	double scaleH;
	double scaleV;
	double threshold;

	Block block;
	int meta;
	int dim = 0;
	boolean allCelestials = false;

	Map<Integer, Set<ChunkCoordIntPair>> alreadyDecorated = new HashMap<Integer, Set<ChunkCoordIntPair>>();

	public OreLayer3D(Block block, int meta) {
		this.block = block;
		this.meta = meta;
		MinecraftForge.EVENT_BUS.register(this);
		this.id = counter++;
	}

	public OreLayer3D setDimension(int dim) {
		this.dim = dim;
		return this;
	}

	// If enabled, this vein will spawn on all celestial bodies
	public OreLayer3D setGlobal(boolean value) {
		this.allCelestials = value;
		return this;
	}

	public OreLayer3D setScaleH(double scale) {
		this.scaleH = scale;
		return this;
	}

	public OreLayer3D setScaleV(double scale) {
		this.scaleV = scale;
		return this;
	}

	public OreLayer3D setThreshold(double threshold) {
		this.threshold = threshold;
		return this;
	}

	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {

		World world = event.world;

		if(world.provider == null) return;

		Block replace = Blocks.stone;
		if(world.provider instanceof WorldProviderCelestial) {
			replace = ((WorldProviderCelestial)world.provider).getStone();
		}

		int cX = event.chunkX;
		int cZ = event.chunkZ;

		if(allCelestials) {
			if(!(world.provider instanceof WorldProviderCelestial) && world.provider.dimensionId != 0) return;
		} else {
			if(world.provider.dimensionId != this.dim) return;
		}

		ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(cX, cZ);
		Set<ChunkCoordIntPair> decoratedChunks = alreadyDecorated.get(world.provider.dimensionId);
		if(decoratedChunks == null) {
			decoratedChunks = new HashSet<ChunkCoordIntPair>();
			alreadyDecorated.put(world.provider.dimensionId, decoratedChunks);
		}
		if(decoratedChunks.contains(chunkPos)) return;
		decoratedChunks.add(chunkPos);

		if(this.noiseX == null || world.getSeed() != this.lastSeed) {
			this.noiseX = new NoiseGeneratorPerlin(new Random(world.getSeed() + 101 + this.id), 4);
			this.noiseY = new NoiseGeneratorPerlin(new Random(world.getSeed() + 102 + this.id), 4);
			this.noiseZ = new NoiseGeneratorPerlin(new Random(world.getSeed() + 103 + this.id), 4);
			this.cacheX = new double[16][65];
			this.cacheZ = new double[16][65];
			this.lastSeed = world.getSeed();
		}

		for(int o = 0; o < 16; o++) {
			for(int y = 64; y > 5; y--) {
				this.cacheX[o][y] = this.noiseX.func_151601_a(y * scaleV, (cZ + 8 + o) * scaleH);
				this.cacheZ[o][y] = this.noiseZ.func_151601_a((cX + 8 + o) * scaleH, y * scaleV);
			}
		}

		for(int ox = 0; ox < 16; ox++) {
			int x = cX + 8 + ox;
			for(int oz = 0; oz < 16; oz++) {
				int z = cZ + 8 + oz;
				double nY = this.noiseY.func_151601_a(x * scaleH, z * scaleH);
				for(int y = 64; y > 5; y--) {
					double nX = this.cacheX[oz][y];
					double nZ = this.cacheZ[ox][y];

					if(nX * nY * nZ > threshold) {
						Block target = world.getBlock(x, y, z);

						if(target.isNormalCube() && target.isReplaceableOreGen(world, x, y, z, replace)) {
							world.setBlock(x, y, z, block, meta, 2);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(event.world.provider != null) this.alreadyDecorated.put(event.world.provider.dimensionId, new HashSet<ChunkCoordIntPair>());
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(event.world.provider != null) this.alreadyDecorated.remove(event.world.provider.dimensionId);
	}
}
