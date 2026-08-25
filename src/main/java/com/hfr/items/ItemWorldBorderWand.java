package com.hfr.items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;

/** Marker item and server-owned, per-administrator temporary corner selections. */
public final class ItemWorldBorderWand extends Item {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<UUID, Selection>();
    public ItemWorldBorderWand() { setMaxStackSize(1); setMaxDamage(0); }
    public static Selection get(EntityPlayer player) { return SELECTIONS.get(player.getUniqueID()); }
    public static void clear(EntityPlayer player) { SELECTIONS.remove(player.getUniqueID()); }
    public static boolean select(EntityPlayer player, boolean first, int x, int z) {
        UUID id = player.getUniqueID(); Selection selection = SELECTIONS.get(id);
        if (selection == null) { selection = new Selection(); SELECTIONS.put(id, selection); }
        if (!first && selection.hasPos1 && selection.dimension != player.dimension) {
            player.addChatMessage(new ChatComponentText("World border exemption position 2 must be in dimension " + selection.dimension + "."));
            return false;
        }
        if (first) { selection.hasPos1=true; selection.x1=x; selection.z1=z; selection.dimension=player.dimension; selection.hasPos2=false; }
        else { selection.hasPos2=true; selection.x2=x; selection.z2=z; selection.dimension=player.dimension; }
        player.addChatMessage(new ChatComponentText("World border exemption position " + (first ? "1" : "2") + " set to X=" + x + ", Z=" + z + ", dimension=" + player.dimension + "."));
        return true;
    }
    public static final class Selection {
        public boolean hasPos1, hasPos2; public int dimension, x1, z1, x2, z2;
    }
}
