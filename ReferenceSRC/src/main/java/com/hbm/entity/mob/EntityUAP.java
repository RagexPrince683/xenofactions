package com.hbm.entity.mob;

import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.items.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityUAP extends EntityFlying implements IMob {

	public int courseChangeCooldown;
	public int scanCooldown;
	/*public double waypointX;
	public double waypointY;
	public double waypointZ;*/
	public int hurtCooldown;
	private Entity target;
	private List<Entity> secondaries = new ArrayList();

	public EntityUAP(World p_i1587_1_) {
		super(p_i1587_1_);
		this.setSize(6F, 6F);
		this.isImmuneToFire = true;
		this.experienceValue = 500;
		this.ignoreFrustumCheck = true;
		this.deathTime = -30;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(hurtCooldown > 0)
			return false;

		boolean hit = super.attackEntityFrom(source, amount);

		if(hit)
			hurtCooldown = 5;

		return hit;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20000.0D);
	}

	@Override
	protected void updateEntityActionState() {

		if (!this.worldObj.isRemote) {
			if (this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}
			if (this.hurtCooldown > 0) this.hurtCooldown--;
		}

		if (this.courseChangeCooldown > 0) this.courseChangeCooldown--;
		if (this.scanCooldown > 0) this.scanCooldown--;

		if (this.target != null && !this.target.isEntityAlive()) {
			this.target = null;
		}

		// Random teleportation (under 5% chance), avoid player proximity
		if (rand.nextInt(200) < 1) {
			double tx = this.posX + rand.nextInt(40) - 20;
			double ty = this.posY + rand.nextInt(20) - 10;
			double tz = this.posZ + rand.nextInt(40) - 20;
			EntityPlayer closestPlayer = this.worldObj.getClosestPlayer(tx, ty, tz, 10.0D);

			if (closestPlayer == null && worldObj.isAirBlock((int) tx, (int) ty, (int) tz)) {
				this.setPosition(tx, ty, tz);
				this.motionX = 0;
				this.motionY = 0;
				this.motionZ = 0;
				this.courseChangeCooldown = 10 + rand.nextInt(10);
				return;
			}
		}

		// Waypoint logic
		if (this.courseChangeCooldown <= 0) {
			if (this.target != null) {
				double distance = this.getDistanceToEntity(this.target);

				// Always maintain 8 block radius
				if (distance < 10.0D) {
					Vec3 escapeVec = Vec3.createVectorHelper(this.posX - this.target.posX, 0, this.posZ - this.target.posZ);
					escapeVec = escapeVec.normalize();

					double escapeX = this.posX + escapeVec.xCoord * 30 + (rand.nextDouble() - 0.5D) * 20;
					double escapeZ = this.posZ + escapeVec.zCoord * 30 + (rand.nextDouble() - 0.5D) * 20;
					int wX = (int) escapeX;
					int wZ = (int) escapeZ;
					int groundY = this.worldObj.getHeightValue(wX, wZ);
					int wY = groundY + 20 + rand.nextInt(61);

					this.setWaypoint(wX, wY, wZ);
				} else {
					Vec3 vec = Vec3.createVectorHelper(this.posX - this.target.posX, 0, this.posZ - this.target.posZ);
					vec = vec.normalize();
					vec.rotateAroundY((float)(Math.PI * 2 * rand.nextDouble()));

					double overshoot = 30 + rand.nextInt(20);
					int wX = (int)(this.target.posX - vec.xCoord * overshoot + rand.nextInt(10) - 5);
					int wZ = (int)(this.target.posZ - vec.zCoord * overshoot + rand.nextInt(10) - 5);
					int groundY = this.worldObj.getHeightValue(wX, wZ);
					int wY = groundY + 20 + rand.nextInt(61);

					// Distance check from target's future location
					double futureDist = this.target.getDistanceSq(wX + 0.5D, wY + 0.5D, wZ + 0.5D);
					if (futureDist >= 64.0D) { // 8 blocks squared
						this.setWaypoint(wX, wY, wZ);
					} else {
						this.setWaypoint((int)(this.posX + (rand.nextDouble() - 0.5D) * 80),
							this.worldObj.getHeightValue((int)this.posX, (int)this.posZ) + 30 + rand.nextInt(40),
							(int)(this.posZ + (rand.nextDouble() - 0.5D) * 80));
					}
				}
			} else {
				int wX = (int)(this.posX + rand.nextInt(60) - 30);
				int wZ = (int)(this.posZ + rand.nextInt(60) - 30);
				int groundY = this.worldObj.getHeightValue(wX, wZ);
				int wY = groundY + 20 + rand.nextInt(61);
				this.setWaypoint(wX, wY, wZ);
			}
			this.courseChangeCooldown = 20 + rand.nextInt(20);
		}

		// Block motion update if too close
		if (this.target != null && this.getDistanceToEntity(this.target) < 8.0D) {
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
			this.courseChangeCooldown = 0; // Trigger immediate reevaluation next tick
			return;
		}

		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		// Motion application
		if (this.courseChangeCooldown > 0) {
			double deltaX = this.getX() - this.posX;
			double deltaY = this.getY() - this.posY;
			double deltaZ = this.getZ() - this.posZ;
			Vec3 delta = Vec3.createVectorHelper(deltaX, deltaY, deltaZ);
			double len = delta.lengthVector();

			if (this.target != null && this.getDistanceToEntity(this.target) < 8.0D) return;

			double baseSpeed = this.target instanceof EntityPlayer ? 4D : 1.5D;
			double speed = baseSpeed + rand.nextDouble() * 2.0;

			if (len > 4 && isCourseTraversable(this.getX(), this.getY(), this.getZ(), len)) {
				this.motionX = delta.xCoord * speed / len;
				this.motionY = delta.yCoord * speed / len;
				this.motionZ = delta.zCoord * speed / len;
			} else {
				this.courseChangeCooldown = 0;
			}
		}
	}

	protected void onDeathUpdate() {

		if(this.getBeam())
			this.setBeam(false);

		this.motionY -= 0.05D;

		if(this.deathTime == -10) {
			worldObj.playSoundAtEntity(this, "hbm:entity.chopperDamage", 10.0F, 1.0F);
		}

		if(this.deathTime == 19 && !worldObj.isRemote) {
			//worldObj.newExplosion(this, posX, posY, posZ, 10F, true, true);
			//ExplosionNukeSmall.explode(worldObj, posX, posY, posZ, ExplosionNukeSmall.PARAMS_MEDIUM);

			List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(200, 200, 200));

			for(EntityPlayer player : players) {
				//player.triggerAchievement(MainRegistry.bossUFO);
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.coin_ufo));
			}
		}

		super.onDeathUpdate();
	}



	@Override
	public boolean canAttackClass(Class clazz) {
		return clazz != this.getClass() && clazz != EntityBulletBaseNT.class;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, Byte.valueOf((byte) 0));
		this.dataWatcher.addObject(17, 0);
		this.dataWatcher.addObject(18, 0);
		this.dataWatcher.addObject(19, 0);
	}

	private boolean isCourseTraversable(double p_70790_1_, double p_70790_3_, double p_70790_5_, double p_70790_7_) {

		double d4 = (this.getX() - this.posX) / p_70790_7_;
		double d5 = (this.getY() - this.posY) / p_70790_7_;
		double d6 = (this.getZ() - this.posZ) / p_70790_7_;
		AxisAlignedBB axisalignedbb = this.boundingBox.copy();

		for(int i = 1; i < p_70790_7_; ++i) {
			axisalignedbb.offset(d4, d5, d6);

			if(!this.worldObj.getCollidingBoundingBoxes(this, axisalignedbb).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected float getSoundVolume() {
		return 10.0F;
	}

	@Override
	protected String getHurtSound() {
		return "mob.blaze.hit";
	}

	@Override
	protected String getDeathSound() {
		return null;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		super.writeEntityToNBT(p_70014_1_);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		super.readEntityFromNBT(p_70037_1_);
	}

	public void setBeam(boolean b) {
		this.dataWatcher.updateObject(16, Byte.valueOf((byte) (b ? 1 : 0)));
	}

	public boolean getBeam() {
		return this.dataWatcher.getWatchableObjectByte(16) == 1;
	}

	public void setWaypoint(int x, int y, int z) {
		this.dataWatcher.updateObject(17, x);
		this.dataWatcher.updateObject(18, y);
		this.dataWatcher.updateObject(19, z);
	}

	@Override
	public int getBrightnessForRender(float partialTicks) {
		return 15728880; // Maximum light level
	}

	@Override
	public float getBrightness(float partialTicks) {
		return 1.0F;
	}

	public int getX() {
		return this.dataWatcher.getWatchableObjectInt(17);
	}

	public int getY() {
		return this.dataWatcher.getWatchableObjectInt(18);
	}

	public int getZ() {
		return this.dataWatcher.getWatchableObjectInt(19);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}

}
