package com.hfr.clowder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.TerritoryCoordinateBounds.Bounds;

public class TerritoryCoordinateBoundsTest {
	private static final int[] BOUNDARY_BLOCKS = {
		-33, -32, -31, -17, -16, -15, -1, 0, 1, 14, 15, 16, 17, 31, 32, 33
	};

	@Test
	public void inverseBoundsMatchLegacyLookupAroundEveryBoundaryOnBothAxes() {
		for(int block : BOUNDARY_BLOCKS) {
			CoordPair pair = ClowderTerritory.getCoordPair(7, block, block);
			assertContainsExactly(pair.x, true);
			assertContainsExactly(pair.z, false);
		}
	}

	@Test
	public void zeroAndAdjacentCoordinatesAreConnectedWithoutOverlap() {
		assertBounds(-2, -48, -32);
		assertBounds(-1, -32, -16);
		assertBounds(0, -16, 15);
		assertBounds(1, 15, 31);
		assertBounds(2, 31, 47);
	}

	@Test
	public void positiveNegativeAndOriginCityCentersRemainInsideTheirTerritories() {
		int[][] cities = { { 160, 160 }, { -160, 160 }, { 160, -160 }, { -160, -160 }, { -1, 0 }, { 0, 1 } };
		for(int[] city : cities) {
			CoordPair pair = ClowderTerritory.getCoordPair(0, city[0], city[1]);
			assertIncluded(pair.x, city[0]);
			assertIncluded(pair.z, city[1]);
		}
	}

	private static void assertContainsExactly(int coordinate, boolean xAxis) {
		Bounds bounds = TerritoryCoordinateBounds.forCoordinate(coordinate);
		for(long block = bounds.minInclusive; block < bounds.maxExclusive; block++) {
			CoordPair resolved = xAxis ? ClowderTerritory.getCoordPair(7, (int)block, 0) : ClowderTerritory.getCoordPair(7, 0, (int)block);
			assertEquals(coordinate, xAxis ? resolved.x : resolved.z);
		}
		CoordPair below = xAxis ? ClowderTerritory.getCoordPair(7, (int)bounds.minInclusive - 1, 0) : ClowderTerritory.getCoordPair(7, 0, (int)bounds.minInclusive - 1);
		CoordPair above = xAxis ? ClowderTerritory.getCoordPair(7, (int)bounds.maxExclusive, 0) : ClowderTerritory.getCoordPair(7, 0, (int)bounds.maxExclusive);
		assertNotEquals(coordinate, xAxis ? below.x : below.z);
		assertNotEquals(coordinate, xAxis ? above.x : above.z);
	}

	private static void assertIncluded(int coordinate, int block) {
		Bounds bounds = TerritoryCoordinateBounds.forCoordinate(coordinate);
		assertEquals(true, block >= bounds.minInclusive && block < bounds.maxExclusive);
	}

	private static void assertBounds(int coordinate, long min, long max) {
		Bounds bounds = TerritoryCoordinateBounds.forCoordinate(coordinate);
		assertEquals(min, bounds.minInclusive);
		assertEquals(max, bounds.maxExclusive);
	}
}
