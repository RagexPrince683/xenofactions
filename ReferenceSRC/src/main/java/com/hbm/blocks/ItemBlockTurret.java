package com.hbm.blocks;

import java.util.List;

import com.hbm.blocks.turret.TurretBase;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockTurret extends ItemBlock {

	public ItemBlockTurret(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		Block block = Block.getBlockFromItem(stack.getItem());

		if(block instanceof TurretBase) {
			((TurretBase) block).addInformation(stack, player, list, ext);
		}
	}
}
