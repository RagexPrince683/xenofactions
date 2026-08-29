package com.hbm.blocks.gas;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockJupiterStorm extends BlockGasBase {

	public BlockJupiterStorm() {
		super(0.72F, 0.58F, 0.42F);

		this.setBlockName("jupiter_storm");
		this.setBlockTextureName(
			"hbm:jupiter_storm"
		);
	}

	@Override
	public boolean isStaticGas() {
		return true;
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

		// violent turbulence
		return ForgeDirection.getOrientation(
			world.rand.nextInt(6)
		);
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return randomHorizontal(world);
	}

	@Override
	public int getDelay(World world) {
		return 1;
	}

	@Override
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {

		// dense atmosphere drag
		entity.motionX *= 0.92D;
		entity.motionY *= 0.95D;
		entity.motionZ *= 0.92D;

		// violent horizontal gusts
		entity.motionX +=
			(world.rand.nextDouble() - 0.5D)
				* 0.8D;

		entity.motionZ +=
			(world.rand.nextDouble() - 0.5D)
				* 0.8D;

		// turbulent vertical motion
		entity.motionY +=
			(world.rand.nextDouble() - 0.35D)
				* 0.25D;

		// prevent normal falling
		entity.fallDistance = 0F;

		if(world.rand.nextInt(50) == 0) {

			entity.motionX +=
				(world.rand.nextDouble() - 0.5D)
					* 3.0D;

			entity.motionY +=
				world.rand.nextDouble()
					* 1.2D;

			entity.motionZ +=
				(world.rand.nextDouble() - 0.5D)
					* 3.0D;
		}

		// lightning / electrical discharge
		if(entity instanceof EntityLivingBase
			&& world.rand.nextInt(35) == 0) {

			((EntityLivingBase)entity)
				.attackEntityFrom(
					DamageSource.magic,
					2.0F
				);
		}
	}

	@Override
	public void randomDisplayTick(
		World world,
		int x,
		int y,
		int z,
		Random rand
	) {

		super.randomDisplayTick(
			world,
			x,
			y,
			z,
			rand
		);

		EntityPlayer p =
			world.getClosestPlayer(
				x + 0.5,
				y + 0.5,
				z + 0.5,
				16
			);

		if(p == null)
			return;

		// electric storm particles
		if(rand.nextInt(120) == 0) {

			world.spawnParticle(
				"reddust",
				x + rand.nextDouble(),
				y + rand.nextDouble(),
				z + rand.nextDouble(),
				0.0D,
				0.0D,
				0.0D
			);
		}
	}
}
