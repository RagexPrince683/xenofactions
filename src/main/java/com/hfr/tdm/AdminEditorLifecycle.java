package com.hfr.tdm;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.AdminEditorClosePacket;

/** Registered regardless of whether the TDM match mode is enabled. */
public final class AdminEditorLifecycle {
    @SubscribeEvent public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) TDMServerTaskQueue.runScheduledTasks();
    }
    @SubscribeEvent public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) return;
        TDMAdminKitEdit.tick(event.player);
        AdminSelectionManager.tick(event.player);
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            TDMMapOverlaySync.tick(player);
            if (AdminEditorSession.has(player) && !player.canCommandSenderUseCommand(4,"tdm")) {
                AdminEditorSession.clear(player);
                PacketDispatcher.wrapper.sendTo(new AdminEditorClosePacket(), player);
            }
        }
    }
    @SubscribeEvent public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        com.hfr.shop.XShopService.logout(event.player);
        com.hfr.shop.PlayerTraderService.logout(event.player);
        TDMMapOverlaySync.clear(event.player);
        TDMAdminKitEdit.cancelIfActive(event.player);
        AdminSelectionManager.clear(event.player);
        if (event.player instanceof EntityPlayerMP) AdminEditorSession.clear((EntityPlayerMP)event.player);
    }
    @SubscribeEvent public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        com.hfr.shop.XShopService.logout(event.player);
        TDMMapOverlaySync.clear(event.player);
        TDMAdminKitEdit.cancelIfActive(event.player);
        AdminSelectionManager.clear(event.player);
        if (event.player instanceof EntityPlayerMP) AdminEditorSession.clear((EntityPlayerMP)event.player);
    }
    @SubscribeEvent public void onClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        TDMMapOverlaySync.copySettings(event.original, event.entityPlayer);
        AdminEditorSession.copyContext(event.original, event.entityPlayer);
        TDMMapOverlaySync.clear(event.original);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST) public void onDeath(LivingDeathEvent event) {
        if (event.entityLiving.worldObj.isRemote || !(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)event.entityLiving;
        com.hfr.shop.XShopService.logout(player);
        com.hfr.shop.PlayerTraderService.logout(player);
        TDMAdminKitEdit.cancelIfActive(player);
        AdminSelectionManager.clear(player);
        if (player instanceof EntityPlayerMP) AdminEditorSession.clear((EntityPlayerMP)player);
    }
    @SubscribeEvent public void onPickup(EntityItemPickupEvent event) {
        if (!event.entityPlayer.worldObj.isRemote && TDMAdminKitEdit.isEditing(event.entityPlayer)) event.setCanceled(true);
    }
    @SubscribeEvent public void onInteract(PlayerInteractEvent event) {
        if (!event.entityPlayer.worldObj.isRemote && TDMAdminKitEdit.isEditing(event.entityPlayer)) event.setCanceled(true);
    }
    @SubscribeEvent public void onEntityInteract(EntityInteractEvent event) {
        if (!event.entityPlayer.worldObj.isRemote && TDMAdminKitEdit.isEditing(event.entityPlayer)) event.setCanceled(true);
    }
    @SubscribeEvent public void onToss(ItemTossEvent event) {
        if (!event.player.worldObj.isRemote && TDMAdminKitEdit.isEditing(event.player)) event.setCanceled(true);
    }
    @SubscribeEvent public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && !event.world.isRemote && event.world.provider.dimensionId == 0) {
            TDMAdminKitEdit.cancelAll(); AdminSelectionManager.clearAll(); AdminEditorSession.clearAll(); TDMServerTaskQueue.clear(); TDMMapOverlaySync.clearAll();
        }
    }
}
