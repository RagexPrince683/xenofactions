package com.hfr.clowder;

import java.util.UUID;

public final class FactionApplication {
	public final UUID playerUuid;
	public String lastKnownName;
	public FactionApplication(UUID playerUuid, String lastKnownName) {
		this.playerUuid = playerUuid;
		this.lastKnownName = lastKnownName == null ? "" : lastKnownName;
	}
}
