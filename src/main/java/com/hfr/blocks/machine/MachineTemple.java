package com.hfr.blocks.machine;

import com.hfr.blocks.BlockDummyable;
import com.hfr.blocks.ModBlocks;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.main.MainRegistry;
import com.hfr.tileentity.machine.TileEntityMachineTemple;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class MachineTemple extends BlockDummyable {

	public MachineTemple(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12)
			return new TileEntityMachineTemple();
		
		return null;
	}
	
	int[] dim = new int[] { 5, 0, 6, 6, 8, 8 };

	@Override
	public int[] getDimensions() {
		return dim;
	}

	@Override
	public int getOffset() {
		return 6;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if(meta >= 12) {
			Ownership owner = ClowderTerritory.getOwnerFromInts(x, z);
			TileEntity entity = world.getTileEntity(x, y, z);
			if(owner != null && owner.zone == Zone.FACTION && entity instanceof TileEntityMachineTemple) {
				TileEntityMachineTemple temple = (TileEntityMachineTemple)entity;
				if(temple.owner != null)
					temple.owner.addPrestigeGen(-Clowder.TempleRate(), world);
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			int[] pos = this.findCore(world, x, y, z);
			
			if(pos == null)
				return false;
			
			FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_temple, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return true;
		}
	}

}
