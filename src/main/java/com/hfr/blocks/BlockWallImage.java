package com.hfr.blocks;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.wallart.WallArtOpenGuiPacket;
import com.hfr.tileentity.TileEntityWallImage;
import com.hfr.wallart.WallArtConstants;
import com.hfr.wallart.WallArtService;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** One thin, wall-mounted controller block per logical display. */
public class BlockWallImage extends BlockContainer {
  public static BlockWallImage instance;
  private static final float THICK = 1F / 16F;
  public BlockWallImage() {
    super(Material.wood);
    setBlockName("wall_image_block");
    setHardness(.5F);
    instance = this;
  }
  @Override
  public TileEntity createNewTileEntity(World world, int meta) {
    return new TileEntityWallImage();
  }
  @Override
  public int onBlockPlaced(World world, int x, int y, int z, int clickedSide,
                           float hitX, float hitY, float hitZ, int metadata) {
    return facingFromPlacedSide(clickedSide);
  }
  @Override
  public void onBlockPlacedBy(World world, int x, int y, int z,
                              EntityLivingBase placer, ItemStack stack) {
    int facing = world.getBlockMetadata(x, y, z);
    if (!WallArtConstants.validFacing(facing))
      return;
    if (!world.isRemote && placer instanceof EntityPlayer) {
      TileEntity te = world.getTileEntity(x, y, z);
      if (te instanceof TileEntityWallImage) {
        ((TileEntityWallImage)te)
            .initialize(UUID.randomUUID(), ((EntityPlayer)placer).getUniqueID(),
                        facing);
        world.markBlockForUpdate(x, y, z);
      }
    }
  }
  @Override
  public boolean onBlockActivated(World world, int x, int y, int z,
                                  EntityPlayer player, int side, float hx,
                                  float hy, float hz) {
    TileEntity te = world.getTileEntity(x, y, z);
    if (!(te instanceof TileEntityWallImage))
      return false;
    TileEntityWallImage wall = (TileEntityWallImage)te;
    if (world.isRemote)
      return true;
    UUID owner = wall.getOwnerId();
    if (owner != null && owner.equals(player.getUniqueID()) &&
        player instanceof EntityPlayerMP) {
      PacketDispatcher.wrapper.sendTo(
          new WallArtOpenGuiPacket(world.provider.dimensionId, x, y, z),
          (EntityPlayerMP)player);
    } else {
      player.addChatMessage(
          new ChatComponentTranslation("wallart.error.not_owner"));
    }
    return true;
  }
  @Override
  public void breakBlock(World world, int x, int y, int z, Block block,
                         int meta) {
    TileEntity te = world.getTileEntity(x, y, z);
    if (te instanceof TileEntityWallImage)
      WallArtService.remove((TileEntityWallImage)te);
    super.breakBlock(world, x, y, z, block, meta);
  }
  @Override
  public boolean canPlaceBlockAt(World world, int x, int y, int z) {
    return super.canPlaceBlockAt(world, x, y, z) &&
        (world.isSideSolid(x - 1, y, z, ForgeDirection.EAST) ||
         world.isSideSolid(x + 1, y, z, ForgeDirection.WEST) ||
         world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH) ||
         world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH));
  }
  @Override
  public boolean canPlaceBlockOnSide(World world, int x, int y, int z,
                                     int clickedSide) {
    int facing = facingFromPlacedSide(clickedSide);
    return WallArtConstants.validFacing(facing) &&
        super.canPlaceBlockAt(world, x, y, z) &&
        hasSupportForFacing(world, x, y, z, facing);
  }
  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z,
                                    Block neighbor) {
    if (!world.isRemote && !canBlockStay(world, x, y, z)) {
      dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
      world.setBlockToAir(x, y, z);
    }
  }
  public boolean canBlockStay(World world, int x, int y, int z) {
    return hasSupportForFacing(world, x, y, z,
                               world.getBlockMetadata(x, y, z));
  }
  private static int facingFromPlacedSide(int clickedSide) {
    switch (clickedSide) {
    case 2:
      return 3;
    case 3:
      return 2;
    case 4:
      return 5;
    case 5:
      return 4;
    default:
      return 0;
    }
  }
  private static boolean hasSupportForFacing(World world, int x, int y, int z,
                                             int facing) {
    switch (facing) {
    case 2:
      return world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH);
    case 3:
      return world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH);
    case 4:
      return world.isSideSolid(x - 1, y, z, ForgeDirection.EAST);
    case 5:
      return world.isSideSolid(x + 1, y, z, ForgeDirection.WEST);
    default:
      return false;
    }
  }
  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess w, int x, int y, int z) {
    switch (w.getBlockMetadata(x, y, z)) {
    case 2:
      setBlockBounds(0, 0, 0, 1, 1, THICK);
      break;
    case 3:
      setBlockBounds(0, 0, 1 - THICK, 1, 1, 1);
      break;
    case 4:
      setBlockBounds(0, 0, 0, THICK, 1, 1);
      break;
    case 5:
      setBlockBounds(1 - THICK, 0, 0, 1, 1, 1);
      break;
    default:
      setBlockBounds(0, 0, 0, 1, 1, THICK);
    }
  }
  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x,
                                                       int y, int z) {
    return null;
  }
  @Override
  public void setBlockBoundsForItemRender() {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, THICK);
  }
  @Override
  public int getRenderType() {
    return WallArtConstants.BLOCK_RENDER_ID;
  }
  @Override
  public boolean isOpaqueCube() {
    return false;
  }
  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }
}
