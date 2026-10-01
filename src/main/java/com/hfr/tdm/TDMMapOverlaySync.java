package com.hfr.tdm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMMapOverlayPacket;
import com.hfr.saveddata.EarthBoundarySavedData;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** A bounded, server-authoritative view of the selected match map. */
public final class TDMMapOverlaySync {
    private static final String SETTINGS = "xenofactionsMapOverlay";
    private static final Map<UUID, String> SENT = new HashMap<UUID, String>();
    private static final Map<UUID, Integer> NEXT = new HashMap<UUID, Integer>();

    private TDMMapOverlaySync() { }

    private static NBTTagCompound settings(EntityPlayer player) {
        NBTTagCompound entity = player.getEntityData();
        NBTTagCompound persisted = entity.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!entity.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) entity.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        NBTTagCompound value = persisted.getCompoundTag(SETTINGS);
        if (!persisted.hasKey(SETTINGS)) persisted.setTag(SETTINGS, value);
        return value;
    }

    public static boolean overlay(EntityPlayer player) { return settings(player).getBoolean("overlay"); }
    public static boolean boundary(EntityPlayer player) { return settings(player).getBoolean("boundary"); }
    public static String requestedMap(EntityPlayer player) { return settings(player).getString("map"); }

    public static void setOverlay(EntityPlayerMP player, boolean enabled, String map) {
        NBTTagCompound s = settings(player);
        s.setBoolean("overlay", enabled);
        s.setString("map", player.canCommandSenderUseCommand(4, "tdm") && map != null ? TDMManager.normalizeMapName(map) : "");
        send(player);
    }

    public static void setBoundary(EntityPlayerMP player, boolean enabled) {
        settings(player).setBoolean("boundary", enabled);
        send(player);
    }

    public static void tick(EntityPlayerMP player) {
        UUID id = player.getUniqueID();
        Integer next = NEXT.get(id);
        if (next != null && next.intValue() > 0) { NEXT.put(id, Integer.valueOf(next.intValue() - 1)); return; }
        NEXT.put(id, Integer.valueOf(19));
        send(player);
    }

    public static void clear(EntityPlayer player) {
        if (player == null) return;
        UUID id = player.getUniqueID(); SENT.remove(id); NEXT.remove(id);
    }

    public static void copySettings(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
        if (oldPlayer == null || newPlayer == null) return;
        NBTTagCompound source = oldPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag(SETTINGS);
        NBTTagCompound target = newPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        target.setTag(SETTINGS, source.copy());
        newPlayer.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, target);
    }

    public static void clearAll() { SENT.clear(); NEXT.clear(); }

    public static void send(EntityPlayerMP player) {
        NBTTagCompound data = snapshot(player);
        String fingerprint = data.toString();
        UUID id = player.getUniqueID();
        if (fingerprint.equals(SENT.get(id))) return;
        SENT.put(id, fingerprint);
        PacketDispatcher.wrapper.sendTo(new TDMMapOverlayPacket(data), player);
    }

    private static NBTTagCompound snapshot(EntityPlayerMP player) {
        NBTTagCompound data = new NBTTagCompound();
        boolean admin = player.canCommandSenderUseCommand(4, "tdm");
        boolean match = TDMManager.isEnabled(player.worldObj) && TDMManager.isCompetitivePlayer(player);
        boolean overlay = overlay(player) && (admin || match);
        data.setInteger("dim", player.dimension);
        TDMManager.TDMMap active = TDMManager.getSelectedMapData(player.worldObj);
        NBTTagCompound publicMap = new NBTTagCompound();
        if (active != null) {
            publicMap.setString("map", active.name);
            publicMap.setString("mode", TDMManager.getGameMode(player.worldObj).name());
            boolean publicBoundary = active.mapBorderEnabled && active.bounds.isComplete()
                    && active.bounds.dimension == player.dimension;
            publicMap.setBoolean("boundary", publicBoundary);
            if (publicBoundary) putArea(publicMap, "bounds", active.bounds);
            if (TDMManager.isEnabled(player.worldObj) && TDMManager.isBombMode(player.worldObj)) {
                if (active.bombsiteA.dimension == player.dimension) putArea(publicMap, "bombA", active.bombsiteA);
                if (active.bombsiteB.dimension == player.dimension) putArea(publicMap, "bombB", active.bombsiteB);
            }
        }
        data.setTag("public", publicMap);

        boolean boundaryView = boundary(player) && admin;
        if (!overlay && !boundaryView) return data;
        String name = admin ? requestedMap(player) : "";
        if (name.length() == 0 || !TDMManager.hasMap(player.worldObj, name)) name = TDMManager.getSelectedMap(player.worldObj);
        TDMManager.TDMMap map = TDMManager.getMap(player.worldObj, name);
        if (map == null) return data;
        NBTTagCompound preview = new NBTTagCompound();
        preview.setBoolean("overlay", overlay);
        preview.setString("map", map.name);
        TDMManager.TDMGameMode previewMode = name.equals(TDMManager.getSelectedMap(player.worldObj))
                ? TDMManager.getGameMode(player.worldObj) : map.mode;
        preview.setString("mode", previewMode.name());
        preview.setBoolean("boundary", boundaryView && map.bounds.isComplete() && map.bounds.dimension == player.dimension);
        if (map.bounds.dimension == player.dimension && (admin || map.mapBorderEnabled)) putArea(preview, "bounds", map.bounds);
        boolean activePair = TDMManager.isEnabled(player.worldObj) && name.equals(TDMManager.getSelectedMap(player.worldObj));
        if (admin && activePair && previewMode == TDMManager.TDMGameMode.BOMB) {
            if (map.bombsiteA.dimension == player.dimension) putArea(preview, "bombA", map.bombsiteA);
            if (map.bombsiteB.dimension == player.dimension) putArea(preview, "bombB", map.bombsiteB);
        }
        data.setTag("preview", preview);
        if (!overlay) return data;
        NBTTagList spawns = new NBTTagList();
        TDMManager.Team team = admin || previewMode == TDMManager.TDMGameMode.FFA ? null
                : TDMManager.getPlayerTeam(player.worldObj, player.getCommandSenderName());
        for (TDMManager.SpawnPoint spawn : activePair ? map.resolvedSpawns(previewMode)
                : java.util.Collections.<TDMManager.SpawnPoint>emptyList()) {
            if (spawns.tagCount() >= 256) break;
            if (spawn.dim != player.dimension || (!admin && spawn.team != team)) continue;
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("team", spawn.team == null ? "ffa" : spawn.team.name);
            tag.setInteger("x", spawn.x); tag.setInteger("y", spawn.y); tag.setInteger("z", spawn.z);
            spawns.appendTag(tag);
        }
        preview.setTag("spawns", spawns);
        // Nearby public zoning uses chunk coordinates, independent of map bounds.
        NBTTagList zones = new NBTTagList();
        ClowderTerritory.CoordPair center = ClowderTerritory.getCoordPair(player.dimension,
                (int)Math.floor(player.posX), (int)Math.floor(player.posZ));
        int cx = center.x, cz = center.z;
        preview.setInteger("zoneCX", cx); preview.setInteger("zoneCZ", cz);
        for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) {
            Ownership owner = ClowderTerritory.getOwner(player.dimension, cx + dx, cz + dz);
            if (owner == null || owner.zone == Zone.WILDERNESS) continue;
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("x", (byte)dx); tag.setByte("z", (byte)dz);
            tag.setString("type", owner.zone.name());
            tag.setInteger("color", owner.getColor());
            if (owner.zone == Zone.FACTION && owner.owner != null) tag.setString("owner", owner.owner.name);
            zones.appendTag(tag);
        }
        preview.setTag("zones", zones);
        if (admin) {
            NBTTagList exemptions = new NBTTagList();
            for (EarthBoundarySavedData.Region region : EarthBoundarySavedData.get(player.worldObj).getRegions()) {
                if (exemptions.tagCount() >= 256) break;
                if (region.dimension != player.dimension || region.maxX < player.posX - 144 || region.minX > player.posX + 144
                        || region.maxZ < player.posZ - 144 || region.minZ > player.posZ + 144) continue;
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("name", region.name);
                tag.setInteger("x1", region.minX); tag.setInteger("x2", region.maxX);
                tag.setInteger("z1", region.minZ); tag.setInteger("z2", region.maxZ);
                exemptions.appendTag(tag);
            }
            preview.setTag("exemptions", exemptions);
        }
        return data;
    }

    private static void putArea(NBTTagCompound data, String key, TDMManager.Bombsite area) {
        if (!area.isComplete()) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dim", area.dimension);
        tag.setInteger("x1", area.x1); tag.setInteger("y1", area.y1); tag.setInteger("z1", area.z1);
        tag.setInteger("x2", area.x2); tag.setInteger("y2", area.y2); tag.setInteger("z2", area.z2);
        data.setTag(key, tag);
    }
}
