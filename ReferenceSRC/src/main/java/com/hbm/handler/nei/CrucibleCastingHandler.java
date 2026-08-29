package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.imc.ICompatNHNEI;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.machine.ItemMold;
import com.hbm.lib.RefStrings;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class CrucibleCastingHandler extends SafeTemplateRecipeHandler implements ICompatNHNEI {

	@Override
	public ItemStack[] getMachinesForRecipe() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.foundry_basin),
				new ItemStack(ModBlocks.foundry_mold),
				new ItemStack(ModBlocks.machine_strand_caster)};
	}
	@Override
	public String getRecipeID() {
		return "ntmCrucibleFoundry";
	}

	public LinkedList<RecipeTransferRect> transferRectsRec = new LinkedList<RecipeTransferRect>();
	public LinkedList<Class<? extends GuiContainer>> guiRec = new LinkedList<Class<? extends GuiContainer>>();

	public class RecipeSet extends TemplateRecipeHandler.CachedRecipe {

		PositionedStack input;
		PositionedStack mold;
		PositionedStack basin;
		PositionedStack output;

		public RecipeSet(ItemStack[] stacks) {
			this.input = NEISafe.positionedStack(NEISafe.copy(stacks != null && stacks.length > 0 ? stacks[0] : null), 48, 24);
			this.mold = NEISafe.positionedStack(NEISafe.copy(stacks != null && stacks.length > 1 ? stacks[1] : null), 75, 6);
			this.basin = NEISafe.positionedStack(NEISafe.copy(stacks != null && stacks.length > 2 ? stacks[2] : null), 75, 42);
			//through reasons i cannot explain, stacks[3]'s stack size does not survive until this point.
			ItemStack o = null;
			if(stacks != null && stacks.length > 1 && NEISafe.isValid(stacks[0]) && NEISafe.isValid(stacks[1]) && ItemMold.moldById.get(stacks[1].getItemDamage()) != null) {
				o = ItemMold.moldById.get(stacks[1].getItemDamage()).getOutput(Mats.matById.get(stacks[0].getItemDamage()));
			}
			this.output = NEISafe.positionedStack(o, 102, 24);
		}

		@Override
		public List<PositionedStack> getIngredients() {
			return NEISafe.getCycledIngredients(this, cycleticks / 20, Arrays.asList(input, mold, basin));
		}

		@Override
		public PositionedStack getResult() {
			return output;
		}

		@Override
		public List<PositionedStack> getOtherStacks() {
			List<PositionedStack> other = new ArrayList();
			other.add(input);
			other.add(mold);
			other.add(basin);
			other.add(output);
			return NEISafe.getCycledIngredients(this, cycleticks / 20, other);
		}
	}

	@Override
	public String getRecipeName() {
		return "Crucible Casting";
	}

	@Override
	public String getGuiTexture() {
		return RefStrings.MODID + ":textures/gui/nei/gui_nei_foundry.png";
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {

		if(outputId.equals("ntmCrucibleFoundry")) {

			for(ItemStack[] recipe : CrucibleRecipes.getMoldRecipes()) {
				this.arecipes.add(new RecipeSet(recipe));
			}
		} else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {

		for(ItemStack[] recipe : CrucibleRecipes.getMoldRecipes()) {
			if(NEIServerUtils.areStacksSameTypeCrafting(recipe[3], result)) {
				this.arecipes.add(new RecipeSet(recipe));
			}
		}
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients) {

		if(inputId.equals("ntmCrucibleFoundry")) {
			loadCraftingRecipes("ntmCrucibleFoundry", new Object[0]);
		} else {
			super.loadUsageRecipes(inputId, ingredients);
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {

		for(ItemStack[] recipe : CrucibleRecipes.getMoldRecipes()) {

			for(int i = 0; i < 3; i++) {
				if(NEIServerUtils.areStacksSameTypeCrafting(recipe[i], ingredient)) {
					this.arecipes.add(new RecipeSet(recipe));
					break;
				}
			}
		}
	}

	@Override
	public void loadTransferRects() {
		transferRects.add(new RecipeTransferRect(new Rectangle(65, 23, 36, 18), "ntmCrucibleFoundry"));
		RecipeTransferRectHandler.registerRectsToGuis(getRecipeTransferRectGuis(), transferRects);
	}
}
