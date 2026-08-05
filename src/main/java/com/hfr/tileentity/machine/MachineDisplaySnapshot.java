package com.hfr.tileentity.machine;

import com.hfr.main.MainRegistry;

public final class MachineDisplaySnapshot {
    public static final int MIN_SECONDS = 0;
    public static final int MAX_SECONDS = 1000000;
    private static volatile MachineDisplaySnapshot clientSnapshot;

    public final int superFishrate;
    public final int goodFishrate;
    public final int averageFishrate;
    public final int crapFishrate;
    public final int jamRate;
    public final int whaleChance;
    public final int windmillProduction;

    public MachineDisplaySnapshot(int superFishrate, int goodFishrate, int averageFishrate, int crapFishrate, int jamRate, int whaleChance, int windmillProduction) {
        this.superFishrate = clamp(superFishrate, MIN_SECONDS, MAX_SECONDS);
        this.goodFishrate = clamp(goodFishrate, MIN_SECONDS, MAX_SECONDS);
        this.averageFishrate = clamp(averageFishrate, MIN_SECONDS, MAX_SECONDS);
        this.crapFishrate = clamp(crapFishrate, MIN_SECONDS, MAX_SECONDS);
        this.jamRate = clamp(jamRate, MIN_SECONDS, MAX_SECONDS);
        this.whaleChance = clamp(whaleChance, 0, 100);
        this.windmillProduction = clamp(windmillProduction, 0, 1000000000);
    }

    public static MachineDisplaySnapshot fromRuntime() {
        return new MachineDisplaySnapshot(MainRegistry.superFishrate, MainRegistry.goodFishrate, MainRegistry.averageFishrate, MainRegistry.crapFishrate, MainRegistry.jamRate, MainRegistry.whaleChance, MainRegistry.windmillProduction);
    }

    public static MachineDisplaySnapshot forClientDisplay() {
        MachineDisplaySnapshot snapshot = clientSnapshot;
        return snapshot == null ? fromRuntime() : snapshot;
    }

    public static void replaceClientSnapshot(MachineDisplaySnapshot snapshot) { clientSnapshot = snapshot; }
    public static void clearClientSnapshot() { clientSnapshot = null; }

    public static int fishRateForBiomeGroup(String group, MachineDisplaySnapshot values) {
        if ("ocean".equals(group)) return values.superFishrate;
        if ("river".equals(group)) return values.goodFishrate;
        if ("dry".equals(group)) return values.crapFishrate;
        return values.averageFishrate;
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
