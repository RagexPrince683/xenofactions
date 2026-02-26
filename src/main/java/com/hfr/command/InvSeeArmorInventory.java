package com.hfr.command;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Exposes the target's armor slots as a chest-style IInventory of size 9.
 * Slots 0..3 map to target.inventory.armorInventory[0..3].
 */
public class InvSeeArmorInventory implements IInventory {

    private final EntityPlayerMP target;

    public InvSeeArmorInventory(EntityPlayerMP target) {
        this.target = target;
    }

    @Override
    public int getSizeInventory() {
        return 9; // chest row of 9; armor occupies 0..3
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index < 0 || index >= getSizeInventory()) return null;
        if (index < 4) {
            return target.inventory.armorInventory[index];
        }
        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index < 0 || index >= getSizeInventory()) return null;
        ItemStack stack = getStackInSlot(index);
        if (stack == null) return null;

        if (stack.stackSize <= count) {
            setInventorySlotContents(index, null);
            return stack;
        } else {
            ItemStack split = stack.splitStack(count);
            if (stack.stackSize == 0) setInventorySlotContents(index, null);
            markDirty();
            return split;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, null);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= getSizeInventory()) return;
        if (index < 4) {
            target.inventory.armorInventory[index] = stack;
            target.inventory.markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return target.getCommandSenderName() + "'s Armor";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        target.inventory.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        // allow viewer always; tighten if you want distance checks
        return true;
    }

    @Override
    public void openInventory() { /* no-op */ }

    @Override
    public void closeInventory() { /* no-op */ }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        // Allow anything to be placed so you can forcibly remove/replace glitched gear.
        // If you want to restrict by armor type, add checks here.
        return index >= 0 && index < 4;
    }
}