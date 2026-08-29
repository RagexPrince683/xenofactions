package com.hbm.handler.nei;

import java.util.ArrayList;

import codechicken.nei.recipe.TemplateRecipeHandler;

/**
 * TemplateRecipeHandler base which rejects malformed cached recipes before GTNH
 * NEI can build recipe ids/favorites from them.
 */
public abstract class SafeTemplateRecipeHandler extends TemplateRecipeHandler {

	public SafeTemplateRecipeHandler() {
		this.arecipes = new SafeRecipeList(this);
	}

	private static class SafeRecipeList extends ArrayList<TemplateRecipeHandler.CachedRecipe> {
		private static final long serialVersionUID = 1L;
		private final SafeTemplateRecipeHandler handler;

		private SafeRecipeList(SafeTemplateRecipeHandler handler) {
			this.handler = handler;
		}

		@Override
		public boolean add(TemplateRecipeHandler.CachedRecipe recipe) {
			if(NEISafe.isSafeRecipe(handler, recipe, "add")) {
				return super.add(recipe);
			}
			return false;
		}

		@Override
		public void add(int index, TemplateRecipeHandler.CachedRecipe element) {
			if(NEISafe.isSafeRecipe(handler, element, "indexed add")) {
				super.add(index, element);
			}
		}
	}
}
