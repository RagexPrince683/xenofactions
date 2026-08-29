package com.hbm.blocks.gas;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockSaturnStorm extends BlockGasBase {

	public BlockSaturnStorm() {

		super(0.90F, 0.84F, 0.62F);

		this.setBlockName("saturn_storm");
		this.setBlockTextureName("hbm:saturn_storm");
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

		// mostly horizontal jet flow with slight instability
		if(world.rand.nextInt(6) == 0) {
			return world.rand.nextBoolean()
				? ForgeDirection.UP
				: ForgeDirection.DOWN;
		}

		return randomHorizontal(world);
	}

	@Override
	public int getDelay(World world) {
		return 2;
	}

	@Override
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {

		// =========================
		// BASE ATMOSPHERIC DRAG
		// =========================
		entity.motionX *= 0.97D;
		entity.motionY *= 0.99D;
		entity.motionZ *= 0.97D;

		// =========================
		// SATURN JET STREAM (MAIN FORCE)
		// smooth, continuous flow
		// =========================
		double angle = (x * 0.01D) + (z * 0.01D);

		double jetStrength = 0.12D;

		entity.motionX += Math.cos(angle) * jetStrength;
		entity.motionZ += Math.sin(angle) * jetStrength;

		// =========================
		// SMALL TURBULENCE
		// =========================
		entity.motionX += (world.rand.nextDouble() - 0.5D) * 0.03D;
		entity.motionZ += (world.rand.nextDouble() - 0.5D) * 0.03D;

		entity.motionY += (world.rand.nextDouble() - 0.5D) * 0.02D;

		entity.fallDistance = 0F;

		// =========================
		// RARE STORM BURST
		// =========================
		if(world.rand.nextInt(200) == 0) {

			entity.motionX += Math.cos(angle) * 0.35D;
			entity.motionZ += Math.sin(angle) * 0.35D;
			entity.motionY += 0.15D;
		}

		// =========================
		// COLD / PRESSURE DAMAGE
		// =========================
		if(entity instanceof EntityLivingBase
			&& world.rand.nextInt(120) == 0) {

			((EntityLivingBase)entity)
				.attackEntityFrom(
					DamageSource.drown,
					1.0F
				);
		}

		// =========================
		// VERY RARE ELECTRICAL DISCHARGE
		// =========================
		if(entity instanceof EntityLivingBase
			&& world.rand.nextInt(300) == 0) {

			((EntityLivingBase)entity)
				.attackEntityFrom(
					DamageSource.magic,
					1.0F
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

		super.randomDisplayTick(world, x, y, z, rand);

		EntityPlayer p =
			world.getClosestPlayer(
				x + 0.5,
				y + 0.5,
				z + 0.5,
				16
			);

		if(p == null)
			return;

		// faint haze
		if(rand.nextInt(60) == 0) {

			world.spawnParticle(
				"cloud",
				x + rand.nextDouble(),
				y + rand.nextDouble(),
				z + rand.nextDouble(),
				0.0D,
				0.005D,
				0.0D
			);
		}

		// rare static
		if(rand.nextInt(240) == 0) {

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
