package com.hbm.dim;

public final class GasGiantChunkUtil {

	private GasGiantChunkUtil() { }

	public static int hash(int x, int y, int z) {
		int h = x * 73428767;
		h ^= y * 912271;
		h ^= z * 42317861;
		h ^= h >>> 13;
		h *= 1274126177;
		return h ^ (h >>> 16);
	}

	public static boolean chance(int x, int y, int z, int chance) {
		return (hash(x, y, z) & Integer.MAX_VALUE) % chance == 0;
	}

	public static double signed(int x, int y, int z) {
		return ((hash(x, y, z) >>> 8) & 65535) / 32767.5D - 1.0D;
	}

	public static int range(int x, int y, int z, int min, int range) {
		return min + (hash(x, y, z) & Integer.MAX_VALUE) % range;
	}
}
