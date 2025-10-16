package com.hfr.tileentity.machine;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityMachineCrusher extends TileEntityMachineBase {

	public TileEntityMachineCrusher() {
		super(4);
	}

	@Override
	public String getName() {
		return "container.machineCrusher";
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public void updateEntity() {
	}
}
