package com.hbm.items.weapon;

import com.hbm.entity.grenade.EntityGrenadeBouncyGeneric;
import com.hbm.entity.grenade.EntityGrenadeImpactGeneric;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemGenericGrenade extends ItemGrenade {

	public ItemGenericGrenade(int fuse) {
		super(fuse);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft) {
		int charge = this.getMaxItemUseDuration(stack) - timeLeft;
		float power = charge / MAX_DRAW_TIME;
		power = (power * power + power * 2.0F) / 3.0F;

		if (power < MIN_DRAW_POWER) {
			return;
		}

		power = Math.min(power, 1.0F);

		if (!player.capabilities.isCreativeMode) {
			--stack.stackSize;
		}

		world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (!world.isRemote) {
			if (fuse == -1) {
				spawnGrenade(world, new EntityGrenadeImpactGeneric(world, player).setType(this), power);
			} else {
				spawnGrenade(world, new EntityGrenadeBouncyGeneric(world, player).setType(this), power);
			}
		}
	}

	public void explode(Entity grenade, EntityLivingBase thrower, World world, double x, double y, double z) { }

	public int getMaxTimer() {
		return this.fuse * 20;
	}

	public double getBounceMod() {
		return 0.5D;
	}
}
