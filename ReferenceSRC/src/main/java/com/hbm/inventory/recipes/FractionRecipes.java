package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.Tuple.Pair;

import net.minecraft.item.ItemStack;

public class FractionRecipes extends SerializableRecipe {

	private static Map<FluidType, Pair<FluidStack, FluidStack>> fractions = new HashMap();

	@Override
	public void registerDefaults() {
		// Atmospheric and vacuum separation only split adjacent boiling ranges; liquid outputs total 100 mB.
		fractions.put(Fluids.HEAVYOIL,			new Pair(new FluidStack(Fluids.HEAVYOIL_VACUUM,	70),	new FluidStack(Fluids.SMEAR,				30)));
		fractions.put(Fluids.HEAVYOIL_VACUUM,	new Pair(new FluidStack(Fluids.HEATINGOIL_VACUUM,	60),	new FluidStack(Fluids.BITUMEN,			40)));
		fractions.put(Fluids.SMEAR,				new Pair(new FluidStack(Fluids.HEATINGOIL,				60),		new FluidStack(Fluids.LUBRICANT,			40)));
		fractions.put(Fluids.LIGHTOIL,			new Pair(new FluidStack(Fluids.DIESEL,					55),		new FluidStack(Fluids.KEROSENE,				45)));
		fractions.put(Fluids.LIGHTOIL_DS,		new Pair(new FluidStack(Fluids.DIESEL,					55),		new FluidStack(Fluids.KEROSENE,				45)));
		// Cracked light distillate retains the same gameplay cut points, but yields the
		// unsaturated diesel stream that must be hydrotreated before normal diesel use.
		fractions.put(Fluids.LIGHTOIL_CRACK,	new Pair(new FluidStack(Fluids.DIESEL_CRACK,			55),	new FluidStack(Fluids.KEROSENE,				45)));
		fractions.put(Fluids.COALOIL,			new Pair(new FluidStack(Fluids.OIL,					70),		new FluidStack(Fluids.COALCREOSOTE,			30)));
		fractions.put(Fluids.COALCREOSOTE,		new Pair(new FluidStack(Fluids.COALOIL,					10),		new FluidStack(Fluids.BITUMEN,				90)));
		fractions.put(Fluids.REFORMATE,			new Pair(new FluidStack(Fluids.AROMATICS,				40),		new FluidStack(Fluids.XYLENE,				60)));
		fractions.put(Fluids.LIGHTOIL_VACUUM,	new Pair(new FluidStack(Fluids.KEROSENE,				70),		new FluidStack(Fluids.REFORMGAS,			30)));
		fractions.put(Fluids.EGG,				new Pair(new FluidStack(Fluids.CHOLESTEROL,				50),		new FluidStack(Fluids.RADIOSOLVENT,			50)));
		fractions.put(Fluids.OIL_COKER,			new Pair(new FluidStack(Fluids.CRACKOIL,				30),		new FluidStack(Fluids.HEATINGOIL,			70)));
		fractions.put(Fluids.NAPHTHA_COKER,		new Pair(new FluidStack(Fluids.NAPHTHA_CRACK,			75),		new FluidStack(Fluids.LIGHTOIL_CRACK,		25)));
		fractions.put(Fluids.CHLOROCALCITE_MIX, new Pair(new FluidStack(Fluids.CHLOROCALCITE_CLEANED,	50),		new FluidStack(Fluids.COLLOID,				50)));
		fractions.put(Fluids.METHYLENE,			new Pair(new FluidStack(Fluids.GAS,						70),		new FluidStack(Fluids.CARBONDIOXIDE,		30)));
		//fractions.put(Fluids.MORKINE,			new Pair(new FluidStack(Fluids.UNSATURATEDS,			40),		new FluidStack(Fluids.HYDROGEN,				25)));

	}

	public static Pair<FluidStack, FluidStack> getFractions(FluidType oil) {
		return fractions.get(oil);
	}

	public static HashMap<Object, Object> getFractionRecipesForNEI() {

		HashMap<Object, Object> recipes = new HashMap();

		for(Entry<FluidType, Pair<FluidStack, FluidStack>> recipe : fractions.entrySet()) {
			ItemStack[] out = new ItemStack[] {
					ItemFluidIcon.make(recipe.getValue().getKey()),
					ItemFluidIcon.make(recipe.getValue().getValue())
			};

			recipes.put(ItemFluidIcon.make(recipe.getKey(), 100), out);
		}

		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmFractions.json";
	}

	@Override
	public String getComment() {
		return "Inputs are always 100mB, set output quantities accordingly.";
	}

	@Override
	public Object getRecipeObject() {
		return fractions;
	}

	@Override
	public void deleteRecipes() {
		fractions.clear();
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		FluidType input = Fluids.fromName(obj.get("input").getAsString());
		FluidStack output1 = this.readFluidStack(obj.get("output1").getAsJsonArray());
		FluidStack output2 = this.readFluidStack(obj.get("output2").getAsJsonArray());

		fractions.put(input, new Pair(output1, output2));
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<FluidType, Pair<FluidStack, FluidStack>> rec = (Entry<FluidType, Pair<FluidStack, FluidStack>>) recipe;

		writer.name("input").value(rec.getKey().getName());
		writer.name("output1"); this.writeFluidStack(rec.getValue().getKey(), writer);
		writer.name("output2"); this.writeFluidStack(rec.getValue().getValue(), writer);
	}
}
