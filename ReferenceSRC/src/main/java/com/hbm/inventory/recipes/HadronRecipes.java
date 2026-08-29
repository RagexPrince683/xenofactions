package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityHadron.EnumHadronState;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class HadronRecipes extends SerializableRecipe {

	/*
	 * Since we're dealing with like 10 or so recipes, using a HashMap (or to combine two keys, a HashMap *in* a HashMap)
	 * would be less performant than those few steps through a good old Array list, and it's much easier to implement too.
	 */
	private static final List<HadronRecipe> recipes = new ArrayList();

	/*
	 * We CAN actually implement recipes with the same input items but different momentum requirements.
	 * Just be sure to register the higher requirement BEFORE the lower one since those need to be checked first.
	 *
	 * It's important to remember that, ok?
	 *
	 * Update, T+6 minutes: I went for coffee and already forgot what I was doing, good thing I keep these notes, hehe.
	 * Having multiple recipes with different momentum requirements (at most I would expect 2) isn't exactly necessary
	 * since the thing differentiates between ring and line accelerator mode, and line accelerators are by design always shorter anyway.
	 */
	@Override
	public void registerDefaults() {

		//okay I don't know why we nuked the fucking recipes out of here, it's still a particle accelerator so we should be able to use it as such.

		// 209Bi(alpha,2n)211At. The bismuth ingot is the natural-target
		// abstraction; the astatine capsule represents a trace accelerator yield.
		recipes.add(new HadronRecipe(
			new ItemStack(ModItems.ingot_bismuth),
			new ItemStack(ModItems.particle_helium),
			225000,
			new ItemStack(ModItems.particle_astatine),
			new ItemStack(ModItems.particle_neutron, 2),
			false
		));

		/*
		 * Fictional collider extension: a tungsten target is used as a high-Z
		 * conversion target. Antimatter and muon capsules are gameplay-scale
		 * collision products, not a claim of practical bulk production.
		 */
		//recipes.add(new HadronRecipe(
		//	new ItemStack(ModItems.ingot_tungsten),
		//	new ItemStack(ModItems.particle_hydrogen),
		//	150000,
		//	new ItemStack(ModItems.particle_amat),
		//	new ItemStack(ModItems.particle_muon),
		//	false
		//));
		//if it's fictional why the fuck did you add it????

		recipes.add(new HadronRecipe(
			new ItemStack(ModItems.powder_lithium),
			new ItemStack(ModItems.particle_hydrogen),
			140000,
			new ItemStack(ModItems.particle_neutron, 2),
			new ItemStack(ModItems.particle_empty),
			true
		));

		//recipes.add(new HadronRecipe(
		//	new ItemStack(Items.coal),
		//	new ItemStack(ModItems.particle_hydrogen),
		//	50000,
		//	new ItemStack(ModItems.particle_muon),
		//	new ItemStack(ModItems.particle_neutron),
		//	true
		//));
		//apparently this isn't physically valid

		// High-energy proton spallation target.
		// Represents pion production followed by pion decay into a muon.
		recipes.add(new HadronRecipe(
			new ItemStack(ModItems.ingot_tungsten),
			new ItemStack(ModItems.particle_hydrogen),
			3000000,
			new ItemStack(ModItems.particle_muon),
			new ItemStack(ModItems.particle_neutron),
			false
		));

		recipes.add(new HadronRecipe(
			new ItemStack(ModItems.ingot_cobalt),
			new ItemStack(ModItems.particle_neutron),
			12000,
			new ItemStack(ModItems.ingot_co60),
			new ItemStack(ModItems.particle_empty),
			true
		));

		recipes.add(new HadronRecipe(
			new ItemStack(Items.gold_ingot),
			new ItemStack(ModItems.particle_neutron),
			15000,
			new ItemStack(ModItems.nugget_au198),
			new ItemStack(ModItems.particle_empty),
			true
		));

		//I do not care enough to add intermediate decay bullshit
		recipes.add(new HadronRecipe(
			new ItemStack(ModItems.ingot_u238),
			new ItemStack(ModItems.particle_neutron),
			25000,
			new ItemStack(ModItems.ingot_pu239),
			new ItemStack(ModItems.particle_beta, 2),
			false
		));

		//I do not care enough to add intermediate decay bullshitx2
		recipes.add(new HadronRecipe(
			new ItemStack(ModItems.ingot_th232),
			new ItemStack(ModItems.particle_neutron),
			25000,
			new ItemStack(ModItems.ingot_u233),
			new ItemStack(ModItems.particle_beta, 2),
			false
		));

	}


	public static EnumHadronState returnCode = EnumHadronState.NORESULT;

	/**
	 * Resolves recipes, simple enough.
	 * @param in1
	 * @param in2
	 * @param momentum
	 * @param analysisOnly true == line accelerator mode
	 * @return either null (no recipe) or an ItemStack array with 2 non-null instances
	 */
	public static ItemStack[] getOutput(ItemStack in1, ItemStack in2, int momentum, boolean analysisOnly) {

		returnCode = EnumHadronState.NORESULT_WRONG_INGREDIENT;

		for(HadronRecipe r : recipes) {

			if((r.in1.isApplicable(in1) && r.in2.isApplicable(in2)) ||
					(r.in1.isApplicable(in2) && r.in2.isApplicable(in1))) {

				if(analysisOnly && !r.analysisOnly)	returnCode = EnumHadronState.NORESULT_WRONG_MODE;
				if(momentum < r.momentum)			returnCode = EnumHadronState.NORESULT_TOO_SLOW;

				if(momentum >= r.momentum && analysisOnly == r.analysisOnly)
					return new ItemStack[] {r.out1, r.out2};
			}
		}
		return null;
	}

	public static List<HadronRecipe> getRecipes() {
		return recipes;
	}

	public static class HadronRecipe {

		public ComparableStack in1;
		public ComparableStack in2;
		public int momentum;
		public ItemStack out1;
		public ItemStack out2;
		public boolean analysisOnly;

		public HadronRecipe(ItemStack in1, ItemStack in2, int momentum, ItemStack out1, ItemStack out2, boolean analysisOnly) {
			this.in1 = new ComparableStack(in1).makeSingular();
			this.in2 = new ComparableStack(in2).makeSingular();
			this.momentum = momentum;
			this.out1 = out1;
			this.out2 = out2;
			this.out1.stackSize = 1;
			this.out2.stackSize = 1;
			this.analysisOnly = analysisOnly;
		}
	}

	@Override
	public String getFileName() {
		return "hbmHadronCollider.json";
	}

	@Override
	public Object getRecipeObject() {
		return this.recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;
		int momentum = obj.get("momentum").getAsInt();
		boolean lineMode = obj.get("lineMode").getAsBoolean();
		ItemStack[] in = this.readItemStackArray(obj.get("inputs").getAsJsonArray());
		ItemStack[] out = this.readItemStackArray(obj.get("outputs").getAsJsonArray());

		this.recipes.add(new HadronRecipe(
				in[0],
				in[1],
				momentum,
				out[0],
				out[1],
				lineMode
				));
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		HadronRecipe rec = (HadronRecipe) recipe;

		writer.name("momentum").value(rec.momentum);
		writer.name("lineMode").value(rec.analysisOnly);

		writer.name("inputs").beginArray();
		this.writeItemStack(rec.in1.toStack(), writer);
		this.writeItemStack(rec.in2.toStack(), writer);
		writer.endArray();

		writer.name("outputs").beginArray();
		this.writeItemStack(rec.out1, writer);
		this.writeItemStack(rec.out2, writer);
		writer.endArray();
	}

	@Override
	public String getComment() {
		return "Rules: Both in- and output stacks cannot be null. Stacksizes are set to 1 for all stacks.";
	}

	@Override
	public void deleteRecipes() {
		this.recipes.clear();
	}
}
