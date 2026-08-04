package com.hfr.client.journeymap;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.hfr.client.journeymap.ClientClaimOverlayCache.Snapshot;
import com.hfr.client.journeymap.ClientClaimOverlayCache.TerritoryGroup;
import com.hfr.clowder.ClaimOverlayData.Claim;
import com.hfr.config.XFConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

final class JourneyMapClaimDrawHandler implements InvocationHandler {
	private final boolean minimap; private final JourneyMapReflection reflection; private final XFJourneyMapIntegration integration;
	private boolean renderFailureLogged;
	JourneyMapClaimDrawHandler(boolean minimap, JourneyMapReflection reflection, XFJourneyMapIntegration integration) { this.minimap = minimap; this.reflection = reflection; this.integration = integration; }
	@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String name = method.getName();
		if("equals".equals(name)) return Boolean.valueOf(proxy == args[0]);
		if("hashCode".equals(name)) return Integer.valueOf(System.identityHashCode(proxy));
		if("toString".equals(name)) return "Xenofactions JourneyMap " + (minimap ? "minimap" : "fullscreen") + " overlay";
		if("draw".equals(name) && args != null && args.length == 6) { try { draw(((Double)args[0]).doubleValue(), ((Double)args[1]).doubleValue(), args[2]); renderFailureLogged = false; } catch(Throwable failure) { if(!renderFailureLogged) { renderFailureLogged = true; integration.renderFail("render invocation failed", failure); } } return null; }
		Class<?> type = method.getReturnType();
		if(type == boolean.class) return Boolean.FALSE; if(type == byte.class) return Byte.valueOf((byte)0);
		if(type == short.class) return Short.valueOf((short)0); if(type == int.class) return Integer.valueOf(0);
		if(type == long.class) return Long.valueOf(0); if(type == float.class) return Float.valueOf(0);
		if(type == double.class) return Double.valueOf(0); if(type == char.class) return Character.valueOf('\0'); return null;
	}
	private void draw(double xOffset, double yOffset, Object grid) throws Exception {
		Minecraft mc = Minecraft.getMinecraft();
		if(!XFConfig.enableJourneyMapIntegration || (minimap ? !XFConfig.journeyMapShowMinimapClaims : !XFConfig.journeyMapShowFullscreenClaims)
				|| mc == null || mc.theWorld == null || mc.thePlayer == null) return;
		Snapshot snapshot = ClientClaimOverlayCache.get(mc.thePlayer.dimension); if(snapshot == null || snapshot.claims.isEmpty()) return;
		int width = ((Integer)reflection.getWidth.invoke(grid)).intValue(), height = ((Integer)reflection.getHeight.invoke(grid)).intValue();
		boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D), blend = GL11.glIsEnabled(GL11.GL_BLEND), alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST), depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
		int blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC), blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST); float oldWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
		// LWJGL 2 generic glGetFloat requires room for 16 floats even when reading GL_CURRENT_COLOR.
		FloatBuffer color = BufferUtils.createFloatBuffer(16); GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
		GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glDisable(GL11.GL_DEPTH_TEST);
		try {
			for(Claim claim : snapshot.claims) renderClaim(grid, claim, snapshot, xOffset, yOffset, width, height);
			if(XFConfig.journeyMapShowTerritoryLabels) {
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glColor4f(1F, 1F, 1F, 1F);
				for(TerritoryGroup group : snapshot.groups) renderLabel(grid, group, xOffset, yOffset, width, height);
			}
		} finally {
			if(texture) GL11.glEnable(GL11.GL_TEXTURE_2D); else GL11.glDisable(GL11.GL_TEXTURE_2D);
			if(blend) GL11.glEnable(GL11.GL_BLEND); else GL11.glDisable(GL11.GL_BLEND); GL11.glBlendFunc(blendSrc, blendDst);
			if(alpha) GL11.glEnable(GL11.GL_ALPHA_TEST); else GL11.glDisable(GL11.GL_ALPHA_TEST); if(depth) GL11.glEnable(GL11.GL_DEPTH_TEST); else GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glLineWidth(oldWidth); GL11.glColor4f(color.get(0), color.get(1), color.get(2), color.get(3));
		}
	}
	private void renderClaim(Object grid, Claim claim, Snapshot snapshot, double xo, double yo, int width, int height) throws Exception {
		double x = claim.chunkX * 16D, z = claim.chunkZ * 16D;
		// Invoke the required public conversion, then use its uncropped companion so partially visible claims retain all corners.
		reflection.getPixel.invoke(grid, x, z);
		Point2D p0 = point(grid, x, z), p1 = point(grid, x + 16D, z), p2 = point(grid, x + 16D, z + 16D), p3 = point(grid, x, z + 16D);
		double minX = Math.min(Math.min(p0.getX(), p1.getX()), Math.min(p2.getX(), p3.getX())) + xo, maxX = Math.max(Math.max(p0.getX(), p1.getX()), Math.max(p2.getX(), p3.getX())) + xo;
		double minY = Math.min(Math.min(p0.getY(), p1.getY()), Math.min(p2.getY(), p3.getY())) + yo, maxY = Math.max(Math.max(p0.getY(), p1.getY()), Math.max(p2.getY(), p3.getY())) + yo;
		if(maxX < -8 || maxY < -8 || minX > width + 8 || minY > height + 8) return;
		float r = ((claim.color >> 16) & 255) / 255F, g = ((claim.color >> 8) & 255) / 255F, b = (claim.color & 255) / 255F;
		Tessellator t = Tessellator.instance; GL11.glColor4f(r, g, b, (float)XFConfig.journeyMapClaimFillOpacity);
		t.startDrawingQuads(); vertex(t,p0,xo,yo); vertex(t,p1,xo,yo); vertex(t,p2,xo,yo); vertex(t,p3,xo,yo); t.draw();
		GL11.glColor4f(r,g,b,(float)XFConfig.journeyMapClaimBorderOpacity); GL11.glLineWidth((float)XFConfig.journeyMapClaimBorderWidth); t.startDrawing(GL11.GL_LINES);
		if(!snapshot.sameGroup(claim.chunkX, claim.chunkZ - 1, claim.groupId)) edge(t,p0,p1,xo,yo);
		if(!snapshot.sameGroup(claim.chunkX + 1, claim.chunkZ, claim.groupId)) edge(t,p1,p2,xo,yo);
		if(!snapshot.sameGroup(claim.chunkX, claim.chunkZ + 1, claim.groupId)) edge(t,p2,p3,xo,yo);
		if(!snapshot.sameGroup(claim.chunkX - 1, claim.chunkZ, claim.groupId)) edge(t,p3,p0,xo,yo); t.draw();
	}
	private void renderLabel(Object grid, TerritoryGroup group, double xo, double yo, int width, int height) throws Exception {
		if(group.label == null || group.label.length() == 0) return;
		Point2D b0 = point(grid, group.minChunkX * 16D, group.minChunkZ * 16D), b1 = point(grid, (group.maxChunkX + 1) * 16D, group.minChunkZ * 16D);
		Point2D b2 = point(grid, (group.maxChunkX + 1) * 16D, (group.maxChunkZ + 1) * 16D), b3 = point(grid, group.minChunkX * 16D, (group.maxChunkZ + 1) * 16D);
		double minX = Math.min(Math.min(b0.getX(), b1.getX()), Math.min(b2.getX(), b3.getX())) + xo, maxX = Math.max(Math.max(b0.getX(), b1.getX()), Math.max(b2.getX(), b3.getX())) + xo;
		double minY = Math.min(Math.min(b0.getY(), b1.getY()), Math.min(b2.getY(), b3.getY())) + yo, maxY = Math.max(Math.max(b0.getY(), b1.getY()), Math.max(b2.getY(), b3.getY())) + yo;
		if(maxX < 0 || maxY < 0 || minX > width || minY > height || maxX - minX < 12D || maxY - minY < 8D) return;
		Minecraft mc = Minecraft.getMinecraft(); if(mc == null || mc.fontRenderer == null) return;
		String text = fit(group.label, Math.max(0, (int)Math.floor(maxX - minX) - 4), mc.fontRenderer);
		if(text.length() == 0) return;
		Point2D label = point(grid, group.labelX, group.labelZ);
		int textWidth = mc.fontRenderer.getStringWidth(text);
		int x = (int)Math.round(label.getX() + xo - textWidth / 2D), y = (int)Math.round(label.getY() + yo - mc.fontRenderer.FONT_HEIGHT / 2D);
		if(x + textWidth < 0 || y + mc.fontRenderer.FONT_HEIGHT < 0 || x > width || y > height) return;
		mc.fontRenderer.drawStringWithShadow(text, x, y, 0xFFFFFF);
	}
	private String fit(String label, int maxWidth, net.minecraft.client.gui.FontRenderer font) {
		if(maxWidth <= 0 || font.getStringWidth(label) <= maxWidth) return maxWidth <= 0 ? "" : label;
		String ellipsis = "..."; int ellipsisWidth = font.getStringWidth(ellipsis); if(ellipsisWidth > maxWidth) return "";
		String text = label;
		while(text.length() > 0 && font.getStringWidth(text) + ellipsisWidth > maxWidth) text = text.substring(0, text.length() - 1);
		return text.length() == 0 ? "" : text + ellipsis;
	}
	private Point2D point(Object grid,double x,double z) throws Exception { return (Point2D)reflection.getBlockPixel.invoke(grid, x, z); }
	private static void vertex(Tessellator t,Point2D p,double x,double y){t.addVertex(p.getX()+x,p.getY()+y,0);}
	private static void edge(Tessellator t,Point2D a,Point2D b,double x,double y){vertex(t,a,x,y);vertex(t,b,x,y);}
}
