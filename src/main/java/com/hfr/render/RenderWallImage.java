package com.hfr.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import com.hfr.tileentity.TileEntityWallImage;

public class RenderWallImage extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileEntityWallImage)) return;
        TileEntityWallImage tie = (TileEntityWallImage) te;

        ResourceLocation tex = tie.getTexture();
        if (tex == null) return; // nothing loaded yet

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        // rotate to face based on metadata
        int meta = tie.getBlockMetadata();
        GL11.glRotatef(180F, 0F, 1F, 0F); // flip default facing
        GL11.glRotatef(meta * 90F, 0F, 1F, 0F);

        // bind texture
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        // flat quad, 1x1 block size, slim like painting
        double depth = 0.0625D; // 1/16th thick
        tess.addVertexWithUV(-0.5, -0.5, -depth, 0, 1);
        tess.addVertexWithUV( 0.5, -0.5, -depth, 1, 1);
        tess.addVertexWithUV( 0.5,  0.5, -depth, 1, 0);
        tess.addVertexWithUV(-0.5,  0.5, -depth, 0, 0);

        tess.draw();

        GL11.glPopMatrix();
    }
}
