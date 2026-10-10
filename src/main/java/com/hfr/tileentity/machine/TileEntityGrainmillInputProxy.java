package com.hfr.tileentity.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/** Exposes a grain mill dummy block to hoppers while the core owns the inventory. */
public class TileEntityGrainmillInputProxy extends TileEntityProxyBase implements ISidedInventory {
	private static final int[] NO_SLOTS = new int[0];

	private TileEntityMachineGrainmill getMill() {
		if(worldObj == null) return null;
		TileEntity tile = getTE();
		return tile instanceof TileEntityMachineGrainmill ? (TileEntityMachineGrainmill)tile : null;
	}

	@Override
	public int getSizeInventory() {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null ? mill.getSizeInventory() : 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null && slot >= 0 && slot < mill.getSizeInventory() ? mill.getStackInSlot(slot) : null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		TileEntityMachineGrainmill mill = getMill();
		if(mill == null || worldObj.isRemote || slot < 0 || slot >= mill.getSizeInventory()) return null;
		ItemStack removed = mill.decrStackSize(slot, count);
		if(removed != null) mill.markDirty();
		return removed;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		TileEntityMachineGrainmill mill = getMill();
		if(mill == null || worldObj.isRemote || slot < 0 || slot >= mill.getSizeInventory()) return null;
		ItemStack removed = mill.getStackInSlotOnClosing(slot);
		if(removed != null) mill.markDirty();
		return removed;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		TileEntityMachineGrainmill mill = getMill();
		if(mill != null && !worldObj.isRemote && slot >= 0 && slot < mill.getSizeInventory()) {
			mill.setInventorySlotContents(slot, stack);
			mill.markDirty();
		}
	}

	@Override
	public String getInventoryName() {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null ? mill.getInventoryName() : "container.grainmill";
	}

	@Override
	public boolean hasCustomInventoryName() {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null && mill.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null ? mill.getInventoryStackLimit() : 64;
	}

	@Override
	public void markDirty() {
		TileEntityMachineGrainmill mill = getMill();
		if(mill != null && !worldObj.isRemote) mill.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null && mill.isUseableByPlayer(player);
	}

	@Override public void openInventory() { }
	@Override public void closeInventory() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null && mill.isItemValidForSlot(slot, stack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null && side != 0 ? mill.getAccessibleSlotsFromSide(side) : NO_SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		TileEntityMachineGrainmill mill = getMill();
		return mill != null && !worldObj.isRemote && mill.canInsertItem(slot, stack, side);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return false;
	}
}
