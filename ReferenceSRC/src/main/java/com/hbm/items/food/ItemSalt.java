package com.hbm.items.food;

import com.hbm.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class ItemSalt extends ItemFood {

	//just kinda like hurts you 1 heart if eaten plainly to represent the fact that it's not really meant to be eaten on its own, but can be used in recipes and such

	public ItemSalt(int hunger) {
		super(hunger, false);
		this.setAlwaysEdible();
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			if (stack.getItem() == ModItems.cesium_salt) {
				//poison
				player.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.poison.id, 200, 0));
			} else {
				//you shouldn't eat salt
				player.attackEntityFrom(DamageSource.generic, 2.0F);
			}
		}

		if (!player.capabilities.isCreativeMode) {
			stack.stackSize--;
		}

		return stack.stackSize > 0 ? stack : null;
	}

}
