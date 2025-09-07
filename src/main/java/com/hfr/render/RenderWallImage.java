package com.hfr.render;

import com.hfr.tileentity.TileEntityWallImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

public class RenderWallImage extends TileEntitySpecialRenderer<TileEntityWallImage> {

    @Override
    public void renderTileEntityAt(TileEntityWallImage te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te.getTexture() == null) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        // Rotate to face same as block metadata if needed
        int meta = te.getBlockMetadata();
        GL11.glRotatef(-90 * meta, 0, 1, 0);

        Minecraft.getMinecraft().getTextureManager().bindTexture(te.getTexture());

        Tessellator tes = Tessellator.instance;
        tes.startDrawingQuads();
        tes.setNormal(0, 0, -1);

        // Flat quad (like painting)
        tes.addVertexWithUV(-0.5, -0.5, -0.5, 0, 1);
        tes.addVertexWithUV( 0.5, -0.5, -0.5, 1, 1);
        tes.addVertexWithUV( 0.5,  0.5, -0.5, 1, 0);
        tes.addVertexWithUV(-0.5,  0.5, -0.5, 0, 0);

        tes.draw();

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_, float p_147500_8_) {

    }
}
