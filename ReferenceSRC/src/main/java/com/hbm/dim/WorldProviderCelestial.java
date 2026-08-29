package com.hbm.dim;

import java.util.ArrayList;

import com.hbm.config.GeneralConfig;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_Destroyed;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.inventory.fluid.Fluids;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.IRenderHandler;

public abstract class WorldProviderCelestial extends WorldProvider {

	private long syncedMasterTime = -1;
	private long clientMasterTimeSyncTick = -1;

	@Override
	public abstract void registerWorldChunkManager();

	// Ore gen will attempt to replace this block with ores
	public Block getStone() {
		return Blocks.stone;
	}

	public boolean hasLife() {
		return false;
	}

	public int getWaterOpacity() {
		return 3;
	}

	// Runs every tick, use it to decrement timers and run effects
	@Override
	public void updateWeather() {

		if(!worldObj.isRemote) {
			CelestialBodyWorldSavedData.get(this).markDirty();
		}

		CBT_Atmosphere atmosphere =
			CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		if(atmosphere != null && atmosphere.getPressure() > 0.5F) {
			super.updateWeather();
			return;
		}

		worldObj.getWorldInfo().setRainTime(0);
		worldObj.getWorldInfo().setRaining(false);
		worldObj.getWorldInfo().setThunderTime(0);
		worldObj.getWorldInfo().setThundering(false);
		worldObj.rainingStrength = 0.0F;
		worldObj.thunderingStrength = 0.0F;
	}

	// Can be overridden to provide fog changing events based on weather
	public float fogDensity() {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
		if(atmosphere == null) return 0;

		float pressure = (float)atmosphere.getPressure();

		if(pressure <= 2F) return 0;

		//planetfogdensity = etc etc bullshit

		return 0.002F; //todo temporary fix for whatever slop this codebase is, PLEASE WORK I SWEAR TO FUCK IF TS ISN'T IT IM GONNA BE MAD AS SHIT
		//AAAAAAAAAAAAAAAND OF COURSE THIS IS USED EVERYWHERE IN THE FUCKING CODE BASE I SWEAR TO FUCK JAMES WHAT IS THIS
		//IS IT SUPPOSED TO BE REALISTIC???
		//GOD FUCKING WEEPS AT THIS MISERABLE CODE
		//old method: pressure * pressure * 0.002F
		//pressure * pressure * 0.00002F * planetfogdensity
	}

	/**
	 * Read/write for weather data and anything else you wanna store that is per planet and not for every body
	 * the serialization function synchronizes weather data to the player
	 *
	 * provider-specific data is marked dirty from updateWeather; celestial time itself is derived from vanilla world time
	 */
	public void writeToNBT(NBTTagCompound nbt) {

	}

	public void readFromNBT(NBTTagCompound nbt) {

	}

	public void serialize(ByteBuf buf) {
		buf.writeLong(getMasterWorldTime());
	}

	public void deserialize(ByteBuf buf) {
		long time = buf.readLong();
		long currentTime = getMasterWorldTime();

		syncedMasterTime = time;
		if(worldObj != null) {
			clientMasterTimeSyncTick = worldObj.getTotalWorldTime();
		}

		if(Math.abs(time - currentTime) > 10) {
			super.setWorldTime(time);
		}
	}


	/**
	 * Override to modify the lightmap, return true if the lightmap is actually modified
	 * @param lightmap a 16x16 lightmap stored in a 256 value buffer
	 * @return whether or not the dynamic lightmap texture needs to be updated
	 */
	public boolean updateLightmap(int[] lightmap) {
		return false;
	}

	protected final int packColor(final int[] colors) {
		return packColor(colors[0], colors[1], colors[2]);
	}

	protected final int packColor(final int r, final int g, final int b) {
		return 255 << 24 | r << 16 | g << 8 | b;
	}

	protected final int[] unpackColor(final int color) {
		final int[] colors = new int[3];
		colors[0] = color >> 16 & 255;
		colors[1] = color >> 8 & 255;
		colors[2] = color & 255;
		return colors;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float celestialAngle, float y) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		// The cold hard vacuum of space
		if(atmosphere == null) return Vec3.createVectorHelper(0, 0, 0);

		float sun = MathHelper.clamp_float(MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F, 0.0F, 1.0F);

		float sunR = sun;
		float sunG = sun;
		float sunB = sun;

		if(!GeneralConfig.enableHardcoreDarkness) {
			sunR *= 0.94F;
			sunG *= 0.94F;
			sunB *= 0.91F;
		}

		float totalPressure = (float)atmosphere.getPressure();
		Vec3 color = Vec3.createVectorHelper(0, 0, 0);

		for(int i = 0; i < atmosphere.fluids.size(); i++) {
			FluidEntry entry = atmosphere.fluids.get(i);
			Vec3 fluidColor;


			if(entry.fluid == Fluids.EVEAIR) {
				fluidColor = Vec3.createVectorHelper(53F / 255F * sunR, 32F / 255F * sunG, 74F / 255F * sunB);
			} else if(entry.fluid == Fluids.DUNAAIR || entry.fluid == Fluids.CARBONDIOXIDE) {
				fluidColor = Vec3.createVectorHelper(212F / 255F * sunR, 112F / 255F * sunG, 78F / 255F * sunB);
			} else if(entry.fluid == Fluids.AIR || entry.fluid == Fluids.OXYGEN || entry.fluid == Fluids.NITROGEN) {
				// Default to regular ol' overworld
				//todo, food eating logic here except this is fucking rendering logic which is client side so get fucked past me
				fluidColor = Vec3.createVectorHelper(0.7529412F * sunR, 0.84705883F * sunG, 1.0F * sunB);
			} else {
				fluidColor = getColorFromHex(entry.fluid.getColor());
				fluidColor.xCoord *= sunR * 1.4F;
				fluidColor.yCoord *= sunG * 1.4F;
				fluidColor.zCoord *= sunB * 1.4F;
			}

			float percentage = (float)entry.pressure / totalPressure;
			color = Vec3.createVectorHelper(
				color.xCoord + fluidColor.xCoord * percentage,
				color.yCoord + fluidColor.yCoord * percentage,
				color.zCoord + fluidColor.zCoord * percentage
			);
			//no, venus doesn't even fucking look like that! It looks actually really clear despite the infinite pressure!
			//wait wtf??? this wasn't even affecting it wtf???
			//oh it was under this amazing.
			//added back in case it wasn't causing the issue
			//todo rework fog density vs pressure/color/fluid composition because it's all just wrong
		}

		// Add minimum fog colour, for night-time glow
		if(!GeneralConfig.enableHardcoreDarkness) {
			float nightDensity = MathHelper.clamp_float(totalPressure, 0.0F, 1.0F);
			color.xCoord += 0.06F * nightDensity;
			color.yCoord += 0.06F * nightDensity;
			color.zCoord += 0.09F * nightDensity;
		}

		// Fog intensity remains high to simulate a thin looking atmosphere on low pressure planets
		//NO DUMBASS THATS NOT EVEN HOW THAT WORKS LOOK AT VENUS
		float pressureFactor = MathHelper.clamp_float(totalPressure * 10.0F, 0.0F, 1.0F);
		color.xCoord *= pressureFactor;
		color.yCoord *= pressureFactor;
		color.zCoord *= pressureFactor;
		//didn't solve our problem, added back

		if(Minecraft.getMinecraft().renderViewEntity.posY > 10000) { //ten thousand meters is the edge of space, not 600. Yeah, no it's that bad.
			//this just renders the stars
			double curvature = MathHelper.clamp_float((1000.0F - (float)Minecraft.getMinecraft().renderViewEntity.posY) / 400.0F, 0.0F, 1.0F);
			color.xCoord *= curvature;
			color.zCoord *= curvature;
			color.yCoord *= curvature;
		}

		return color;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity camera, float partialTicks) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		// The cold hard vacuum of space
		if(atmosphere == null) return Vec3.createVectorHelper(0, 0, 0);

		float sun = this.getSunBrightnessFactor(1.0F);
		float totalPressure = (float)atmosphere.getPressure();
		Vec3 color = Vec3.createVectorHelper(0, 0, 0);

		for(int i = 0; i < atmosphere.fluids.size(); i++) {
			FluidEntry entry = atmosphere.fluids.get(i);
			Vec3 fluidColor;

			if(entry.fluid == Fluids.EVEAIR) {
				fluidColor = Vec3.createVectorHelper(230F / 255F * sun, 200F / 255F * sun, 50F / 255F * sun);
			} else if(entry.fluid == Fluids.DUNAAIR || entry.fluid == Fluids.CARBONDIOXIDE) {
				fluidColor = Vec3.createVectorHelper(212F / 255F * sun, 112F / 255F * sun, 78F / 255F * sun);
			} else if(entry.fluid == Fluids.AIR || entry.fluid == Fluids.OXYGEN || entry.fluid == Fluids.NITROGEN) {
				// Default to regular ol' overworld
				fluidColor = super.getSkyColor(camera, partialTicks);
			} else {
				fluidColor = getColorFromHex(entry.fluid.getColor());
				fluidColor.xCoord *= sun;
				fluidColor.yCoord *= sun;
				fluidColor.zCoord *= sun;
			}

			float percentage = (float)entry.pressure / totalPressure;
			color = Vec3.createVectorHelper(
				color.xCoord + fluidColor.xCoord * percentage,
				color.yCoord + fluidColor.yCoord * percentage,
				color.zCoord + fluidColor.zCoord * percentage
			);
		}

		// Lower pressure sky renders thinner
		float pressureFactor = MathHelper.clamp_float(totalPressure, 0.0F, 1.0F);
		color.xCoord *= pressureFactor;
		color.yCoord *= pressureFactor;
		color.zCoord *= pressureFactor;

		return color;
	}

	private Vec3 getColorFromHex(int hexColor) {
		float red = ((hexColor >> 16) & 0xFF) / 255.0F;
		float green = ((hexColor >> 8) & 0xFF) / 255.0F;
		float blue = (hexColor & 0xFF) / 255.0F;
		return Vec3.createVectorHelper(red, green, blue);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
		if(atmosphere == null || atmosphere.getPressure() < 0.05F) return null;

		float[] colors = super.calcSunriseSunsetColors(celestialAngle, partialTicks);
		if(colors == null) return null;

		// Mars IRL has inverted blue sunsets, which look cool as
		// So carbon dioxide rich atmospheres will do the same
		// for now, it's just a swizzle between red and blue

		//>mars
		//>look inside
		//>its actually just a ksp rip off
		//mfw
		if(atmosphere.hasFluid(Fluids.DUNAAIR) || atmosphere.hasFluid(Fluids.CARBONDIOXIDE)) {
			float tmp = colors[0];
			colors[0] = colors[2];
			colors[2] = tmp;
		}

		else if (atmosphere.hasFluid(Fluids.EVEAIR)) {
			float f2 = 0.4F;
			float f3 = MathHelper.cos((celestialAngle) * (float)Math.PI * 2.0F);
			float f4 = 0.0F;
//
			if (f3 >= f4 - f2 && f3 <= f4 + f2) {
				float f5 = (f3 - f4) / f2 * 0.5F + 0.5F;
				float f6 = 1.0F - (1.0F - MathHelper.sin(f5 * (float)Math.PI)) * 0.99F;
				f6 *= f6;
//
				// Venus-like yellow-orange sunset
				colors[0] = f5 * 0.9F + 0.1F;       // Red: dominant
				colors[1] = f5 * 0.7F + 0.2F;       // Green: moderately strong
				colors[2] = f5 * 0.1F;              // Blue: nearly absent for orange/yellow
				colors[3] = f6;                     // Alpha/brightness
			}
		}
		//whatever
		//did not solve the issue
		//added back

		return colors;
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		if(atmosphere != null && atmosphere.getPressure() > 0.2)
			return super.canDoLightning(chunk);

		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		if(atmosphere != null && atmosphere.getPressure() > 0.2)
			return super.canDoRainSnowIce(chunk);

		return false;
	}

	// Stars do not show up during the day in a vacuum, common misconception:
	// The reason stars aren't visible during the day on Earth isn't because of the sky,
	// the sky is ALWAYS there. The reason they aren't visible is because the Sun is too bright!
	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1) {
		// Stars become visible during the day beyond the orbit of Duna
		// And are fully visible during the day beyond the orbit of Jool
		float distanceStart = 20_000_000;
		float distanceEnd = 80_000_000;

		double semiMajorAxisKm = CelestialBody.getPlanet(worldObj).semiMajorAxisKm;
		double distanceFactor = MathHelper.clamp_double((semiMajorAxisKm - distanceStart) / (distanceEnd - distanceStart), 0F, 1F);

		double starBrightness = super.getStarBrightness(par1);

		return MathHelper.clamp_float((float) starBrightness,
									  (float) distanceFactor, 1F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		if(CelestialBody.getStar(worldObj).hasTrait(CBT_Destroyed.class))
			return 0;

		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
		float sunBrightness = super.getSunBrightness(par1);

		if(atmosphere == null) return sunBrightness;

		return sunBrightness * MathHelper.clamp_float(1.0F - ((float)atmosphere.getPressure() - 1.5F) * 0.2F, 0.25F, 1.0F);
	}

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

	// We want spawning to check for breathable, and getRespawnDimension() only runs if this is FALSE
	// BUT this also makes beds blow up (Mojang I swear), so we hook into the sleep event and set a flag
	public static boolean attemptingSleep = false;

	@Override
	public boolean canRespawnHere() {
		if(attemptingSleep) {
			attemptingSleep = false;
			return true;
		}

		return false;
	}

	// Another AWFULLY named deobfuscation function, this one is called when players have all slept,
	// which means we can set the master clock to the next local morning safely here!
	@Override
	public void resetRainAndThunder() {
		super.resetRainAndThunder();

		if(dimensionId == 0) return;
		if(!worldObj.getGameRules().getGameRuleBooleanValue("doDaylightCycle")) return;

		setWorldTime(getNextLocalMorningTime(getMasterWorldTime()));
	}

	@Override
	public long getWorldTime() {
		return getMasterWorldTime();
	}

	public long getLocalTime() {
		return getLocalTime(getMasterWorldTime());
	}

	public long getLocalTime(long masterTime) {
		double dayLength = getDayLength();
		if(dayLength <= 0.0D) return masterTime;

		double direction = CelestialBody.getBody(worldObj).getRotationDirection();
		return (long)(masterTime * direction * CelestialBody.VANILLA_DAY_TICKS / dayLength);
	}

	@Override
	public void setWorldTime(long time) {
		super.setWorldTime(time);
		syncedMasterTime = time;
		if(worldObj != null && worldObj.isRemote) {
			clientMasterTimeSyncTick = worldObj.getTotalWorldTime();
		}
	}

	private long getNextLocalMorningTime(long masterTime) {
		double dayLength = getDayLength();
		if(dayLength <= 0.0D) return masterTime;

		double direction = CelestialBody.getBody(worldObj).getRotationDirection();
		double localCycle = normalizeDayTime(masterTime * direction, dayLength);
		double ticksUntilMorning = (1.0D - localCycle) * dayLength;

		if(direction < 0.0D) {
			ticksUntilMorning = localCycle * dayLength;
		}

		if(ticksUntilMorning < 1.0D) {
			ticksUntilMorning += dayLength;
		}

		return masterTime + MathHelper.ceiling_double_int(ticksUntilMorning);
	}

	protected long getMasterWorldTime() {
		if(worldObj == null) {
			return syncedMasterTime >= 0 ? syncedMasterTime : 0;
		}

		long masterTime = super.getWorldTime();
		if(worldObj.isRemote && syncedMasterTime >= 0 && clientMasterTimeSyncTick >= 0) {
			long syncedTime = syncedMasterTime;
			if(worldObj.getGameRules().getGameRuleBooleanValue("doDaylightCycle")) {
				syncedTime += worldObj.getTotalWorldTime() - clientMasterTimeSyncTick;
			}

			if(Math.abs(masterTime - syncedTime) > 10) {
				masterTime = syncedTime;
			}
		}

		return masterTime;
	}

	public static long getMasterWorldTime(World world) {
		if(world != null && world.provider instanceof WorldProviderCelestial) {
			return ((WorldProviderCelestial)world.provider).getMasterWorldTime();
		}

		return world != null ? world.getWorldTime() : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);

		if(atmosphere == null || atmosphere.getPressure() < 0.5F) return -99999;

		return super.getCloudHeight();
	}

	private IRenderHandler skyProvider;

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		// I do not condone this because it WILL confuse your players, but if you absolutely must,
		// you can uncomment this line below in your fork to get default skybox rendering on Earth.

		// if(dimensionId == 0) return super.getSkyRenderer();

		// Make sure you also uncomment the relevant line in getMoonPhase below too.

		// This is not in a config because it is not a decision you should make lightly, as it will break:
		//  * certain atmosphere/terraforming modifications
		//  * Dyson swarm rendering
		//  * seeing weapons platforms in orbit (the big cannon from the trailer will NOT be visible)
		//  * weapon effects on the atmosphere (burning holes in the atmosphere, hitting planetary defense shields)
		//  * accurate celestial body rendering (you won't be able to see ANY other planets)
		//     * this also breaks future plans to modify orbits via huge mass drivers, if someone decides to yeet the moon at you, you won't know
		//  * sun extinction/modification events (the sun will appear normal even if it has been turned into a black hole)
		//  * player launched satellites won't be visible
		//  * artificial moons/rings (once implemented) won't be visible

		if(skyProvider == null) skyProvider = new SkyProviderCelestial();
		return skyProvider;
	}

	protected double getDayLength() {
		CelestialBody body = CelestialBody.getBody(worldObj);
		if(body.dimensionId == 0) {
			return CelestialBody.VANILLA_DAY_TICKS;
		}

		double siderealDay = body.getRotationalPeriod();
		double year = getSolarYearLength(body);

		if(Double.isInfinite(year) || year <= 0.0D) {
			return Math.max(1.0D, siderealDay);
		}

		// Convert sidereal rotation into local apparent solar-day length, still
		// measured in vanilla master ticks. This keeps Earth at exactly vanilla
		// 24,000-tick days while proportionally scaling every other body.
		double solarFrequency = (body.getRotationDirection() / siderealDay) - (1.0D / year);
		if(Math.abs(solarFrequency) < 1.0E-9D) {
			return Math.max(1.0D, siderealDay);
		}

		return Math.max(1.0D, Math.abs(1.0D / solarFrequency));
	}

	private double getSolarYearLength(CelestialBody body) {
		CelestialBody orbiting = body.parent != null && body.parent.parent != null ? body.parent : body;
		double orbitalPeriod = orbiting.getOrbitalPeriod();

		if(Double.isInfinite(orbitalPeriod)) {
			return Double.POSITIVE_INFINITY;
		}

		return CelestialBody.secondsToVanillaTicks(orbitalPeriod);
	}

	public float getNormalizedDayTime() {
		return (float)normalizeDayTime(getLocalTime(), CelestialBody.VANILLA_DAY_TICKS);
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		double localTime = getLocalTime(worldTime) + partialTicks * CelestialBody.VANILLA_DAY_TICKS / getDayLength();
		return calculateVanillaCelestialAngle(localTime);
	}

	private float calculateVanillaCelestialAngle(double localTime) {
		double f1 = normalizeDayTime(localTime, CelestialBody.VANILLA_DAY_TICKS) - 0.25F;
		if(f1 < 0.0F) ++f1;
		if(f1 > 1.0F) --f1;

		double f2 = f1;
		f1 = 0.5F - Math.cos(f1 * Math.PI) / 2.0F;

		return (float)(f2 + (f1 - f2) / 3.0D);
	}

	private double normalizeDayTime(double time, double dayLength) {
		double normalized = time % dayLength;
		if(normalized < 0.0D) normalized += dayLength;
		return normalized / dayLength;
	}

	@Override
	public int getMoonPhase(long worldTime) { //where is it wtf - this shit doesn't even work
		// Uncomment this line as well to return moon phase difficulty calcs to vanilla
		// if(dimensionId == 0) return super.getMoonPhase(worldTime);

		CelestialBody body = CelestialBody.getBody(worldObj);

		// if no moons, default to half-moon difficulty
		if(body.satellites.size() == 0) return 2;

		// Determine difficulty phase from closest moon
		float angle = (float)SolarSystem.calculateSingleAngle(worldObj, 0.0F, body, body.satellites.get(0));
		double normalizedAngle = ((angle % 360.0D) + 360.0D) % 360.0D;
		return (int)Math.floor(normalizedAngle / 45.0D) % 8;
	}

	// This is the vanilla junk table, for replacing fish on dead worlds
	private static ArrayList<WeightedRandomFishable> junk;

	// you know what that means
	/// FISH ///

	// returning null from any of these methods will revert to overworld loot tables
	public ArrayList<WeightedRandomFishable> getFish() {
		if(junk == null) {
			junk = new ArrayList<>();
			// junk.add((new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).func_150709_a(0.9F));
			// junk.add(new WeightedRandomFishable(new ItemStack(Items.leather), 10));
			// junk.add(new WeightedRandomFishable(new ItemStack(Items.bone), 10));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.potionitem), 10));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.string), 5));
			junk.add((new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).func_150709_a(0.9F));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.bowl), 10));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.stick), 5));
			junk.add(new WeightedRandomFishable(new ItemStack(Items.dye, 10, 0), 1));
			junk.add(new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10));
			// junk.add(new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
		}

		return junk;
	}

	public ArrayList<WeightedRandomFishable> getJunk() {
		return null;
	}

	public ArrayList<WeightedRandomFishable> getTreasure() {
		return null;
	}
	/// FISH ///

}
