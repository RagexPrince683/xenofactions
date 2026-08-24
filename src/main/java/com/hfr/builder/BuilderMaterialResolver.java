package com.hfr.builder;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;

/** Single conservative block-to-item mapping; unknown blocks are never free. */
public final class BuilderMaterialResolver {
	private BuilderMaterialResolver(){}
	public static ItemStack resolve(Block b,int meta){
		if(b==Blocks.wooden_door)return new ItemStack(Items.wooden_door);
		if(b==Blocks.iron_door)return new ItemStack(Items.iron_door);
		if(b==Blocks.bed)return new ItemStack(Items.bed);
		Item item=Item.getItemFromBlock(b); if(item==null)return null;
		int damage=(b==Blocks.log||b==Blocks.log2||b==Blocks.leaves||b==Blocks.leaves2)?meta&3:meta;
		return new ItemStack(item,1,damage);
	}
}
