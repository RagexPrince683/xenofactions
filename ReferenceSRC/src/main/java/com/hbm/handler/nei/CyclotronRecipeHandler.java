package com.hbm.handler.nei;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.imc.ICompatNHNEI;
import com.hbm.inventory.gui.GUIMachineCyclotron;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.lib.RefStrings;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class CyclotronRecipeHandler extends SafeTemplateRecipeHandler implements ICompatNHNEI {
	@Override
	public ItemStack[] getMachinesForRecipe() {
		return new ItemStack[]{
				new ItemStack(ModBlocks.machine_cyclotron)};
	}

	@Override
	public String getRecipeID() {
		return "cyclotronProcessing";
	}
    public LinkedList<RecipeTransferRect> transferRectsRec = new LinkedList<RecipeTransferRect>();
    public LinkedList<RecipeTransferRect> transferRectsGui = new LinkedList<RecipeTransferRect>();
    public LinkedList<Class<? extends GuiContainer>> guiRec = new LinkedList<Class<? extends GuiContainer>>();
    public LinkedList<Class<? extends GuiContainer>> guiGui = new LinkedList<Class<? extends GuiContainer>>();

    public class SmeltingSet extends TemplateRecipeHandler.CachedRecipe
    {
	PositionedStack input1;
		PositionedStack input2;
        PositionedStack result;

        public SmeltingSet(ItemStack input1, ItemStack input2, ItemStack result) {
	input1 = NEISafe.copy(input1);
	input2 = NEISafe.copy(input2);
	if(input1 != null) input1.stackSize = 1;
	if(input2 != null) input2.stackSize = 1;
            this.input1 = NEISafe.positionedStack(input1, 66 - 45, 6 + 18);
            this.input2 = NEISafe.positionedStack(input2, 66 + 9, 42 - 18);
            this.result = NEISafe.positionedStack(result, 129, 24);
        }

        @Override
		public List<PositionedStack> getIngredients() {
            return NEISafe.getCycledIngredients(this, cycleticks / 48, Arrays.asList(new PositionedStack[] {input1, input2}));
        }

        @Override
		public PositionedStack getResult() {
            return result;
        }
    }

	@Override
	public String getRecipeName() {
		return "Cyclotron";
	}

	@Override
	public String getGuiTexture() {
		return RefStrings.MODID + ":textures/gui/nei/gui_nei_cyclotron.png";
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if ((outputId.equals("cyclotronProcessing")) && getClass() == CyclotronRecipeHandler.class) {
			Map<Object[], Object> recipes = CyclotronRecipes.getRecipes();
			for (Map.Entry<Object[], Object> recipe : recipes.entrySet()) {
				this.arecipes.add(new SmeltingSet((ItemStack)recipe.getKey()[0], (ItemStack)recipe.getKey()[1], (ItemStack)recipe.getValue()));
			}
		} else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {
		Map<Object[], Object> recipes = CyclotronRecipes.getRecipes();
		for (Map.Entry<Object[], Object> recipe : recipes.entrySet()) {
			if (NEIServerUtils.areStacksSameType((ItemStack)recipe.getValue(), result))
				this.arecipes.add(new SmeltingSet((ItemStack)recipe.getKey()[0], (ItemStack)recipe.getKey()[1], (ItemStack)recipe.getValue()));
		}
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients) {
		if ((inputId.equals("cyclotronProcessing")) && getClass() == CyclotronRecipeHandler.class) {
			loadCraftingRecipes("cyclotronProcessing", new Object[0]);
		} else {
			super.loadUsageRecipes(inputId, ingredients);
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		if (ingredient == null) return;

		Map<Object[], Object> recipes = CyclotronRecipes.getRecipes();

		for (Map.Entry<Object[], Object> entry : recipes.entrySet()) {

			Object[] key = entry.getKey();
			if (key == null || key.length < 2) continue;

			ItemStack particle = (ItemStack) key[0];
			ItemStack input = (ItemStack) key[1];
			ItemStack output = (ItemStack) entry.getValue();

			if (particle == null || input == null || output == null) continue;

			if (NEIServerUtils.areStacksSameType(ingredient, particle) ||
				NEIServerUtils.areStacksSameType(ingredient, input)) {

				this.arecipes.add(new SmeltingSet(particle, input, output));
			}
		}
	}

    @Override
    public Class<? extends GuiContainer> getGuiClass() {
        //return GUITestDiFurnace.class;
	return null;
    }

    @Override
    public void loadTransferRects() {
        transferRectsGui = new LinkedList<RecipeTransferRect>();
        guiGui = new LinkedList<Class<? extends GuiContainer>>();

        transferRects.add(new RecipeTransferRect(new Rectangle(83 - 3 + 16 - 52, 5 + 18 + 1, 24, 18), "cyclotronProcessing"));
        transferRectsGui.add(new RecipeTransferRect(new Rectangle(48 - 5, 27 - 11, 34, 34), "cyclotronProcessing"));
        guiGui.add(GUIMachineCyclotron.class);
        RecipeTransferRectHandler.registerRectsToGuis(getRecipeTransferRectGuis(), transferRects);
        RecipeTransferRectHandler.registerRectsToGuis(guiGui, transferRectsGui);
    }

    @Override
    public void drawExtras(int recipe) {

        drawProgressBar(83 - 3 + 16 - 52, 5 + 18 + 1, 100, 118 + 1, 24, 16, 48, 0);
    }

    @Override
    public TemplateRecipeHandler newInstance() {
        return super.newInstance();
    }
}
