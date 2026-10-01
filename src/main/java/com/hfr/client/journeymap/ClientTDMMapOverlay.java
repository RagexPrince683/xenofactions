package com.hfr.client.journeymap;

import com.hfr.inventory.gui.GUIAdminEditor;
import com.hfr.clowder.TerritoryCoordinateBounds;
import com.hfr.main.MainRegistry;
import com.hfr.tdm.BlockAreaEdges;
import cpw.mods.fml.common.Loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Client presentation of the server's permission-scoped map snapshot. */
public final class ClientTDMMapOverlay {
    private static NBTTagCompound data = new NBTTagCompound();
    private ClientTDMMapOverlay() { }
    public static void accept(NBTTagCompound next) { data = next == null ? new NBTTagCompound() : (NBTTagCompound)next.copy(); }
    public static void clear() { data = new NBTTagCompound(); }
    public static NBTTagCompound snapshot() { return data; }
    public static boolean visible() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null && mc.theWorld != null && data.getInteger("dim") == mc.thePlayer.dimension;
    }
    public static boolean areaVisible(NBTTagCompound area) { return area.hasKey("dim") && area.getInteger("dim") == data.getInteger("dim"); }

    public static void drawHud(RenderGameOverlayEvent event) {
        if (!ClientBorderVisuals.enabled() || !visible() || XFJourneyMapIntegration.isUsable()) return;
        NBTTagCompound publicMap = data.getCompoundTag("public");
        NBTTagCompound preview = data.getCompoundTag("preview");
        boolean publicAreas = publicMap.hasKey("bounds") || publicMap.hasKey("bombA") || publicMap.hasKey("bombB");
        boolean previewOn = preview.getBoolean("overlay") && preview.hasKey("map");
        if (!publicAreas && !previewOn) return;
        Minecraft mc = Minecraft.getMinecraft();
        int side = 128;
        int x = GUIAdminEditor.hasActiveSelectionPreview() || Loader.isModLoaded("journeymap")
                ? 8 : event.resolution.getScaledWidth() - side - 8, y = 8;
        int cx = (int)Math.floor(mc.thePlayer.posX), cz = (int)Math.floor(mc.thePlayer.posZ);
        Gui.drawRect(x - 3, y - 3, x + side + 3, y + side + 35, 0xDD172532);
        Gui.drawRect(x, y, x + side, y + side, 0xFF263744);
        if (previewOn) {
            NBTTagList zones = preview.getTagList("zones", 10);
            for (int i = 0; i < zones.tagCount(); i++) {
                NBTTagCompound zone = zones.getCompoundTagAt(i);
                TerritoryCoordinateBounds.Bounds bx = TerritoryCoordinateBounds.forCoordinate(preview.getInteger("zoneCX") + zone.getByte("x"));
                TerritoryCoordinateBounds.Bounds bz = TerritoryCoordinateBounds.forCoordinate(preview.getInteger("zoneCZ") + zone.getByte("z"));
                rect(x, y, side, cx, cz, bx.minInclusive, bz.minInclusive, bx.maxExclusive, bz.maxExclusive,
                        0x66000000 | (zone.getInteger("color") & 0xFFFFFF), false);
            }
        }
        rectArea(publicMap, x, y, side, cx, cz, "bounds", 0xFF6BC8FF);
        rectArea(publicMap, x, y, side, cx, cz, "bombA", 0xFFFFD262);
        rectArea(publicMap, x, y, side, cx, cz, "bombB", 0xFFFF965E);
        if (previewOn) {
            boolean sameMap = preview.getString("map").equals(publicMap.getString("map"));
            if (!sameMap || !publicMap.hasKey("bounds")) rectArea(preview, x, y, side, cx, cz, "bounds", 0xFF6BC8FF);
            if (!sameMap || !publicMap.hasKey("bombA")) rectArea(preview, x, y, side, cx, cz, "bombA", 0xFFFFD262);
            if (!sameMap || !publicMap.hasKey("bombB")) rectArea(preview, x, y, side, cx, cz, "bombB", 0xFFFF965E);
            NBTTagList exemptions = preview.getTagList("exemptions", 10);
            for (int i = 0; i < exemptions.tagCount(); i++) {
                NBTTagCompound a = exemptions.getCompoundTagAt(i);
                rect(x, y, side, cx, cz, a.getInteger("x1"), a.getInteger("z1"),
                        a.getInteger("x2") + 1L, a.getInteger("z2") + 1L, 0xFFBF9BFF, true);
            }
        }
        areaLabel(mc, publicMap, x, y, side, cx, cz, "bombA", "A", 0xFFFFD262);
        areaLabel(mc, publicMap, x, y, side, cx, cz, "bombB", "B", 0xFFFF965E);
        if (previewOn) {
            boolean sameMap = preview.getString("map").equals(publicMap.getString("map"));
            if (!sameMap || !publicMap.hasKey("bombA"))
                areaLabel(mc, preview, x, y, side, cx, cz, "bombA", "A", 0xFFFFD262);
            if (!sameMap || !publicMap.hasKey("bombB"))
                areaLabel(mc, preview, x, y, side, cx, cz, "bombB", "B", 0xFFFF965E);
        }
        NBTTagList spawns = previewOn ? preview.getTagList("spawns", 10) : new NBTTagList();
        for (int i = 0; i < spawns.tagCount(); i++) {
            NBTTagCompound spawn = spawns.getCompoundTagAt(i);
            int sx = pixel(x, side, cx, spawn.getInteger("x")), sz = pixel(y, side, cz, spawn.getInteger("z"));
            if (sx < x + 3 || sx >= x + side - 3 || sz < y + 3 || sz >= y + side - 3) continue;
            String team = spawn.getString("team");
            int color = "red".equals(team) ? 0xFFFF6565 : "blue".equals(team) ? 0xFF6BA9FF : 0xFFFFFFFF;
            Gui.drawRect(sx - 2, sz - 2, sx + 3, sz + 3, color);
            mc.fontRenderer.drawStringWithShadow("red".equals(team) ? "R" : "blue".equals(team) ? "B" : "F", sx + 4, sz - 4, color);
        }
        Gui.drawRect(x + side / 2 - 1, y + side / 2 - 1, x + side / 2 + 2, y + side / 2 + 2, 0xFF5BFFAA);
        String title = previewOn ? preview.getString("map") + " | " + preview.getString("mode")
                : publicMap.getString("map") + " | " + publicMap.getString("mode");
        mc.fontRenderer.drawStringWithShadow(mc.fontRenderer.trimStringToWidth(title, side), x, y + side + 4, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("Cyan bounds  A/B sites", x, y + side + 15, 0xDDDDDD);
        mc.fontRenderer.drawStringWithShadow("R/B/F spawn  S/W zones", x, y + side + 25, 0xDDDDDD);
    }

    private static void rectArea(NBTTagCompound layer, int x, int y, int side, int cx, int cz, String key, int color) {
        NBTTagCompound a = layer.getCompoundTag(key);
        if (!areaVisible(a)) return;
        BlockAreaEdges edges = BlockAreaEdges.of(a.getInteger("x1"), a.getInteger("z1"), a.getInteger("x2"), a.getInteger("z2"));
        rect(x, y, side, cx, cz, edges.minX, edges.minZ, edges.maxXExclusive, edges.maxZExclusive, color, true);
    }
    private static void areaLabel(Minecraft mc, NBTTagCompound layer, int x, int y, int side, int cx, int cz, String key, String text, int color) {
        NBTTagCompound a = layer.getCompoundTag(key);
        if (!areaVisible(a)) return;
        int px = pixel(x, side, cx, ((long)a.getInteger("x1") + a.getInteger("x2")) / 2L);
        int pz = pixel(y, side, cz, ((long)a.getInteger("z1") + a.getInteger("z2")) / 2L);
        if (px > x + 4 && px < x + side - 4 && pz > y + 4 && pz < y + side - 4)
            mc.fontRenderer.drawStringWithShadow(text, px - 3, pz - 4, color);
    }
    private static int pixel(int origin, int side, int center, long world) { return origin + side / 2 + (int)((world - center) * side / 256L); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static void rect(int x, int y, int side, int cx, int cz, long x1, long z1, long x2, long z2, int color, boolean outline) {
        int minX = pixel(x, side, cx, x1), maxX = pixel(x, side, cx, x2);
        int minZ = pixel(y, side, cz, z1), maxZ = pixel(y, side, cz, z2);
        if (maxX < x || minX > x + side || maxZ < y || minZ > y + side) return;
        minX = clamp(minX, x, x + side); maxX = clamp(maxX, x, x + side);
        minZ = clamp(minZ, y, y + side); maxZ = clamp(maxZ, y, y + side);
        if (maxX == minX && maxX < x + side) maxX++;
        if (maxZ == minZ && maxZ < y + side) maxZ++;
        if (maxX <= minX || maxZ <= minZ) return;
        if (!outline) { Gui.drawRect(minX, minZ, maxX, maxZ, color); return; }
        Gui.drawRect(minX, minZ, maxX, minZ + 1, color); Gui.drawRect(minX, maxZ - 1, maxX, maxZ, color);
        Gui.drawRect(minX, minZ, minX + 1, maxZ, color); Gui.drawRect(maxX - 1, minZ, maxX, maxZ, color);
    }

    /** Uses the same ground-following colored spark effect as Clowder borders. */
    public static void emitWorldBorder() {
        if (!ClientBorderVisuals.enabled() || !visible()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || mc.thePlayer.ticksExisted % 3 != 0) return;
        NBTTagCompound publicMap = data.getCompoundTag("public");
        NBTTagCompound preview = data.getCompoundTag("preview");
        int px = (int)Math.floor(mc.thePlayer.posX), pz = (int)Math.floor(mc.thePlayer.posZ);
        if (publicMap.getBoolean("boundary")) emitArea(publicMap.getCompoundTag("bounds"), px, pz);
        if (preview.getBoolean("boundary")
                && (!publicMap.getBoolean("boundary") || !preview.getString("map").equals(publicMap.getString("map"))))
            emitArea(preview.getCompoundTag("bounds"), px, pz);
    }
    private static void emitArea(NBTTagCompound area, int px, int pz) {
        if (!areaVisible(area)) return;
        BlockAreaEdges edges = BlockAreaEdges.of(area.getInteger("x1"), area.getInteger("z1"),
                area.getInteger("x2"), area.getInteger("z2"));
        emit(edges.minX, edges.minZ, edges.maxXExclusive, edges.minZ, px, pz);
        emit(edges.maxXExclusive, edges.minZ, edges.maxXExclusive, edges.maxZExclusive, px, pz);
        emit(edges.maxXExclusive, edges.maxZExclusive, edges.minX, edges.maxZExclusive, px, pz);
        emit(edges.minX, edges.maxZExclusive, edges.minX, edges.minZ, px, pz);
    }
    private static void emit(long x1, long z1, long x2, long z2, int px, int pz) {
        if (x1 == x2) {
            if (Math.abs(x1 - px) > 64) return;
            long a = Math.max(Math.min(z1, z2), (long)pz - 64L), b = Math.min(Math.max(z1, z2), (long)pz + 64L);
            if (a >= b) return;
            z1 = a; z2 = b;
        } else {
            if (Math.abs(z1 - pz) > 64) return;
            long a = Math.max(Math.min(x1, x2), (long)px - 64L), b = Math.min(Math.max(x1, x2), (long)px + 64L);
            if (a >= b) return;
            x1 = a; x2 = b;
        }
        if (x1 < Integer.MIN_VALUE || x1 > Integer.MAX_VALUE || x2 < Integer.MIN_VALUE || x2 > Integer.MAX_VALUE
                || z1 < Integer.MIN_VALUE || z1 > Integer.MAX_VALUE || z2 < Integer.MIN_VALUE || z2 > Integer.MAX_VALUE) return;
        MainRegistry.proxy.spawnSFX(Minecraft.getMinecraft().theWorld, 0, 0, 0,
                MainRegistry.proxy.SFX_BORDER, new int[] { (int)x1, (int)z1, (int)x2, (int)z2, 0x6BC8FF });
    }
}
