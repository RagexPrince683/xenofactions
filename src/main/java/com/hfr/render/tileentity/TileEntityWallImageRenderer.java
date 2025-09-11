package com.hfr.render.tileentity;

import com.hfr.tileentity.TileEntityWallImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

//no longer used I guess. Thanks schizo, I mean chatgpt.

@SideOnly(Side.CLIENT)
public class TileEntityWallImageRenderer extends TileEntitySpecialRenderer {
    private static final float THICK = 1.0F / 16.0F; // must match Block thickness

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileEntityWallImage)) return;
        TileEntityWallImage tie = (TileEntityWallImage) te;
        ResourceLocation tex = tie.getTexture();
        if (tex == null) return;

        int meta = te.getBlockMetadata();
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        // small offset to avoid z-fight
        final double eps = 0.001;
        GL11.glTranslated(eps, 0, eps);

        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_LIGHTING);
        t.startDrawingQuads();

        // draw the thin quad(s) depending on meta
        switch (meta) {
            case 2: // north (-Z): draw rectangle at z=0..THICK
                t.addVertexWithUV(0, 0, THICK, 0, 1);
                t.addVertexWithUV(1, 0, THICK, 1, 1);
                t.addVertexWithUV(1, 1, THICK, 1, 0);
                t.addVertexWithUV(0, 1, THICK, 0, 0);
                break;
            case 3: // south (+Z): z = 1-THICK
                t.addVertexWithUV(1, 0, 1.0 - THICK, 0, 1);
                t.addVertexWithUV(0, 0, 1.0 - THICK, 1, 1);
                t.addVertexWithUV(0, 1, 1.0 - THICK, 1, 0);
                t.addVertexWithUV(1, 1, 1.0 - THICK, 0, 0);
                break;
            case 4: // west (-X)
                t.addVertexWithUV(THICK, 0, 0, 0, 1);
                t.addVertexWithUV(THICK, 0, 1, 1, 1);
                t.addVertexWithUV(THICK, 1, 1, 1, 0);
                t.addVertexWithUV(THICK, 1, 0, 0, 0);
                break;
            case 5: // east (+X)
                t.addVertexWithUV(1.0 - THICK, 0, 1, 0, 1);
                t.addVertexWithUV(1.0 - THICK, 0, 0, 1, 1);
                t.addVertexWithUV(1.0 - THICK, 1, 0, 1, 0);
                t.addVertexWithUV(1.0 - THICK, 1, 1, 0, 0);
                break;
        }

        t.draw();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
