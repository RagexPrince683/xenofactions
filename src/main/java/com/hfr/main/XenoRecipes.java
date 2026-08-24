package com.hfr.main;

import com.hfr.blocks.ModBlocks;
import com.hfr.config.XFConfig;
import com.hfr.items.ModItems;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Survival entry points for the established faction, prestige, and support loop. */
public final class XenoRecipes {

	private XenoRecipes() { }

	/** The single boundary for recipes added by the survival-recipes feature. */
	public static void registerSurvivalRecipes() {
		if(!XFConfig.enableSurvivalRecipes)
			return;

		// Founding and war: costly enough to matter, but replaceable after conflict.
		shaped(ModBlocks.clowder_flag, "GEG", "ODO", "IRI", 'G', Items.gold_ingot, 'E', Items.emerald,
				'O', Blocks.obsidian, 'D', Items.diamond, 'I', Items.iron_ingot, 'R', Items.redstone);
		shaped(ModBlocks.clowder_conquerer, "LWL", "GRG", "ISI", 'L', Items.leather, 'W', Blocks.wool,
				'G', Items.gold_ingot, 'R', Items.redstone, 'I', Items.iron_ingot, 'S', Items.stick);

		// Faction utilities.
		shaped(ModBlocks.officer_chest, "I I", "ICI", "IRI", 'I', Items.iron_ingot, 'C', Blocks.chest, 'R', Items.redstone);
		shaped(ModBlocks.med_tent, "WWW", "WRW", "S S", 'W', Blocks.wool, 'R', Items.golden_apple, 'S', Items.stick);
		shaped(ModBlocks.tp_tent, "WWW", "PEP", "SRS", 'W', Blocks.wool, 'P', Items.ender_pearl,
				'E', Items.ender_eye, 'S', Items.stick, 'R', Items.redstone);

		// Developed production/prestige infrastructure; shared components form the progression chain.
		shaped(ModBlocks.machine_coalmine, "SMS", "MCM", "SIS", 'S', ModItems.components_scaffold,
				'M', ModItems.components_mechanical, 'C', Blocks.chest, 'I', Items.minecart);
		shaped(ModBlocks.machine_factory, "SCS", "MEM", "SPS", 'S', ModItems.components_steel,
				'C', Blocks.crafting_table, 'M', ModItems.components_mechanical, 'E', ModItems.components_electronics, 'P', Blocks.piston);
		shaped(ModBlocks.machine_uni, "BEB", "SDS", "BKB", 'B', Blocks.bookshelf, 'E', ModItems.components_electronics,
				'S', Blocks.stonebrick, 'D', Items.diamond, 'K', Blocks.crafting_table);
		shaped(ModBlocks.machine_reserve, "GEG", "SCS", "ODO", 'G', Blocks.gold_block, 'E', ModItems.components_electronics,
				'S', ModItems.components_steel, 'C', Blocks.chest, 'O', Blocks.obsidian, 'D', Items.diamond);
		shaped(ModBlocks.machine_temple, "QGQ", "SBS", "ODO", 'Q', Blocks.quartz_block, 'G', Blocks.gold_block,
				'S', Blocks.stonebrick, 'B', Blocks.bookshelf, 'O', Blocks.obsidian, 'D', Items.diamond);
		shaped(ModBlocks.statue, " Q ", "QEQ", "SGS", 'Q', Blocks.quartz_block, 'E', Items.emerald,
				'S', Blocks.stonebrick, 'G', Blocks.gold_block);
	}

	private static void shaped(Block output, Object... recipe) {
		GameRegistry.addRecipe(new ItemStack(output), recipe);
	}
}
