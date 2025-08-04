package com.hfr.command;

import java.util.Map;

public class MuteManager {
    private static final Map<String, Long> mutedPlayers = new HashMap<>();

    public static void mute(String name, int seconds) {
        mutedPlayers.put(name.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public static boolean isMuted(String name) {
        Long until = mutedPlayers.get(name.toLowerCase());
        return until != null && until > System.currentTimeMillis();
    }
}
