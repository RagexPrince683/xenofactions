package com.hbm.entity.mob.ai;

import com.hbm.entity.mob.EntityFRIEND;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;

public class EntityAIDigToPlayer extends EntityAIBase {

	private final EntityCreature entity;
	private final double speed;
	private final double range;
	private EntityPlayer target;

	private int attackCooldown = 0;
	private int teleportDelay = 0;
	private boolean wasWatched = false;

	public EntityAIDigToPlayer(EntityCreature entity, double speed, double range) {
		this.entity = entity;
		this.speed = speed;
		this.range = range;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (entity.worldObj.isRemote) return false;

		EntityPlayer player = entity.worldObj.getClosestPlayerToEntity(entity, range);
		if (player == null || player.isDead) return false;

		this.target = player;
		return true;
	}

	@Override
	public boolean continueExecuting() {
		return target != null
			&& !target.isDead
			&& entity.getDistanceSqToEntity(target) <= range * range;
	}

	@Override
	public void resetTask() {
		this.target = null;
		this.attackCooldown = 0;
		this.teleportDelay = 0;
		this.wasWatched = false;
		entity.getNavigator().clearPathEntity();
	}

	private int stuckTime = 0;

	@Override
	public void updateTask() {
		if (target == null) return;

		entity.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);

		boolean entitySeesPlayer = canEntitySeeTarget(entity, target);
		boolean playerLooking = isPlayerLookingAtEntity(target);
		boolean watched = entitySeesPlayer && playerLooking;

		double distSq = entity.getDistanceSqToEntity(target);
		applyEffects(target, distSq);

		if (entity.getNavigator().noPath()) {
			stuckTime++;
		} else {
			stuckTime = 0;
		}

		// Freeze only while the player is actually watching it.
		if (watched && ((EntityFRIEND)entity).forcedAggroTime <= 0) {
			wasWatched = true;
			entity.getNavigator().clearPathEntity();
			entity.motionX = 0.0D;
			entity.motionZ = 0.0D;
			return;
		}

		// Just came out of frozen state: clear any stale path/motion.
		if (wasWatched) {
			wasWatched = false;
			entity.getNavigator().clearPathEntity();
			entity.motionX = 0.0D;
			entity.motionZ = 0.0D;
		}

		// Close range attack / teleport logic
		if (distSq < 4.0D) {
			if (attackCooldown > 0) attackCooldown--;

			if (attackCooldown <= 0) {
				entity.attackEntityAsMob(target);
				attackCooldown = 20;
				teleportDelay = 30 + entity.getRNG().nextInt(20);
			}

			if (teleportDelay > 0) {
				teleportDelay--;
				if (teleportDelay == 0) {
					((EntityFRIEND) entity).teleportUndergroundNearPlayer(target);
				}
			}
		}

		// If it can see the player, STOP DIGGING and move normally toward them.
		if (entitySeesPlayer) {
			entity.getNavigator().clearPathEntity();
			moveDirectlyTowardTarget(0.18D);
			return;
		}

		// Try pathfinding first
		if (entity.getNavigator().tryMoveToEntityLiving(target, speed)) {

			// If not stuck, keep using path
			if (stuckTime < 20) {
				return;
			}

			// Path failed → fall through to digging
		}

		// No path exists → dig toward them
		entity.getNavigator().clearPathEntity();

		double dx = target.posX - entity.posX;
		double dy = (target.posY + target.getEyeHeight()) - entity.posY;
		double dz = target.posZ - entity.posZ;

		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist == 0) return;

		dx /= dist;
		dy /= dist;
		dz /= dist;

		double moveSpeed = 0.45D;
		double mx = dx * moveSpeed;
		double my = dy * moveSpeed * 0.5D;
		double mz = dz * moveSpeed;

		boolean moved = false;

		if (canMoveForward(mx, my, mz)) {
			entity.moveEntity(mx, my, mz);
			moved = true;
		}

		if (!moved) {
			carveTunnel(dx, dy, dz);
			carveTunnel(dx, dy, dz);

			if (canMoveForward(mx, my, mz)) {
				entity.moveEntity(mx, my, mz);
			} else {
				entity.motionX *= 0.2D;
				entity.motionZ *= 0.2D;
			}
		}
	}

	private void moveDirectlyTowardTarget(double step) {
		double dx = target.posX - entity.posX;
		double dy = (target.posY + target.getEyeHeight()) - entity.posY;
		double dz = target.posZ - entity.posZ;

		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist == 0.0D) return;

		dx /= dist;
		dy /= dist;
		dz /= dist;

		double mx = dx * step;
		double my = dy * step * 0.35D;
		double mz = dz * step;

		// Direct movement only. No digging here.
		if (canMoveForward(mx, my, mz)) {
			entity.moveEntity(mx, my, mz);
		} else {
			// If it somehow still has a block in the way, do not dig in the visible state.
			// Just keep pressure on it instead of freezing it in place.
			entity.motionX = dx * step;
			entity.motionY = dy * step * 0.20D;
			entity.motionZ = dz * step;
		}
	}

	private boolean canEntitySeeTarget(EntityCreature entity, EntityPlayer target) {
		Vec3 start = Vec3.createVectorHelper(
			entity.posX,
			entity.posY + entity.getEyeHeight(),
			entity.posZ
		);

		Vec3 end = Vec3.createVectorHelper(
			target.posX,
			target.posY + target.getEyeHeight(),
			target.posZ
		);

		return entity.worldObj.rayTraceBlocks(start, end) == null;
	}

	private boolean isPlayerLookingAtEntity(EntityPlayer player) {
		double dx = entity.posX - player.posX;
		double dy = (entity.posY + entity.height / 2.0) - (player.posY + player.getEyeHeight());
		double dz = entity.posZ - player.posZ;

		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist == 0) return true;

		dx /= dist;
		dy /= dist;
		dz /= dist;

		double lookX = player.getLookVec().xCoord;
		double lookY = player.getLookVec().yCoord;
		double lookZ = player.getLookVec().zCoord;

		double dot = dx * lookX + dy * lookY + dz * lookZ;
		return dot > 0.85D;
	}

	private void applyEffects(EntityPlayer player, double distSq) {
		if (distSq < 36) {
			player.addPotionEffect(new PotionEffect(Potion.confusion.id, 40, 0));
		}

		if (distSq < 144) {
			player.addPotionEffect(new PotionEffect(Potion.weakness.id, 40, 0));
		}

		if (distSq < 900) {
			player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 60, 0));
		}
	}

	private final Map<String, Float> breakProgress = new HashMap<String, Float>();

	private boolean canMoveForward(double dx, double dy, double dz) {
		int bx = (int)Math.floor(entity.posX + dx);
		int by = (int)Math.floor(entity.posY + dy);
		int bz = (int)Math.floor(entity.posZ + dz);

		for (int xOff = -1; xOff <= 1; xOff++) {
			for (int zOff = -1; zOff <= 1; zOff++) {
				for (int yOff = 0; yOff < 2; yOff++) {
					int x = bx + xOff;
					int y = by + yOff;
					int z = bz + zOff;

					Block block = entity.worldObj.getBlock(x, y, z);

					if (block != Blocks.air && block.getMaterial().blocksMovement()) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private void carveTunnel(double dx, double dy, double dz) {
		int steps = 2;

		for (int i = 0; i <= steps; i++) {
			int bx = (int)Math.floor(entity.posX + dx * i);
			int by = (int)Math.floor(entity.posY + dy * i);
			int bz = (int)Math.floor(entity.posZ + dz * i);

			for (int xOff = -1; xOff <= 1; xOff++) {
				for (int zOff = -1; zOff <= 1; zOff++) {
					for (int yOff = 0; yOff < 4; yOff++) {
						int x = bx + xOff;
						int y = by + yOff;
						int z = bz + zOff;

						Block block = entity.worldObj.getBlock(x, y, z);

						if (block != Blocks.air &&
							block != Blocks.bedrock &&
							block.getMaterial().blocksMovement()) {

							float hardness = block.getBlockHardness(entity.worldObj, x, y, z);
							if (hardness < 0) continue;

							String key = x + "," + y + "," + z;
							float progress = breakProgress.containsKey(key) ? breakProgress.get(key) : 0F;

							float breakSpeed = 0.02F / (hardness + 0.1F);
							progress += breakSpeed;

							if (progress >= 1.0F) {
								entity.worldObj.func_147480_a(x, y, z, true);
								breakProgress.remove(key);
								entity.worldObj.destroyBlockInWorldPartially(entity.getEntityId(), x, y, z, -1);
							} else {
								breakProgress.put(key, progress);
								int stage = (int)(progress * 10F);
								if (stage < 0) stage = 0;
								if (stage > 9) stage = 9;
								entity.worldObj.destroyBlockInWorldPartially(entity.getEntityId(), x, y, z, stage);
							}
						}
					}
				}
			}
		}
	}
}
