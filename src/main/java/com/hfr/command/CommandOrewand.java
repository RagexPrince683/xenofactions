package com.hfr.command;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import java.util.Random;

public class CommandOrewand extends CommandBase {

   @Override
   public String getCommandName() {
       return "orewand";
   }

   @Override
   public String getCommandUsage(ICommandSender sender) {
       return "/orewand <percentage> <veinAmount>";
   }

   @Override
   public void processCommand(ICommandSender sender, String[] args) {
       if (!(sender instanceof EntityPlayer)) {
           sender.addChatMessage(new ChatComponentText("Command can only be used by a player."));
           return;
       }
   //extra } here because this was a pain in my side and im not gonna fix it
   }

   //    if (args.length != 2) {
   //        sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
   //        return;
   //    }

   //    EntityPlayer player = (EntityPlayer) sender;
   //    World world = player.worldObj;
   //    ItemStack heldItem = player.getCurrentEquippedItem();

   //    if (heldItem == null) {
   //        player.addChatMessage(new ChatComponentText("You must be holding a block or item."));
   //        return;
   //    }

   //    try {
   //        float percentage = Float.parseFloat(args[0]) / 100.0f;
   //        int veinAmount = Integer.parseInt(args[1]);

   //        if (percentage < 0 || percentage > 1 || veinAmount < 1) {
   //            throw new NumberFormatException();
   //        }

   //        generateVeins(world, player, heldItem, percentage, veinAmount);
   //        player.addChatMessage(new ChatComponentText("Veins generated!"));

   //    } catch (NumberFormatException e) {
   //        player.addChatMessage(new ChatComponentText("Invalid arguments. Percentage should be a number between 0 and 100, and vein amount should be a positive integer."));
   //    }
   //}

   //private void generateVeins(World world, EntityPlayer player, ItemStack heldItem, float percentage, int veinAmount) {
   //    Random random = new Random();
   //    for (int i = 0; i < veinAmount; i++) {
   //        if (random.nextFloat() < percentage) {
   //            int x = (int) player.posX + random.nextInt(16) - 8;
   //            int y = world.getTopSolidOrLiquidBlock(x, (int) player.posZ) + random.nextInt(30);
   //            int z = (int) player.posZ + random.nextInt(16) - 8;

   //            world.setBlock(x, y, z, Block.getBlockFromItem(heldItem.getItem()));
   //        }
   //    }
   //}
}
