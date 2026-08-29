package com.hbm.core.compat;

/** Early-load-safe shared state and numeric hooks. Deliberately has no Minecraft or HD references. */
public final class HardcoreDarknessCompatHooks {
	private static final float EPSILON = 0.0001F;
	private static volatile boolean clientEnabled;
	private static volatile boolean detected;
	private static volatile boolean patched;
	private static volatile float baseSkyCarrier = 0.06F;
	private static volatile float effectiveAdaptation;
	private static volatile float perceivedAmbient;
	private static volatile float activeSkyCarrier = 0.06F;

	private HardcoreDarknessCompatHooks() { }

	public static void configure(boolean enabled, float carrier) {
		clientEnabled = enabled;
		baseSkyCarrier = clamp(carrier, 0F, 0.12F);
		updateActiveCarrier();
	}

	/** Receives client-computed perception values without loading any Minecraft classes here. */
	public static void updateDarkAdaptation(float adaptation, float ambient) {
		effectiveAdaptation = clamp(adaptation, 0F, 1F);
		perceivedAmbient = clamp(ambient, 0F, 1F);
		updateActiveCarrier();
	}

	private static void updateActiveCarrier() {
		activeSkyCarrier = clamp(baseSkyCarrier + 0.16F * effectiveAdaptation * perceivedAmbient, 0F, 0.25F);
	}

	public static boolean shouldOverrideUp(float value) {
		return clientEnabled && detected && patched && Math.abs(value - 0.95F) < EPSILON;
	}

	public static boolean shouldOverrideDown(float value) {
		return clientEnabled && detected && patched && Math.abs(value - 0.05F) < EPSILON;
	}

	public static float overrideUp(float value) { return 1F - activeSkyCarrier; }
	public static float overrideDown(float value) { return activeSkyCarrier; }
	public static void markDetected() { detected = true; }
	public static void markPatched() { patched = true; }
	public static boolean isDetected() { return detected; }
	public static boolean isCompatEnabled() { return clientEnabled && detected && patched; }
	public static boolean isHookPatched() { return patched; }
	public static float getSkyCarrier() { return activeSkyCarrier; }
	public static float getSkyMultiplierOverride() { return 1F - activeSkyCarrier; }
	public static float getSkyMinimumOverride() { return activeSkyCarrier; }
	private static float clamp(float value, float low, float high) { return Math.max(low, Math.min(high, value)); }
}
