package com.hfr.items;

import com.hfr.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import static com.hfr.command.CommandClowderAdmin.WARENABLED;

public class ItemBlockConqueror extends ItemBlock {

    public ItemBlockConqueror(Block block) {
        super(block);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack heldStack, World world, EntityPlayer player) {
        if (WARENABLED) {
            // Raytrace to find where the player is aiming
            MovingObjectPosition target = getMovingObjectPositionFromPlayer(world, player, true);

            // If no target was hit, do nothing
            if (target == null) {
                return heldStack;
            }

            // Check if the player targeted a block
            if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int x = target.blockX;
                int y = target.blockY;
                int z = target.blockZ;

                // Prevent placement if the block can't be mined
                if (!world.canMineBlock(player, x, y, z)) {
                    return heldStack;
                }

                // Prevent placement if the player can't edit the block
                if (!player.canPlayerEdit(x, y, z, target.sideHit, heldStack)) {
                    return heldStack;
                }

                // Special placement case: place conqueror block on still water with air above
                boolean isStillWater = world.getBlock(x, y, z).getMaterial() == Material.water
                        && world.getBlockMetadata(x, y, z) == 0;
                boolean isAirAbove = world.isAirBlock(x, y + 1, z);

                if (isStillWater && isAirAbove) {
                    BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, x, y + 1, z);
                    world.setBlock(x, y + 1, z, ModBlocks.clowder_conquerer);

                    // Fire block place event — cancel if blocked by plugins/mods
                    if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, ForgeDirection.UP).isCanceled()) {
                        snapshot.restore(true, false);
                        return heldStack;
                    }

                    // Reduce item stack if not in creative mode
                    if (!player.capabilities.isCreativeMode) {
                        heldStack.stackSize--;
                    }
                }
            }

        }return heldStack;
    }

}
