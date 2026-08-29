package com.hbm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hbm.blocks.ModBlocks;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class FurnaceGasEmission {

	private static final int MAX_OUTPUT_RANGE = 4;

	private static final ForgeDirection[] OUTPUT_DIRECTIONS = {
		ForgeDirection.UP,
		ForgeDirection.NORTH,
		ForgeDirection.SOUTH,
		ForgeDirection.WEST,
		ForgeDirection.EAST
	};

	private static final List<EmissionOffset> OUTPUT_OFFSETS = createOutputOffsets();

	private FurnaceGasEmission() { }

	/**
	 * Attempts to release carbon monoxide into an open block near a burning furnace or machine.
	 * Calls are intentionally probabilistic so gas accumulates slowly rather than every tick.
	 */
	public static void emitCarbonMonoxide(World world, int x, int y, int z, int chance) {
		if(world == null || world.isRemote || chance <= 0 || world.rand.nextInt(chance) != 0) return;

		int start = world.rand.nextInt(OUTPUT_DIRECTIONS.length);
		for(int i = 0; i < OUTPUT_DIRECTIONS.length; i++) {
			ForgeDirection direction = OUTPUT_DIRECTIONS[(start + i) % OUTPUT_DIRECTIONS.length];
			if(tryEmitCarbonMonoxide(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ)) return;
		}

		for(EmissionOffset offset : OUTPUT_OFFSETS) {
			if(tryEmitCarbonMonoxide(world, x + offset.x, y + offset.y, z + offset.z)) return;
		}
	}

	private static boolean tryEmitCarbonMonoxide(World world, int x, int y, int z) {
		if(!world.isAirBlock(x, y, z)) return false;
		world.setBlock(x, y, z, ModBlocks.gas_monoxide);
		return true;
	}

	private static List<EmissionOffset> createOutputOffsets() {
		List<EmissionOffset> offsets = new ArrayList<EmissionOffset>();

		for(int x = -MAX_OUTPUT_RANGE; x <= MAX_OUTPUT_RANGE; x++) {
			for(int y = -MAX_OUTPUT_RANGE; y <= MAX_OUTPUT_RANGE; y++) {
				for(int z = -MAX_OUTPUT_RANGE; z <= MAX_OUTPUT_RANGE; z++) {
					if(x == 0 && y == 0 && z == 0) continue;
					if(isPrimaryOutputDirection(x, y, z)) continue;
					offsets.add(new EmissionOffset(x, y, z));
				}
			}
		}

		Collections.sort(offsets, new Comparator<EmissionOffset>() {
			@Override
			public int compare(EmissionOffset first, EmissionOffset second) {
				return first.distanceSq - second.distanceSq;
			}
		});

		return offsets;
	}

	private static boolean isPrimaryOutputDirection(int x, int y, int z) {
		for(ForgeDirection direction : OUTPUT_DIRECTIONS) {
			if(x == direction.offsetX && y == direction.offsetY && z == direction.offsetZ) return true;
		}
		return false;
	}

	private static class EmissionOffset {
		private final int x;
		private final int y;
		private final int z;
		private final int distanceSq;

		private EmissionOffset(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.distanceSq = x * x + y * y + z * z;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof EmissionOffset)) return false;
			EmissionOffset offset = (EmissionOffset) obj;
			return x == offset.x && y == offset.y && z == offset.z;
		}

		@Override
		public int hashCode() {
			int result = x;
			result = 31 * result + y;
			result = 31 * result + z;
			return result;
		}
	}
}
