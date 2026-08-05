package com.hfr.tileentity.machine;

import com.hfr.blocks.machine.MachineCoalMine;
import com.hfr.handler.MultiblockHandler;
import com.hfr.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MachineNeiRecipes {
    private MachineNeiRecipes() { }

    public static final class ItemValue {
        public final ItemStack stack;
        public final float value;
        public ItemValue(ItemStack stack, float value) { this.stack = stack; this.value = value; }
    }


    public static final class ChanceEntry {
        public final ItemStack stack;
        public final int chancePercent;
        public ChanceEntry(ItemStack stack, int chancePercent) { this.stack = stack.copy(); this.chancePercent = chancePercent; }
    }

    public static ItemStack grainMillInput() { return new ItemStack(Items.wheat); }
    public static ItemStack grainMillOutput() { return new ItemStack(ModItems.flour); }
    public static int grainMillTicks() { return TileEntityMachineGrainmill.maxProgress; }
    public static ItemStack universityOutput() { return new ItemStack(ModItems.science); }
    public static ItemStack productionLineOutput() { return new ItemStack(ModItems.cog); }
    public static ItemStack templeOutput() { return new ItemStack(ModItems.scroll); }
    public static ItemStack coalMineOutput() { return new ItemStack(Items.coal); }
    public static ItemStack miner() { return new ItemStack(ModItems.miner); }
    public static ItemStack minerSupplies() { return new ItemStack(ModItems.miner_supplies); }
    public static ItemStack canary() { return new ItemStack(ModItems.canary); }
    public static int coalMineMaxSupplies() { return TileEntityMachineCoalMine.maxSupplies; }
    public static int coalMineSupplyChanceDenominator() { return 15; }
    public static int coalMineMinerLossChancePercent() { return 25; }
    public static int coalMineTicksForWorkforce(int workforce, MachineDisplaySnapshot snapshot) { return snapshot.coalRate * 20 / workforce; }
    public static int[] coalMineRotatedFootprint() { return MultiblockHandler.rotate(new MachineCoalMine(net.minecraft.block.material.Material.rock).getDimensions(), ForgeDirection.NORTH); }

    public static List<ChanceEntry> universityJams() {
        List<ChanceEntry> list = new ArrayList<ChanceEntry>();
        list.add(new ChanceEntry(new ItemStack(Items.paper).setStackDisplayName("Student Strike"), 50));
        list.add(new ChanceEntry(new ItemStack(Items.gunpowder).setStackDisplayName("Bomb Threat"), 20));
        list.add(new ChanceEntry(new ItemStack(Items.skull).setStackDisplayName("Workplace Accident"), 20));
        list.add(new ChanceEntry(new ItemStack(Items.bone).setStackDisplayName("Skeleton Attack"), 10));
        return list;
    }

    public static List<ChanceEntry> productionLineJams() {
        List<ChanceEntry> list = new ArrayList<ChanceEntry>();
        list.add(new ChanceEntry(new ItemStack(Items.paper).setStackDisplayName("Worker Strike"), 50));
        list.add(new ChanceEntry(new ItemStack(Items.skull).setStackDisplayName("Workplace Accident"), 20));
        list.add(new ChanceEntry(new ItemStack(Items.slime_ball).setStackDisplayName("Chemical Spill"), 20));
        list.add(new ChanceEntry(new ItemStack(Items.potato).setStackDisplayName("Communist Takeover"), 10));
        return list;
    }

    public static List<ItemStack> blastFurnaceInputs() {
        List<ItemStack> list = new ArrayList<ItemStack>();
        list.add(new ItemStack(Items.iron_ingot));
        list.add(new ItemStack(Blocks.iron_ore));
        return list;
    }

    public static List<ItemValue> blastFurnaceFuels() {
        List<ItemValue> list = new ArrayList<ItemValue>();
        list.add(new ItemValue(new ItemStack(Items.coal), TileEntityMachineBlastFurnace.coalValue));
        list.add(new ItemValue(new ItemStack(Blocks.coal_block), TileEntityMachineBlastFurnace.coalValue * 10));
        return list;
    }

    public static int blastFurnaceTicks() { return TileEntityMachineBlastFurnace.maxProgress / 2; }

    public static List<ItemValue> foundryMeltingInputs() {
        List<ItemValue> list = new ArrayList<ItemValue>();
        for (Item item : TileEntityFoundry.options) addIfValid(list, new ItemStack(item), TileEntityFoundry.getSteelValue(new ItemStack(item)));
        for (ItemStack ore : OreDictionary.getOres("ingotSteel")) addIfValid(list, ore == null ? null : ore.copy(), TileEntityFoundry.getSteelValue(ore));
        sort(list);
        return list;
    }

    public static List<ItemValue> foundryCastingOutputs() {
        List<ItemValue> list = new ArrayList<ItemValue>();
        for (Item item : TileEntityFoundry.options) addIfValid(list, new ItemStack(item), TileEntityFoundry.getSteelCost(item));
        return list;
    }

    public static List<ItemStack> fishingOutputsAndJams() {
        List<ItemStack> list = new ArrayList<ItemStack>();
        list.add(new ItemStack(Items.fish));
        list.add(new ItemStack(ModItems.whale_meat));
        list.add(new ItemStack(Items.stick).setStackDisplayName("Driftwood"));
        list.add(new ItemStack(Blocks.sapling));
        list.add(new ItemStack(Blocks.waterlily));
        list.add(new ItemStack(Items.bone).setStackDisplayName("Rattle Me Bones"));
        return list;
    }

    private static void addIfValid(List<ItemValue> list, ItemStack stack, float value) { if (stack != null && stack.getItem() != null && value > 0) list.add(new ItemValue(stack.copy(), value)); }
    public static void sort(List<ItemValue> list) { Collections.sort(list, new Comparator<ItemValue>() { public int compare(ItemValue a, ItemValue b) { int c = Item.itemRegistry.getNameForObject(a.stack.getItem()).compareTo(Item.itemRegistry.getNameForObject(b.stack.getItem())); return c != 0 ? c : Integer.compare(a.stack.getItemDamage(), b.stack.getItemDamage()); }}); }
}
