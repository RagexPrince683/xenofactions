package com.hbm.blocks.gas;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockNeptuneCloudThin extends BlockGasBase {

	public BlockNeptuneCloudThin() {
		super(0.10F, 0.22F, 0.70F);
		this.setBlockName("neptune_cloud_thin");
		this.setBlockTextureName("hbm:neptune_cloud_thin");
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
		return randomHorizontal(world);
	}

	@Override
	public int getDelay(World world) {
		return 3;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		entity.motionX *= 0.985D;
		entity.motionZ *= 0.985D;

		double wind = Math.sin(z * 0.006D + y * 0.03D) * 0.08D;
		entity.motionX += wind;
		entity.fallDistance = 0F;
	}
}
