package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import static com.hbm.inventory.OreDictManager.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.EmptyAStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Pair;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CyclotronRecipes extends SerializableRecipe {

	public static HashMap<Pair<ComparableStack, AStack>, Pair<ItemStack, Integer>> recipes = new HashMap();


	@Override
	public void registerDefaults() {

		/*
		 * The cyclotron accepts the legacy ion-part items, not neutron capsules.
		 * Keep neutron captures in the exposure chamber, whose capsule input is
		 * consumed as an irradiation source.
		 */

		//hey how about we stop removing shit without a good replacement yeah
		makeRecipe(new ComparableStack(ModItems.part_lithium), new OreDictStack("dustBeryllium"), new ItemStack(ModItems.part_carbon), 6); // Li + Be → C (simplified)
		makeRecipe(new ComparableStack(ModItems.part_copper), new OreDictStack("dustNickel"), new ItemStack(ModItems.powder_cobalt), 1);
		makeRecipe(new ComparableStack(ModItems.part_copper), new OreDictStack("dustZinc"), new ItemStack(ModItems.powder_gallium), 1);
		makeRecipe(new ComparableStack(ModItems.neutron_reflector), new OreDictStack("dustBismuth"), new ItemStack(ModItems.powder_polonium), 1);
		makeRecipe(
			new ComparableStack(ModItems.powder_uranium),
			new ComparableStack(Items.redstone),
			new ItemStack(ModItems.powder_actinium),
			2
		);
		makeRecipe(new ComparableStack(ModItems.powder_uranium), new OreDictStack("nuggetUranium238"), new ItemStack(ModItems.nugget_pu239), 5);
		makeRecipe(new ComparableStack(ModItems.ingot_actinium), new OreDictStack("dustActinium227"), new ItemStack(ModItems.francium_ingot), 3);
		makeRecipe(new ComparableStack(ModItems.nugget_cf249), new ComparableStack(ModItems.powder_poison), new ItemStack(ModItems.dubnium_nugget), 6);
		makeRecipe(new ComparableStack(ModItems.nugget_pu241), new ComparableStack(Items.redstone), new ItemStack(ModItems.nugget_am241), 4);


		// Cf-249 + C-12 -> Rf + xn. Carbon is the existing C-12 ion abstraction.
		// The nugget output represents the very small heavy-ion fusion yield.
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(ModItems.nugget_cf249),
				new ItemStack(ModItems.rutherfordium_nugget), 4);
		//so I guess the cyclotron just doesn't have a use anymore according to codex?
	}

	private static void makeRecipe(ComparableStack part, AStack in, ItemStack out, int amat) {
		recipes.put(new Pair(part, in), new Pair(out, amat));
	}

	public static Object[] getOutput(ItemStack stack, ItemStack box) {

		if(stack == null || stack.getItem() == null || box == null)
			return null;

		ComparableStack boxStack = new ComparableStack(box).makeSingular();
		ComparableStack comp = new ComparableStack(stack).makeSingular();

		//boo hoo we iterate over a hash map, cry me a river
		Pair<ItemStack, Integer> fallback = null;

		for(Entry<Pair<ComparableStack, AStack>, Pair<ItemStack, Integer>> entry : recipes.entrySet()) {

			//System.out.println("---- CHECKING RECIPE ----");
			//System.out.println("Part: " + entry.getKey().getKey().toStack());
			//System.out.println("Input type: " + entry.getKey().getValue().getClass().getSimpleName());
			//System.out.println("BoxStack: " + boxStack.toStack());
			//System.out.println("Input stack: " + (stack == null ? "null" : stack.toString()));
			//OK SHUT UP

			AStack input = entry.getKey().getValue();

			if(entry.getKey().getKey().isApplicable(boxStack)) {

				// FIRST: try specific recipes
				if(!(input instanceof EmptyAStack) && input.matchesRecipe(stack, true)) {
					return new Object[] {
						entry.getValue().getKey().copy(),
						entry.getValue().getValue()
					};
				}

				// store fallback (EmptyAStack)
				if(input instanceof EmptyAStack) {
					fallback = entry.getValue();
				}
			}
		}

		// ONLY use EmptyAStack if nothing else matched
		if(fallback != null) {
			return new Object[] {
				fallback.getKey().copy(),
				fallback.getValue()
			};
		}

		//there's literally 0 reason why this doesn't work yet it refuses, fuck this

		/*Pair<ItemStack, Integer> output = recipes.get(new Pair(boxStack, comp));

		if(output != null) {
			return new Object[] { output.getKey().copy(), output.getValue() };
		}

		for(String name : ItemStackUtil.getOreDictNames(stack)) {
			OreDictStack ods = new OreDictStack(name);
			output = recipes.get(new Pair(new ComparableStack(ModItems.part_beryllium), new OreDictStack("dustCobalt")));

			if(output != null) {
				return new Object[] { output.getKey().copy(), output.getValue() };
			}
		}*/

		return null;
	}

	public static Map<Object[], Object> getRecipes() {

		Map<Object[], Object> map = new HashMap<Object[], Object>();

		for(Entry<Pair<ComparableStack, AStack>, Pair<ItemStack, Integer>> entry : recipes.entrySet()) {
			AStack input = entry.getKey().getValue();
			if (input == null) continue;

			List<ItemStack> stack = input.extractForNEI();

			if(stack == null) continue;

			for(ItemStack ingredient : stack) {
				if(ingredient == null) continue;

				map.put(new ItemStack[] {
					entry.getKey().getKey().toStack(),
					ingredient
				}, entry.getValue().getKey());
			}
		}

		return map;
	}

	@Override
	public String getFileName() {
		return "hbmCyclotron.json";
	}

	@Override
	public Object getRecipeObject() {
		return this.recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonArray particle = ((JsonObject)recipe).get("particle").getAsJsonArray();
		JsonArray input = ((JsonObject)recipe).get("input").getAsJsonArray();
		JsonArray output = ((JsonObject)recipe).get("output").getAsJsonArray();
		int antimatter = ((JsonObject)recipe).get("antimatter").getAsInt();
		ItemStack partStack = this.readItemStack(particle);
		AStack inStack = this.readAStack(input);
		ItemStack outStack = this.readItemStack(output);

		this.recipes.put(new Pair(new ComparableStack(partStack), inStack),  new Pair(outStack, antimatter));
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		try{
			Entry<Pair<ComparableStack, AStack>, Pair<ItemStack, Integer>> rec = (Entry<Pair<ComparableStack, AStack>, Pair<ItemStack, Integer>>) recipe;

			writer.name("particle");
			this.writeItemStack(rec.getKey().getKey().toStack(), writer);
			writer.name("input");
			this.writeAStack(rec.getKey().getValue(), writer);
			writer.name("output");
			this.writeItemStack(rec.getValue().getKey(), writer);
			writer.name("antimatter").value(rec.getValue().getValue());

		} catch(Exception ex) {
			MainRegistry.logger.error(ex);
			ex.printStackTrace();
		}
	}



	@Override
	public void deleteRecipes() {
		this.recipes.clear();
	}

	@Override
	public String getComment() {
		return "The particle item, while being an input, has to be defined as an item stack without ore dictionary support.";
	}
}
