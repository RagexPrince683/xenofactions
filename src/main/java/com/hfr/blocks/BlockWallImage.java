package com.hfr.blocks;

import java.util.UUID;

import com.hfr.main.MainRegistry;
import com.hfr.tileentity.TileEntityWallImage;
import com.hfr.wallart.WallArtService;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** One thin, wall-mounted controller block per logical display. */
public class BlockWallImage extends BlockContainer {
    public static BlockWallImage instance; private static final float THICK=1F/16F;
    public BlockWallImage(){super(Material.wood);setBlockName("wall_image_block");setHardness(.5F);instance=this;}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new TileEntityWallImage();}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){int d=MathHelper.floor_double(placer.rotationYaw*4F/360F+.5D)&3;int facing=d==0?3:d==1?4:d==2?2:5;world.setBlockMetadataWithNotify(x,y,z,facing,2);if(!world.isRemote&&placer instanceof EntityPlayer){TileEntity te=world.getTileEntity(x,y,z);if(te instanceof TileEntityWallImage)((TileEntityWallImage)te).initialize(UUID.randomUUID(),((EntityPlayer)placer).getUniqueID(),facing);}}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){TileEntity te=world.getTileEntity(x,y,z);if(!(te instanceof TileEntityWallImage))return false;TileEntityWallImage wall=(TileEntityWallImage)te;if(world.isRemote){MainRegistry.proxy.openWallArtGui(wall);return true;}return true;}
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta){TileEntity te=world.getTileEntity(x,y,z);if(te instanceof TileEntityWallImage)WallArtService.remove((TileEntityWallImage)te);super.breakBlock(world,x,y,z,block,meta);}
    @Override public boolean canPlaceBlockAt(World w,int x,int y,int z){return super.canPlaceBlockAt(w,x,y,z)&&(w.isSideSolid(x-1,y,z,5)||w.isSideSolid(x+1,y,z,4)||w.isSideSolid(x,y,z-1,3)||w.isSideSolid(x,y,z+1,2));}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){switch(w.getBlockMetadata(x,y,z)){case 2:setBlockBounds(0,0,0,1,1,THICK);break;case 3:setBlockBounds(0,0,1-THICK,1,1,1);break;case 4:setBlockBounds(0,0,0,THICK,1,1);break;case 5:setBlockBounds(1-THICK,0,0,1,1,1);break;default:setBlockBounds(0,0,0,1,1,1);}}
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;}
}
