package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import com.hfr.main.MainRegistry;

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

        // Now store these in MainRegistry, or wherever you keep config data.
        // If you only want one custom drop at a time:
        MainRegistry.customDropChance = rarity;
        MainRegistry.customDropStack  = heldItem.copy();

        // Send feedback to player
        sender.addChatMessage(new ChatComponentText("Stone drop set! "
                + " Item: " + heldItem.getDisplayName()
                + " | Rarity (chance): " + rarity));
    }
}
