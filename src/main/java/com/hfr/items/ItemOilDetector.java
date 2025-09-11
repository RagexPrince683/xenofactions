package com.hfr.items;

import java.util.List;

import com.hfr.blocks.ModBlocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemOilDetector extends Item {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		list.add("USE RTM TO SCAN FOR OIL DEPOSITS");
		list.add("Scanner can only detect larger deposits!");
	}

}
