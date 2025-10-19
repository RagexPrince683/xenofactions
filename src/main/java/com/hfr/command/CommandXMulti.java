package com.hfr.command;

import com.hfr.items.ModItems;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

public class CommandXMulti extends CommandBase {

    //everyone should be able to use this crap
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
        return "xmulti";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/xmulti";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            ItemStack map = new ItemStack(ModItems.mult_alloy, 1);
            player.inventory.addItemStackToInventory(map);
            player.addChatMessage(new ChatComponentText("Giving you a multitool!"));
        }
    }

}
