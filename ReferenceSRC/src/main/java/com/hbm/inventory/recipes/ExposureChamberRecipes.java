package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import static com.hbm.inventory.OreDictManager.*;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;

import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.items.machine.ItemCircuit;
import com.hbm.items.machine.ItemZirnoxRod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ExposureChamberRecipes extends SerializableRecipe {

	public static List<ExposureChamberRecipe> recipes = new ArrayList();

	@Override
	public void registerDefaults() {
		/* The chamber consumes particle capsules as an irradiation source. */

		// 59Co(n,gamma)60Co: natural cobalt stands in for its stable Co-59 isotope.
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new OreDictStack(CO.ingot()), new ItemStack(ModItems.ingot_co60)));

		// 6Li(n,alpha)3H; the breeding-rod conversion abstracts retained tritium.
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(new ItemStack(ModItems.rod_quad, 1,
						ItemBreedingRod.BreedingRodType.LITHIUM.ordinal())),
				new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.TRITIUM.ordinal())));

		// U-238(n,gamma)U-239 -> Np-239 -> Pu-239 (compressed beta-decay chain).
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.ingot_u238), new ItemStack(ModItems.ingot_pu239)));
		// Th-232(n,gamma)Th-233 -> Pa-233 -> U-233 (compressed beta-decay chain).
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.ingot_th232), new ItemStack(ModItems.ingot_u233)));

		// Incremental neutron-capture activation chains.
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.nugget_am241), new ItemStack(ModItems.nugget_am242)));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.nugget_cf252), new ItemStack(ModItems.nugget_es253)));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.nugget_pu239), new ItemStack(ModItems.nugget_pu240)));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.nugget_pu240), new ItemStack(ModItems.nugget_pu241)));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.nugget_cf249), new ItemStack(ModItems.nugget_cf250)));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.nugget_cf250), new ItemStack(ModItems.nugget_cf251)));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.nugget_cf251), new ItemStack(ModItems.nugget_cf252)));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.ingot_cm247), new ItemStack(ModItems.ingot_cm248)));
		// Cm-248 capture followed by short beta decays is compressed to Cf-249.
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new ComparableStack(ModItems.ingot_cm248), new ItemStack(ModItems.nugget_cf249)));
		// 209Bi(n,gamma)210Bi -> 210Po; natural bismuth is the target abstraction.
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_neutron),
				new OreDictStack("dustBismuth"), new ItemStack(ModItems.powder_polonium)));

		//why were these removed???
		//this is scientifically possible and highly researched
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_aelectron), new OreDictStack(U.ingot()), new ItemStack(ModItems.ingot_uranium_fuel)));
		//Semiconductor doping (THIS IS A REAL PROCESS!!!)
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_copper), new OreDictStack(SI.nugget()),
											  new ComparableStack(ModItems.circuit, 8, ItemCircuit.EnumCircuitType.CHIP_BISMOID).toStack()));

	}

	public static ExposureChamberRecipe getRecipe(ItemStack particle, ItemStack input) {
		for(ExposureChamberRecipe recipe : recipes) if(recipe.particle.matchesRecipe(particle, true) && recipe.ingredient.matchesRecipe(input, true)) return recipe;
		return null;
	}

	public static HashMap getRecipes() {

		HashMap<Object, Object> recipes = new HashMap<Object, Object>();

		for(ExposureChamberRecipe recipe : ExposureChamberRecipes.recipes) {

			Object[] array = new Object[2];

			array[1] = recipe.particle;
			AStack stack = recipe.ingredient.copy();
			stack.stacksize = 8;
			array[0] = stack;
			ItemStack output = recipe.output.copy();
			output.stackSize = 8;

			recipes.put(array, output);
		}

		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmExposureChamber.json";
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

		AStack particle = this.readAStack(obj.get("particle").getAsJsonArray());
		AStack ingredient = this.readAStack(obj.get("ingredient").getAsJsonArray());
		ItemStack output = this.readItemStack(obj.get("output").getAsJsonArray());

		ExposureChamberRecipe rec = new ExposureChamberRecipe(particle, ingredient, output);
		recipes.add(rec);
	}

	@Override
	public void writeRecipe(Object o, JsonWriter writer) throws IOException {
		ExposureChamberRecipe recipe = (ExposureChamberRecipe) o;

		writer.name("particle");
		this.writeAStack(recipe.particle, writer);
		writer.name("ingredient");
		this.writeAStack(recipe.ingredient, writer);
		writer.name("output");
		this.writeItemStack(recipe.output, writer);
	}

	public static class ExposureChamberRecipe {

		public AStack particle;
		public AStack ingredient;
		public ItemStack output;

		public ExposureChamberRecipe(AStack particle, AStack ingredient, ItemStack output) {
			this.particle = particle;
			this.ingredient = ingredient;
			this.output = output;
		}
	}
}
