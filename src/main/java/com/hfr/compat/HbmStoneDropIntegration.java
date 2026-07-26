package com.hfr.compat;

import java.lang.reflect.Field;
import java.util.List;

import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.Loader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Optional, reflection-only defaults sourced from HBM's excavator configuration. */
public final class HbmStoneDropIntegration {

    private static final String HBM_MOD_ID = "hbm";
    private static final String MINING_CONFIG_CLASS = "com.hbm.config.MiningConfig";
    private static final double DEFAULT_DROP_CHANCE = 0.01D;

    private HbmStoneDropIntegration() {
    }

    /**
     * Seeds a new stone-drop configuration without making HBM a required dependency.
     * Existing configurations are always left alone, including deliberately empty ones.
     */
    public static void seedDefaultsIfAvailable(boolean stoneDropFileExists) {
        if (stoneDropFileExists || !Loader.isModLoaded(HBM_MOD_ID)) {
            return;
        }

        try {
            Class<?> miningConfigClass = Class.forName(MINING_CONFIG_CLASS, false,
                    HbmStoneDropIntegration.class.getClassLoader());
            Field dropsField = miningConfigClass.getField("excavatorBedrockDrops");
            Object configuredDrops = dropsField.get(null);
            if (!(configuredDrops instanceof List)) {
                MainRegistry.logger.warn("[XF] HBM MiningConfig.excavatorBedrockDrops is not a list; stone-drop defaults were not created.");
                return;
            }

            int added = 0;
            for (Object configuredDrop : (List<?>) configuredDrops) {
                if (configuredDrop instanceof String && addDrop((String) configuredDrop)) {
                    added++;
                }
            }

            if (added > 0) {
                MainRegistry.saveCustomDrops();
                MainRegistry.logger.info("[XF] Created config/stonedrops.json with " + added
                        + " defaults from HBM MiningConfig.");
            }
        } catch (ClassNotFoundException e) {
            MainRegistry.logger.info("[XF] HBM is installed without com.hbm.config.MiningConfig; stone-drop defaults were not created.");
        } catch (Exception e) {
            MainRegistry.logger.warn("[XF] Could not read HBM MiningConfig for stone-drop defaults: " + e.getMessage());
        }
    }

    private static boolean addDrop(String specification) {
        String[] values = specification.trim().split("\\s+");
        if (values.length < 4) {
            MainRegistry.logger.warn("[XF] Ignoring invalid HBM excavator drop: " + specification);
            return false;
        }

        try {
            Item item = (Item) Item.itemRegistry.getObject(values[0]);
            if (item == null) {
                MainRegistry.logger.warn("[XF] Ignoring missing HBM excavator item: " + values[0]);
                return false;
            }

            int metadata = Integer.parseInt(values[1]);
            int minimumAmount = Integer.parseInt(values[2]);
            Integer.parseInt(values[3]); // Validate HBM's maximum-amount field as well.
            int[] yRange = depthForMaterial(values[0]);

            MainRegistry.customDrops.add(new ItemStack(item, Math.max(1, minimumAmount), metadata));
            MainRegistry.customDropChances.add(DEFAULT_DROP_CHANCE);
            MainRegistry.customDropMinYs.add(Integer.valueOf(yRange[0]));
            MainRegistry.customDropMaxYs.add(Integer.valueOf(yRange[1]));
            return true;
        } catch (NumberFormatException e) {
            MainRegistry.logger.warn("[XF] Ignoring invalid HBM excavator drop: " + specification);
            return false;
        }
    }

    /** Harder minerals are assigned deeper ranges; progressively softer deposits extend upward. */
    static int[] depthForMaterial(String registryName) {
        String name = registryName.toLowerCase();
        if (containsAny(name, "fire", "uranium", "beryllium", "pollucite", "chunk_ore")) {
            return new int[] { 1, 20 };
        }
        if (containsAny(name, "lithium", "tintungsten", "chromite")) {
            return new int[] { 5, 32 };
        }
        if (containsAny(name, "leadzinc", "nickel", "aluminium", "heavymineral")) {
            return new int[] { 10, 48 };
        }
        if (containsAny(name, "copper", "ironoxide", "evaporite", "quartz", "lapis")) {
            return new int[] { 16, 64 };
        }
        return new int[] { 24, 96 };
    }

    private static boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
