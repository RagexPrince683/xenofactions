package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemWatzPellet;
import com.hbm.items.special.ItemBedrockOre.EnumBedrockOre;
import com.hbm.main.MainRegistry;
import com.hbm.util.Compat;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ShredderRecipes extends SerializableRecipe {

	/* Post generation is deliberately conservative.  Ore dictionary names describe recipe
	 * interchangeability, not whether an object is safe to dismantle. */
	private static final Set<String> RAW_FORMS = new HashSet<>(Arrays.asList(
		"Coal", "Diamond", "Emerald", "Lapis", "Quartz", "Redstone", "Fluorite",
		"Sulfur", "Niter", "Uranium", "Thorium", "Plutonium", "Trixite"));
	private static final Set<String> STORAGE_MATERIALS = new HashSet<>(Arrays.asList(
		"Iron", "Gold", "Coal", "Diamond", "Emerald", "Lapis", "Redstone",
		"Copper", "Tin", "Lead", "Zinc", "Aluminium", "Titanium", "Tungsten",
		"Steel", "Beryllium", "Cobalt", "Nickel", "Uranium", "Thorium", "Plutonium"));
	private static final Set<String> REJECTED_NAMES = new HashSet<>(Arrays.asList(
		"wall", "panel", "grate", "furniture", "cabinet", "toaster", "computer", "crt",
		"office", "sign", "light", "deco_pipe", "vehicle", "boxcar", "launch", "oil",
		"machine", "multiblock", "appliance", "scaffold", "stairs", "flower_pot"));
	private static final boolean AUDIT_POST_RECIPES = false;

	public static HashMap<ComparableStack, ItemStack> shredderRecipes = new HashMap<>();
	public static HashMap<Object, Object> neiShredderRecipes;

	@Override
	public void registerPost() {

		String[] names = OreDictionary.getOreNames();

		for(int i = 0; i < names.length; i++) {

			String name = names[i];

			//if the dict contains invalid names, skip
			if(name == null || name.isEmpty())
				continue;

			if(name.contains("Any")) continue;

			List<ItemStack> matches = OreDictionary.getOres(name);

			//if the name isn't assigned to an ore, also skip
			if(matches == null || matches.isEmpty())
				continue;

			// Forms are separate from materials: only materials with an RTM dust are accepted.
			generateRecipes("ingot", name, matches, 1);
			generateRecipes("plate", name, matches, 1);
			generateApprovedRawRecipes("gem", name, matches, 1);
			generateApprovedRawRecipes("crystal", name, matches, 1);
			generateRecipes("ore", name, matches, 2);

			if(name.startsWith("block") && STORAGE_MATERIALS.contains(name.substring(5))) {
				String material = name.substring(5);
				ItemStack dust = getDustByName(material);

				if(dust != null && dust.getItem() != ModItems.scrap) {

					dust.stackSize = 9;

					if(getIngotOrGemByName(material) == null)
						dust.stackSize = 4;

					for(ItemStack stack : matches) {
						putIfValid(stack, dust, name);
					}
				}
			}

			if(name.length() > 7 && name.substring(0, 8).equals("dustTiny")) {
				for(ItemStack stack : matches) {
					putIfValid(stack, new ItemStack(ModItems.dust_tiny), name);
				}
			} else if(name.length() > 3 && name.substring(0, 4).equals("dust")) {
				for(ItemStack stack : matches) {
					putIfValid(stack, new ItemStack(ModItems.dust), name);
				}
			}
		}
		if(AUDIT_POST_RECIPES) auditPostGeneratedRecipes();
	}

	private static void generateApprovedRawRecipes(String prefix, String name, List<ItemStack> matches, int outCount) {
		if(name.startsWith(prefix) && RAW_FORMS.contains(name.substring(prefix.length())))
			generateRecipes(prefix, name, matches, outCount);
	}

	private static void generateRecipes(String prefix, String name, List<ItemStack> matches, int outCount) {

		int len = prefix.length();

		if(name.length() > len && name.startsWith(prefix)) {
			String matName = name.substring(len);

			ItemStack dust = getDustByName(matName);

			if(dust != null && dust.getItem() != ModItems.scrap) {

				dust.stackSize = outCount;

				for(ItemStack stack : matches) {
					putIfValid(stack, dust, name);
				}
			}
		}
	}

	private static void putIfValid(ItemStack in, ItemStack dust, String name) {

		if(in != null) {

			if(in.getItem() != null) {
				if(in.getItemDamage() == OreDictionary.WILDCARD_VALUE && !name.startsWith("ore")) return;
				if(name.startsWith("block") && !isOwnedStorageBlock(in)) return;
				if(isUnsafeManufacturedObject(in)) return;
				setRecipe(new ComparableStack(in), dust);
			} else {
				MainRegistry.logger.error("Ore dict entry '" + name + "' has a null item in its stack! How does that even happen?");
				Thread.currentThread().dumpStack();
			}

		} else {
			MainRegistry.logger.error("Ore dict entry '" + name + "' has a null stack!");
			Thread.currentThread().dumpStack();
		}
	}

	private static boolean isOwnedStorageBlock(ItemStack stack) {
		Object key = Item.itemRegistry.getNameForObject(stack.getItem());
		if(key == null) return false;
		String registryName = key.toString().toLowerCase();
		// A foreign object cannot acquire salvage semantics by registering as blockSteel.
		return registryName.startsWith("hbm:") || registryName.startsWith("minecraft:");
	}

	private static boolean isUnsafeManufacturedObject(ItemStack stack) {
		Block block = Block.getBlockFromItem(stack.getItem());
		if(block != null && block != Blocks.air && block.hasTileEntity(stack.getItemDamage())) return true;
		Object key = Item.itemRegistry.getNameForObject(stack.getItem());
		String registryName = key == null ? "" : key.toString().toLowerCase();
		for(String rejected : REJECTED_NAMES) if(registryName.contains(rejected)) return true;
		return false;
	}

	/** Development diagnostic for reviewing the final post-generated shredder surface. */
	public static void auditPostGeneratedRecipes() {
		for(Entry<ComparableStack, ItemStack> entry : shredderRecipes.entrySet()) {
			ItemStack input = entry.getKey().toStack();
			int[] ids = OreDictionary.getOreIDs(input);
			StringBuilder names = new StringBuilder();
			for(int id : ids) {
				if(names.length() > 0) names.append(',');
				names.append(OreDictionary.getOreName(id));
			}
			MainRegistry.logger.info("Shredder audit: " + input + " [" + names + "] -> " + entry.getValue());
		}
	}

	@Override
	public void registerDefaults() {

		//actual real life bullshit:

		//Fe2
		ShredderRecipes.setRecipe(new ItemStack(ModBlocks.stone_resource, 1, EnumStoneType.HEMATITE.ordinal()), new ItemStack(Blocks.iron_ore, 2));
		//idea: you get a benefit for doing the REAL industrial processes. The old ones will still exist though. For now.
		//Fe3
		ShredderRecipes.setRecipe(new ItemStack(ModItems.magnetite), new ItemStack(Blocks.iron_ore, 3));

		//ShredderRecipes.setRecipe(new ItemStack(ModItems.goethite), new ItemStack(ModItems.crystal_iron, 1));
		//goethite -> hematite in a furnace

		//oh come on, is there really no way to export more than one itemstack at a time per shred?
			//i guess i could just make a custom itemstack that contains multiple stacks, but that seems like a lot of work for something that would only be used in like 5 recipes at most


		//chalcocite
		ShredderRecipes.setRecipe(new ItemStack(ModItems.chalcocite), new ItemStack(ModBlocks.ore_copper, 2)); //should also produce sulfur but whatever

		//galena
		ShredderRecipes.setRecipe(new ItemStack(ModItems.galena), new ItemStack(ModItems.crystal_lead, 2));

		//sphalerite
		ShredderRecipes.setRecipe(new ItemStack(ModItems.sphalerite), new ItemStack(ModBlocks.ore_zinc, 2)); //should also produce sulfur but whatever

		//tin
		ShredderRecipes.setRecipe(new ItemStack(ModItems.cassiterite), new ItemStack(ModBlocks.ore_tin, 2));

		//columbite
		ShredderRecipes.setRecipe(new ItemStack(ModItems.columbite), new ItemStack(ModItems.powder_columbite, 2));

		//spodumene
		ShredderRecipes.setRecipe(new ItemStack(ModItems.spodumene), new ItemStack(ModItems.crushed_spodumene, 2));

		//petalite
		ShredderRecipes.setRecipe(new ItemStack(ModItems.petalite), new ItemStack(ModItems.crushed_petalite, 1));

		//ilmenite
		ShredderRecipes.setRecipe(new ItemStack(ModItems.ilmenite), new ItemStack(ModItems.crystal_titanium, 2));

		//rutile
		ShredderRecipes.setRecipe(new ItemStack(ModItems.rutile), new ItemStack(ModItems.powder_titanium, 3));

		//zircon
		ShredderRecipes.setRecipe(new ItemStack(ModItems.zircon), new ItemStack(ModItems.powder_zircon, 3));

		//magnesium -> glowstone, for my sanity.
		ShredderRecipes.setRecipe(new ItemStack(ModItems.magnesium_ingot), new ItemStack(Blocks.glowstone, 4));

		//other possibly realistic probably not, bullshit:


		/* Primary recipes */
		ShredderRecipes.setRecipe(ModItems.scrap, new ItemStack(ModItems.dust));
		ShredderRecipes.setRecipe(ModItems.dust, new ItemStack(ModItems.dust));
		ShredderRecipes.setRecipe(ModItems.dust_tiny, new ItemStack(ModItems.dust_tiny));
		ShredderRecipes.setRecipe(Blocks.glowstone, new ItemStack(Items.glowstone_dust, 4));
		ShredderRecipes.setRecipe(new ItemStack(ModBlocks.ore_glowstone, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.glowstone_dust, 4));
		//basic geology - granite shreds into quartz, diorite into limestone, andesite into clay, stone is just limestone im tired of pretending its not

		//ShredderRecipes.setRecipe(Blocks)
		ShredderRecipes.setRecipe(Items.quartz, new ItemStack(ModItems.powder_quartz));
		ShredderRecipes.setRecipe(Blocks.quartz_ore, new ItemStack(ModItems.powder_quartz, 2));
		ShredderRecipes.setRecipe(ModBlocks.ore_quartz, new ItemStack(ModItems.powder_quartz, 2));
		ShredderRecipes.setRecipe(new ItemStack(ModBlocks.ore_fire, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.powder_fire, 6));
		ShredderRecipes.setRecipe(ModBlocks.ore_fire, new ItemStack(ModItems.powder_fire, 6));
		ShredderRecipes.setRecipe(Blocks.packed_ice, new ItemStack(ModItems.powder_ice, 1));
		ShredderRecipes.setRecipe(ModBlocks.brick_obsidian, new ItemStack(ModBlocks.gravel_obsidian, 1));
		ShredderRecipes.setRecipe(Blocks.obsidian, new ItemStack(ModBlocks.gravel_obsidian, 1));
		ShredderRecipes.setRecipe(ModBlocks.ore_oil_empty, new ItemStack(Blocks.gravel, 1));
		ShredderRecipes.setRecipe(ModBlocks.ore_gas_empty, new ItemStack(Blocks.gravel, 1));
		ShredderRecipes.setRecipe(Blocks.cobblestone, new ItemStack(Blocks.gravel, 1));
		ShredderRecipes.setRecipe(Blocks.gravel, new ItemStack(Blocks.sand, 1));
		ShredderRecipes.setRecipe(Items.brick, new ItemStack(Items.clay_ball, 1));
		ShredderRecipes.setRecipe(Blocks.clay, new ItemStack(Items.clay_ball, 4));
		ShredderRecipes.setRecipe(Blocks.hardened_clay, new ItemStack(Items.clay_ball, 4));
		ShredderRecipes.setRecipe(Blocks.tnt, new ItemStack(Items.gunpowder, Compat.isModLoaded(Compat.MOD_GT6) ? 4 : 5));
		ShredderRecipes.setRecipe(DictFrame.fromOne(ModBlocks.stone_resource, EnumStoneType.LIMESTONE), new ItemStack(ModItems.powder_limestone, 4));
		ShredderRecipes.setRecipe(ModBlocks.stone_gneiss, new ItemStack(ModItems.powder_lithium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.powder_lapis, new ItemStack(ModItems.powder_cobalt_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_neodymium, new ItemStack(ModItems.powder_neodymium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_cobalt, new ItemStack(ModItems.powder_cobalt_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_alien, new ItemStack(ModItems.powder_alien_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_niobium, new ItemStack(ModItems.powder_niobium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_cerium, new ItemStack(ModItems.powder_cerium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_lanthanium, new ItemStack(ModItems.powder_lanthanium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_yttrium, new ItemStack(ModItems.powder_yttrium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_actinium, new ItemStack(ModItems.powder_actinium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_praseodymium, new ItemStack(ModItems.powder_praseodymium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_boron, new ItemStack(ModItems.powder_boron_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_meteorite, new ItemStack(ModItems.powder_meteorite_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_vanadium, new ItemStack(ModItems.powder_vanadium_tiny, 1));


		//terbium into terbium powder
		ShredderRecipes.setRecipe(ModItems.fragment_terbium, new ItemStack(ModItems.powder_terbium_tiny, 4));
		ShredderRecipes.setRecipe(ModItems.fragment_dysprosium, new ItemStack(ModItems.powder_dysprosium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_holmium, new ItemStack(ModItems.powder_holmium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_erbium, new ItemStack(ModItems.powder_erbium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_ytterbium, new ItemStack(ModItems.powder_ytterbium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_lutetium, new ItemStack(ModItems.powder_lutetium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_thulium, new ItemStack(ModItems.powder_thulium_tiny, 1));

		//PBR fuel pellets
		// fissile fuel
		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.HALEU1975.ordinal()),
			new ItemStack(ModItems.powder_spent_haleu, 2)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.HALEU15.ordinal()),
			new ItemStack(ModItems.powder_spent_haleu, 1)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.LEU5.ordinal()),
			new ItemStack(ModItems.powder_spent_leu, 1)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.TH232.ordinal()),
			new ItemStack(ModItems.powder_spent_thorium, 1)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.U233.ordinal()),
			new ItemStack(ModItems.powder_spent_u233, 1)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.MOX241.ordinal()),
			new ItemStack(ModItems.powder_spent_mox, 1)
		);

		// moderator / absorbers
		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.GRAPHITE.ordinal()),
			new ItemStack(ModItems.dust_graphite, 1)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.LEAD.ordinal()),
			new ItemStack(ModItems.powder_lead_irradiated, 1)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.BORON.ordinal()),
			new ItemStack(ModItems.powder_boron_spent, 1)
		);

		ShredderRecipes.setRecipe(
			new ItemStack(ModItems.watz_pellet_depleted, 1, ItemWatzPellet.EnumWatzType.DU.ordinal()),
			new ItemStack(ModItems.powder_du_spent, 1)
		);



		//ShredderRecipes.setRecipe(ModBlocks.ore_sellafield_diamond, new ItemStack(ModBlocks.gravel_diamond, 2));
		ShredderRecipes.setRecipe(ModItems.coal_infernal, new ItemStack(ModItems.powder_coal, 2));
		ShredderRecipes.setRecipe(Items.fermented_spider_eye, new ItemStack(ModItems.powder_poison, 3));
		ShredderRecipes.setRecipe(Items.poisonous_potato, new ItemStack(ModItems.powder_poison, 1));
		ShredderRecipes.setRecipe(ModBlocks.ore_tektite_osmiridium, new ItemStack(ModItems.powder_tektite, 1));
		ShredderRecipes.setRecipe(Blocks.dirt, new ItemStack(ModItems.dust, 1));
		ShredderRecipes.setRecipe(Items.reeds, new ItemStack(Items.sugar, 3));
		ShredderRecipes.setRecipe(Items.apple, new ItemStack(Items.sugar, 1));
		ShredderRecipes.setRecipe(Items.carrot, new ItemStack(Items.sugar, 1));
		ShredderRecipes.setRecipe(ModItems.crystal_cleaned, new ItemStack(ModItems.mineral_dust, 4));

		ShredderRecipes.setRecipe(new ItemStack (ModBlocks.ore_potash, 1 , OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.powder_potash, 4));
		//pollucite powder
		ShredderRecipes.setRecipe(new ItemStack(ModBlocks.ore_pollucite, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.powder_pollucite, 4));

		ShredderRecipes.setRecipe(new ItemStack(ModBlocks.ore_mineral, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.mineral_dust, 1)); // it was deserved

		//powder_celestite
		ShredderRecipes.setRecipe(new ItemStack(ModBlocks.ore_celestite, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.powder_celestite, 4));

		//molybdenite
		ShredderRecipes.setRecipe(new ItemStack(ModItems.molybdenite, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.powder_molybdenite, 4));

		//osmiridium step 1
		ShredderRecipes.setRecipe(new ItemStack(ModItems.osmiridium, 1), new ItemStack(ModItems.powder_impure_osmiridium, 1));

		//erbium ingot back into erbium powder
		 ShredderRecipes.setRecipe(new ItemStack(ModItems.ingot_erbium, 1), new ItemStack(ModItems.erbium_powder, 1));
		 //ingot ytterbium back into powder
		 ShredderRecipes.setRecipe(new ItemStack(ModItems.ingot_ytterbium, 1), new ItemStack(ModItems.powder_ytterbium, 1));

		ShredderRecipes.setRecipe(ModItems.bean_roast,  new ItemStack(ModItems.powder_coffee, 1));

		//BYPRODUCTS
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 0), new ItemStack(ModItems.powder_iron, 10));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 1), new ItemStack(ModItems.powder_copper, 10));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 2), new ItemStack(ModItems.powder_lithium, 10));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 3), new ItemStack(ModItems.powder_quartz, 8));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 4), new ItemStack(ModItems.powder_lead, 6));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 5), new ItemStack(ModItems.powder_titanium, 9));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 6), new ItemStack(ModItems.powder_aluminium, 12));
	//	ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 7), new ItemStack(ModItems.sulfur, 6));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 1, 8), new ItemStack(Items.bone, 4));
		//ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_byproduct, 2, 9), new ItemStack(ModItems.nugget_bismuth, 1));

		ShredderRecipes.setRecipe(DictFrame.fromOne(ModBlocks.stone_resource, EnumStoneType.LIMESTONE), new ItemStack(ModItems.powder_calcium, 4));
		ShredderRecipes.setRecipe(DictFrame.fromOne(ModBlocks.stone_resource, EnumStoneType.CALCIUM), new ItemStack(ModItems.powder_calcium, 6));
		ShredderRecipes.setRecipe(ModItems.can_empty, new ItemStack(ModItems.powder_aluminium, 2));
		//ShredderRecipes.setRecipe(DictFrame.fromOne(ModItems.chunk_ore, EnumChunkType.RARE), new ItemStack(ModItems.powder_desh_mix));
		ShredderRecipes.setRecipe(Blocks.sand, new ItemStack(ModItems.dust, 2));
		ShredderRecipes.setRecipe(ModBlocks.block_slag, new ItemStack(ModItems.powder_cement, 4));

		List<ItemStack> logs = OreDictionary.getOres("logWood");
		List<ItemStack> planks = OreDictionary.getOres("plankWood");
		List<ItemStack> saplings = OreDictionary.getOres("treeSapling");
		List<ItemStack> stones = OreDictionary.getOres(OreDictManager.KEY_STONE);
		List<ItemStack> cobbles = OreDictionary.getOres(OreDictManager.KEY_COBBLESTONE);
		List<ItemStack> sands = OreDictionary.getOres(OreDictManager.KEY_SAND);

		for(ItemStack log : logs) ShredderRecipes.setRecipe(log, new ItemStack(ModItems.powder_sawdust, 4));
		for(ItemStack plank : planks) ShredderRecipes.setRecipe(plank, new ItemStack(ModItems.powder_sawdust, 1));
		for(ItemStack sapling : saplings) ShredderRecipes.setRecipe(sapling, new ItemStack(Items.stick, 1));
		for(ItemStack stone : stones) ShredderRecipes.setRecipe(stone, new ItemStack(Blocks.gravel, 1));
		for(ItemStack cobble : cobbles) ShredderRecipes.setRecipe(cobble, new ItemStack(Blocks.gravel, 1));
		for(ItemStack sand : sands) ShredderRecipes.setRecipe(sand, new ItemStack(ModItems.dust, 2));

		for(EnumBedrockOre ore : EnumBedrockOre.values()) {
			int i = ore.ordinal();
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_bedrock, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_centrifuged, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_cleaned, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_separated, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_purified, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_nitrated, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_nitrocrystalline, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_deepcleaned, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			ShredderRecipes.setRecipe(new ItemStack(ModItems.ore_seared, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
		}

		for(int i = 0; i < 5; i++) ShredderRecipes.setRecipe(new ItemStack(Items.skull, 1, i), new ItemStack(ModItems.biomass, 4));

		/* Crystal processing */
		//ShredderRecipes.setRecipe(ModItems.ingot_schraranium, new ItemStack(ModItems.nugget_schrabidium, 2));
		ShredderRecipes.setRecipe(ModItems.crystal_coal, new ItemStack(ModItems.powder_coal, 1));
		ShredderRecipes.setRecipe(ModItems.crystal_iron, new ItemStack(ModItems.powder_iron, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_gold, new ItemStack(ModItems.powder_gold, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_redstone, new ItemStack(Items.redstone, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_lapis, new ItemStack(ModItems.powder_lapis, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_diamond, new ItemStack(ModItems.powder_diamond, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_uranium, new ItemStack(ModItems.powder_uranium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_plutonium, new ItemStack(ModItems.powder_plutonium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_thorium, new ItemStack(ModItems.powder_thorium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_titanium, new ItemStack(ModItems.powder_titanium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_sulfur, new ItemStack(ModItems.sulfur, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_niter, new ItemStack(ModItems.niter, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_copper, new ItemStack(ModItems.powder_copper, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_tungsten, new ItemStack(ModItems.powder_tungsten, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_aluminium, new ItemStack(ModItems.powder_aluminium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_fluorite, new ItemStack(ModItems.fluorite, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_beryllium, new ItemStack(ModItems.powder_beryllium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_lead, new ItemStack(ModItems.powder_lead, 3));
		//ShredderRecipes.setRecipe(ModItems.crystal_schraranium, new ItemStack(ModItems.nugget_schrabidium, 3));
		//ShredderRecipes.setRecipe(ModItems.crystal_schrabidium, new ItemStack(ModItems.powder_schrabidium, 3));
		//ShredderRecipes.setRecipe(ModItems.crystal_rare, new ItemStack(ModItems.powder_desh_mix, 2));
		ShredderRecipes.setRecipe(ModItems.crystal_phosphorus, new ItemStack(ModItems.powder_fire, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_trixite, new ItemStack(ModItems.powder_plutonium, 6));
		ShredderRecipes.setRecipe(ModItems.crystal_lithium, new ItemStack(ModItems.powder_lithium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_starmetal, new ItemStack(ModItems.powder_dura_steel, 6));
		ShredderRecipes.setRecipe(ModItems.crystal_cobalt, new ItemStack(ModItems.powder_cobalt, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_nickel, new ItemStack(ModItems.powder_nickel, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_niobium, new ItemStack(ModItems.powder_niobium, 3));

		/* Manufactured objects are intentionally not universal salvage. */
		ShredderRecipes.setRecipe(new ItemStack(ModItems.bedrock_ore, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Blocks.gravel));

		/* Sellafite scrapping */
		//ShredderRecipes.setRecipe(ModBlocks.sellafield_slaked, new ItemStack(Blocks.gravel));
		//ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 0), new ItemStack(ModItems.scrap_nuclear, 1));
		//ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 1), new ItemStack(ModItems.scrap_nuclear, 2));
		//ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 2), new ItemStack(ModItems.scrap_nuclear, 3));
		//ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 3), new ItemStack(ModItems.scrap_nuclear, 5));
		//ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 4), new ItemStack(ModItems.scrap_nuclear, 7));
		//ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 5), new ItemStack(ModItems.scrap_nuclear, 15));

		//scorched_stone
		ShredderRecipes.setRecipe(ModBlocks.scorched_stone, new ItemStack(ModItems.scrap_nuclear, 1));

		//europium stage 1
		ShredderRecipes.setRecipe(ModItems.fragment_europium, new ItemStack(ModItems.europium_dust_tiny, 1));

		//fragment_gadolinium
		ShredderRecipes.setRecipe(ModItems.fragment_gadolinium, new ItemStack(ModItems.gadolinium_dust_tiny, 1));

		/* Fracking debris scrapping */
		ShredderRecipes.setRecipe(ModBlocks.dirt_dead, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.dirt_oily, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.sand_dirty, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.sand_dirty_red, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.stone_cracked, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.stone_porous, new ItemStack(ModItems.scrap_oil, 1));

		/* Wool and clay scrapping */
		for(int i = 0; i < 16; i++) {
			ShredderRecipes.setRecipe(new ItemStack(Blocks.stained_hardened_clay, 1, i), new ItemStack(Items.clay_ball, 4));
			ShredderRecipes.setRecipe(new ItemStack(Blocks.wool, 1, i), new ItemStack(Items.string, 4));
		}

		/* Shredding bobbleheads */
		for(int i = 0; i < BobbleType.values().length; i++) {
			BobbleType type = BobbleType.values()[i];
			ShredderRecipes.setRecipe(new ItemStack(ModBlocks.bobblehead, 1, i), new ItemStack(ModItems.scrap_plastic, 1, type.scrap.ordinal()));
		}

		/* Debris shredding */
		ShredderRecipes.setRecipe(ModItems.debris_concrete, new ItemStack(ModItems.scrap_nuclear, 2));
		ShredderRecipes.setRecipe(ModItems.debris_shrapnel, new ItemStack(ModItems.powder_steel_tiny, 5));
		ShredderRecipes.setRecipe(ModItems.debris_exchanger, new ItemStack(ModItems.powder_steel, 3));
		ShredderRecipes.setRecipe(ModItems.debris_element, new ItemStack(ModItems.scrap_nuclear, 4));
		ShredderRecipes.setRecipe(ModItems.debris_metal, new ItemStack(ModItems.powder_steel_tiny, 3));
		ShredderRecipes.setRecipe(ModItems.debris_graphite, new ItemStack(ModItems.powder_coal, 1));

		/* GC COMPAT */
		Block gcMoonBlock = Compat.tryLoadBlock(Compat.MOD_GCC, "moonBlock");
		if(gcMoonBlock != null && gcMoonBlock != Blocks.air) {
			ShredderRecipes.setRecipe(new ItemStack(gcMoonBlock, 1, 3), new ItemStack(ModBlocks.moon_turf)); //Moon dirt
			ShredderRecipes.setRecipe(new ItemStack(gcMoonBlock, 1, 5), new ItemStack(ModBlocks.moon_turf)); //Moon topsoil
		}

		/* AR COMPAT */
		Block arMoonTurf = Compat.tryLoadBlock(Compat.MOD_AR, "turf");
		if(arMoonTurf != null && gcMoonBlock != Blocks.air) ShredderRecipes.setRecipe(arMoonTurf, new ItemStack(ModBlocks.moon_turf)); //i assume it's moon turf
		Block arMoonTurfDark = Compat.tryLoadBlock(Compat.MOD_AR, "turfDark");
		if(arMoonTurfDark != null && gcMoonBlock != Blocks.air) ShredderRecipes.setRecipe(arMoonTurfDark, new ItemStack(ModBlocks.moon_turf)); //probably moon dirt? would have helped if i had ever played AR for more than 5 seconds
	}

	/**
	 * Returns scrap when no dust is found, for quickly adding recipes
	 * @param name
	 * @return
	 */
	public static ItemStack getDustByName(String name) {

		List<ItemStack> matches = OreDictionary.getOres("dust" + name);

		if(matches != null) for(ItemStack match : matches) {
			Object key = match == null || match.getItem() == null ? null : Item.itemRegistry.getNameForObject(match.getItem());
			String registryName = key == null ? "" : key.toString().toLowerCase();
			if(registryName.startsWith("hbm:") || registryName.startsWith("minecraft:")) return match.copy();
		}

		return new ItemStack(ModItems.scrap);
	}

	/**
	 * Returns null when no ingot or gem is found, for deciding whether the block shredding output should be 9 or 4 dusts
	 * @param name
	 * @return
	 */
	public static ItemStack getIngotOrGemByName(String name) {

		List<ItemStack> matches = OreDictionary.getOres("ingot" + name);

		if(matches != null && !matches.isEmpty())
			return matches.get(0).copy();

		matches = OreDictionary.getOres("gem" + name);

		if(matches != null && !matches.isEmpty())
			return matches.get(0).copy();

		return null;
	}

	public static void setRecipe(Item in, ItemStack out) {
		setRecipe(new ComparableStack(in), out);
	}

	public static void setRecipe(Block in, ItemStack out) {
		setRecipe(new ComparableStack(in), out);
	}

	public static void setRecipe(ItemStack in, ItemStack out) {
		setRecipe(new ComparableStack(in), out);
	}

	public static void setRecipe(ComparableStack in, ItemStack out) {
		if(!shredderRecipes.containsKey(in)) {
			shredderRecipes.put(in, out);
		}
	}

	public static Map<Object, Object> getShredderRecipes() {

		//convert the map only once to save on processing power (might be more ram intensive but that can't be THAT bad, right?)
		if(neiShredderRecipes == null)
			neiShredderRecipes = new HashMap<>(shredderRecipes);

		return neiShredderRecipes;
	}

	public static ItemStack getShredderResult(ItemStack stack) {

		if(stack == null || stack.getItem() == null)
			return new ItemStack(ModItems.scrap);

		ComparableStack comp = new ComparableStack(stack).makeSingular();
		ItemStack sta = shredderRecipes.get(comp);

		if(sta == null) {
			comp.meta = OreDictionary.WILDCARD_VALUE;
			sta = shredderRecipes.get(comp);
		}

		return sta == null ? new ItemStack(ModItems.scrap) : sta;
	}

	@Override
	public String getFileName() {
		return "hbmShredder.json";
	}

	@Override
	public Object getRecipeObject() {
		return shredderRecipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;
		ItemStack stack = this.readItemStack(obj.get("input").getAsJsonArray());
		ComparableStack comp = new ComparableStack(stack).makeSingular();
		ItemStack out = this.readItemStack(obj.get("output").getAsJsonArray());
		this.shredderRecipes.put(comp, out);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<ComparableStack, ItemStack> entry = (Entry<ComparableStack, ItemStack>) recipe;

		writer.name("input");
		this.writeItemStack(entry.getKey().toStack(), writer);
		writer.name("output");
		this.writeItemStack(entry.getValue(), writer);
	}

	@Override
	public void deleteRecipes() {
		this.shredderRecipes.clear();
		this.neiShredderRecipes = null;
	}

	@Override
	public String getComment() {
		return "Approved feedstock forms -> RTM dust recipes are generated in post and cannot be changed with the config. Storage blocks require an explicit homogeneous-material approval; ore-dictionary substitution alone never grants salvage semantics.";
	}
}
