package com.hfr.clowder;

import com.hfr.config.XFConfig;

public enum CityLevel {
	SETTLEMENT("Settlement", 2, 75F, 10F),
	TOWN("Town", 3, 150F, 25F),
	CITY("City", 4, 300F, 50F),
	METROPOLIS("Metropolis", 5, 600F, 90F),
	CAPITAL("Capital", 6, 1000F, 140F);

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

	public int level() {
		return ordinal() + 1;
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
