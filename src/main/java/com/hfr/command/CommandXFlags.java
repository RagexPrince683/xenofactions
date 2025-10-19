package com.hfr.command;

import com.hfr.blocks.ModBlocks;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

public class CommandXFlags extends CommandBase {
    //gives players conquest flags
    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Allows all players to execute the command
        //1WRONG
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender)
    { //2THIS DOES!!! well probably plus that, whatever!
        return true;
    }


    @Override
    public String getCommandName() {
        return "xflags";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/xflags";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP && CommandClowderAdmin.WARENABLED ) { //todone? add central toggle
            EntityPlayerMP player = (EntityPlayerMP) sender;
            ItemStack map = new ItemStack(ModBlocks.clowder_conquerer, 64);
            player.inventory.addItemStackToInventory(map);
            player.addChatMessage(new ChatComponentText("Conquest Flags Given!"));
        }
    }
}
