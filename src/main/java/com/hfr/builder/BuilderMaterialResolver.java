package com.hfr.builder;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import com.hfr.util.XFLog;

/** Single conservative block-to-item mapping; unknown blocks are never free. */
public final class BuilderMaterialResolver {
	private BuilderMaterialResolver(){}
	public static ItemStack resolve(Block b,int meta){
		if(b==Blocks.wooden_door)return new ItemStack(Items.wooden_door);
		if(b==Blocks.iron_door)return new ItemStack(Items.iron_door);
		if(b==Blocks.bed)return new ItemStack(Items.bed);
		if(b==Blocks.standing_sign||b==Blocks.wall_sign)return new ItemStack(Items.sign);
		Item item=Item.getItemFromBlock(b); if(item==null)return null;
		// damageDropped is the 1.7.10 block contract for the inventory variant. It
		// strips placement-only state (stairs, rails, switches, trapdoors, etc.)
		// while retaining genuine variants (wool, wood, stone, slabs and stains).
		int damage=b.damageDropped(meta);
		ItemStack result=new ItemStack(item,1,damage);
		XFLog.debug("Builder material "+Block.blockRegistry.getNameForObject(b)+" blockMeta="+meta+" -> "+Item.itemRegistry.getNameForObject(item)+" itemDamage="+damage);
		return result;
	}
	public static boolean matches(ItemStack a,ItemStack b){return a!=null&&b!=null&&new BuilderMaterialKey(a).equals(new BuilderMaterialKey(b));}
}
