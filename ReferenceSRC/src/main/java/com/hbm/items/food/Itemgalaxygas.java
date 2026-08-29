package com.hbm.items.food;

import com.hbm.items.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class Itemgalaxygas extends ItemFood {

	public Itemgalaxygas(int hunger) {
		super(hunger, false);
		this.setAlwaysEdible();
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		// Prevent default eating or drinking animations and sounds
		//no no we want the animation
		return EnumAction.drink;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			// Custom effects
			double slowdownChance = 0.5; // 50% chance
			if (Math.random() < slowdownChance) {
				player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20 * 20, 1));
			}

			double confusionChance = 0.3; // 30% chance
			if (Math.random() < confusionChance) {
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 20 * 20, 2));
			}

			double blindnessChance = 0.2; // 20% chance
			if (Math.random() < blindnessChance) {
				player.addPotionEffect(new PotionEffect(Potion.blindness.id, 40 * 20, 2));
			}

			double weaknessChance = 0.4; // 40% chance
			if (Math.random() < weaknessChance) {
				player.addPotionEffect(new PotionEffect(Potion.weakness.id, 10 * 20, 2));
			}
		}

		// Reduce stack size if the player is not in Creative mode
		if (!player.capabilities.isCreativeMode) {
			stack.stackSize--;
		}

		// Play custom sound
		player.playSound("hbm:player.gulp", 1.0F, 1.0F);

		return stack.stackSize > 0 ? stack : null;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		// Do not call super.onFoodEaten to avoid triggering default behavior
		// All effects are handled in onEaten
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		if (count % 5 == 0 && count >= 10) {
			player.playSound("hbm:player.gulp", 1F, 1F);
		}

		if (count == 1) {
			this.onEaten(stack, player.worldObj, player);
			player.clearItemInUse();
			player.itemInUseCount = 10;
			player.playSound("hbm:player.groan", 1F, 1F);
			return;
		}

		if (count <= 24 && count % 4 == 0) {
			player.itemInUseCount--;
		}
	}
}
