package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.item.ItemStack;

public class AlkylationRecipes extends SerializableRecipe {
	
	private static HashMap<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> recipes = new HashMap<>();

	@Override
	public void registerDefaults() {
		// The two input tanks model olefin feed plus LPG-range isobutane; the unit makes alkylate.
		recipes.put(Fluids.UNSATURATEDS, new Triplet<>(
			new FluidStack(Fluids.LPG, 100),
			new FluidStack(Fluids.REFORMATE, 170),
			new FluidStack(Fluids.SOURGAS, 10)
		));
	}
	
	public static Triplet<FluidStack, FluidStack, FluidStack> getOutput(FluidType type) {
		return recipes.get(type);
	}
	
	public static HashMap<Object, Object[]> getRecipes() {

		HashMap<Object, Object[]> map = new HashMap<Object, Object[]>();
		
		for(Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> recipe : recipes.entrySet()) {
			ItemStack[] inputs = recipe.getValue().getX().type == Fluids.NONE
				? new ItemStack[] { ItemFluidIcon.make(recipe.getKey(), 1000) }
				: new ItemStack[] {
					ItemFluidIcon.make(recipe.getKey(), 1000),
					ItemFluidIcon.make(recipe.getValue().getX().type,	recipe.getValue().getX().fill * 10) }; // this nesting level is bird-behaviour

			map.put(inputs,
				new ItemStack[] {
					ItemFluidIcon.make(recipe.getValue().getY().type,	recipe.getValue().getY().fill * 10),
					ItemFluidIcon.make(recipe.getValue().getZ().type,	recipe.getValue().getZ().fill * 10) });
		}
		
		return map;
	}

	@Override
	public String getFileName() {
		return "hbmAlkylation.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		FluidType input = Fluids.fromName(obj.get("input").getAsString());
		FluidStack acid = readFluidStack(obj.get("acid").getAsJsonArray());
		FluidStack output1 = readFluidStack(obj.get("output1").getAsJsonArray());
		FluidStack output2 = readFluidStack(obj.get("output2").getAsJsonArray());
		
		recipes.put(input, new Triplet<>(acid, output1, output2));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> rec = (Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>>) recipe;
		
		writer.name("input").value(rec.getKey().getName());
		writer.name("acid"); writeFluidStack(rec.getValue().getX(), writer);
		writer.name("output1"); writeFluidStack(rec.getValue().getY(), writer);
		writer.name("output2"); writeFluidStack(rec.getValue().getZ(), writer);
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}
}
