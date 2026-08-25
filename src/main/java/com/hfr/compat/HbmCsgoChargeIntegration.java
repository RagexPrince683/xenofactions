package com.hfr.compat;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Optional HBM lookup for the CSGO bomb block; never links against HBM classes. */
public final class HbmCsgoChargeIntegration {

    private static final String HBM_MOD_ID = "hbm";
    private static final String[] REGISTRY_NAMES = { "tile.charge_c4csgo", "charge_c4csgo" };
    private static boolean resolved;
    private static Block csgoCharge;

    private HbmCsgoChargeIntegration() {
    }

    public static boolean isCsgoCharge(Block block) {
        if (!resolved) {
            resolve();
        }
        return csgoCharge != null && block == csgoCharge;
    }

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        if (!Loader.isModLoaded(HBM_MOD_ID)) return;
        for (String name : REGISTRY_NAMES) {
            csgoCharge = GameRegistry.findBlock(HBM_MOD_ID, name);
            if (csgoCharge != null) return;
        }
    }
}
