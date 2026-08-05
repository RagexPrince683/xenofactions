package com.hfr.clowder;

public enum FactionRole {
	OWNER(3), OFFICER(2), MEMBER(1);

	private final int permissionLevel;
	FactionRole(int permissionLevel) { this.permissionLevel = permissionLevel; }
	public int getPermissionLevel() { return permissionLevel; }
}
