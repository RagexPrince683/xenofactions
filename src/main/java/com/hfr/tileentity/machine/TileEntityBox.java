package com.hfr.tileentity.machine;

import com.hfr.main.MainRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityBox extends TileEntityMachineBase {
	
	public int mold;

	public TileEntityBox() {
		super(15);
	}

	@Override
	public String getName() {
		return "container.box";
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public void updateEntity() {
		
		if(!worldObj.isRemote) {
			
			mold++;
			
			if(mold >= MainRegistry.mold) {
				worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
			}
			
			this.updateGauge(mold, 0, 15);
		}
	}
	
	public void processGauge(int val, int id) {
		
		this.mold = val;
	}

	public int getMoldScaled(int i) {
		return (mold * i) / MainRegistry.mold;
	}

}
