package com.hbm.blocks.gas;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockNeptuneCloud extends BlockGasBase {

	public BlockNeptuneCloud() {
		super(0.08F, 0.19F, 0.62F);
		this.setBlockName("neptune_cloud");
		this.setBlockTextureName("hbm:neptune_cloud");
		this.setLightOpacity(1);
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
		if(world.rand.nextInt(10) == 0)
			return world.rand.nextBoolean() ? ForgeDirection.UP : ForgeDirection.DOWN;

		return randomHorizontal(world);
	}

	@Override
	public int getDelay(World world) {
		return 2;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		entity.motionX *= 0.96D;
		entity.motionY *= 0.99D;
		entity.motionZ *= 0.96D;

		double jet = Math.sin(z * 0.004D) * 0.18D;
		entity.motionX += jet;
		entity.motionZ += (world.rand.nextDouble() - 0.5D) * 0.04D;
		entity.fallDistance = 0F;
	}
}
