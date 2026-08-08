package com.hfr.clowder;

/** Converts Xenofactions' stored territory coordinates back to world-space block bounds. */
public final class TerritoryCoordinateBounds {
	private TerritoryCoordinateBounds() { }

	/**
	 * Returns the half-open world interval containing every integer block coordinate
	 * which {@link ClowderTerritory#getCoordPair(int, int, int)} maps to {@code territoryCoordinate}.
	 * Stored territory coordinates are not vanilla chunks: the legacy lookup adds one
	 * before Java's truncating division. In particular, coordinate zero spans 31 blocks,
	 * so rendering {@code coordinate * 16} is not a safe approximation.
	 */
	public static Bounds forCoordinate(int territoryCoordinate) {
		long scaled = (long)territoryCoordinate * 16L;
		if(territoryCoordinate > 0) return new Bounds(scaled - 1L, scaled + 15L);
		if(territoryCoordinate < 0) return new Bounds(scaled - 16L, scaled);
		return new Bounds(-16L, 15L);
	}

	public static final class Bounds {
		public final long minInclusive;
		public final long maxExclusive;

		private Bounds(long minInclusive, long maxExclusive) {
			this.minInclusive = minInclusive;
			this.maxExclusive = maxExclusive;
		}

		public double center() { return (minInclusive + maxExclusive) / 2D; }
	}
}
