package com.hbm.items.machine;

import java.io.IOException;
import java.util.List;

import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.inventory.recipes.ChemplantRecipes.ChemRecipe;
import com.hbm.items.ModItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class ItemChemistryIcon extends Item {

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public ItemChemistryIcon() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	public String getItemStackDisplayName(ItemStack stack) {
		
		ChemRecipe recipe = ChemplantRecipes.indexMapping.get(stack.getItemDamage());
		
		String s = ("" + StatCollector.translateToLocal(ModItems.chemistry_template.getUnlocalizedName() + ".name")).trim();
		String s1 = ("" + StatCollector.translateToLocal("chem." + recipe.name)).trim();

		if(s1 != null) {
			s = s + " " + s1;
		}

		return s;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tabs, List list) {
		for(int i = 0; i < ChemplantRecipes.recipes.size(); i++) {
			list.add(new ItemStack(item, 1, ChemplantRecipes.recipes.get(i).getId()));
		}
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.icons = new IIcon[ChemplantRecipes.recipes.size()];

		for(int i = 0; i < icons.length; ++i) {
			String iconName = "chem_icon_" + ChemplantRecipes.recipes.get(i).name;
			try {
				Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("hbm", "textures/items/" + iconName + ".png"));
				this.icons[i] = reg.registerIcon("hbm:" + iconName);
			} catch(IOException ex) {
				// Leave the entry empty so recipes without bespoke art use their product below.
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int i) {
		ChemRecipe rec = ChemplantRecipes.indexMapping.get(i);
		
		if(rec != null) {
			IIcon icon = this.icons[rec.listing % this.icons.length];
			if(icon != null) return icon;

			ItemStack product = getRecipeProduct(rec);
			if(product != null) return product.getIconIndex();
		} else {
			return ModItems.nothing.getIconFromDamage(i);
		}

		return ModItems.nothing.getIconFromDamage(i);
	}

	/**
	 * Returns the stack which should be rendered for a chemistry recipe. Rendering the
	 * product stack itself is important for block outputs: block and item icons live on
	 * different texture atlases, so borrowing a block's IIcon for this item makes the
	 * renderer sample an unrelated part of the item atlas.
	 */
	@SideOnly(Side.CLIENT)
	public ItemStack getDisplayStack(int damage) {
		ChemRecipe recipe = ChemplantRecipes.indexMapping.get(damage);
		if(recipe == null) return new ItemStack(ModItems.nothing, 1, damage);

		IIcon icon = this.icons[recipe.listing % this.icons.length];
		if(icon != null) return new ItemStack(this, 1, damage);

		ItemStack product = getRecipeProduct(recipe);
		return product != null ? product : new ItemStack(ModItems.nothing, 1, damage);
	}

	@SideOnly(Side.CLIENT)
	private ItemStack getRecipeProduct(ChemRecipe recipe) {
		for(ItemStack output : recipe.outputs) {
			if(output != null && output.getItem() != null) return output;
		}

		for(int i = 0; i < recipe.outputFluids.length; i++) {
			if(recipe.outputFluids[i] != null) return ItemFluidIcon.make(recipe.outputFluids[i]);
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		ChemRecipe recipe = ChemplantRecipes.indexMapping.get(stack.getItemDamage());
		if(recipe != null && this.icons != null && this.icons[recipe.listing % this.icons.length] == null) {
			ItemStack product = getRecipeProduct(recipe);
			if(product != null) return product.getItem().getColorFromItemStack(product, renderPass);
		}

		return super.getColorFromItemStack(stack, renderPass);
	}
}
