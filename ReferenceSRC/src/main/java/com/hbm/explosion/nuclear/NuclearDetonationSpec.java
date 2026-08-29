package com.hbm.explosion.nuclear;

import com.hbm.config.BombConfig;

/**
 * Compact, serializable description of a detonation. Distances remain compressed
 * Minecraft gameplay distances; yield is only used to derive separate effects.
 */
public final class NuclearDetonationSpec {
	public double yieldKt;
	public double fissionFraction = 1.0D;
	public BurstType burstType = BurstType.SURFACE;
	public double burstHeight;
	public double groundCoupling = 1.0D;
	public double burialDepth;
	public double predictedBreakthroughFactor = 1.0D;
	/** @deprecated compatibility alias; prediction never authorizes atmospheric effects. */
	public double surfaceBreakthroughFactor = 1.0D;
	public boolean actualSurfaceBreach;
	public double atmosphericReleaseFactor = 1.0D;
	public double surfaceDeformationFactor = 1.0D;
	public boolean breachConfirmationComplete;
	public boolean contained;
	public boolean vented;
	public int breachX, breachY, breachZ;
	public double thermalFraction = 0.35D;
	public double promptGammaFraction = 0.05D;
	public double promptNeutronFraction = 0.02D;
	public boolean createsFallout = true;
	public boolean createsEMP = true;
	public boolean salted;

	public static NuclearDetonationSpec fromLegacyRadius(int radius) {
		NuclearDetonationSpec spec = new NuclearDetonationSpec();
		spec.yieldKt = BombConfig.ktFromRadius(Math.max(1, radius));
		return spec;
	}
}
