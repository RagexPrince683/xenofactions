package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import static com.hbm.inventory.OreDictManager.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.imc.IMCBlastFurnace;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Magic!
 *
 * @author UFFR
 */
public class BlastFurnaceRecipes extends SerializableRecipe {

	private static final ArrayList<Triplet<Object, Object, ItemStack>> blastFurnaceRecipes = new ArrayList();
	private static final ArrayList<ComparableStack> hiddenRecipes = new ArrayList();

	@Override
	public void registerDefaults() {
		/* STEEL */
		addRecipe(IRON,			COAL,										new ItemStack(ModItems.ingot_steel, 1));
		addRecipe(IRON,			ANY_COKE,									new ItemStack(ModItems.ingot_steel, 1));
		addRecipe(IRON.ore(),	COAL,										new ItemStack(ModItems.ingot_steel, 2));
		addRecipe(IRON.ore(),	ANY_COKE,									new ItemStack(ModItems.ingot_steel, 3));
		//addRecipe(IRON.ore(),	new ComparableStack(ModItems.powder_flux),	new ItemStack(ModItems.ingot_steel, 3));
		//No?


		//complex tool steel
		addRecipe(ModItems.ingot_zinc, ModItems.ingot_dura_steel, new ItemStack(ModItems.powder_desh, 1));

		//stainless steel metallurgy
		addRecipe(NI,			STEEL,										new ItemStack(ModItems.ingot_stainless, 2));
		addRecipe(CE, 			STEEL,										new ItemStack(ModItems.ingot_stainless, 1));
		addRecipe(Cr, 			STEEL,										new ItemStack(ModItems.ingot_stainless, 3));
		addRecipe(ModItems.powder_ytterbium_tiny, 			STEEL,										new ItemStack(ModItems.ingot_stainless, 12));

		addRecipe(ModItems.er_yag_feedstock, COAL, new ItemStack(ModItems.er_yag_sintered));
		addRecipe(ModItems.yby_feedstock, COAL, new ItemStack(ModItems.yby_sintered));

		//super steel alloy from ingot_molybdenum
		addRecipe(Mo, DESH, new ItemStack(ModItems.ingot_saturnite, 1));

		//MOVED:
		//HSS
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.ingot_steel_dusted, 1), new Object[] { STEEL.ingot(), COAL.dust() });
		//why is this not done in at least a blast furnace?
		addRecipe(STEEL, COAL.dust(), new ItemStack(ModItems.ingot_steel_dusted));
		//manganese too
		addRecipe(STEEL, Mn.dust(), new ItemStack(ModItems.ingot_steel_dusted, 2));
		//manganese is also used in stainless steel batteries, so let's make it also make stainless from dusted steel
		addRecipe(ModItems.ingot_steel_dusted, Mn.dust(), new ItemStack(ModItems.ingot_stainless, 2));

		//stainless steel from selenium
		addRecipe(STEEL, Se.dust(), new ItemStack(ModItems.ingot_stainless, 4));

		//zinc ingot + rhenium powder = steel alloy (ingot_saturnite)
		addRecipe(ZI.ingot(), ModItems.powder_rhenium, new ItemStack(ModItems.ingot_saturnite));

		//osmium powder -> osmium ingot
		addRecipe(ModItems.powder_osmium, COAL, new ItemStack(ModItems.ingot_osmiridium, 1)); //bobcat when calling what is clearly osmium osmiridium

		//arsenic from arsenic_trioxide + ingot_carbon (graphite is a form of carbon...? I guess it works)
		addRecipe(ModItems.arsenic_trioxide, ModItems.ingot_graphite, new ItemStack(ModItems.nugget_arsenic, 1));

		//antimony from antimony_trioxide + ingot_carbon
		addRecipe(ModItems.powder_antimony_trioxide, ModItems.ingot_graphite, new ItemStack(ModItems.nugget_antimony, 1));
		//inverse
		addRecipe(ModItems.nugget_antimony, ModItems.ingot_graphite, new ItemStack(ModItems.powder_antimony_trioxide, 1));

		//tellurium from tellurium_dioxide + ingot_carbon
		addRecipe(ModItems.powder_tellurium_dioxide, ModItems.ingot_graphite, new ItemStack(ModItems.nugget_tellurium, 1));

		addRecipe(CU,									REDSTONE,										new ItemStack(ModItems.ingot_red_copper, 2));
		addRecipe(CU,									Te.nugget(),										new ItemStack(ModItems.ingot_red_copper, 3));
		//steel copper alloy
		addRecipe(STEEL,								MINGRADE,										new ItemStack(ModItems.ingot_advanced_alloy, 2));
		addRecipe(W,									COAL,											new ItemStack(ModItems.neutron_reflector, 2));
		addRecipe(W,									ANY_COKE,										new ItemStack(ModItems.neutron_reflector, 2));
		addRecipe(new ComparableStack(ModItems.canister_full, 1, Fluids.GASOLINE.getID()), "slimeball",	new ItemStack(ModItems.canister_napalm));
		addRecipe(W,									CO.nugget(),									new ItemStack(ModItems.ingot_magnetized_tungsten));
		addRecipe(W,									NI.nugget(),									new ItemStack(ModItems.ingot_magnetized_tungsten));
		addRecipe(W, 									Pr.nugget(), 									new ItemStack(ModItems.ingot_magnetized_tungsten));

		addRecipe(STEEL,								TC99.nugget(),									new ItemStack(ModItems.ingot_tcalloy));
		addRecipe(GOLD.plate(),							ModItems.plate_mixed,							new ItemStack(ModItems.plate_paa, 2));
		//addRecipe(BIGMT,								ModItems.powder_meteorite,						new ItemStack(ModItems.ingot_starmetal, 2));
		//addRecipe(CO,									ModBlocks.block_meteor,							new ItemStack(ModItems.ingot_meteorite));
		//addRecipe(ModItems.meteorite_sword_hardened,	CO,												new ItemStack(ModItems.meteorite_sword_alloyed));
		//addRecipe(ModBlocks.block_meteor,				CO,												new ItemStack(ModItems.ingot_meteorite));

		//addRecipe(new ComparableStack(ModItems.strontium_sulfide, 1),							COAL,										new ItemStack(ModItems.powder_strontium, 2));

		//early game hematite -> iron progression
		//FUCK BOBCAT AND HIS FUCKING ENUMS
		addRecipe(new ComparableStack(ModBlocks.stone_resource, 1, BlockEnums.EnumStoneType.HEMATITE.ordinal()), COAL, new ItemStack(Items.iron_ingot, 1));

		addRecipe(
			new ComparableStack(ModItems.strontium_sulfide, 1),
			COAL,
			new ItemStack(ModItems.powder_strontium_oxide, 1)
		);

		addRecipe(
			new ComparableStack(ModItems.powder_celestite, 1),
			COAL,
			new ItemStack(ModItems.strontium_sulfide, 1)
		);

		//powder_samarium
		//    ↓ (Blast Furnace / Arc Furnace)
		//ingot_samarium
		addRecipe(
			new ComparableStack(ModItems.powder_samarium, 1),
			COAL,
			new ItemStack(ModItems.ingot_samarium)
		);

		//samarium cobalt magnet
		addRecipe(
			ModItems.ingot_samarium,
			ModItems.ingot_cobalt,
			new ItemStack(ModItems.ingot_smco)
		);

		//europium
		addRecipe(
			ModItems.europiumsol,
			LA.ingot(),
			new ItemStack(ModItems.ingot_europium)
		);

		//terbium powder_terbium_fluoride -> ingot_terbium_impure
		addRecipe(
			new ComparableStack(ModItems.powder_terbium_fluoride, 1),
			CA.dust(),
			new ItemStack(ModItems.ingot_terbium_impure)
		);
		addRecipe(
			new ComparableStack(ModItems.powder_terbium_fluoride, 1),
			LI.dust(),
			new ItemStack(ModItems.ingot_terbium_impure)
		);

		//holy shit I am losing it

		//powder_dysprosium2
		addRecipe(
			new ComparableStack(ModItems.powder_dysprosium2, 1),
			COAL,
			new ItemStack(ModItems.ingot_dysprosium)
		);

		//ingot_holmium
		addRecipe(
			new ComparableStack(ModItems.powder_holmium_oxide, 1),
			CA.dust(),
			new ItemStack(ModItems.ingot_holmium)
		);

		//powder_erbium_concentrate reduction using magnesium or calcium
		addRecipe(
			new ComparableStack(ModItems.powder_erbium_concentrate,1),
			MG.ingot(),
			new ItemStack(ModItems.erbium_powder)
		);

		//powder_ytterbium_fluoride
		//    + powder_calcium
		//        ↓ blast furnace
		//powder_ytterbium
		addRecipe(
			new ComparableStack(ModItems.powder_ytterbium_fluoride, 1),
			CA.dust(),
			new ItemStack(ModItems.powder_ytterbium)
		);

		//powder_thulium2 + powder_calcium
		addRecipe(
			new ComparableStack(ModItems.powder_thulium2, 1),
			CA.dust(),
			new ItemStack(ModItems.powder_thulium)
		);



		if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleChemsitry) {
			addRecipe(ModItems.canister_empty, COAL, new ItemStack(ModItems.canister_full, 1, Fluids.OIL.getID()));
		}

		if(!IMCBlastFurnace.buffer.isEmpty()) {
			blastFurnaceRecipes.addAll(IMCBlastFurnace.buffer);
			MainRegistry.logger.info("Fetched " + IMCBlastFurnace.buffer.size() + " IMC blast furnace recipes!");
			IMCBlastFurnace.buffer.clear();
		}

		//barium
		addRecipe(new ComparableStack(ModBlocks.ore_barite, 1), COAL, new ItemStack(ModItems.barium_sulfide, 1));

		//Pyrometallurgy of chalcopyrite
		addRecipe(new ComparableStack(ModItems.chalcopyrite, 1), COAL, new ItemStack(ModBlocks.ore_copper, 2));

		addRecipe( //ultra high strength steel alt recipe in case you don't like bobcat CBT
			new ComparableStack(ModItems.ingot_steel_dusted, 4),
			B.block(),
			new ItemStack(ModItems.ingot_chainsteel, 1)
		);

		//hiddenRecipes.add(new ComparableStack(ModItems.meteorite_sword_alloyed));
	}

	private static void addRecipe(Object in1, Object in2, ItemStack out) {

		if(in1 instanceof Item) in1 = new ComparableStack((Item) in1);
		if(in1 instanceof Block) in1 = new ComparableStack((Block) in1);
		if(in2 instanceof Item) in2 = new ComparableStack((Item) in2);
		if(in2 instanceof Block) in2 = new ComparableStack((Block) in2);

		blastFurnaceRecipes.add(new Triplet<Object, Object, ItemStack>(in1, in2, out));
	}

	@CheckForNull
	public static ItemStack getOutput(ItemStack in1, ItemStack in2) {
		for(Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
			AStack[] recipeItem1 = getRecipeStacks(recipe.getX());
			AStack[] recipeItem2 = getRecipeStacks(recipe.getY());

			if((doStacksMatch(recipeItem1, in1) && doStacksMatch(recipeItem2, in2)) || (doStacksMatch(recipeItem2, in1) && doStacksMatch(recipeItem1, in2))) {
				return recipe.getZ().copy();
			} else {
				continue;
			}
		}
		return null;
	}

	private static boolean doStacksMatch(AStack[] recipe, ItemStack in) {
		boolean flag = false;
		byte i = 0;
		while(!flag && i < recipe.length) {
			flag = recipe[i].matchesRecipe(in, true);
			i++;
		}
		return flag;
	}

	private static AStack[] getRecipeStacks(Object in) {

		AStack[] recipeItem1 = new AStack[0];

		if(in instanceof DictFrame) {
			DictFrame recipeItem = (DictFrame) in;
			recipeItem1 = new AStack[] { new OreDictStack(recipeItem.ingot()), new OreDictStack(recipeItem.plate()), new OreDictStack(recipeItem.gem()), new OreDictStack(recipeItem.dust()) };

		} else if(in instanceof AStack) {
			recipeItem1 = new AStack[] { (AStack) in };

		} else if(in instanceof String) {
			recipeItem1 = new AStack[] { new OreDictStack((String) in) };

		}/* else if(in instanceof List<?>) {
			List<?> oreList = (List<?>) in;
			recipeItem1 = new AStack[oreList.size()];
			for(int i = 0; i < oreList.size(); i++)
				recipeItem1[i] = new OreDictStack((String) oreList.get(i));

		}*/

		return recipeItem1;
	}

	public static Map<List<ItemStack>[], ItemStack> getRecipesForNEI() {
		HashMap<List<ItemStack>[], ItemStack> recipes = new HashMap<>();

		for(Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
			if(!hiddenRecipes.contains(new ComparableStack(recipe.getZ()))) {
				ItemStack nothing = new ItemStack(ModItems.nothing).setStackDisplayName("If you're reading this, an error has occured! Check the console.");
				List<ItemStack> in1 = new ArrayList();
				List<ItemStack> in2 = new ArrayList();
				in1.add(nothing);
				in2.add(nothing);

				for(AStack stack : getRecipeStacks(recipe.getX())) {
					List<ItemStack> variants = stack.extractForNEI();
					if(!variants.isEmpty()) {
						in1.remove(nothing);
						in1.addAll(variants);
					}
				}
				if(in1.contains(nothing)) {
					MainRegistry.logger.error("Blast furnace cannot compile recipes for NEI: apparent nonexistent item #1 in recipe for item: " + recipe.getZ().getDisplayName());
				}
				for(AStack stack : getRecipeStacks(recipe.getY())) {
					List<ItemStack> variants = stack.extractForNEI();
					if(!variants.isEmpty()) {
						in2.remove(nothing);
						in2.addAll(variants);
					}
				}
				if(in2.contains(nothing)) {
					MainRegistry.logger.error("Blast furnace cannot compile recipes for NEI: apparent nonexistent item #2 in recipe for item: " + recipe.getZ().getDisplayName());
				}

				List<ItemStack>[] inputs = new List[2];
				inputs[0] = in1;
				inputs[1] = in2;
				recipes.put(inputs, recipe.getZ());
			}
		}
		return ImmutableMap.copyOf(recipes);
	}

	public static List<Triplet<AStack[], AStack[], ItemStack>> getRecipes() {
		List<Triplet<AStack[], AStack[], ItemStack>> subRecipes = new ArrayList<>();
		for(Triplet<Object, Object, ItemStack> recipe : blastFurnaceRecipes) {
			subRecipes.add(new Triplet<AStack[], AStack[], ItemStack>(getRecipeStacks(recipe.getX()), getRecipeStacks(recipe.getY()), recipe.getZ()));
		}
		return ImmutableList.copyOf(subRecipes);
	}

	@Override
	public String getFileName() {
		return "hbmBlastFurnace.json";
	}

	@Override
	public String getComment() {
		return "Inputs can use the unique 'dictframe' type which is an ore dictionary material suffix. The recipes will accept most ore dictionary entries equivalent to one ingot (gems, dust, plates, etc).";
	}

	@Override
	public Object getRecipeObject() {
		return blastFurnaceRecipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject rec = (JsonObject) recipe;

		ItemStack output = this.readItemStack(rec.get("output").getAsJsonArray());

		Object input1 = null;
		Object input2 = null;

		JsonArray array1 = rec.get("input1").getAsJsonArray();
		if(array1.get(0).getAsString().equals("item")) input1 = this.readAStack(array1);
		if(array1.get(0).getAsString().equals("dict")) input1 = ((OreDictStack) this.readAStack(array1)).name;
		if(array1.get(0).getAsString().equals("dictframe")) input1 = readDictFrame(array1);

		JsonArray array2 = rec.get("input2").getAsJsonArray();
		if(array2.get(0).getAsString().equals("item")) input2 = this.readAStack(array2);
		if(array2.get(0).getAsString().equals("dict")) input2 = ((OreDictStack) this.readAStack(array2)).name;
		if(array2.get(0).getAsString().equals("dictframe")) input2 = readDictFrame(array2);

		if(input1 != null && input2 != null) {
			addRecipe(input1, input2, output);

			if(rec.has("hidden") && rec.get("hidden").getAsBoolean()) {
				this.hiddenRecipes.add(new ComparableStack(output));
			}
		}
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Triplet<Object, Object, ItemStack> rec = (Triplet<Object, Object, ItemStack>) recipe;
		writer.name("output");
		this.writeItemStack(rec.getZ(), writer);

		writer.name("input1");
		if(rec.getX() instanceof ComparableStack) this.writeAStack((ComparableStack) rec.getX(), writer);
		if(rec.getX() instanceof String) this.writeAStack(new OreDictStack((String) rec.getX()), writer);
		if(rec.getX() instanceof DictFrame) this.writeDictFrame((DictFrame) rec.getX(), writer);

		writer.name("input2");
		if(rec.getY() instanceof ComparableStack) this.writeAStack((ComparableStack) rec.getY(), writer);
		if(rec.getY() instanceof String) this.writeAStack(new OreDictStack((String) rec.getY()), writer);
		if(rec.getY() instanceof DictFrame) this.writeDictFrame((DictFrame) rec.getY(), writer);

		if(this.hiddenRecipes.contains(new ComparableStack(rec.getZ()))) {
			writer.name("hidden").value(true);
		}
	}

	public static void writeDictFrame(DictFrame frame, JsonWriter writer) throws IOException {
		writer.beginArray();
		writer.setIndent("");
		writer.value("dictframe");
		writer.value(frame.mats[0]);
		writer.endArray();
		writer.setIndent("  ");
	}

	public static DictFrame readDictFrame(JsonArray array) {
		return new DictFrame(array.get(1).getAsString());
	}

	@Override
	public void deleteRecipes() {
		blastFurnaceRecipes.clear();
		hiddenRecipes.clear();
	}
}
