package com.hbm.inventory;

import java.util.List;
import net.minecraft.item.ItemStack;

public class EmptyAStack extends RecipesCommon.AStack {

	@Override
	public boolean isApplicable(ItemStack stack) {
		return true; // always matches
	}

	@Override
	public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {
		return true; // also always valid
	}

	@Override
	public RecipesCommon.AStack copy() {
		return new EmptyAStack(); // ❗ DO NOT return null
	}

	@Override
	public List<ItemStack> extractForNEI() {
		return null; // no NEI display
	}

	@Override
	public int compareTo(RecipesCommon.AStack o) {
		return -1; // lowest priority, won't override real inputs
	}
}
