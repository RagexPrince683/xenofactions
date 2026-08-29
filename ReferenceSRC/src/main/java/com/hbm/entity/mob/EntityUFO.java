package com.hbm.entity.mob;

import java.util.ArrayList;
import java.util.List;

import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import api.hbm.entity.IRadiationImmune;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityUFO extends EntityFlying implements IBossDisplayData, IRadiationImmune {

	public int courseChangeCooldown;
	public int scanCooldown;
	/*public double waypointX;
	public double waypointY;
	public double waypointZ;*/
	public int hurtCooldown;
	public int beamTimer;
	private Entity target;
	private List<Entity> secondaries = new ArrayList();

	public EntityUFO(World p_i1587_1_) {
		super(p_i1587_1_);
		this.setSize(25F, 4F);
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

		if(!this.worldObj.isRemote) {

			if(this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}

			if(this.hurtCooldown > 0) {
				this.hurtCooldown--;
			}
		}

		if(this.courseChangeCooldown > 0) {
			this.courseChangeCooldown--;
		}
		if(this.scanCooldown > 0) {
			this.scanCooldown--;
		}

		if(this.target != null && !this.target.isEntityAlive()) {
			this.target = null;
		}



		if(this.target != null && this.courseChangeCooldown <= 0) {

			Vec3 vec = Vec3.createVectorHelper(this.posX - this.target.posX, 0, this.posZ - this.target.posZ);

			if(rand.nextInt(3) > 0)
				vec.rotateAroundY((float)Math.PI * 2 * rand.nextFloat());

			double length = vec.lengthVector();
			double overshoot = 35;

			int wX = (int)Math.floor(this.target.posX - vec.xCoord / length * overshoot);
			int wZ = (int)Math.floor(this.target.posZ - vec.zCoord / length * overshoot);

			this.setWaypoint(wX, Math.max(this.worldObj.getHeightValue(wX, wZ) + 20 + rand.nextInt(15), (int) this.target.posY + 15),  wZ);

			this.courseChangeCooldown = 40 + rand.nextInt(20);
		}

		if(!worldObj.isRemote) {




		}

		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		if(this.courseChangeCooldown > 0) {

			double deltaX = this.getX() - this.posX;
			double deltaY = this.getY() - this.posY;
			double deltaZ = this.getZ() - this.posZ;
			Vec3 delta = Vec3.createVectorHelper(deltaX, deltaY, deltaZ);
			double len = delta.lengthVector();
			double speed = this.target instanceof EntityPlayer ? 5D : 2D;

			if(len > 5) {
				if(isCourseTraversable(this.getX(), this.getY(), this.getZ(), len)) {
					this.motionX = delta.xCoord * speed / len;
					this.motionY = delta.yCoord * speed / len;
					this.motionZ = delta.zCoord * speed / len;
				} else {
					this.courseChangeCooldown = 0;
				}
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
