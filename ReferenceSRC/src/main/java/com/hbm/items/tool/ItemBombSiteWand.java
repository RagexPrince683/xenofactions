package com.hbm.items.tool;

import java.util.List;

import com.hbm.saveddata.BombSiteSavedData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemBombSiteWand extends Item {

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add("Creative-only item");
		list.add(EnumChatFormatting.YELLOW + "Right-click two corners to add a bomb site.");
		list.add(EnumChatFormatting.YELLOW + "Sneak-right-click inside a site to remove it.");

		if(stack.stackTagCompound != null && stack.stackTagCompound.getBoolean("hasPos")) {
			list.add(EnumChatFormatting.AQUA + "First corner: " + stack.stackTagCompound.getInteger("x") + ", " + stack.stackTagCompound.getInteger("y") + ", " + stack.stackTagCompound.getInteger("z"));
		} else {
			list.add(EnumChatFormatting.AQUA + "No first corner set.");
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if(stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}

		if(world.isRemote) {
			return true;
		}

		if(player.isSneaking()) {
			int removed = BombSiteSavedData.getData(world).removeSitesAt(x, y, z);
			stack.stackTagCompound.setBoolean("hasPos", false);
			player.addChatMessage(new ChatComponentText(removed > 0 ? "Removed " + removed + " bomb site(s)." : "No bomb site contains this block."));
			return true;
		}

		if(!stack.stackTagCompound.getBoolean("hasPos")) {
			stack.stackTagCompound.setInteger("x", x);
			stack.stackTagCompound.setInteger("y", y);
			stack.stackTagCompound.setInteger("z", z);
			stack.stackTagCompound.setBoolean("hasPos", true);
			player.addChatMessage(new ChatComponentText("Bomb site first corner set at " + x + ", " + y + ", " + z + "."));
		} else {
			int startX = stack.stackTagCompound.getInteger("x");
			int startY = stack.stackTagCompound.getInteger("y");
			int startZ = stack.stackTagCompound.getInteger("z");
			boolean added = BombSiteSavedData.getData(world).addSite(startX, startY, startZ, x, y, z);
			stack.stackTagCompound.setBoolean("hasPos", false);
			player.addChatMessage(new ChatComponentText(added ? "Bomb site added from " + startX + ", " + startY + ", " + startZ + " to " + x + ", " + y + ", " + z + "." : "That bomb site already exists."));
		}

		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}

		if(player.isSneaking()) {
			stack.stackTagCompound.setBoolean("hasPos", false);

			if(!world.isRemote) {
				player.addChatMessage(new ChatComponentText("Cleared pending bomb site corner."));
			}
		}

		return stack;
	}
}
