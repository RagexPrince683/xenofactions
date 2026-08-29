package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBreedingRod.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BreederRecipes extends SerializableRecipe {

	private static HashMap<ComparableStack, List<BreederRecipe>> recipes = new HashMap();

	@Override
	public void registerDefaults() {

		setRecipe(BreedingRodType.LITHIUM, BreedingRodType.TRITIUM, 200);
		setRecipe(BreedingRodType.CO, BreedingRodType.CO60, 100);

		// THF is the irradiated thorium-fuel intermediate, not immediately recovered U-233.
		setRecipe(BreedingRodType.TH232, BreedingRodType.THF, 500);

		setRecipe(BreedingRodType.URANIUM, BreedingRodType.RGP, 200);

		setRecipe(BreedingRodType.U235, BreedingRodType.WASTE, 300); //not neptunium thats for sure
		setRecipe(BreedingRodType.NP237, BreedingRodType.PU238, 200);

		//setRecipe(BreedingRodType.U238, BreedingRodType.NP237, 500);
		// U-238 capture followed by beta decays is compressed to Pu-239.  Direct
		// U-238-to-Np-237 was removed: that nuclide is not the one-capture product.
		setRecipe(BreedingRodType.U238, BreedingRodType.PU239, 1000);
		setRecipe(BreedingRodType.U238, BreedingRodType.RGP, 300);

		// Direct neutron capture; retained as an advanced isotope-production route.
		setRecipe(BreedingRodType.PU238, BreedingRodType.PU239, 1000);

		setRecipe(BreedingRodType.RGP, BreedingRodType.WASTE, 200);
		//rgp = reactor grade plutonium
		//You breed Pu-239 from U-238. You can breed Pu-238 from Pu-239, but it's not really worth it.
		//setRecipe(BreedingRodType.RA226, BreedingRodType.AC227, 300);
		// this would literally be radon and radon has no real uses yet other than killing you
		//so IDC

		//AM241 -> AM242
		setRecipe(BreedingRodType.AM241, BreedingRodType.AM242, 200);

		//AM242 -> CM242
		setRecipe(BreedingRodType.AM242, BreedingRodType.CM242, 200);

		//Cm242 -> Cm243   (200)
		setRecipe(BreedingRodType.CM242, BreedingRodType.CM243, 200);
		//Cm243 -> Cm244   (300)
		setRecipe(BreedingRodType.CM243, BreedingRodType.CM244, 300);
		//Cm244 -> Cm245   (500)
		setRecipe(BreedingRodType.CM244, BreedingRodType.CM245, 500);
		//Cm245 -> Cm246   (700)
		setRecipe(BreedingRodType.CM245, BreedingRodType.CM246, 700);
		//Cm246 -> Cm247   (1000)
		setRecipe(BreedingRodType.CM246, BreedingRodType.CM247, 1000);
		//CM247 -> BK247   (2000)
		setRecipe(BreedingRodType.CM247, BreedingRodType.BK247, 2000);

		//TODO should be:

		// BK247 -> CF251 (Californium)
		//setRecipe(BreedingRodType.BK247, BreedingRodType.CF251, 3000);

		// CF251 -> ES253 (Einsteinium)
		//setRecipe(BreedingRodType.CF251, BreedingRodType.ES253, 4000);

		// ES253 -> FM255 (Fermium!) - The ultimate endgame reward
		//setRecipe(BreedingRodType.ES253, BreedingRodType.FM255, 50000);


		//Current (for my fucking mental sanity):
		//fermium from plutonium-239 or curium 244
		setRecipe(BreedingRodType.PU239, BreedingRodType.FM255, 100000);
		setRecipe(BreedingRodType.CM244, BreedingRodType.FM257, 50000);
		//Codex crying:
		// Do not collapse many captures and decays from Pu/Cm directly to fermium.
		// The available incremental actinide chain remains the intended progression.
		//me crying:
		//BROTHER. THIS IS THE ONLY SANE WAY TO EVEN ADD THESE INTO THE GAME. I AM LEAVING THEM IN....?????


		//thulium normal into tm-170
		//setRecipe(BreedingRodType.THULIUM, BreedingRodType.TM170, 200);


	}

	/** Sets recipes for single, dual, and quad rods **/
	public static void setRecipe(BreedingRodType inputType, BreedingRodType outputType, int flux) {

		addRecipe(
			new ComparableStack(new ItemStack(ModItems.rod, 1, inputType.ordinal())),
			new BreederRecipe(new ItemStack(ModItems.rod, 1, outputType.ordinal()), flux)
		);

		addRecipe(
			new ComparableStack(new ItemStack(ModItems.rod_dual, 1, inputType.ordinal())),
			new BreederRecipe(new ItemStack(ModItems.rod_dual, 1, outputType.ordinal()), flux * 2)
		);

		addRecipe(
			new ComparableStack(new ItemStack(ModItems.rod_quad, 1, inputType.ordinal())),
			new BreederRecipe(new ItemStack(ModItems.rod_quad, 1, outputType.ordinal()), flux * 3)
		);
	}

	private static void addRecipe(ComparableStack stack, BreederRecipe recipe) {
		recipes.computeIfAbsent(stack, k -> new ArrayList<>()).add(recipe);
	}

	public static List<Map.Entry<ItemStack, BreederRecipe>> getAllRecipes() {

		List<Map.Entry<ItemStack, BreederRecipe>> list = new ArrayList<>();

		for(Map.Entry<ComparableStack, List<BreederRecipe>> recipe : recipes.entrySet()) {

			ItemStack input = recipe.getKey().toStack();

			for(BreederRecipe breederRecipe : recipe.getValue()) {
				list.add(new AbstractMap.SimpleEntry<>(
					input.copy(),
					breederRecipe
				));
			}
		}

		return list;
	}

	public static BreederRecipe getOutput(ItemStack stack, int flux) {

		if(stack == null)
			return null;

		ComparableStack sta = new ComparableStack(stack).makeSingular();
		List<BreederRecipe> possible = recipes.get(sta);

		if(possible == null)
			return null;

		BreederRecipe best = null;

		for(BreederRecipe recipe : possible) {
			if(flux >= recipe.flux) {

				if(best == null || recipe.flux > best.flux) {
					best = recipe;
				}
			}
		}

		return best;
	}

	//nicer than opaque object arrays
	public static class BreederRecipe {

		public ItemStack output;
		public int flux;

		public BreederRecipe(Item output, int flux) {
			this(new ItemStack(output), flux);
		}

		public BreederRecipe(ItemStack output, int flux) {
			this.output = output;
			this.flux = flux;
		}
	}

	@Override
	public String getFileName() {
		return "hbmBreeder.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		AStack in = this.readAStack(obj.get("input").getAsJsonArray());
		int flux = obj.get("flux").getAsInt();
		ItemStack out = this.readItemStack(obj.get("output").getAsJsonArray());

		addRecipe(
			((ComparableStack) in),
			new BreederRecipe(out, flux)
		);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<ComparableStack, BreederRecipe> rec = (Entry<ComparableStack, BreederRecipe>) recipe;
		ComparableStack in = rec.getKey();

		writer.name("input");
		this.writeAStack(in, writer);
		writer.name("flux").value(rec.getValue().flux);
		writer.name("output");
		this.writeItemStack(rec.getValue().output, writer);
	}

	public static boolean hasRecipe(ItemStack stack) {

		if(stack == null)
			return false;

		ComparableStack sta =
			new ComparableStack(stack).makeSingular();

		List<BreederRecipe> possible = recipes.get(sta);

		return possible != null && !possible.isEmpty();
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}


}
