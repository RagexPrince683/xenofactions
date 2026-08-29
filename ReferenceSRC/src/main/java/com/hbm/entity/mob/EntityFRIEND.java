package com.hbm.entity.mob;

import com.hbm.dim.laythe.WorldProviderLaythe;
import com.hbm.entity.mob.ai.EntityAIDigToPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

public class EntityFRIEND extends EntityCreature {

	public EntityFRIEND(World world) {
		super(world);

		this.tasks.addTask(0, new EntityAIDigToPlayer(this, 0.1D, 160.0D));
		this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 64.0F));
		this.tasks.addTask(2, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(4, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

		this.renderDistanceWeight *= 10;
		this.setSize(0.6F, 3.3F);
	}

	//	@Override
//	public boolean getCanSpawnHere() {
//
//		// Only in Laythe (europa)
//		if (!(this.worldObj.provider instanceof WorldProviderLaythe)) return false;
//
//		// Only below Y = 50
//		if (this.posY >= 50) return false;
//
//		// 1% chance
//		//if (this.rand.nextInt(100) != 0) return false;
//
//		// Check for other FRIEND entities
//		//double range = 64;
//		//List<EntityFRIEND> friends = this.worldObj.getEntitiesWithinAABB(
//		//	EntityFRIEND.class,
//		//	this.boundingBox.expand(range, range, range)
//		//);
////
//		//for (EntityFRIEND f : friends) {
//		//	if (f != this) return false;
//		//}
//		//done in ntmworldgenerator
//
//		return super.getCanSpawnHere();
//	}

	// ✅ LIMIT TO ONE ENTITY NEARBY
	@Override
	public boolean getCanSpawnHere() {

		double range = 64;

		List<EntityFRIEND> list = this.worldObj.getEntitiesWithinAABB(
			EntityFRIEND.class,
			this.boundingBox.expand(range, range, range)
		);

		for (EntityFRIEND e : list) {
			if (e != this) return false;
		}

		return super.getCanSpawnHere();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
		this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1.0D);
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	private int teleportCooldown = 0;

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (teleportCooldown > 0) teleportCooldown--;

		if (forcedAggroTime > 0) {
			forcedAggroTime--;

			if (forcedTarget != null) {
				this.getNavigator().tryMoveToEntityLiving(forcedTarget, 1.2D);
			}
		} else {
			forcedTarget = null;
		}

	}

	// ✅ REAL DAMAGE
	@Override
	public boolean attackEntityAsMob(Entity entity) {

		boolean hit = entity.attackEntityFrom(DamageSource.causeMobDamage(this), 6.0F);

		if (hit && entity instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) entity;

			// teleport after landing hit
			if (!this.worldObj.isRemote && teleportCooldown <= 0) {
				this.teleportUndergroundNearPlayer(player);
				teleportCooldown = 40; // 2 seconds
			}
		}

		return hit;
	}

	public int forcedAggroTime = 0;
	private EntityPlayer forcedTarget = null;

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		Entity src = source.getSourceOfDamage();

		//50%


			// Handle projectiles FIRST (like Enderman)
			if (src instanceof net.minecraft.entity.projectile.EntityArrow
				|| src instanceof net.minecraft.entity.projectile.EntityThrowable) {

				if (!this.worldObj.isRemote) {

					EntityPlayer player = null;

					if (src instanceof net.minecraft.entity.projectile.EntityArrow) {
						net.minecraft.entity.projectile.EntityArrow arrow = (net.minecraft.entity.projectile.EntityArrow) src;
						if (arrow.shootingEntity instanceof EntityPlayer) {
							player = (EntityPlayer) arrow.shootingEntity;
						}
					} else if (src instanceof net.minecraft.entity.projectile.EntityThrowable) {
						net.minecraft.entity.projectile.EntityThrowable throwable = (net.minecraft.entity.projectile.EntityThrowable) src;
						if (throwable.getThrower() instanceof EntityPlayer) {
							player = (EntityPlayer) throwable.getThrower();
						}
					}

					// 50/50 behavior (FIXED RNG)
					if (this.rand.nextBoolean()) {

						boolean success = false;

						if (player != null) {
							for (int i = 0; i < 16; i++) {
								if (this.teleportUndergroundNearPlayer(player)) {
									success = true;
									break;
								}
							}
						}

						// fallback if no player OR teleport failed
						if (!success) {
							for (int i = 0; i < 16; i++) {
								if (this.teleportRandomly()) break;
							}
						}

					} else {
						// move toward player (creepy pressure)
						if (player != null) {
							this.forcedAggroTime = 60; // 3 seconds
							this.forcedTarget = player;
						}
					}
				}

				return false; // always cancel projectile damage
			}


		return super.attackEntityFrom(source, amount);
	}

	private boolean teleportRandomly() {

		double range = 16;

		for (int i = 0; i < 16; i++) {

			int x = (int)(this.posX + (rand.nextDouble() - 0.5D) * range);
			int z = (int)(this.posZ + (rand.nextDouble() - 0.5D) * range);
			int y = worldObj.getTopSolidOrLiquidBlock(x, z);

			if (y > 0 && y < worldObj.getHeight()) {
				this.setPosition(x + 0.5, y, z + 0.5);
				return true;
			}
		}

		return false;
	}

	public boolean teleportUndergroundNearPlayer(EntityPlayer player) {

		int range = 32;

		for (int i = 0; i < 32; i++) {

			int x = (int)(player.posX + (rand.nextDouble() - 0.5) * range);
			int z = (int)(player.posZ + (rand.nextDouble() - 0.5) * range);

			int topY = worldObj.getTopSolidOrLiquidBlock(x, z);
			int y = topY - (10 + rand.nextInt(20));
			if (y < 5) y = 5;

			if (isSafeTeleportSpot(x, y, z)) {
				this.setPosition(x + 0.5, y, z + 0.5);
				return true;
			}

			if (placeUnderground(x, y, z)) {
				return true;
			}
		}

		return false;
	}

	private boolean isSafeTeleportSpot(int x, int y, int z) {

		if(y <= 0 || y >= worldObj.getHeight()) return false;

		if(!worldObj.isAirBlock(x, y, z)) return false;
		if(!worldObj.isAirBlock(x, y + 1, z)) return false;
		if(!worldObj.isAirBlock(x, y + 2, z)) return false;

		if(!worldObj.getBlock(x, y - 1, z).getMaterial().isSolid()) return false;

		return true;
	}

	private boolean placeUnderground(int x, int y, int z) {

		if (y <= 1 || y >= worldObj.getHeight() - 2) return false;

		if (!worldObj.getBlock(x, y - 1, z).getMaterial().isSolid()) return false;

		worldObj.setBlockToAir(x, y, z);
		worldObj.setBlockToAir(x, y + 1, z);
		worldObj.setBlockToAir(x, y + 2, z);

		this.setPosition(x + 0.5, y, z + 0.5);
		return true;
	}



	@Override
	public boolean isEntityInvulnerable() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}
}
