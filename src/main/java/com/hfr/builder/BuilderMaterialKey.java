package com.hfr.builder;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Stable, inventory-relevant identity shared by requirements, depots and workers. */
public final class BuilderMaterialKey {
    private final String itemName;
    private final int damage;
    private final String tag;

    public BuilderMaterialKey(ItemStack stack) {
        Item item=stack.getItem();
        itemName=String.valueOf(GameData.getItemRegistry().getNameForObject(item));
        damage=stack.getItemDamage();
        tag=stack.hasTagCompound()?stack.getTagCompound().toString():"";
    }
    public boolean matches(ItemStack stack){return stack!=null&&equals(new BuilderMaterialKey(stack));}
    @Override public int hashCode(){int h=31*itemName.hashCode()+damage;return 31*h+tag.hashCode();}
    @Override public boolean equals(Object value){if(!(value instanceof BuilderMaterialKey))return false;BuilderMaterialKey other=(BuilderMaterialKey)value;return damage==other.damage&&itemName.equals(other.itemName)&&tag.equals(other.tag);}
    @Override public String toString(){return itemName+":"+damage;}
}
