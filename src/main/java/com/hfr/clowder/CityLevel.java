package com.hfr.clowder;

public enum CityLevel {
	SETTLEMENT("Settlement", 2, 1F, 0.05F),
	TOWN("Town", 4, 2F, 0.10F),
	CITY("City", 6, 4F, 0.20F),
	METROPOLIS("Metropolis", 8, 7F, 0.35F),
	CAPITAL("Capital", 10, 11F, 0.50F);

	public final String displayName;
	public final int radius;
	public final float upgradeCost;
	public final float upkeep;

	CityLevel(String displayName, int radius, float upgradeCost, float upkeep) {
		this.displayName = displayName;
		this.radius = radius;
		this.upgradeCost = upgradeCost;
		this.upkeep = upkeep;
	}

	public CityLevel next() {
		int next = ordinal() + 1;
		return next < values().length ? values()[next] : null;
	}

	public static CityLevel byOrdinal(int ordinal) {
		if(ordinal < 0 || ordinal >= values().length)
			return SETTLEMENT;
		return values()[ordinal];
	}

	public static int maxRadius() {
		return CAPITAL.radius;
	}
}
