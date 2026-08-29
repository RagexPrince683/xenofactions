package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.material.MaterialShapes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.imc.IMCCentrifuge;
import static com.hbm.inventory.OreDictManager.*;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.items.special.ItemBedrockOre.EnumBedrockOre;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreGrade;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreType;
import com.hbm.items.special.ItemByproduct.EnumByproduct;
//import com.hbm.items.special.ItemMineralOre.EnumMineralOre;
import com.hbm.main.MainRegistry;
import com.hbm.util.ItemStackUtil;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class CentrifugeRecipes extends SerializableRecipe {

	private static HashMap<AStack, ItemStack[]> recipes = new HashMap();

	@Override
	public void registerDefaults() {

		boolean lbs = GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleCentrifuge;

		recipes.put(new ComparableStack(ModItems.waste_natural_uranium), new ItemStack[] {
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nugget_pu_mix, 2),
				new ItemStack(ModItems.nugget_pu239, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_uranium), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 2),
				new ItemStack(ModItems.nugget_plutonium, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_thorium), new ItemStack[] {
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nugget_th232, 1),
				new ItemStack(ModItems.nugget_u233, 2),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_mox), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_plutonium), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_u233), new ItemStack[] {
				new ItemStack(ModItems.nugget_u235, 1),
				new ItemStack(ModItems.nugget_neptunium, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_u235), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu238, 1),
				new ItemStack(ModItems.nugget_neptunium, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_schrabidium), new ItemStack[] {
				new ItemStack(ModItems.nugget_beryllium, 2),
				new ItemStack(ModItems.nugget_pu239, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_zfb_mox), new ItemStack[] {
				new ItemStack(ModItems.nugget_zirconium, 3),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 1) });

		recipes.put(new ComparableStack(ModItems.waste_plate_mox), new ItemStack[] {
				new ItemStack(ModItems.powder_sr90_tiny, 1),
				new ItemStack(ModItems.nugget_pu_mix, 3),
				new ItemStack(ModItems.powder_cs137_tiny, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 4) });

		recipes.put(new ComparableStack(ModItems.waste_plate_pu238be), new ItemStack[] {
				new ItemStack(ModItems.nugget_beryllium, 1),
				new ItemStack(ModItems.nugget_pu238, 1),
				new ItemStack(ModItems.powder_coal_tiny, 2),
				new ItemStack(ModItems.nugget_lead, 2) });

		recipes.put(new ComparableStack(ModItems.waste_plate_pu239), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu240, 2),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.powder_cs137_tiny, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 5) });

		recipes.put(new ComparableStack(ModItems.waste_plate_ra226be), new ItemStack[] {
				new ItemStack(ModItems.nugget_beryllium, 2),
				new ItemStack(ModItems.nugget_polonium, 2),
				new ItemStack(ModItems.powder_coal_tiny, 1),
				new ItemStack(ModItems.nugget_lead, 1) });

		recipes.put(new ComparableStack(ModItems.waste_plate_sa326), new ItemStack[] {
				new ItemStack(ModItems.nugget_solinium, 1),
				new ItemStack(ModItems.powder_neodymium_tiny, 1),
				new ItemStack(ModItems.nugget_tantalium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 6) });

		recipes.put(new ComparableStack(ModItems.waste_plate_u233), new ItemStack[] {
				new ItemStack(ModItems.nugget_u235, 1),
				new ItemStack(ModItems.powder_i131_tiny, 1),
				new ItemStack(ModItems.powder_sr90_tiny, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 6) });

		recipes.put(new ComparableStack(ModItems.waste_plate_u235), new ItemStack[] {
				new ItemStack(ModItems.nugget_neptunium, 1),
				new ItemStack(ModItems.nugget_pu238, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 6) });

		//even more efficient gunpowder crafting
		recipes.put(new ComparableStack(ModItems.gpmix), new ItemStack[] {
				new ItemStack(Items.gunpowder, 32) });



		// Cooled PWR fuel is deliberately not an ordinary-centrifuge input.  It must
		// be dissolved and separated in the chemical plant (PWR_*_REPROCESS), which
		// keeps bulk recovery and high-level raffinate together rather than creating
		// a set of implausibly pure isotope nuggets from an intact fuel rod.
		//recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.pwr_fuel_depleted, EnumPWRFuel.HES326)), new ItemStack[] {
		//		new ItemStack(ModItems.nugget_solinium, 3),
		//		new ItemStack(ModItems.nugget_lead, 2),
		//		new ItemStack(ModItems.nugget_euphemium, 1),
		//		new ItemStack(ModItems.nuclear_waste_tiny, 6) });
		//recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.pwr_fuel_depleted, EnumPWRFuel.HES327)), new ItemStack[] {
		//		new ItemStack(ModItems.nugget_australium, 4),
		//		new ItemStack(ModItems.nugget_lead, 1),
		//		new ItemStack(ModItems.nugget_euphemium, 1),
		//		new ItemStack(ModItems.nuclear_waste_tiny, 6) });

		recipes.put(new ComparableStack(ModItems.icf_pellet_depleted), new ItemStack[] {
				new ItemStack(ModItems.icf_pellet_empty, 1),
				new ItemStack(ModItems.pellet_charged, 1),
				new ItemStack(ModItems.pellet_charged, 1),
				new ItemStack(ModItems.powder_iron, 1) });

		recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.chunk_ore, EnumChunkType.RARE)), new ItemStack[] {
				new ItemStack(ModItems.powder_cobalt_tiny, 2),
				new ItemStack(ModItems.powder_boron_tiny, 2),
				new ItemStack(ModItems.powder_niobium_tiny, 2),
				new ItemStack(ModItems.nugget_zirconium, 3) });


		recipes.put(new OreDictStack(COAL.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_coal, 2),
				new ItemStack(ModItems.powder_coal, 2),
				new ItemStack(ModItems.powder_coal, 2),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(LIGNITE.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_lignite, 2),
				new ItemStack(ModItems.powder_lignite, 2),
				new ItemStack(ModItems.powder_lignite, 2),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(IRON.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack("oreNickel"), new ItemStack[] {
				new ItemStack(ModItems.powder_nickel, 1),
				new ItemStack(ModItems.powder_nickel, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack("oreZinc"), new ItemStack[] {
			new ItemStack(ModItems.powder_zinc, 1),
			new ItemStack(ModItems.powder_cadmium, 1),
			new ItemStack(ModItems.powder_gallium, 1),
			new ItemStack(ModItems.powder_germanium, 1) });


		recipes.put(new OreDictStack(GOLD.ore()), new ItemStack[] {
				lbs ? new ItemStack(ModItems.powder_gold, 2) : new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(ModItems.powder_gold, 1),
				lbs ? new ItemStack(ModItems.nugget_bismuth, 1) : new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(DIAMOND.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_diamond, 1),
				new ItemStack(ModItems.powder_diamond, 1),
				new ItemStack(ModItems.powder_diamond, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(EMERALD.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(TI.ore()), new ItemStack[] {
				lbs ? new ItemStack(ModItems.powder_titanium, 2) : new ItemStack(ModItems.powder_titanium, 1),
				lbs ? new ItemStack(ModItems.powder_titanium, 2) : new ItemStack(ModItems.powder_titanium, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(NETHERQUARTZ.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_quartz, 1),
				new ItemStack(ModItems.powder_quartz, 1),
				new ItemStack(ModItems.powder_lithium_tiny, 1),
				new ItemStack(Blocks.netherrack, 1) });

		recipes.put(new OreDictStack(W.ore()), new ItemStack[] {
				lbs ? new ItemStack(ModItems.powder_tungsten, 2) : new ItemStack(ModItems.powder_tungsten, 1),
				new ItemStack(ModItems.powder_tungsten, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(CU.ore()), new ItemStack[] {
				lbs ? new ItemStack(ModItems.powder_copper, 2) : new ItemStack(ModItems.powder_copper, 1),
				new ItemStack(ModItems.powder_copper, 1),
				new ItemStack(ModItems.powder_cadmium, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(AL.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_aluminium, 1),
				new ItemStack(ModItems.powder_aluminium, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(PB.ore()), new ItemStack[] {
				lbs ? new ItemStack(ModItems.powder_lead, 2) : new ItemStack(ModItems.powder_lead, 1),
				lbs ? new ItemStack(ModItems.nugget_bismuth, 1) : new ItemStack(ModItems.powder_lead, 1),
				new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(PU.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_plutonium, 1),
				new ItemStack(ModItems.powder_plutonium, 1),
				new ItemStack(ModItems.nugget_polonium, 3),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(U.ore()), new ItemStack[] {
				lbs ? new ItemStack(ModItems.powder_uranium, 2) : new ItemStack(ModItems.powder_uranium, 1),
				lbs ? new ItemStack(ModItems.nugget_technetium, 2) : new ItemStack(ModItems.powder_uranium, 1),
				lbs ? new ItemStack(ModItems.nugget_ra226, 2) : new ItemStack(ModItems.nugget_ra226, 1),
				new ItemStack(Blocks.gravel, 1) });

		for(String ore : OreDictManager.TH232.all(MaterialShapes.ORE)) recipes.put(new OreDictStack(ore), new ItemStack[] {
				new ItemStack(ModItems.powder_thorium, 1),
				new ItemStack(ModItems.powder_thorium, 1),
				new ItemStack(ModItems.powder_uranium, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(BE.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_beryllium, 1),
				new ItemStack(ModItems.powder_beryllium, 1),
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(F.ore()), new ItemStack[] {
				new ItemStack(ModItems.fluorite, 3),
				new ItemStack(ModItems.fluorite, 3),
				new ItemStack(ModItems.gem_sodalite, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(REDSTONE.ore()), new ItemStack[] {
				new ItemStack(Items.redstone, 3),
				new ItemStack(Items.redstone, 3),
				lbs ? new ItemStack(ModItems.ingot_mercury, 3) : new ItemStack(ModItems.ingot_mercury, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new OreDictStack(LAPIS.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_lapis, 6),
				new ItemStack(ModItems.powder_cobalt_tiny, 1),
				new ItemStack(ModItems.gem_sodalite, 1),
				new ItemStack(Blocks.gravel, 1) });

		recipes.put(new ComparableStack(ModBlocks.ore_nether_fire), new ItemStack[] {
				new ItemStack(Items.blaze_powder, 2),
				new ItemStack(ModItems.powder_fire, 2),
				new ItemStack(ModItems.ingot_phosphorus),
				new ItemStack(Blocks.netherrack) });

		recipes.put(new OreDictStack(P_RED.ore()), new ItemStack[] {
				new ItemStack(Items.blaze_powder, 2),
				new ItemStack(ModItems.powder_fire, 2),
				new ItemStack(ModItems.ingot_phosphorus),
				new ItemStack(Blocks.gravel) });

		recipes.put(new OreDictStack(CO.ore()), new ItemStack[] {
				new ItemStack(ModItems.powder_cobalt, 2),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.powder_copper, 1),
				new ItemStack(Blocks.gravel, 1) });

		//recipes.put(new ComparableStack(ModItems.powder_tektite), new ItemStack[] {
		//		new ItemStack(ModItems.powder_meteorite_tiny, 1),
		//		new ItemStack(ModItems.powder_paleogenite_tiny, 1),
		//		new ItemStack(ModItems.powder_meteorite_tiny, 1),
		//		new ItemStack(ModItems.dust, 6) });

		recipes.put(new ComparableStack(ModBlocks.block_slag), new ItemStack[] {
				new ItemStack(Blocks.gravel, 1),
				new ItemStack(ModItems.powder_fire, 1),
				new ItemStack(ModItems.powder_calcium),
				new ItemStack(ModItems.dust) });
		//nitric acid needs air chem to use, something that c
		recipes.put(new ComparableStack(ModItems.mineral_fragment, 1, 0), new ItemStack[] { //peroxide, easy to use and get
				new ItemStack(ModItems.powder_niobium, 4),
				new ItemStack(ModItems.powder_cobalt, 2),
				new ItemStack(ModItems.powder_zirconium, 1),
				new ItemStack(ModItems.powder_beryllium, 1) });

		recipes.put(new ComparableStack(ModItems.mineral_fragment, 1, 1), new ItemStack[] { //nitric acid, harder and energy expensive
				new ItemStack(ModItems.powder_gallium, 4),
				new ItemStack(ModItems.powder_beryllium, 2),
				new ItemStack(ModItems.powder_niobium, 1),
				new ItemStack(ModItems.fragment_lanthanium, 1) });

		recipes.put(new ComparableStack(ModItems.mineral_fragment, 1, 2), new ItemStack[] { //sulfuric acid, less harder
				new ItemStack(ModItems.powder_beryllium, 4),
				new ItemStack(ModItems.powder_niobium, 2),
				new ItemStack(ModItems.powder_gallium, 1),
				new ItemStack(ModItems.fragment_lanthanium, 1) });

		recipes.put(new ComparableStack(ModItems.mineral_fragment, 1, 3), new ItemStack[] {// solvent uses *oil* something that sulfuric doesnt
				new ItemStack(ModItems.powder_cobalt, 4),
				new ItemStack(ModItems.powder_gallium, 2),
				new ItemStack(ModItems.powder_niobium, 1),
				new ItemStack(ModItems.powder_coltan, 1) });

		recipes.put(new ComparableStack(ModItems.mineral_fragment, 1, 4), new ItemStack[] {  // chlorine is important mid-lategame. since it makes Plastics
				new ItemStack(ModItems.powder_zirconium, 4),
				new ItemStack(ModItems.powder_neodymium, 2),
				new ItemStack(ModItems.powder_niobium, 1),
				new ItemStack(ModItems.powder_cobalt, 1) });

		recipes.put(new ComparableStack(ModItems.mineral_fragment, 1, 5), new ItemStack[] { // shchrab acid can go fuck itself
				new ItemStack(ModItems.powder_co60, 1),
				new ItemStack(ModItems.nugget_bismuth, 1),
				new ItemStack(ModItems.powder_asbestos, 6),
				new ItemStack(ModItems.nugget_technetium, 1) });

		recipes.put(new ComparableStack(ModItems.powder_ash, 1, EnumAshType.COAL.ordinal()), new ItemStack[] {
				new ItemStack(ModItems.powder_coal_tiny, 2),
				new ItemStack(ModItems.powder_boron_tiny, 1),
				new ItemStack(ModItems.dust_tiny, 6)});

		recipes.put(new ComparableStack(ModBlocks.ferric_clay, 1), new ItemStack[] {
				new ItemStack(Items.clay_ball, 1),
				new ItemStack(Items.clay_ball, 1),
				new ItemStack(ModItems.powder_iron, 1), //temp
				new ItemStack(ModItems.powder_iron, 1)});

		for(EnumBedrockOre ore : EnumBedrockOre.values()) {
			int i = ore.ordinal();

			recipes.put(new ComparableStack(ModItems.ore_bedrock, 1, i), new ItemStack[] {
					new ItemStack(ModItems.ore_centrifuged, 1, i),
					new ItemStack(ModItems.ore_centrifuged, 1, i),
					new ItemStack(ModItems.ore_centrifuged, 1, i),
					new ItemStack(ModItems.ore_centrifuged, 1, i) });

			recipes.put(new ComparableStack(ModItems.ore_cleaned, 1, i), new ItemStack[] {
					new ItemStack(ModItems.ore_separated, 1, i),
					new ItemStack(ModItems.ore_separated, 1, i),
					new ItemStack(ModItems.ore_separated, 1, i),
					new ItemStack(ModItems.ore_separated, 1, i) });

			recipes.put(new ComparableStack(ModItems.ore_purified, 1, i), new ItemStack[] {
					new ItemStack(ModItems.ore_enriched, 1, i),
					new ItemStack(ModItems.ore_enriched, 1, i),
					new ItemStack(ModItems.ore_enriched, 1, i),
					new ItemStack(ModItems.ore_enriched, 1, i) });

			EnumByproduct tier1 = ore.byproducts[0];
			ItemStack by1 = tier1 == null ? new ItemStack(ModItems.dust) : DictFrame.fromOne(ModItems.ore_byproduct, tier1, 1);
			recipes.put(new ComparableStack(ModItems.ore_nitrated, 1, i), new ItemStack[] {
					new ItemStack(ModItems.ore_nitrocrystalline, 1, i),
					new ItemStack(ModItems.ore_nitrocrystalline, 1, i),
					ItemStackUtil.carefulCopy(by1),
					ItemStackUtil.carefulCopy(by1) });

			EnumByproduct tier2 = ore.byproducts[1];
			ItemStack by2 = tier2 == null ? new ItemStack(ModItems.dust) : DictFrame.fromOne(ModItems.ore_byproduct, tier2, 1);
			recipes.put(new ComparableStack(ModItems.ore_deepcleaned, 1, i), new ItemStack[] {
					new ItemStack(ModItems.ore_enriched, 1, i),
					new ItemStack(ModItems.ore_enriched, 1, i),
					ItemStackUtil.carefulCopy(by2),
					ItemStackUtil.carefulCopy(by2) });

			EnumByproduct tier3 = ore.byproducts[2];
			ItemStack by3 = tier3 == null ? new ItemStack(ModItems.dust) : DictFrame.fromOne(ModItems.ore_byproduct, tier3, 1);
			recipes.put(new ComparableStack(ModItems.ore_seared, 1, i), new ItemStack[] {
					new ItemStack(ModItems.ore_enriched, 1, i),
					new ItemStack(ModItems.ore_enriched, 1, i),
					ItemStackUtil.carefulCopy(by3),
					ItemStackUtil.carefulCopy(by3) });
		}

		for(BedrockOreType type : BedrockOreType.values()) {

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.BASE, type)), new ItemStack[] {ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.gravel)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.BASE_ROASTED, type)), new ItemStack[] {ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.gravel)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.BASE_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY, type), ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.gravel)});

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_SULFURIC, type)), new ItemStack[] {ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_NOSULFURIC, type, 2), ItemBedrockOreNew.make(BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_SOLVENT, type)), new ItemStack[] {ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_NOSOLVENT, type, 2), ItemBedrockOreNew.make(BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2), ItemBedrockOreNew.make(BedrockOreGrade.SOLVENT_BYPRODUCT, type, 2)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_RAD, type)), new ItemStack[] {ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_NORAD, type, 2), ItemBedrockOreNew.make(BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2), ItemBedrockOreNew.make(BedrockOreGrade.SOLVENT_BYPRODUCT, type, 2), ItemBedrockOreNew.make(BedrockOreGrade.RAD_BYPRODUCT, type, 2)});

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_ROASTED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_NOSULFURIC, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_NOSOLVENT, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_NORAD, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_FIRST, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type, 2)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_SECOND, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type, 2)});

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.SULFURIC_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.byproductAcid1, 1), ItemBedrockOreNew.extract(type.byproductAcid2, 1), ItemBedrockOreNew.extract(type.byproductAcid3, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.SOLVENT_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.byproductSolvent1, 1), ItemBedrockOreNew.extract(type.byproductSolvent2, 1), ItemBedrockOreNew.extract(type.byproductSolvent3, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.RAD_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.byproductRad1, 1), ItemBedrockOreNew.extract(type.byproductRad2, 1), ItemBedrockOreNew.extract(type.byproductRad3, 1), ItemBedrockOreNew.make(BedrockOreGrade.CRUMBS, type)});
		}

		List<ItemStack> quartz = OreDictionary.getOres("crystalCertusQuartz");

		if(quartz != null && !quartz.isEmpty()) {
			ItemStack qItem = quartz.get(0).copy();
			qItem.stackSize = 2;

			recipes.put(new OreDictStack("oreCertusQuartz"), new ItemStack[] {
					qItem.copy(),
					qItem.copy(),
					qItem.copy(),
					qItem.copy() });
		}

		recipes.put(new ComparableStack(Items.blaze_rod), new ItemStack[] {new ItemStack(Items.blaze_powder, 1), new ItemStack(Items.blaze_powder, 1), new ItemStack(ModItems.powder_fire, 1), new ItemStack(ModItems.powder_fire, 1) });

		//recipes.put(new ComparableStack(ModItems.ingot_schraranium), new ItemStack[] { new ItemStack(ModItems.nugget_schrabidium, 2), new ItemStack(ModItems.nugget_schrabidium, 1), new ItemStack(ModItems.nugget_uranium, 3), new ItemStack(ModItems.nugget_neptunium, 2) });


		//crystals
		//recipes.put(new ComparableStack(ModItems.crystal_coal), new ItemStack[] { new ItemStack(ModItems.powder_coal, 3), new ItemStack(ModItems.powder_coal, 3), new ItemStack(ModItems.powder_coal, 3), new ItemStack(ModItems.nugget_silicon, 1) });
		//fuel source that makes a fuel source... DUPE
		recipes.put(new ComparableStack(ModItems.crystal_iron), new ItemStack[] { new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.powder_titanium, 1), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_gold), new ItemStack[] { new ItemStack(ModItems.powder_gold, 2), new ItemStack(ModItems.powder_gold, 2), new ItemStack(ModItems.ingot_mercury, 1), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_redstone), new ItemStack[] { new ItemStack(Items.redstone, 3), new ItemStack(Items.redstone, 3), new ItemStack(Items.redstone, 3), new ItemStack(ModItems.nugget_silicon, 3) });
		recipes.put(new ComparableStack(ModItems.crystal_lapis), new ItemStack[] { new ItemStack(ModItems.powder_lapis, 4), new ItemStack(ModItems.powder_lapis, 4), new ItemStack(ModItems.powder_cobalt, 1), new ItemStack(ModItems.gem_sodalite, 2) });
		recipes.put(new ComparableStack(ModItems.crystal_diamond), new ItemStack[] { new ItemStack(ModItems.powder_diamond, 1), new ItemStack(ModItems.powder_diamond, 1), new ItemStack(ModItems.powder_diamond, 1), new ItemStack(ModItems.powder_diamond, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_uranium), new ItemStack[] { new ItemStack(ModItems.powder_uranium, 2), new ItemStack(ModItems.powder_uranium, 2), new ItemStack(ModItems.nugget_ra226, 2), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_thorium), new ItemStack[] { new ItemStack(ModItems.powder_thorium, 2), new ItemStack(ModItems.powder_thorium, 2), new ItemStack(ModItems.powder_uranium, 1), new ItemStack(ModItems.nugget_ra226, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_plutonium), new ItemStack[] { new ItemStack(ModItems.powder_plutonium, 2), new ItemStack(ModItems.powder_plutonium, 2), new ItemStack(ModItems.powder_polonium, 1), new ItemStack(ModItems.nugget_silicon, 1) });

		recipes.put(new ComparableStack(ModItems.crystal_titanium), new ItemStack[] { new ItemStack(ModItems.powder_titanium, 2), new ItemStack(ModItems.powder_titanium, 2), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.titanium_trace_metals_slurry, 1) });

		recipes.put(new ComparableStack(ModItems.crystal_sulfur), new ItemStack[] { new ItemStack(ModItems.sulfur, 4), new ItemStack(ModItems.sulfur, 4), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_niter), new ItemStack[] { new ItemStack(ModItems.niter, 3), new ItemStack(ModItems.niter, 3), new ItemStack(ModItems.niter, 3), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_copper), new ItemStack[] { new ItemStack(ModItems.powder_copper, 2), new ItemStack(ModItems.powder_copper, 2), new ItemStack(ModItems.powder_copper, 1), new ItemStack(ModItems.powder_cobalt_tiny, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_tungsten), new ItemStack[] { new ItemStack(ModItems.powder_tungsten, 2), new ItemStack(ModItems.powder_tungsten, 2), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_aluminium), new ItemStack[] { new ItemStack(ModItems.powder_aluminium, 2), new ItemStack(ModItems.powder_aluminium, 2), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_fluorite), new ItemStack[] { new ItemStack(ModItems.fluorite, 4), new ItemStack(ModItems.fluorite, 4), new ItemStack(ModItems.gem_sodalite, 2), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_beryllium), new ItemStack[] { new ItemStack(ModItems.powder_beryllium, 2), new ItemStack(ModItems.powder_beryllium, 2), new ItemStack(ModItems.powder_quartz, 1), new ItemStack(ModItems.powder_lithium, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_lead), new ItemStack[] { new ItemStack(ModItems.powder_lead, 2), new ItemStack(ModItems.powder_silver, 4), new ItemStack(ModItems.powder_gold, 1), new ItemStack(ModItems.powder_copper, 1) });


		recipes.put(new ComparableStack(ModItems.crystal_phosphorus), new ItemStack[] { new ItemStack(ModItems.powder_fire, 3), new ItemStack(ModItems.powder_fire, 3), new ItemStack(ModItems.ingot_phosphorus, 2), new ItemStack(Items.blaze_powder, 2) });
		recipes.put(new ComparableStack(ModItems.crystal_lithium), new ItemStack[] { new ItemStack(ModItems.powder_lithium, 2), new ItemStack(ModItems.powder_lithium, 2), new ItemStack(ModItems.powder_quartz, 1), new ItemStack(ModItems.fluorite, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_cobalt), new ItemStack[] { new ItemStack(ModItems.powder_cobalt, 2), new ItemStack(ModItems.powder_iron, 3), new ItemStack(ModItems.powder_copper, 3), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_mineral), new ItemStack[] { new ItemStack(ModItems.mineral_dust, 2), new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.powder_aluminium, 2), new ItemStack(ModItems.nugget_silicon, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_nickel), new ItemStack[] { new ItemStack(ModItems.powder_nickel, 2), new ItemStack(ModItems.powder_nickel, 2), new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.powder_titanium, 1) });
		recipes.put(new ComparableStack(ModItems.crystal_niobium), new ItemStack[] { new ItemStack(ModItems.powder_niobium, 2), new ItemStack(ModItems.powder_niobium, 2), new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.nugget_hafnium, 1) }); //THERE WE GO

		recipes.put(new ComparableStack(ModItems.crystal_basaltic), new ItemStack[] { new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.powder_calcium, 2), new ItemStack(ModItems.nugget_silicon, 1), new ItemStack(ModItems.powder_aluminium, 1) });


		//real shit
		// Froth Flotation (chalcopyrite): Chalcopyrite (CuFeS2)
		recipes.put(new ComparableStack(ModItems.chalcopyrite), new ItemStack[] {
				new ItemStack(ModBlocks.ore_copper, 4),
				new ItemStack(ModItems.powder_iron, 2),
				new ItemStack(Items.gold_nugget, 1),
			//Secondary Elements: Often contains gold, silver, nickel, and cobalt, which can be extracted as byproducts.
				new ItemStack(ModItems.sulfur, 1) });

		//Bornite Cu5FeS4
		recipes.put(new ComparableStack(ModItems.bornite), new ItemStack[] {
				new ItemStack(ModBlocks.ore_copper, 5),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.nugget_bismuth, 1),
				//trace ^
				new ItemStack(ModItems.sulfur, 4) });

		//covellite CuS
		recipes.put(new ComparableStack(ModItems.covellite), new ItemStack[] {
				new ItemStack(ModBlocks.ore_copper, 3),
				new ItemStack(ModItems.powder_cadmium, 1),
				new ItemStack(ModItems.nugget_lead, 1),
				//Sb (antimony) should be in place of lead but I'll have to add it.
				//trace ^
				new ItemStack(ModItems.sulfur, 3) });

		//galena PbS
		recipes.put(new ComparableStack(ModItems.galena), new ItemStack[] {
				new ItemStack(ModItems.powder_lead, 3),
				new ItemStack(ModItems.nugget_silver, 16),

				new ItemStack(ModItems.nugget_bismuth, 1),
				//minor/trace elements—notably
				//silver
				//bismuth
				//selenium
				//and tellurium
				new ItemStack(ModItems.sulfur, 1) });

		//sphalerite ZnS
		recipes.put(new ComparableStack(ModItems.sphalerite), new ItemStack[] {
				new ItemStack(ModItems.powder_zinc, 8),
				new ItemStack(ModItems.powder_cadmium, 1),
				new ItemStack(ModItems.nugget_gallium, 1),
				//new ItemStack(ModItems.nugget_cobalt, 1),
				//replace with germanium when added ^
				//minor/trace elements—notably
				//cadmium (Cd), gallium (Ga), germanium (Ge), and indium (In)
				//wait that's 5 nevermind
				new ItemStack(ModItems.sulfur, 1) });

		//pentlandite (Fe,Ni)9S8
		recipes.put(new ComparableStack(ModItems.pentlandite), new ItemStack[] {
				new ItemStack(ModItems.powder_iron, 9),
				new ItemStack(ModItems.powder_nickel, 9),
				new ItemStack(ModItems.nugget_ruthenium, 1),
				//ruthenium byproduct
				new ItemStack(ModItems.sulfur, 8) });

		//pyrrhotite Fe(1-x)S
		recipes.put(new ComparableStack(ModItems.pyrrhotite), new ItemStack[] {
				new ItemStack(ModItems.powder_iron, 10),
				new ItemStack(ModItems.nugget_cobalt, 1),
				//trace cobalt
				new ItemStack(ModItems.sulfur, 10) });


		//gravity separation:
		//wolframite
		recipes.put(new ComparableStack(ModItems.wolframite), new ItemStack[] {
				new ItemStack(ModItems.powder_tungsten, 3),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.manganese_powder, 1),
				//trace manganese
			//ill fix this crap later
				new ItemStack(Blocks.gravel, 1) });

		//columbite
		recipes.put(new ComparableStack(ModItems.powder_columbite), new ItemStack[] {
				new ItemStack(ModItems.powder_niobium, 2),
				new ItemStack(ModItems.powder_tantalium, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.manganese_powder, 1) });

		//chromite processing
		recipes.put(new ComparableStack(ModItems.chromite), new ItemStack[] {
				new ItemStack(ModItems.powder_chromium, 2),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.gravel, 1) });

		//froth flotation of molybdenite
		recipes.put(new ComparableStack(ModItems.powder_molybdenite), new ItemStack[] {
				new ItemStack(ModItems.powder_rhenium, 1),
				new ItemStack(ModItems.ingot_molybdenum, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.powder_coal_tiny, 1)
				 });

		//pgm_residue into platinum group metals
		recipes.put(new ComparableStack(ModItems.pgm_residue), new ItemStack[] {
				new ItemStack(ModItems.powder_rhodium_solution, 1),
				new ItemStack(ModItems.powder_palladium, 1),
				new ItemStack(ModItems.powder_platnium, 1), //mod is made by a fucking IDIOT
				// I just work here and don't feel like renaming. Could I refactor? Yes. Will I? No.
				new ItemStack(ModItems.powder_iron, 1) });

		//stibnite
		recipes.put(new ComparableStack(ModItems.stibnite), new ItemStack[] {
				new ItemStack(ModItems.powder_antimony_trioxide, 2),
				new ItemStack(Items.gold_nugget, 1),
				new ItemStack(ModItems.nugget_silver, 1),
				new ItemStack(ModItems.powder_lead, 1) });

		//gadoliniumsol to gadolinium concentrate
		recipes.put(new ComparableStack(ModItems.gadoliniumsol), new ItemStack[] {
				new ItemStack(ModItems.powder_gadolinium, 1),
				new ItemStack(ModItems.nugget_th232, 1),
				new ItemStack(ModItems.powder_cerium_tiny)
				//new ItemStack(ModItems.powder_iron, 1) //no?
		});




	}



	@Override
	public void registerPost() {

		if(!IMCCentrifuge.buffer.isEmpty()) {
			recipes.putAll(IMCCentrifuge.buffer);
			MainRegistry.logger.info("Fetched " + IMCCentrifuge.buffer.size() + " IMC centrifuge recipes!");
			IMCCentrifuge.buffer.clear();
		}
	}

	public static ItemStack[] getOutput(ItemStack stack) {

		if(stack == null || stack.getItem() == null)
			return null;

		ComparableStack comp = new ComparableStack(stack).makeSingular();

		if(recipes.containsKey(comp))
			return RecipesCommon.copyStackArray(recipes.get(comp));

		for(Entry<AStack, ItemStack[]> entry : recipes.entrySet()) {
			if(entry.getKey().isApplicable(stack)) {
				return RecipesCommon.copyStackArray(entry.getValue());
			}
		}

		return null;
	}

	public static HashMap getRecipes() {

		HashMap<Object, Object[]> recipes = new HashMap<Object, Object[]>();

		for(Entry<AStack, ItemStack[]> entry : CentrifugeRecipes.recipes.entrySet()) {
			recipes.put(entry.getKey(), entry.getValue());
		}

		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmCentrifuge.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;
		AStack in = this.readAStack(obj.get("input").getAsJsonArray());
		ItemStack[] out = this.readItemStackArray((JsonArray) obj.get("output"));
		this.recipes.put(in, out);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		try {
			Entry<AStack, ItemStack[]> entry = (Entry<AStack, ItemStack[]>) recipe;
			writer.name("input");
			this.writeAStack(entry.getKey(), writer);
			writer.name("output").beginArray();
			for(ItemStack stack : entry.getValue()) {
				this.writeItemStack(stack, writer);
			}
			writer.endArray();
		} catch(Exception ex) {
			MainRegistry.logger.error(ex);
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	@Override
	public String getComment() {
		return "Outputs have to be an array of up to four item stacks. Fewer aren't used by default recipes, but should work anyway.";
	}
}
