package com.hfr.tileentity.machine;

import com.hfr.tileentity.machine.TileEntityMachineBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityRift extends TileEntityMachineBase {

	public TileEntityRift() {
		super(3);
	}

	@Override
	public String getName() {
		return "container.rift";
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public void updateEntity() { }

}
