package com.hbm.items.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

//MAGNOX Nuclear Reactor.
public class ItemZirnoxRod extends ItemEnumMulti {

	public ItemZirnoxRod() {
		super(EnumZirnoxType.class, true, true);
		this.setMaxStackSize(1);
		this.canRepair = false;
	}

	public static void incrementLifeTime(ItemStack stack) {

		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		int time = stack.stackTagCompound.getInteger("life");

		stack.stackTagCompound.setInteger("life", time + 1);
	}

	public static void setLifeTime(ItemStack stack, int time) {

		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.setInteger("life", time);
	}

	public static int getLifeTime(ItemStack stack) {

		if(!stack.hasTagCompound()) {
			stack.stackTagCompound = new NBTTagCompound();
			return 0;
		}

		return stack.stackTagCompound.getInteger("life");
	}

	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	public double getDurabilityForDisplay(ItemStack stack) {
		EnumZirnoxType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		return (double) getLifeTime(stack) / (double) num.maxLife;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {


		EnumZirnoxType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.depletion", ((int)((((double)getLifeTime(stack)) / (double)num.maxLife) * 100000)) / 1000D + "%"));
		String[] loc = I18nUtil.resolveKeyArray("desc.item.zirnox" + (num.breeding ? "BreedingRod" : "Rod"), BobMathUtil.getShortNumber(num.maxLife));

		if(num.breeding)
			loc = I18nUtil.resolveKeyArray("desc.item.zirnoxBreedingRod", BobMathUtil.getShortNumber(num.maxLife));
		else
			loc = I18nUtil.resolveKeyArray("desc.item.zirnoxRod", num.heat, BobMathUtil.getShortNumber(num.maxLife));

		for(String s : loc) {
			list.add(s);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];

		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(this.getIconString() + "_" + num.name().toLowerCase(Locale.US));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		Enum num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		return super.getUnlocalizedName() + "_" + num.name().toLowerCase(Locale.US);
	}

	//MAGNOX reactor.
	public static enum EnumZirnoxType {

		// actual historical Magnox fuel
		NATURAL_URANIUM_FUEL(300_000, 35),
		// slightly enriched uranium
		URANIUM_FUEL(280_000, 45),
		// fertile breeder blanket 1
		TH232(120_000, 0, true),

		// bred U-233 from thorium cycle
		THORIUM_FUEL(240_000, 50),
		// experimental / less ideal fuels in graphite gas reactors
		MOX_FUEL(180_000, 55),
		PLUTONIUM_FUEL(150_000, 58),
		// premium fissile fuels
		U233_FUEL(230_000, 65),
		U235_FUEL(220_000, 60),
		// stupid fucking schrabidium bob bullshit
		LES_FUEL(250_000, 50),
		//fert breeder blanket2
		LITHIUM(80_000, 0, true),
		// Zirconium Fast Breeder fuel MOX
		ZFB_MOX(125_000, 90);

		public final int maxLife;
		public final int heat;
		public final boolean breeding;

		private EnumZirnoxType(int life, int heat, boolean breeding) {
			this.maxLife = life;
			this.heat = heat;
			this.breeding = breeding;
		}

		private EnumZirnoxType(int life, int heat) {
			this.maxLife = life;
			this.heat = heat;
			this.breeding = false;
		}
	}
}
