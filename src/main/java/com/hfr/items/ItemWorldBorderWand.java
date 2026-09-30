package com.hfr.items;

import com.hfr.tdm.AdminSelectionManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.entity.player.EntityPlayerMP;

/** Legacy registry ID retained; this is the generic admin selection wand. */
public final class ItemWorldBorderWand extends Item {
    public ItemWorldBorderWand() { setMaxStackSize(1); setMaxDamage(0); }
    public static void giveIfNeeded(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        for (ItemStack stack : player.inventory.mainInventory)
            if (stack != null && stack.getItem() == ModItems.world_border_wand) return;
        ItemStack cursor = player.inventory.getItemStack();
        if (cursor != null && cursor.getItem() == ModItems.world_border_wand) return;
        ItemStack wand = new ItemStack(ModItems.world_border_wand);
        if (player.inventory.addItemStackToInventory(wand)) {
            player.inventoryContainer.detectAndSendChanges();
            player.addChatMessage(new ChatComponentText("Admin Selection Wand added to your inventory. Left click: Point A; right click: Point B."));
        } else {
            player.addChatMessage(new ChatComponentText("Inventory full. Make room and use /xc worldborder wand to get the Admin Selection Wand."));
        }
    }
}
