package com.hfr.tdm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.saveddata.EarthBoundarySavedData;
import com.hfr.config.XFConfig;
import com.hfr.tdm.TDMManager.TDMMap;
import com.hfr.items.ItemWorldBorderWand;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.AdminSelectionSyncPacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

/** One server-owned typed area selection per administrator. */
public final class AdminSelectionManager {
    public enum Type { MAP, BOMB_A, BOMB_B, SAFEZONE, WARZONE, WILDERNESS, BORDER_EXEMPT }
    public static final class Selection {
        public final Type type;
        public final String map;
        public final int dimension;
        public final Object targetIdentity;
        public boolean hasA, hasB;
        public int ax, ay, az, bx, by, bz;
        private Selection(Type type, String map, int dimension, Object targetIdentity) {
            this.type = type; this.map = map; this.dimension = dimension; this.targetIdentity = targetIdentity;
        }
    }
    private static final Map<UUID, Selection> ACTIVE = new HashMap<UUID, Selection>();
    private AdminSelectionManager() { }
    public static Selection get(EntityPlayer player) { return player == null ? null : ACTIVE.get(player.getUniqueID()); }
    public static boolean canUse(EntityPlayer player) {
        Selection s = get(player);
        return s != null && allowed(player, s.type) && player.dimension == s.dimension
                && (!isMapType(s.type) || TDMManager.getMap(player.worldObj, s.map) == s.targetIdentity);
    }
    public static void clear(EntityPlayer player) { if (player != null) { ACTIVE.remove(player.getUniqueID()); sync(player); } }
    public static void clearAll() { ACTIVE.clear(); }

    public static String begin(EntityPlayer player, Type type, String mapName) {
        if (!allowed(player, type)) return "You do not have permission for this selection type.";
        if (TDMAdminKitEdit.isEditing(player)) return "Finish or cancel the kit inventory edit before starting an area selection.";
        String map = TDMManager.normalizeMapName(mapName);
        Object identity = isMapType(type) ? TDMManager.getMap(player.worldObj, map) : null;
        if (isMapType(type) && identity == null) return "Unknown TDM map: " + map;
        ACTIVE.put(player.getUniqueID(), new Selection(type, map, player.dimension, identity));
        sync(player);
        ItemWorldBorderWand.giveIfNeeded(player);
        return "Selecting " + type + (isMapType(type) ? " for " + map : "") + ". Left click: Point A; right click: Point B. Then commit or cancel.";
    }

    public static String point(EntityPlayer player, boolean a, int x, int y, int z) {
        Selection s = get(player);
        if (s == null) return "No active selection. Choose its type first.";
        if (!allowed(player, s.type) || player.dimension != s.dimension) { clear(player); return "Selection cancelled: permission or dimension changed."; }
        if (isMapType(s.type) && TDMManager.getMap(player.worldObj, s.map) != s.targetIdentity) { clear(player); return "Map changed or was deleted; selection cancelled."; }
        if (a) { s.ax = x; s.ay = y; s.az = z; s.hasA = true; }
        else { s.bx = x; s.by = y; s.bz = z; s.hasB = true; }
        sync(player);
        return s.type + (isMapType(s.type) ? " / " + s.map : "") + " point " + (a ? "A" : "B") + " = " + x + ", " + y + ", " + z + ".";
    }

    public static String pointHere(EntityPlayer player, boolean a) {
        return point(player, a, (int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
    }

    public static String status(EntityPlayer player) {
        Selection s = get(player);
        return s == null ? "No active admin selection." : s.type + (isMapType(s.type) ? " / " + s.map : "") +
                " in dimension " + s.dimension + ": A " + (s.hasA ? "set" : "unset") + ", B " + (s.hasB ? "set" : "unset") + ".";
    }

    public static String commit(EntityPlayer player, Type expected) {
        Selection s = get(player);
        if (s == null || (expected != null && s.type != expected)) return "Selection type mismatch or no active selection; nothing changed.";
        if (!allowed(player, s.type) || player.dimension != s.dimension) { clear(player); return "Selection cancelled: permission or dimension changed."; }
        if (!s.hasA || !s.hasB) return "Set both selection points before committing.";
        if (isMapType(s.type) && TDMManager.getMap(player.worldObj, s.map) != s.targetIdentity) { clear(player); return "Map changed or was deleted; selection cancelled."; }
        if (isMapType(s.type) && (s.ay < 0 || s.by < 0 || s.ay > 255 || s.by > 255)) return "Selection exceeds the world height.";
        if (s.type == Type.SAFEZONE || s.type == Type.WARZONE || s.type == Type.WILDERNESS) {
            if (!XFConfig.canClaimInDimension(s.dimension)) return "Clowder zones are disabled in this dimension.";
            long chunksX = (Math.floorDiv(Math.max(s.ax, s.bx), 16) - Math.floorDiv(Math.min(s.ax, s.bx), 16)) + 1L;
            long chunksZ = (Math.floorDiv(Math.max(s.az, s.bz), 16) - Math.floorDiv(Math.min(s.az, s.bz), 16)) + 1L;
            if (chunksX > 51 || chunksZ > 51 || chunksX * chunksZ > 2601) return "Zone selection exceeds the existing 51 by 51 chunk limit.";
            Zone zone = s.type == Type.SAFEZONE ? Zone.SAFEZONE : s.type == Type.WARZONE ? Zone.WARZONE : Zone.WILDERNESS;
            for (int cx = Math.floorDiv(Math.min(s.ax,s.bx),16); cx <= Math.floorDiv(Math.max(s.ax,s.bx),16); cx++)
                for (int cz = Math.floorDiv(Math.min(s.az,s.bz),16); cz <= Math.floorDiv(Math.max(s.az,s.bz),16); cz++)
                    ClowderTerritory.setZoneForCoord(player.worldObj, new ClowderTerritory.CoordPair(s.dimension, cx, cz), zone);
        } else if (s.type == Type.BORDER_EXEMPT) {
            EarthBoundarySavedData.get(player.worldObj).addRegion(s.dimension, s.ax, s.az, s.bx, s.bz);
        } else if (s.type == Type.MAP) {
            TDMManager.setMapBounds(player.worldObj, s.map, s.dimension, s.ax, s.ay, s.az, s.bx, s.by, s.bz);
        } else {
            TDMMap map = (TDMMap)s.targetIdentity;
            TDMManager.Bombsite site = s.type == Type.BOMB_A ? map.bombsiteA : map.bombsiteB;
            site.clear();
            TDMManager.setBombsite(player.worldObj, s.map, s.type == Type.BOMB_A, 1, s.dimension, s.ax, s.ay, s.az);
            TDMManager.setBombsite(player.worldObj, s.map, s.type == Type.BOMB_A, 2, s.dimension, s.bx, s.by, s.bz);
        }
        clear(player);
        return "Committed " + s.type + " selection" + (isMapType(s.type) ? " for " + s.map : "") + ".";
    }

    public static void tick(EntityPlayer player) {
        Selection s = get(player);
        if (s != null && (!allowed(player, s.type) || player.dimension != s.dimension
                || (isMapType(s.type) && TDMManager.getMap(player.worldObj, s.map) != s.targetIdentity))) {
            clear(player);
            player.addChatMessage(new ChatComponentText("Admin selection cancelled: permission, dimension, or target changed."));
        }
    }
    private static boolean isMapType(Type type) { return type == Type.MAP || type == Type.BOMB_A || type == Type.BOMB_B; }
    private static void sync(EntityPlayer player) {
        if (player instanceof EntityPlayerMP && ((EntityPlayerMP)player).playerNetServerHandler != null)
            PacketDispatcher.wrapper.sendTo(new AdminSelectionSyncPacket(get(player)), (EntityPlayerMP)player);
    }
    private static boolean allowed(EntityPlayer player, Type type) {
        return player != null && type != null && player.canCommandSenderUseCommand(isMapType(type) ? 4 : 3, isMapType(type) ? "tdm" : "xclowder");
    }
}
