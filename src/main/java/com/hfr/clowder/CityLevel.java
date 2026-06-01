package com.hfr.clowder;

public enum CityLevel {
	SETTLEMENT("Settlement", 1, 75F, 10F),
	TOWN("Town", 2, 150F, 25F),
	CITY("City", 3, 300F, 50F),
	METROPOLIS("Metropolis", 4, 600F, 90F),
	CAPITAL("Capital", 5, 1000F, 140F);

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
