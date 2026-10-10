package com.hfr.ender;

import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Opens the restricted container for vanilla ender chest block interactions. */
public final class EnderChestInteraction {

    public static final int GUI_ID = 42;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!EnderChestRestrictions.isEnabled() || event.isCanceled()
                || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
                || event.useBlock == Event.Result.DENY
                || event.world.isRemote || event.world.getBlock(event.x, event.y, event.z) != Blocks.ender_chest)
            return;

        ItemStack held = event.entityPlayer.getHeldItem();
        if (event.entityPlayer.isSneaking() && held != null
                && !held.getItem().doesSneakBypassUse(event.world, event.x, event.y, event.z, event.entityPlayer))
            return;
        if (event.world.getBlock(event.x, event.y + 1, event.z).isNormalCube()
                || !(event.world.getTileEntity(event.x, event.y, event.z) instanceof TileEntityEnderChest))
            return;

        event.entityPlayer.openGui(MainRegistry.instance, GUI_ID, event.world, event.x, event.y, event.z);
        event.setCanceled(true);
    }
}
