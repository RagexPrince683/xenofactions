package com.hbm.crafting;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemAmmoEnums.AmmoFatman;
import com.hbm.items.food.ItemConserve;
import com.hbm.items.machine.ItemCircuit.EnumCircuitType;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.main.CraftingManager;

import static com.hbm.inventory.OreDictManager.*;

import com.hbm.util.EnumUtil;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * For foods, drinks or other consumables
 * @author hbm
 */
public class ConsumableRecipes {

	public static void register() {

		//Airstikes
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 0), new Object[] { "TTT", "TRT", "TTT", 'T', Blocks.tnt, 'R', ModItems.detonator_laser });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 1), new Object[] { "TTT", "TRT", "TTT", 'T', ModItems.grenade_gascan, 'R', ModItems.detonator_laser });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 2), new Object[] { "TTT", "TRT", "TTT", 'T', ModItems.pellet_gas, 'R', ModItems.detonator_laser });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 3), new Object[] { "TRT", 'T', ModItems.grenade_cloud, 'R', ModItems.detonator_laser });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 4), new Object[] { "TR", 'T', ModItems.ammo_nuke.stackFromEnum(AmmoFatman.HIGH), 'R', ModItems.detonator_laser });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 8), new Object[] { "TRG", 'T', ModItems.iv_blood,'G',ModItems.acetylene_torch, 'R', ModItems.detonator_laser });

		//Food
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_waffle, 1), new Object[] { "WEW", "MPM", "WEW", 'W', Items.wheat, 'E', Items.egg, 'M', Items.milk_bucket, 'P', ModItems.man_core });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.schnitzel_vegan, 2), new Object[] { "RWR", "WPW", "RWR", 'W', Items.carrot, 'R', Items.reeds, 'P', Items.pumpkin_seeds });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.cotton_candy, 1), new Object[] { " S ", "SPS", " H ", 'P', Items.dye, 'S', Items.sugar, 'H', Items.stick });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_schrabidium, 1, 0), new Object[] { "SSS", "SAS", "SSS", 'S', SA326.nugget(), 'A', Items.apple });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_schrabidium, 1, 1), new Object[] { "SSS", "SAS", "SSS", 'S', SA326.ingot(), 'A', Items.apple });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_schrabidium, 1, 2), new Object[] { "SSS", "SAS", "SSS", 'S', SA326.block(), 'A', Items.apple });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_lead, 1, 0), new Object[] { "SSS", "SAS", "SSS", 'S', PB.nugget(), 'A', Items.apple });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_lead, 1, 1), new Object[] { "SSS", "SAS", "SSS", 'S', PB.ingot(), 'A', Items.apple });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_lead, 1, 2), new Object[] { "SSS", "SAS", "SSS", 'S', PB.block(), 'A', Items.apple });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_euphemium, 1), new Object[] { "EEE", "EAE", "EEE", 'E', EUPH.nugget(), 'A', Items.apple });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.tem_flakes, 1, 0), new Object[] { GOLD.nugget(), Items.paper });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.tem_flakes, 1, 1), new Object[] { GOLD.nugget(), GOLD.nugget(), GOLD.nugget(), Items.paper });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.tem_flakes, 1, 2), new Object[] { GOLD.ingot(), GOLD.ingot(), GOLD.nugget(), GOLD.nugget(), Items.paper });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.glowing_stew, 1), new Object[] { Items.bowl, Item.getItemFromBlock(ModBlocks.mush), Item.getItemFromBlock(ModBlocks.mush) });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.flesh_burger), new Object[] {Items.bread, ModItems.grilled_flesh});
		// A compact rendering/grinding abstraction: flesh is organic feedstock, not a net food source.
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.biomass), new Object[] {ModItems.flesh, ModItems.flesh, ModItems.flesh, ModItems.flesh});
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.balefire_scrambled, 1), new Object[] { Items.bowl, ModItems.egg_balefire });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.balefire_and_ham, 1), new Object[] { ModItems.balefire_scrambled, Items.cooked_beef });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.med_ipecac, 1), new Object[] { Items.glass_bottle, Items.wheat_seeds, Items.sugar});
		//idk what kind of shitty joke is trying to be made here but it really wasn't funny
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.med_ptsd, 1), new Object[] { ModItems.med_ipecac });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.pancake, 1), new Object[] { REDSTONE.dust(), DIAMOND.dust(), Items.wheat, STEEL.bolt(), CU.wireFine(), STEEL.plate() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.pancake, 1), new Object[] { REDSTONE.dust(), EMERALD.dust(), Items.wheat, STEEL.bolt(), CU.wireFine(), STEEL.plate() });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.chocolate_milk, 1), new Object[] { ModItems.glass_empty, new ItemStack(Items.dye, 1, 3), Items.milk_bucket, Items.sugar });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.loops), new Object[] { Items.bread, Items.wheat, Items.sugar });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.loop_stew), new Object[] { ModItems.loops, ModItems.can_smart, Items.bowl });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.spongebob_macaroni), new Object[] { Items.wheat, Items.wheat, ModItems.cheese });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.cheese), new Object[] { Items.wheat, Items.milk_bucket, ModItems.bucket_sulfuric_acid });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.cheese), new Object[] { Items.wheat_seeds, Items.milk_bucket, ModItems.bucket_sulfuric_acid });

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.cheese), new Object[] { Items.wheat, Items.milk_bucket, ModItems.sulfur });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.cheese), new Object[] { Items.wheat_seeds, Items.milk_bucket, ModItems.sulfur });

		////astronaut food
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.canned_conserve &&
		//	ItemConserve.EnumFoodType.TUBE.name().equals(EnumUtil.grabEnumSafely(ItemConserve.EnumFoodType.class, item.getItemDamage()).name())), new Object[] { Items.wheat_seeds, Items.milk_bucket, ModItems.sulfur });

		//List<ItemStack> allFoods = new ArrayList<>();
		//for (Object obj : GameData.getItemRegistry()) {
		//	if (obj instanceof Item) {
		//		Item item = (Item) obj;
		//		if (item instanceof ItemFood) {
		//			allFoods.add(new ItemStack(item));
		//		}
		//	}
		//}
//
		//int tubeMeta = ItemConserve.EnumFoodType.TUBE.ordinal(); // Get the ordinal value of the TUBE enum
		//CraftingManager.addShapelessAuto(
		//	new ItemStack(ModItems.canned_conserve, 1, tubeMeta),
		//	new Object[] {
		//		allFoods,
		//	}
		//);

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.twinkie), new Object[] { Items.wheat, Items.egg, Items.sugar, ModItems.butter});

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.pudding), new Object[] { Items.milk_bucket, new ItemStack(Items.dye, 1, 3), Items.sugar });

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.butter, 4), new Object[] { Items.milk_bucket, Items.bowl });

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.s_cream, 4), new Object[] { Items.milk_bucket, ModItems.butter, Items.sugar, Blocks.packed_ice, ModItems.strawberry});

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.nugget, 16), new Object[] { Items.cooked_chicken});

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.coffee), new Object[] { ModItems.powder_coffee, Items.milk_bucket, Items.potionitem, Items.sugar });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.coffee_radium), new Object[] { ModItems.coffee, RA226.nugget() });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.ingot_smore), new Object[] { Items.wheat, ModItems.marshmallow, new ItemStack(Items.dye, 1, 3) });
		//oh no im not doing allat
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.marshmallow), new Object[] { Items.stick, Items.sugar, Items.wheat_seeds });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.quesadilla, 3), new Object[] { ModItems.cheese, ModItems.cheese, Items.bread });

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.chocolate, 3), new Object[] { RA226.nugget(), Items.wheat, new ItemStack(Items.dye, 1, 3), Items.sugar });
		//Peas
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.peas), new Object[] { Items.wheat_seeds, Items.wheat_seeds, Items.wheat_seeds, Items.wheat_seeds });

		//Cans
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.can_empty, 1), new Object[] { "P", "P", 'P', AL.plate() });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.can_smart, 1), new Object[] { ModItems.can_empty, Items.potionitem, Items.sugar, KNO.dust() });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.can_creature, 1), new Object[] { ModItems.can_empty, Items.potionitem, Items.sugar, ModItems.sulfur });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.can_redbomb, 1), new Object[] { ModItems.can_empty, Items.potionitem, Items.sugar, Items.wheat });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.can_bepis, 1), new Object[] { ModItems.can_empty, Items.potionitem, Items.sugar, Items.coal });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.can_mrsugar, 1), new Object[] { ModItems.can_empty, Items.potionitem, Items.sugar, F.dust() });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.can_overcharge, 1), new Object[] { ModItems.can_empty, Items.potionitem, Items.sugar, S.dust() });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.can_luna, 1), new Object[] { ModItems.can_empty, Items.potionitem, Items.sugar, ModItems.powder_meteorite_tiny });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.mucho_mango, 1), new Object[] { Items.potionitem, Items.sugar, Items.sugar, KEY_ORANGE });

		//Tea you fucking mong
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.teacup), new Object[] { ModItems.tea_leaf, Items.water_bucket, ModItems.teacup_empty}); // Items.sugar

		//Canteens
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.canteen_vodka, 1), new Object[] { "O", "P", 'O', Items.potato, 'P', ModItems.ingot_steel });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.canteen_fab, 1), new Object[] { " M ", "MVM", " M ", 'V', ModItems.canteen_vodka, 'M', Items.sugar });

		//Soda
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.bottle_empty, 6), new Object[] { " G ", "G G", "GGG", 'G', KEY_ANYPANE });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle_nuka, 1), new Object[] { ModItems.bottle_empty, Items.potionitem, Items.sugar, COAL.dust() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle_cherry, 1), new Object[] { ModItems.bottle_empty, Items.potionitem, Items.sugar, REDSTONE.dust() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle_quantum, 1), new Object[] { ModItems.bottle_empty, Items.potionitem, Items.sugar, ModItems.trinitite });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle_sparkle), new Object[] { ModItems.bottle_nuka, Items.carrot, GOLD.nugget() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle_rad), new Object[] { ModItems.bottle_quantum, Items.carrot, GOLD.nugget() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bottle2_empty, 6), new Object[] { " G ", "G G", "G G", 'G', KEY_ANYPANE });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle2_korl, 1), new Object[] { ModItems.bottle2_empty, Items.potionitem, Items.sugar, CU.dust() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle2_fritz, 1), new Object[] { ModItems.bottle2_empty, Items.potionitem, Items.sugar, W.dust() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle2_korl_special, 1), new Object[] { ModItems.bottle2_empty, Items.potionitem, Items.sugar, CU.dust(), ST.dust() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.bottle2_fritz_special, 1), new Object[] { ModItems.bottle2_empty, Items.potionitem, Items.sugar, W.dust(), TH232.dust() });

		//Syringes
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_empty, 6), new Object[] { "P", "C", "B", 'B', Item.getItemFromBlock(Blocks.iron_bars), 'C', ModItems.cell_empty, 'P', IRON.plate() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6), new Object[] { "SSS", "PMP", "SSS", 'S', ModItems.syringe_empty, 'P', Items.pumpkin_seeds, 'M', Items.milk_bucket });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6), new Object[] { "SSS", "PMP", "SSS", 'S', ModItems.syringe_empty, 'P', Items.pumpkin_seeds, 'M', Items.coal });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6), new Object[] { "SPS", "SMS", "SPS", 'S', ModItems.syringe_empty, 'P', Items.pumpkin_seeds, 'M', Items.milk_bucket });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6), new Object[] { "SPS", "SMS", "SPS", 'S', ModItems.syringe_empty, 'P', Items.pumpkin_seeds, 'M', new ItemStack(Items.coal, 1, 1) });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6), new Object[] { "SSS", "PMP", "SSS", 'S', ModItems.syringe_empty, 'P', Items.pumpkin_seeds, 'M', Items.reeds });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6), new Object[] { "SPS", "SMS", "SPS", 'S', ModItems.syringe_empty, 'P', Items.pumpkin_seeds, 'M', Items.reeds });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_poison, 1), new Object[] { "SLS", "LCL", "SLS", 'C', ModItems.syringe_empty, 'S', Items.spider_eye, 'L', PB.dust() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_poison, 1), new Object[] { "SLS", "LCL", "SLS", 'C', ModItems.syringe_empty, 'S', Items.spider_eye, 'L', ModItems.powder_poison });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_awesome, 1), new Object[] { "SPS", "NCN", "SPS", 'C', ModItems.syringe_empty, 'S', S.dust(), 'P', PU239.nugget(), 'N', PU238.nugget() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_awesome, 1), new Object[] { "SNS", "PCP", "SNS", 'C', ModItems.syringe_empty, 'S', S.dust(), 'P', PU239.nugget(), 'N', PU238.nugget() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_empty, 6), new Object[] { "P", "C", "B", 'B', Blocks.iron_bars, 'C', ModItems.rod_empty, 'P', IRON.plate() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_stimpak, 1), new Object[] { " N ", "NSN", " N ", 'N', Items.nether_wart, 'S', ModItems.syringe_metal_empty });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.syringe_metal_stimpak, 1), new Object[] { ModItems.nitra_small, ModItems.nitra_small, ModItems.nitra_small, ModItems.syringe_metal_empty });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_medx, 1), new Object[] { " N ", "NSN", " N ", 'N', Items.quartz, 'S', ModItems.syringe_metal_empty });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_psycho, 1), new Object[] { " N ", "NSN", " N ", 'N', Items.glowstone_dust, 'S', ModItems.syringe_metal_empty });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1), new Object[] { " N ", "PSP", "L L", 'N', ModItems.bottle_nuka, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', Items.leather });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1), new Object[] { " N ", "PSP", "L L", 'N', ModItems.bottle_nuka, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', ANY_RUBBER.ingot() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1), new Object[] { " N ", "PSP", "L L", 'N', ModItems.bottle_cherry, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', Items.leather });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1), new Object[] { " N ", "PSP", "L L", 'N', ModItems.bottle_cherry, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', ANY_RUBBER.ingot() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.syringe_taint), new Object[] { ModItems.bottle2_empty, ModItems.syringe_metal_empty, ModItems.ducttape, ModItems.powder_magic, SA326.nugget(), Items.potionitem });

		//Medicine
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.pill_iodine, 8), new Object[] { "IF", 'I', I.dust(), 'F', F.dust() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.prussian_blue_pill, 3), new Object[] { "IF", 'I', ModItems.prussian_blue_powder, 'F', F.dust() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.plan_c, 1), new Object[] { "PFP", 'P', ModItems.powder_poison, 'F', F.dust() });
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.radx, 4), new Object[] { ModItems.potassium_iodide_powder, F.dust() });

		//unfunny dogshit award
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.fmn, 1), new Object[] { COAL.dust(), PO210.dust(), ST.dust() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.five_htp, 1), new Object[] { COAL.dust(), EUPH.dust(), ModItems.canteen_fab });

		CraftingManager.addShapelessAuto(new ItemStack(ModItems.cigarette, 16), new Object[] { KEY_ANY_TAR, DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.TOBACCO), ModItems.ammonium_nitrate, Items.paper });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.crackpipe, 1), new Object[] { ModItems.catalytic_converter });
		//BYE

		if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleMedicineRecipes) {
			CraftingManager.addShapelessAuto(new ItemStack(ModItems.siox, 8), new Object[] { COAL.dust(), ASBESTOS.dust(), GOLD.nugget() });
			CraftingManager.addShapelessAuto(new ItemStack(ModItems.xanax, 1), new Object[] { COAL.dust(), KNO.dust(), NETHERQUARTZ.dust() });
		} else {
			CraftingManager.addShapelessAuto(new ItemStack(ModItems.siox, 8), new Object[] { Items.wheat_seeds, ModBlocks.mush });
			CraftingManager.addShapelessAuto(new ItemStack(ModItems.xanax, 1), new Object[] { COAL.dust(), KNO.dust(), BR.dust() });
		}

		//Med bags
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1), new Object[] { "LLL", "SIS", "LLL", 'L', Items.leather, 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.syringe_antidote });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1), new Object[] { "LLL", "SIS", "LLL", 'L', Items.leather, 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.pill_iodine });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1), new Object[] { "LL", "SI", "LL", 'L', Items.leather, 'S', ModItems.syringe_metal_super, 'I', ModItems.radaway });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1), new Object[] { "LLL", "SIS", "LLL", 'L', ANY_RUBBER.ingot(), 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.syringe_antidote });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1), new Object[] { "LLL", "SIS", "LLL", 'L', ANY_RUBBER.ingot(), 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.pill_iodine });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1), new Object[] { "LL", "SI", "LL", 'L', ANY_RUBBER.ingot(), 'S', ModItems.syringe_metal_super, 'I', ModItems.radaway });

		//IV Bags
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.iv_empty, 4), new Object[] { "S", "I", "S", 'S', ANY_RUBBER.ingot(), 'I', IRON.plate() });

		//tourniquets
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.tourniquet, 1), new Object[] { "R", "P", "R", 'R', Blocks.wool, 'P', Items.stick });

		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.iv_xp_empty, 1), new Object[] { ModItems.iv_empty, ModItems.powder_magic });

		//ok let's add prussian blue powder taken orally too
		CraftingManager.addShapelessAuto(
			new ItemStack(ModItems.prussian_blue_powder, 1),
			new Object[] {
				IRON.dust(),
				KEY_BLUE,
				ModItems.sulfur,
				Items.coal
			} //Prussian blue is a medication used to treat certain types of heavy metal poisoning, such as thallium or radioactive cesium. It works by binding to the metals and preventing their absorption in the body.
		);


		//Radaway you mean prussian blue idiot
		CraftingManager.addShapelessAuto(
			new ItemStack(ModItems.radaway, 1),
			new Object[] {
				IRON.dust(),
				KEY_BLUE,
				ModItems.sulfur,
				Items.coal,
				ModItems.magnesium_chloride,
				ModItems.iv_empty
			}
		);

		CraftingManager.addShapelessAuto(
			new ItemStack(ModItems.radaway, 1),
			new Object[] {
				ModItems.prussian_blue_powder,
				ModItems.magnesium_chloride,
				ModItems.iv_empty
			} //Prussian blue nanoparticles are sometimes tested via intravenous injection.
			// However, this is strictly experimental and not standard clinical practice.
		);


		CraftingManager.addShapelessAuto(
			new ItemStack(ModItems.radaway_strong, 1),
			new Object[] {
				ModItems.radaway,
				ModItems.iv_blood,
				ModItems.powder_copper,
				ModItems.powder_iodine
			}
		);
		CraftingManager.addShapelessAuto(
			new ItemStack(ModItems.radaway_flush, 1),
			new Object[] {
				ModItems.radaway_strong,
				I.dust(),
				ModItems.syringe_antidote,
				new ItemStack(Items.dye, 1, 15)
			}
		);

		//Cladding
		CraftingManager.addShapelessAuto(new ItemStack(ModItems.cladding_paint, 1), new Object[] { PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget(), Items.clay_ball, Items.glass_bottle });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.cladding_rubber, 1), new Object[] { "RCR", "CDC", "RCR", 'R', ANY_RUBBER.ingot(), 'C', COKE.dust(), 'D', ModItems.ducttape });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.cladding_lead, 1), new Object[] { "DPD", "PRP", "DPD", 'R', ModItems.cladding_rubber, 'P', PB.plate(), 'D', ModItems.ducttape });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.cladding_desh, 1), new Object[] { "DPD", "PRP", "DPD", 'R', ModItems.cladding_lead, 'P', ModItems.plate_desh, 'D', ModItems.ducttape });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.cladding_ghiorsium, 1), new Object[] { "DPD", "PRP", "DPD", 'R', ModItems.cladding_desh, 'P', ModItems.ingot_gh336, 'D', ModItems.ducttape });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.cladding_obsidian, 1), new Object[] { "OOO", "PDP", "OOO", 'O', Blocks.obsidian, 'P', STEEL.plate(), 'D', ModItems.ducttape });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.cladding_iron, 1), new Object[] { "OOO", "PDP", "OOO", 'O', IRON.plate(), 'P', ModItems.plate_polymer, 'D', ModItems.ducttape });

		//Inserts
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_steel, 1), new Object[] { "DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', STEEL.block() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_du, 1), new Object[] { "DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', U238.block() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_ghiorsium, 1), new Object[] { "DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', GH336.ingot(), 'S', U238.ingot() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_cmb, 1), new Object[] { "DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', CMB.ingot(), 'S', U238.ingot() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_polonium, 1), new Object[] { "DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', PO210.block() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_era, 1), new Object[] { "DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', ModItems.ingot_semtex });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_kevlar, 1), new Object[] { "KIK", "IDI", "KIK", 'K', ModItems.plate_kevlar, 'I', ANY_RUBBER.ingot(), 'D', ModItems.ducttape });


		CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_sapi, 1), new Object[] { "PKP", "DXD", "PKP", 'P', ANY_PLASTIC.ingot(), 'K', ModItems.insert_kevlar, 'D', ModItems.ducttape, 'X', ModItems.plate_stainless });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_sapi, 1), new Object[] { "PKP", "DXD", "PKP", 'P', ANY_PLASTIC.ingot(), 'K', ModItems.insert_kevlar, 'D', ModItems.ducttape, 'X', ModItems.plate_welded, Mats.MAT_TITANIUM.id }); //titanium new ItemStack(ModItems.plate_welded, 1, Mats.MAT_TITANIUM.id)

		CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_esapi, 1), new Object[] { "PKP", "DSD", "PKP", 'P', ANY_PLASTIC.ingot(), 'K', ModItems.insert_sapi, 'D', ModItems.ducttape, 'S', ModItems.plate_titanium });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_xsapi, 1), new Object[] { "PKP", "DSD", "PKP", 'P', ModItems.ingot_boron, 'K', ModItems.insert_esapi, 'D', ModItems.ducttape, 'S', ModItems.ingot_silicon }); //boron ingot isn't an ingot. it isnt an ingot IT ISNT AN INGOT IT ISNT AN INGOT DIE DIE DIE DIE DIE DIE
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.insert_yharonite, 1), new Object[] { "YIY", "IYI", "YIY", 'Y', ModItems.billet_yharonite, 'I', ModItems.insert_du });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.australium_iii, 1), new Object[] { "WSW", "PAP", "SPS", 'S', STEEL.plateWelded(), 'P', ANY_PLASTIC.ingot(), 'A', AUSTRALIUM.ingot(), 'W', GOLD.wireDense() });

		//Servos
		//ill leave it in on the aspect that it's theoretically probable
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.servo_set, 1), new Object[] { "MBM", "PBP", "MBM", 'M', ModItems.motor, 'B', STEEL.bolt(), 'P', ModItems.ingot_chainsteel }); //titanium plate
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.servo_set_desh, 1), new Object[] { "MBM", "PSP", "MBM", 'M', ModItems.motor_desh, 'B', DURA.bolt(), 'P', ALLOY.plate(), 'S', ModItems.servo_set });

		//Helmet Mods
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.attachment_mask, 1), new Object[] { "DID", "IGI", " F ", 'D', ModItems.ducttape, 'I', ANY_RUBBER.ingot(), 'G', KEY_ANYPANE, 'F', IRON.plate() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.attachment_mask_mono, 1), new Object[] { " D ", "DID", " F ", 'D', ModItems.ducttape, 'I', ANY_RUBBER.ingot(), 'F', IRON.plate() });

		//Boot Mods
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.pads_rubber, 1), new Object[] { "P P", "IDI", "P P", 'P', ANY_RUBBER.ingot(), 'I', IRON.plate(), 'D', ModItems.ducttape });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.pads_slime, 1), new Object[] { "SPS", "DSD", "SPS", 'S', KEY_SLIME, 'P', ModItems.pads_rubber, 'D', ModItems.ducttape });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.pads_static, 1), new Object[] { "CDC", "ISI", "CDC", 'C', CU.ingot(), 'D', ModItems.ducttape, 'I', ANY_RUBBER.ingot(), 'S', ModItems.pads_slime });

		//Batteries
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.armor_battery, 1), new Object[] { "PCP", "PCP", "PCP", 'P', STEEL.plate(), 'C', ModBlocks.capacitor_gold });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.armor_battery_mk2, 1), new Object[] { "PCP", "PCP", "PCP", 'P', ANY_PLASTIC.ingot(), 'C', ModBlocks.capacitor_niobium });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.armor_battery_mk3, 1), new Object[] { "PCP", "PCP", "PCP", 'P', GOLD.plate(), 'C', ModBlocks.capacitor_tantalium });

		//Special Mods
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.lodestone, 1), new Object[] { "IGI", "GPG", "IGI", 'I', IRON.ingot(), 'G', NI.ingot(), 'P', ModItems.ingot_praseodymium });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.horseshoe_magnet, 1), new Object[] { "L L", "I I", "ILI", 'L', ModItems.lodestone, 'I', IRON.ingot() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.industrial_magnet, 1), new Object[] { "SMS", " B ", "SMS", 'S', STEEL.ingot(), 'M', ModItems.horseshoe_magnet, 'B', ModBlocks.fusion_conductor });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.heart_container, 1), new Object[] { "HAH", "ACA", "HAH", 'H', ModItems.heart_piece, 'A', AL.ingot(), 'C', ModItems.coin_creeper });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.heart_booster, 1), new Object[] { "GHG", "MCM", "GHG", 'G', GOLD.ingot(), 'H', ModItems.heart_container, 'M', ModItems.morning_glory, 'C', ModItems.coin_maskman });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.heart_fab, 1), new Object[] { "GHG", "MCM", "GHG", 'G', PO210.billet(), 'H', ModItems.heart_booster, 'M', ModItems.canteen_fab, 'C', ModItems.coin_worm });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.ink, 1), new Object[] { "FPF", "PIP", "FPF", 'F', new ItemStack(Blocks.red_flower, 1, OreDictionary.WILDCARD_VALUE), 'P', ModItems.armor_polish, 'I', KEY_BLACK });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.bathwater_mk2, 1), new Object[] { "MWM", "WBW", "MWM", 'M', ModItems.bottle_mercury, 'W', ModItems.nuclear_waste, 'B', ModItems.bathwater });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.back_tesla, 1), new Object[] { "DGD", "GTG", "DGD", 'D', ModItems.ducttape, 'G', GOLD.wireFine(), 'T', ModBlocks.tesla });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.medal_liquidator, 1), new Object[] { "GBG", "BFB", "GBG", 'G', AU198.nugget(), 'B', B.ingot(), 'F', ModItems.debris_fuel });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.injector_5htp, 1), new Object[] { ModItems.five_htp, DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), BIGMT.plate() });
		//CraftingManager.addShapelessAuto(new ItemStack(ModItems.injector_knife, 1), new Object[] { ModItems.injector_5htp, Items.iron_sword });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.shackles, 1), new Object[] { "CIC", "C C", "I I", 'I', ModItems.ingot_chainsteel, 'C', ModBlocks.chain });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.black_diamond, 1), new Object[] { "NIN", "IGI", "NIN", 'N', AU198.nugget(), 'I', ModItems.ink, 'G', VOLCANIC.gem() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.protection_charm, 1), new Object[] { " M ", "MDM", " M ", 'M', ModItems.fragment_meteorite, 'D', DIAMOND.gem() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.meteor_charm, 1), new Object[] { " M ", "MDM", " M ", 'M', ModItems.fragment_meteorite, 'D', VOLCANIC.gem() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.neutrino_lens, 1), new Object[] { "PSP", "SCS", "PSP", 'P', ANY_PLASTIC.ingot(), 'S', STAR.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID) });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_tester, 1), new Object[] { "G", "C", "I", 'G', GOLD.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'I', IRON.plate() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_tester, 1), new Object[] { "G", "C", "I", 'G', ModItems.powder_erbium_tiny, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.VACUUM_TUBE), 'I', IRON.plate() });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.defuser_gold, 1), new Object[] { "GPG", "PRP", "GPG", 'G', Items.gunpowder, 'P', GOLD.plate(), 'R', "record" });
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.ballistic_gauntlet, 1), new Object[] { " WS", "WRS", " RS", 'W', CU.wireFine(), 'R', ModItems.ring_starmetal, 'S', STEEL.plate() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.night_vision, 1), "P P", "GCG", 'P', ANY_PLASTIC.ingot(), 'G', KEY_ANYGLASS, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC));

		//Stealth boy
		//CraftingManager.addRecipeAuto(new ItemStack(ModItems.stealth_boy, 1), new Object[] { " B", "LI", "LC", 'B', Blocks.stone_button, 'L', Items.leather, 'I', STEEL.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) });

		//RD40 Filters
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter, 1), new Object[] { "I", "F", 'F', ModItems.filter_coal, 'I', IRON.plate() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_mono, 1), new Object[] { "ZZZ", "ZCZ", "ZZZ", 'Z', ZR.nugget(), 'C', ModItems.catalyst_clay });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_combo, 1), new Object[] { "ZCZ", "CFC", "ZCZ", 'Z', ZR.ingot(), 'C', ModItems.catalyst_clay, 'F', ModItems.gas_mask_filter });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_rag, 1), new Object[] { "I", "F", 'F', ModItems.rag_damp, 'I', IRON.ingot() });
		CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_piss, 1), new Object[] { "I", "F", 'F', ModItems.rag_piss, 'I', IRON.ingot() });
	}
}
