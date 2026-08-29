package com.hbm.util;

import com.hbm.dim.CelestialBody;

public class AstronomyUtil {

	// Compress the solar system into Minecraft space
	public static final double ORBIT_SCALE = 0.01D;

	// the G in G*M1*M2/r
	public static final double GRAVITATIONAL_CONSTANT = 6.67430e-11;
	// Default orbital altitude, added onto planet radius to get intended orbital radius
	public static final float DEFAULT_ALTITUDE_KM = 100;
	public static final long TICKS_PER_SECOND = 20;
	public static double TIME_SCALE = 1.0 / 30.0;

	public static final long SECONDS_IN_MC_DAY = 20 * 60; // 1200 sec
	public static final long TICKS_IN_MC_DAY = SECONDS_IN_MC_DAY * TICKS_PER_SECOND;
	// How many seconds in a MC day
	public static final long SECONDS_IN_DAY = 20 * 60;
	//public static final long SECONDS_IN_KSP_DAY = 6 * 60 * 60; replaced with SECONDS_IN_MC_DAY
	public static final long TICKS_IN_DAY = SECONDS_IN_DAY * 20;

	// Day length in KSP -> day length in MC
	// This conversion will make orbital mechanics run a considerable fraction faster than normal
	//public static final double DAY_FACTOR = (double)SECONDS_IN_DAY / (double)SECONDS_IN_MC_DAY;
	//unused

	// Default for how fast the player character accelerates downwards due to gravity in m/s/s
	public static final float STANDARD_GRAVITY = 1.6F; // 0.08 per tick
	public static final double EARTH_GRAVITY = 9.81;
	public static final double MC_GRAVITY = 0.08 * 20; // per second equivalent
	public static final double PLAYER_GRAVITY_MODIFIER =
		MC_GRAVITY / EARTH_GRAVITY;

	public static final double KM_IN_AU = 149_597_870.7;

	// How quickly time moves, for testing celestial mechanics
	public static final long TIME_MULTIPLIER = 1;

	// Conversion rate from millibuckets to atmospheres
	// 1 atmosphere is 1 gigabucket
	public static final double MB_PER_ATM = 1_000_000_000D * 1_000D;
	//for atmosphere editor

	public static boolean canEscapeSolarGravity(double vesselDeltaV, CelestialBody solar) {
		double solarRadiusKm = solar.radiusKm;

		// escape velocity from surface (simplified classical approximation)
		double escapeVelocity = Math.sqrt(
			2.0 * GRAVITATIONAL_CONSTANT * solar.massKg / (solarRadiusKm * 1000.0)
		);

		// convert to "game delta-v equivalent scaling"
		// (you can tune this factor based on your mod’s velocity scale)
		double requiredDeltaV = escapeVelocity * ORBIT_SCALE;

		return vesselDeltaV >= requiredDeltaV;
	}

}
