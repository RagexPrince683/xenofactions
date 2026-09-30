package com.hfr.client.journeymap;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hfr.client.journeymap.ClientClaimOverlayCache.Snapshot;
import com.hfr.clowder.ClaimOverlayData.Claim;
import com.hfr.clowder.TerritoryCoordinateBounds;
import com.hfr.config.XFConfig;
import com.hfr.lib.RefStrings;
import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.DisplayType;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.model.MapPolygonWithHoles;
import journeymap.api.v2.client.model.ShapeProperties;
import journeymap.api.v2.client.model.TextProperties;
import journeymap.api.v2.client.util.PolygonHelper;
import journeymap.api.v2.common.Context;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.util.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.world.WorldEvent;

/** JourneyMap 6 discovers this client-only class through its API annotation. */
@JourneyMapPlugin(apiVersion = "2.0.0")
public final class JourneyMap6Plugin implements IClientPlugin {
    private IClientAPI api;
    private final List<PolygonOverlay> displayed = new ArrayList<PolygonOverlay>();
    private Snapshot lastClaims;
    private String lastMap;
    private int lastDimension = Integer.MIN_VALUE;
    private boolean lastBorders, failed;

    @Override public String getModId() { return RefStrings.MODID; }

    @Override public void initialize(IClientAPI clientApi) {
        api = clientApi;
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(this);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || failed || api == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) { clear(); return; }
        if (mc.thePlayer.ticksExisted % 10 != 0) return;
        boolean accepted = XFConfig.enableJourneyMapIntegration && api.playerAccepts(getModId(), DisplayType.Polygon);
        XFJourneyMapIntegration.setApi6Ready(accepted);
        if (!accepted) { clear(); return; }
        int dimension = mc.thePlayer.dimension;
        Snapshot claims = ClientClaimOverlayCache.get(dimension);
        NBTTagCompound map = ClientTDMMapOverlay.snapshot();
        String mapKey = ClientTDMMapOverlay.visible() ? map.toString() : "";
        boolean borders = ClientBorderVisuals.enabled();
        if (dimension == lastDimension && claims == lastClaims && mapKey.equals(lastMap) && borders == lastBorders) return;
        clear();
        lastDimension = dimension; lastClaims = claims; lastMap = mapKey; lastBorders = borders;
        try {
            if (borders && claims != null) addClaims(claims, dimension);
            if (ClientTDMMapOverlay.visible() && map.getBoolean("overlay") && map.hasKey("map")) addMap(map, dimension, borders, claims == null);
        } catch (Exception failure) {
            clear(); failed = true;
            XFJourneyMapIntegration.setApi6Ready(false);
            if (MainRegistry.logger != null) MainRegistry.logger.warn("JourneyMap 6 overlay unavailable; using standalone map", failure);
        }
    }

    @SubscribeEvent public void disconnect(ClientDisconnectionFromServerEvent event) { clear(); failed = false; XFJourneyMapIntegration.setApi6Ready(false); }
    @SubscribeEvent public void unload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote) { clear(); failed = false; XFJourneyMapIntegration.setApi6Ready(false); }
    }

    private void clear() {
        if (api != null) for (PolygonOverlay overlay : displayed) api.remove(overlay);
        displayed.clear(); lastClaims = null; lastMap = null; lastDimension = Integer.MIN_VALUE;
    }

    private ShapeProperties style(int color, float fill, float stroke, float width) {
        return new ShapeProperties().setFillColor(color).setFillOpacity(fill)
                .setStrokeColor(color).setStrokeOpacity(stroke).setStrokeWidth(width);
    }

    private void show(PolygonOverlay overlay, String group, String label, boolean claim) throws Exception {
        overlay.setOverlayGroupName(group);
        if (label != null && label.length() > 0) {
            overlay.setLabel(label);
            overlay.setTextProperties(new TextProperties().setColor(0xFFFFFF).setOpacity(1F)
                    .setFontShadow(true).setMinZoom(2));
        }
        if (claim) {
            if (XFConfig.journeyMapShowMinimapClaims && XFConfig.journeyMapShowFullscreenClaims)
                overlay.setActiveUIs(Context.UI.Minimap, Context.UI.Fullscreen);
            else if (XFConfig.journeyMapShowMinimapClaims) overlay.setActiveUIs(Context.UI.Minimap);
            else if (XFConfig.journeyMapShowFullscreenClaims) overlay.setActiveUIs(Context.UI.Fullscreen);
            else return;
        } else overlay.setActiveUIs(Context.UI.Minimap, Context.UI.Fullscreen);
        api.show(overlay); displayed.add(overlay);
    }

    private void addClaims(Snapshot snapshot, int dimension) throws Exception {
        Map<String, Area> areas = new HashMap<String, Area>();
        Map<String, Claim> sample = new HashMap<String, Claim>();
        for (Claim claim : snapshot.claims) {
            TerritoryCoordinateBounds.Bounds bx = TerritoryCoordinateBounds.forCoordinate(claim.chunkX);
            TerritoryCoordinateBounds.Bounds bz = TerritoryCoordinateBounds.forCoordinate(claim.chunkZ);
            Area area = areas.get(claim.groupId);
            if (area == null) { area = new Area(); areas.put(claim.groupId, area); sample.put(claim.groupId, claim); }
            area.add(new Area(new Rectangle((int)bx.minInclusive, (int)bz.minInclusive,
                    (int)(bx.maxExclusive - bx.minInclusive), (int)(bz.maxExclusive - bz.minInclusive))));
        }
        for (Map.Entry<String, Area> entry : areas.entrySet()) {
            Claim claim = sample.get(entry.getKey());
            List<MapPolygonWithHoles> polygons = PolygonHelper.createPolygonFromArea(entry.getValue(), 64);
            boolean first = true;
            for (MapPolygonWithHoles polygon : polygons) {
                PolygonOverlay overlay = new PolygonOverlay(getModId(), dimension,
                        style(claim.color, (float)XFConfig.journeyMapClaimFillOpacity,
                                (float)XFConfig.journeyMapClaimBorderOpacity, (float)XFConfig.journeyMapClaimBorderWidth), polygon);
                show(overlay, "Faction claims and zones", first && XFConfig.journeyMapShowTerritoryLabels ? claim.label : null, true);
                first = false;
            }
        }
    }

    private void addMap(NBTTagCompound map, int dimension, boolean borders, boolean zones) throws Exception {
        if (borders) {
            if (zones) {
                NBTTagList list = map.getTagList("zones", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound zone = list.getCompoundTagAt(i);
                    TerritoryCoordinateBounds.Bounds x = TerritoryCoordinateBounds.forCoordinate(map.getInteger("zoneCX") + zone.getByte("x"));
                    TerritoryCoordinateBounds.Bounds z = TerritoryCoordinateBounds.forCoordinate(map.getInteger("zoneCZ") + zone.getByte("z"));
                    rectangle(dimension, x.minInclusive, z.minInclusive, x.maxExclusive, z.maxExclusive,
                            zone.getInteger("color"), 0.14F, 0.9F, null);
                }
            }
            area(map.getCompoundTag("bounds"), dimension, 0x6BC8FF, 0F, null);
            NBTTagList exemptions = map.getTagList("exemptions", 10);
            for (int i = 0; i < exemptions.tagCount(); i++) {
                NBTTagCompound a = exemptions.getCompoundTagAt(i);
                rectangle(dimension, a.getInteger("x1"), a.getInteger("z1"), a.getInteger("x2") + 1L,
                        a.getInteger("z2") + 1L, 0xBF9BFF, 0F, 0.9F, null);
            }
        }
        area(map.getCompoundTag("bombA"), dimension, 0xFFD262, 0.2F, "A");
        area(map.getCompoundTag("bombB"), dimension, 0xFF965E, 0.2F, "B");
        NBTTagList spawns = map.getTagList("spawns", 10);
        for (int i = 0; i < spawns.tagCount(); i++) {
            NBTTagCompound spawn = spawns.getCompoundTagAt(i);
            String team = spawn.getString("team");
            int color = "red".equals(team) ? 0xFF6565 : "blue".equals(team) ? 0x6BA9FF : 0xFFFFFF;
            rectangle(dimension, spawn.getInteger("x") - 2L, spawn.getInteger("z") - 2L,
                    spawn.getInteger("x") + 3L, spawn.getInteger("z") + 3L, color, 0.75F, 0.9F,
                    "red".equals(team) ? "R" : "blue".equals(team) ? "B" : "F");
        }
    }

    private void area(NBTTagCompound area, int dimension, int color, float fill, String label) throws Exception {
        if (!ClientTDMMapOverlay.areaVisible(area)) return;
        rectangle(dimension, Math.min(area.getInteger("x1"), area.getInteger("x2")),
                Math.min(area.getInteger("z1"), area.getInteger("z2")),
                Math.max(area.getInteger("x1"), area.getInteger("x2")) + 1L,
                Math.max(area.getInteger("z1"), area.getInteger("z2")) + 1L, color, fill, 0.9F, label);
    }

    private void rectangle(int dimension, long x1, long z1, long x2, long z2,
            int color, float fill, float stroke, String label) throws Exception {
        PolygonOverlay overlay = new PolygonOverlay(getModId(), dimension, style(color, fill, stroke, 2F),
                PolygonHelper.createBlockRect(new BlockPos((int)x1, 64, (int)z1), new BlockPos((int)x2, 64, (int)z2)));
        show(overlay, "TDM map", label, false);
    }
}
