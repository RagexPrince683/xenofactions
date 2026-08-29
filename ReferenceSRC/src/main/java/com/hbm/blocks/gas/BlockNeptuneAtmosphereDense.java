package com.hbm.blocks.gas;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockNeptuneAtmosphereDense extends BlockGasBase {

	public BlockNeptuneAtmosphereDense() {
		super(0.015F, 0.045F, 0.16F);
		this.setBlockName("neptune_atmosphere_dense");
		this.setBlockTextureName("hbm:neptune_atmosphere_dense");
		this.setLightOpacity(6);
	}

	@Override
	public boolean isStaticGas() {
		return true;
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {
		return ForgeDirection.DOWN;
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return randomHorizontal(world);
	}

	@Override
	public int getDelay(World world) {
		return 2;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		entity.motionX *= 0.82D;
		entity.motionY *= 0.82D;
		entity.motionZ *= 0.82D;

		entity.motionX += Math.sin(z * 0.002D) * 0.20D;
		entity.motionY -= 0.02D;
		entity.fallDistance = 0F;

		if(entity instanceof EntityLivingBase && world.rand.nextInt(100) == 0) {
			((EntityLivingBase)entity).attackEntityFrom(DamageSource.drown, 1.0F);
		}
	}
}
