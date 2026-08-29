package com.hbm.saveddata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class BombSiteSavedData extends WorldSavedData {

	private static final String DATA_NAME = "hbmbombsites";

	public final List<BombSite> sites = new ArrayList<BombSite>();

	public BombSiteSavedData(String name) {
		super(name);
	}

	public BombSiteSavedData() {
		super(DATA_NAME);
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		sites.clear();

		NBTTagList list = nbt.getTagList("sites", 10);
		for(int i = 0; i < list.tagCount(); i++) {
			sites.add(BombSite.readFromNBT(list.getCompoundTagAt(i)));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();

		for(BombSite site : sites) {
			NBTTagCompound siteTag = new NBTTagCompound();
			site.writeToNBT(siteTag);
			list.appendTag(siteTag);
		}

		nbt.setTag("sites", list);
	}

	public boolean hasSiteAt(int x, int y, int z) {
		for(BombSite site : sites) {
			if(site.contains(x, y, z)) {
				return true;
			}
		}

		return false;
	}

	public boolean addSite(int x1, int y1, int z1, int x2, int y2, int z2) {
		BombSite site = new BombSite(x1, y1, z1, x2, y2, z2);

		for(BombSite existing : sites) {
			if(existing.equals(site)) {
				return false;
			}
		}

		sites.add(site);
		markDirty();
		return true;
	}

	public int removeSitesAt(int x, int y, int z) {
		int removed = 0;
		Iterator<BombSite> iterator = sites.iterator();

		while(iterator.hasNext()) {
			if(iterator.next().contains(x, y, z)) {
				iterator.remove();
				removed++;
			}
		}

		if(removed > 0) {
			markDirty();
		}

		return removed;
	}

	public static BombSiteSavedData getData(World world) {
		BombSiteSavedData data = (BombSiteSavedData) world.perWorldStorage.loadData(BombSiteSavedData.class, DATA_NAME);

		if(data == null) {
			world.perWorldStorage.setData(DATA_NAME, new BombSiteSavedData());
			data = (BombSiteSavedData) world.perWorldStorage.loadData(BombSiteSavedData.class, DATA_NAME);
		}

		return data;
	}

	public static boolean isBombSite(World world, int x, int y, int z) {
		return getData(world).hasSiteAt(x, y, z);
	}

	public static class BombSite {
		public final int minX;
		public final int minY;
		public final int minZ;
		public final int maxX;
		public final int maxY;
		public final int maxZ;

		public BombSite(int x1, int y1, int z1, int x2, int y2, int z2) {
			this.minX = Math.min(x1, x2);
			this.minY = Math.min(y1, y2);
			this.minZ = Math.min(z1, z2);
			this.maxX = Math.max(x1, x2);
			this.maxY = Math.max(y1, y2);
			this.maxZ = Math.max(z1, z2);
		}

		public boolean contains(int x, int y, int z) {
			return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
		}

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("minX", minX);
			nbt.setInteger("minY", minY);
			nbt.setInteger("minZ", minZ);
			nbt.setInteger("maxX", maxX);
			nbt.setInteger("maxY", maxY);
			nbt.setInteger("maxZ", maxZ);
		}

		public static BombSite readFromNBT(NBTTagCompound nbt) {
			return new BombSite(nbt.getInteger("minX"), nbt.getInteger("minY"), nbt.getInteger("minZ"), nbt.getInteger("maxX"), nbt.getInteger("maxY"), nbt.getInteger("maxZ"));
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof BombSite)) {
				return false;
			}

			BombSite site = (BombSite) obj;
			return minX == site.minX && minY == site.minY && minZ == site.minZ && maxX == site.maxX && maxY == site.maxY && maxZ == site.maxZ;
		}

		@Override
		public int hashCode() {
			int result = minX;
			result = 31 * result + minY;
			result = 31 * result + minZ;
			result = 31 * result + maxX;
			result = 31 * result + maxY;
			result = 31 * result + maxZ;
			return result;
		}
	}
}
