package com.hbm.tileentity.machine;

import com.hbm.blocks.IRadResistantBlock;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CompatEnergyControl;

import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.tile.IInfoProviderEC;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMoltenSaltReactor extends TileEntityMachineBase implements IFluidStandardTransceiver, IFluidCopiable, IInfoProviderEC {

	public static final int SALT_CAPACITY = 16_000;
	public static final int HOT_SALT_CAPACITY = 16_000;
	public static final int SALT_PER_TICK = 4;
	public static final int MAX_CORROSION = 100;

	public FluidTank[] tanks;
	public int output;
	public int corrosion;

	public TileEntityMoltenSaltReactor() {
		super(0);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.THORIUM_SALT, SALT_CAPACITY);
		tanks[1] = new FluidTank(Fluids.THORIUM_SALT_HOT, HOT_SALT_CAPACITY);
	}

	@Override
	public String getName() {
		return "container.moltenSaltReactor";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			this.output = 0;
			this.updateConnections();
			this.processSalt();
			this.updateCorrosionAndRadiation();
			this.sendFluidToAll(tanks[1], this);

			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("output", output);
			data.setInteger("corrosion", corrosion);
			tanks[0].writeToNBT(data, "salt");
			tanks[1].writeToNBT(data, "hotSalt");
			this.networkPack(data, 50);
		}
	}

	protected void updateConnections() {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			this.trySubscribe(tanks[0].getTankType(), worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
		}
	}

	protected void processSalt() {
		int operations = Math.min(SALT_PER_TICK, tanks[0].getFill());
		operations = Math.min(operations, tanks[1].getMaxFill() - tanks[1].getFill());

		if(operations > 0) {
			tanks[0].setFill(tanks[0].getFill() - operations);
			tanks[1].setFill(tanks[1].getFill() + operations);
			this.output = operations;
		}
	}

	protected void updateCorrosionAndRadiation() {
		boolean active = this.output > 0 || tanks[1].getFill() > 0;
		if(!active) return;

		boolean shielded = this.isShielded();
		if(this.output > 0 && worldObj.getTotalWorldTime() % (shielded ? 200 : 40) == 0 && corrosion < MAX_CORROSION) {
			corrosion++;
		}

		if(!shielded && worldObj.getTotalWorldTime() % 20 == 0) {
			float rad = 0.25F + corrosion * 0.05F + tanks[1].getFill() / 16_000F;
			ChunkRadiationManager.proxy.incrementRad(worldObj, xCoord, yCoord, zCoord, rad);
		}

		if(corrosion > 0 && worldObj.getTotalWorldTime() % Math.max(20, 220 - corrosion * 2) == 0) {
			this.leakSalt(shielded ? 1 : 2);
		}
	}

	protected void leakSalt(int amount) {
		int hotLeak = Math.min(amount, tanks[1].getFill());
		tanks[1].setFill(tanks[1].getFill() - hotLeak);
		int coldLeak = Math.min(amount - hotLeak, tanks[0].getFill());
		tanks[0].setFill(tanks[0].getFill() - coldLeak);
	}

	public boolean isShielded() {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Block block = worldObj.getBlock(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			if(!isShieldBlock(block)) return false;
		}
		return true;
	}

	protected boolean isShieldBlock(Block block) {
		return block instanceof IRadResistantBlock ||
				block == ModBlocks.block_lead ||
				block == ModBlocks.block_boron ||
				block == ModBlocks.concrete_super ||
				block == ModBlocks.concrete_asbestos ||
				block == ModBlocks.brick_concrete ||
				block == ModBlocks.brick_ducrete ||
				//block == ModBlocks.brick_concrete_cracked ||
				block == ModBlocks.concrete_colored ||
				block == ModBlocks.concrete_colored_ext ||
				block == ModBlocks.brick_concrete_mossy ||
				block == ModBlocks.concrete_pillar ||
				block == ModBlocks.machine_msr_input ||
				block == ModBlocks.machine_msr_output;
	}

	@Override
	public void networkUnpack(NBTTagCompound data) {
		super.networkUnpack(data);
		this.output = data.getInteger("output");
		this.corrosion = data.getInteger("corrosion");
		tanks[0].readFromNBT(data, "salt");
		tanks[1].readFromNBT(data, "hotSalt");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.output = nbt.getInteger("output");
		this.corrosion = nbt.getInteger("corrosion");
		tanks[0].readFromNBT(nbt, "salt");
		tanks[1].readFromNBT(nbt, "hotSalt");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("output", output);
		nbt.setInteger("corrosion", corrosion);
		tanks[0].writeToNBT(nbt, "salt");
		tanks[1].writeToNBT(nbt, "hotSalt");
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0] };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank getTankToPaste() {
		return tanks[0];
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setBoolean(CompatEnergyControl.B_ACTIVE, this.output > 0);
		data.setDouble(CompatEnergyControl.D_OUTPUT_MB, this.output);
	}
}
