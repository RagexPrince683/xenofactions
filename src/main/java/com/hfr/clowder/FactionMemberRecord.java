package com.hfr.clowder;

import java.util.UUID;

/** The UUID is authoritative; the name is display-only cached metadata. */
public final class FactionMemberRecord {
	public final UUID playerUuid;
	public String lastKnownName;
	public final long joinedAt;
	public FactionRole role;

	public FactionMemberRecord(UUID playerUuid, String lastKnownName, long joinedAt, FactionRole role) {
		if (playerUuid == null || role == null) throw new IllegalArgumentException("UUID and role are required");
		this.playerUuid = playerUuid;
		this.lastKnownName = lastKnownName == null ? "" : lastKnownName;
		this.joinedAt = joinedAt;
		this.role = role;
	}
}
