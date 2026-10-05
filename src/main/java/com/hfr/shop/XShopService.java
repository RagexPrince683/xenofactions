package com.hfr.shop;

import com.hfr.blocks.ModBlocks;
import com.hfr.blocks.machine.MachineMarket.TileEntityMarket;
import com.hfr.data.MarketData;
import com.hfr.data.MarketData.Shop;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.shop.XShopActionPacket;
import com.hfr.packet.shop.XShopSnapshotPacket;
import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/** The only authority for shop menu navigation, editing and transactions; runs on the server thread. */
public final class XShopService {
    private static final Map<EntityPlayerMP, Session> SESSIONS = new WeakHashMap<EntityPlayerMP, Session>();
    private static final int PAGE_SIZE = 6;
    private static final class Session {
        String token = UUID.randomUUID().toString(), mode, selected = "", filter = "";
        World world;
        TileEntityMarket anchor;
        int page;
        long revision, expires, lastAction, lastTrade;
        boolean opening = true;
        Session(EntityPlayerMP player, TileEntityMarket anchor, String mode) {
            world = player.worldObj; this.anchor = anchor; this.mode = mode;
        }
    }
    private XShopService() { }
    public static boolean isAdmin(EntityPlayer player) { return player != null && player.canCommandSenderUseCommand(3, "xshop"); }
    public static void clearSessions() { SESSIONS.clear(); }
    public static void logout(EntityPlayer player) { SESSIONS.remove(player); }
    private static void say(EntityPlayer player, String message) { player.addChatMessage(new ChatComponentText("[XShop] " + message)); }
    private static boolean near(EntityPlayerMP player, TileEntityMarket tile) {
        return tile != null && !tile.isInvalid() && tile.getWorldObj() == player.worldObj
            && player.getDistanceSq(tile.xCoord + .5, tile.yCoord + .5, tile.zCoord + .5) <= 64
            && tile.getWorldObj().blockExists(tile.xCoord, tile.yCoord, tile.zCoord)
            && tile.getWorldObj().getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile;
    }
    public static void openBlock(EntityPlayerMP player, TileEntityMarket tile) {
        if (!near(player, tile) || player.isDead) return;
        boolean terminal = tile.getWorldObj().getBlock(tile.xCoord, tile.yCoord, tile.zCoord) == ModBlocks.faction_market;
        if (terminal && !FactionMarketRegistry.canUse(player, tile)) { say(player, "This terminal is not active for your faction's current capital."); return; }
        if (!terminal && tile.getWorldObj().getBlock(tile.xCoord, tile.yCoord, tile.zCoord) != ModBlocks.machine_market) return;
        Session session = new Session(player, tile, terminal ? "browser" : isAdmin(player) ? "admin" : "trade");
        session.selected = terminal ? "" : tile.resolveShopId();
        if (session.mode.equals("trade") && !accessible(player, MarketData.get(session.selected), false)) {
            say(player, "This shop is unlinked, missing, disabled, or restricted."); return;
        }
        SESSIONS.put(player, session); snapshot(player, session);
    }
    public static void edit(EntityPlayerMP player, Shop shop) {
        if (!isAdmin(player) || shop == null) return;
        Session session = new Session(player, null, "edit"); session.selected = shop.id;
        SESSIONS.put(player, session); snapshot(player, session);
    }
    private static boolean accessible(EntityPlayerMP player, Shop shop, boolean terminal) {
        return shop != null && shop.enabled && (!shop.adminOnly || isAdmin(player)) && (!terminal || shop.marketEligible());
    }
    private static boolean validate(EntityPlayerMP player, Session session) {
        if (player.isDead || player.playerNetServerHandler == null || player.worldObj != session.world
            || System.currentTimeMillis() > session.expires) return false;
        if (session.anchor == null) return isAdmin(player) && (session.mode.equals("edit") || session.mode.equals("admin"));
        if (!near(player, session.anchor)) return false;
        boolean terminal = session.world.getBlock(session.anchor.xCoord, session.anchor.yCoord, session.anchor.zCoord) == ModBlocks.faction_market;
        if (terminal) {
            if (!FactionMarketRegistry.canUse(player, session.anchor)) return false;
        } else if (session.world.getBlock(session.anchor.xCoord, session.anchor.yCoord, session.anchor.zCoord) != ModBlocks.machine_market) return false;
        if (session.mode.equals("admin") || session.mode.equals("edit")) return !terminal && isAdmin(player);
        if (session.mode.equals("trade")) {
            if (!terminal && !session.selected.equals(session.anchor.resolveShopId())) return false;
            return accessible(player, MarketData.get(session.selected), terminal);
        }
        return session.mode.equals("browser") && terminal;
    }
    public static ItemStack[] hotbar(EntityPlayer player) {
        ItemStack[] result = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i); if (stack != null) result[i] = stack.copy();
        }
        return result;
    }
    public static void action(EntityPlayerMP player, XShopActionPacket request) {
        Session session = SESSIONS.get(player);
        if (session == null || !session.token.equals(request.token)) return;
        if (!validate(player, session)) {
            SESSIONS.remove(player); close(player, session, "Market access changed. Reopen the block or editor."); return;
        }
        long now = System.currentTimeMillis();
        if (now - session.lastAction < 100) return;
        session.lastAction = now; session.expires = now + 300000L;
        if (request.action.equals("close")) { SESSIONS.remove(player); return; }
        if (session.revision != MarketData.revision() || request.revision != session.revision) {
            say(player, "The shop catalog changed; review the refreshed menu."); snapshot(player, session); return;
        }
        try {
            String action = request.action;
            if (action.equals("page")) session.page = Math.max(0, request.value);
            else if (action.equals("search") && (session.mode.equals("browser") || session.mode.equals("admin"))) {
                session.filter = request.text.trim().toLowerCase(Locale.ROOT); session.page = 0;
            } else if (session.mode.equals("browser") && action.equals("select")) {
                Shop shop = MarketData.get(request.shopId);
                if (!accessible(player, shop, true)) throw new IllegalArgumentException("That shop is unavailable in the faction market.");
                session.selected = shop.id; session.mode = "trade"; session.page = 0;
            } else if (session.mode.equals("admin") && isAdmin(player)) {
                if (action.equals("link") && session.anchor != null) {
                    Shop shop = MarketData.get(request.shopId);
                    if (shop == null) throw new IllegalArgumentException("That shop no longer exists.");
                    session.anchor.link(shop.id); session.selected = shop.id;
                } else if (action.equals("unlink") && session.anchor != null) {
                    session.anchor.link(""); session.selected = "";
                } else if (action.equals("edit")) {
                    Shop shop = MarketData.get(request.shopId);
                    if (shop == null) throw new IllegalArgumentException("Select an existing shop.");
                    session.selected = shop.id; session.mode = "edit"; session.page = 0;
                } else if (action.equals("view") && session.anchor != null) {
                    session.selected = session.anchor.resolveShopId();
                    if (!accessible(player, MarketData.get(session.selected), false))
                        throw new IllegalArgumentException("The linked shop is unavailable.");
                    session.mode = "trade"; session.page = 0;
                }
            } else if (session.mode.equals("edit") && isAdmin(player)) {
                Shop shop = MarketData.get(session.selected);
                if (shop == null) throw new IllegalArgumentException("This shop was deleted.");
                if (action.equals("rename")) MarketData.configure(shop.id, request.text, shop.enabled, shop.visibleInFactionTerminal, shop.adminOnly);
                else if (action.equals("enabled")) MarketData.configure(shop.id, shop.displayName, !shop.enabled, shop.visibleInFactionTerminal, shop.adminOnly);
                else if (action.equals("visible")) MarketData.configure(shop.id, shop.displayName, shop.enabled, !shop.visibleInFactionTerminal, shop.adminOnly);
                else if (action.equals("restricted")) MarketData.configure(shop.id, shop.displayName, shop.enabled, shop.visibleInFactionTerminal, !shop.adminOnly);
                else if (action.equals("add")) MarketData.addOffer(shop.id, hotbar(player));
                else if (action.equals("remove") && request.value / PAGE_SIZE == session.page) MarketData.removeOffer(shop.id, request.value);
                else if (action.equals("back")) { session.mode = "admin"; session.page = 0; }
            } else if (session.mode.equals("trade")) {
                if (action.equals("buy") && request.value >= 0 && request.value / PAGE_SIZE == session.page) {
                    if (now - session.lastTrade < 300) return;
                    session.lastTrade = now; trade(player, session.selected, request.value);
                } else if (action.equals("back")) {
                    session.mode = session.world.getBlock(session.anchor.xCoord, session.anchor.yCoord, session.anchor.zCoord) == ModBlocks.faction_market
                        ? "browser" : isAdmin(player) ? "admin" : "trade"; session.page = 0;
                }
            }
            snapshot(player, session);
        } catch (IllegalArgumentException | IllegalStateException e) { say(player, e.getMessage()); snapshot(player, session); }
    }
    private static void close(EntityPlayerMP player, Session session, String reason) {
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("token", session.token); tag.setString("mode", "close"); tag.setString("reason", reason);
        PacketDispatcher.wrapper.sendTo(new XShopSnapshotPacket(tag), player);
    }
    private static void snapshot(EntityPlayerMP player, Session session) {
        session.expires = System.currentTimeMillis() + 300000L; session.revision = MarketData.revision();
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("token", session.token);
        tag.setBoolean("opening", session.opening); session.opening = false;
        tag.setLong("revision", session.revision); tag.setString("mode", session.mode); tag.setString("selected", session.selected);
        tag.setBoolean("block", session.anchor != null);
        Shop selected = MarketData.get(session.selected);
        tag.setString("name", selected == null ? "Unlinked / deleted" : selected.displayName);
        if (session.anchor != null && session.world.getBlock(session.anchor.xCoord, session.anchor.yCoord, session.anchor.zCoord) == ModBlocks.machine_market) {
            Shop linked = MarketData.get(session.anchor.resolveShopId()); tag.setString("linked", linked == null ? "Unlinked / deleted" : linked.displayName);
        }
        int pages;
        if (session.mode.equals("trade") || session.mode.equals("edit")) {
            if (selected == null) { SESSIONS.remove(player); close(player, session, "This shop no longer exists."); return; }
            pages = Math.max(1, (selected.offerCount() + PAGE_SIZE - 1) / PAGE_SIZE);
            session.page = Math.min(session.page, pages - 1);
            List<ItemStack[]> offers = new ArrayList<ItemStack[]>();
            for (int i = session.page * PAGE_SIZE; i < Math.min(selected.offerCount(), (session.page + 1) * PAGE_SIZE); i++) {
                ItemStack[] offer = MarketData.offer(selected.id, i); offers.add(offer == null ? new ItemStack[4] : offer);
            }
            tag.setTag("offers", MarketData.offersToNBT(offers).getTag("offers"));
            tag.setBoolean("enabled", selected.enabled); tag.setBoolean("visible", selected.visibleInFactionTerminal); tag.setBoolean("restricted", selected.adminOnly);
        } else {
            List<Shop> shops = new ArrayList<Shop>();
            for (Shop shop : MarketData.list()) if ((!session.mode.equals("browser") || shop.marketEligible())
                && shop.displayName.toLowerCase(Locale.ROOT).contains(session.filter)) shops.add(shop);
            pages = Math.max(1, (shops.size() + PAGE_SIZE - 1) / PAGE_SIZE); session.page = Math.min(session.page, pages - 1);
            NBTTagList rows = new NBTTagList();
            for (int i = session.page * PAGE_SIZE; i < Math.min(shops.size(), (session.page + 1) * PAGE_SIZE); i++) {
                Shop shop = shops.get(i); NBTTagCompound row = new NBTTagCompound();
                row.setString("id", shop.id); row.setString("name", shop.displayName); rows.appendTag(row);
            }
            tag.setTag("shops", rows); tag.setString("filter", session.filter);
        }
        tag.setInteger("page", session.page); tag.setInteger("pages", pages);
        PacketDispatcher.wrapper.sendTo(new XShopSnapshotPacket(tag), player);
    }
    /** Preserve legacy item+metadata currency matching, but reserve combined repeated costs atomically. */
    private static void trade(EntityPlayerMP player, String id, int index) {
        ItemStack[] offer = MarketData.offer(id, index);
        if (offer == null) { say(player, "This offer contains an unavailable item."); return; }
        int[] reserved = new int[player.inventory.getSizeInventory()];
        for (int cost = 1; cost < 4; cost++) {
            if (offer[cost] == null) continue;
            int needed = offer[cost].stackSize;
            for (int slot = 0; slot < reserved.length && needed > 0; slot++) {
                ItemStack stack = player.inventory.getStackInSlot(slot);
                if (stack == null || stack.getItem() != offer[cost].getItem() || stack.getItemDamage() != offer[cost].getItemDamage()) continue;
                int take = Math.min(needed, stack.stackSize - reserved[slot]); reserved[slot] += take; needed -= take;
            }
            if (needed > 0) {
                player.worldObj.playSoundAtEntity(player, "hfr:block.buttonNo", 1F, 1F); say(player, "You lack the required items."); return;
            }
        }
        for (int slot = 0; slot < reserved.length; slot++) if (reserved[slot] > 0) player.inventory.decrStackSize(slot, reserved[slot]);
        ItemStack purchased = offer[0].copy();
        if (!player.inventory.addItemStackToInventory(purchased) && purchased.stackSize > 0)
            player.dropPlayerItemWithRandomChoice(purchased, true);
        player.inventory.markDirty(); player.inventoryContainer.detectAndSendChanges();
        player.worldObj.playSoundAtEntity(player, "hfr:block.buttonYes", 1F, 1F);
    }
}
