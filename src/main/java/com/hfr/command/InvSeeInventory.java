package com.hfr.command;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InvSeeInventory implements IInventory {

    private final EntityPlayerMP target;

    public InvSeeInventory(EntityPlayerMP target) {
        this.target = target;
    }

    @Override
    public int getSizeInventory() {
        return target.inventory.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return target.inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return target.inventory.decrStackSize(slot, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return target.inventory.getStackInSlotOnClosing(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        target.inventory.setInventorySlotContents(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return target.getCommandSenderName() + "'s Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return target.inventory.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        target.inventory.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return false;
    }
}
