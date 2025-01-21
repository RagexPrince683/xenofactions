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
        return "/stonedrop <rarity> OR /stonedrop list OR /stonedrop remove [index]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // Check argument count.
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
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
            sender.addChatMessage(new ChatComponentText(i + 1 + ". " + stack.getDisplayName() + " (Chance: " + MainRegistry.customDropChances.get(i) + ")"));
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

        sender.addChatMessage(new ChatComponentText("Removed custom drop: " + removed.getDisplayName() + " (Chance: " + removedChance + ")"));
    }

    private void handleAddCommand(ICommandSender sender, String[] args) {
        // Attempt to parse rarity (drop chance).
        double rarity;
        try {
            rarity = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText("Invalid rarity. Must be a number, e.g. 0.25"));
            return;
        }

        // Ensure the command is run by a player (not console, etc.).
        if (!(sender instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("Only a player can run this command in-game."));
            return;
        }

        // Get the player and the item in their hand.
        EntityPlayerMP player = (EntityPlayerMP) sender;
        ItemStack heldItem = player.getHeldItem();

        if (heldItem == null) {
            sender.addChatMessage(new ChatComponentText("You must be holding an item or block to set the custom drop."));
            return;
        }

        // Add the custom drop to the registry.
        MainRegistry.customDropStack = heldItem.copy();
        MainRegistry.customDropChance = rarity;

        // Send feedback to player
        sender.addChatMessage(new ChatComponentText("Stone drop added! "
                + " Item: " + heldItem.getDisplayName()
                + " | Rarity (chance): " + rarity));
    }


}
