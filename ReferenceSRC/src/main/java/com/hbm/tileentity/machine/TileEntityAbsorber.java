package com.hbm.tileentity.machine;

import com.hbm.blocks.generic.BlockAbsorber;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityAbsorber extends TileEntity {

	public float storedRad = 0F;
	public float getMaxRad() {

		if(worldObj == null)
			return 10000F;

		if(worldObj.getBlock(xCoord, yCoord, zCoord)
			instanceof BlockAbsorber) {

			BlockAbsorber absorber =
				(BlockAbsorber) worldObj.getBlock(
					xCoord,
					yCoord,
					zCoord
				);

			float rate = absorber.getAbsorbRate();

			return 2000F + (rate * rate * 25F);
		}

		return 10000F;
	}
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setFloat("storedRad", storedRad);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		storedRad = nbt.getFloat("storedRad");
	}

	public boolean isFull() {
		return storedRad >= getMaxRad();
	}

	public float remainingCapacity() {
		return getMaxRad() - storedRad;
	}
}
