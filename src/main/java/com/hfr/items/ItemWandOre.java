package com.hfr.items;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemWandOre extends Item {

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.getBlock(x, y, z) == Blocks.redstone_block) {
            world.playSoundAtEntity(player, "hfr:item.toggle", 0.25F, 0.5F);
            return true;
        }

        // If no position is stored, set position 1
        if (!stack.hasTagCompound()) {
            stack.stackTagCompound = new NBTTagCompound();
            stack.stackTagCompound.setInteger("xCoord", x);
            stack.stackTagCompound.setInteger("zCoord", z);

            world.playSoundAtEntity(player, "hfr:item.techBoop", 1.0F, 1.0F);
        } else {
            // If position 1 is already set, define area and generate ore veins
            if (!world.isRemote) {
                int x1 = stack.stackTagCompound.getInteger("xCoord");
                int z1 = stack.stackTagCompound.getInteger("zCoord");

                int xStart = Math.min(x1, x);
                int xEnd = Math.max(x1, x);
                int zStart = Math.min(z1, z);
                int zEnd = Math.max(z1, z);

                generateOreVeins(world, xStart, xEnd, zStart, zEnd, this);
            }

            world.playSoundAtEntity(player, "hfr:item.techBleep", 1.0F, 1.0F);
            stack.stackTagCompound = null; // Reset the stored position
        }

        return true;
    }

    private void generateOreVeins(World world, int xStart, int xEnd, int zStart, int zEnd, Item wand) {
        Random random = new Random();
        for (int i = xStart; i <= xEnd; i++) {
            for (int j = zStart; j <= zEnd; j++) {
                if (random.nextFloat() < 0.4) { // 40% chance per block to spawn a vein
                    int veinSize = 3 + random.nextInt(4); // Veins of size 3-6
                    for (int k = 0; k < veinSize; k++) {
                        int xOffset = i + random.nextInt(3) - 1;
                        int yOffset = 5 + random.nextInt(55); // Veins between Y=5 and Y=60
                        int zOffset = j + random.nextInt(3) - 1;

                        if (world.getBlock(xOffset, yOffset, zOffset) == Blocks.stone) {
                            if (wand == ModItems.wand_iron) {
                                world.setBlock(xOffset, yOffset, zOffset, Blocks.iron_ore);
                            } else if (wand == ModItems.wand_coal) {
                                world.setBlock(xOffset, yOffset, zOffset, Blocks.coal_ore);
                            }
                           // else if (wand == ModItems.wand_custom) {
                           //     world.setBlock(xOffset, yOffset, zOffset, Blocks.);
                           // }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
        if (itemstack.stackTagCompound != null) {
            list.add("POS:");
            list.add("X: " + itemstack.stackTagCompound.getInteger("xCoord"));
            list.add("Z: " + itemstack.stackTagCompound.getInteger("zCoord"));
        } else {
            list.add("Please select a starting position.");
        }
    }
}