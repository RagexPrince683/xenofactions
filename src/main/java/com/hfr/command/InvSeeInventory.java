package com.hfr.command;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * A thin IInventory wrapper that exposes the target player's 36-slot main inventory
 * (hotbar + main) as a chest-style IInventory. All operations delegate straight to
 * the target InventoryPlayer so changes happen immediately.
 */
public class InvSeeInventory implements IInventory {

    private final EntityPlayerMP target;

    public InvSeeInventory(EntityPlayerMP target) {
        this.target = target;
    }

    // Chest GUI expects 36 slots (27 main + 9 hotbar)
    @Override
    public int getSizeInventory() {
        return 36;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        // InventoryPlayer uses 0-8 hotbar, 9-35 main; same indexing we'll expose
        return target.inventory.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return target.inventory.decrStackSize(index, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return target.inventory.getStackInSlotOnClosing(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        target.inventory.setInventorySlotContents(index, stack);
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
        // Allow viewer to use it regardless of distance; you can tighten this if desired.
        return true;
    }

    @Override
    public void openInventory() { /* no-op */ }

    @Override
    public void closeInventory() { /* no-op */ }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        // Delegate to InventoryPlayer behavior (allow everything by default)
        return true;
    }
}