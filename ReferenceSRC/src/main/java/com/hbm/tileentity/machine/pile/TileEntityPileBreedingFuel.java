package com.hbm.tileentity.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;

import api.hbm.block.IPileNeutronReceiver;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityPileBreedingFuel extends TileEntityPileBase implements IPileNeutronReceiver {

	public int neutrons;
	public int lastNeutrons;
	public int progress;
	public static final int maxProgress =
		GeneralConfig.enable528 ? 120000 : 80000;

	public double heat;
	public static final double maxHeat = 500D;

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			react();

			if(this.progress >= this.maxProgress) {
				worldObj.setBlock(xCoord, yCoord, zCoord, ModBlocks.block_graphite_tritium, this.getBlockMetadata(), 3);
			}
		}
	}

	private void react() {

		this.lastNeutrons = this.neutrons;

		double efficiency =
			GeneralConfig.enable528 ? 0.12D : 0.20D;

		double heatPenalty =
			1D - Math.min(
				0.4D,
				(heat / maxHeat) * 0.4D);

		int absorbed =
			(int)(this.neutrons *
				efficiency *
				heatPenalty);

		this.progress += absorbed;

		heat += absorbed * 0.015D;
		heat *= 0.995D;

		if(lastNeutrons <= 0) {
			this.neutrons = 0;
			return;
		}

		this.neutrons = 0;

		int secondary =
			Math.min(4,
					 Math.max(1, lastNeutrons / 8));

		for(int i = 0; i < secondary; i++) {
			this.castRay(1, 3);
		}
	}

	@Override
	public void receiveNeutrons(int n) {
		this.neutrons += n;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.heat = nbt.getDouble("heat");
		this.progress = nbt.getInteger("progress");
		this.neutrons = nbt.getInteger("neutrons");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setDouble("heat", this.heat);
		nbt.setInteger("progress", this.progress);
		nbt.setInteger("neutrons", this.neutrons);
	}
}
