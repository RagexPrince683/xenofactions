package com.hfr.tileentity.machine;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityComboProxy extends TileEntityProxyBase implements IEnergyHandler, IFluidHandler, ISidedInventory {

	public boolean connectEnergy = true;
	public boolean connectFluid = true;
	public boolean connectInventory = true;

	// ================================
	// ENERGY
	// ================================
	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		TileEntity te = getTE();
		if (!connectEnergy || te == null) return false;

		if (te instanceof IEnergyProvider)
			return ((IEnergyProvider) te).canConnectEnergy(from);
		if (te instanceof IEnergyReceiver)
			return ((IEnergyReceiver) te).canConnectEnergy(from);
		return false;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		TileEntity te = getTE();
		if (!connectEnergy || te == null) return 0;
		if (te instanceof IEnergyReceiver)
			return ((IEnergyReceiver) te).receiveEnergy(from, maxReceive, simulate);
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		TileEntity te = getTE();
		if (!connectEnergy || te == null) return 0;
		if (te instanceof IEnergyProvider)
			return ((IEnergyProvider) te).extractEnergy(from, maxExtract, simulate);
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		TileEntity te = getTE();
		if (!connectEnergy || te == null) return 0;
		if (te instanceof IEnergyProvider)
			return ((IEnergyProvider) te).getEnergyStored(from);
		if (te instanceof IEnergyReceiver)
			return ((IEnergyReceiver) te).getEnergyStored(from);
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		TileEntity te = getTE();
		if (!connectEnergy || te == null) return 0;
		if (te instanceof IEnergyProvider)
			return ((IEnergyProvider) te).getMaxEnergyStored(from);
		if (te instanceof IEnergyReceiver)
			return ((IEnergyReceiver) te).getMaxEnergyStored(from);
		return 0;
	}

	// ================================
	// FLUID
	// ================================
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		TileEntity te = getTE();
		if (!connectFluid || te == null) return 0;
		return ((IFluidHandler) te).fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		TileEntity te = getTE();
		if (!connectFluid || te == null) return null;
		return ((IFluidHandler) te).drain(from, resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		TileEntity te = getTE();
		if (!connectFluid || te == null) return null;
		return ((IFluidHandler) te).drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		TileEntity te = getTE();
		return connectFluid && te instanceof IFluidHandler && ((IFluidHandler) te).canFill(from, fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		TileEntity te = getTE();
		return connectFluid && te instanceof IFluidHandler && ((IFluidHandler) te).canDrain(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		TileEntity te = getTE();
		if (!connectFluid || te == null) return new FluidTankInfo[0];
		return ((IFluidHandler) te).getTankInfo(from);
	}

	// ================================
	// INVENTORY (hopper/pipe forwarding)
	// ================================
	private IInventory getInv() {
		TileEntity te = getTE();
		return te instanceof IInventory ? (IInventory) te : null;
	}

	@Override
	public int getSizeInventory() {
		IInventory inv = getInv();
		return inv != null ? inv.getSizeInventory() : 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		IInventory inv = getInv();
		return inv != null ? inv.getStackInSlot(i) : null;
	}

	@Override
	public ItemStack decrStackSize(int i, int count) {
		IInventory inv = getInv();
		return inv != null ? inv.decrStackSize(i, count) : null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		IInventory inv = getInv();
		return inv != null ? inv.getStackInSlotOnClosing(i) : null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		IInventory inv = getInv();
		if (inv != null) inv.setInventorySlotContents(i, stack);
	}

	@Override
	public String getInventoryName() {
		IInventory inv = getInv();
		return inv != null ? inv.getInventoryName() : "proxy";
	}

	@Override
	public boolean hasCustomInventoryName() {
		IInventory inv = getInv();
		return inv != null && inv.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		IInventory inv = getInv();
		return inv != null ? inv.getInventoryStackLimit() : 64;
	}

	@Override
	public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
		IInventory inv = getInv();
		return inv != null && inv.isUseableByPlayer(player);
	}

	@Override
	public void openInventory() {
		IInventory inv = getInv();
		if (inv != null) inv.openInventory();
	}

	@Override
	public void closeInventory() {
		IInventory inv = getInv();
		if (inv != null) inv.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		IInventory inv = getInv();
		return inv != null && inv.isItemValidForSlot(i, stack);
	}

	// Sided forwarding (important for hoppers)
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		TileEntity te = getTE();
		if (te instanceof ISidedInventory)
			return ((ISidedInventory) te).getAccessibleSlotsFromSide(side);
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int i, ItemStack stack, int side) {
		TileEntity te = getTE();
		return te instanceof ISidedInventory && ((ISidedInventory) te).canInsertItem(i, stack, side);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int side) {
		TileEntity te = getTE();
		return te instanceof ISidedInventory && ((ISidedInventory) te).canExtractItem(i, stack, side);
	}

	// ================================
	// NBT
	// ================================
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		connectEnergy = nbt.getBoolean("conEn");
		connectFluid = nbt.getBoolean("conFl");
		connectInventory = nbt.getBoolean("conInv");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("conEn", connectEnergy);
		nbt.setBoolean("conFl", connectFluid);
		nbt.setBoolean("conInv", connectInventory);
	}
}
