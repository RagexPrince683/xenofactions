package com.hbm.dim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_Destroyed;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.lib.RefStrings;
import com.hbm.render.shader.Shader;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.util.BobMathUtil;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class SkyProviderCelestial extends IRenderHandler {
	/** Rings below this apparent sky size are not resolvable without magnification. */
	private static final double MIN_RING_APPARENT_SIZE = 0.05D;
	/** Dense atmospheres and other visibility loss should obscure fine ring detail first. */
	private static final float MIN_RING_VISIBILITY = 0.25F;

	private static final ResourceLocation planetTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/planet.png");
	private static final ResourceLocation flareTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/sunspike.png");
	private static final ResourceLocation nightTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/night.png");
	//private static final ResourceLocation digammaStar = new ResourceLocation(RefStrings.MODID, "textures/misc/space/star_digamma.png");

	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");

	protected static final Shader planetShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/crescent.frag"));

	private static final String[] GL_SKY_LIST = new String[] { "glSkyList", "field_72771_w", "G" };
	private static final String[] GL_SKY_LIST2 = new String[] { "glSkyList2", "field_72781_x", "H" };

	public static boolean displayListsInitialized = false;
	public static int glSkyList;
	public static int glSkyList2;

	public SkyProviderCelestial() {
		if (!displayListsInitialized) {
			initializeDisplayLists();
		}
	}

	private void initializeDisplayLists() {
		Minecraft mc = Minecraft.getMinecraft();
		glSkyList = ReflectionHelper.getPrivateValue(RenderGlobal.class, mc.renderGlobal, GL_SKY_LIST);
		glSkyList2 = ReflectionHelper.getPrivateValue(RenderGlobal.class, mc.renderGlobal, GL_SKY_LIST2);

		displayListsInitialized = true;
	}

	private static int lastBrightestPixel = 0;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		float fogIntensity = 0;

		if(world.provider instanceof WorldProviderCelestial) {
			// Without mixins, we have to resort to some very wacky ways of checking that the lightmap needs to be updated
			// fortunately, thanks to torch flickering, we can just check to see if the brightest pixel has been modified
			if(lastBrightestPixel != mc.entityRenderer.lightmapColors[255] + mc.entityRenderer.lightmapColors[250]) {
				if(((WorldProviderCelestial)world.provider).updateLightmap(mc.entityRenderer.lightmapColors)) {
					mc.entityRenderer.lightmapTexture.updateDynamicTexture();
				}

				lastBrightestPixel = mc.entityRenderer.lightmapColors[255] + mc.entityRenderer.lightmapColors[250];
			}

			fogIntensity = ((WorldProviderCelestial) world.provider).fogDensity() * 30;
		}

		CelestialBody body = CelestialBody.getBody(world);
		CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

		boolean hasAtmosphere = atmosphere != null;

		float pressure = hasAtmosphere ? (float)atmosphere.getPressure() : 0.0F;
		float visibility = hasAtmosphere ? MathHelper.clamp_float(2.0F - pressure, 0.1F, 1.0F) : 1.0F;
		//NO DUMBASS NOT - pressure
		//wait why is it still foggy as shit WTF IS THIS

		//added back - pressure

		//OH MY GOD WHAT THE ACTUAL FUCKJ IS GOING ON IN THIS CODE BASE

		//float visibility = 1.0F;
		//if (hasAtmosphere) {
		//	if (atmosphere.hasObscuringClouds()) {
		//		visibility = 0.3F;
		//	} else if (atmosphere.getFluids().contains(Fluids.DUNAAIR)) {
		//		visibility = 0.6F;
		//	} else {
		//		visibility = 1.0F;
		//	}
		//}

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Vec3 skyColor = world.getSkyColor(mc.renderViewEntity, partialTicks);

		float skyR = (float) skyColor.xCoord;
		float skyG = (float) skyColor.yCoord;
		float skyB = (float) skyColor.zCoord;

		// Diminish sky colour when leaving the atmosphere
		if (mc.renderViewEntity.posY > 20000.0) {
			float curvature = MathHelper.clamp_float((25000.0F - (float) mc.renderViewEntity.posY) / 5000.0F, 0.0F, 1.0F);
			skyR *= curvature;
			skyG *= curvature;
			skyB *= curvature;
		}

		if(mc.gameSettings.anaglyph) {
			float[] anaglyphColor = applyAnaglyph(skyR, skyG, skyB);
			skyR = anaglyphColor[0];
			skyG = anaglyphColor[1];
			skyB = anaglyphColor[2];
		}

		float planetR = skyR;
		float planetG = skyG;
		float planetB = skyB;

		if(fogIntensity > 0.01F) {
			Vec3 fogColor = world.getFogColor(partialTicks);
			planetR = (float)BobMathUtil.clampedLerp(skyR, fogColor.xCoord, fogIntensity);
			planetG = (float)BobMathUtil.clampedLerp(skyG, fogColor.yCoord, fogIntensity);
			planetB = (float)BobMathUtil.clampedLerp(skyB, fogColor.zCoord, fogIntensity);
		}

		Vec3 planetTint = Vec3.createVectorHelper(planetR, planetG, planetB);

		Tessellator tessellator = Tessellator.instance;

		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor3f(skyR, skyG, skyB);

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0.0F, mc.gameSettings.renderDistanceChunks - 12.0F, 0.0F);

			GL11.glCallList(glSkyList);

		}
		GL11.glPopMatrix();

		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_BLEND);
		RenderHelper.disableStandardItemLighting();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		float celestialAngle = world.getCelestialAngle(partialTicks);
		double longitude = 0;
		CelestialBody tidalLockedBody = body.tidallyLockedTo != null ? CelestialBody.getBody(body.tidallyLockedTo) : null;

		if(tidalLockedBody != null) {
			longitude = SolarSystem.calculateSingleAngle(world, partialTicks, body, tidalLockedBody) + celestialAngle * 360.0 + 60.0;
		}

		// Calculate the system before drawing the star field so sunlight can be
		// reduced continuously when a nearer body crosses the solar disc.
		List<AstroMetric> metrics = SolarSystem.calculateMetricsFromBody(world, partialTicks, longitude, body);
		float starBrightness = calculateStarVisibility(partialTicks, world, mc, body, atmosphere, metrics);

		// Handle any special per-body sunset rendering
		renderSunset(partialTicks, world, mc);

		renderStars(partialTicks, world, mc, starBrightness, celestialAngle, body.axialTilt);


		GL11.glPushMatrix();
		{

			GL11.glRotatef(body.axialTilt, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

			// Draw DIGAMMA STAR
			//renderDigamma(partialTicks, world, mc, celestialAngle);
			//nah fuck allat wack shit

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			double sunSize = SolarSystem.calculateSunSize(body);
			double coronaSize = sunSize * (3 - MathHelper.clamp_float(pressure, 0.0F, 1.0F));

			renderSun(partialTicks, world, mc, SolarSystem.kerbol, sunSize, coronaSize, visibility, pressure);

			float blendAmount = hasAtmosphere ? MathHelper.clamp_float(1 - world.getSunBrightnessFactor(partialTicks), 0.25F, 1F) : 1F;

			renderCelestials(partialTicks, world, mc, metrics, celestialAngle, tidalLockedBody, planetTint, visibility, blendAmount, null, 24);

			GL11.glEnable(GL11.GL_BLEND);

			if(visibility > 0.2F) {
				// JEFF BOZOS WOULD LIKE TO KNOW YOUR LOCATION
				// ... to send you a pakedge :)))
				if(world.provider.dimensionId == 0) {
					renderSatellite(partialTicks, world, mc, celestialAngle, 1916169, new float[] { 1.0F, 0.534F, 0.385F });
				}

				// Light up the sky
				for(Map.Entry<Integer, Satellite> entry : SatelliteSavedData.getClientSats().entrySet()) {
					renderSatellite(partialTicks, world, mc, celestialAngle, entry.getKey(), entry.getValue().getColor());
				}
			}

		}
		GL11.glPopMatrix();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(0.0F, 0.0F, 0.0F);

		Vec3 pos = mc.thePlayer.getPosition(partialTicks);
		double heightAboveHorizon = pos.yCoord - world.getHorizon();

		if(heightAboveHorizon < 0.0D) {
			GL11.glPushMatrix();
			{

				GL11.glTranslatef(0.0F, 12.0F, 0.0F);
				GL11.glCallList(glSkyList2);

			}
			GL11.glPopMatrix();

			float f8 = 1.0F;
			float f9 = -((float) (heightAboveHorizon + 65.0D));
			float opposite = -f8;

			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_I(0, 255);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.draw();
		}

		if(world.provider.isSkyColored()) {
			GL11.glColor3f(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
		} else {
			GL11.glColor3f(skyR, skyG, skyB);
		}

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0.0F, -((float) (heightAboveHorizon - 16.0D)), 0.0F);
			GL11.glCallList(glSkyList2);

		}
		GL11.glPopMatrix();

		renderAtmosphereGlow(partialTicks, world, mc, body, pos);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);

	}

	/**
	 * Models naked-eye star visibility from the light actually washing out the
	 * sky, rather than using a daytime switch. The vanilla night curve remains a
	 * useful lower bound, while atmospheric scattering, altitude, weather and
	 * solar-disc occlusion provide smooth daytime transitions.
	 */
	protected float calculateStarVisibility(float partialTicks, WorldClient world, Minecraft mc, CelestialBody body, CBT_Atmosphere atmosphere, List<AstroMetric> metrics) {
		float nightCurve = 1.0F - (MathHelper.cos(world.getCelestialAngle(partialTicks) * (float)Math.PI * 2.0F) * 2.0F + 0.25F);
		nightCurve = MathHelper.clamp_float(nightCurve, 0.0F, 1.0F);
		float nightVisibility = nightCurve * nightCurve;
		float sunVisibility = body.getStar().hasTrait(CBT_Destroyed.class) ? 0.0F : calculateVisibleSunFraction(body, metrics);
		float sunFactor = MathHelper.clamp_float((world.getSunBrightnessFactor(partialTicks) - 0.2F) / 0.8F, 0.0F, 1.0F);

		Vec3 sky = world.getSkyColor(mc.renderViewEntity, partialTicks);
		float skyLuminance = MathHelper.clamp_float((float)(sky.xCoord * 0.2126D + sky.yCoord * 0.7152D + sky.zCoord * 0.0722D), 0.0F, 1.0F);
		float pressure = atmosphere != null ? MathHelper.clamp_float((float)atmosphere.getPressure(), 0.0F, 1.0F) : 0.0F;
		float altitude = MathHelper.clamp_float(((float)mc.renderViewEntity.posY - 256.0F) / (20000.0F - 256.0F), 0.0F, 1.0F);
		float atmosphereDensity = pressure * (1.0F - altitude);
		float weatherTransmission = 1.0F - MathHelper.clamp_float(world.getRainStrength(partialTicks) * 0.75F, 0.0F, 0.75F);

		float scatteredLight = skyLuminance * atmosphereDensity * sunVisibility * weatherTransmission;
		float directGlare = sunFactor * sunVisibility * weatherTransmission * (0.35F + atmosphereDensity * 0.65F);
		float washout = MathHelper.clamp_float(Math.max(scatteredLight, directGlare), 0.0F, 1.0F);
		float daylightVisibility = 1.0F - smoothstep(0.08F, 0.55F, washout);

		return MathHelper.clamp_float(Math.max(nightVisibility, daylightVisibility), 0.0F, 1.0F);
	}

	protected float calculateVisibleSunFraction(CelestialBody observer, List<AstroMetric> metrics) {
		AstroMetric observerMetric = null;
		for(AstroMetric metric : metrics) {
			if(metric.body == observer) {
				observerMetric = metric;
				break;
			}
		}
		if(observerMetric == null || observerMetric.position.lengthVector() <= 0.0D) return 1.0F;

		Vec3 toSun = Vec3.createVectorHelper(-observerMetric.position.xCoord, -observerMetric.position.yCoord, -observerMetric.position.zCoord);
		double sunDistance = toSun.lengthVector();
		double sunRadius = Math.atan(observer.getStar().radiusKm / sunDistance);
		float visible = 1.0F;

		for(AstroMetric metric : metrics) {
			if(metric.body == observer || metric.body == observer.getStar() || metric.distance <= 0.0D || metric.distance >= sunDistance) continue;
			Vec3 toBody = Vec3.createVectorHelper(metric.position.xCoord - observerMetric.position.xCoord, metric.position.yCoord - observerMetric.position.yCoord, metric.position.zCoord - observerMetric.position.zCoord);
			double separation = Math.acos(MathHelper.clamp_double(toSun.normalize().dotProduct(toBody.normalize()), -1.0D, 1.0D));
			double bodyRadius = Math.atan(metric.body.radiusKm / metric.distance);
			double overlap = 1.0D - smoothstep((float)Math.abs(sunRadius - bodyRadius), (float)(sunRadius + bodyRadius), (float)separation);
			double maximumCoverage = Math.min(1.0D, bodyRadius * bodyRadius / (sunRadius * sunRadius));
			visible = Math.min(visible, (float)(1.0D - overlap * maximumCoverage));
		}
		return MathHelper.clamp_float(visible, 0.0F, 1.0F);
	}

	private static float smoothstep(float edge0, float edge1, float value) {
		if(edge1 <= edge0) return value < edge0 ? 0.0F : 1.0F;
		float t = MathHelper.clamp_float((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}

	protected void renderAtmosphereGlow(float partialTicks, WorldClient world, Minecraft mc, CelestialBody body, Vec3 pos) {
		// Modern Angelica's optional NTM:Space compatibility mixin names this newer HBM hook.
		// RTM used to inline the glow rendering in render(), so keep the hook local and dependency-free.
		Tessellator tessellator = Tessellator.instance;
		double sc = 4.0; // scale? probably. I love magic numbers and schizophrenic bullshit.
		// AT LEAST ITS NOT MCHELI SCHIZOPHRENIC BULLSHIT!!!
		double uvOffset = (pos.xCoord / 1024) % 1;
		GL11.glPushMatrix();
		{

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_FOG);
			GL11.glEnable(GL11.GL_BLEND);

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			float sunBrightness = world.getSunBrightness(partialTicks);

			float alpha = MathHelper.clamp_float(((float)pos.yCoord - 20000.0F) / 5000.0F, 0.0F, 1.0F);
			GL11.glColor4f(sunBrightness, sunBrightness, sunBrightness, alpha);
			mc.renderEngine.bindTexture(body.texture);
			GL11.glRotated(180, 1, 0, 0);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-115 * sc, 100.0D, -115 * sc, 0.0D + uvOffset, 0.0D);
			tessellator.addVertexWithUV(115 * sc, 100.0D, -115 * sc, 1.0D + uvOffset, 0.0D);
			tessellator.addVertexWithUV(115 * sc, 100.0D, 115 * sc, 1.0D + uvOffset, 1.0D);
			tessellator.addVertexWithUV(-115 * sc, 100.0D, 115 * sc, 0.0D + uvOffset, 1.0D);
			tessellator.draw();

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glDisable(GL11.GL_BLEND);

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

		}
		GL11.glPopMatrix();
	}

	protected void renderSunset(float partialTicks, WorldClient world, Minecraft mc) {
		Tessellator tessellator = Tessellator.instance;

		float[] sunsetColor = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

		if(sunsetColor != null) {
			float[] anaglyphColor = mc.gameSettings.anaglyph ? applyAnaglyph(sunsetColor) : sunsetColor;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glPushMatrix();
			{

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);

				tessellator.startDrawing(6);
				tessellator.setColorRGBA_F(anaglyphColor[0], anaglyphColor[1], anaglyphColor[2], sunsetColor[3]);
				tessellator.addVertex(0.0, 100.0, 0.0);
				tessellator.setColorRGBA_F(sunsetColor[0], sunsetColor[1], sunsetColor[2], 0.0F);
				byte segments = 16;

				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * 3.1415927F * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					tessellator.addVertex((double)(sinAngle * 120.0F), (double)(cosAngle * 120.0F), (double)(-cosAngle * 40.0F * sunsetColor[3]));
				}

				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}

	protected void renderStars(float partialTicks, WorldClient world, Minecraft mc, float starBrightness, float celestialAngle, float axialTilt) {
		Tessellator tessellator = Tessellator.instance;

		if(starBrightness > 0.0F) {
			GL11.glPushMatrix();
			{
				GL11.glRotatef(axialTilt, 1.0F, 0.0F, 0.0F);

				mc.renderEngine.bindTexture(nightTexture);

				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

				float starBrightnessAlpha = starBrightness * 0.6f;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightnessAlpha);

				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

				GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightnessAlpha);

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 4);

				GL11.glPushMatrix();
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, 1);
				GL11.glPopMatrix();

				GL11.glPushMatrix();
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, 0);
				GL11.glPopMatrix();

				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 5);

				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 2);

				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 3);

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

			}
			GL11.glPopMatrix();
		}
	}

	protected void renderSun(float partialTicks, WorldClient world, Minecraft mc, CelestialBody sun, double sunSize, double coronaSize, float visibility, float pressure) {
		// Modern Angelica's optional NTM:Space mixin was written against a newer HBM hook with this
		// CelestialBody parameter. RTM still renders Kerbol internally, so keep this bridge as a
		// soft compatibility entry point instead of adding Angelica as a hard dependency.
		renderSun(partialTicks, world, mc, sunSize, coronaSize, visibility, pressure);
	}

	protected void renderSun(float partialTicks, WorldClient world, Minecraft mc, double sunSize, double coronaSize, float visibility, float pressure) {
		renderSun(partialTicks, world, mc, sunSize, coronaSize, visibility, pressure, 1.0F);
	}

	protected void renderSun(float partialTicks, WorldClient world, Minecraft mc, double sunSize, double coronaSize, float visibility, float pressure, float glareBrightness) {
		Tessellator tessellator = Tessellator.instance;

		if(SolarSystem.kerbol.shader != null && SolarSystem.kerbol.hasTrait(CBT_Destroyed.class)) {
			// BLACK HOLE SUN
			// WON'T YOU COME
			// AND WASH AWAY THE RAIN

			Shader shader = SolarSystem.kerbol.shader;
			double shaderSize = sunSize * SolarSystem.kerbol.shaderScale;

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			shader.use();

			float time = ((float)WorldProviderCelestial.getMasterWorldTime(world) + partialTicks) / 20.0F;
			int textureUnit = 0;

			mc.renderEngine.bindTexture(noise);

			shader.setTime(time);
			shader.setTextureUnit(textureUnit);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-shaderSize, 100.0D, -shaderSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(shaderSize, 100.0D, -shaderSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(shaderSize, 100.0D, shaderSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-shaderSize, 100.0D, shaderSize, 0.0D, 1.0D);
			tessellator.draw();

			shader.stop();

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		} else {
			// Some blanking to conceal the stars
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);

			tessellator.startDrawingQuads();
			tessellator.addVertex(-sunSize, 99.9D, -sunSize);
			tessellator.addVertex(sunSize, 99.9D, -sunSize);
			tessellator.addVertex(sunSize, 99.9D, sunSize);
			tessellator.addVertex(-sunSize, 99.9D, sunSize);
			tessellator.draw();

			// Draw the MIGHTY SUN
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);

			mc.renderEngine.bindTexture(SolarSystem.kerbol.texture);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-sunSize, 100.0D, -sunSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize, 100.0D, -sunSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize, 100.0D, sunSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-sunSize, 100.0D, sunSize, 0.0D, 1.0D);
			tessellator.draw();

			// Draw a big ol' spiky flare! Less so when there is an atmosphere,
			// and scale the glare by irradiance for vacuum/orbital views.
			float flareAlpha = MathHelper.clamp_float(glareBrightness, 0.0F, 1.0F) * (1 - MathHelper.clamp_float(pressure, 0.0F, 1.0F) * 0.75F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, flareAlpha);

			mc.renderEngine.bindTexture(flareTexture);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-coronaSize, 100.0D, -coronaSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(coronaSize, 100.0D, -coronaSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(coronaSize, 100.0D, coronaSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-coronaSize, 100.0D, coronaSize, 0.0D, 1.0D);
			tessellator.draw();
		}
	}

	protected void renderCelestials(float partialTicks, WorldClient world, Minecraft mc, List<AstroMetric> metrics, float celestialAngle, CelestialBody tidalLockedBody, Vec3 planetTint, float visibility, float blendAmount, CelestialBody orbiting, float maxSize) {
		Tessellator tessellator = Tessellator.instance;
		double minSize = 0.35D;
		float blendDarken = 0.1F;

		for(AstroMetric metric : metrics) {

			// Ignore self
			if(metric.distance == 0)
				continue;

			boolean orbitingThis = metric.body == orbiting;

			// When orbiting the sun, never render planets
			// closer than the sun visually
			if(orbiting != null && orbiting.parent == null) {

				// farther objects than our orbit are behind the sun
				if(metric.distance > orbiting.radiusKm * 5D) {
					continue;
				}
			}

			double uvOffset = orbitingThis ? 1 - ((((double)WorldProviderCelestial.getMasterWorldTime(world) + partialTicks) / 1024) % 1) : 0;
			float axialTilt = orbitingThis ? 0 : metric.body.axialTilt;

			GL11.glPushMatrix();
			{

				double size = metric.apparentSize * 2.5D;
				if(metric.apparentSize <= 0D) {
					GL11.glPopMatrix();
					continue;
				}
				size = MathHelper.clamp_double(size, 0.2D, maxSize);
				boolean renderAsPoint = size < 0.15D;

				if(renderAsPoint) {
					float alpha = MathHelper.clamp_float((float)size * 100.0F, 0.0F, 1.0F);
					GL11.glColor4f(metric.body.color[0], metric.body.color[1], metric.body.color[2], alpha * visibility);
					mc.renderEngine.bindTexture(planetTexture);

					size = minSize;
				} else {
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);
					mc.renderEngine.bindTexture(metric.body.texture);
				}

				if(tidalLockedBody != null && metric.body == tidalLockedBody) {
					GL11.glRotated(celestialAngle * -360.0 - 60.0, 1.0, 0.0, 0.0);
				} else {
					GL11.glRotated(metric.angle, 1.0, 0.0, 0.0);
				}
				GL11.glRotatef(axialTilt + 90.0F, 0.0F, 1.0F, 0.0F);

				boolean renderRings = !renderAsPoint && shouldRenderBodyRings(metric, visibility);
				if(renderRings) {
					// Draw the far side of the rings before the planet disc so the planet
					// naturally masks ring geometry that should be behind it.
					renderBodyRings(mc, tessellator, metric.body, size, visibility, true);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);
					mc.renderEngine.bindTexture(metric.body.texture);
				}

				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D + uvOffset, 0.0D);
				tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D + uvOffset, 0.0D);
				tessellator.addVertexWithUV(size, 100.0D, size, 1.0D + uvOffset, 1.0D);
				tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D + uvOffset, 1.0D);
				tessellator.draw();

				if(!renderAsPoint) {
					GL11.glEnable(GL11.GL_BLEND);

					// Draw a shader on top to render celestial phase
					OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

					planetShader.use();
					planetShader.setTime((float)-metric.phase);
					planetShader.setOffset((float)uvOffset);

					tessellator.startDrawingQuads();
					tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
					tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
					tessellator.draw();

					planetShader.stop();


					GL11.glDisable(GL11.GL_TEXTURE_2D);

					// Draw another layer on top to blend with the atmosphere
					GL11.glColor4d(planetTint.xCoord - blendDarken, planetTint.yCoord - blendDarken, planetTint.zCoord - blendDarken, (1 - blendAmount * visibility));
					OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

					tessellator.startDrawingQuads();
					tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
					tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
					tessellator.draw();

					GL11.glEnable(GL11.GL_TEXTURE_2D);

					if(renderRings) {
						renderBodyRings(mc, tessellator, metric.body, size, visibility, false);
					}
				}

			}
			GL11.glPopMatrix();
		}
	}

	/**
	 * Fine ring geometry is only useful when the body's unclamped angular size is
	 * large enough to resolve. Using the metric rather than a dimension ID keeps
	 * distant rings hidden from planetary surfaces while retaining them in nearby
	 * and orbital views (and for callers which supply magnified metrics).
	 */
	protected boolean shouldRenderBodyRings(AstroMetric metric, float visibility) {
		return SpaceConfig.enablePlanetRingRendering
			&& metric != null
			&& metric.body != null
			&& metric.body.hasRings
			&& metric.apparentSize >= MIN_RING_APPARENT_SIZE
			&& visibility >= MIN_RING_VISIBILITY;
	}

	protected void renderBodyRings(Minecraft mc, Tessellator tessellator, CelestialBody body, double size, float visibility, boolean backHalf) {
		if(body == null || body.ringColor == null || body.ringColor.length < 3 || body.ringSize <= 1.0F) return;

		boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(body.ringColor[0], body.ringColor[1], body.ringColor[2], MathHelper.clamp_float(visibility * 0.8F, 0.0F, 1.0F));

		// Ring radii were imported from NTMspace/KSP-style presentation values,
		// but RTM renders planet discs at 1:1 sky scale. Treat ringSize as an
		// altitude above the planet surface and expand that altitude so close,
		// faint systems like Uranus no longer clip into the body.
		double ringDistanceScale = 3.0D;
		double innerSize = size * (1.0D + (1.15D - 1.0D) * ringDistanceScale);
		double outerSize = size * (1.0D + (body.ringSize - 1.0D) * ringDistanceScale);
		double start = backHalf ? Math.PI : 0.0D;
		double end = backHalf ? Math.PI * 2.0D : Math.PI;
		int segments = 48;

		GL11.glPushMatrix();
		{
			// Keep the ring transform centered on the already-oriented body quad.
			// Rotating vertices at y=100 around the world origin moves the ring out of
			// the sky plane, which can make all ring geometry disappear. Translate to
			// the body center first, then draw local ring vertices around y=0.
			// Use explicit quads instead of a quad strip so old/compatibility renderers
			// do not discard the whole annulus if strip winding is interpreted poorly.
			GL11.glTranslated(0.0D, 100.0D, 0.0D);
			GL11.glRotatef(90.0F - body.ringTilt, 1.0F, 0.0F, 0.0F);
			tessellator.startDrawingQuads();
			for(int i = 0; i < segments; i++) {
				double angleA = start + (end - start) * i / segments;
				double angleB = start + (end - start) * (i + 1) / segments;
				double sinA = Math.sin(angleA);
				double cosA = Math.cos(angleA);
				double sinB = Math.sin(angleB);
				double cosB = Math.cos(angleB);

				tessellator.addVertex(cosA * outerSize, 0.0D, sinA * outerSize);
				tessellator.addVertex(cosB * outerSize, 0.0D, sinB * outerSize);
				tessellator.addVertex(cosB * innerSize, 0.0D, sinB * innerSize);
				tessellator.addVertex(cosA * innerSize, 0.0D, sinA * innerSize);
			}
			tessellator.draw();
		}
		GL11.glPopMatrix();

		if(cullWasEnabled) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	protected void renderDigamma(float partialTicks, WorldClient world, Minecraft mc, float celestialAngle) {
		Tessellator tessellator = Tessellator.instance;

		GL11.glPushMatrix();
		{

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			float brightness = (float) Math.sin(celestialAngle * Math.PI);
			brightness *= brightness;
			GL11.glColor4f(brightness, brightness, brightness, brightness);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(140.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F, 0.0F, 0.0F, 1.0F);

			//	mc.renderEngine.bindTexture(digammaStar);

			//float digamma = HbmLivingProps.getDigamma(Minecraft.getMinecraft().thePlayer);
			//	float var12 = 1F * (1 + digamma * 0.25F);
			//double dist = 100D - digamma * 2.5;

			//	tessellator.startDrawingQuads();
			//	tessellator.addVertexWithUV(-var12, dist, -var12, 0.0D, 0.0D);
			//	tessellator.addVertexWithUV(var12, dist, -var12, 0.0D, 1.0D);
			//	tessellator.addVertexWithUV(var12, dist, var12, 1.0D, 1.0D);
			//	tessellator.addVertexWithUV(-var12, dist, var12, 1.0D, 0.0D);
			//	tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	// Does anyone even play with 3D glasses anymore?
	protected float[] applyAnaglyph(float... colors) {
		float r = (colors[0] * 30.0F + colors[1] * 59.0F + colors[2] * 11.0F) / 100.0F;
		float g = (colors[0] * 30.0F + colors[1] * 70.0F) / 100.0F;
		float b = (colors[0] * 30.0F + colors[2] * 70.0F) / 100.0F;

		return new float[] { r, g, b };
	}

	protected void renderSatellite(float partialTicks, WorldClient world, Minecraft mc, float celestialAngle, long seed, float[] color) {
		Tessellator tessellator = Tessellator.instance;

		double ticks = (double)(System.currentTimeMillis() % (600 * 50)) / 50;

		GL11.glPushMatrix();
		{

			GL11.glRotatef(celestialAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F + (float)(seed % 800) * 0.1F - 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef((float)(seed % 50) * 0.1F - 20.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float)(seed % 80) * 0.1F - 2.5F, 0.0F, 0.0F, 1.0F);
			GL11.glRotated((ticks / 600.0D) * 360.0D, 1.0F, 0.0F, 0.0F);

			GL11.glColor4f(color[0], color[1], color[2], 1F);

			mc.renderEngine.bindTexture(planetTexture);

			float size = 0.5F;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-size, 100.0, -size, 0.0D, 0.0D);
			tessellator.addVertexWithUV(size, 100.0, -size, 0.0D, 1.0D);
			tessellator.addVertexWithUV(size, 100.0, size, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-size, 100.0, size, 1.0D, 0.0D);
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	// is just drawing a big cube with UVs prepared to draw a gradient
	private void renderSkyboxSide(Tessellator tessellator, int side) {
		double u = side % 3 / 3.0D;
		double v = side / 3 / 2.0D;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, u, v);
		tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, u, v + 0.5D);
		tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, u + 0.3333333333333333D, v + 0.5D);
		tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, u + 0.3333333333333333D, v);
		tessellator.draw();
	}

}
