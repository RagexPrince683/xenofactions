package com.hfr.compat;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Optional HBM lookup for the CSGO bomb block; never links against HBM classes. */
public final class HbmCsgoChargeIntegration {

    private static final String HBM_MOD_ID = "hbm";
    private static final String[] REGISTRY_NAMES = { "tile.charge_c4csgo", "charge_c4csgo" };
    private static boolean resolved;
    private static Block csgoCharge;
    private static Item csgoChargeItem;

    private HbmCsgoChargeIntegration() {
    }

    public static boolean isCsgoCharge(Block block) {
        if (!resolved) {
            resolve();
        }
        return csgoCharge != null && block == csgoCharge;
    }

    /** Runtime registry contract only; no HBM class is linked at compile time. */
    public static boolean isAvailable() {
        if (!resolved) resolve();
        return csgoCharge != null;
    }

    /** Returns a fresh inventory form of the exact HBM CSGO charge, or null when unavailable. */
    public static ItemStack createCsgoChargeStack() {
        if (!resolved) resolve();
        return csgoChargeItem == null ? null : new ItemStack(csgoChargeItem, 1, 0);
    }

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        if (!Loader.isModLoaded(HBM_MOD_ID)) return;
        for (String name : REGISTRY_NAMES) {
            csgoCharge = GameRegistry.findBlock(HBM_MOD_ID, name);
            if (csgoCharge != null) break;
        }
        if (csgoCharge == null) return;
        csgoChargeItem = Item.getItemFromBlock(csgoCharge);
        if (csgoChargeItem != null) return;
        for (String name : REGISTRY_NAMES) {
            csgoChargeItem = GameRegistry.findItem(HBM_MOD_ID, name);
            if (csgoChargeItem != null) return;
        }
    }
}
