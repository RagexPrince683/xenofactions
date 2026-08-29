package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;

import net.minecraft.item.ItemStack;

public class ElectrolyserFluidRecipes extends SerializableRecipe {

	public static HashMap<FluidType, ElectrolysisRecipe> recipes = new HashMap();

	@Override
	public void registerDefaults() {
		// Gas fluid units are volume-like batches: electrolysis therefore keeps the 2:1 hydrogen/deuterium to oxygen ratio.
		recipes.put(Fluids.WATER, new ElectrolysisRecipe(2_000, new FluidStack(Fluids.HYDROGEN, 400), new FluidStack(Fluids.OXYGEN, 200), 10));
		recipes.put(Fluids.HEAVYWATER, new ElectrolysisRecipe(2_000, new FluidStack(Fluids.DEUTERIUM, 400), new FluidStack(Fluids.OXYGEN, 200), 10));
		recipes.put(Fluids.VITRIOL, new ElectrolysisRecipe(1_000, new FluidStack(Fluids.SULFURIC_ACID, 500), new FluidStack(Fluids.CHLORINE, 500), new ItemStack(ModItems.powder_iron), new ItemStack(ModItems.ingot_mercury)));
		recipes.put(Fluids.SLOP, new ElectrolysisRecipe(1_000, new FluidStack(Fluids.MERCURY, 250), new FluidStack(Fluids.NONE, 0), new ItemStack(ModItems.niter, 2), new ItemStack(ModItems.powder_limestone, 2), new ItemStack(ModItems.sulfur)));

		// Aqueous chloride electrolysis releases chlorine and hydrogen. The two-output machine cannot also retain lye;
		// sodium hydroxide remains a separate chemical-plant product rather than incorrectly yielding alkali metal here.
		recipes.put(Fluids.BRINE, new ElectrolysisRecipe(1_000, new FluidStack(Fluids.CHLORINE, 250), new FluidStack(Fluids.HYDROGEN, 250)));
		recipes.put(Fluids.POTASSIUM_CHLORIDE, new ElectrolysisRecipe(1_000, new FluidStack(Fluids.CHLORINE, 250), new FluidStack(Fluids.HYDROGEN, 250)));

		//molten salt -> 2sodium and 2chlorine
		recipes.put(Fluids.MOLTEN_SALT, new ElectrolysisRecipe(1_000, new FluidStack(Fluids.SODIUM, 500), new FluidStack(Fluids.CHLORINE, 500)));

		// CACL2 is an aqueous processing liquor, not a molten calcium-metal feed; calcium remains supplied by its existing thermal route.

		recipes.put(Fluids.MOLTEN_STRONTIUM_CHLORIDE,
			new ElectrolysisRecipe(
				1000,
				new FluidStack(Fluids.CHLORINE, 500),
				new FluidStack(Fluids.NONE, 0),
				new ItemStack(ModItems.powder_strontium, 1)
			));

		// Electrochemical recovery from chloride-bearing refinery solutions.
		recipes.put(Fluids.RHODIUM_SOLUTION,
					new ElectrolysisRecipe(
						1000,
						new FluidStack(Fluids.NONE, 0),
						new FluidStack(Fluids.HCL, 500),
						new ItemStack(ModItems.powder_rhodium, 1)
					));

		recipes.put(Fluids.IRIDIUM_SOLUTION,
					new ElectrolysisRecipe(
						1000,
						new FluidStack(Fluids.NONE, 0),
						new FluidStack(Fluids.ACIDWASTE, 500),
						new ItemStack(ModItems.powder_iridium, 1)
					));

		// This is the mod's coarse anhydrous fluoride-electrolyte abstraction. It deliberately is not a water electrolysis recipe.
		recipes.put(Fluids.HYDROFLUORIC_ACID,
					new ElectrolysisRecipe(
						1000,
						new FluidStack(Fluids.FLUORINE, 500),
						new FluidStack(Fluids.HYDROGEN, 500)
					));





		//recipes.put(Fluids.MOLTEN_BARIUM_CHLORIDE,
		//	new ElectrolysisRecipe(
		//		1000,
		//		new FluidStack(Fluids.CHLORINE, 500),
		//		new FluidStack(Fluids.NONE, 0),
		//		new ItemStack(ModItems.ingot_barium, 1)
		//	)
		//);

	}

	public static HashMap getRecipes() {

		HashMap<Object, Object[]> recipes = new HashMap<Object, Object[]>();

		for(Entry<FluidType, ElectrolysisRecipe> entry : ElectrolyserFluidRecipes.recipes.entrySet()) {

			ElectrolysisRecipe recipe = entry.getValue();
			FluidStack input = new FluidStack(entry.getKey(), recipe.amount);
			List outputs = new ArrayList();
			if(recipe.output1.type != Fluids.NONE) outputs.add(ItemFluidIcon.make(recipe.output1));
			if(recipe.output2.type != Fluids.NONE) outputs.add(ItemFluidIcon.make(recipe.output2));
			for(ItemStack byproduct : recipe.byproduct) outputs.add(byproduct);

			recipes.put(ItemFluidIcon.make(input), outputs.toArray());
		}

		return recipes;
	}
	public static ElectrolysisRecipe getRecipe(FluidType type) {
		if(type == null)
			return null;
		return recipes.get(type);
	}

	@Override
	public String getFileName() {
		return "hbmElectrolyzerFluid.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		FluidStack input = this.readFluidStack(obj.get("input").getAsJsonArray());
		FluidStack output1 = this.readFluidStack(obj.get("output1").getAsJsonArray());
		FluidStack output2 = this.readFluidStack(obj.get("output2").getAsJsonArray());

		int duration = 20;
		if(obj.has("duration")) duration = obj.get("duration").getAsInt();

		ItemStack[] byproducts = new ItemStack[0];
		if(obj.has("byproducts")) byproducts = this.readItemStackArray(obj.get("byproducts").getAsJsonArray());

		recipes.put(input.type, new ElectrolysisRecipe(input.fill, output1, output2, duration, byproducts));
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<FluidType, ElectrolysisRecipe> rec = (Entry) recipe;

		writer.name("input"); this.writeFluidStack(new FluidStack(rec.getKey(), rec.getValue().amount), writer);
		writer.name("output1"); this.writeFluidStack(rec.getValue().output1, writer);
		writer.name("output2"); this.writeFluidStack(rec.getValue().output2, writer);

		if(rec.getValue().byproduct != null && rec.getValue().byproduct.length > 0) {
			writer.name("byproducts").beginArray();
			for(ItemStack stack : rec.getValue().byproduct) this.writeItemStack(stack, writer);
			writer.endArray();
		}

		writer.name("duration").value(rec.getValue().duration);
	}

	public static class ElectrolysisRecipe {
		public FluidStack output1;
		public FluidStack output2;
		public int amount;
		public ItemStack[] byproduct;
		public int duration;

		public ElectrolysisRecipe(int amount, FluidStack output1, FluidStack output2, ItemStack... byproduct) {
			this.output1 = output1;
			this.output2 = output2;
			this.amount = amount;
			this.byproduct = byproduct;
			this.duration = 20;
		}
		public ElectrolysisRecipe(int amount, FluidStack output1, FluidStack output2, int duration, ItemStack... byproduct) {
			this.output1 = output1;
			this.output2 = output2;
			this.amount = amount;
			this.byproduct = byproduct;
			this.duration = duration;
		}
	}


}
