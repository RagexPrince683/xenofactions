package com.hfr.util;

import com.hfr.blocks.BlockWallImage;
import com.hfr.tileentity.TileEntityWallImage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;

//depricated

public class ImagePlacerUtil {
    // Raytrace from player and place block on hit face
    public static boolean placeWallImageAtLook(EntityPlayer player, String url, String ownerUUID, String imageName) {
        World world = player.worldObj;
        MovingObjectPosition mop = rayTrace(player, 5.0D);
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) return false;

        int x = mop.blockX, y = mop.blockY, z = mop.blockZ;
        int side = mop.sideHit; // 0..5; we'll place adjacent to that side
        int tx = x, ty = y, tz = z;
        switch (side) {
            case 0: ty--; break;
            case 1: ty++; break;
            case 2: tz--; break;
            case 3: tz++; break;
            case 4: tx--; break;
            case 5: tx++; break;
        }
        // place only if empty
        if (!world.isAirBlock(tx, ty, tz)) return false;

        if (!world.setBlock(tx, ty, tz, BlockWallImage.instance)) return false;
        TileEntity te = world.getTileEntity(tx, ty, tz);
        if (te instanceof TileEntityWallImage) {
            TileEntityWallImage tie = (TileEntityWallImage) te;
            tie.ownerUUID = ownerUUID;
            tie.imageName = imageName;
            tie.imageURL = url;
            world.setBlockMetadataWithNotify(tx, ty, tz, sideToMetadata(side), 2);
            tie.markDirty();
            world.markBlockForUpdate(tx, ty, tz);
            return true;
        }
        return false;
    }

    private static int sideToMetadata(int side) {
        // map vanilla sides to our 2..5 facings
        if (side == 2) return 3; // north hit -> place on north? adjust to taste
        if (side == 3) return 2;
        if (side == 4) return 5;
        if (side == 5) return 4;
        return 2;
    }

    // simple rayTrace using player's look vector
    public static MovingObjectPosition rayTrace(EntityPlayer player, double reach) {
        Vec3 eye = player.getPosition(1.0F).addVector(0, player.getEyeHeight(), 0);
        Vec3 look = player.getLook(1.0F);
        Vec3 end = eye.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        return player.worldObj.rayTraceBlocks(eye, end, true);
    }
}
