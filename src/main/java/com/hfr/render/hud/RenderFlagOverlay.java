package com.hfr.render.hud;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public class RenderFlagOverlay {

	public static ResourceLocation flag = null;
	public static ResourceLocation overlay = null;
	public static String title = "";
	public static String zone = "";
	public static int color = 0xFF0000;
	public static boolean tintFlag = true;
	public static String customFlagName = "";
	public static long startingTime;
	public static int ttl = 5000;
    private static ScaledResolution res = null;

	public static void drawFlag() {

		long endTime = startingTime + ttl;
		if(endTime < System.currentTimeMillis())
			return;

		GL11.glPushMatrix();

		Minecraft minecraft = Minecraft.getMinecraft();

		// ---- FIX: reset broken GL state from other renderers ----
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1F,1F,1F,1F);
		// ----------------------------------------------------------

		res = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();

		double posX = width / 2D;
		double posY = 30D;

		GL11.glTranslated(posX, posY, 0);

		byte r = (byte)(((color & 0xFF0000) >> 16) / 2);
		byte g = (byte)(((color & 0xFF00) >> 8) / 2);
		byte b = (byte)((color & 0xFF) / 2);

		int alpha = (int)((endTime - System.currentTimeMillis()) * 256 / 1000);
		if(alpha > 255) alpha = 255;

		if(alpha > 10) {

			if(flag != null) {
				minecraft.getTextureManager().bindTexture(flag);
				if(tintFlag)
					GL11.glColor4b(r, g, b, (byte)(alpha / 2));
				else
					GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)(alpha / 2));
				drawRectangle(0, 0);
			}

			if(overlay != null) {
				minecraft.getTextureManager().bindTexture(overlay);
				GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)(alpha / 2));
				drawRectangle(0, 0);
			}

			FontRenderer font = minecraft.fontRenderer;

			String s = title.replace('_', ' ');
			int w = font.getStringWidth(s);
			font.drawString(s, -w / 2, (int)posY, color | (alpha << 24));

			s = zone.replace('_', ' ');
			w = font.getStringWidth(s);
			font.drawString(s, -w / 2, (int)(posY + 10), color | (alpha << 24));
		}

		// ---- restore GL state ----
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1F,1F,1F,1F);
		// --------------------------

		GL11.glPopMatrix();
	}
	
	int delay;
	
	private static void drawRectangle(double posX, double posY) {
		
		int hor = 30;
		int ver = 20;

		Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(posX - hor, posY - ver, 0, 0, 0);
        tessellator.addVertexWithUV(posX - hor, posY + ver, 0, 0, 1);
        tessellator.addVertexWithUV(posX + hor, posY + ver, 0, 1, 1);
        tessellator.addVertexWithUV(posX + hor, posY - ver, 0, 1, 0);
        tessellator.draw();
	}

}
