package com.hfr.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.hfr.stonedrops.StoneDropDisplayEntry;
import com.hfr.stonedrops.StoneDropDisplaySnapshot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
    public String getGuiTexture() { return "textures/gui/container/crafting_table.png"; }
    public String getOverlayIdentifier() { return OVERLAY_ID; }

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
