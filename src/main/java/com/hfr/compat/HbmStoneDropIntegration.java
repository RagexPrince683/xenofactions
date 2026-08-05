package com.hfr.compat;

import java.lang.reflect.Field;
import java.util.List;

import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Optional, reflection-only defaults sourced from HBM's excavator configuration. */
public final class HbmStoneDropIntegration {

    private static final String HBM_MOD_ID = "hbm";
    private static final String MINING_CONFIG_CLASS = "com.hbm.config.MiningConfig";
    private static final double DEFAULT_DROP_CHANCE = 0.01D;

    private HbmStoneDropIntegration() {
    }

    public static boolean seedDefaultsIfAvailable(MainRegistry.StoneDropLoadState loadState) {
        if (!Loader.isModLoaded(HBM_MOD_ID)) {
            return false;
        }
        if (loadState == MainRegistry.StoneDropLoadState.EMPTY_LIST) {
            MainRegistry.logger.info("[XF] Existing config/stonedrops.json contains an empty list; HBM automatic stone-drop generation skipped.");
            return false;
        }

        try {
            Class<?> miningConfigClass = Class.forName(MINING_CONFIG_CLASS, true,
                    HbmStoneDropIntegration.class.getClassLoader());
            Field dropsField = miningConfigClass.getField("excavatorBedrockDrops");
            Object configuredDrops = dropsField.get(null);
            if (!(configuredDrops instanceof List)) {
                MainRegistry.logger.warn("[XF] HBM MiningConfig.excavatorBedrockDrops is not a java.util.List; automatic stone drops were not created.");
                return false;
            }

            int added = 0;
            for (Object configuredDrop : (List<?>) configuredDrops) {
                if (!(configuredDrop instanceof String)) {
                    MainRegistry.logger.warn("[XF] Ignoring non-string HBM excavator drop specification: " + String.valueOf(configuredDrop));
                    continue;
                }
                if (addDrop((String) configuredDrop)) {
                    added++;
                }
            }

            if (added > 0) {
                if (loadState == MainRegistry.StoneDropLoadState.MALFORMED) {
                    MainRegistry.backupMalformedStoneDrops();
                }
                MainRegistry.saveCustomDrops();
                MainRegistry.logger.info("[XF] Registered " + added + " automatic HBM stone drops from MiningConfig.excavatorBedrockDrops.");
                return true;
            }
            MainRegistry.logger.warn("[XF] HBM MiningConfig.excavatorBedrockDrops contained no valid automatic stone drops; config/stonedrops.json was not rewritten.");
        } catch (ClassNotFoundException e) {
            MainRegistry.logger.warn("[XF] HBM is loaded but com.hbm.config.MiningConfig is unavailable; automatic stone drops were not created.");
        } catch (NoSuchFieldException e) {
            MainRegistry.logger.warn("[XF] HBM MiningConfig.excavatorBedrockDrops is unavailable; automatic stone drops were not created.");
        } catch (IllegalAccessException e) {
            MainRegistry.logger.warn("[XF] HBM MiningConfig.excavatorBedrockDrops could not be read: " + e.getMessage());
        } catch (LinkageError e) {
            MainRegistry.logger.warn("[XF] HBM MiningConfig could not be linked while reading automatic stone drops: " + e.getMessage());
        }
        return false;
    }

    private static boolean addDrop(String specification) {
        String[] values = specification.trim().split("\\s+");
        if (values.length != 4) {
            MainRegistry.logger.warn("[XF] Ignoring invalid HBM excavator drop specification '" + specification + "': expected 'modid:item metadata minimumAmount maximumAmount'.");
            return false;
        }

        try {
            String registryName = values[0];
            int metadata = Integer.parseInt(values[1]);
            int minimumAmount = Integer.parseInt(values[2]);
            int maximumAmount = Integer.parseInt(values[3]);
            if (metadata < 0 || minimumAmount < 0 || maximumAmount < minimumAmount) {
                MainRegistry.logger.warn("[XF] Ignoring invalid HBM excavator drop specification '" + specification + "': invalid metadata or amount range.");
                return false;
            }

            Item item = resolveItem(registryName);
            if (item == null) {
                MainRegistry.logger.warn("[XF] Ignoring HBM excavator drop specification '" + specification + "': item registry name could not be resolved.");
                return false;
            }

            ItemStack stack = new ItemStack(item, Math.max(1, minimumAmount), metadata);
            if (MainRegistry.hasCustomDrop(stack)) {
                return false;
            }

            int[] yRange = depthForMaterial(registryName);
            MainRegistry.customDrops.add(stack);
            MainRegistry.customDropChances.add(DEFAULT_DROP_CHANCE);
            MainRegistry.customDropMinYs.add(Integer.valueOf(yRange[0]));
            MainRegistry.customDropMaxYs.add(Integer.valueOf(yRange[1]));
            if (MainRegistry.logger.isDebugEnabled()) {
                MainRegistry.logger.debug("[XF] Resolved HBM stone drop " + Item.itemRegistry.getNameForObject(item)
                        + " meta " + metadata + " chance " + DEFAULT_DROP_CHANCE
                        + " Y " + yRange[0] + "-" + yRange[1] + " from '" + specification + "'.");
            }
            return true;
        } catch (NumberFormatException e) {
            MainRegistry.logger.warn("[XF] Ignoring invalid HBM excavator drop specification '" + specification + "': " + e.getMessage());
            return false;
        }
    }

    private static Item resolveItem(String registryName) {
        Item item = (Item) Item.itemRegistry.getObject(registryName);
        if (item != null) return item;
        int separator = registryName.indexOf(':');
        if (separator < 1 || separator == registryName.length() - 1) return null;
        String modId = registryName.substring(0, separator);
        String name = registryName.substring(separator + 1);
        item = GameRegistry.findItem(modId, name);
        if (item != null) return item;
        Block block = GameRegistry.findBlock(modId, name);
        return block == null ? null : Item.getItemFromBlock(block);
    }

    /** Harder minerals are assigned deeper ranges; progressively softer deposits extend upward. */
    static int[] depthForMaterial(String registryName) {
        String name = registryName.toLowerCase();
        if (containsAny(name, "fire", "uranium", "beryllium", "pollucite", "chunk_ore")) return new int[] { 1, 20 };
        if (containsAny(name, "lithium", "tintungsten", "chromite")) return new int[] { 5, 32 };
        if (containsAny(name, "leadzinc", "nickel", "aluminium", "heavymineral")) return new int[] { 10, 48 };
        if (containsAny(name, "copper", "ironoxide", "evaporite", "quartz", "lapis")) return new int[] { 16, 64 };
        return new int[] { 24, 96 };
    }

    private static boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) if (value.contains(candidate)) return true;
        return false;
    }
}
