package com.hbm.main;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Startup-only diagnostics for content declarations.  These messages are deliberately
 * emitted only while registering content: a bad declaration is actionable for a pack
 * author, whereas repeating it during play would only fill the server log.
 */
public final class RegistrationDiagnostics {

	private RegistrationDiagnostics() { }

	public static boolean validateRecipe(ItemStack output, Object[] inputs, String registration) {
		boolean valid = validateStack(output, "output", registration);
		if(inputs == null || inputs.length == 0) {
			log("Malformed recipe " + registration + ": it has no inputs; check the recipe declaration.");
			return false;
		}
		for(int i = 0; i < inputs.length; i++) valid &= validateIngredient(inputs[i], i, registration);
		return valid;
	}

	private static boolean validateIngredient(Object input, int index, String registration) {
		if(input == null) {
			log("Malformed recipe " + registration + ": input " + index + " is null; an item, block, stack, or ore-dictionary key is missing.");
			return false;
		}
		if(input instanceof ItemStack) return validateStack((ItemStack) input, "input " + index, registration);
		if(input instanceof Item && Item.itemRegistry.getNameForObject((Item) input) == null) {
			log("Malformed recipe " + registration + ": input " + index + " references an unregistered item " + input + ".");
			return false;
		}
		if(input instanceof Block && Block.blockRegistry.getNameForObject((Block) input) == null) {
			log("Malformed recipe " + registration + ": input " + index + " references an unregistered block " + input + ".");
			return false;
		}
		return true;
	}

	private static boolean validateStack(ItemStack stack, String role, String registration) {
		if(stack == null || stack.getItem() == null) {
			log("Malformed recipe " + registration + ": " + role + " is a null stack or has no item; check the referenced registry entry.");
			return false;
		}
		if(stack.stackSize <= 0 || stack.getItemDamage() < 0) {
			log("Malformed recipe " + registration + ": " + role + " " + describe(stack) + " has invalid count or metadata; use a positive count and non-negative metadata.");
			return false;
		}
		if(Item.itemRegistry.getNameForObject(stack.getItem()) == null) {
			log("Malformed recipe " + registration + ": " + role + " references unregistered item " + describe(stack) + ".");
			return false;
		}
		return true;
	}

	public static String describe(ItemStack stack) {
		return stack == null ? "<null>" : String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem())) + " x" + stack.stackSize + " meta " + stack.getItemDamage();
	}

	public static void failedRecipe(ItemStack output, Object[] inputs, Throwable error) {
		log("Failed to register recipe for " + describe(output) + " (inputs=" + (inputs == null ? 0 : inputs.length) + "): " + error.getClass().getSimpleName() + ": " + error.getMessage());
	}

	public static void failedRegistryEntry(String kind, Object entry, String name, Throwable error) {
		log("Failed to register " + kind + " '" + name + "' (" + entry + "): " + error.getClass().getSimpleName() + ": " + error.getMessage());
	}

	/** Checks declared registry fields once registration has completed, not during play. */
	public static void validateDeclaredEntries(Class<?> owner, Class<?> type, String kind) {
		for(Field field : owner.getDeclaredFields()) {
			if(!Modifier.isStatic(field.getModifiers()) || !type.isAssignableFrom(field.getType())) continue;
			try {
				Object entry = field.get(null);
				if(entry instanceof Item && Item.itemRegistry.getNameForObject((Item) entry) == null) {
					log("Unregistered " + kind + " " + owner.getSimpleName() + "." + field.getName() + " (" + entry + "); check its registration name and order.");
				} else if(entry instanceof Block && Block.blockRegistry.getNameForObject((Block) entry) == null) {
					log("Unregistered " + kind + " " + owner.getSimpleName() + "." + field.getName() + " (" + entry + "); check its registration name and order.");
				}
			} catch(IllegalAccessException e) {
				log("Could not inspect " + kind + " field " + owner.getSimpleName() + "." + field.getName() + "; check registry diagnostics access.");
			}
		}
	}

	private static void log(String message) {
		MainRegistry.logger.warn("[REGISTRATION] " + message);
	}
}
