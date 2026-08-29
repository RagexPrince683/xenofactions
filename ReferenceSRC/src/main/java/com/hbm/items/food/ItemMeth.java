package com.hbm.items.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemMeth extends ItemFood {

	public ItemMeth(int hunger) {
		super(hunger, false);
		this.setAlwaysEdible();
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 60 * 20, 3));
			//give off the appearance of being on meth by adjusting the FOV and adding a slight blur effect

			//also schedule a task to apply slowness after a long time to simulate withdrawal... and if you take it again you lose with drawal. oh yeah and add overdosing lol

			NBTTagCompound data = player.getEntityData();

			long time = player.worldObj.getTotalWorldTime();

			data.setLong("MethLastUse", time);

			int dose = data.getInteger("MethDose");
			data.setInteger("MethDose", dose + 1);


		}

		// run on BOTH sides so client sees it
		NBTTagCompound data = player.getEntityData();
		data.setBoolean("OnMeth", true);
		data.setInteger("MethVisualTicks", 20 * 60);

		if (!player.capabilities.isCreativeMode) {
			stack.stackSize--;
		}

		return stack.stackSize > 0 ? stack : null;

	}

}
