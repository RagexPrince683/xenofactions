package com.hbm.items.food;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ModItems;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class Itemmed_ipecac extends ItemFood {
	public Itemmed_ipecac(int hunger) {
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
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		// Ensure the super method is called
		ItemStack result = super.onEaten(stack, world, player);

		// Call the method to apply food-eaten effects
		onFoodEaten(stack, world, player);
		return result;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		// Add throwing up effect here
		if (!world.isRemote) { // Ensure this logic only runs on the server side
			// Apply the hunger effect (server-side)
			player.addPotionEffect(new PotionEffect(Potion.hunger.id, 50, 49));

			// Adjust hunger level
			int hungerLevel = player.getFoodStats().getFoodLevel();
			player.getFoodStats().addExhaustion(6.0F); // Equivalent to reducing hunger


			// If player's radiation level is low, apply radaway potion effect
			if (hungerLevel > 0 && HbmLivingProps.getRadiation(player) < 600) {
				player.addPotionEffect(new PotionEffect(HbmPotion.radaway.id, 1, 20));
			}

			// Handle Nitan count reset if applicable
			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if (props.nitanCount > 0) {
				player.removePotionEffect(HbmPotion.nitan.id);
				props.nitanCount = 0;
			}

			// Send vomit particle effect to all players around
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("type", "vomit");
			nbt.setString("mode", "normal");
			nbt.setInteger("count", 15);
			nbt.setInteger("entity", player.getEntityId());

			// Ensure packet is properly sent to players around the player
			PacketDispatcher.wrapper.sendToAllAround(
				new AuxParticlePacketNT(nbt, 0, 0, 0),
				new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 25)
			);

			// Play sound for the vomit effect (only on the server)
			world.playSoundAtEntity(player, "hbm:player.vomit", 1.0F, 1.0F);
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		// Begin item usage (on right-click) and start the item use action
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}
}
