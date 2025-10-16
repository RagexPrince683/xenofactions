package com.hfr.tileentity.machine;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityMachineDistillery extends TileEntityMachineBase {

	public TileEntityMachineDistillery() {
		super(4);
	}

	@Override
	public String getName() {
		return "container.machineDistillery";
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public void updateEntity() {
	}
}
