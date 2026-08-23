package com.hfr.render.block;

import com.hfr.tileentity.TileEntityWallImage;
import com.hfr.wallart.WallArtConstants;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Renders the generic Wall Art backing without interfering with its TESR. */
@SideOnly(Side.CLIENT)
public final class RenderWallArtBlock
    implements ISimpleBlockRenderingHandler {
  @Override
  public void renderInventoryBlock(Block block, int metadata, int modelId,
                                   RenderBlocks renderer) {
    block.setBlockBoundsForItemRender();
    renderer.setRenderBoundsFromBlock(block);

    Tessellator tessellator = Tessellator.instance;
    GL11.glPushMatrix();
    GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

    renderInventoryFace(renderer, tessellator, block, 0, 0.0D, -1.0D, 0.0D);
    renderInventoryFace(renderer, tessellator, block, 1, 0.0D, 1.0D, 0.0D);
    renderInventoryFace(renderer, tessellator, block, 2, 0.0D, 0.0D, -1.0D);
    renderInventoryFace(renderer, tessellator, block, 3, 0.0D, 0.0D, 1.0D);
    renderInventoryFace(renderer, tessellator, block, 4, -1.0D, 0.0D, 0.0D);
    renderInventoryFace(renderer, tessellator, block, 5, 1.0D, 0.0D, 0.0D);

    GL11.glPopMatrix();
  }

  private void renderInventoryFace(RenderBlocks renderer,
                                   Tessellator tessellator, Block block,
                                   int side, double normalX, double normalY,
                                   double normalZ) {
    IIcon icon = renderer.hasOverrideBlockTexture()
        ? renderer.overrideBlockTexture
        : block.getIcon(side, 0);
    tessellator.startDrawingQuads();
    tessellator.setNormal((float) normalX, (float) normalY, (float) normalZ);
    switch (side) {
    case 0:
      renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, icon);
      break;
    case 1:
      renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, icon);
      break;
    case 2:
      renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, icon);
      break;
    case 3:
      renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, icon);
      break;
    case 4:
      renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, icon);
      break;
    case 5:
      renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, icon);
      break;
    default:
      break;
    }
    tessellator.draw();
  }

  @Override
  public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                  Block block, int modelId,
                                  RenderBlocks renderer) {
    TileEntity tile = world.getTileEntity(x, y, z);
    if (tile instanceof TileEntityWallImage) {
      TileEntityWallImage wallArt = (TileEntityWallImage) tile;
      if (WallArtConstants.validHash(wallArt.getImageHash())) {
        return true;
      }
    }

    block.setBlockBoundsBasedOnState(world, x, y, z);
    renderer.setRenderBoundsFromBlock(block);
    return renderer.renderStandardBlock(block, x, y, z);
  }

  @Override
  public boolean shouldRender3DInInventory(int modelId) {
    return true;
  }

  @Override
  public int getRenderId() {
    return WallArtConstants.BLOCK_RENDER_ID;
  }
}
