package com.hfr.builder;

import com.hfr.blocks.clowder.*;
import com.hfr.tileentity.clowder.ITerritoryProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** The sole server-side gateway for Builder world edits. */
public final class BuilderPlacement {
	public enum Result { SUCCESS, TARGET_OCCUPIED, PROTECTED_BLOCK, INVALID_TERRITORY, PLACEMENT_REJECTED, BREAK_REJECTED, INVALID_Y, UNSUPPORTED_BLOCK }
	private BuilderPlacement(){}
	public static boolean protectedBlock(World w,int x,int y,int z){
		Block b=w.getBlock(x,y,z); TileEntity te=w.getTileEntity(x,y,z);
		return b==Blocks.bedrock||b instanceof Flag||b instanceof FlagBig||b instanceof Conquerer||te instanceof ITerritoryProvider;
	}
	public static boolean breakBlock(World w,int x,int y,int z,java.util.UUID faction){
		return breakBlockResult(w,x,y,z,faction)==Result.SUCCESS;
	}
	public static Result breakBlockResult(World w,int x,int y,int z,java.util.UUID faction){if(y<0||y>=w.getHeight())return Result.INVALID_Y;if(protectedBlock(w,x,y,z))return Result.PROTECTED_BLOCK;if(w.isRemote||!BuilderTerritory.mayChange(w,x,z,faction,true))return Result.INVALID_TERRITORY;return w.func_147480_a(x,y,z,true)?Result.SUCCESS:Result.BREAK_REJECTED;}
	public static boolean place(World w,int x,int y,int z,Block block,int meta,java.util.UUID faction){
		return placeResult(w,x,y,z,block,meta,faction)==Result.SUCCESS;
	}
	public static Result placeResult(World w,int x,int y,int z,Block block,int meta,java.util.UUID faction){if(y<0||y>=w.getHeight())return Result.INVALID_Y;if(block==null||block==Blocks.command_block||block==Blocks.bedrock)return Result.UNSUPPORTED_BLOCK;if(w.isRemote||!BuilderTerritory.mayChange(w,x,z,faction,false))return Result.INVALID_TERRITORY;Block old=w.getBlock(x,y,z);if(old!=Blocks.air&&old!=block)return Result.TARGET_OCCUPIED;return w.setBlock(x,y,z,block,meta&15,3)?Result.SUCCESS:Result.PLACEMENT_REJECTED;}
}
