package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.fluid.IFluidStandardSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMoltenSaltReactorPort extends TileEntityLoadedBase implements IFluidStandardReceiver, IFluidStandardSender {

	private boolean input;
	public FluidTank tank;

	public TileEntityMoltenSaltReactorPort() {
		this(true);
	}

	public TileEntityMoltenSaltReactorPort(boolean input) {
		this.input = input;
		this.tank = new FluidTank(input ? Fluids.THORIUM_SALT : Fluids.THORIUM_SALT_HOT, 16_000);
	}

	public boolean isInput() {
		return input;
	}

	public TileEntityMoltenSaltReactor getReactor() {
		if(worldObj == null) return null;
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			if(tile instanceof TileEntityMoltenSaltReactor) return (TileEntityMoltenSaltReactor) tile;
		}
		return null;
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			if(input) {
				this.subscribeToAllAround(tank.getTankType(), this);
				this.pushInputToReactor();
			} else {
				this.pullOutputFromReactor();
				this.sendFluidToAll(tank, this);
			}
		}
	}

	protected void pushInputToReactor() {
		TileEntityMoltenSaltReactor reactor = this.getReactor();
		if(reactor != null) {
			int fill = tank.getFill();
			long overshoot = reactor.transferFluid(tank.getTankType(), tank.getPressure(), fill);
			tank.setFill((int) overshoot);
		}
	}

	protected void pullOutputFromReactor() {
		TileEntityMoltenSaltReactor reactor = this.getReactor();
		if(reactor != null) {
			FluidTank hotTank = reactor.tanks[1];
			int transfer = Math.min(tank.getMaxFill() - tank.getFill(), hotTank.getFill());
			if(transfer > 0) {
				hotTank.setFill(hotTank.getFill() - transfer);
				tank.setFill(tank.getFill() + transfer);
			}
		}
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long amount) {
		if(!input || tank.getTankType() != type || tank.getPressure() != pressure) return amount;
		tank.setFill(tank.getFill() + (int) amount);
		if(tank.getFill() > tank.getMaxFill()) {
			long overshoot = tank.getFill() - tank.getMaxFill();
			tank.setFill(tank.getMaxFill());
			return overshoot;
		}
		return 0;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		if(!input || tank.getTankType() != type || tank.getPressure() != pressure) return 0;
		return tank.getMaxFill() - tank.getFill();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.input = nbt.getBoolean("input");
		this.tank.setTankType(input ? Fluids.THORIUM_SALT : Fluids.THORIUM_SALT_HOT);
		this.tank.readFromNBT(nbt, "tank");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("input", input);
		this.tank.writeToNBT(nbt, "tank");
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return input ? new FluidTank[] { tank } : new FluidTank[0];
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return input ? new FluidTank[0] : new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}
}
