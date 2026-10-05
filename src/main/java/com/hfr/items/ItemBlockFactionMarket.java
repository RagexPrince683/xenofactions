package com.hfr.items;

import com.hfr.shop.FactionMarketRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/** Checks the actual vanilla-resolved placement position before consuming the item. */
public final class ItemBlockFactionMarket extends ItemBlock {
    public ItemBlockFactionMarket(Block block) { super(block); setMaxStackSize(1); }
    @Override public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                                         int side, float hitX, float hitY, float hitZ, int metadata) {
        if (!world.isRemote) {
            String error = FactionMarketRegistry.placementError(player, world, x, y, z);
            if (error != null) { player.addChatMessage(new ChatComponentText("[Market] " + error)); return false; }
        }
        if (!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata)) return false;
        if (!world.isRemote && !FactionMarketRegistry.register(player, world, x, y, z)) {
            world.setBlockToAir(x, y, z);
            player.addChatMessage(new ChatComponentText("[Market] Placement could not be registered; your item was retained."));
            return false;
        }
        return true;
    }
}
