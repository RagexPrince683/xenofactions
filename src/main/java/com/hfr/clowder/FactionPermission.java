package com.hfr.clowder;

/** Actions a territory owner may grant to allies or neutral visitors. */
public enum FactionPermission {
	BUILD, DESTROY, CONTAINER, INTERACT, SWITCH;

	public static FactionPermission parse(String value) {
		try { return value == null ? null : valueOf(value.toUpperCase()); }
		catch(IllegalArgumentException ignored) { return null; }
	}
}
