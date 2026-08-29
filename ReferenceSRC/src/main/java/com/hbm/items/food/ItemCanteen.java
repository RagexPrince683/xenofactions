package com.hbm.items.food;

import java.util.List;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemCanteen extends Item {

	public ItemCanteen() {
		// Constructor does not set any cooldown-related attributes.
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			if (this == ModItems.canteen_vodka) {
				player.heal(2F);
				int intoxicationLevel = VersatileConfig.getIntoxicationLevel(player);

				// Effects scale with intoxication level
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 40 * 20 + intoxicationLevel * 20, 1));
				player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 40 * 20 + intoxicationLevel * 10, 1));
				player.addPotionEffect(new PotionEffect(Potion.hunger.id, 5 * 10, 0));

				VersatileConfig.increaseIntoxication(player, 1); // Track intoxication level
			}
			if (this == ModItems.canteen_fab) {
				player.heal(2F);
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 120 * 20, 3));
				player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20 * 20, 1));
				player.addPotionEffect(new PotionEffect(Potion.hunger.id, 5 * 10, 0));
			}

			// Optional: Apply potion sickness if you still want this mechanic
			VersatileConfig.applyPotionSickness(player, 5);

			// Handle stack reduction
			stack.setItemDamage(stack.getItemDamage() + 1);
			if (stack.getItemDamage() >= stack.getMaxDamage()) {
				stack.stackSize--; // Bottle becomes empty
			}
		}

		return stack;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		// Allow drinking unconditionally
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 10; // Duration of the drinking action
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.drink; // Drinking animation
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		if (this == ModItems.canteen_vodka) {
			list.add("Coping with pain using shitty substances.");
			list.add("May help you cope.");
			list.add("");
			if (MainRegistry.polaroidID == 11) {
				list.add("Well, I'm not gonna stop you, it's *your* drinking problem.");
			} else {
				list.add("In Soviet Russia, vodka drinks you. Haha guys, please upvote my post!");
			}
		}
		if (this == ModItems.canteen_fab) {
			list.add("Fun fact for all you gigachads out there, alcohol has estrogenic properties.");
		}
	}
}
