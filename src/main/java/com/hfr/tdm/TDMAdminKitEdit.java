package com.hfr.tdm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

/** Server-owned inventory edit lease. Kit contents never survive cancellation in the admin inventory. */
public final class TDMAdminKitEdit {
    private static final Map<UUID, Session> SESSIONS = new HashMap<UUID, Session>();
    private TDMAdminKitEdit() { }

    private static final class Session {
        final String map, revision;
        final Object identity;
        final TDMManager.Team team;
        final int index, dimension;
        final ItemStack[] main = new ItemStack[36], armor = new ItemStack[4];
        final long started;
        Session(EntityPlayer player, String map, TDMManager.Team team, int index, Object identity, String revision) {
            this.map = map; this.team = team; this.index = index; this.revision = revision;
            this.identity = identity;
            this.dimension = player.dimension; this.started = System.currentTimeMillis();
            for (int i = 0; i < main.length; i++) main[i] = copy(player.inventory.mainInventory[i]);
            for (int i = 0; i < armor.length; i++) armor[i] = copy(player.inventory.armorInventory[i]);
        }
    }

    public static String begin(EntityPlayer player, String map, TDMManager.Team team, int index) {
        if (!authorized(player)) return "TDM administrator permission is required.";
        if (!player.capabilities.isCreativeMode) return "Inventory kit editing requires creative mode to avoid consuming survival items.";
        if (TDMManager.isEnabled(player.worldObj) && TDMManager.isCompetitivePlayer(player))
            return "Leave the active TDM team before editing kits; /tdm teamless is available to administrators.";
        if (player.openContainer != player.inventoryContainer) return "Close other containers before loading a kit.";
        if (SESSIONS.containsKey(player.getUniqueID())) return "Finish or cancel the active kit edit first.";
        if (player.inventory.getItemStack() != null) return "Put down the item held by your cursor first.";
        String revision = TDMKitManager.getDirectKitRevision(map, team, index);
        ItemStack[] preview = TDMKitManager.getDirectKitPreview(map, team, index);
        if (revision == null || preview == null) return "No direct kit at that map, team, and number. Global fallback kits must be edited as global.";
        if (!TDMKitManager.canEditDirectKit(map, team, index)) return "Kit contains an item that cannot be loaded safely; its saved definition was left unchanged.";
        Session session = new Session(player, map, team, index, TDMKitManager.getDirectKitIdentity(map, team, index), revision);
        SESSIONS.put(player.getUniqueID(), session);
        for (int i = 0; i < 36; i++) player.inventory.mainInventory[i] = copy(preview[i]);
        for (int i = 0; i < 4; i++) player.inventory.armorInventory[i] = copy(preview[36 + i]);
        sync(player);
        return "Editing " + describe(session) + " in your inventory. Use /tdm kit commit or /tdm kit cancel.";
    }

    public static String commit(EntityPlayer player) {
        Session s = SESSIONS.get(player.getUniqueID());
        if (s == null) return "No active kit inventory edit.";
        if (!authorized(player) || !player.capabilities.isCreativeMode || player.dimension != s.dimension || expired(s)
                || (TDMManager.isEnabled(player.worldObj) && TDMManager.isCompetitivePlayer(player))
                || player.openContainer != player.inventoryContainer) {
            cancel(player);
            return "Kit edit expired or permission/mode/world changed; original inventory restored.";
        }
        if (player.inventory.getItemStack() != null) return "Put down the item held by your cursor before saving.";
        if (s.identity != TDMKitManager.getDirectKitIdentity(s.map, s.team, s.index)
                || !s.revision.equals(TDMKitManager.getDirectKitRevision(s.map, s.team, s.index))) {
            cancel(player);
            return "Kit changed since loading; edit cancelled to prevent overwriting another admin's work.";
        }
        boolean saved = TDMKitManager.replaceKit(s.map, s.team, s.index, s.identity, s.revision, player);
        restore(player, s);
        SESSIONS.remove(player.getUniqueID());
        return saved ? "Saved " + describe(s) + "; original inventory restored." : "Kit save failed; original inventory restored.";
    }

    public static String cancel(EntityPlayer player) {
        Session s = SESSIONS.remove(player.getUniqueID());
        if (s == null) return "No active kit inventory edit.";
        restore(player, s);
        return "Cancelled " + describe(s) + "; original inventory restored.";
    }

    public static String status(EntityPlayer player) {
        Session s = SESSIONS.get(player.getUniqueID());
        return s == null ? "No active kit inventory edit." : "Editing " + describe(s) + ". Save with /tdm kit commit, or cancel to restore your original inventory.";
    }

    public static void tick(EntityPlayer player) {
        Session s = SESSIONS.get(player.getUniqueID());
        if (s != null && (!authorized(player) || !player.capabilities.isCreativeMode || player.dimension != s.dimension || expired(s)
                || (TDMManager.isEnabled(player.worldObj) && TDMManager.isCompetitivePlayer(player))
                || player.openContainer != player.inventoryContainer)) {
            player.addChatMessage(new ChatComponentText(cancel(player)));
        }
    }

    public static void cancelIfActive(EntityPlayer player) {
        if (player != null && SESSIONS.containsKey(player.getUniqueID())) cancel(player);
    }

    public static void cancelAll() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null && server.getConfigurationManager() != null) {
            for (Object object : server.getConfigurationManager().playerEntityList)
                if (object instanceof EntityPlayerMP) cancelIfActive((EntityPlayerMP)object);
        }
        SESSIONS.clear();
    }

    public static boolean isEditing(EntityPlayer player) { return player != null && SESSIONS.containsKey(player.getUniqueID()); }
    private static boolean authorized(EntityPlayer player) { return player != null && player.canCommandSenderUseCommand(4, "tdm"); }
    private static boolean expired(Session s) { return System.currentTimeMillis() - s.started > 30L * 60L * 1000L; }
    private static String describe(Session s) { return (s.map.length() == 0 ? "global" : s.map) + " " + s.team.name + " kit #" + (s.index + 1); }
    private static ItemStack copy(ItemStack stack) { return stack == null ? null : stack.copy(); }
    private static void sync(EntityPlayer player) {
        player.inventory.markDirty(); player.inventoryContainer.detectAndSendChanges();
        if (player.openContainer != null && player.openContainer != player.inventoryContainer) player.openContainer.detectAndSendChanges();
    }
    private static void restore(EntityPlayer player, Session s) {
        player.inventory.setItemStack(null);
        for (int i = 0; i < 36; i++) player.inventory.mainInventory[i] = copy(s.main[i]);
        for (int i = 0; i < 4; i++) player.inventory.armorInventory[i] = copy(s.armor[i]);
        sync(player);
    }
}
