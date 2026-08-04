package com.hfr.render.tileentity;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

final class FlagBeamRenderer {

    static final ResourceLocation BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");
    private static final int BEAM_HEIGHT = 256;

    private FlagBeamRenderer() { }

    static void render(TileEntity te, double x, double y, double z, float interp, int color) {
        int beamColor = color == 0 ? 0xFFFFFF : color;
        int red = (beamColor >> 16) & 255;
        int green = (beamColor >> 8) & 255;
        int blue = beamColor & 255;
        double localStartY = -te.yCoord;

        boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int alphaFunc = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
        float alphaRef = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
        FloatBuffer color = BufferUtils.createFloatBuffer(4);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y + localStartY, z);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDepthMask(false);
        renderBeamQuads(te, interp, red, green, blue);
        GL11.glDepthMask(depthMask);
        setEnabled(GL11.GL_BLEND, blendEnabled);
        setEnabled(GL11.GL_CULL_FACE, cullEnabled);
        setEnabled(GL11.GL_LIGHTING, lightingEnabled);
        GL11.glAlphaFunc(alphaFunc, alphaRef);
        GL11.glColor4f(color.get(0), color.get(1), color.get(2), color.get(3));
        GL11.glPopMatrix();
    }

    private static void setEnabled(int capability, boolean enabled) {
        if(enabled) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
    }

    private static void renderBeamQuads(TileEntity te, float interp, int red, int green, int blue) {
        Tessellator tessellator = Tessellator.instance;
        float f2 = (float)te.getWorldObj().getTotalWorldTime() + interp;
        float f3 = -f2 * 0.2F - (float)MathHelper.floor_float(-f2 * 0.1F);
        double rotation = (double)f2 * 0.025D * -1.5D;
        double radius = 0.2D;
        double x1 = 0.5D + Math.cos(rotation + 2.356194490192345D) * radius;
        double z1 = 0.5D + Math.sin(rotation + 2.356194490192345D) * radius;
        double x2 = 0.5D + Math.cos(rotation + (Math.PI / 4D)) * radius;
        double z2 = 0.5D + Math.sin(rotation + (Math.PI / 4D)) * radius;
        double x3 = 0.5D + Math.cos(rotation + 3.9269908169872414D) * radius;
        double z3 = 0.5D + Math.sin(rotation + 3.9269908169872414D) * radius;
        double x4 = 0.5D + Math.cos(rotation + 5.497787143782138D) * radius;
        double z4 = 0.5D + Math.sin(rotation + 5.497787143782138D) * radius;
        double minU = 0.0D;
        double maxU = 1.0D;
        double minV = -1.0F + f3;
        double maxV = BEAM_HEIGHT + minV;

        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(red, green, blue, 96);
        addFace(tessellator, x1, z1, x2, z2, minU, maxU, minV, maxV);
        addFace(tessellator, x4, z4, x3, z3, minU, maxU, minV, maxV);
        addFace(tessellator, x2, z2, x4, z4, minU, maxU, minV, maxV);
        addFace(tessellator, x3, z3, x1, z1, minU, maxU, minV, maxV);
        tessellator.draw();
    }

    private static void addFace(Tessellator tessellator, double x1, double z1, double x2, double z2, double minU, double maxU, double minV, double maxV) {
        tessellator.addVertexWithUV(x1, BEAM_HEIGHT, z1, maxU, maxV);
        tessellator.addVertexWithUV(x1, 0.0D, z1, maxU, minV);
        tessellator.addVertexWithUV(x2, 0.0D, z2, minU, minV);
        tessellator.addVertexWithUV(x2, BEAM_HEIGHT, z2, minU, maxV);
    }
}
