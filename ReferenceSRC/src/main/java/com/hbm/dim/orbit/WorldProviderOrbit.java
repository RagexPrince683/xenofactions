package com.hbm.dim.orbit;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_Destroyed;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.lib.Library;
import com.hbm.potion.HbmPotion;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.BobMathUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldProviderOrbit extends WorldProvider {

	// How fast orbital simulation runs relative to real seconds
	//unused
	//public static final double ORBIT_SCALE = 1.0 / 100000.0;

	//todo spawn our FRIEND here when there's an atmosphere, it's breathable, and there's a door. then our friend can KNOCK.

	// Orbit at an altitude that provides an hour-long realtime orbit (game time is fast so we go slow)
	// We want a consistent orbital period to prevent orbiting too slow or fast (both for player comfort and feel)
	//private static final double ORBIT_PERIOD_SECONDS = AstronomyUtil.SECONDS_IN_MC_DAY * 5; // 5 MC days per orbit
	private static final double ORBIT_PERIOD_SECONDS = 60 * 60 * 2; // 2 real hours per orbit

	protected float getOrbitalAltitude(CelestialBody body) {

		if(body.parent == null) {
			// stars
			return (float)(body.radiusKm * 0.03D);
		}

		// planets/moons
		return (float)(body.radiusKm * 0.08D);
	}

	// r = ∛[(G x Me x T2) / (4π2)]
	private double getAltitudeForPeriod(double massKg, double periodSeconds) {

		double G = AstronomyUtil.GRAVITATIONAL_CONSTANT;

		return Math.cbrt(
			(G * massKg * periodSeconds * periodSeconds)
				/ (4.0 * Math.PI * Math.PI)
		);
	}

	public float getSunPower() {

		CelestialBody body = OrbitalStation.clientStation.orbiting;

		double rAU = body.getPlanet().semiMajorAxisKm / AstronomyUtil.KM_IN_AU;

		return (float)(1.0 / (rAU * rAU));
	}

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(new BiomeGenOrbit(SpaceConfig.orbitBiome), dimensionId);
	}

	@Override
	public String getDimensionName() {
		return "Orbit";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderOrbit(this.worldObj);
	}

	private boolean playerHasAtmosphere(EntityPlayer player) {
		//checks if the player has an atmosphere for radiation effects
		CBT_Atmosphere atm = ChunkAtmosphereManager.proxy.getAtmosphere(
			player.worldObj,
			(int) player.posX, (int) player.posY, (int) player.posZ
		);

		if (atm == null || atm.fluids == null) return false;

		for (CBT_Atmosphere.FluidEntry entry : atm.fluids) {
			if (entry.pressure > 0.02) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void updateWeather() {
		super.updateWeather();

		// Apply radiation effect to players in orbit
		if (!worldObj.isRemote) {
			Random rand = new Random();

			for (Object obj : worldObj.playerEntities) {
				if (obj instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) obj;

					//todone when added cryochamber,
					// if not in cryo chamber, or riding rocket (drop pods, etc)
					//we won't need to do all that because if you're dumb enough to put
					// a fucking cryochamber in nil atmosphere you deserve the rads

					//todone more conditions like shielding, atmosphere

					if (playerHasAtmosphere(player)) {
						continue;
					}
					//works

					// Check if the player can see the sky
					if (worldObj.canBlockSeeTheSky((int) player.posX, (int) player.posY, (int) player.posZ)) {
						// Apply radiation effect with a random chance
						if (rand.nextInt(80) == 0) {
							player.addPotionEffect(new PotionEffect(HbmPotion.radiation.id, 100, 1));
						}
					}
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float x, float y) {
		return Vec3.createVectorHelper(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		return Vec3.createVectorHelper(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1) {
		// Stars look cool in orbit, but obvs at Moho we don't want the big fuckoff sun to not extinguish
		// Stars become visible during the day part of orbit just before Earth
		// And are fully visible during the day beyond the orbit of Duna (mars)
		float distanceStart = 9_000_000;
		float distanceEnd = 30_000_000;

		double progress = OrbitalStation.clientStation.getTransferProgress(par1);
		double semiMajorAxisKm = OrbitalStation.clientStation.orbiting.getPlanet().semiMajorAxisKm;
		if(progress > 0) {
			semiMajorAxisKm = (float)BobMathUtil.lerp(progress, semiMajorAxisKm, OrbitalStation.clientStation.target.getPlanet().semiMajorAxisKm);
		}

		float distanceFactor = MathHelper.clamp_float((float) ((semiMajorAxisKm - distanceStart) / (distanceEnd - distanceStart)), 0F, 1F);

		//float celestialAngle = worldObj.getCelestialAngle(par1);
		//float celestialPhase = (1 - (celestialAngle + 0.5F) % 1) * 2 - 1;
		//float starBrightness = (float)Library.smoothstep(Math.abs(celestialPhase), 0.6, 0.75);
		float angle = worldObj.getCelestialAngle(par1);

		// convert to night factor
		float night = Math.abs(angle - 0.5F) * 2.0F;
		night = MathHelper.clamp_float(night, 0F, 1F);

		// smooth curve
		float starBrightness = night * night;

		return MathHelper.clamp_float(starBrightness, distanceFactor, 1F);
	}

	@Override
	public float getSunBrightnessFactor(float partialTicks) {
		return getSunBrightness(partialTicks);
	}

	@Override
	public float getSunBrightness(float partialTicks) {
		OrbitalStation station = OrbitalStation.clientStation;
		CelestialBody orbiting = station.orbiting;

		if(orbiting.getStar().hasTrait(CBT_Destroyed.class))
			return 0;

		double ticks =
			SolarSystem.getCelestialTicks(worldObj, partialTicks)
				* AstronomyUtil.TIME_MULTIPLIER;

		double progress = station.getTransferProgress(partialTicks);
		Vec3 observer = getObserverPosition(station, progress, ticks);

		float solarPower = getSolarPower(observer);
		CelestialBody eclipsingBody = progress > 0.5D ? station.target : orbiting;

		if(isEclipsedBy(observer, eclipsingBody, ticks))
			return solarPower * 0.05F;

		return solarPower;
	}

	private Vec3 getObserverPosition(OrbitalStation station, double progress, double ticks) {
		Vec3 from = getSatellitePosition(station.orbiting, getOrbitalAltitude(station.orbiting), ticks);

		if(progress <= 0.0D)
			return from;

		Vec3 to = getSatellitePosition(station.target, getOrbitalAltitude(station.target), ticks);

		return Vec3.createVectorHelper(
			BobMathUtil.clampedLerp(from.xCoord, to.xCoord, progress),
			BobMathUtil.clampedLerp(from.yCoord, to.yCoord, progress),
			BobMathUtil.clampedLerp(from.zCoord, to.zCoord, progress)
		);
	}

	private Vec3 getSatellitePosition(CelestialBody body, double altitude, double ticks) {
		Vec3 bodyPosition = getBodyPosition(body, ticks);
		Vec3 localOrbit = SolarSystem.calculatePosition(body, altitude, ticks);

		return bodyPosition.addVector(localOrbit.xCoord, localOrbit.yCoord, localOrbit.zCoord);
	}

	private Vec3 getBodyPosition(CelestialBody body, double ticks) {
		if(body.parent == null)
			return Vec3.createVectorHelper(0, 0, 0);

		Vec3 parentPosition = getBodyPosition(body.parent, ticks);
		double yearTicks = CelestialBody.secondsToVanillaTicks(body.getOrbitalPeriod());
		double angleRadians = 2.0D * Math.PI * (ticks / yearTicks) + Math.toRadians(body.initialOrbitalAngle);

		return parentPosition.addVector(
			body.semiMajorAxisKm * Math.cos(angleRadians),
			body.semiMajorAxisKm * Math.sin(angleRadians),
			0
		);
	}

	private float getSolarPower(Vec3 observer) {
		double sunDistanceKm = observer.lengthVector();

		if(sunDistanceKm < 1.0D)
			return 1.0F;

		double distanceAU = sunDistanceKm / AstronomyUtil.KM_IN_AU;
		return MathHelper.clamp_float((float)(1.0D / (distanceAU * distanceAU)), 0F, 1F);
	}

	private boolean isEclipsedBy(Vec3 observer, CelestialBody body, double ticks) {
		if(body.parent == null)
			return false;

		Vec3 bodyPosition = getBodyPosition(body, ticks);
		Vec3 toSun = Vec3.createVectorHelper(-observer.xCoord, -observer.yCoord, -observer.zCoord);
		Vec3 toBody = Vec3.createVectorHelper(
			bodyPosition.xCoord - observer.xCoord,
			bodyPosition.yCoord - observer.yCoord,
			bodyPosition.zCoord - observer.zCoord
		);

		double sunDistance = toSun.lengthVector();
		double bodyDistance = toBody.lengthVector();

		if(sunDistance < 1.0D || bodyDistance < 1.0D || bodyDistance >= sunDistance)
			return false;

		double alignment = toSun.normalize().dotProduct(toBody.normalize());
		double angularSeparation = Math.acos(MathHelper.clamp_double(alignment, -1.0D, 1.0D));
		double sunAngularRadius = Math.atan(SolarSystem.kerbol.radiusKm / sunDistance);
		double bodyAngularRadius = Math.atan(body.radiusKm / bodyDistance);

		return angularSeparation < bodyAngularRadius + sunAngularRadius;
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		return -99999;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderOrbit();
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		CelestialBody orbiting = OrbitalStation.clientStation.orbiting;
		CelestialBody target = OrbitalStation.clientStation.target;
		double progress = OrbitalStation.clientStation.getTransferProgress(partialTicks);
		float angle = (float)SolarSystem.calculateSingleAngle(worldObj, partialTicks, orbiting, getOrbitalAltitude(orbiting));
		if(progress > 0) {
			angle = (float)BobMathUtil.lerp(progress, angle, (float)SolarSystem.calculateSingleAngle(worldObj, partialTicks, target, getOrbitalAltitude(target)));
		}
		return (float)(angle / 360.0);
	}

	// Same shit as in Celestial
	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		ChunkCoordinates coords = player.getBedLocation(dimensionId);

		// If no bed, respawn in overworld
		if(coords == null)
			return 0;

		// If the bed location has no breathable atmosphere, respawn in overworld
		CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(worldObj, coords.posX, coords.posY, coords.posZ);
		if(!ChunkAtmosphereManager.proxy.canBreathe(atmosphere))
			return 0;

		return dimensionId;
	}

	@Override
	public boolean canRespawnHere() {
		if(WorldProviderCelestial.attemptingSleep) {
			WorldProviderCelestial.attemptingSleep = false;
			return true;
		}

		return false;
	}

	private static ArrayList<WeightedRandomFishable> plushie;

	private ArrayList<WeightedRandomFishable> getPlushie() {
		if(plushie == null) {
			plushie = new ArrayList<>();
			plushie.add(new WeightedRandomFishable(new ItemStack(Blocks.air, 1, 1), 100));
			//DIE
		}
//
		return plushie;
	}

	/// FISH ///
	public ArrayList<WeightedRandomFishable> getFish() {
		return getPlushie();
	}
	//
	public ArrayList<WeightedRandomFishable> getJunk() {
		return getPlushie();
	}
	//
	public ArrayList<WeightedRandomFishable> getTreasure() {
		return getPlushie();
	}

}
