package com.hfr.ender;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/** Validates one complete vanilla click before keeping its resulting inventory state. */
public final class RestrictedEnderChestContainer extends ContainerChest {

    private final InventoryEnderChest ender;
    private final ItemStack[] openingContents;

    public RestrictedEnderChestContainer(InventoryPlayer player, InventoryEnderChest ender) {
        super(player, ender);
        this.ender = ender;
        this.openingContents = snapshot(ender);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        if (player.worldObj.isRemote || !EnderChestRestrictions.isEnabled())
            return super.slotClick(slotId, button, mode, player);

        ItemStack[] oldEnder = snapshot(ender);
        ItemStack[] oldMain = copy(player.inventory.mainInventory);
        ItemStack[] oldArmor = copy(player.inventory.armorInventory);
        ItemStack oldCursor = copy(player.inventory.getItemStack());
        ItemStack result = super.slotClick(slotId, button, mode, player);
        String reason = EnderChestRestrictions.rejection(openingContents, ender);
        if (reason != null) {
            for (int i = 0; i < oldEnder.length; i++) ender.setInventorySlotContents(i, oldEnder[i]);
            restore(player.inventory.mainInventory, oldMain);
            restore(player.inventory.armorInventory, oldArmor);
            player.inventory.setItemStack(oldCursor);
            player.inventory.markDirty();
            player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + reason));
            if (player instanceof EntityPlayerMP) ((EntityPlayerMP) player).sendContainerToPlayer(this);
        }
        return result;
    }

    private static ItemStack[] snapshot(InventoryEnderChest inventory) {
        ItemStack[] result = new ItemStack[inventory.getSizeInventory()];
        for (int i = 0; i < result.length; i++) result[i] = copy(inventory.getStackInSlot(i));
        return result;
    }

    private static ItemStack[] copy(ItemStack[] original) {
        ItemStack[] result = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) result[i] = copy(original[i]);
        return result;
    }

    private static ItemStack copy(ItemStack stack) { return stack == null ? null : stack.copy(); }

    private static void restore(ItemStack[] target, ItemStack[] original) {
        for (int i = 0; i < target.length; i++) target[i] = original[i];
    }
}
