package com.hbm.render.postprocess;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import com.hbm.config.ClientConfig;
import com.hbm.core.compat.HardcoreDarknessCompatHooks;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.main.MainRegistry;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.util.Compat;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

/** Captures and processes the completed world at the pre-HUD ALL boundary. */
public final class DarkAdaptationRenderer implements IResourceManagerReloadListener {
	private static final int METER_WIDTH = 16;
	private static final int METER_HEIGHT = 12;
	private static final int METER_PIXELS = METER_WIDTH * METER_HEIGHT;
	private final DarkAdaptationState state = new DarkAdaptationState();
	private final DarkAdaptationShader shader = new DarkAdaptationShader();
	private final boolean angelica = Compat.isModLoaded(Compat.MOD_ANG);
	private final IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
	private final FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
	private final ByteBuffer meterPixels = BufferUtils.createByteBuffer(METER_PIXELS * 4);
	private final float[] meterLuminance = new float[METER_PIXELS];
	private int sourceTexture, depthTexture, meterTexture, meterFramebuffer;
	private int textureWidth, textureHeight;
	private long lastNanos;
	private World lastWorld;
	private int exposureFrames;
	private boolean reloadPending = true;
	private long renderFrameId;
	private long worldDepthFrameId = -1L;
	private boolean depthCopySucceeded;
	private boolean geometryCoverageValid;
	private float geometryCoverage;
	private int worldDepthFramebuffer = -1;
	private float projectionScaleX = 1F, projectionScaleY = 1F;
	private int colorFramebuffer = -1;
	private String worldDepthSource = "none";
	private boolean fboSupported;
	private boolean capabilitiesKnown;
	private String exposurePath = "not sampled";
	private String failureReason = "none";
	private boolean captureWarned, depthWarned, meterWarned;
	private float ambientScotopic, skyAvailability, moonFactor, nightContribution, weatherAttenuation = 1F;
	private float requestedStrength, usedStrength, perceivedAmbient, eyeRecovery, expectedBroadBlackLuma, expectedShapeBlackLuma;

	/**
	 * Forge posts this after terrain, entities and tile entities, but EntityRenderer renders the
	 * first-person hand only after the event returns. HIGHEST captures before other world-last
	 * subscribers can replace the completed vanilla depth buffer.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void captureWorldDepth(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		renderFrameId++;
		invalidateWorldDepth("capture pending");
		if(mc.theWorld == null || mc.thePlayer == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) return;
		if(lastWorld != null && lastWorld != mc.theWorld) { state.reset(); deleteResources(); lastWorld = mc.theWorld; lastNanos = System.nanoTime(); }
		captureProjectionScale();
		try { copyWorldDepth(mc.displayWidth, mc.displayHeight); }
		catch(Throwable t) { invalidateWorldDepth("capture failed"); failOnce("World depth capture", t, 1); }
	}

	/** Restores the fog color that vanilla had after rain, before its extra thunder multiplier. */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void correctThunderFog(FogColors event) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.theWorld;
		if(world == null || mc.thePlayer == null || world.provider.hasNoSky
				|| !ClientConfig.DARK_ADAPTATION_ENABLED.get() || quality() <= 0
				|| !HardcoreDarknessCompatHooks.isCompatEnabled() || suppressed(mc.thePlayer)
				|| state.getEffectiveAdaptation() < 0.002F) return;

		float partialTicks = (float)event.renderPartialTicks;
		float angle = world.getCelestialAngle(partialTicks);
		float night = clamp((-(float)Math.cos(angle * Math.PI * 2D) - 0.05F) / 0.95F, 0F, 1F);
		if(night <= 0F) return;

		float thunder = world.getWeightedThunderStrength(partialTicks);
		if(thunder <= 0F) return;
		float inverseThunderFactor = 1F / Math.max(0.5F, 1F - thunder * 0.5F);
		event.red = clamp(event.red * inverseThunderFactor, 0F, 1F);
		event.green = clamp(event.green * inverseThunderFactor, 0F, 1F);
		event.blue = clamp(event.blue * inverseThunderFactor, 0F, 1F);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void beforeHud(RenderGameOverlayEvent.Pre event) {
		if(event.type != RenderGameOverlayEvent.ElementType.ALL) return;
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.theWorld == null || mc.thePlayer == null) { releaseWorld(); return; }
		if(lastWorld == null) { lastWorld = mc.theWorld; lastNanos = System.nanoTime(); }
		else if(lastWorld != mc.theWorld) { lastWorld = mc.theWorld; deleteResources(); lastNanos = System.nanoTime(); }
		if(mc.isGamePaused()) { lastNanos = System.nanoTime(); return; }

		long now = System.nanoTime();
		float delta = lastNanos == 0L ? 0F : (now - lastNanos) * 0.000000001F;
		lastNanos = now;
		updateEnvironmentalScotopic(mc.theWorld, mc.thePlayer);
		requestedStrength = ClientConfig.DARK_ADAPTATION_STRENGTH.get();
		usedStrength = clamp(requestedStrength, 0F, 2F);
		boolean enabled = ClientConfig.DARK_ADAPTATION_ENABLED.get() && quality() > 0;
		boolean captured = false;
		if(enabled) {
			try { captured = captureScene(mc.displayWidth, mc.displayHeight); }
			catch(Throwable t) { failOnce("Scene capture", t, 0); }
		}
		if(captured && ++exposureFrames >= (quality() == 2 ? 3 : 5)) {
			exposureFrames = 0;
			state.update(measureRenderedExposure(), delta, nuclearFlashActive());
		} else state.update(state.getExposure(), delta, nuclearFlashActive());
		updateRecoveryDiagnostics(usedStrength);
		HardcoreDarknessCompatHooks.updateDarkAdaptation(state.getEffectiveAdaptation(), perceivedAmbient);

		if(!enabled || suppressed(mc.thePlayer) || !captured) return;
		float configuredStrength = usedStrength;
		if(configuredStrength < 0.001F || state.getEffectiveAdaptation() < 0.002F) return;
		if(reloadPending) {
			reloadPending = false;
			if(!shader.load()) { recordFailure(shader.getFailureReason()); return; }
		}
		if(!shader.isLoaded()) return;
		try { composite(mc, configuredStrength); }
		catch(Throwable t) { recordFailure("Post-process: " + describe(t)); shader.destroy(); }
	}

	private boolean captureScene(int width, int height) {
		if(width <= 0 || height <= 0) return false;
		ensureCapabilities();
		colorFramebuffer = fboSupported ? GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT) : 0;
		int oldActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		int oldTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		try {
			ensureSceneTextures(width, height);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
			clearErrors();
			GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
			if(GL11.glGetError() != GL11.GL_NO_ERROR) throw new IllegalStateException("color buffer copy was rejected");
			return true;
		} finally {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture);
			GL13.glActiveTexture(oldActive);
		}
	}

	private void copyWorldDepth(int width, int height) {
		ensureCapabilities();
		int sourceFramebuffer = fboSupported ? GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT) : 0;
		int oldActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		int oldTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		try {
			ensureSceneTextures(width, height);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
			clearErrors();
			GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
			int error = GL11.glGetError();
			if(error != GL11.GL_NO_ERROR) throw new IllegalStateException("depth buffer copy GL error " + error);
			depthCopySucceeded = true;
			worldDepthFrameId = renderFrameId;
			worldDepthFramebuffer = sourceFramebuffer;
			worldDepthSource = sourceFramebuffer == 0 ? "active default framebuffer" : (angelica ? "active Angelica framebuffer" : "active Minecraft framebuffer");
			if(ClientConfig.DARK_ADAPTATION_DEBUG.get()) measureGeometryCoverage();
		} finally {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture);
			GL13.glActiveTexture(oldActive);
			if(fboSupported) EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, sourceFramebuffer);
		}
	}

	/** Reads the column-major OpenGL projection's diagonal perspective scales for this depth frame. */
	private void captureProjectionScale() {
		projectionScaleX = projectionScaleY = 1F;
		try {
			projectionBuffer.clear();
			GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer);
			float scaleX = projectionBuffer.get(0);
			float scaleY = projectionBuffer.get(5);
			if(!Float.isNaN(scaleX) && !Float.isInfinite(scaleX) && scaleX > 0.0001F
					&& !Float.isNaN(scaleY) && !Float.isInfinite(scaleY) && scaleY > 0.0001F) {
				projectionScaleX = scaleX;
				projectionScaleY = scaleY;
			}
		} catch(Throwable ignored) { }
	}

	/** Debug-only 16x12 GPU downsample of the exact shader geometry mask. */
	private void measureGeometryCoverage() {
		geometryCoverageValid = false;
		if(!fboSupported || !shader.isLoaded()) return;
		int oldFramebuffer = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
		int oldProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		int oldActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		int oldMatrix = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
		int oldTexture0 = textureBinding(GL13.GL_TEXTURE0), oldTexture1 = textureBinding(GL13.GL_TEXTURE1);
		readViewport(); int x = viewportBuffer.get(0), y = viewportBuffer.get(1), w = viewportBuffer.get(2), h = viewportBuffer.get(3);
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		boolean projection = false, modelview = false;
		try {
			ensureMeterTarget();
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, meterFramebuffer);
			GL11.glViewport(0, 0, METER_WIDTH, METER_HEIGHT);
			GL11.glDisable(GL11.GL_BLEND); GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_LIGHTING); GL11.glDepthMask(false); GL11.glColorMask(true, true, true, true);
			GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix(); projection = true; GL11.glLoadIdentity(); GL11.glOrtho(0, 1, 0, 1, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix(); modelview = true; GL11.glLoadIdentity(); GL11.glColor4f(1, 1, 1, 1);
			GL13.glActiveTexture(GL13.GL_TEXTURE0); GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
			GL13.glActiveTexture(GL13.GL_TEXTURE1); GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
			shader.use(textureWidth, textureHeight, state, 0F, 0F, 0F, 0F, 0F, 0, true, cameraFarPlane(), projectionScaleX, projectionScaleY, 1);
			drawQuad();
			meterPixels.clear(); clearErrors();
			GL11.glReadPixels(0, 0, METER_WIDTH, METER_HEIGHT, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, meterPixels);
			if(GL11.glGetError() != GL11.GL_NO_ERROR) throw new IllegalStateException("geometry coverage readback was rejected");
			int covered = 0;
			for(int i = 0; i < METER_PIXELS; i++) if((meterPixels.get(i * 4) & 255) >= 128) covered++;
			geometryCoverage = covered / (float)METER_PIXELS;
			geometryCoverageValid = true;
		} catch(Throwable t) { failOnce("Depth coverage diagnostic", t, 2); }
		finally {
			if(modelview) { GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix(); }
			if(projection) { GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); }
			GL20.glUseProgram(oldProgram); EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, oldFramebuffer);
			GL11.glPopAttrib(); GL11.glViewport(x, y, w, h);
			GL13.glActiveTexture(GL13.GL_TEXTURE1); GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture1);
			GL13.glActiveTexture(GL13.GL_TEXTURE0); GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture0);
			GL13.glActiveTexture(oldActive); GL11.glMatrixMode(oldMatrix);
		}
	}

	private float measureRenderedExposure() {
		if(!fboSupported) { exposurePath = "held rendered-scene value (FBO unsupported)"; return state.getExposure(); }
		int oldFramebuffer = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
		int oldProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		int oldActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		int oldMatrix = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		int oldTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		readViewport();
		int x = viewportBuffer.get(0), y = viewportBuffer.get(1), w = viewportBuffer.get(2), h = viewportBuffer.get(3);
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		boolean projection = false, modelview = false;
		try {
			ensureMeterTarget();
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, meterFramebuffer);
			GL11.glViewport(0, 0, METER_WIDTH, METER_HEIGHT);
			GL20.glUseProgram(0);
			GL11.glDisable(GL11.GL_BLEND); GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_LIGHTING); GL11.glDepthMask(false); GL11.glColorMask(true, true, true, true);
			GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix(); projection = true; GL11.glLoadIdentity(); GL11.glOrtho(0, 1, 0, 1, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix(); modelview = true; GL11.glLoadIdentity();
			GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture); GL11.glColor4f(1, 1, 1, 1);
			drawQuad();
			meterPixels.clear();
			GL11.glReadPixels(0, 0, METER_WIDTH, METER_HEIGHT, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, meterPixels);
			if(GL11.glGetError() != GL11.GL_NO_ERROR) throw new IllegalStateException("reduced luminance readback was rejected");
			for(int i = 0; i < METER_PIXELS; i++) {
				float r = (meterPixels.get(i * 4) & 255) / 255F;
				float g = (meterPixels.get(i * 4 + 1) & 255) / 255F;
				float b = (meterPixels.get(i * 4 + 2) & 255) / 255F;
				meterLuminance[i] = r * 0.2126F + g * 0.7152F + b * 0.0722F;
			}
			Arrays.sort(meterLuminance);
			// A trimmed mean represents the scene while preventing a few torches or particles dominating it.
			int high = (int)(METER_PIXELS * 0.85F); float sum = 0F;
			for(int i = 0; i < high; i++) sum += meterLuminance[i];
			exposurePath = "GPU 16x12 / lower-85% readback";
			return clamp(sum / high, 0F, 1F);
		} catch(Throwable t) {
			failOnce("Luminance measurement", t, 2);
			exposurePath = "held rendered-scene value (meter failed)";
			return state.getExposure();
		} finally {
			if(modelview) { GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix(); }
			if(projection) { GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); }
			GL20.glUseProgram(oldProgram); EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, oldFramebuffer);
			GL11.glPopAttrib(); GL11.glViewport(x, y, w, h);
			GL13.glActiveTexture(GL13.GL_TEXTURE0); GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture); GL13.glActiveTexture(oldActive);
			GL11.glMatrixMode(oldMatrix);
		}
	}

	private void composite(Minecraft mc, float configuredStrength) {
		int width = mc.displayWidth, height = mc.displayHeight;
		int oldFramebuffer = fboSupported ? GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT) : 0;
		int oldProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM), oldActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		int oldMatrix = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
		int oldTexture0 = textureBinding(GL13.GL_TEXTURE0), oldTexture1 = textureBinding(GL13.GL_TEXTURE1);
		readViewport(); int x = viewportBuffer.get(0), y = viewportBuffer.get(1), w = viewportBuffer.get(2), h = viewportBuffer.get(3);
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		boolean projection = false, modelview = false;
		try {
			GL11.glViewport(0, 0, width, height);
			GL11.glDisable(GL11.GL_BLEND); GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_LIGHTING); GL11.glDepthMask(false); GL11.glColorMask(true, true, true, true);
			GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix(); projection = true; GL11.glLoadIdentity(); GL11.glOrtho(0, 1, 0, 1, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix(); modelview = true; GL11.glLoadIdentity(); GL11.glColor4f(1, 1, 1, 1);
			GL13.glActiveTexture(GL13.GL_TEXTURE0); GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
			GL13.glActiveTexture(GL13.GL_TEXTURE1); GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
			int debugView = ClientConfig.DARK_ADAPTATION_DEBUG.get() ? Math.max(0, Math.min(3, ClientConfig.DARK_ADAPTATION_DEBUG_VIEW.get())) : 0;
			boolean depthAvailableToShader = worldDepthCurrent();
			shader.use(width, height, state, configuredStrength, clamp(ClientConfig.DARK_ADAPTATION_SCOTOPIC_FLOOR.get(), 0F, 0.15F), ambientScotopic, clamp(ClientConfig.DARK_ADAPTATION_NOISE.get(), 0F, 0.05F),
				clamp(ClientConfig.DARK_ADAPTATION_CENTER_LOSS.get(), 0F, 0.20F), quality(), depthAvailableToShader, cameraFarPlane(), projectionScaleX, projectionScaleY, debugView);
			drawQuad();
		} finally {
			if(modelview) { GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix(); }
			if(projection) { GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); }
			GL20.glUseProgram(oldProgram);
			if(fboSupported) EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, oldFramebuffer);
			GL11.glPopAttrib(); GL11.glViewport(x, y, w, h);
			GL13.glActiveTexture(GL13.GL_TEXTURE1); GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture1);
			GL13.glActiveTexture(GL13.GL_TEXTURE0); GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture0);
			GL13.glActiveTexture(oldActive); GL11.glMatrixMode(oldMatrix);
		}
	}

	private int textureBinding(int unit) { GL13.glActiveTexture(unit); return GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D); }
	private float cameraFarPlane() { return Math.max(32F, Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 32F); }
	private boolean worldDepthCurrent() {
		return depthCopySucceeded && worldDepthFrameId == renderFrameId && textureWidth == Minecraft.getMinecraft().displayWidth
			&& textureHeight == Minecraft.getMinecraft().displayHeight && worldDepthFramebuffer == colorFramebuffer
			&& (!geometryCoverageValid || geometryCoverage > 0F);
	}
	private void invalidateWorldDepth(String source) {
		depthCopySucceeded = false; geometryCoverageValid = false; geometryCoverage = 0F;
		worldDepthFrameId = -1L; worldDepthFramebuffer = -1; worldDepthSource = source;
		projectionScaleX = projectionScaleY = 1F;
	}
	/** Uses saved vanilla sky light and celestial state, not HD's patched final lightmap/brightness. */
	private void updateEnvironmentalScotopic(World world, EntityPlayer player) {
		if(world.provider.hasNoSky) { ambientScotopic = skyAvailability = moonFactor = nightContribution = 0F; weatherAttenuation = 1F; return; }
		int x = MathHelper.floor_double(player.posX), y = MathHelper.floor_double(player.posY + player.getEyeHeight()), z = MathHelper.floor_double(player.posZ);
		float center = world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) / 15F;
		float nearby = world.getSavedLightValue(EnumSkyBlock.Sky, x + 2, y, z) + world.getSavedLightValue(EnumSkyBlock.Sky, x - 2, y, z)
			+ world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z + 2) + world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z - 2);
		skyAvailability = clamp(center * 0.60F + nearby / 60F * 0.40F, 0F, 1F);
		if(world.canBlockSeeTheSky(x, y, z)) skyAvailability = Math.max(skyAvailability, 0.95F);
		float angle = world.getCelestialAngle(1F);
		float night = clamp((-(float)Math.cos(angle * Math.PI * 2D) - 0.05F) / 0.95F, 0F, 1F);
		int phase = world.getMoonPhase();
		int phaseDistance = Math.min(phase, 8 - phase);
		moonFactor = 1F - phaseDistance / 4F;
		nightContribution = night * (0.22F + 0.78F * moonFactor * moonFactor);
		float rain = world.getRainStrength(1F);
		weatherAttenuation = clamp(1F - rain * 0.35F, 0.65F, 1F);
		ambientScotopic = clamp(skyAvailability * nightContribution * weatherAttenuation, 0F, 1F);
	}
	private void readViewport() { viewportBuffer.clear(); GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer); }
	private static void drawQuad() { Tessellator t = Tessellator.instance; t.startDrawingQuads(); t.addVertexWithUV(0,0,0,0,0); t.addVertexWithUV(1,0,0,1,0); t.addVertexWithUV(1,1,0,1,1); t.addVertexWithUV(0,1,0,0,1); t.draw(); }

	private void ensureCapabilities() { if(!capabilitiesKnown) { fboSupported = GLContext.getCapabilities().GL_EXT_framebuffer_object; capabilitiesKnown = true; } }
	private void ensureSceneTextures(int width, int height) {
		if(sourceTexture == 0) sourceTexture = createTexture(GL11.GL_LINEAR);
		if(depthTexture == 0) depthTexture = createTexture(GL11.GL_NEAREST);
		if(width == textureWidth && height == textureHeight) return;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture); GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture); GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (ByteBuffer)null);
		textureWidth = width; textureHeight = height;
	}
	private void ensureMeterTarget() {
		if(meterTexture == 0) { meterTexture = createTexture(GL11.GL_LINEAR); GL11.glBindTexture(GL11.GL_TEXTURE_2D, meterTexture); GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, METER_WIDTH, METER_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null); }
		if(meterFramebuffer == 0) meterFramebuffer = EXTFramebufferObject.glGenFramebuffersEXT();
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, meterFramebuffer);
		EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, meterTexture, 0);
		int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
		if(status != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) throw new IllegalStateException("meter FBO incomplete: " + status);
	}
	private static int createTexture(int filter) { int id = GL11.glGenTextures(); GL11.glBindTexture(GL11.GL_TEXTURE_2D, id); GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter); GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter); GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP); GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP); return id; }
	private static void clearErrors() { while(GL11.glGetError() != GL11.GL_NO_ERROR) { } }

	private boolean suppressed(EntityPlayer player) { if(player.isPotionActive(Potion.nightVision) || player.isPotionActive(Potion.blindness)) return true; ItemStack chest = player.inventory.armorInventory[2]; return chest != null && chest.getItem() instanceof ArmorFSB && ((ArmorFSB)chest.getItem()).thermal; }
	private boolean nuclearFlashActive() { return System.currentTimeMillis() < ModEventHandlerClient.flashTimestamp + ModEventHandlerClient.flashDuration; }
	private int quality() { return Math.max(0, Math.min(2, ClientConfig.DARK_ADAPTATION_QUALITY.get())); }
	private static float clamp(float v, float lo, float hi) { return DarkAdaptationState.clamp(v, lo, hi); }
	private void updateRecoveryDiagnostics(float strength) {
		perceivedAmbient = (float)Math.pow(clamp(ambientScotopic, 0F, 1F), 0.30D);
		eyeRecovery = clamp(state.getConeAdaptation() * 0.25F + state.getRodAdaptation() * 0.75F, 0F, 1F);
		float target = clamp(ClientConfig.DARK_ADAPTATION_SCOTOPIC_FLOOR.get(), 0F, 0.15F) * 2.55F;
		float recoveryStrength = clamp(strength, 0F, 1F);
		expectedBroadBlackLuma = perceivedAmbient * eyeRecovery * target * recoveryStrength;
		expectedShapeBlackLuma = expectedBroadBlackLuma * 1.20F;
	}

	@SubscribeEvent public void debug(RenderGameOverlayEvent.Post event) {
		if(event.type != RenderGameOverlayEvent.ElementType.ALL || !ClientConfig.DARK_ADAPTATION_DEBUG.get()) return;
		Minecraft mc = Minecraft.getMinecraft(); if(mc.theWorld == null) return;
		updateRecoveryDiagnostics(usedStrength);
		mc.fontRenderer.drawStringWithShadow(String.format("sceneExposure %.3f coneAdaptation %.3f rodAdaptation %.3f effectiveAdaptation %.3f", state.getExposure(), state.getConeAdaptation(), state.getRodAdaptation(), state.getEffectiveAdaptation()), 4, 4, 0xB0B0B0);
		mc.fontRenderer.drawStringWithShadow(String.format("requestedStrength %.3f usedStrength %.3f ambientScotopic %.3f perceivedAmbient %.3f eyeRecovery %.3f", requestedStrength, usedStrength, ambientScotopic, perceivedAmbient, eyeRecovery), 4, 14, 0x909090);
		mc.fontRenderer.drawStringWithShadow(String.format("sky/night/moon/weather %.3f/%.3f/%.3f/%.3f expectedBroadBlackLuma %.3f expectedShapeBlackLuma %.3f", skyAvailability, nightContribution, moonFactor, weatherAttenuation, expectedBroadBlackLuma, expectedShapeBlackLuma), 4, 24, 0x909090);
		int debugView = Math.max(0, Math.min(3, ClientConfig.DARK_ADAPTATION_DEBUG_VIEW.get()));
		long age = worldDepthFrameId < 0L ? -1L : renderFrameId - worldDepthFrameId;
		mc.fontRenderer.drawStringWithShadow("depthCopySucceeded " + depthCopySucceeded + " worldDepthCurrent " + worldDepthCurrent() + " worldDepthFrameAge " + age, 4, 34, 0x909090);
		mc.fontRenderer.drawStringWithShadow("worldDepthSource " + worldDepthSource + " worldDepthFramebuffer " + worldDepthFramebuffer, 4, 44, 0x909090);
		mc.fontRenderer.drawStringWithShadow("geometryCoverage " + (geometryCoverageValid ? String.format("%.3f", geometryCoverage) : "n/a") + " depthAvailableToShader " + worldDepthCurrent(), 4, 54, 0x909090);
		mc.fontRenderer.drawStringWithShadow("shaderLoaded " + shader.isLoaded() + " framebuffer MC/EXT " + OpenGlHelper.isFramebufferEnabled() + "/" + fboSupported + " exposure " + exposurePath, 4, 64, 0x909090);
		mc.fontRenderer.drawStringWithShadow("failureReason " + failureReason + " debugView " + debugView + " Angelica " + angelica, 4, 74, 0x909090);
		mc.fontRenderer.drawStringWithShadow("hardcoreDarknessDetected " + HardcoreDarknessCompatHooks.isDetected() + " hardcoreDarknessCompatEnabled " + HardcoreDarknessCompatHooks.isCompatEnabled() + " hardcoreDarknessHookPatched " + HardcoreDarknessCompatHooks.isHookPatched(), 4, 84, 0x909090);
		mc.fontRenderer.drawStringWithShadow(String.format("hdSkyCarrier %.3f hdSkyMultiplierOverride %.3f hdSkyMinimumOverride %.3f", HardcoreDarknessCompatHooks.getSkyCarrier(), HardcoreDarknessCompatHooks.getSkyMultiplierOverride(), HardcoreDarknessCompatHooks.getSkyMinimumOverride()), 4, 94, 0x909090);
	}

	private void failOnce(String operation, Throwable t, int kind) { if((kind == 0 && captureWarned) || (kind == 1 && depthWarned) || (kind == 2 && meterWarned)) return; if(kind == 0) captureWarned = true; else if(kind == 1) depthWarned = true; else meterWarned = true; recordFailure(operation + ": " + describe(t)); }
	private void recordFailure(String reason) { failureReason = reason == null ? "unknown failure" : reason; MainRegistry.logger.warn("Dark adaptation " + failureReason); }
	private static String describe(Throwable t) { return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage()); }
	private void releaseWorld() { if(lastWorld != null) { state.reset(); lastWorld = null; } ambientScotopic = skyAvailability = moonFactor = nightContribution = 0F; weatherAttenuation = 1F; lastNanos = 0L; deleteResources(); }
	private void deleteResources() { if(sourceTexture != 0) GL11.glDeleteTextures(sourceTexture); if(depthTexture != 0) GL11.glDeleteTextures(depthTexture); if(meterTexture != 0) GL11.glDeleteTextures(meterTexture); if(meterFramebuffer != 0 && fboSupported) EXTFramebufferObject.glDeleteFramebuffersEXT(meterFramebuffer); sourceTexture = depthTexture = meterTexture = meterFramebuffer = textureWidth = textureHeight = 0; colorFramebuffer = -1; invalidateWorldDepth("resources released"); }
	@Override public void onResourceManagerReload(IResourceManager manager) { shader.destroy(); deleteResources(); reloadPending = true; captureWarned = depthWarned = meterWarned = false; failureReason = "none"; exposurePath = "not sampled"; }
}
