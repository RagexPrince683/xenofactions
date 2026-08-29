package com.hbm.items.special;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public class ItemFusionShield extends Item {

	public long maxDamage;
	public int maxTemp;

	/*
	 * 1.0 = baseline.
	 * Higher captures more fusion neutron energy as useful blanket heat.
	 */
	public double heatEfficiency;

	/*
	 * 1.0 = baseline.
	 * Higher means better tritium/breeder behavior.
	 */
	public double breedingEfficiency;

	/*
	 * 1.0 = baseline.
	 * Lower means less reactor operating loss.
	 */
	public double powerDrainMultiplier;

	public ItemFusionShield(long maxDamage, int maxTemp) {
		this(maxDamage, maxTemp, 1.0D, 1.0D, 1.0D);
	}

	public ItemFusionShield(
		long maxDamage,
		int maxTemp,
		double heatEfficiency,
		double breedingEfficiency,
		double powerDrainMultiplier
	) {
		this.maxDamage = maxDamage;
		this.maxTemp = maxTemp;
		this.heatEfficiency = heatEfficiency;
		this.breedingEfficiency = breedingEfficiency;
		this.powerDrainMultiplier = powerDrainMultiplier;
	}

	public static long getShieldDamage(ItemStack stack) {

		if(!stack.hasTagCompound()) {
			stack.stackTagCompound = new NBTTagCompound();
			return 0;
		}

		return stack.stackTagCompound.getLong("damage");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		long damage = getShieldDamage(stack);

		if(damage < 0) {
			damage = 0;
		}

		if(damage > maxDamage) {
			damage = maxDamage;
		}

		int percent = (int) ((maxDamage - damage) * 100 / maxDamage);

		list.add("Durability: " + (maxDamage - damage) + "/" + maxDamage + " (" + percent + "%)");
		list.add("Thermal limit: " + EnumChatFormatting.RED + "" + maxTemp + "°C");

		list.add("Heat capture: " + EnumChatFormatting.YELLOW + formatMultiplier(heatEfficiency) + "x");
		list.add("Breeding rate: " + EnumChatFormatting.GREEN + formatMultiplier(breedingEfficiency) + "x");
		list.add("Power drain: " + EnumChatFormatting.AQUA + formatMultiplier(powerDrainMultiplier) + "x");
	}

	public static void setShieldDamage(ItemStack stack, long damage) {

		if(!stack.hasTagCompound()) {
			stack.stackTagCompound = new NBTTagCompound();
		}

		if(damage < 0) {
			damage = 0;
		}

		stack.stackTagCompound.setLong("damage", damage);
	}

	public static void addShieldDamage(ItemStack stack, long damage) {

		if(stack == null) {
			return;
		}

		setShieldDamage(stack, getShieldDamage(stack) + damage);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {

		if(maxDamage <= 0) {
			return 1.0D;
		}

		double display = (double) getShieldDamage(stack) / (double) maxDamage;

		if(display < 0.0D) {
			return 0.0D;
		}

		if(display > 1.0D) {
			return 1.0D;
		}

		return display;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0.0D;
	}

	private static String formatMultiplier(double value) {
		return String.format("%.2f", value);
	}
}
