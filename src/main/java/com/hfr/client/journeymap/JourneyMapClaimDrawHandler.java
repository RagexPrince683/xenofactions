package com.hfr.client.journeymap;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.hfr.client.journeymap.ClientClaimOverlayCache.Snapshot;
import com.hfr.clowder.ClaimOverlayData.Claim;
import com.hfr.config.XFConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

final class JourneyMapClaimDrawHandler implements InvocationHandler {
	private final boolean minimap; private final JourneyMapReflection reflection; private final XFJourneyMapIntegration integration;
	JourneyMapClaimDrawHandler(boolean minimap, JourneyMapReflection reflection, XFJourneyMapIntegration integration) { this.minimap = minimap; this.reflection = reflection; this.integration = integration; }
	@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String name = method.getName();
		if("equals".equals(name)) return Boolean.valueOf(proxy == args[0]);
		if("hashCode".equals(name)) return Integer.valueOf(System.identityHashCode(proxy));
		if("toString".equals(name)) return "Xenofactions JourneyMap " + (minimap ? "minimap" : "fullscreen") + " overlay";
		if("draw".equals(name) && args != null && args.length == 6) { try { draw(((Double)args[0]).doubleValue(), ((Double)args[1]).doubleValue(), args[2]); } catch(Throwable failure) { integration.fail("render invocation failed", failure); } return null; }
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
		FloatBuffer color = BufferUtils.createFloatBuffer(4); GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
		GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glDisable(GL11.GL_DEPTH_TEST);
		try {
			for(Claim claim : snapshot.claims) renderClaim(grid, claim, snapshot, xOffset, yOffset, width, height);
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
	private Point2D point(Object grid,double x,double z) throws Exception { return (Point2D)reflection.getBlockPixel.invoke(grid, x, z); }
	private static void vertex(Tessellator t,Point2D p,double x,double y){t.addVertex(p.getX()+x,p.getY()+y,0);}
	private static void edge(Tessellator t,Point2D a,Point2D b,double x,double y){vertex(t,a,x,y);vertex(t,b,x,y);}
}
