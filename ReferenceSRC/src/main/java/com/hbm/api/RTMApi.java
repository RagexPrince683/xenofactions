package com.hbm.api;

/**
 * Stable runtime identity API for Ragex Nuclear Tech.
 * <p>
 * Integration mods may safely check for this class reflectively to distinguish
 * Ragex Nuclear Tech (RTM) from regular HBM Nuclear Tech without initializing
 * RTM internals or client-only classes.
 */
public final class RTMApi {

	public static final String MOD_FAMILY = "HBM";
	public static final String MOD_VARIANT = "RTM";
	public static final String MOD_NAME = "Ragex Nuclear Tech";
	public static final int API_VERSION = 1;

	private RTMApi() { }

	public static boolean isRTM() {
		return true;
	}

	public static String getVariant() {
		return MOD_VARIANT;
	}

	public static int getApiVersion() {
		return API_VERSION;
	}
}
