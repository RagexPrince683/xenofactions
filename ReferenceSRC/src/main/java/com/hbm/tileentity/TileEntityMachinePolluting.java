package com.hbm.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.ThreeInts;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;

import api.hbm.fluid.IFluidUser;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class TileEntityMachinePolluting extends TileEntityMachineBase implements IFluidUser {

	public FluidTank smoke;
	public FluidTank smoke_leaded;
	public FluidTank smoke_poison;

	public TileEntityMachinePolluting(int scount, int buffer) {
		super(scount);
		smoke = new FluidTank(Fluids.SMOKE, buffer);
		smoke_leaded = new FluidTank(Fluids.SMOKE_LEADED, buffer);
		smoke_poison = new FluidTank(Fluids.SMOKE_POISON, buffer);
	}

	public void pollute(PollutionType type, float amount) {
		FluidTank tank = type == PollutionType.SOOT ? smoke : type == PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

		int fluidAmount = (int) Math.ceil(amount * 100);
		tank.setFill(tank.getFill() + fluidAmount);

		if(tank.getFill() > tank.getMaxFill()) {
			int overflow = tank.getFill() - tank.getMaxFill();
			tank.setFill(tank.getMaxFill());
			PollutionHandler.incrementPollution(worldObj, xCoord, yCoord, zCoord, type, overflow / 800F);

			if(worldObj.rand.nextInt(3) == 0) worldObj.playSoundEffect(xCoord, yCoord, zCoord, "random.fizz", 0.1F, 1.5F);
		}
	}
	public void pollute(FluidType type, FluidTrait.FluidReleaseType release, float amount) {
		FluidTank tank;
		FT_Polluting trait = type.getTrait(FT_Polluting.class);
		if(trait == null) return;
		if(release == FluidTrait.FluidReleaseType.VOID) return;

		HashMap<PollutionType, Float> map = release == FluidTrait.FluidReleaseType.BURN ? trait.burnMap : trait.releaseMap;

		for(Map.Entry<PollutionType, Float> entry : map.entrySet()) {

			tank = entry.getKey() == PollutionType.SOOT ? smoke : entry.getKey() == PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;
			int fluidAmount = (int) Math.ceil(entry.getValue() * amount * 100);
			tank.setFill(tank.getFill() + fluidAmount);

			if (tank.getFill() > tank.getMaxFill()) {
				int overflow = tank.getFill() - tank.getMaxFill();
				tank.setFill(tank.getMaxFill());
				PollutionHandler.incrementPollution(worldObj, xCoord, yCoord, zCoord, entry.getKey(), overflow / 800F);

				if (worldObj.rand.nextInt(3) == 0)
					worldObj.playSoundEffect(xCoord, yCoord, zCoord, "random.fizz", 0.1F, 1.5F);
			}
		}
	}

	public void sendSmoke(int x, int y, int z, ForgeDirection dir) {
		if(this.smoke.getFill() > 0) this.sendFluid(smoke, worldObj, x, y, z, dir);
		if(this.smoke_leaded.getFill() > 0) this.sendFluid(smoke_leaded, worldObj, x, y, z, dir);
		if(this.smoke_poison.getFill() > 0) this.sendFluid(smoke_poison, worldObj, x, y, z, dir);
	}

	public FluidTank[] getSmokeTanks() {
		return new FluidTank[] {smoke, smoke_leaded, smoke_poison};
	}

	/**
	 * Returns whether water is touching an exposed side or top of this machine.
	 * For dummyable multiblocks, every part belonging to this core is checked. The
	 * underside is deliberately excluded so machines can still sit above water.
	 */
	protected boolean isWaterlogged() {
		Block block = getBlockType();
		if(!(block instanceof BlockDummyable)) return isWaterTouching(xCoord, yCoord, zCoord);

		BlockDummyable dummyable = (BlockDummyable) block;
		ArrayDeque<int[]> partsToCheck = new ArrayDeque<int[]>();
		Set<ThreeInts> checkedParts = new HashSet<ThreeInts>();
		partsToCheck.add(new int[] {xCoord, yCoord, zCoord});

		while(!partsToCheck.isEmpty()) {
			int[] part = partsToCheck.removeFirst();
			if(!checkedParts.add(new ThreeInts(part[0], part[1], part[2]))) continue;

			if(isWaterTouching(part[0], part[1], part[2])) return true;

			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				int x = part[0] + direction.offsetX;
				int y = part[1] + direction.offsetY;
				int z = part[2] + direction.offsetZ;
				if(worldObj.getBlock(x, y, z) != block) continue;

				int[] core = dummyable.findCore(worldObj, x, y, z);
				if(core != null && core[0] == xCoord && core[1] == yCoord && core[2] == zCoord) {
					partsToCheck.add(new int[] {x, y, z});
				}
			}
		}

		return false;
	}

	private boolean isWaterTouching(int x, int y, int z) {
		for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if(direction == ForgeDirection.DOWN) continue;
			if(worldObj.getBlock(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ).getMaterial() == Material.water) return true;
		}

		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		smoke.readFromNBT(nbt, "smoke0");
		smoke_leaded.readFromNBT(nbt, "smoke1");
		smoke_poison.readFromNBT(nbt, "smoke2");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		smoke.writeToNBT(nbt, "smoke0");
		smoke_leaded.writeToNBT(nbt, "smoke1");
		smoke_poison.writeToNBT(nbt, "smoke2");
	}
}
