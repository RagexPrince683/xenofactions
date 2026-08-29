package com.hbm.explosion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

public class ExplosionNukeRayBatched {

	public HashMap<ChunkCoordIntPair, List<FloatTriplet>> perChunk = new HashMap(); //for future: optimize blockmap further by using sub-chunks instead of chunks
	public List<ChunkCoordIntPair> orderedChunks = new ArrayList();
	private CoordComparator comparator = new CoordComparator();
	int posX;
	int posY;
	int posZ;
	World world;

	int strength;
	int length;
	private boolean contained;
	private int surfaceY = 256;

	int gspNumMax;
	int gspNum;
	double gspX;
	double gspY;

	public boolean isAusf3Complete = false;

	public ExplosionNukeRayBatched(World world, int x, int y, int z, int strength, int speed, int length) {
		this(world, x, y, z, strength, speed, length, false, 256);
	}

	public ExplosionNukeRayBatched(World world, int x, int y, int z, int strength, int speed, int length, boolean contained, int surfaceY) {
		this.world = world;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.strength = strength;
		this.length = length;
		this.contained = contained;
		this.surfaceY = surfaceY;

		// Total number of points
		this.gspNumMax = (int)(2.5 * Math.PI * Math.pow(this.strength,2));
		this.gspNum = 1;

		// The beginning of the generalized spiral points
		this.gspX = Math.PI;
		this.gspY = 0.0;
	}

	private void generateGspUp(){
		if (this.gspNum < this.gspNumMax) {
			int k = this.gspNum + 1;
			double hk = -1.0 + 2.0 * (k - 1.0) / (this.gspNumMax - 1.0);
			this.gspX = Math.acos(hk);

			double prev_lon = this.gspY;
			double lon = prev_lon + 3.6 / Math.sqrt(this.gspNumMax) / Math.sqrt(1.0 - hk * hk);
			this.gspY = lon % (Math.PI * 2);
		} else {
			this.gspX = 0.0;
			this.gspY = 0.0;
		}
		this.gspNum++;
	}

	// Get Cartesian coordinates for spherical coordinates
	private Vec3 getSpherical2cartesian(){
		double dx = Math.sin(this.gspX) * Math.cos(this.gspY);
		double dz = Math.sin(this.gspX) * Math.sin(this.gspY);
		double dy = Math.cos(this.gspX);
		return Vec3.createVectorHelper(dx, dy, dz);
	}

	public void collectTip(int count) {

		//count = Math.min(count, 10);

		int amountProcessed = 0;

		while(this.gspNumMax >= this.gspNum){
			// Get Cartesian coordinates for spherical coordinates
			Vec3 vec = this.getSpherical2cartesian();

			int length = (int)Math.ceil(strength);
			float res = strength;

			FloatTriplet lastPos = null;
			HashSet<ChunkCoordIntPair> chunkCoords = new HashSet();

			for(int i = 0; i < length; i ++) {

				if(i > this.length)
					break;

				float x0 = (float) (posX + (vec.xCoord * i));
				float y0 = (float) (posY + (vec.yCoord * i));
				float z0 = (float) (posZ + (vec.zCoord * i));
				if(contained && y0 >= surfaceY) break;

				int iX = (int) Math.floor(x0);
				int iY = (int) Math.floor(y0);
				int iZ = (int) Math.floor(z0);

				double fac = 100 - ((double) i) / ((double) length) * 100;
				fac *= 0.07D;

				Block block = world.getBlock(iX, iY, iZ);

				if(!block.getMaterial().isLiquid())
					res -= Math.pow(masqueradeResistance(block), 7.5D - fac);
				//else
				//	res -= Math.pow(Blocks.air.getExplosionResistance(null), 7.5D - fac); // air is 0, might want to raise that is necessary

				if(res > 0 && block != Blocks.air && !block.getMaterial().isLiquid()) {
					//just chunk crap, should be fine but if it isn't I will add a fluid check.
					lastPos = new FloatTriplet(x0, y0, z0);
					//all-air chunks or water blocks don't need to be buffered at all
					ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(iX >> 4, iZ >> 4);
					chunkCoords.add(chunkPos);
				}

				if(res <= 0 || i + 1 >= this.length || i == length - 1) {
					break;
				}
			}

			for(ChunkCoordIntPair pos : chunkCoords) {
				List<FloatTriplet> triplets = perChunk.get(pos);

				if(triplets == null) {
					triplets = new ArrayList();
					perChunk.put(pos, triplets); //we re-use the same pos instead of using individualized per-chunk ones to save on RAM
				}

				triplets.add(lastPos);
			}

			// Raise one generalized spiral points
			this.generateGspUp();

			amountProcessed++;
			if(amountProcessed >= count) {
				return;
			}
		}

		orderedChunks.addAll(perChunk.keySet());
		orderedChunks.sort(comparator);

		isAusf3Complete = true;
	}


	/** Stores the incremental ray cursor and queued terrain work so reloads resume safely. */
	public void writeToNBT(NBTTagCompound tag) {
		tag.setInteger("gspNum", gspNum); tag.setDouble("gspX", gspX); tag.setDouble("gspY", gspY); tag.setBoolean("complete", isAusf3Complete); tag.setBoolean("contained", contained); tag.setInteger("surfaceY", surfaceY);
		NBTTagList chunks = new NBTTagList();
		for(ChunkCoordIntPair coord : perChunk.keySet()) {
			NBTTagCompound chunk = new NBTTagCompound(); chunk.setInteger("x", coord.chunkXPos); chunk.setInteger("z", coord.chunkZPos);
			NBTTagList points = new NBTTagList(); for(FloatTriplet point : perChunk.get(coord)) { NBTTagCompound p = new NBTTagCompound(); p.setFloat("x", point.xCoord); p.setFloat("y", point.yCoord); p.setFloat("z", point.zCoord); points.appendTag(p); } chunk.setTag("points", points); chunks.appendTag(chunk);
		}
		tag.setTag("chunks", chunks);
	}
	public void readFromNBT(NBTTagCompound tag) {
		gspNum = tag.getInteger("gspNum"); gspX = tag.getDouble("gspX"); gspY = tag.getDouble("gspY"); isAusf3Complete = tag.getBoolean("complete"); if(tag.hasKey("contained")) contained = tag.getBoolean("contained"); if(tag.hasKey("surfaceY")) surfaceY = tag.getInteger("surfaceY"); perChunk.clear(); orderedChunks.clear();
		NBTTagList chunks = tag.getTagList("chunks", 10);
		for(int i = 0; i < chunks.tagCount(); i++) { NBTTagCompound chunk = chunks.getCompoundTagAt(i); ChunkCoordIntPair coord = new ChunkCoordIntPair(chunk.getInteger("x"), chunk.getInteger("z")); List<FloatTriplet> points = new ArrayList<FloatTriplet>(); NBTTagList savedPoints = chunk.getTagList("points", 10); for(int j = 0; j < savedPoints.tagCount(); j++) { NBTTagCompound point = savedPoints.getCompoundTagAt(j); points.add(new FloatTriplet(point.getFloat("x"), point.getFloat("y"), point.getFloat("z"))); } perChunk.put(coord, points); }
		if(isAusf3Complete) { orderedChunks.addAll(perChunk.keySet()); orderedChunks.sort(comparator); }
	}

	public static float masqueradeResistance(Block block) {

		if(block == Blocks.sandstone) return Blocks.stone.getExplosionResistance(null);
		if(block == Blocks.obsidian) return Blocks.stone.getExplosionResistance(null) * 3;
		return block.getExplosionResistance(null);
	}

	/** little comparator for roughly sorting chunks by distance to the center */
	public class CoordComparator implements Comparator<ChunkCoordIntPair> {

		@Override
		public int compare(ChunkCoordIntPair o1, ChunkCoordIntPair o2) {

			int chunkX = ExplosionNukeRayBatched.this.posX >> 4;
			int chunkZ = ExplosionNukeRayBatched.this.posZ >> 4;

			int diff1 = Math.abs((chunkX - o1.chunkXPos)) + Math.abs((chunkZ - o1.chunkZPos));
			int diff2 = Math.abs((chunkX - o2.chunkXPos)) + Math.abs((chunkZ - o2.chunkZPos));

			return diff1 - diff2;
		}
	}

	public void processChunk() {

		if(this.perChunk.isEmpty()) return;

		ChunkCoordIntPair coord = orderedChunks.get(0);
		List<FloatTriplet> list = perChunk.get(coord);
		HashSet<BlockPos> toRem = new HashSet();
		HashSet<BlockPos> toRemTips = new HashSet();
		//List<BlockPos> toRem = new ArrayList();
		int chunkX = coord.chunkXPos;
		int chunkZ = coord.chunkZPos;

		int enter = (int) (Math.min(
				Math.abs(posX - (chunkX << 4)),
				Math.abs(posZ - (chunkZ << 4)))) - 16; //jump ahead to cut back on NOPs

		enter = Math.max(enter, 0);

		for(FloatTriplet triplet : list) {
			float x = triplet.xCoord;
			float y = triplet.yCoord;
			float z = triplet.zCoord;
			Vec3 vec = Vec3.createVectorHelper(x - this.posX, y - this.posY, z - this.posZ);
			double pX = vec.xCoord / vec.lengthVector();
			double pY = vec.yCoord / vec.lengthVector();
			double pZ = vec.zCoord / vec.lengthVector();

			int tipX = (int) Math.floor(x);
			int tipY = (int) Math.floor(y);
			int tipZ = (int) Math.floor(z);

			boolean inChunk = false;
			for(int i = enter; i < vec.lengthVector(); i++) {
				int x0 = (int) Math.floor(posX + pX * i);
				int y0 = (int) Math.floor(posY + pY * i);
				int z0 = (int) Math.floor(posZ + pZ * i);

				if(x0 >> 4 != chunkX || z0 >> 4 != chunkZ) {
					if(inChunk) {
						break;
					} else {
						continue;
					}
				}

				inChunk = true;

				// skip air and liquids — treat liquids like air
				Block blockHere = world.getBlock(x0, y0, z0);
				if (!world.isAirBlock(x0, y0, z0) && !blockHere.getMaterial().isLiquid()) {
					BlockPos pos = new BlockPos(x0, y0, z0);

					if (x0 == tipX && y0 == tipY && z0 == tipZ) {
						toRemTips.add(pos);
					}
					toRem.add(pos);
				}
			}
		}

		for(BlockPos pos : toRem) {
			if(toRemTips.contains(pos)) {
				this.handleTip(pos.getX(), pos.getY(), pos.getZ());
			} else {
				world.setBlock(pos.getX(), pos.getY(), pos.getZ(), Blocks.air, 0, 2);
			}
		}

		perChunk.remove(coord);
		orderedChunks.remove(0);
	}

	protected void handleTip(int x, int y, int z) {
		world.setBlock(x, y, z, Blocks.air, 0, 3);
	}

	public class FloatTriplet {
		public float xCoord;
		public float yCoord;
		public float zCoord;

		public FloatTriplet(float x, float y, float z) {
			xCoord = x;
			yCoord = y;
			zCoord = z;
		}
	}
}
