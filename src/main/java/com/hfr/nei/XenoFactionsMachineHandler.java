package com.hfr.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.hfr.items.ModItems;
import com.hfr.tileentity.machine.MachineDisplaySnapshot;
import com.hfr.tileentity.machine.MachineNeiRecipes;
import com.hfr.tileentity.machine.TileEntityFoundry;
import com.hfr.tileentity.machine.TileEntityMachineBlastFurnace;
import com.hfr.tileentity.machine.TileEntityMachineWindmill;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class XenoFactionsMachineHandler extends TemplateRecipeHandler {
    public static final String BLAST = "hfr.blast_furnace";
    public static final String FOUNDRY_MELT = "hfr.foundry_melting";
    public static final String FOUNDRY_CAST = "hfr.foundry_casting";
    public static final String NET = "hfr.fishing_net";
    public static final String WIND = "hfr.windmill";
    private static final DecimalFormat NUM = new DecimalFormat("0.######", DecimalFormatSymbols.getInstance(Locale.US));
    private final String id;

    public XenoFactionsMachineHandler(String id) { this.id = id; }
    public String getRecipeName() { return tr("hfr.nei." + key() + ".title"); }
    public String getGuiTexture() { return "textures/gui/options_background.png"; }
    public String getOverlayIdentifier() { return id; }

    public void drawBackground(int recipe) { XenoFactionsStoneDropHandler.drawCleanRecipeBackground(30, 24, 116, 24); }

    public void loadCraftingRecipes(String outputId, Object... results) { if (id.equals(outputId)) loadAll(); else super.loadCraftingRecipes(outputId, results); }
    public void loadCraftingRecipes(ItemStack result) { if (result == null) return; loadMatching(result, false); }
    public void loadUsageRecipes(ItemStack ingredient) { if (ingredient == null) return; loadMatching(ingredient, true); }

    private void loadAll() {
        if (BLAST.equals(id)) {
            for (ItemStack input : MachineNeiRecipes.blastFurnaceInputs()) {
                arecipes.add(new SimpleRecipe(input, new ItemStack(ModItems.ingot_steel), blastLines()));
            }
        } else if (FOUNDRY_MELT.equals(id)) {
            for (MachineNeiRecipes.ItemValue input : MachineNeiRecipes.foundryMeltingInputs()) {
                arecipes.add(new SimpleRecipe(input.stack, new ItemStack(ModItems.ingot_steel), meltLines(input.value)));
            }
        } else if (FOUNDRY_CAST.equals(id)) {
            for (MachineNeiRecipes.ItemValue castingOutput : MachineNeiRecipes.foundryCastingOutputs()) {
                arecipes.add(new SimpleRecipe(new ItemStack(ModItems.ingot_steel), castingOutput.stack, castLines(castingOutput.value)));
            }
        } else if (NET.equals(id)) {
            arecipes.add(new SimpleRecipe(new ItemStack(Blocks.water), new ItemStack(Items.fish), netLines(false)));
            arecipes.add(new SimpleRecipe(new ItemStack(Blocks.water), new ItemStack(ModItems.whale_meat), netLines(true)));
            for (ItemStack jam : MachineNeiRecipes.fishingOutputsAndJams().subList(2, 6)) {
                arecipes.add(new SimpleRecipe(new ItemStack(Blocks.water), jam, jamLines(jam)));
            }
        } else if (WIND.equals(id)) {
            arecipes.add(new SimpleRecipe(new ItemStack(Blocks.air), new ItemStack(Blocks.redstone_block), windLines()));
        }
    }

    private void loadMatching(ItemStack stack, boolean usage) {
        if (BLAST.equals(id)) {
            for (ItemStack input : MachineNeiRecipes.blastFurnaceInputs()) {
                if (same(stack, input) && usage) {
                    arecipes.add(new SimpleRecipe(input, new ItemStack(ModItems.ingot_steel), blastLines()));
                }
            }
            if (same(stack, new ItemStack(ModItems.ingot_steel)) && !usage) {
                loadAll();
            }
            for (MachineNeiRecipes.ItemValue fuel : MachineNeiRecipes.blastFurnaceFuels()) {
                if (same(stack, fuel.stack) && usage) {
                    arecipes.add(new SimpleRecipe(fuel.stack, new ItemStack(ModItems.ingot_steel), fuelLines(fuel.value)));
                }
            }
        } else if (FOUNDRY_MELT.equals(id)) {
            for (MachineNeiRecipes.ItemValue input : MachineNeiRecipes.foundryMeltingInputs()) {
                if (same(stack, input.stack) && usage) {
                    arecipes.add(new SimpleRecipe(input.stack, new ItemStack(ModItems.ingot_steel), meltLines(input.value)));
                }
            }
            if (same(stack, new ItemStack(Items.coal)) && usage) {
                arecipes.add(new SimpleRecipe(new ItemStack(Items.coal), new ItemStack(ModItems.ingot_steel), fuelLines(TileEntityFoundry.maxHeat)));
            }
        } else if (FOUNDRY_CAST.equals(id)) {
            for (MachineNeiRecipes.ItemValue castingOutput : MachineNeiRecipes.foundryCastingOutputs()) {
                if (same(stack, castingOutput.stack) && !usage) {
                    arecipes.add(new SimpleRecipe(
                        new ItemStack(ModItems.ingot_steel),
                        castingOutput.stack,
                        castLines(castingOutput.value)
                    ));
                }
            }
        } else if (NET.equals(id)) {
            for (ItemStack fishingOutput : MachineNeiRecipes.fishingOutputsAndJams()) {
                if (same(stack, fishingOutput) && !usage) {
                    boolean isWhale = fishingOutput.getItem() == ModItems.whale_meat;
                    boolean isNormalOutput = fishingOutput.getItem() == Items.fish || isWhale;

                    arecipes.add(new SimpleRecipe(
                        new ItemStack(Blocks.water),
                        fishingOutput,
                        isNormalOutput ? netLines(isWhale) : jamLines(fishingOutput)
                    ));
                }
            }
        }
    }

    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) {
            return;
        }
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        SimpleRecipe r = (SimpleRecipe) arecipes.get(recipe);
        int y = 4;
        for (String line : r.lines) {
            fr.drawString(trim(fr, line, 160), 6, y, 0x404040);
            y += 10;
        }
        GL11.glColor4f(1, 1, 1, 1);
    }
    private String[] blastLines() { return new String[] { tr("hfr.nei.processing_time") + ": " + ticks(TileEntityMachineBlastFurnace.maxProgress / 2), tr("hfr.nei.fuel_units") + ": 1", tr("hfr.nei.structure") + ": " + tr("hfr.nei.blast.structure") }; }
    private String[] fuelLines(float value) { return new String[] { tr("hfr.nei.fuel_value") + ": " + NUM.format(value), tr("hfr.nei.blast.fuel.note") }; }
    private String[] meltLines(float value) { return new String[] { tr("hfr.nei.steel_units") + ": " + NUM.format(value), tr("hfr.nei.fuel_value") + ": 1 " + tr("hfr.nei.heat_unit"), tr("hfr.nei.processing_time") + ": " + ticks(21) }; }
    private String[] castLines(float cost) { return new String[] { tr("hfr.nei.steel_units") + ": " + NUM.format(cost), tr("hfr.nei.processing_time") + ": " + ticks(TileEntityFoundry.castTime) }; }
    private String[] netLines(boolean whale) { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.output_chance") + ": " + (whale ? s.whaleChance : 100 - s.whaleChance) + "%", tr("hfr.nei.biome") + ": " + s.superFishrate + "/" + s.goodFishrate + "/" + s.averageFishrate + "/" + s.crapFishrate + "s", tr("hfr.nei.requirements") + ": " + tr("hfr.nei.net.requirements") }; }
    private String[] jamLines(ItemStack jam) { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); String chance = jam.getItem() == Items.stick ? "50%" : jam.getItem() == Items.bone ? "10%" : "20%"; return new String[] { tr("hfr.nei.expected_interval") + ": " + s.jamRate + "s", tr("hfr.nei.jam_chance") + ": " + chance, tr("hfr.nei.net.jam_note") }; }
    private String[] windLines() { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.rf_per_tick") + ": " + s.windmillProduction, tr("hfr.nei.storage") + ": " + (s.windmillProduction * 10), tr("hfr.nei.extraction_rate") + ": " + s.windmillProduction, tr("hfr.nei.wind.requirements") }; }
    private String key() { return id.substring(4).replace('.', '_'); }
    private static String ticks(int t) { return t + " ticks / " + NUM.format(t / 20D) + "s"; }
    private static boolean same(ItemStack a, ItemStack b) { return a != null && b != null && a.getItem() == b.getItem() && (b.getItemDamage() == 32767 || a.getItemDamage() == b.getItemDamage()); }
    private static String tr(String key) { String v = StatCollector.translateToLocal(key); return v == null ? key : v; }
    private static String trim(FontRenderer fr, String text, int width) { return fr.getStringWidth(text) <= width ? text : fr.trimStringToWidth(text, width - fr.getStringWidth("...")) + "..."; }

    public class SimpleRecipe extends CachedRecipe {
        private final PositionedStack in; private final PositionedStack out; private final String[] lines;
        public SimpleRecipe(ItemStack in, ItemStack out, String[] lines) { this.in = new PositionedStack(in.copy(), 30, 24); this.out = new PositionedStack(out.copy(), 116, 24); this.lines = lines; }
        public PositionedStack getIngredient() { return in; }
        public PositionedStack getResult() { return out; }
    }
}
