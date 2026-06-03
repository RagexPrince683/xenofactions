package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import com.hfr.main.MainRegistry;
import java.util.List;

public class CommandStoneDrop extends CommandBase {

    @Override
    public String getCommandName() {
        return "stonedrop";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/stonedrop <rarity> [minY] [maxY] OR /stonedrop list OR /stonedrop remove [index]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // Check argument count.
        if (args.length < 1) {
            sendUsage(sender);
            return;
        }

        String subCommand = args[0];

        if (subCommand.toLowerCase().equals("list")) {
            handleListCommand(sender);
        } else if (subCommand.toLowerCase().equals("remove")) {
            handleRemoveCommand(sender, args);
        } else {
            handleAddCommand(sender, args);
        }
    }

    private void handleListCommand(ICommandSender sender) {
        List<ItemStack> drops = MainRegistry.customDrops;

        if (drops.isEmpty()) {
            sender.addChatMessage(new ChatComponentText("No custom stone drops set."));
            return;
        }

        sender.addChatMessage(new ChatComponentText("Custom Stone Drops:"));
        for (int i = 0; i < drops.size(); i++) {
            ItemStack stack = drops.get(i);
            Integer minY = MainRegistry.customDropMinYs.get(i);
            Integer maxY = MainRegistry.customDropMaxYs.get(i);
            sender.addChatMessage(new ChatComponentText(i + 1 + ". " + stack.getDisplayName()
                    + " (Chance: " + MainRegistry.customDropChances.get(i)
                    + ", Y: " + formatYRange(minY, maxY) + ")"));
        }
    }

    private void handleRemoveCommand(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("Usage: /stonedrop remove [index]"));
            return;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]) - 1;
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText("Invalid index. Must be a number."));
            return;
        }

        if (index < 0 || index >= MainRegistry.customDrops.size()) {
            sender.addChatMessage(new ChatComponentText("Invalid index. Out of range."));
            return;
        }

        ItemStack removed = MainRegistry.customDrops.remove(index);
        double removedChance = MainRegistry.customDropChances.remove(index);
        Integer removedMinY = MainRegistry.customDropMinYs.remove(index);
        Integer removedMaxY = MainRegistry.customDropMaxYs.remove(index);

        MainRegistry.saveCustomDrops(); // Save after removing

        sender.addChatMessage(new ChatComponentText("Removed custom drop: " + removed.getDisplayName()
                + " (Chance: " + removedChance + ", Y: " + formatYRange(removedMinY, removedMaxY) + ")"));
    }

    private void handleAddCommand(ICommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 3) {
            sendUsage(sender);
            return;
        }

        double rarity;
        try {
            rarity = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText("Invalid rarity. Must be a number, e.g. 0.25"));
            sendUsage(sender);
            return;
        }

        Integer minY = null;
        Integer maxY = null;

        if (args.length == 3) {
            try {
                minY = Integer.valueOf(args[1]);
                maxY = Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText("Invalid Y levels. minY and maxY must be whole numbers, e.g. 5 30"));
                sendUsage(sender);
                return;
            }

            if (minY.intValue() > maxY.intValue()) {
                sender.addChatMessage(new ChatComponentText("Invalid Y range. minY must be less than or equal to maxY."));
                return;
            }
        }

        if (!(sender instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("Only a player can run this command in-game."));
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;
        ItemStack heldItem = player.getHeldItem();

        if (heldItem == null) {
            sender.addChatMessage(new ChatComponentText("You must be holding an item or block to set the custom drop."));
            return;
        }

        MainRegistry.customDrops.add(heldItem.copy());
        MainRegistry.customDropChances.add(rarity);
        MainRegistry.customDropMinYs.add(minY);
        MainRegistry.customDropMaxYs.add(maxY);

        MainRegistry.saveCustomDrops(); // Save after adding

        sender.addChatMessage(new ChatComponentText("Stone drop added! "
                + " Item: " + heldItem.getDisplayName()
                + " | Rarity (chance): " + rarity
                + " | Y levels: " + formatYRange(minY, maxY)));
    }

    private void sendUsage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
        sender.addChatMessage(new ChatComponentText("Hold the item/block you want to drop, then run /stonedrop <rarity> to allow it at any Y level."));
        sender.addChatMessage(new ChatComponentText("To limit it to a Y range, run /stonedrop <rarity> <minY> <maxY>, e.g. /stonedrop 0.02 5 30."));
    }

    private String formatYRange(Integer minY, Integer maxY) {
        if (minY == null || maxY == null) {
            return "any";
        }

        return minY + "-" + maxY;
    }
}
