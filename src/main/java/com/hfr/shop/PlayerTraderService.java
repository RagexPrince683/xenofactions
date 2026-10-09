package com.hfr.shop;

import com.hfr.blocks.machine.PlayerTrader.Tile;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.clowder.FactionPermission;
import com.hfr.items.ModItems;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.shop.PlayerTraderSnapshotPacket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;

/** All shop mutations run on the server thread; packets contain only an intent and offer revision. */
public final class PlayerTraderService {
    private static final Map<EntityPlayerMP, Session> SESSIONS = new WeakHashMap<EntityPlayerMP, Session>();
    private static class Session {
        final Tile tile;
        final String token = UUID.randomUUID().toString();
        Session(Tile tile) { this.tile = tile; }
    }
    private PlayerTraderService() { }
    public static void logout(EntityPlayer player) { SESSIONS.remove(player); }
    public static void clearSessions() { SESSIONS.clear(); }
    public static void broken(Tile tile) {
        for (Iterator<Map.Entry<EntityPlayerMP, Session>> it = SESSIONS.entrySet().iterator(); it.hasNext();) {
            Map.Entry<EntityPlayerMP, Session> entry = it.next();
            if (entry.getValue().tile == tile) { close(entry.getKey(), entry.getValue()); it.remove(); }
        }
    }
    private static void close(EntityPlayerMP player, Session session) {
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("token", session.token); tag.setBoolean("closed", true);
        PacketDispatcher.wrapper.sendTo(new PlayerTraderSnapshotPacket(tag), player);
    }

    public static boolean owner(EntityPlayer player, Tile tile) {
        return player != null && tile != null && tile.owner.equals(player.getUniqueID().toString());
    }
    public static boolean manager(EntityPlayer player, Tile tile) {
        return owner(player, tile) || player != null && player.canCommandSenderUseCommand(3, "xshop");
    }
    /** Matches the ordinary INTERACT faction rule, with existing op and debug access for shop administration. */
    public static boolean mayAccess(EntityPlayer player, int x, int z) {
        if (player == null || player.worldObj == null) return false;
        if (player.canCommandSenderUseCommand(3, "xshop") || player.inventory.hasItem(ModItems.debug)) return true;
        Ownership land = ClowderTerritory.getOwnerFromInts(player.worldObj, x, z);
        if (land == null || land.zone != Zone.FACTION) return true;
        Clowder faction = Clowder.getClowderFromPlayer(player);
        if (land.owner == null) return false;
        if (faction == land.owner) return !land.owner.isInfrastructureDisabled();
        return land.owner != null && land.owner.canVisitorAccess(faction, FactionPermission.INTERACT);
    }
    private static boolean near(EntityPlayerMP player, Tile tile) {
        return !player.isDead && tile != null && tile.valid() && player.worldObj == tile.getWorldObj()
            && player.getDistanceSq(tile.xCoord + .5, tile.yCoord + .5, tile.zCoord + .5) <= 64
            && mayAccess(player, tile.xCoord, tile.zCoord);
    }
    public static void open(EntityPlayerMP player, Tile tile) {
        if (!near(player, tile)) return;
        Session session = new Session(tile); SESSIONS.put(player, session); snapshot(player, session, true);
    }
    public static void action(EntityPlayerMP player, String token, String action, long revision, int slot, int quantity) {
        Session session = SESSIONS.get(player);
        if (session == null || !session.token.equals(token)) return;
        if ("close".equals(action)) { SESSIONS.remove(player); return; }
        Tile tile = session.tile;
        if (!near(player, tile)) { SESSIONS.remove(player); close(player, session); return; }
        if (revision != tile.offerRevision) { say(player, "The offer changed. Review the refreshed shop."); snapshot(player, session, false); return; }
        String error = null;
        if ("buy".equals(action)) error = buy(player, tile);
        else if (manager(player, tile)) {
            if ("sale".equals(action) || "payment".equals(action)) error = configure(player, tile, action, slot, quantity);
            else if ("deposit".equals(action)) error = deposit(player, tile, slot);
            else if ("stock".equals(action) || "collect".equals(action)) error = withdraw(player, tile, action, slot);
        }
        if (error != null) say(player, error);
        refresh(tile);
    }
    private static void say(EntityPlayer player, String message) { player.addChatMessage(new ChatComponentText("[Player Trader] " + message)); }
    private static void snapshot(EntityPlayerMP player, Session session, boolean opening) {
        Tile tile = session.tile;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("token", session.token); tag.setBoolean("opening", opening);
        tag.setLong("revision", tile.offerRevision); tag.setBoolean("manager", manager(player, tile));
        tag.setInteger("saleQuantity", tile.saleQuantity); tag.setInteger("paymentQuantity", tile.paymentQuantity);
        if (tile.sale != null) { NBTTagCompound n = new NBTTagCompound(); tile.sale.writeToNBT(n); tag.setTag("sale", n); }
        if (tile.payment != null) { NBTTagCompound n = new NBTTagCompound(); tile.payment.writeToNBT(n); tag.setTag("payment", n); }
        int stock = 0;
        if (tile.sale != null) for (ItemStack stack : tile.stock) if (same(stack, tile.sale)) stock += stack.stackSize;
        tag.setInteger("available", tile.saleQuantity > 0 ? stock / tile.saleQuantity : 0);
        if (manager(player, tile)) {
            tag.setTag("stock", list(tile.stock)); tag.setTag("payments", list(tile.payments));
            tag.setTag("hotbar", list(Arrays.copyOf(player.inventory.mainInventory, 9)));
        }
        PacketDispatcher.wrapper.sendTo(new PlayerTraderSnapshotPacket(tag), player);
    }
    private static NBTTagList list(ItemStack[] items) {
        NBTTagList result = new NBTTagList();
        for (int i = 0; i < items.length; i++) if (items[i] != null) {
            NBTTagCompound n = new NBTTagCompound(); n.setByte("Slot", (byte)i); items[i].writeToNBT(n); result.appendTag(n);
        }
        return result;
    }
    private static void refresh(Tile tile) {
        for (Map.Entry<EntityPlayerMP, Session> entry : SESSIONS.entrySet())
            if (entry.getValue().tile == tile && near(entry.getKey(), tile)) snapshot(entry.getKey(), entry.getValue(), false);
    }
    private static String configure(EntityPlayerMP player, Tile tile, String action, int slot, int quantity) {
        if (slot < 0 || slot >= 9 || quantity <= 0 || quantity > 576) return "Choose hotbar slot 1-9 and a quantity from 1 to 576.";
        ItemStack source = player.inventory.mainInventory[slot];
        if (source == null || source.stackSize <= 0) return "That hotbar slot is empty.";
        if (quantity > 9 * Math.min(64, source.getMaxStackSize()))
            return "That quantity exceeds the shop's storage capacity for this item.";
        ItemStack template = source.copy(); template.stackSize = 1;
        if ("sale".equals(action)) { tile.sale = template; tile.saleQuantity = quantity; }
        else { tile.payment = template; tile.paymentQuantity = quantity; }
        tile.offerRevision++; tile.markDirty();
        return null;
    }
    private static String deposit(EntityPlayerMP player, Tile tile, int slot) {
        if (slot < 0 || slot >= 9) return "Choose hotbar slot 1-9.";
        ItemStack source = player.inventory.mainInventory[slot];
        if (source == null || source.stackSize <= 0 || tile.sale == null || !same(source, tile.sale))
            return "Put the configured sale item in that hotbar slot.";
        ItemStack[] stock = copy(tile.stock);
        if (!insert(stock, source, source.stackSize)) return "Sale stock is full.";
        player.inventory.mainInventory[slot] = null;
        tile.stock = stock; changed(player, tile); return null;
    }
    private static String withdraw(EntityPlayerMP player, Tile tile, String action, int slot) {
        if (slot < 0 || slot >= 9) return "Choose storage slot 1-9.";
        ItemStack[] source = "stock".equals(action) ? tile.stock : tile.payments;
        if (source[slot] == null) return "That storage slot is empty.";
        ItemStack[] inventory = copy(player.inventory.mainInventory);
        if (!insert(inventory, source[slot], source[slot].stackSize)) return "Your inventory is full.";
        System.arraycopy(inventory, 0, player.inventory.mainInventory, 0, inventory.length);
        source[slot] = null; changed(player, tile); return null;
    }
    private static String buy(EntityPlayerMP player, Tile tile) {
        if (tile.sale == null || tile.payment == null || tile.saleQuantity <= 0 || tile.paymentQuantity <= 0)
            return "This shop has no valid offer.";
        ItemStack[] stock = copy(tile.stock), payments = copy(tile.payments);
        ItemStack[] inventory = copy(player.inventory.mainInventory);
        if (!remove(stock, tile.sale, tile.saleQuantity)) return "Insufficient stock.";
        if (!remove(inventory, tile.payment, tile.paymentQuantity)) return "Insufficient payment.";
        if (!insert(inventory, tile.sale, tile.saleQuantity)) return "Your inventory is full.";
        if (!insert(payments, tile.payment, tile.paymentQuantity)) return "Shop payment storage is full.";
        // All capacity and identity checks succeeded. No callback runs between these assignments.
        tile.stock = stock; tile.payments = payments;
        System.arraycopy(inventory, 0, player.inventory.mainInventory, 0, inventory.length);
        changed(player, tile); say(player, "Purchase complete."); return null;
    }
    private static void changed(EntityPlayerMP player, Tile tile) {
        tile.markDirty(); player.inventory.markDirty(); player.inventoryContainer.detectAndSendChanges();
    }
    private static ItemStack[] copy(ItemStack[] source) {
        ItemStack[] result = new ItemStack[source.length];
        for (int i = 0; i < result.length; i++) if (source[i] != null) result[i] = source[i].copy();
        return result;
    }
    private static boolean same(ItemStack a, ItemStack b) {
        return a != null && b != null && a.getItem() == b.getItem()
            && a.getItemDamage() == b.getItemDamage() && ItemStack.areItemStackTagsEqual(a, b);
    }
    private static boolean remove(ItemStack[] items, ItemStack template, int amount) {
        for (int i = 0; i < items.length && amount > 0; i++) if (same(items[i], template)) {
            int taken = Math.min(amount, items[i].stackSize); amount -= taken; items[i].stackSize -= taken;
            if (items[i].stackSize == 0) items[i] = null;
        }
        return amount == 0;
    }
    private static boolean insert(ItemStack[] items, ItemStack template, int amount) {
        for (int pass = 0; pass < 2 && amount > 0; pass++) for (int i = 0; i < items.length && amount > 0; i++) {
            ItemStack stack = items[i];
            if (pass == 0 && !same(stack, template) || pass == 1 && stack != null) continue;
            int limit = Math.min(64, template.getMaxStackSize());
            int space = stack == null ? limit : limit - stack.stackSize;
            if (space <= 0) continue;
            int added = Math.min(amount, space);
            if (stack == null) { items[i] = template.copy(); items[i].stackSize = added; }
            else stack.stackSize += added;
            amount -= added;
        }
        return amount == 0;
    }
}
