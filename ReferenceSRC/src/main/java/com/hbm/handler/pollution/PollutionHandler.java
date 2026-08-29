package com.hbm.handler.pollution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import com.hbm.config.MobConfig;
import com.hbm.config.RadiationConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;

public class PollutionHandler {

	public static final String fileName = "hbmpollution.dat";
	public static HashMap<World, PollutionPerWorld> perWorld = new HashMap();

	/*
	 * Pollution is stored in 64x64 block sectors, not vanilla 16x16 chunks.
	 *
	 * The use of ChunkCoordIntPair is kept for compatibility/convenience,
	 * but chunkXPos/chunkZPos here mean "pollution sector coords".
	 */
	private static final int POLLUTION_SECTOR_SHIFT = 6;
	private static final int POLLUTION_SECTOR_SIZE = 1 << POLLUTION_SECTOR_SHIFT;

	/*
	 * Abstract burden cap.
	 *
	 * This is not ppm, kg, or real concentration. It is a gameplay pollution burden.
	 *
	 * Rough interpretation:
	 * 10     = noticeable local pollution
	 * 100    = unhealthy / visible environmental stress
	 * 1,000  = severe industrial contamination
	 * 10,000 = catastrophic contamination cap
	 */
	private static final float POLLUTION_CAP = 10_000F;

	/*
	 * Removes microscopic leftovers.
	 *
	 * Without this, values like 0.0000001 remain greater than zero forever,
	 * keeping sectors alive in memory and save data.
	 */
	private static final float POLLUTION_EPSILON = 0.001F;

	/*
	 * Pollution updates once every 60 server ticks.
	 *
	 * 20 ticks = 1 second.
	 * 60 ticks = 3 seconds.
	 */
	private static final int POLLUTION_UPDATE_TICKS = 60;

	/*
	 * Per-update neighbor spread.
	 *
	 * These are per neighbor. Exported pollution is subtracted from the source.
	 *
	 * Soot spreads relatively well because it represents smoke, ash, and airborne particulates.
	 * Poison spreads moderately because it represents chemical contamination.
	 * Heavy metals barely spread without runoff/dust transport.
	 * Fallout spreads slightly as radioactive dust/deposition.
	 */
	private static final float SOOT_SPREAD_PER_NEIGHBOR = 0.04F;
	private static final float POISON_SPREAD_PER_NEIGHBOR = 0.02F;
	private static final float HEAVY_METAL_SPREAD_PER_NEIGHBOR = 0.0025F;
	private static final float FALLOUT_SPREAD_PER_NEIGHBOR = 0.005F;

	/*
	 * Per-update decay.
	 *
	 * These happen every 3 seconds, not every tick.
	 *
	 * Soot clears comparatively quickly.
	 * Poison is semi-persistent.
	 * Heavy metals are effectively permanent. The tiny decay is only an abstraction
	 * for burial, dilution, and long-term environmental settling.
	 * Fallout decays slowly as a gameplay abstraction of radioactive decay + burial.
	 */
	private static final float SOOT_DECAY = 0.985F;
	private static final float POISON_DECAY = 0.9975F;
	private static final float HEAVY_METAL_DECAY = 0.999995F;
	private static final float FALLOUT_DECAY = 0.9999F;

	/*
	 * Terrain damage threshold.
	 *
	 * Old value was 15, which is extremely low compared to a 10,000 cap.
	 * 100 means small pollution is tolerated, while genuinely contaminated sectors
	 * start visibly degrading.
	 */
	protected static final float DESTRUCTION_THRESHOLD = 100F;
	protected static final int DESTRUCTION_COUNT = 4;

	/** Baserate of soot generation for a furnace-equivalent machine per second */
	public static final float SOOT_PER_SECOND = 1F / 25F;

	/** Baserate of heavy metal generation, balanced around the soot values of combustion engines */
	public static final float HEAVY_METAL_PER_SECOND = 1F / 50F;

	/** Baserate for poison when spilled */
	public static final float POISON_PER_SECOND = 1F / 50F;

	public static Vec3 targetCoords;

	///////////////////////
	/// UTILITY METHODS ///
	///////////////////////

	public static void incrementPollution(World world, int x, int y, int z, PollutionType type, float amount) {

		if(!RadiationConfig.enablePollution) return;

		PollutionPerWorld ppw = perWorld.get(world);
		if(ppw == null) return;

		ChunkCoordIntPair pos = getPollutionSector(x, z);

		PollutionData data = ppw.pollution.get(pos);
		if(data == null) {
			data = new PollutionData();
			ppw.pollution.put(pos, data);
		}

		int index = type.ordinal();

		/*
		 * Emissions are affected by pollutionMult.
		 *
		 * Negative values are allowed here for compatibility, but direct cleanup
		 * should use decrementPollution(), which intentionally does NOT apply
		 * pollutionMult.
		 */
		float scaled = (float) (amount * MobConfig.pollutionMult);

		data.pollution[index] = MathHelper.clamp_float(data.pollution[index] + scaled, 0F, POLLUTION_CAP);

		if(data.pollution[index] < POLLUTION_EPSILON) {
			data.pollution[index] = 0F;
		}
	}

	public static void decrementPollution(World world, int x, int y, int z, PollutionType type, float amount) {

		if(!RadiationConfig.enablePollution) return;

		PollutionPerWorld ppw = perWorld.get(world);
		if(ppw == null) return;

		ChunkCoordIntPair pos = getPollutionSector(x, z);

		PollutionData data = ppw.pollution.get(pos);
		if(data == null) return;

		/*
		 * Cleanup/removal should not be scaled by MobConfig.pollutionMult.
		 *
		 * pollutionMult is an emission multiplier, not a remediation multiplier.
		 */
		int index = type.ordinal();
		data.pollution[index] = MathHelper.clamp_float(data.pollution[index] - amount, 0F, POLLUTION_CAP);

		if(data.pollution[index] < POLLUTION_EPSILON) {
			data.pollution[index] = 0F;
		}

		if(data.isEmpty()) {
			ppw.pollution.remove(pos);
		}
	}

	public static void setPollution(World world, int x, int y, int z, PollutionType type, float amount) {

		if(!RadiationConfig.enablePollution) return;

		PollutionPerWorld ppw = perWorld.get(world);
		if(ppw == null) return;

		ChunkCoordIntPair pos = getPollutionSector(x, z);

		PollutionData data = ppw.pollution.get(pos);
		if(data == null) {
			data = new PollutionData();
			ppw.pollution.put(pos, data);
		}

		int index = type.ordinal();

		data.pollution[index] = MathHelper.clamp_float(amount, 0F, POLLUTION_CAP);

		if(data.pollution[index] < POLLUTION_EPSILON) {
			data.pollution[index] = 0F;
		}

		if(data.isEmpty()) {
			ppw.pollution.remove(pos);
		}
	}

	public static float getPollution(World world, int x, int y, int z, PollutionType type) {

		if(!RadiationConfig.enablePollution) return 0F;

		PollutionPerWorld ppw = perWorld.get(world);
		if(ppw == null) return 0F;

		ChunkCoordIntPair pos = getPollutionSector(x, z);

		PollutionData data = ppw.pollution.get(pos);
		if(data == null) return 0F;

		return data.pollution[type.ordinal()];
	}

	public static PollutionData getPollutionData(World world, int x, int y, int z) {

		if(!RadiationConfig.enablePollution) return null;

		PollutionPerWorld ppw = perWorld.get(world);
		if(ppw == null) return null;

		ChunkCoordIntPair pos = getPollutionSector(x, z);

		return ppw.pollution.get(pos);
	}

	private static ChunkCoordIntPair getPollutionSector(int x, int z) {
		return new ChunkCoordIntPair(x >> POLLUTION_SECTOR_SHIFT, z >> POLLUTION_SECTOR_SHIFT);
	}

	//////////////////////
	/// EVENT HANDLING ///
	//////////////////////

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {

		if(!event.world.isRemote && RadiationConfig.enablePollution) {

			WorldServer world = (WorldServer) event.world;
			String dirPath = getDataDir(world);

			try {
				File pollutionFile = new File(dirPath, fileName);

				if(pollutionFile.exists()) {
					try {
						FileInputStream io = new FileInputStream(pollutionFile);
						NBTTagCompound data = CompressedStreamTools.readCompressed(io);
						io.close();
						perWorld.put(event.world, new PollutionPerWorld(data));
					} catch(Exception ex) {
						System.out.println("Failed to read " + pollutionFile.getAbsolutePath());
						ex.printStackTrace();
						perWorld.put(event.world, new PollutionPerWorld());
					}
				} else {
					perWorld.put(event.world, new PollutionPerWorld());
				}

			} catch(Exception ex) {
				System.out.println("Failed to create " + dirPath + File.separator + fileName);
				ex.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(!event.world.isRemote) {
			perWorld.remove(event.world);
		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {

		if(!event.world.isRemote) {

			WorldServer world = (WorldServer) event.world;
			String dirPath = getDataDir(world);
			File pollutionFile = new File(dirPath, fileName);

			try {
				if(!pollutionFile.getParentFile().exists()) {
					pollutionFile.getParentFile().mkdirs();
				}

				if(!pollutionFile.exists()) {
					pollutionFile.createNewFile();
				}

				PollutionPerWorld ppw = perWorld.get(world);

				if(ppw != null) {
					NBTTagCompound data = ppw.writeToNBT();
					CompressedStreamTools.writeCompressed(data, new FileOutputStream(pollutionFile));
				}

			} catch(Exception ex) {
				System.out.println("Failed to write " + pollutionFile.getAbsolutePath());
				ex.printStackTrace();
			}
		}
	}

	public String getDataDir(WorldServer world) {

		String dir = world.getSaveHandler().getWorldDirectory().getAbsolutePath();

		// Crucible and probably Thermos provide dimId by themselves.
		String dimId = File.separator + "DIM" + world.provider.dimensionId;

		if(world.provider.dimensionId != 0 && !dir.endsWith(dimId)) {
			dir += dimId;
		}

		dir += File.separator + "data";

		return dir;
	}

	//////////////////////////
	/// SYSTEM UPDATE LOOP ///
	//////////////////////////

	int eggTimer = 0;

	@SubscribeEvent
	public void updateSystem(TickEvent.ServerTickEvent event) {

		if(event.side != Side.SERVER || event.phase != Phase.END) return;

		eggTimer++;

		if(eggTimer < POLLUTION_UPDATE_TICKS) return;

		eggTimer = 0;

		/*
		 * Terrain damage should run at pollution-simulation speed, not every server tick.
		 *
		 * Old behavior ran this 20 times per second, which made terrain destruction
		 * much more aggressive than the actual pollution update.
		 */
		handleWorldDestruction();

		for(Entry<World, PollutionPerWorld> entry : perWorld.entrySet()) {

			World world = entry.getKey();

			HashMap<ChunkCoordIntPair, PollutionData> newPollution = new HashMap();

			for(Entry<ChunkCoordIntPair, PollutionData> sector : entry.getValue().pollution.entrySet()) {

				int sectorX = sector.getKey().chunkXPos;
				int sectorZ = sector.getKey().chunkZPos;

				PollutionData data = sector.getValue();

				float[] spread = new float[PollutionType.values().length];

				int S = PollutionType.SOOT.ordinal();
				int P = PollutionType.POISON.ordinal();
				int H = PollutionType.HEAVYMETAL.ordinal();
				int F = PollutionType.FALLOUT.ordinal();

				/*
				 * Weather abstraction:
				 *
				 * Rain scrubs soot from the air and slightly deposits/settles fallout.
				 * It does not erase heavy metals because metals do not vanish. A more complex
				 * runoff system could move heavy metals into water/low terrain later.
				 */
				boolean raining = world.isRaining();

				////////////////
				/// SOOT     ///
				////////////////

				/*
				 * Soot = smoke, ash, and airborne particulates.
				 *
				 * Realistic behavior:
				 * - spreads better than other pollution types,
				 * - clears from the air over time,
				 * - rain removes it faster,
				 * - high soot can stress vegetation but is not as soil-persistent as metal.
				 */
				if(data.pollution[S] > 10F) {
					float exported = data.pollution[S] * SOOT_SPREAD_PER_NEIGHBOR;
					spread[S] = exported;
					data.pollution[S] -= exported * 4F;
				}

				data.pollution[S] *= SOOT_DECAY;

				if(raining) {
					data.pollution[S] *= 0.97F;
				}

				////////////////
				/// POISON   ///
				////////////////

				/*
				 * Poison = generic toxic chemical contamination.
				 *
				 * Realistic behavior:
				 * - spreads less than soot,
				 * - lasts longer than soot,
				 * - strongly damages plants/soil,
				 * - still eventually breaks down, reacts, dilutes, or gets buried.
				 */
				if(data.pollution[P] > 10F) {
					float exported = data.pollution[P] * POISON_SPREAD_PER_NEIGHBOR;
					spread[P] = exported;
					data.pollution[P] -= exported * 4F;
				}

				data.pollution[P] *= POISON_DECAY;

				//////////////////////
				/// HEAVY METALS   ///
				//////////////////////

				/*
				 * Heavy metals = lead, cadmium, mercury, arsenic, uranium dust, etc.
				 *
				 * Realistic behavior:
				 * - practically does not decay,
				 * - barely migrates without water/dust/soil transport,
				 * - contaminates soil for a very long time,
				 * - should require cleanup/remediation to remove meaningfully.
				 *
				 * The tiny decay here is not literal chemical decay. It represents burial,
				 * dilution, and gameplay cleanup over extremely long periods.
				 */
				if(data.pollution[H] > 25F) {
					float exported = data.pollution[H] * HEAVY_METAL_SPREAD_PER_NEIGHBOR;
					spread[H] = exported;
					data.pollution[H] -= exported * 4F;
				}

				data.pollution[H] *= HEAVY_METAL_DECAY;

				////////////////
				/// FALLOUT  ///
				////////////////

				/*
				 * Fallout = radioactive particulate deposition.
				 *
				 * Realistic behavior:
				 * - spreads slightly as dust,
				 * - decays slowly,
				 * - rain helps settle airborne fallout locally,
				 * - should remain dangerous much longer than soot/poison.
				 *
				 * This does not directly call the radiation system here because this class
				 * originally only tracked pollution. Hooking fallout into chunk radiation
				 * should be done deliberately elsewhere to avoid double-counting radiation.
				 */
				if(data.pollution[F] > 10F) {
					float exported = data.pollution[F] * FALLOUT_SPREAD_PER_NEIGHBOR;
					spread[F] = exported;
					data.pollution[F] -= exported * 4F;
				}

				data.pollution[F] *= FALLOUT_DECAY;

				if(raining) {
					data.pollution[F] *= 0.995F;
				}

				//////////////////////
				/// CLEANUP/CLAMP  ///
				//////////////////////

				for(int i = 0; i < data.pollution.length; i++) {

					data.pollution[i] = MathHelper.clamp_float(data.pollution[i], 0F, POLLUTION_CAP);

					if(data.pollution[i] < POLLUTION_EPSILON) {
						data.pollution[i] = 0F;
					}
				}

				// Apply remaining pollution to self.
				mergePollution(newPollution, sector.getKey(), data.pollution);

				// Apply exported pollution to neighboring 64x64 sectors.
				int[][] offsets = new int[][] {
					{ 1,  0},
					{-1,  0},
					{ 0,  1},
					{ 0, -1}
				};

				for(int[] offset : offsets) {
					ChunkCoordIntPair offPos = new ChunkCoordIntPair(sectorX + offset[0], sectorZ + offset[1]);
					mergePollution(newPollution, offPos, spread);
				}
			}

			entry.getValue().pollution.clear();
			entry.getValue().pollution.putAll(newPollution);
		}
	}

	private static void mergePollution(HashMap<ChunkCoordIntPair, PollutionData> map, ChunkCoordIntPair pos, float[] values) {

		PollutionData data = map.get(pos);

		if(data == null) {
			data = new PollutionData();
		}

		boolean shouldPut = false;

		for(int i = 0; i < data.pollution.length; i++) {

			data.pollution[i] += values[i];
			data.pollution[i] = MathHelper.clamp_float(data.pollution[i], 0F, POLLUTION_CAP);

			if(data.pollution[i] < POLLUTION_EPSILON) {
				data.pollution[i] = 0F;
			}

			if(data.pollution[i] >= POLLUTION_EPSILON) {
				shouldPut = true;
			}
		}

		if(shouldPut) {
			map.put(pos, data);
		}
	}

	protected static void handleWorldDestruction() {

		for(Entry<World, PollutionPerWorld> entry : perWorld.entrySet()) {

			World world = entry.getKey();

			if(world.isRemote) continue;
			if(!(world instanceof WorldServer)) continue;

			WorldServer serv = (WorldServer) world;
			ChunkProviderServer provider = (ChunkProviderServer) serv.getChunkProvider();

			for(Entry<ChunkCoordIntPair, PollutionData> pollution : entry.getValue().pollution.entrySet()) {

				PollutionData data = pollution.getValue();

				float soot = data.pollution[PollutionType.SOOT.ordinal()];
				float poison = data.pollution[PollutionType.POISON.ordinal()];
				float heavy = data.pollution[PollutionType.HEAVYMETAL.ordinal()];
				float fallout = data.pollution[PollutionType.FALLOUT.ordinal()];

				/*
				 * Terrain damage weighting.
				 *
				 * Poison is the most direct chemical killer.
				 * Fallout is extremely hostile to life.
				 * Heavy metals ruin soil but usually more slowly.
				 * Soot stresses plants but is less directly destructive.
				 */
				float damage =
					poison * 1.0F +
						fallout * 0.75F +
						heavy * 0.5F +
						soot * 0.25F;

				if(damage < DESTRUCTION_THRESHOLD) continue;

				ChunkCoordIntPair entryPos = pollution.getKey();

				for(int i = 0; i < DESTRUCTION_COUNT; i++) {

					int x = (entryPos.chunkXPos << POLLUTION_SECTOR_SHIFT) + world.rand.nextInt(POLLUTION_SECTOR_SIZE);
					int z = (entryPos.chunkZPos << POLLUTION_SECTOR_SHIFT) + world.rand.nextInt(POLLUTION_SECTOR_SIZE);

					/*
					 * provider.chunkExists() expects vanilla 16x16 chunk coords,
					 * not pollution-sector coords.
					 */
					if(!provider.chunkExists(x >> 4, z >> 4)) continue;

					int y = world.getHeightValue(x, z) - world.rand.nextInt(3) + 1;

					if(y <= 0 || y >= world.getActualHeight()) continue;

					Block b = world.getBlock(x, y, z);

					/*
					 * Visible environmental stress.
					 *
					 * Grass becomes coarse/dead dirt.
					 * Plants and leaves die.
					 *
					 * This intentionally avoids replacing stone/sand/ores/etc.
					 */
					if(b == Blocks.grass) {

						world.setBlock(x, y, z, Blocks.dirt, 1, 3);

					} else if(b == Blocks.dirt && world.getBlockMetadata(x, y, z) == 0 && damage > DESTRUCTION_THRESHOLD * 2F) {

						world.setBlockMetadataWithNotify(x, y, z, 1, 3);

					} else if(b == Blocks.tallgrass || b.getMaterial() == Material.leaves || b.getMaterial() == Material.plants) {

						world.setBlock(x, y, z, Blocks.air);
					}
				}
			}
		}
	}

	//////////////////////
	/// DATA STRUCTURE ///
	//////////////////////

	public static class PollutionPerWorld {

		public HashMap<ChunkCoordIntPair, PollutionData> pollution = new HashMap();

		public PollutionPerWorld() { }

		public PollutionPerWorld(NBTTagCompound data) {

			NBTTagList list = data.getTagList("entries", 10);

			for(int i = 0; i < list.tagCount(); i++) {

				NBTTagCompound nbt = list.getCompoundTagAt(i);

				int chunkX = nbt.getInteger("chunkX");
				int chunkZ = nbt.getInteger("chunkZ");

				PollutionData pollutionData = PollutionData.fromNBT(nbt);

				if(!pollutionData.isEmpty()) {
					pollution.put(new ChunkCoordIntPair(chunkX, chunkZ), pollutionData);
				}
			}
		}

		public NBTTagCompound writeToNBT() {

			NBTTagCompound data = new NBTTagCompound();
			NBTTagList list = new NBTTagList();

			for(Entry<ChunkCoordIntPair, PollutionData> entry : pollution.entrySet()) {

				if(entry.getValue() == null || entry.getValue().isEmpty()) continue;

				NBTTagCompound nbt = new NBTTagCompound();

				/*
				 * Kept as chunkX/chunkZ for save compatibility.
				 * These are actually 64x64 pollution-sector coordinates.
				 */
				nbt.setInteger("chunkX", entry.getKey().chunkXPos);
				nbt.setInteger("chunkZ", entry.getKey().chunkZPos);

				entry.getValue().toNBT(nbt);

				list.appendTag(nbt);
			}

			data.setTag("entries", list);

			return data;
		}
	}

	public static class PollutionData {

		public float[] pollution = new float[PollutionType.values().length];

		public static PollutionData fromNBT(NBTTagCompound nbt) {

			PollutionData data = new PollutionData();

			for(int i = 0; i < PollutionType.values().length; i++) {

				float value = nbt.getFloat(PollutionType.values()[i].name().toLowerCase(Locale.US));

				value = MathHelper.clamp_float(value, 0F, POLLUTION_CAP);

				if(value < POLLUTION_EPSILON) {
					value = 0F;
				}

				data.pollution[i] = value;
			}

			return data;
		}

		public void toNBT(NBTTagCompound nbt) {

			for(int i = 0; i < PollutionType.values().length; i++) {

				float value = MathHelper.clamp_float(pollution[i], 0F, POLLUTION_CAP);

				if(value < POLLUTION_EPSILON) {
					value = 0F;
				}

				nbt.setFloat(PollutionType.values()[i].name().toLowerCase(Locale.US), value);
			}
		}

		public boolean isEmpty() {

			for(int i = 0; i < pollution.length; i++) {
				if(pollution[i] >= POLLUTION_EPSILON) {
					return false;
				}
			}

			return true;
		}
	}

	public static enum PollutionType {
		SOOT,
		POISON,
		HEAVYMETAL,
		FALLOUT;
	}

	///////////////////
	/// MOB EFFECTS ///
	///////////////////

	public static final UUID maxHealth = UUID.fromString("25462f6c-2cb2-4ca8-9b47-3a011cc61207");
	public static final UUID attackDamage = UUID.fromString("8f442d7c-d03f-49f6-a040-249ae742eed9");

	@SubscribeEvent
	public void decorateMob(LivingSpawnEvent event) {

		if(!RadiationConfig.enablePollution) return;

		World world = event.world;

		if(world.isRemote) return;

		EntityLivingBase living = event.entityLiving;

		PollutionData data = getPollutionData(
			world,
			(int) Math.floor(event.x),
			(int) Math.floor(event.y),
			(int) Math.floor(event.z)
		);

		if(data == null) return;
	}
}
