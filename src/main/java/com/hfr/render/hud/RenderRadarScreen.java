package com.hfr.render.hud;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hfr.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class RenderRadarScreen {

	private static final ResourceLocation base = new ResourceLocation(RefStrings.MODID + ":textures/hud/radarscreen.png");
	private static final ResourceLocation error = new ResourceLocation(RefStrings.MODID + ":textures/hud/radarscreen_altitude.png");
	private static final ResourceLocation blip = new ResourceLocation(RefStrings.MODID + ":textures/hud/blip.png");
	
	public static List<Blip> blips = new ArrayList();
	public static boolean sufficient;
	
	public static void renderRadar() {
		
		Minecraft minecraft = Minecraft.getMinecraft();
		
		int width = minecraft.displayWidth;
		int height = minecraft.displayHeight;
		int size = (int) (height * 0.075);
		int marginX = 10;
		int marginY = 50;
		double zLevel = 0;
		int blipSize = 1;
		float clamp = size * 0.7F;
		float clampScaled = clamp * 0.005F;

		minecraft.getTextureManager().bindTexture(base);
		renderBase(marginX, marginY, size, zLevel);
		
		if(sufficient) {
			minecraft.getTextureManager().bindTexture(blip);
			renderBlips(marginX, marginY, size, zLevel, blipSize, clampScaled);
		} else {
			minecraft.getTextureManager().bindTexture(error);
			renderBase(marginX, marginY, size, zLevel);
		}
		
        minecraft.fontRenderer.drawString("" + blips.size(), marginX, marginY, 0xBBFFBB);
        GL11.glColor4f(1F, 1F, 1F, 1F);
	}
	
	public static void renderBase(int marginX, int marginY, int size, double zLevel) {

		Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(marginX + 0, 	marginY + size, zLevel, 0, 1);
        tessellator.addVertexWithUV(marginX + size, marginY + size, zLevel, 1, 1);
        tessellator.addVertexWithUV(marginX + size, marginY + 0, 	zLevel, 1, 0);
        tessellator.addVertexWithUV(marginX + 0, 	marginY + 0, 	zLevel, 0, 0);
        tessellator.draw();
	}
	
	public static void renderBlips(int marginX, int marginY, int size, double zLevel, int blipSize, float clamp) {

		int cX = marginX + size / 2;
		int cY = marginY + size / 2;

		Tessellator tessellator = Tessellator.instance;
		Minecraft minecraft = Minecraft.getMinecraft();
        tessellator.startDrawingQuads();
        
		for(Blip blip : blips) {

	        tessellator.addVertexWithUV(cX + (blip.x * clamp) - blipSize, cY + (blip.z * clamp) + blipSize, zLevel, 0, 1);
	        tessellator.addVertexWithUV(cX + (blip.x * clamp) + blipSize, cY + (blip.z * clamp) + blipSize, zLevel, 1, 1);
	        tessellator.addVertexWithUV(cX + (blip.x * clamp) + blipSize, cY + (blip.z * clamp) - blipSize, zLevel, 1, 0);
	        tessellator.addVertexWithUV(cX + (blip.x * clamp) - blipSize, cY + (blip.z * clamp) - blipSize, zLevel, 0, 0);
		}
		
        tessellator.draw();

		GL11.glScalef(0.25F, 0.25F, 0.25F);
		for(Blip blip : blips) {

	        minecraft.fontRenderer.drawString("" + Math.round(blip.y), cX * 4 + (int)(blip.x * clamp * 4) - 6, cY * 4 + (int)(blip.z * clamp * 4) + 4, 0xBBFFBB);
		}
		GL11.glScalef(4, 4, 4);
		
        GL11.glColor4f(1F, 1F, 1F, 1F);
	}
	
	public static class Blip {
		
		public float x;
		public float y;
		public float z;
		
		public Blip(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
}
