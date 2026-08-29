package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockGasMonoxide extends BlockGasBase {

	private static final String EXPOSURE_KEY = "hbmMonoxideExposure";
	private static final String LAST_EXPOSURE_KEY = "hbmMonoxideLastExposure";
	private static final int VENT_SEARCH_RANGE = 5;
	private static final int VENT_SEARCH_LIMIT = 128;
	private static final int VENT_BLOCK_DISSIPATION_CHANCE = 2;
	private static final int EXPOSURE_PER_TICK = 3;
	private static final int CONFUSION_THRESHOLD = 160;
	private static final int WEAKNESS_THRESHOLD = 280;
	private static final int DAMAGE_THRESHOLD = 360;
	private static final int SEVERE_DAMAGE_THRESHOLD = 720;
	private static final int MAX_EXPOSURE = 2400;

	public BlockGasMonoxide() {
		super(0.1F, 0.1F, 0.1F);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if(world.isRemote || !(entity instanceof EntityLivingBase) || isVentilated(world, x, y, z)) return;

		EntityLivingBase living = (EntityLivingBase) entity;
		NBTTagCompound data = living.getEntityData();
		long now = world.getTotalWorldTime();
		long lastExposure = data.getLong(LAST_EXPOSURE_KEY);
		int exposure = data.getInteger(EXPOSURE_KEY);

		// Tall entities may overlap more than one gas block, but should only inhale once per tick.
		if(lastExposure == now) return;

		if(lastExposure > 0 && now - lastExposure > 5) {
			exposure = Math.max(0, exposure - (int) Math.min((now - lastExposure) / 2, Integer.MAX_VALUE));
		}

		data.setLong(LAST_EXPOSURE_KEY, now);

		if(ArmorRegistry.hasAllProtection(living, 3, HazardClass.GAS_MONOXIDE)) {
			if(living.ticksExisted % 20 == 0) ArmorUtil.damageGasMaskFilter(living, 1);
			data.setInteger(EXPOSURE_KEY, Math.max(0, exposure - 4));
			return;
		}

		exposure = Math.min(exposure + EXPOSURE_PER_TICK, MAX_EXPOSURE);
		data.setInteger(EXPOSURE_KEY, exposure);

		if(exposure >= CONFUSION_THRESHOLD) living.addPotionEffect(new PotionEffect(Potion.confusion.id, 200, 0));
		if(exposure >= WEAKNESS_THRESHOLD) living.addPotionEffect(new PotionEffect(Potion.weakness.id, 120, MathHelper.clamp_int(exposure / 300, 0, 2)));
		if(exposure >= DAMAGE_THRESHOLD && exposure % 20 < EXPOSURE_PER_TICK) {
			//blindness for blurred vision
			living.addPotionEffect(new PotionEffect(Potion.blindness.id, 120, MathHelper.clamp_int(exposure / 300, 0, 2)));
			living.attackEntityFrom(ModDamageSource.monoxide, exposure >= SEVERE_DAMAGE_THRESHOLD ? 6 : 4);
		}
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {
		return world.rand.nextInt(4) == 0 ? ForgeDirection.UP : this.randomHorizontal(world);
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return this.randomHorizontal(world);
	}

	@Override
	public int getDelay(World world) {
		return 8 + world.rand.nextInt(9);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(world.isRemote) return;

		ForgeDirection ventDirection = findVentDirection(world, x, y, z);
		if(ventDirection == ForgeDirection.UNKNOWN) {
			tryDriftTowardExit(world, x, y, z);
			return;
		}

		if(isVentBlockNearby(world, x, y, z) && rand.nextInt(VENT_BLOCK_DISSIPATION_CHANCE) != 0) {
			world.setBlockToAir(x, y, z);
			return;
		}

		if(tryMove(world, x, y, z, ventDirection)) return;

		if(rand.nextInt(16) != 0) {
			world.setBlockToAir(x, y, z);
			return;
		}

		ForgeDirection first = getFirstDirection(world, x, y, z);
		boolean moved = tryMove(world, x, y, z, first);
		if(!moved) moved = tryMove(world, x, y, z, first == ForgeDirection.UP ? randomHorizontal(world) : ForgeDirection.UP);
		if(!moved) world.scheduleBlockUpdate(x, y, z, this, getDelay(world));
	}

	/**
	 * Without a known vent path, allow carbon monoxide to seep horizontally toward
	 * doorways and corridors without preferentially lifting the entire cloud to the
	 * ceiling. Sealed rooms should still be able to fill with gas properly.
	 */
	private void tryDriftTowardExit(World world, int x, int y, int z) {
		ForgeDirection first = randomHorizontal(world);
		boolean moved = tryMove(world, x, y, z, first);
		if(!moved) moved = tryMove(world, x, y, z, randomHorizontal(world));
		if(!moved) world.scheduleBlockUpdate(x, y, z, this, getDelay(world));
	}

	/**
	 * Treats a gas pocket as ventilated when air can reach an actually open sky column
	 * through a short open path. Ventilated pockets dissipate quickly, while sealed pockets
	 * stay in place so local detector readings do not drop just because gas drifted away.
	 */
	private boolean isVentilated(World world, int x, int y, int z) {
		return findVentDirection(world, x, y, z) != ForgeDirection.UNKNOWN;
	}

	private ForgeDirection findVentDirection(World world, int x, int y, int z) {
		int[] queueX = new int[VENT_SEARCH_LIMIT];
		int[] queueY = new int[VENT_SEARCH_LIMIT];
		int[] queueZ = new int[VENT_SEARCH_LIMIT];
		int[] firstDirections = new int[VENT_SEARCH_LIMIT];
		int read = 0;
		int write = 1;
		queueX[0] = x;
		queueY[0] = y;
		queueZ[0] = z;
		firstDirections[0] = ForgeDirection.UNKNOWN.ordinal();

		while(read < write) {
			int currentX = queueX[read];
			int currentY = queueY[read];
			int currentZ = queueZ[read];
			int firstDirection = firstDirections[read++];
			if(hasOpenSkyColumn(world, currentX, currentY, currentZ)) return firstDirection == ForgeDirection.UNKNOWN.ordinal() ? ForgeDirection.UP : ForgeDirection.getOrientation(firstDirection);

			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				int nextX = currentX + direction.offsetX;
				int nextY = currentY + direction.offsetY;
				int nextZ = currentZ + direction.offsetZ;
				if(Math.abs(nextX - x) > VENT_SEARCH_RANGE || Math.abs(nextY - y) > VENT_SEARCH_RANGE || Math.abs(nextZ - z) > VENT_SEARCH_RANGE) continue;

				Block block = world.getBlock(nextX, nextY, nextZ);
				int nextFirstDirection = firstDirection == ForgeDirection.UNKNOWN.ordinal() ? direction.ordinal() : firstDirection;
				if(isVentBlock(block)) return ForgeDirection.getOrientation(nextFirstDirection);
				if(!world.isAirBlock(nextX, nextY, nextZ) && block != this && !isPermeableVentBlock(block)) continue;

				boolean visited = false;
				for(int i = 0; i < write; i++) {
					if(queueX[i] == nextX && queueY[i] == nextY && queueZ[i] == nextZ) {
						visited = true;
						break;
					}
				}

				if(!visited && write < VENT_SEARCH_LIMIT) {
					queueX[write] = nextX;
					queueY[write] = nextY;
					queueZ[write] = nextZ;
					firstDirections[write++] = nextFirstDirection;
				}
			}
		}

		return ForgeDirection.UNKNOWN;
	}

	/**
	 * World.canBlockSeeTheSky also succeeds through transparent sealed blocks such as glass.
	 * For ventilation, require the path above the candidate pocket to be physically open.
	 */
	private boolean hasOpenSkyColumn(World world, int x, int y, int z) {
		for(int checkY = y + 1; checkY < world.getHeight(); checkY++) {
			Block block = world.getBlock(x, checkY, z);
			if(!world.isAirBlock(x, checkY, z) && block != this && !isPermeableVentBlock(block)) return false;
		}

		return true;
	}

	private boolean isVentBlockNearby(World world, int x, int y, int z) {
		for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if(isVentBlock(world.getBlock(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ))) return true;
		}

		return false;
	}

	private boolean isVentBlock(Block block) {
		return block == ModBlocks.air_vent || isPermeableVentBlock(block);
	}

	private boolean isPermeableVentBlock(Block block) {
		return block == ModBlocks.steel_grate || block == ModBlocks.steel_grate_wide;
	}
}
