package com.hfr.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMultitool extends Item {

    @Override
    public float getDigSpeed(ItemStack stack, Block block, int meta)
    {
    	try {
            return block.getBlockHardness(null, 0, 0, 0) * 5F;
    	} catch(NullPointerException ex) { }
    	
        return 5F;
    }
    
    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass)
    {
        return 100;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean isFull3D()
    {
        return true;
    }

}
