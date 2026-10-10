package com.hfr.ender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.Loader;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/** Server-side rules for deposits into the player's vanilla ender inventory. */
public final class EnderChestRestrictions {

    public static final String CATEGORY = "XENOFACTIONS_21_ENDER_CHEST";
    private static final String[] DEFAULT_STORAGE = {
        "hbm:item.plastic_bag", "hbm:item.containment_box", "hbm:item.kit_toolbox", "hbm:item.kit_custom",
        "hbm:tile.crate_iron", "hbm:tile.crate_steel", "hbm:tile.crate_desh",
        "hbm:tile.crate_tungsten", "hbm:tile.crate_template", "hbm:tile.safe"
    };

    private static boolean enabled;
    private static boolean whitelist;
    private static int maxTotal = -1;
    private static List<Rule> items = new ArrayList<Rule>();
    private static List<Rule> storage = new ArrayList<Rule>();
    private static List<Limit> limits = new ArrayList<Limit>();

    private EnderChestRestrictions() { }

    public static boolean isEnabled() { return enabled; }

    public static void load(Configuration config) {
        config.addCustomCategoryComment(CATEGORY, "21 - Server-side ender chest deposit rules. Saved contents are never removed.");
        enabled = bool(config, "enabled", false, "Enable ender chest deposit restrictions. Disabled preserves vanilla behavior.");
        String mode = string(config, "mode", "blacklist", "blacklist rejects itemRules; whitelist accepts only itemRules. storageDenyList always wins.");
        if (!"blacklist".equalsIgnoreCase(mode) && !"whitelist".equalsIgnoreCase(mode)) {
            warn("mode '" + mode + "' is invalid; using blacklist");
            mode = "blacklist";
        }
        whitelist = "whitelist".equalsIgnoreCase(mode);
        items = parseRules(list(config, "itemRules", new String[0], "Registry names, optionally @metadata, for the selected mode. Example: minecraft:diamond or minecraft:wool@14."), "itemRules");
        storage = parseRules(list(config, "storageDenyList", DEFAULT_STORAGE, "Portable storage item registry names, optionally @metadata. These are denied even in whitelist mode."), "storageDenyList");
        maxTotal = quantity(string(config, "maxTotalItems", "-1", "Maximum individual items across all 27 slots; -1 unlimited, 0 prevents deposits."), "maxTotalItems");
        if (maxTotal == -2) maxTotal = -1;
        limits = parseLimits(list(config, "itemQuantityLimits", new String[0], "Registry name[@metadata]=quantity, e.g. minecraft:diamond=16. -1 unlimited; 0 prevents deposits."));
    }

    /** Run after all mods have registered their items, including optional storage mods. */
    public static void validateRegistry() {
        validateRules(items, "itemRules");
        validateRules(storage, "storageDenyList");
        for (Iterator<Limit> it = limits.iterator(); it.hasNext();) {
            Limit limit = it.next();
            if (!limit.rule.isRegistered()) {
                warn("itemQuantityLimits entry '" + limit.rule + "' is not a registered item; ignoring it");
                it.remove();
            }
        }
    }

    private static void validateRules(List<Rule> rules, String setting) {
        for (Iterator<Rule> it = rules.iterator(); it.hasNext();) {
            Rule rule = it.next();
            if (rule.isRegistered()) continue;
            if (!("storageDenyList".equals(setting) && !Loader.isModLoaded("hbm") && isDefaultStorage(rule.name)))
                warn(setting + " entry '" + rule + "' is not a registered item; ignoring it");
            it.remove();
        }
    }

    private static boolean isDefaultStorage(String name) {
        for (String known : DEFAULT_STORAGE) if (known.equals(name)) return true;
        return false;
    }

    private static boolean bool(Configuration c, String name, boolean def, String comment) {
        Property p = c.get(CATEGORY, name, def);
        p.comment = comment;
        String value = p.getString().trim();
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        warn(name + " value '" + value + "' is invalid; using " + def);
        return def;
    }

    private static String string(Configuration c, String name, String def, String comment) {
        Property p = c.get(CATEGORY, name, def); p.comment = comment; return p.getString().trim();
    }

    private static String[] list(Configuration c, String name, String[] def, String comment) {
        Property p = c.get(CATEGORY, name, def); p.comment = comment; return p.getStringList();
    }

    private static List<Rule> parseRules(String[] entries, String setting) {
        List<Rule> out = new ArrayList<Rule>();
        for (String entry : entries) {
            Rule rule = Rule.parse(entry);
            if (rule == null) warn(setting + " entry '" + entry + "' is invalid; expected modid:registry_name[@metadata]");
            else out.add(rule);
        }
        return out;
    }

    private static List<Limit> parseLimits(String[] entries) {
        List<Limit> out = new ArrayList<Limit>();
        for (String entry : entries) {
            String[] parts = entry == null ? new String[0] : entry.split("=", -1);
            Rule rule = parts.length == 2 ? Rule.parse(parts[0]) : null;
            int amount = parts.length == 2 ? quantity(parts[1].trim(), "itemQuantityLimits entry '" + entry + "'") : -2;
            if (rule == null || amount == -2) {
                warn("itemQuantityLimits entry '" + entry + "' is invalid; expected modid:registry_name[@metadata]=quantity");
            } else if (amount >= 0) {
                out.add(new Limit(rule, amount));
            }
        }
        return out;
    }

    private static int quantity(String value, String setting) {
        try {
            int n = Integer.parseInt(value);
            if (n >= -1) return n;
        } catch (NumberFormatException ignored) { }
        warn(setting + " has invalid quantity '" + value + "'; using -1 (unlimited)");
        return -2;
    }

    public static String rejection(ItemStack[] openingContents, IInventory after) {
        if (!enabled) return null;
        int oldTotal = total(openingContents);
        int newTotal = total(after);
        boolean newItem = hasNewItems(openingContents, after);

        for (int i = 0; i < after.getSizeInventory(); i++) {
            ItemStack stack = after.getStackInSlot(i);
            if (stack == null || countExact(stack, after) <= countExact(stack, openingContents)) continue;
            if (matches(storage, stack)) return "Portable storage is blocked from the ender chest.";
            if (whitelist && !matches(items, stack)) return "This item is not on the ender chest whitelist.";
            if (!whitelist && matches(items, stack)) return "This item is on the ender chest blacklist.";
        }

        if (maxTotal >= 0 && newItem && (oldTotal > maxTotal || newTotal > maxTotal))
            return "Ender chest limit: maximum " + maxTotal + " items.";

        for (Limit limit : limits) {
            int oldCount = count(limit.rule, openingContents);
            int newCount = count(limit.rule, after);
            if (newCount > limit.max && newCount > oldCount)
                return "Ender chest item limit: " + limit.rule + " may have at most " + limit.max + ".";
            if (oldCount > limit.max && newItem)
                return "Ender chest item limit: withdraw " + limit.rule + " before depositing.";
        }
        return null;
    }

    private static boolean hasNewItems(ItemStack[] before, IInventory after) {
        for (int i = 0; i < after.getSizeInventory(); i++) {
            ItemStack stack = after.getStackInSlot(i);
            if (stack != null && countExact(stack, after) > countExact(stack, before)) return true;
        }
        return false;
    }

    private static int countExact(ItemStack target, ItemStack[] stacks) {
        int n = 0;
        for (ItemStack stack : stacks) if (sameStack(target, stack)) n += stack.stackSize;
        return n;
    }

    private static int countExact(ItemStack target, IInventory inventory) {
        int n = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (sameStack(target, stack)) n += stack.stackSize;
        }
        return n;
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        return b != null && a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
            && ItemStack.areItemStackTagsEqual(a, b);
    }

    private static int total(ItemStack[] stacks) {
        int n = 0; for (ItemStack stack : stacks) if (stack != null) n += stack.stackSize; return n;
    }

    private static int total(IInventory inventory) {
        int n = 0; for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i); if (stack != null) n += stack.stackSize;
        } return n;
    }

    private static int count(Rule rule, ItemStack[] stacks) {
        int n = 0; for (ItemStack stack : stacks) if (rule.matches(stack)) n += stack.stackSize; return n;
    }

    private static int count(Rule rule, IInventory inventory) {
        int n = 0; for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i); if (rule.matches(stack)) n += stack.stackSize;
        } return n;
    }

    private static boolean matches(List<Rule> rules, ItemStack stack) {
        for (Rule rule : rules) if (rule.matches(stack)) return true;
        return false;
    }

    private static void warn(String message) {
        if (MainRegistry.logger != null) MainRegistry.logger.warn("Xenofactions ender chest config: " + message);
    }

    private static final class Limit {
        final Rule rule; final int max;
        Limit(Rule rule, int max) { this.rule = rule; this.max = max; }
    }

    private static final class Rule {
        final String name; final int metadata;
        Rule(String name, int metadata) { this.name = name; this.metadata = metadata; }

        static Rule parse(String raw) {
            if (raw == null) return null;
            String value = raw.trim();
            int at = value.indexOf('@');
            String name = at < 0 ? value : value.substring(0, at);
            if (!name.matches("[A-Za-z0-9_.-]+:[A-Za-z0-9_./-]+")) return null;
            if (at < 0) return new Rule(name, -1);
            try {
                int meta = Integer.parseInt(value.substring(at + 1));
                return meta >= 0 && meta <= 32767 ? new Rule(name, meta) : null;
            } catch (NumberFormatException ignored) { return null; }
        }

        boolean matches(ItemStack stack) {
            if (stack == null) return false;
            Item item = stack.getItem();
            String registered = item == null ? null : (String) Item.itemRegistry.getNameForObject(item);
            return name.equals(registered) && (metadata < 0 || metadata == stack.getItemDamage());
        }

        boolean isRegistered() {
            Item item = (Item) Item.itemRegistry.getObject(name);
            return item != null && name.equals(Item.itemRegistry.getNameForObject(item));
        }

        @Override public String toString() { return name + (metadata < 0 ? "" : "@" + metadata); }
    }
}
