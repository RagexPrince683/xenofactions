package com.hbm.tileentity.machine.pile;

import api.hbm.block.IPileNeutronReceiver;
import com.hbm.blocks.machine.pile.BlockGraphiteNeutronDetector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPileNeutronDetector extends TileEntity implements IPileNeutronReceiver {

	public int lastNeutrons;
	public int neutrons;
	public int maxNeutrons = 10;
	public int averagedNeutrons;
	public int cooldown;

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			// smooth detector readings (primitive instrumentation realism)
			this.averagedNeutrons =
				(int)(this.averagedNeutrons * 0.8D +
					this.neutrons * 0.2D);

			// rod movement delay
			if(this.cooldown > 0)
				this.cooldown--;

			int insertThreshold = this.maxNeutrons;
			int retractThreshold = this.maxNeutrons - 4;

			boolean rodsInserted =
				(this.getBlockMetadata() & 8) > 0;

			// insert rods
			if(this.averagedNeutrons >= insertThreshold
				&& rodsInserted
				&& this.cooldown <= 0) {

				((BlockGraphiteNeutronDetector)
					worldObj.getBlock(xCoord, yCoord, zCoord))
					.triggerRods(worldObj, xCoord, yCoord, zCoord);

				this.cooldown = 20;
			}

			// retract rods
			if(this.averagedNeutrons <= retractThreshold
				&& this.lastNeutrons <= retractThreshold
				&& !rodsInserted
				&& this.cooldown <= 0) {

				((BlockGraphiteNeutronDetector)
					worldObj.getBlock(xCoord, yCoord, zCoord))
					.triggerRods(worldObj, xCoord, yCoord, zCoord);

				this.cooldown = 20;
			}

			this.lastNeutrons = this.averagedNeutrons;
			this.neutrons = 0;
		}
	}

	@Override
	public void receiveNeutrons(int n) {
		this.neutrons += n;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("maxNeutrons", this.maxNeutrons);
		nbt.setInteger("avgNeutrons", this.averagedNeutrons);
		nbt.setInteger("cooldown", this.cooldown);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.maxNeutrons = nbt.getInteger("maxNeutrons");
		this.averagedNeutrons =
			nbt.getInteger("avgNeutrons");
		this.cooldown =
			nbt.getInteger("cooldown");
	}
}
