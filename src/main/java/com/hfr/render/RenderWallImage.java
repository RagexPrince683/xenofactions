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
        if (tex == null) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        int meta = tie.getBlockMetadata();

        // rotate quad to match block facing
        switch (meta) {
            case 2: // north (-Z)
                GL11.glRotatef(0F, 0F, 1F, 0F);
                break;
            case 3: // south (+Z)
                GL11.glRotatef(180F, 0F, 1F, 0F);
                break;
            case 4: // west (-X)
                GL11.glRotatef(90F, 0F, 1F, 0F);
                break;
            case 5: // east (+X)
                GL11.glRotatef(-90F, 0F, 1F, 0F);
                break;
        }

        // bind texture
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);

        // draw quad (flush against wall)
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        double depth = 0.0625D;

        tess.addVertexWithUV(-0.5, -0.5, -depth, 0, 1);
        tess.addVertexWithUV(0.5, -0.5, -depth, 1, 1);
        tess.addVertexWithUV(0.5, 0.5, -depth, 1, 0);
        tess.addVertexWithUV(-0.5, 0.5, -depth, 0, 0);

        tess.draw();
        GL11.glPopMatrix();
    }
}
