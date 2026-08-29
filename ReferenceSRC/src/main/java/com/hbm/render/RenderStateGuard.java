package com.hbm.render;

import java.util.ArrayDeque;
import java.util.Deque;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

import com.hbm.config.ClientConfig;
import com.hbm.main.MainRegistry;

import net.minecraft.client.renderer.OpenGlHelper;

/**
 * Fixed-function state boundary for legacy render callbacks.
 *
 * Angelica and similar renderers track GL state in software, so render callbacks
 * must not leak raw LWJGL state into the next callback. This guard deliberately
 * uses the compatibility-profile attribute stack and a modelview matrix scope.
 */
public final class RenderStateGuard {

	private static final ThreadLocal<Deque<String>> CONTEXTS = new ThreadLocal<Deque<String>>() {
		@Override
		protected Deque<String> initialValue() {
			return new ArrayDeque<String>();
		}
	};

	private RenderStateGuard() { }

	public static void push(String context) {
		checkGLError(context + " before push");
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		CONTEXTS.get().push(context);
	}

	public static void pop(String context) {
		Deque<String> contexts = CONTEXTS.get();
		String pushed = contexts.isEmpty() ? null : contexts.pop();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		if(pushed != null && !pushed.equals(context) && ClientConfig.DEBUG_RENDER_GL_ERRORS.get()) {
			MainRegistry.logger.warn("Render state guard mismatch: pushed '" + pushed + "' but popped '" + context + "'");
		}
		checkGLError(context + " after pop");
	}

	public static void pushMatrix() { GL11.glPushMatrix(); }
	public static void popMatrix() { GL11.glPopMatrix(); }
	public static void pushAttrib() { GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS); }
	public static void popAttrib() { GL11.glPopAttrib(); }

	public static void resetColor() { GL11.glColor4f(1F, 1F, 1F, 1F); }
	public static void safeBlendOn() {
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	}
	public static void safeBlendOff() { GL11.glDisable(GL11.GL_BLEND); }
	public static void safeAlphaOn() {
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
	}
	public static void safeCullOn() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}
	public static void safeCullOff() { GL11.glDisable(GL11.GL_CULL_FACE); }
	public static void safeDepthWriteOn() { GL11.glDepthMask(true); }
	public static void safeDepthWriteOff() { GL11.glDepthMask(false); }
	public static void safeLightingOn() { GL11.glEnable(GL11.GL_LIGHTING); }
	public static void safeLightingOff() { GL11.glDisable(GL11.GL_LIGHTING); }
	public static void safeRescaleNormalOn() { GL11.glEnable(GL12.GL_RESCALE_NORMAL); }
	public static void safeRescaleNormalOff() { GL11.glDisable(GL12.GL_RESCALE_NORMAL); }

	public static void safeDefaultBlockState() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		resetColor();
		safeBlendOff();
		safeAlphaOn();
		safeCullOn();
		safeDepthWriteOn();
		safeLightingOn();
		safeRescaleNormalOff();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
	}

	public static void safeDefaultItemState() {
		safeDefaultBlockState();
	}

	public static void safeDefaultTESRState() {
		safeDefaultBlockState();
	}

	public static void checkGLError(String context) {
		if(!ClientConfig.DEBUG_RENDER_GL_ERRORS.get()) return;
		int error;
		while((error = GL11.glGetError()) != GL11.GL_NO_ERROR) {
			MainRegistry.logger.warn("OpenGL error in " + context + ": " + error + " (" + GLU.gluErrorString(error) + ")");
		}
	}
}
