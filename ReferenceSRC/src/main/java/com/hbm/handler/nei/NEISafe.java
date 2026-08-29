package com.hbm.handler.nei;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.main.MainRegistry;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.ItemStack;

/** Shared null-safety helpers for HBM NEI recipe handlers. */
public final class NEISafe {

	private static final Set<String> LOGGED = new HashSet<String>();

	private NEISafe() { }

	public static boolean isValid(ItemStack stack) {
		return stack != null && stack.getItem() != null && stack.stackSize > 0;
	}

	public static ItemStack copy(ItemStack stack) {
		return isValid(stack) ? stack.copy() : null;
	}

	public static ItemStack[] copyValid(ItemStack[] stacks) {
		if(stacks == null || stacks.length == 0) return null;
		List<ItemStack> valid = new ArrayList<ItemStack>();
		for(ItemStack stack : stacks) {
			ItemStack copy = copy(stack);
			if(copy != null) valid.add(copy);
		}
		return valid.isEmpty() ? null : valid.toArray(new ItemStack[valid.size()]);
	}

	public static List<ItemStack> copyValid(Collection<ItemStack> stacks) {
		if(stacks == null || stacks.isEmpty()) return Collections.emptyList();
		List<ItemStack> valid = new ArrayList<ItemStack>();
		for(ItemStack stack : stacks) {
			ItemStack copy = copy(stack);
			if(copy != null) valid.add(copy);
		}
		return valid;
	}

	public static ItemStack[][] copyValid2D(ItemStack[][] stacks) {
		if(stacks == null || stacks.length == 0) return null;
		List<ItemStack[]> valid = new ArrayList<ItemStack[]>();
		for(ItemStack[] stack : stacks) {
			ItemStack[] copy = copyValid(stack);
			if(copy != null) valid.add(copy);
		}
		return valid.isEmpty() ? null : valid.toArray(new ItemStack[valid.size()][]);
	}

	public static PositionedStack positionedStack(Object object, int x, int y) {
		return positionedStack(object, x, y, true);
	}

	public static PositionedStack positionedStack(Object object, int x, int y, boolean genPermutations) {
		Object safe = safeIngredientObject(object);
		if(safe == null) return null;
		try {
			PositionedStack stack = new PositionedStack(safe, x, y, genPermutations);
			return isValid(stack) ? stack : null;
		} catch(Throwable t) {
			logSkip("PositionedStack", "Failed to create positioned stack from " + describe(object), t);
			return null;
		}
	}

	private static Object safeIngredientObject(Object object) {
		if(object instanceof ItemStack) return copy((ItemStack)object);
		if(object instanceof ItemStack[]) return copyValid((ItemStack[])object);
		if(object instanceof Collection) return copyValid((Collection<ItemStack>)object);
		return object;
	}

	public static List<PositionedStack> positionedList(PositionedStack... stacks) {
		List<PositionedStack> list = new ArrayList<PositionedStack>();
		if(stacks == null) return list;
		for(PositionedStack stack : stacks) if(isValid(stack)) list.add(stack);
		return list;
	}

	public static List<PositionedStack> cleanPositionedList(Collection<PositionedStack> stacks) {
		List<PositionedStack> list = new ArrayList<PositionedStack>();
		if(stacks == null) return list;
		for(PositionedStack stack : stacks) if(isValid(stack)) list.add(stack);
		return list;
	}

	public static PositionedStack firstValid(Collection<PositionedStack> stacks) {
		if(stacks == null) return null;
		for(PositionedStack stack : stacks) if(isValid(stack)) return stack;
		return null;
	}

	public static List<PositionedStack> getCycledIngredients(TemplateRecipeHandler.CachedRecipe recipe, int cycle, Collection<PositionedStack> stacks) {
		List<PositionedStack> list = cleanPositionedList(stacks);
		for(PositionedStack stack : list) setPermutation(stack, cycle);
		return list;
	}

	public static boolean isValid(PositionedStack stack) {
		if(stack == null) return false;
		ItemStack[] items = getItems(stack);
		if(items == null || items.length == 0) return false;
		List<ItemStack> valid = new ArrayList<ItemStack>();
		for(ItemStack item : items) {
			ItemStack copy = copy(item);
			if(copy != null) valid.add(copy);
		}
		if(valid.isEmpty()) return false;
		setItems(stack, valid.toArray(new ItemStack[valid.size()]));
		return true;
	}

	public static boolean isSafeRecipe(TemplateRecipeHandler handler, TemplateRecipeHandler.CachedRecipe recipe, String source) {
		if(recipe == null) {
			logSkip(handler, source, "null cached recipe", null);
			return false;
		}
		try {
			PositionedStack result = recipe.getResult();
			if(!isValid(result)) {
				logSkip(handler, source, "missing or invalid main output in " + recipe.getClass().getName(), null);
				return false;
			}
			List<PositionedStack> ingredients = cleanPositionedList(recipe.getIngredients());
			if(ingredients.isEmpty()) {
				PositionedStack ingredient = recipe.getIngredient();
				if(isValid(ingredient)) ingredients.add(ingredient);
			}
			if(ingredients.isEmpty()) {
				logSkip(handler, source, "missing or invalid required input in " + recipe.getClass().getName(), null);
				return false;
			}
			recipe.getOtherStacks(); // optional stacks are sanitized by handler return helpers where present
			return true;
		} catch(Throwable t) {
			logSkip(handler, source, "malformed cached recipe " + recipe.getClass().getName(), t);
			return false;
		}
	}

	public static ItemStack firstStack(PositionedStack stack) {
		if(!isValid(stack)) return null;
		ItemStack[] items = getItems(stack);
		return items != null && items.length > 0 ? copy(items[0]) : null;
	}

	/** Development helper: validates currently cached NEI recipes without rendering them. */
	public static void validateCachedRecipes(TemplateRecipeHandler handler) {
		if(handler == null || handler.arecipes == null) return;
		for(Object recipe : new ArrayList<Object>(handler.arecipes)) {
			if(recipe instanceof TemplateRecipeHandler.CachedRecipe) {
				isSafeRecipe(handler, (TemplateRecipeHandler.CachedRecipe) recipe, "dev validation");
			} else {
				logSkip(handler, "dev validation", "non-cached recipe entry " + describe(recipe), null);
			}
		}
	}

	private static ItemStack[] getItems(PositionedStack stack) {
		try {
			Field field = PositionedStack.class.getField("items");
			return (ItemStack[]) field.get(stack);
		} catch(Throwable ignored) { }
		try {
			Field field = PositionedStack.class.getDeclaredField("items");
			field.setAccessible(true);
			return (ItemStack[]) field.get(stack);
		} catch(Throwable ignored) { }
		try {
			Field field = PositionedStack.class.getField("item");
			Object item = field.get(stack);
			if(item instanceof ItemStack) return new ItemStack[] {(ItemStack)item};
		} catch(Throwable ignored) { }
		return null;
	}

	private static void setItems(PositionedStack stack, ItemStack[] items) {
		try {
			Field field = PositionedStack.class.getField("items");
			field.set(stack, items);
		} catch(Throwable ignored) { }
	}

	private static void setPermutation(PositionedStack stack, int cycle) {
		try {
			PositionedStack.class.getMethod("setPermutationToRender", int.class).invoke(stack, cycle);
		} catch(Throwable ignored) { }
	}

	private static void logSkip(TemplateRecipeHandler handler, String source, String reason, Throwable t) {
		String name = handler == null ? "unknown" : handler.getClass().getSimpleName() + "/" + handler.getRecipeName();
		logSkip(name, source + ": " + reason, t);
	}

	private static void logSkip(String name, String reason, Throwable t) {
		String key = name + "|" + reason;
		if(!LOGGED.add(key)) return;
		if(t == null) {
			MainRegistry.logger.warn("[NEI safety] Skipped broken recipe in " + name + ": " + reason);
		} else {
			MainRegistry.logger.warn("[NEI safety] Skipped broken recipe in " + name + ": " + reason + " (" + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
		}
	}

	private static String describe(Object object) {
		return object == null ? "null" : object.getClass().getName();
	}
}
