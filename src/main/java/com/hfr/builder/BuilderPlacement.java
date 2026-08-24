package com.hfr.builder;

import com.hfr.blocks.clowder.*;
import com.hfr.tileentity.clowder.ITerritoryProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** The sole server-side gateway for Builder world edits. */
public final class BuilderPlacement {
	private BuilderPlacement(){}
	public static boolean protectedBlock(World w,int x,int y,int z){
		Block b=w.getBlock(x,y,z); TileEntity te=w.getTileEntity(x,y,z);
		return b==Blocks.bedrock||b instanceof Flag||b instanceof FlagBig||b instanceof Conquerer||te instanceof ITerritoryProvider;
	}
	public static boolean breakBlock(World w,int x,int y,int z,java.util.UUID faction){
		if(w.isRemote||protectedBlock(w,x,y,z)||!BuilderTerritory.mayChange(w,x,z,faction,true))return false;
		return w.func_147480_a(x,y,z,true);
	}
	public static boolean place(World w,int x,int y,int z,Block block,int meta,java.util.UUID faction){
		if(w.isRemote||block==null||block==Blocks.command_block||block==Blocks.bedrock||!BuilderTerritory.mayChange(w,x,z,faction,false))return false;
		Block old=w.getBlock(x,y,z); if(old!=Blocks.air&&old!=block)return false;
		return w.setBlock(x,y,z,block,meta&15,3);
	}
}
