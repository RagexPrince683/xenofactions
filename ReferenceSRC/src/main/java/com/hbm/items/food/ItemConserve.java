package com.hbm.items.food;

import java.util.List;
import java.util.Locale;

import com.hbm.entity.effect.EntityVortex;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

//tfw no multiple inheritance
public class ItemConserve extends ItemEnumMulti {

	public ItemConserve() {
		super(EnumFoodType.class, true, true);
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		EnumFoodType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());

		stack.stackSize--;
		player.getFoodStats().addStats(num.foodLevel, num.saturation);
		world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		this.onFoodEaten(stack, world, player);
		return stack;
	}
	//tfw this just fucking breaks anything special and hates god for no reason

	//the fancy enum lambdas and method references and whatever can come later if necessary
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		EnumFoodType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		if (num == EnumFoodType.TUBE) {
		} else {
			player.inventory.addItemStackToInventory(new ItemStack(ModItems.can_key));
		}

		//

		//if(num == EnumFoodType.BHOLE && !world.isRemote) {
		//	EntityVortex vortex = new EntityVortex(world, 0.5F);
    	//	vortex.posX = player.posX;
    	//	vortex.posY = player.posY;
    	//	vortex.posZ = player.posZ;
    	//	world.spawnEntityInWorld(vortex);
//
		//} else if(num == EnumFoodType.RECURSION && world.rand.nextInt(10) > 0) {
//
		//	player.inventory.addItemStackToInventory(stackFromEnum(EnumFoodType.RECURSION));
		//}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.eat;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(player.canEat(false))
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));

		return stack;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		String unloc = this.getUnlocalizedName(itemstack) + ".desc";
		String loc = I18nUtil.resolveKey(unloc);

		if(!unloc.equals(loc)) {
			String[] locs = loc.split("\\$");

			for(String s : locs) {
				list.add(s);
			}
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
		return "item.canned_" + num.name().toLowerCase(Locale.US);
	}

	public static enum EnumFoodType {
		BEEF(8, 0.75F),
		TUNA(4, 0.75F),
		MYSTERY(6, 0.5F),
		PASHTET(4, 0.5F),
		CHEESE(3, 1F),
		MILK(5, 0.25F),
		PIZZA(8, 075F),
		TUBE(2, 0.25F),
		TOMATO(4, 0.5F),
		HOTDOGS(5, 0.75F),
		YOGURT(3, 0.5F),
		CHINESE(6, 0.1F),
		BARK(2, 1F);

		// Define a method to check if an item is astronaut food


		protected int foodLevel;
		protected float saturation;

		private EnumFoodType(int level, float sat) {
			this.foodLevel = level;
			this.saturation = sat;
		}
	}

	//you have been a massive pain in my side since I found out you were an enum
	public static boolean isAstronautFood(ItemStack itemStack) {
		if (itemStack == null || itemStack.getItem() == null) {
			return false;
		}

		// Check if the item is a "canned conserve" and matches the "TUBE" food type
		if (itemStack.getItem() == ModItems.canned_conserve) {
			EnumFoodType foodType = EnumUtil.grabEnumSafely(EnumFoodType.class, itemStack.getItemDamage());
			return foodType == EnumFoodType.TUBE;
		}

		return false;
	}

}
