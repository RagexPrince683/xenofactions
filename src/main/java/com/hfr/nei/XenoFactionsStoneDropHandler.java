package com.hfr.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.hfr.stonedrops.StoneDropDisplayEntry;
import com.hfr.stonedrops.StoneDropDisplaySnapshot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class XenoFactionsStoneDropHandler extends TemplateRecipeHandler {
    public static final String OVERLAY_ID = "hfr.stone_drops";
    private static final DecimalFormat CHANCE_FORMAT = new DecimalFormat("0.######%", DecimalFormatSymbols.getInstance(Locale.US));

    public String getRecipeName() { return tr("hfr.nei.stone_drops.title"); }
    public String getGuiTexture() { return "textures/gui/options_background.png"; }
    public String getOverlayIdentifier() { return OVERLAY_ID; }

    public void drawBackground(int recipe) {
        drawCleanRecipeBackground(25, 20, 111, 20);
    }

    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(OVERLAY_ID)) loadAll(); else super.loadCraftingRecipes(outputId, results);
    }

    public void loadCraftingRecipes(ItemStack result) {
        if (result == null || result.getItem() == null) return;
        for (StoneDropDisplayEntry e : snapshot()) {
            ItemStack drop = e.toStack();
            if (drop != null && drop.getItem() == result.getItem() && drop.getItemDamage() == result.getItemDamage() && ItemStack.areItemStackTagsEqual(drop, result)) arecipes.add(new CachedStoneDrop(e));
        }
    }

    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient != null && ingredient.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.stone)) loadAll();
    }

    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;
        CachedStoneDrop c = (CachedStoneDrop) arecipes.get(recipe);
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        fr.drawString(tr("hfr.nei.label.source"), 18, 5, 0x404040);
        fr.drawString(tr("hfr.nei.label.drop"), 104, 5, 0x404040);
        fr.drawString(trim(fr, tr("hfr.nei.label.chance") + ": " + CHANCE_FORMAT.format(c.entry.chance), 150), 18, 42, 0x404040);
        fr.drawString(trim(fr, tr("hfr.nei.label.y_range") + ": " + yRange(c.entry), 150), 18, 54, 0x404040);
        GL11.glColor4f(1, 1, 1, 1);
    }

    static void drawCleanRecipeBackground(int sourceX, int sourceY, int resultX, int resultY) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1, 1, 1, 1);

        drawSlotBackground(sourceX, sourceY);
        drawSlotBackground(resultX, resultY);
        drawArrow(sourceX + 37, sourceY + 8, resultX - 17);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void drawSlotBackground(int x, int y) {
        Gui.drawRect(x, y, x + 18, y + 18, 0xFF8B8B8B);
        Gui.drawRect(x + 1, y + 1, x + 17, y + 17, 0xFFE0E0E0);
        Gui.drawRect(x + 2, y + 2, x + 16, y + 16, 0xFFB8B8B8);
    }

    private static void drawArrow(int x, int centerY, int right) {
        int color = 0xFF4A4A4A;
        Gui.drawRect(x, centerY - 1, right - 5, centerY + 2, color);
        Gui.drawRect(right - 5, centerY - 4, right - 2, centerY + 5, color);
        Gui.drawRect(right - 2, centerY - 3, right + 1, centerY + 4, color);
        Gui.drawRect(right + 1, centerY - 2, right + 4, centerY + 3, color);
        Gui.drawRect(right + 4, centerY - 1, right + 7, centerY + 2, color);
    }

    private void loadAll() { for (StoneDropDisplayEntry e : snapshot()) if (e.toStack() != null) arecipes.add(new CachedStoneDrop(e)); }
    private List<StoneDropDisplayEntry> snapshot() { return StoneDropDisplaySnapshot.getClientSnapshot(); }
    private static String tr(String key) { String value = StatCollector.translateToLocal(key); return value == null ? key : value; }
    private static String trim(FontRenderer fr, String text, int width) { return fr.getStringWidth(text) <= width ? text : fr.trimStringToWidth(text, width - fr.getStringWidth("...")) + "..."; }
    private static String yRange(StoneDropDisplayEntry e) { if (e.minY == null && e.maxY == null) return tr("hfr.nei.y.unrestricted"); if (e.minY != null && e.maxY != null) return e.minY + " - " + e.maxY; if (e.minY != null) return tr("hfr.nei.label.min_y") + " " + e.minY; return tr("hfr.nei.label.max_y") + " " + e.maxY; }

    public class CachedStoneDrop extends CachedRecipe {
        private final StoneDropDisplayEntry entry;
        private final PositionedStack source;
        private final PositionedStack result;
        public CachedStoneDrop(StoneDropDisplayEntry entry) {
            this.entry = entry;
            this.source = new PositionedStack(new ItemStack(Blocks.stone), 25, 20);
            this.result = new PositionedStack(entry.toStack().copy(), 111, 20);
        }
        public PositionedStack getIngredient() { return source; }
        public PositionedStack getResult() { return result; }
    }
}
