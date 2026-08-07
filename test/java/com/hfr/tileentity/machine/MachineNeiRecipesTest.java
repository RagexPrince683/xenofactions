package com.hfr.tileentity.machine;

import com.hfr.main.MainRegistry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MachineNeiRecipesTest {
    @Test public void grainMillUsesRuntimeConstants() { assertEquals(TileEntityMachineGrainmill.maxProgress, MachineNeiRecipes.grainMillTicks()); }
    @Test public void universityJamChancesTotalOneHundred() { int total = 0; for (MachineNeiRecipes.ChanceEntry entry : MachineNeiRecipes.universityJams()) { total += entry.chancePercent; } assertEquals(100, total); }
    @Test public void productionLineJamChancesTotalOneHundred() { int total = 0; for (MachineNeiRecipes.ChanceEntry entry : MachineNeiRecipes.productionLineJams()) { total += entry.chancePercent; } assertEquals(100, total); }
    @Test public void coalMineChancesAndIntervalsMatchRuntimeMath() { MachineDisplaySnapshot s = MachineDisplaySnapshot.fromRuntime(); for (int workforce = 1; workforce <= 5; workforce++) { assertEquals(MainRegistry.coalRate * 20 / workforce, MachineNeiRecipes.coalMineTicksForWorkforce(workforce, s)); } assertEquals(15, MachineNeiRecipes.coalMineSupplyChanceDenominator()); assertEquals(25, MachineNeiRecipes.coalMineMinerLossChancePercent()); }
}
