package com.hbm.blocks.gas;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockNeptuneStorm extends BlockGasBase {

	public BlockNeptuneStorm() {
		super(0.02F, 0.08F, 0.34F);
		this.setBlockName("neptune_storm");
		this.setBlockTextureName("hbm:neptune_storm");
		this.setLightOpacity(4);
	}

	@Override
	public boolean isStaticGas() {
		return true;
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {
		return randomHorizontal(world);
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return ForgeDirection.getOrientation(world.rand.nextInt(6));
	}

	@Override
	public int getDelay(World world) {
		return 1;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		entity.motionX *= 0.88D;
		entity.motionY *= 0.93D;
		entity.motionZ *= 0.88D;

		double angle = x * 0.012D + z * 0.017D;
		entity.motionX += Math.cos(angle) * 0.45D + (world.rand.nextDouble() - 0.5D) * 0.35D;
		entity.motionZ += Math.sin(angle) * 0.45D + (world.rand.nextDouble() - 0.5D) * 0.35D;
		entity.motionY += (world.rand.nextDouble() - 0.45D) * 0.16D;
		entity.fallDistance = 0F;

		if(entity instanceof EntityLivingBase && world.rand.nextInt(60) == 0) {
			((EntityLivingBase)entity).attackEntityFrom(DamageSource.magic, 1.0F);
		}
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);

		EntityPlayer player = world.getClosestPlayer(x + 0.5D, y + 0.5D, z + 0.5D, 18.0D);

		if(player == null)
			return;

		if(rand.nextInt(80) == 0) {
			world.spawnParticle(
				"reddust",
				x + rand.nextDouble(),
				y + rand.nextDouble(),
				z + rand.nextDouble(),
				0.0D,
				0.0D,
				1.0D
			);
		}
	}
}
