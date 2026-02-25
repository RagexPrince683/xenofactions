package com.hfr.command;

import java.util.UUID;

public class Mute {

    public UUID uuid;
    public long expiresAt; // Long.MAX_VALUE = permanent
    public String reason;

    public Mute(UUID uuid, long expiresAt, String reason) {
        this.uuid = uuid;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    public boolean isExpired() {
        return expiresAt != Long.MAX_VALUE &&
                expiresAt <= System.currentTimeMillis();
    }

    public boolean isPermanent() {
        return expiresAt == Long.MAX_VALUE;
    }
}