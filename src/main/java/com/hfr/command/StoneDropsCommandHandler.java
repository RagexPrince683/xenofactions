package com.hfr.command;

import com.hfr.main.MainRegistry;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class StoneDropsCommandHandler {

    private StoneDropsCommandHandler() { }

    private static final int ENTRIES_PER_PAGE = 8;
    private static final String ERROR = EnumChatFormatting.RED.toString();
    private static final String TITLE = EnumChatFormatting.GOLD.toString();
    private static final String INFO = EnumChatFormatting.GREEN.toString();
    private static final String HELP = EnumChatFormatting.DARK_GREEN.toString();
    private static final String COMMAND = EnumChatFormatting.RED.toString();
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.######", DecimalFormatSymbols.getInstance(Locale.US));

    public static void execute(ICommandSender sender, String[] args) {
        if (args.length > 1) {
            sendUsage(sender);
            return;
        }

        int requestedPage = 1;
        if (args.length == 1) {
            if (!args[0].matches("[1-9][0-9]*")) {
                sendInvalidPage(sender);
                return;
            }
            try {
                requestedPage = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sendInvalidPage(sender);
                return;
            }
        }

        int validEntryCount = getValidEntryCount();
        if (validEntryCount <= 0) {
            sender.addChatMessage(new ChatComponentText(INFO + "No custom stone drops are configured."));
            return;
        }

        int totalPages = (validEntryCount + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;
        int page = Math.min(requestedPage, totalPages);
        int start = (page - 1) * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, validEntryCount);

        sender.addChatMessage(new ChatComponentText(TITLE + "Custom Stone Drops - Page " + page + "/" + totalPages));
        for (int i = start; i < end; i++) {
            ItemStack stack = MainRegistry.customDrops.get(i);
            Double chance = MainRegistry.customDropChances.get(i);
            Integer minY = MainRegistry.customDropMinYs.get(i);
            Integer maxY = MainRegistry.customDropMaxYs.get(i);

            sender.addChatMessage(new ChatComponentText(HELP + "#" + (i + 1) + " " + INFO + getDisplayName(stack)
                    + HELP + " x" + getStackAmount(stack)));
            sender.addChatMessage(new ChatComponentText(HELP + "  Registry: " + INFO + getRegistryName(stack)
                    + HELP + " | Meta: " + INFO + getMetadata(stack)));
            sender.addChatMessage(new ChatComponentText(HELP + "  Chance: " + INFO + formatChance(chance)
                    + HELP + " | Raw: " + INFO + String.valueOf(chance)
                    + HELP + " | " + INFO + formatYRange(minY, maxY)));
        }

        if (page < totalPages) {
            sender.addChatMessage(new ChatComponentText(COMMAND + "/c stonedrops " + (page + 1) + INFO + " - next page"));
        }
        if (page > 1) {
            sender.addChatMessage(new ChatComponentText(COMMAND + "/c stonedrops " + (page - 1) + INFO + " - previous page"));
        }
    }

    private static int getValidEntryCount() {
        return Math.min(Math.min(MainRegistry.customDrops.size(), MainRegistry.customDropChances.size()),
                Math.min(MainRegistry.customDropMinYs.size(), MainRegistry.customDropMaxYs.size()));
    }

    private static void sendUsage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(ERROR + "Usage: " + "/c stonedrops [page]"));
    }

    private static void sendInvalidPage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(ERROR + "Invalid page. Use a positive whole number, e.g. /c stonedrops 1"));
    }

    private static String getDisplayName(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return "Unknown item";
        }
        try {
            String displayName = stack.getDisplayName();
            return displayName == null || displayName.length() == 0 ? "Unknown item" : displayName;
        } catch (RuntimeException e) {
            return "Unknown item";
        }
    }

    private static int getStackAmount(ItemStack stack) {
        return stack == null ? 0 : stack.stackSize;
    }

    private static String getRegistryName(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return "unknown";
        }
        String registryName = Item.itemRegistry.getNameForObject(stack.getItem());
        return registryName == null || registryName.length() == 0 ? "unknown" : registryName;
    }

    private static int getMetadata(ItemStack stack) {
        return stack == null ? 0 : stack.getItemDamage();
    }

    private static String formatChance(Double chance) {
        if (chance == null) {
            return "unknown per stone block";
        }
        return PERCENT_FORMAT.format(chance.doubleValue() * 100.0D) + "% per stone block";
    }

    private static String formatYRange(Integer minY, Integer maxY) {
        if (minY == null || maxY == null) {
            return "Any height";
        }
        return "Y " + minY + "-" + maxY;
    }
}
