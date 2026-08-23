package com.hfr.command;

import com.hfr.blocks.ModBlocks;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

/** Gives a regular player one generic Wall Art controller block. */
public class CommandXWallArt extends CommandBase {
  @Override
  public int getRequiredPermissionLevel() {
    return 0;
  }

  @Override
  public String getCommandName() {
    return "xwallart";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "/xwallart";
  }

  @Override
  public void processCommand(ICommandSender sender, String[] args) {
    if (!(sender instanceof EntityPlayerMP))
      return;
    EntityPlayerMP player = (EntityPlayerMP)sender;
    ItemStack wallArt = new ItemStack(ModBlocks.wallImageBlock, 1);
    if (!player.inventory.addItemStackToInventory(wallArt) &&
        wallArt.stackSize > 0) {
      player.dropPlayerItemWithRandomChoice(wallArt, false);
    }
    player.addChatMessage(
        new ChatComponentTranslation("wallart.command.give"));
  }
}
