package com.hfr.items;

import com.hfr.data.OutOfBoundsData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.List;

public class ItemOutOfBoundsWand extends Item {

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        if (!player.canCommandSenderUseCommand(4, "outofboundswand")) {
            player.addChatMessage(new ChatComponentText("You do not have permission to define out of bounds regions."));
            return true;
        }

        if (!stack.hasTagCompound()) {
            stack.stackTagCompound = new NBTTagCompound();
            stack.stackTagCompound.setInteger("dim", player.dimension);
            stack.stackTagCompound.setInteger("xCoord", x);
            stack.stackTagCompound.setInteger("zCoord", z);
            player.addChatMessage(new ChatComponentText("Out of bounds position 1 set at X " + x + ", Z " + z + ". Y is ignored."));
            world.playSoundAtEntity(player, "hfr:item.techBoop", 1.0F, 1.0F);
            return true;
        }

        int dim = stack.stackTagCompound.getInteger("dim");
        if (dim != player.dimension) {
            player.addChatMessage(new ChatComponentText("Position 2 must be in the same dimension as position 1."));
            return true;
        }

        OutOfBoundsData.addRegion(
                world,
                dim,
                stack.stackTagCompound.getInteger("xCoord"),
                stack.stackTagCompound.getInteger("zCoord"),
                x,
                z
        );
        player.addChatMessage(new ChatComponentText("Out of bounds region saved to out_of_bounds_regions.txt. Y is ignored."));
        world.playSoundAtEntity(player, "hfr:item.techBleep", 1.0F, 1.0F);
        stack.stackTagCompound = null;
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
        if (stack.stackTagCompound != null) {
            list.add("POS 1:");
            list.add("Dim: " + stack.stackTagCompound.getInteger("dim"));
            list.add("X: " + stack.stackTagCompound.getInteger("xCoord"));
            list.add("Z: " + stack.stackTagCompound.getInteger("zCoord"));
            list.add("Y is ignored.");
        } else {
            list.add("Select two corners of an out of bounds region.");
            list.add("Y is ignored.");
        }
    }
}
