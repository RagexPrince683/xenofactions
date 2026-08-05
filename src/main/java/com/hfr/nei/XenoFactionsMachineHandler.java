package com.hfr.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.hfr.blocks.ModBlocks;
import com.hfr.items.ModItems;
import com.hfr.tileentity.machine.MachineDisplaySnapshot;
import com.hfr.tileentity.machine.MachineNeiRecipes;
import com.hfr.tileentity.machine.TileEntityFoundry;
import com.hfr.tileentity.machine.TileEntityMachineBlastFurnace;
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
public abstract class XenoFactionsMachineHandler extends TemplateRecipeHandler {
    public static final String BLAST = "hfr.blast_furnace";
    public static final String FOUNDRY_MELT = "hfr.foundry_melting";
    public static final String FOUNDRY_CAST = "hfr.foundry_casting";
    public static final String NET = "hfr.fishing_net";
    public static final String GRAIN_MILL = "hfr.grain_mill";
    public static final String UNIVERSITY = "hfr.university";
    public static final String PRODUCTION_LINE = "hfr.production_line";
    public static final String TEMPLE = "hfr.temple";
    public static final String COAL_MINE = "hfr.coal_mine";
    private static final DecimalFormat NUM = new DecimalFormat("0.######", DecimalFormatSymbols.getInstance(Locale.US));
    private final String id;

    protected XenoFactionsMachineHandler(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("NEI machine handler id must not be null or empty");
        }
        this.id = id;
    }
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
        } else if (GRAIN_MILL.equals(id)) {
            arecipes.add(new SimpleRecipe(MachineNeiRecipes.grainMillInput(), MachineNeiRecipes.grainMillOutput(), grainMillLines()));
        } else if (UNIVERSITY.equals(id)) {
            addGeneratedRecipes(new ItemStack(ModBlocks.machine_uni), MachineNeiRecipes.universityOutput(), uniLines());
            for (MachineNeiRecipes.ChanceEntry jam : MachineNeiRecipes.universityJams()) {
                addGeneratedRecipes(new ItemStack(ModBlocks.machine_uni), jam.stack, uniJamLines(jam));
            }
        } else if (PRODUCTION_LINE.equals(id)) {
            addGeneratedRecipes(new ItemStack(ModBlocks.machine_factory), MachineNeiRecipes.productionLineOutput(), factoryLines());
            for (MachineNeiRecipes.ChanceEntry jam : MachineNeiRecipes.productionLineJams()) {
                addGeneratedRecipes(new ItemStack(ModBlocks.machine_factory), jam.stack, factoryJamLines(jam));
            }
        } else if (TEMPLE.equals(id)) {
            addGeneratedRecipes(new ItemStack(ModBlocks.machine_temple), MachineNeiRecipes.templeOutput(), templeLines());
        } else if (COAL_MINE.equals(id)) {
            arecipes.add(new MultiInputRecipe(new ItemStack[] { new ItemStack(ModBlocks.machine_coalmine), MachineNeiRecipes.miner(), MachineNeiRecipes.minerSupplies(), MachineNeiRecipes.canary() }, MachineNeiRecipes.coalMineOutput(), coalMineLines()));
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
        } else if (GRAIN_MILL.equals(id)) {
            if (!usage && same(stack, MachineNeiRecipes.grainMillOutput())) { loadAll(); }
            if (usage && same(stack, MachineNeiRecipes.grainMillInput())) { loadAll(); }
        } else if (UNIVERSITY.equals(id)) {
            if (!usage && same(stack, MachineNeiRecipes.universityOutput())) { addGeneratedRecipes(new ItemStack(ModBlocks.machine_uni), MachineNeiRecipes.universityOutput(), uniLines()); }
            if (!usage) { for (MachineNeiRecipes.ChanceEntry jam : MachineNeiRecipes.universityJams()) { if (same(stack, jam.stack)) { addGeneratedRecipes(new ItemStack(ModBlocks.machine_uni), jam.stack, uniJamLines(jam)); } } }
        } else if (PRODUCTION_LINE.equals(id)) {
            if (!usage && same(stack, MachineNeiRecipes.productionLineOutput())) { addGeneratedRecipes(new ItemStack(ModBlocks.machine_factory), MachineNeiRecipes.productionLineOutput(), factoryLines()); }
            if (!usage) { for (MachineNeiRecipes.ChanceEntry jam : MachineNeiRecipes.productionLineJams()) { if (same(stack, jam.stack)) { addGeneratedRecipes(new ItemStack(ModBlocks.machine_factory), jam.stack, factoryJamLines(jam)); } } }
        } else if (TEMPLE.equals(id)) {
            if (!usage && same(stack, MachineNeiRecipes.templeOutput())) { loadAll(); }
        } else if (COAL_MINE.equals(id)) {
            if (!usage && same(stack, MachineNeiRecipes.coalMineOutput())) { loadAll(); }
            if (usage && (same(stack, MachineNeiRecipes.miner()) || same(stack, MachineNeiRecipes.minerSupplies()) || same(stack, MachineNeiRecipes.canary()))) { loadAll(); }
        }
    }

    private void addGeneratedRecipes(ItemStack machine, ItemStack output, String[] lines) { arecipes.add(new SimpleRecipe(machine, output, lines)); }

    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) {
            return;
        }
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        LineRecipe r = (LineRecipe) arecipes.get(recipe);
        int y = 4;
        for (String line : r.lines()) {
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
    private String[] grainMillLines() { return new String[] { tr("hfr.nei.processing_time") + ": " + ticks(MachineNeiRecipes.grainMillTicks()), tr("hfr.nei.operational_requirements") + ": " + tr("hfr.nei.grain_mill.requirements"), tr("hfr.nei.foundation_requirements") + ": " + tr("hfr.nei.grain_mill.foundation"), tr("hfr.nei.clearance_requirements") + ": " + tr("hfr.nei.grain_mill.clearance") }; }
    private String[] uniLines() { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.no_item_input"), tr("hfr.nei.generated_output"), tr("hfr.nei.expected_output_interval") + ": " + seconds(s.uniRate), tr("hfr.nei.jam_interval") + ": " + seconds(s.uniJamRate), tr("hfr.nei.foundation_requirements") + ": " + tr("hfr.nei.university.foundation"), tr("hfr.nei.clearance_requirements") + ": " + tr("hfr.nei.university.clearance") }; }
    private String[] uniJamLines(MachineNeiRecipes.ChanceEntry jam) { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.jam_interval") + ": " + seconds(s.uniJamRate), tr("hfr.nei.jam_chance") + ": " + jam.chancePercent + "%", tr("hfr.nei.production_blocked_by_jam") + ": " + tr("hfr.nei.university.jam_note") }; }
    private String[] factoryLines() { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.no_item_input"), tr("hfr.nei.generated_output"), tr("hfr.nei.expected_output_interval") + ": " + seconds(s.factoryRate), tr("hfr.nei.rf_per_active_tick") + ": " + s.factoryConsumption, tr("hfr.nei.energy_capacity") + ": " + (s.factoryConsumption * 100), tr("hfr.nei.maximum_receive_rate") + ": " + (s.factoryConsumption * 10), tr("hfr.nei.jam_interval") + ": " + seconds(s.factoryJamRate), tr("hfr.nei.foundation_requirements") + ": " + tr("hfr.nei.production_line.foundation"), tr("hfr.nei.clearance_requirements") + ": " + tr("hfr.nei.production_line.clearance") }; }
    private String[] factoryJamLines(MachineNeiRecipes.ChanceEntry jam) { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.jam_interval") + ": " + seconds(s.factoryJamRate), tr("hfr.nei.jam_chance") + ": " + jam.chancePercent + "%", tr("hfr.nei.production_blocked_by_jam") + ": " + tr("hfr.nei.production_line.jam_note") }; }
    private String[] templeLines() { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.no_item_input"), tr("hfr.nei.generated_output"), tr("hfr.nei.expected_output_interval") + ": " + seconds(s.temple), tr("hfr.nei.operational_requirements") + ": " + tr("hfr.nei.temple.requirements") }; }
    private String[] coalMineLines() { MachineDisplaySnapshot s = MachineDisplaySnapshot.forClientDisplay(); return new String[] { tr("hfr.nei.no_item_input"), tr("hfr.nei.workforce") + ": " + tr("hfr.nei.coal_mine.workforce"), tr("hfr.nei.stored_supplies") + ": " + tr("hfr.nei.coal_mine.supplies") + " " + MachineNeiRecipes.coalMineMaxSupplies(), tr("hfr.nei.base_production_interval") + ": " + seconds(s.coalRate), tr("hfr.nei.expected_output_interval") + ": " + coalIntervals(s), tr("hfr.nei.supply_consumption_chance") + ": 1 / " + MachineNeiRecipes.coalMineSupplyChanceDenominator(), tr("hfr.nei.hazard_interval") + ": " + seconds(s.coalJamRate), tr("hfr.nei.canary_protection") + ": " + tr("hfr.nei.coal_mine.canary"), tr("hfr.nei.miner_loss_chance") + ": " + MachineNeiRecipes.coalMineMinerLossChancePercent() + "%", tr("hfr.nei.foundation_requirements") + ": " + footprint() + " " + tr("hfr.nei.coal_mine.foundation"), tr("hfr.nei.clearance_requirements") + ": " + tr("hfr.nei.coal_mine.clearance") }; }
    private static String seconds(int seconds) { return seconds + "s"; }
    private static String coalIntervals(MachineDisplaySnapshot s) { StringBuilder b = new StringBuilder(); for (int i = 1; i <= 5; i++) { if (i > 1) { b.append(", "); } int ticks = MachineNeiRecipes.coalMineTicksForWorkforce(i, s); b.append(i).append("=").append(ticks(ticks)); } return b.toString(); }
    private static String footprint() { int[] d = MachineNeiRecipes.coalMineRotatedFootprint(); return "N" + d[2] + " S" + d[3] + " W" + d[4] + " E" + d[5]; }
    private String key() { return id.substring(4).replace('.', '_'); }
    private static String ticks(int t) { return t + " ticks / " + NUM.format(t / 20D) + "s"; }
    private static boolean same(ItemStack a, ItemStack b) { return a != null && b != null && a.getItem() == b.getItem() && (b.getItemDamage() == 32767 || a.getItemDamage() == b.getItemDamage()); }
    private static String tr(String key) { String v = StatCollector.translateToLocal(key); return v == null ? key : v; }
    private static String trim(FontRenderer fr, String text, int width) { return fr.getStringWidth(text) <= width ? text : fr.trimStringToWidth(text, width - fr.getStringWidth("...")) + "..."; }

    public interface LineRecipe { String[] lines(); }

    public class SimpleRecipe extends CachedRecipe implements LineRecipe {
        private final PositionedStack in; private final PositionedStack out; private final String[] lines;
        public SimpleRecipe(ItemStack in, ItemStack out, String[] lines) { this.in = new PositionedStack(in.copy(), 30, 24); this.out = new PositionedStack(out.copy(), 116, 24); this.lines = lines; }
        public PositionedStack getIngredient() { return in; }
        public PositionedStack getResult() { return out; }
        public String[] lines() { return lines; }
    }

    public class MultiInputRecipe extends CachedRecipe implements LineRecipe {
        private final List<PositionedStack> in; private final PositionedStack out; private final String[] lines;
        public MultiInputRecipe(ItemStack[] inputs, ItemStack out, String[] lines) {
            this.in = new java.util.ArrayList<PositionedStack>();
            int x = 12;
            for (ItemStack input : inputs) {
                if (input != null && input.getItem() != null) {
                    this.in.add(new PositionedStack(input.copy(), x, 24));
                    x += 20;
                }
            }
            this.out = new PositionedStack(out.copy(), 116, 24);
            this.lines = lines;
        }
        public List<PositionedStack> getIngredients() { return in; }
        public PositionedStack getResult() { return out; }
        public String[] lines() { return lines; }
    }
}
