package com.hfr.compat;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * Adds missing HBM defaults without making HBM a required dependency.
     * Existing item and metadata entries keep their configured count, chance, and Y range.
     */
    public static void seedDefaultsIfAvailable(boolean stoneDropFileExists) {
        if (!Loader.isModLoaded(HBM_MOD_ID)) {
            return;
        }

        try {
            Class<?> miningConfigClass = Class.forName(MINING_CONFIG_CLASS, true,
                    HbmStoneDropIntegration.class.getClassLoader());
            Field dropsField = miningConfigClass.getField("excavatorBedrockDrops");
            Object configuredDrops = dropsField.get(null);
            if (!(configuredDrops instanceof List)) {
                MainRegistry.logger.warn("[XF] HBM MiningConfig.excavatorBedrockDrops is not a list; stone-drop defaults were not merged.");
                return;
            }

            Set<String> configuredKeys = collectConfiguredKeys();
            int added = 0;
            int valid = 0;

            for (Object configuredDrop : (List<?>) configuredDrops) {
                if (!(configuredDrop instanceof String)) {
                    continue;
                }

                AddResult result = addDropIfMissing((String) configuredDrop, configuredKeys);
                if (result != AddResult.INVALID) {
                    valid++;
                }
                if (result == AddResult.ADDED) {
                    added++;
                }
            }

            if (added > 0) {
                MainRegistry.saveCustomDrops();
                String action = stoneDropFileExists ? "Merged " : "Created config/stonedrops.json with ";
                MainRegistry.logger.info("[XF] " + action + added
                        + " missing defaults from HBM MiningConfig.");
            } else if (valid > 0) {
                MainRegistry.logger.info("[XF] HBM stone-drop defaults are already configured.");
            } else {
                MainRegistry.logger.warn("[XF] HBM MiningConfig did not contain any valid excavator stone drops.");
            }
        } catch (ClassNotFoundException e) {
            MainRegistry.logger.info("[XF] HBM is installed without com.hbm.config.MiningConfig; stone-drop defaults were not merged.");
        } catch (Exception e) {
            MainRegistry.logger.warn("[XF] Could not read HBM MiningConfig for stone-drop defaults: " + e.getMessage());
        }
    }

    private static Set<String> collectConfiguredKeys() {
        Set<String> configuredKeys = new HashSet<String>();

        for (ItemStack stack : MainRegistry.customDrops) {
            if (stack == null || stack.getItem() == null) {
                continue;
            }

            String registryName = Item.itemRegistry.getNameForObject(stack.getItem());
            if (registryName != null) {
                configuredKeys.add(dropKey(registryName, stack.getItemDamage()));
            }
        }

        return configuredKeys;
    }

    private static AddResult addDropIfMissing(String specification, Set<String> configuredKeys) {
        String trimmed = specification == null ? "" : specification.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return AddResult.INVALID;
        }

        String[] values = trimmed.split("\\s+");
        if (values.length < 4) {
            MainRegistry.logger.warn("[XF] Ignoring invalid HBM excavator drop: " + specification);
            return AddResult.INVALID;
        }

        try {
            Item item = (Item) Item.itemRegistry.getObject(values[0]);
            if (item == null) {
                MainRegistry.logger.warn("[XF] Ignoring missing HBM excavator item: " + values[0]);
                return AddResult.INVALID;
            }

            int metadata = Integer.parseInt(values[1]);
            int minimumAmount = Integer.parseInt(values[2]);
            Integer.parseInt(values[3]); // Validate HBM's maximum-amount field as well.

            String registryName = Item.itemRegistry.getNameForObject(item);
            if (registryName == null) {
                MainRegistry.logger.warn("[XF] Ignoring unregistered HBM excavator item: " + values[0]);
                return AddResult.INVALID;
            }

            String key = dropKey(registryName, metadata);
            if (configuredKeys.contains(key)) {
                return AddResult.ALREADY_CONFIGURED;
            }

            int[] yRange = depthForMaterial(registryName);
            MainRegistry.customDrops.add(new ItemStack(item, Math.max(1, minimumAmount), metadata));
            MainRegistry.customDropChances.add(DEFAULT_DROP_CHANCE);
            MainRegistry.customDropMinYs.add(Integer.valueOf(yRange[0]));
            MainRegistry.customDropMaxYs.add(Integer.valueOf(yRange[1]));
            configuredKeys.add(key);
            return AddResult.ADDED;
        } catch (NumberFormatException e) {
            MainRegistry.logger.warn("[XF] Ignoring invalid HBM excavator drop: " + specification);
            return AddResult.INVALID;
        }
    }

    private static String dropKey(String registryName, int metadata) {
        return registryName + "@" + metadata;
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

    private enum AddResult {
        ADDED,
        ALREADY_CONFIGURED,
        INVALID
    }
}
