package com.hbm.dim;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Temperature;
import com.hbm.dim.trait.CBT_Water;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.BobMathUtil;

import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SolarSystem {

	public static CelestialBody kerbol;

	// How much to scale celestial objects when rendering
	public static final double RENDER_SCALE = 90F;
	//nerfed for irl values
	public static final double SUN_RENDER_SCALE = 16F;


	public static void init() {
		// All values WERE pulled directly from KSP, most values WERE auto-converted to MC friendly ones
		// Then selectively lobotomized to realistic standards by ragex
		//TODO: FIX VALUES GOING OVER LIMIT
		// ALSO FIX ISSUE MENTIONED HERE BY COMMENT FROM MELLOW:
		//ALREADY DONE via refactor of celestial body:
		// tip: Rotational period is the sidereal rotation period of the body, not the orbital period!
		// Orbital period is derived from the bodies semi-major axis (the average orbital distance from the parent body)
		// and the mass of the parent,
		// it is not entered manually! In fact most values are not entered manually,
		// since we can use real-world orbital mechanics to derive them.
		//the sun
		kerbol = new CelestialBody("kerbol", SpaceConfig.sunDimension, Body.SUN)
			.withMassRadius(1.989e30F, 696_340) //testing irl values
			.withRotationalPeriod(2_199_040) // ~25.45 Earth days
			.withTexture("textures/environment/sun.png")
			//lava ore texutres
			.withBlockTextures(
				"minecraft:textures/blocks/lava_still.png",
				"minecraft:textures/blocks/lava_flow.png"
			)
			.withShader(new ResourceLocation(RefStrings.MODID, "shaders/blackhole.frag"), 3) // Only shows when CBT_Destroyed
			.withTraits(
				new CBT_Temperature(5505) // photosphere temperature
			)
			.withSatellites(



				//yk what nah just gonna put irl values in

				//mercury
				new CelestialBody("moho", SpaceConfig.mohoDimension, Body.MOHO)
					.withMassRadius(3.301e23F, 2_440)
					.withSemiMajorAxis(57_909_050D)
					.withInitialOrbitalAngle(252.25D)
					.withRotationalPeriod(5_067_072) // 58.646 Earth days
					.withColor(0.4863F, 0.4F, 0.3456F)
					.withBlockTextures(RefStrings.MODID + ":textures/blocks/moho_stone.png", RefStrings.MODID + ":textures/blocks/moho_regolith.png")
					.withAxialTilt(0.03F)
					.withProcessingLevel(1)
					.withTraits(new CBT_Temperature(167)),

				//venus (retrograde)
				new CelestialBody("eve", SpaceConfig.eveDimension, Body.EVE)
					.withMassRadius(4.867e24F, 6_052)
					.withSemiMajorAxis(108_208_000D)
					.withInitialOrbitalAngle(181.98D)
					.withRotationalPeriod(-20_996_640) // -243.025 Earth days
					.withColor(0.408F, 0.298F, 0.553F)
					.withBlockTextures(RefStrings.MODID + ":textures/blocks/eve_stone_2.png", RefStrings.MODID + ":textures/blocks/eve_silt.png")
					.withProcessingLevel(2)
					.withTraits(new CBT_Atmosphere(Fluids.EVEAIR, 92D), new CBT_Temperature(464), new CBT_Water(Fluids.MERCURY)),


				//earth
				new CelestialBody("kerbin", 0, Body.KERBIN) // overworld
					.withMassRadius(5.972e24F, 6_371)
					.withSemiMajorAxis(149_598_023D)
					.withInitialOrbitalAngle(100.46D)
					.withRotationalPeriod(86_164) // sidereal day
					.withAxialTilt(23.44F)
					.withBlockTextures("textures/blocks/stone.png", "textures/blocks/dirt.png")
					//by default, minecraft has a day lasting 20 minutes. That's retarded.
					//I'm gonna make it an hour
					.withColor(0.608F, 0.914F, 1.0F)
					.withTraits(
						new CBT_Atmosphere(Fluids.AIR, 1D),
						new CBT_Water(),
						new CBT_Temperature(15)
					)
					.withSatellites(

						//our atmosphere refracts light and shit so fuck you it looks right
						//even if the axis and shit is wrong

						new CelestialBody("mun", SpaceConfig.moonDimension, Body.MUN)
							.withMassRadius(7.346e22F, 1_737)
							.withSemiMajorAxis(384_399D)
							.withRotationalPeriod(2_360_591) // tidally locked
							.withTraits(new CBT_Temperature(-20))
							//testing
							//but then minecraft scaling and shit so 655_719
							//that dont work
							.withTidalLockingTo("kerbin")
							//.withBlockTextures(RefStrings.MODID + ":moon_rock", "", "", "")
							.withBlockTextures(RefStrings.MODID + ":textures/blocks/moon_rock.png", RefStrings.MODID + ":textures/blocks/moon_turf.png")

							//.getOrbitalAngle()
							//.getAngle("kerbin")
							//.
						//,

						//new CelestialBody("minmus", SpaceConfig.minmusDimension, Body.MINMUS)
						//	.withMassRadius(2.646e19F, 60)
						//	.withSemiMajorAxis(47_000)
						//	.withRotationalPeriod(40_400)
						//	.withBlockTextures(RefStrings.MODID + ":minmus_stone", "", "", "")
						//	.withTraits(new CBT_Water(Fluids.MILK))

					),

				//mars
				new CelestialBody("duna", SpaceConfig.dunaDimension, Body.DUNA)
					.withMassRadius(6.417e23F, 3_390)
					.withSemiMajorAxis(227_943_824D)
					.withInitialOrbitalAngle(355.43D)
					.withRotationalPeriod(88_775) // 24h 37m 22s
					.withAxialTilt(25.19F)
					.withColor(0.6471f, 0.2824f, 0.1608f)
					.withBlockTextures(RefStrings.MODID + ":textures/blocks/duna_rock.png", RefStrings.MODID + ":textures/blocks/duna_sands.png")
					.withProcessingLevel(1)
					.withTraits(
						new CBT_Atmosphere(Fluids.DUNAAIR, 0.006D),
						new CBT_Temperature(-63)
					)
					.withProcessingLevel(1)
					.withSatellites(

						//phobos
						new CelestialBody("ike", SpaceConfig.ikeDimension, Body.IKE)
							.withMassRadius(1.0659e16F, 11)
							.withSemiMajorAxis(9_376D)
							.withRotationalPeriod(27_553) // tidally locked
							.withBlockTextures(RefStrings.MODID + ":textures/blocks/ike_stone.png", RefStrings.MODID + ":textures/blocks/ike_regolith.png")
							.withProcessingLevel(1)
							.withTraits(
								new CBT_Temperature(-40)
							)
							.withTidalLockingTo("duna"),

						//RTM changes: Adding rest of moons/planets for realism WIP

						//Deimos
						new CelestialBody("deimos")
							//, SpaceConfig.deimosDimension, Body.DEI
							.withMassRadius(1.4762e15F, 6)
							.withSemiMajorAxis(23_463D)
							.withRotationalPeriod(109_123) // tidally locked
							//todo change block textures
							.withBlockTextures(RefStrings.MODID + ":textures/blocks/ike_stone.png", RefStrings.MODID + ":textures/blocks/ike_regolith.png")
							//this is probably fine
							.withTexture("hbm:textures/misc/space/planet.png")
							//todo add new bullshit
							.withProcessingLevel(1)
							.withTidalLockingTo("duna")
							//processing level is tech progression

					),

				//ceres
				new CelestialBody("dres", SpaceConfig.dresDimension, Body.DRES)
					.withMassRadius(9.393e20F, 469)
					.withSemiMajorAxis(413_700_000D)
					.withInitialOrbitalAngle(80.30D)
					.withRotationalPeriod(32_673)
					.withBlockTextures(RefStrings.MODID + ":textures/blocks/dresbase.png", RefStrings.MODID + ":textures/blocks/sellafield_slaked.png")
					.withTraits(new CBT_Temperature(-105))
					.withProcessingLevel(2),




				//jupiter
				new CelestialBody("jool", SpaceConfig.jupiterDimension, Body.JOOL)
					.withMassRadius(1.898e27F, 69_911)
					.withBlockTextures(RefStrings.MODID + ":textures/blocks/jupiter_storm.png", RefStrings.MODID + ":textures/blocks/jupiter_storm.png")

					.withSemiMajorAxis(778_547_200D)
					.withInitialOrbitalAngle(34.35D)
					.withRotationalPeriod(35_730) // System III rotation
					.withProcessingLevel(3)
					.withColor(1.0f, 0.5f, 0.0f)
					.withAxialTilt(3.13F)
					.withTraits(
						new CBT_Atmosphere(Fluids.HYDROGEN, 89D)
							.and(Fluids.HELIUM4, 10D)
							.and(Fluids.GAS, 1D),
						new CBT_Temperature(-145)
					)

					//hopefully orange?
					//OH ITS BECAUSE THIS ISNT AN OVERLAY, THIS MOD ACTUALLY USES A TEXTURE FOR UP CLOSE SHIT AMAZING
					.withSatellites(

						//europa
						new CelestialBody("laythe", SpaceConfig.laytheDimension, Body.LAYTHE)
							.withBlockTextures(RefStrings.MODID + ":textures/blocks/laythe_silt", "textures/blocks/stone.png")
							.withMassRadius(4.7998e22F, 1_560)
							.withSemiMajorAxis(671_100D)
							.withRotationalPeriod(306_806)
							.withTidalLockingTo("jool")
							.withProcessingLevel(3)
							.withTraits(new CBT_Atmosphere(Fluids.AIR, 0.12D).and(Fluids.HYDROGEN, 0.15D), new CBT_Water()),

						//Ganymede
						new CelestialBody("vall") //probably
							.withMassRadius(1.4819e23F, 2_634)
							.withSemiMajorAxis(1_070_400D)
							.withRotationalPeriod(618_153)
							.withTidalLockingTo("jool"),

						//Callisto
						new CelestialBody("tylo") // what value is this planet gonna add???
							.withMassRadius(1.0759e23F, 2_410)
							.withSemiMajorAxis(1_882_700D)
							.withRotationalPeriod(1_441_931)
							.withTidalLockingTo("jool"),

						//Amalthea
						new CelestialBody("bop")
							.withMassRadius(2.08e18F, 83)
							.withSemiMajorAxis(181_400D)
							.withRotationalPeriod(42_498)
							.withTidalLockingTo("jool"),

						//Himalia
						new CelestialBody("pol")
							.withMassRadius(6.7e18F, 85)
							.withSemiMajorAxis(11_461_000D)
							.withRotationalPeriod(27_756) // NOT tidally locked
							.withTidalLockingTo("jool"),

						//RTM changes: Adding rest of moons/planets for realism WIP

						//IO moon
						new CelestialBody("io")
							//volcanic hazards
							.withMassRadius(8.9319e22F, 1_821)
							.withSemiMajorAxis(421_700D)
							.withRotationalPeriod(152_853) // tidally locked
							.withTidalLockingTo("jool")

					),

				//saturn
				new CelestialBody("sarnus", SpaceConfig.saturnDimension, Body.SARNUS)
					.withBlockTextures(RefStrings.MODID + ":textures/blocks/stormcloud.png", RefStrings.MODID + ":textures/blocks/stormcloud.png")
					.withMassRadius(5.683e26F, 58_232)
					.withSemiMajorAxis(1_433_530_000D)
					.withInitialOrbitalAngle(50.08D)
					.withRotationalPeriod(38_362)
					.withColor(1f, 0.6862f, 0.5882f)
					.withAxialTilt(26.73F)
					.withProcessingLevel(3)
					.withRings(26.73F, 3.0F, 0.72F, 0.60F, 0.48F)
					.withTraits(
						new CBT_Atmosphere(Fluids.HYDROGEN, 96D)
							.and(Fluids.HELIUM4, 3D)
							.and(Fluids.GAS, 1D),
						new CBT_Temperature(-178)
					)
					.withSatellites(

					//pan
					new CelestialBody("hale") //tiny rock thing
						.withMassRadius(4.95e15F, 14)
						.withSemiMajorAxis(133_584D)
						.withRotationalPeriod(49_823)
						.withTidalLockingTo("sarnus"),

					//Atlas
					new CelestialBody("ovok") //nah
						.withMassRadius(6.6e16F, 15)
						.withSemiMajorAxis(137_670D)
						.withRotationalPeriod(51_754)
						.withTidalLockingTo("sarnus"),

					//Enceladus
					new CelestialBody("slate") //Subsurface ocean, geysers.
						.withMassRadius(1.08e20F, 252)
						.withSemiMajorAxis(237_948D)
						.withRotationalPeriod(118_386)
						.withTidalLockingTo("sarnus"),

					//Titan
					new CelestialBody("tekto")

						.withMassRadius(1.3452e23F, 2_575)
						.withSemiMajorAxis(1_221_870D)
						.withRotationalPeriod(1_377_648)
						.withTidalLockingTo("sarnus")
						.withAxialTilt(25F)
						.withTraits(new CBT_Atmosphere(Fluids.TEKTOAIR, 1.5F), new CBT_Temperature(-179)),

					//Iapetus
					new CelestialBody("iapetus")
						.withMassRadius(1.8056e21F, 734)
						.withSemiMajorAxis(3_560_820D)
						.withRotationalPeriod(6_853_440)
						.withTidalLockingTo("sarnus"),

					//Mimas
					new CelestialBody("mimas")
						.withMassRadius(3.7493e19F, 198)
						.withSemiMajorAxis(185_539D)
						.withRotationalPeriod(81_259)
						.withTidalLockingTo("sarnus")

				),

				//Uranus (retrograde)
				new CelestialBody("uranus", SpaceConfig.uranusDimension, Body.URANUS)
					.withBlockTextures("textures/blocks/water_still.png", "textures/blocks/water_flowing.png")
					.withMassRadius(8.681e25F, 25_362)
					.withSemiMajorAxis(2_872_463_000D)
					.withInitialOrbitalAngle(314.06D)
					.withRotationalPeriod(-62_064)
					.withColor(0.4F, 0.6F, 0.8F)
					.withAxialTilt(97.77F)
					.withProcessingLevel(3)
					.withRings(97.77F, 2.25F, 0.55F, 0.72F, 0.82F)
					.withTraits(
						new CBT_Atmosphere(Fluids.HYDROGEN, 82D)
							.and(Fluids.HELIUM4, 15D)
							.and(Fluids.GAS, 3D), // methane
						new CBT_Temperature(-224)
					)
					.withSatellites(

						//Miranda
						new CelestialBody("miranda")
							.withMassRadius(6.59e19F, 235)
							.withSemiMajorAxis(129_390D)
							.withRotationalPeriod(122_146)
							.withTidalLockingTo("uranus"),

						//Titania
						new CelestialBody("titania")
							.withMassRadius(3.527e21F, 788)
							.withSemiMajorAxis(435_910D)
							.withRotationalPeriod(752_198)
							.withTidalLockingTo("uranus"),

						//Oberon
						new CelestialBody("oberon")
							.withMassRadius(3.01e21F, 761)
							.withSemiMajorAxis(583_520D)
							.withRotationalPeriod(1_164_322)
							.withTidalLockingTo("uranus"),

						//Ariel
						new CelestialBody("ariel")
							.withMassRadius(1.353e21F, 578)
							.withSemiMajorAxis(191_020D)
							.withRotationalPeriod(217_728)
							.withTidalLockingTo("uranus")

					),

				//neptune
				new CelestialBody("neptune", SpaceConfig.neptuneDimension, Body.NEPTUNE)
					.withBlockTextures(RefStrings.MODID + ":textures/blocks/water_still.png", RefStrings.MODID + ":textures/blocks/water_flowing.png")
					.withMassRadius(1.024e26F, 24_622)
					.withSemiMajorAxis(4_495_060_000D)
					.withInitialOrbitalAngle(304.35D)
					.withRotationalPeriod(57_996)
					.withColor(0.2F, 0.4F, 0.6F)
					.withAxialTilt(28.32F)
					.withProcessingLevel(3)
					.withTraits(
						new CBT_Atmosphere(Fluids.HYDROGEN, 80D)
							.and(Fluids.HELIUM4, 19D)
							.and(Fluids.GAS, 1D), // methane
						new CBT_Temperature(-214)
					)
					.withSatellites(

						//Triton
						new CelestialBody("triton")
							.withMassRadius(2.14e22F, 1_353)
							.withSemiMajorAxis(354_759D)
							.withRotationalPeriod(-507_773) // retrograde + tidally locked
							.withTidalLockingTo("neptune"),

						//Proteus
						new CelestialBody("proteus")
							.withMassRadius(4.4e19F, 210)
							.withSemiMajorAxis(117_647D)
							.withRotationalPeriod(96_426)
							.withTidalLockingTo("neptune"),

						//Nereid
						new CelestialBody("nereid")
							.withMassRadius(3.1e19F, 170)
							.withSemiMajorAxis(5_513_818D)
							.withRotationalPeriod(41_067) // NOT tidally locked?
							//.withTidalLockingTo("neptune")



					),

				//Pluto
				new CelestialBody("eeloo")
					//todo at some point just go through and change all the annoying KSP names to be correct
					// but god only knows how many fucking times in this code it's referenced
					.withMassRadius(1.309e22F, 1_188)
					.withSemiMajorAxis(5_906_380_000D)
					.withInitialOrbitalAngle(238.93D)
					.withRotationalPeriod(-551_857) // retrograde
					.withAxialTilt(122.53F)
					.withTraits(new CBT_Temperature(-229))
					.withSatellites(
						//Charon
						new CelestialBody("charon")
							.withMassRadius(1.586e21F, 606)
							.withSemiMajorAxis(19_596D)
							.withRotationalPeriod(551_857) // tidally locked
							.withTidalLockingTo("eeloo") //tidal locking to pluto

					)


			);

		runTests();
	}

	// Simple enum used for blocks and items
	public enum Body {
		ORBIT(""),
		KERBIN("kerbin"),
		MUN("mun"),
		//MINMUS("minmus"),
		DUNA("duna"),
		MOHO("moho"),
		DRES("dres"),
		EVE("eve"),
		IKE("ike"),
		LAYTHE("laythe"),
		SUN("kerbol"), //God I really need to change this to real names
		JOOL("jool"), //pain
		SARNUS("sarnus"),
		URANUS("uranus"),
		NEPTUNE("neptune");
		// TEKTO("tekto");

		public String name;

		Body(String name) {
			this.name = name;
		}

		// memoising, since ore rendering would be horrendous otherwise
		private CelestialBody body;
		public CelestialBody getBody() {
			if(this == ORBIT)
				return null;

			if(body == null)
				body = CelestialBody.getBody(name);

			return body;
		}

		public int getProcessingLevel() {
			if(this == ORBIT) return 0;
			return getBody().processingLevel;
		}

		public ResourceLocation getStoneTexture() {
			if(this == ORBIT) return null;
			return getBody().stoneTexture;
		}

		public int getDimensionId() {
			if(this == ORBIT) return SpaceConfig.orbitDimension;
			return getBody().dimensionId;
		}
	}

	public static class AstroMetric {

		// Convert a solar system into a set of metrics defining their position and size in the sky for a given body

		public double distance;
		public double angle;
		public double apparentSize;
		public double phase;

		protected Vec3 position;

		public CelestialBody body;

		public AstroMetric(CelestialBody body, Vec3 position) {
			this.body = body;
			this.position = position;
		}

	}

	/**
	 * Celestial mechanics
	 */

	// Create an ordered list for rendering all bodies within the system, minus the parent star
	public static List<AstroMetric> calculateMetricsFromBody(World world, float partialTicks, double longitude, CelestialBody body) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		// You know not the horrors I have suffered through, in order to fix tidal locking
		double offset = (double)body.getRotationalPeriod() * (longitude / 360.0);

		double ticks = (getCelestialTicks(world, partialTicks) + offset) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, body.getStar(), ticks);

		// Get the metrics from a given body
		calculateMetricsFromBody(metrics, body);

		// Sort by increasing distance
		metrics.sort(
			Comparator.comparingDouble(a -> -a.distance)
		);

		return metrics;
	}

	public static List<AstroMetric> calculateMetricsFromSatellite(
		World world,
		float partialTicks,
		CelestialBody orbiting,
		double altitude
	) {

		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks =
			getCelestialTicks(world, partialTicks)
				* (double)AstronomyUtil.TIME_MULTIPLIER;

		// Determine root body
		CelestialBody root =
			orbiting.parent == null
				? orbiting
				: orbiting.getStar();

		// Get coordinates of all bodies
		calculatePositionsRecursive(
			metrics,
			null,
			root,
			ticks
		);

		// Observer position (station orbit)
		Vec3 position;

		// Orbiting the root star
		if(orbiting.parent == null) {

			position = calculatePosition(
				orbiting,
				altitude,
				ticks
			);

		} else {

			position = calculatePosition(
				orbiting,
				altitude,
				ticks
			);

			// offset by body's real position
			for(AstroMetric metric : metrics) {

				if(metric.body == orbiting) {

					position =
						position.addVector(
							metric.position.xCoord,
							metric.position.yCoord,
							metric.position.zCoord
						);

					break;
				}
			}
		}

		// Convert all bodies to observer-relative metrics
		calculateMetricsFromPosition(
			metrics,
			position
		);

		// Sort farthest first
		metrics.sort((a, b) ->
						 Double.compare(
							 b.distance,
							 a.distance
						 )
		);

		return metrics;
	}

	public static List<AstroMetric> calculateMetricsBetweenSatelliteOrbits(World world, float partialTicks, CelestialBody from, CelestialBody to, double fromAltitude, double toAltitude, double t) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks = getCelestialTicks(world, partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		//calculatePositionsRecursive(metrics, null, from.getStar(), ticks);
		calculatePositionsRecursive(
			metrics,
			null,
			from.parent == null ? from : from.getStar(),
			ticks
		);

		// Add our orbiting satellite position
		Vec3 fromPos = calculatePosition(from, fromAltitude, ticks);
		Vec3 toPos = calculatePosition(to, toAltitude, ticks);
		for(AstroMetric metric : metrics) {
			if(metric.body == from) {
				fromPos = fromPos.addVector(metric.position.xCoord, metric.position.yCoord, metric.position.zCoord);
			}
			if(metric.body == to) {
				toPos = toPos.addVector(metric.position.xCoord, metric.position.yCoord, metric.position.zCoord);
			}
		}

		// Lerp smoothly between the two positions (maybe a fancy circular lerp somehow?)
		Vec3 position = lerp(fromPos, toPos, t);

		// Get the metrics from the orbiting position
		calculateMetricsFromPosition(metrics, position);

		// Sort by increasing distance
		metrics.sort((a, b) -> {
			return (int)(b.distance - a.distance);
		});

		return metrics;
	}

	public static double calculateDistanceBetweenTwoBodies(World world, CelestialBody from, CelestialBody to) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks = getCelestialTicks(world, 0.0F) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, from.getStar(), ticks);

		Vec3 fromPos = Vec3.createVectorHelper(0, 0, 0);
		Vec3 toPos = Vec3.createVectorHelper(0, 0, 0);
		for(AstroMetric metric : metrics) {
			if(metric.body == from) fromPos = metric.position;
			if(metric.body == to) toPos = metric.position;
		}

		return fromPos.distanceTo(toPos);
	}

	private static Vec3 lerp(Vec3 from, Vec3 to, double t) {
		double x = BobMathUtil.clampedLerp(from.xCoord, to.xCoord, t);
		double y = BobMathUtil.clampedLerp(from.yCoord, to.yCoord, t);
		double z = BobMathUtil.clampedLerp(from.zCoord, to.zCoord, t);

		return Vec3.createVectorHelper(x, y, z);
	}

	public static double getCelestialTicks(World world, float partialTicks) {
		return (double)WorldProviderCelestial.getMasterWorldTime(world) + partialTicks;
	}

	// Recursively calculate the XYZ position of all planets from polar coordinates + time
	private static void calculatePositionsRecursive(List<AstroMetric> metrics, AstroMetric parentMetric, CelestialBody body, double ticks) {
		Vec3 parentPosition = parentMetric != null ? parentMetric.position : Vec3.createVectorHelper(0, 0, 0);

		for(CelestialBody satellite : body.satellites) {
			Vec3 position = calculatePosition(satellite, ticks).addVector(parentPosition.xCoord, parentPosition.yCoord, parentPosition.zCoord);
			AstroMetric metric = new AstroMetric(satellite, position);

			metrics.add(metric);

			calculatePositionsRecursive(metrics, metric, satellite, ticks);
		}
	}

	// Calculates the position of the body around its parent
	private static Vec3 calculatePosition(CelestialBody body, double ticks) {
		// Get how far (in radians) a planet has gone around its parent.
		double yearTicks = CelestialBody.secondsToVanillaTicks(body.getOrbitalPeriod());
		double angleRadians = 2 * Math.PI * (ticks / yearTicks) + Math.toRadians(body.initialOrbitalAngle);

		double x = body.semiMajorAxisKm * Math.cos(angleRadians);
		double y = body.semiMajorAxisKm * Math.sin(angleRadians);

		return Vec3.createVectorHelper(x, y, 0);
	}

	// Same but for an arbitrary satellite around a body
	public static Vec3 calculatePosition(CelestialBody body, double altitude, double ticks) {
		double orbitalRadiusMeters =
			(body.radiusKm + altitude) * 1000.0;

		double orbitalPeriod =
			2 * Math.PI *
				Math.sqrt(
					(orbitalRadiusMeters * orbitalRadiusMeters * orbitalRadiusMeters) /
						(AstronomyUtil.GRAVITATIONAL_CONSTANT * body.massKg)
				);
		double orbitTicks = CelestialBody.secondsToVanillaTicks(orbitalPeriod);
		double angleRadians = 2 * Math.PI * (ticks / orbitTicks);

		double x = (body.radiusKm + altitude)
			* Math.cos(angleRadians);
		double y = (body.radiusKm + altitude)
			* Math.sin(angleRadians);

		return Vec3.createVectorHelper(x, y, 0);
	}

	// Calculates the metrics for a given body in the system
	private static void calculateMetricsFromBody(List<AstroMetric> metrics, CelestialBody body) {

		AstroMetric from = null;

		for(AstroMetric metric : metrics) {
			if(metric.body == body) {
				from = metric;
				break;
			}
		}

		// We are on the system star (Kerbol/Sun)
		// The star itself isn't in metrics because only satellites are added
		if(from == null) {

			Vec3 origin = Vec3.createVectorHelper(0, 0, 0);

			for(AstroMetric to : metrics) {
				calculateMetric(to, origin);
			}

			return;
		}

		for(AstroMetric to : metrics) {
			if(from == to)
				continue;

			calculateMetric(to, from.position);
		}
	}

	private static void calculateMetricsFromPosition(List<AstroMetric> metrics, Vec3 position) {
		for(AstroMetric to : metrics) {
			calculateMetric(to, position);
		}
	}

	private static void calculateMetric(
		AstroMetric metric,
		Vec3 position
	) {

		// relative vector observer -> body
		Vec3 relative =
			Vec3.createVectorHelper(
				metric.position.xCoord - position.xCoord,
				metric.position.yCoord - position.yCoord,
				metric.position.zCoord - position.zCoord
			);

		// distance for sorting
		metric.distance =
			relative.lengthVector();

		// apparent size
		metric.apparentSize =
			getApparentSize(
				metric.body.radiusKm,
				metric.distance
			);

		// angle relative to observer
		metric.angle =
			getApparentAngleDegrees(
				Vec3.createVectorHelper(0,0,0),
				relative
			);

		// ---------- SOLAR OCCLUSION FIX ----------
		// if a planet is visually behind the Sun,
		// make it effectively invisible

		if(metric.body.parent != null) {

			Vec3 toSun =
				Vec3.createVectorHelper(
					-position.xCoord,
					-position.yCoord,
					-position.zCoord
				).normalize();

			Vec3 toPlanet =
				relative.normalize();

			double alignment =
				toSun.dotProduct(toPlanet);

			// nearly same direction as sun
			if(alignment > 0.9995D) {

				double sunDistance =
					position.lengthVector();

				double sunAngularRadius =
					Math.atan(
						SolarSystem.kerbol.radiusKm /
							sunDistance
					);

				double planetAngularOffset =
					Math.acos(
						MathHelper.clamp_double(
							alignment,
							-1D,
							1D
						)
					);

				// behind solar disc
				if(planetAngularOffset <
					sunAngularRadius) {
					//System.out.println(
					//	"OCCLUDED: " +
					//		metric.body.name
					//);

					metric.apparentSize = 0;
				}
			}
		}

		// phase calculation
		Vec3 toSun =
			Vec3.createVectorHelper(
				-metric.position.xCoord,
				-metric.position.yCoord,
				-metric.position.zCoord
			);

		Vec3 toObserver =
			Vec3.createVectorHelper(
				position.xCoord - metric.position.xCoord,
				position.yCoord - metric.position.yCoord,
				position.zCoord - metric.position.zCoord
			);

		double dot =
			toSun.normalize().dotProduct(
				toObserver.normalize()
			);

		metric.phase = (1 - dot) * 0.5;
	}

	private static double getApparentSize(double radius, double distance) {
		// Apparent angular size in radians
		double angle = 2.0 * Math.atan(radius / distance);
		return angle * RENDER_SCALE;
	}

	private static double getApparentAngleDegrees(Vec3 from, Vec3 to) {
		double angleToOrigin = Math.atan2(-from.yCoord, -from.xCoord);
		double angleToTarget = Math.atan2(to.yCoord - from.yCoord, to.xCoord - from.xCoord);

		return MathHelper.wrapAngleTo180_double(Math.toDegrees(angleToOrigin - angleToTarget));
	}

	// Calculates how large to render the sun in the sky from a given vantage point
	public static double calculateSunSize(CelestialBody from) {

		// Orbiting the system star itself
		if(from.parent == null) {

			// fake observer altitude near photosphere
			double distance =
				from.radiusKm * 1.03D;

			return getApparentSize(
				from.radiusKm,
				distance
			) * SUN_RENDER_SCALE;
		}

		// orbiting a moon -> recurse upward
		if(from.parent.parent != null) {
			return calculateSunSize(from.parent);
		}

		// orbiting a planet
		return getApparentSize(
			from.parent.radiusKm,
			from.semiMajorAxisKm
		) * SUN_RENDER_SCALE;
	}

	// Gets angle for a single planet, good for locking tidal bodies
	public static double calculateSingleAngle(World world, float partialTicks, CelestialBody from, CelestialBody to) {

		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks =
			getCelestialTicks(world, partialTicks)
				* (double) AstronomyUtil.TIME_MULTIPLIER;

		// Start from system root
		CelestialBody root =
			from.parent == null ? from : from.getStar();

		Vec3 rootPos = Vec3.createVectorHelper(0, 0, 0);
		metrics.add(new AstroMetric(root, rootPos));
		calculatePositionsRecursive(metrics, null, root, ticks);

		Vec3 fromPos = Vec3.createVectorHelper(0, 0, 0);
		Vec3 toPos = Vec3.createVectorHelper(0, 0, 0);

		boolean foundFrom = false;
		boolean foundTo = false;

		// Sun/root body lives at origin
		if(from.parent == null) {
			foundFrom = true;
		}

		if(to.parent == null) {
			foundTo = true;
		}

		for(AstroMetric metric : metrics) {

			if(metric.body == from) {
				fromPos = metric.position;
				foundFrom = true;
			}

			if(metric.body == to) {
				toPos = metric.position;
				foundTo = true;
			}
		}

		if(!foundFrom || !foundTo) {
			throw new IllegalStateException(
				"Missing celestial metric! from=" +
					(from != null ? from.name : "null") +
					" to=" +
					(to != null ? to.name : "null") +
					" foundFrom=" + foundFrom +
					" foundTo=" + foundTo
			);
		}

		return getApparentAngleDegrees(fromPos, toPos);
	}

	public static double calculateSingleAngle(World world, float partialTicks, CelestialBody orbiting, double altitude) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks = getCelestialTicks(world, partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, orbiting.getStar(), ticks);

		// Add our orbiting satellite position
		Vec3 from = calculatePosition(orbiting, altitude, ticks);
		Vec3 to = Vec3.createVectorHelper(0, 0, 0);
		for(AstroMetric metric : metrics) {
			if(metric.body == orbiting) {
				to = metric.position;
				from = from.addVector(to.xCoord, to.yCoord, to.zCoord);
				break;
			}
		}

		return getApparentAngleDegrees(from, to);
	}


	/**
	 * Delta-V Calcs
	 */

	// Get a number of buckets of fuel required to travel somewhere, (halved, since we're assuming bipropellant)
	public static int getCostBetween(CelestialBody from, CelestialBody to, int mass, int thrust, int isp, boolean fromOrbit, boolean toOrbit) {
		double fromDrag = getAtmosphericDrag(from.getTrait(CBT_Atmosphere.class));
		double toDrag = getAtmosphericDrag(to.getTrait(CBT_Atmosphere.class));

		// Scale deltaV to gameplay-friendly numbers
		double launchDV = fromOrbit ? 0 : SolarSystem.getLiftoffDeltaV(from, mass, thrust, fromDrag) * 0.4;
		double travelDV = SolarSystem.getDeltaVBetween(from, to) * 0.4;
		double landerDV = toOrbit ? 0 : SolarSystem.getLandingDeltaV(to, mass, thrust, toDrag) * 0.4;

		double totalDV = launchDV + travelDV + landerDV;
		totalDV = Math.min(totalDV, 1000000); // cap insane transfers to 1M

		return getFuelCost(totalDV, mass, isp);
	}

	public static int getFuelCost(double deltaV, int mass, int isp) {
		// Get the fraction of the rocket that must be fuel in order to achieve the deltaV
		double g0 = 9.81;
		//our theory of gravity is wrong so fuck you it is not 9.81
		double exhaustVelocity = isp * g0;
		double massFraction = 1 - Math.exp(-(deltaV / exhaustVelocity));

		// Get the mass of a rocket that has that fraction, and the mass of the propellant
		double totalMass = mass / (1 - massFraction);
		double propellantMass = totalMass - mass;
		double propellantVolume = propellantMass / 2; // two propellants

		return propellantVolume + 100 > Integer.MAX_VALUE ? Integer.MAX_VALUE : MathHelper.ceiling_double_int(propellantVolume * 0.008D) * 100;
		//cocking spaniel le ebin fuel costs are in trump tarrif territories ebin :DDD
		//but jesse you cant just do that you have to align yourself to my arbitrary made up bullshit space game!
		//i dont care walter white yo im gonna fucking do it my way
		//ok so less is more fuel efficient, because I tried 0.15 and that increased the fuel costs to a million
		//however going down to like 0.00001D made fuel costs go to heckin 100mb for a moon trip
	}

	private static double getAtmosphericDrag(CBT_Atmosphere atmosphere) {
		if(atmosphere == null) return 0;
		double pressure = atmosphere.getPressure();
		return Math.log(pressure + 1.0D) / 10.0D;
	}

	// Provides the deltaV required to get into orbit, ignoring losses due to atmospheric friction
	// Make sure to convert from kN to N (kilonewtons to newtons) before calling these two functions
	public static double getLiftoffDeltaV(CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag) {
		return calculateSurfaceToOrbitDeltaV(body, craftMassKg, craftThrustN, atmosphericDrag, false);
	}

	// Uses aerobraking if an atmosphere is present
	public static double getLandingDeltaV(CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag) {
		return calculateSurfaceToOrbitDeltaV(body, craftMassKg, craftThrustN, atmosphericDrag, atmosphericDrag > 0.006);
	}

	private static double calculateSurfaceToOrbitDeltaV(CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag, boolean lossesOnly) {
		float gravity = body.getSurfaceGravity();
		double orbitalDeltaV = Math.sqrt((AstronomyUtil.GRAVITATIONAL_CONSTANT * body.massKg) / (body.radiusKm * 1_000));
		double thrustToWeightRatio = craftThrustN / (craftMassKg * gravity);

		if(thrustToWeightRatio < 1)
			return Double.MAX_VALUE;

		// We have to find out how long the burn will take to get our "gravity tax"
		// Shorter burns have less gravity losses, meaning higher thrust is desirable
		double acceleration = (thrustToWeightRatio - 1) * gravity;
		double timeToOrbit = orbitalDeltaV / acceleration;
		double gravityLosses = gravity * timeToOrbit * 1.4; // 1.2–1.5 km/s losses est

		if(lossesOnly)
			return gravityLosses * (1 - atmosphericDrag); // drag helps on the way down

		return orbitalDeltaV + gravityLosses * (1 + atmosphericDrag); // and hinders on the way up
	}

	// Provides the deltaV required to transfer from the orbit of one body to the orbit of another
	// Does not currently support travelling to the main body (Sol)
	// Our structure doesn't currently require this, but if it does, go annoy Mellow to add it lmao
	public static double getDeltaVBetween(CelestialBody start, CelestialBody end) {
		return calculateHohmannTransfer(start, end);
	}

	// This calculates the entire transfer cost, adding together the cost of two burns
	private static double calculateHohmannTransfer(CelestialBody start, CelestialBody end) {
		if(start == end) {
			// Transfer to self, ignore

			return 0;
		} else if (start.parent == null || end.parent == null) {
			// One of the bodies is a solar body (root)

			CelestialBody solar = (start.parent == null) ? start : end;
			CelestialBody other = (start.parent == null) ? end : start;

			boolean fromSolar = (start.parent == null);

			double solarOrbitRadius = solar.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM;

			if (!fromSolar) {
				// Traveling TO the sun: treat as direct fall / capture trajectory
				// No need for interplanetary transfer complexity
				return calculateSingleHohmannTransfer(
					other.parent.massKg,
					other.semiMajorAxisKm,
					solarOrbitRadius,
					other.massKg,
					other.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM
				);
			} else {
				// Traveling FROM the sun: require extreme capability

				// "realistic bullshit gate"
				if (!AstronomyUtil.canEscapeSolarGravity(Double.MAX_VALUE, solar)) {
					return Double.POSITIVE_INFINITY; // effectively impossible without special tech
				}

				// escape burn from solar surface orbit to target orbit
				double escapeBurn = calculateSingleHohmannTransfer(
					solar.massKg,
					solarOrbitRadius,
					other.semiMajorAxisKm,
					solar.massKg,
					solarOrbitRadius
				);

				// then standard insertion around destination body
				double insertionBurn = calculateSingleHohmannTransfer(
					other.parent.massKg,
					other.semiMajorAxisKm,
					solarOrbitRadius,
					other.massKg,
					other.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM
				);

				// make it intentionally punishing but still physically grounded
				return escapeBurn * 1.5 + insertionBurn;
			}
		} else if(start.parent == end.parent) {
			// Intersystem transfer

			double firstBurnCost = calculateSingleHohmannTransfer(start.parent.massKg, start.semiMajorAxisKm, end.semiMajorAxisKm, start.massKg, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);
			double secondBurnCost = calculateSingleHohmannTransfer(start.parent.massKg, end.semiMajorAxisKm, start.semiMajorAxisKm, end.massKg, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);

			return firstBurnCost + secondBurnCost;
		} else if (start == end.parent) {
			// Transferring from parent body to moon

			double firstBurnCost = calculateSingleHohmannTransfer(start.massKg, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, end.semiMajorAxisKm);
			double secondBurnCost = calculateSingleHohmannTransfer(start.massKg, end.semiMajorAxisKm, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, end.massKg, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);

			return firstBurnCost + secondBurnCost;
		} else if(start.parent == end) {
			// Transferring from moon to parent body

			double firstBurnCost = calculateSingleHohmannTransfer(end.massKg, start.semiMajorAxisKm, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, start.massKg, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);
			double secondBurnCost = calculateSingleHohmannTransfer(end.massKg, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, start.semiMajorAxisKm);

			return firstBurnCost + secondBurnCost;
		} else {
			// Complex transfer (moon -> moon, moon -> other planet)

			CelestialBody commonParent = getCommonParent(start, end);
			CelestialBody fromBody = start;
			CelestialBody toBody = end;
			double currentFromOrbitRadius = fromBody.semiMajorAxisKm;
			double currentToOrbitRadius = toBody.semiMajorAxisKm;

			double burnCost = 0;

			// Go up the tree from start
			while(fromBody.parent != commonParent) {
				burnCost += calculateSingleHohmannTransfer(fromBody.parent.massKg, fromBody.semiMajorAxisKm, fromBody.semiMajorAxisKm, fromBody.massKg, fromBody.semiMajorAxisKm);
				currentFromOrbitRadius = fromBody.semiMajorAxisKm;
				fromBody = fromBody.parent;
			}

			// Go up the tree from end
			while(toBody.parent != commonParent) {
				burnCost += calculateSingleHohmannTransfer(toBody.parent.massKg, toBody.semiMajorAxisKm, toBody.semiMajorAxisKm, toBody.massKg, toBody.semiMajorAxisKm);
				currentToOrbitRadius = toBody.semiMajorAxisKm;
				toBody = toBody.parent;
			}

			// Transfer interplanetary
			burnCost += calculateSingleHohmannTransfer(commonParent.massKg, fromBody.semiMajorAxisKm, toBody.semiMajorAxisKm, fromBody.massKg, currentFromOrbitRadius);
			burnCost += calculateSingleHohmannTransfer(commonParent.massKg, toBody.semiMajorAxisKm, fromBody.semiMajorAxisKm, toBody.massKg, currentToOrbitRadius);

			return burnCost;
		}
	}

	private static CelestialBody getCommonParent(CelestialBody start, CelestialBody end) {
		CelestialBody startParent = start.parent;

		while(startParent != null) {
			CelestialBody endParent = end.parent;
			while(endParent != null) {
				if(startParent == endParent)
					return startParent;

				endParent = endParent.parent;
			}
			startParent = startParent.parent;
		}

		throw new InvalidParameterException("Bodies aren't in the same solar system");
	}


	// All transfer math is commutative, injection burn (getting onto the transfer orbit) takes the exact same dV as
	// the insertion burn (entering the target orbit)

	// Calculate orbit to orbit transfer around a parent body, without any need to escape an inner gravity well.
	// This is used to transfer from low orbit to a moon.
	private static double calculateSingleHohmannTransfer(float parentMassKg, double fromRadiusKm, double toRadiusKm) {
		double parentGravitationalParameter = parentMassKg * AstronomyUtil.GRAVITATIONAL_CONSTANT;

		// We're finding the dv to transfer between these circular orbits
		double startOrbitalRadius = fromRadiusKm * 1_000;
		double endOrbitalRadius = toRadiusKm * 1_000;

		// The semimajor axis of the transfer orbit (average distance of orbit)
		double transferSemiMajorAxis = (startOrbitalRadius + endOrbitalRadius) / 2;

		// Our current orbital velocity around the parent (planet orbital velocity)
		double currentOrbitalVelocity = Math.sqrt(parentGravitationalParameter / startOrbitalRadius);

		// The velocity we need to get onto our transfer orbit
		double requiredVelocity = Math.sqrt(parentGravitationalParameter * ((2 / startOrbitalRadius) - (1 / transferSemiMajorAxis)));

		// The true velocity we need to add to get onto our transfer orbit (desired energy minus the energy we already have)
		return Math.abs(requiredVelocity - currentOrbitalVelocity);
	}

	// Calculate orbit to orbit transfer around a parent body, escaping from a well.
	// This is used for interplanetary transfers.
	private static double calculateSingleHohmannTransfer(float parentMassKg, double fromRadiusKm, double toRadiusKm, float fromMassKg, double parkingOrbitRadiusKm) {
		// First we get our required velocity change ignoring the body we're currently orbiting
		double hyperbolicVelocity = calculateSingleHohmannTransfer(parentMassKg, fromRadiusKm, toRadiusKm);

		double fromGravitationalParameter = fromMassKg * AstronomyUtil.GRAVITATIONAL_CONSTANT;

		// Our current orbital radius and velocity around the starting body
		double parkingOrbitRadius = parkingOrbitRadiusKm * 1_000;
		double parkingOrbitVelocity = Math.sqrt(fromGravitationalParameter / parkingOrbitRadius);

		// The amount of energy needed to escape the start body to get onto our transfer orbit
		double escapeVelocity = Math.sqrt((2 * fromGravitationalParameter) / parkingOrbitRadius);
		double escapeHyperVelocity = Math.sqrt(hyperbolicVelocity * hyperbolicVelocity + escapeVelocity * escapeVelocity);

		// The first half of the required dV to get to our destination!
		return escapeHyperVelocity - parkingOrbitVelocity;
	}

	public static void runTests() {
		CelestialBody kerbin = CelestialBody.getBody("kerbin");
		CelestialBody eve = CelestialBody.getBody("eve");
		CelestialBody duna = CelestialBody.getBody("duna");
		CelestialBody mun = CelestialBody.getBody("mun");
		//CelestialBody minmus = CelestialBody.getBody("minmus");
		//sorry lil bro ur not real
		CelestialBody ike = CelestialBody.getBody("ike");

		float deltaIVMass = 500_000;
		float RD180RocketThrust = 7_887 * 1_000;

		MainRegistry.logger.info("Kerbin launch cost: " + getLiftoffDeltaV(kerbin, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Eve launch cost: " + getLiftoffDeltaV(eve, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Duna launch cost: " + getLiftoffDeltaV(duna, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Mun launch cost: " + getLiftoffDeltaV(mun, deltaIVMass, RD180RocketThrust, 0));
		//MainRegistry.logger.info("Minmus launch cost: " + getLiftoffDeltaV(minmus, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Ike launch cost: " + getLiftoffDeltaV(ike, deltaIVMass, RD180RocketThrust, 0));

		MainRegistry.logger.info("Kerbin -> Eve cost: " + getDeltaVBetween(kerbin, eve) + " - should be: " + (950+90+80+1330));
		MainRegistry.logger.info("Kerbin -> Duna cost: " + getDeltaVBetween(kerbin, duna) + " - should be: " + (950+130+250+360));
		MainRegistry.logger.info("Kerbin -> Ike cost: " + getDeltaVBetween(kerbin, ike) + " - should be: " + (950+130+250+30+180));
		MainRegistry.logger.info("Eve -> Duna cost: " + getDeltaVBetween(eve, duna));
		MainRegistry.logger.info("Kerbin -> Mun cost: " + getDeltaVBetween(kerbin, mun) + " - should be: " + (860+310));
		//MainRegistry.logger.info("Kerbin -> Minmus cost: " + getDeltaVBetween(kerbin, minmus) + " - should be: " + (930+160));
		MainRegistry.logger.info("Mun -> Kerbin cost: " + getDeltaVBetween(mun, kerbin) + " - should be: " + (860+310));
		//MainRegistry.logger.info("Minmus -> Kerbin cost: " + getDeltaVBetween(minmus, kerbin) + " - should be: " + (930+160));
		//MainRegistry.logger.info("Minmus -> Ike cost: " + getDeltaVBetween(minmus, ike));

		MainRegistry.logger.info("Kerbin orbital period: " + kerbin.getOrbitalPeriod() + " - should be: " + 426);
		MainRegistry.logger.info("Eve orbital period: " + eve.getOrbitalPeriod() + " - should be: " + 261);
		MainRegistry.logger.info("Mun orbital period: " + mun.getOrbitalPeriod() + " - should be: " + 6);
	}

}
