package com.hbm.blocks.gas;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockNeptuneCloudDense extends BlockGasBase {

	public BlockNeptuneCloudDense() {
		super(0.04F, 0.12F, 0.40F);
		this.setBlockName("neptune_cloud_dense");
		this.setBlockTextureName("hbm:neptune_cloud_dense");
		this.setLightOpacity(3);
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
		return world.rand.nextInt(8) == 0
			? ForgeDirection.DOWN
			: randomHorizontal(world);
	}

	@Override
	public int getDelay(World world) {
		return 2;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		entity.motionX *= 0.91D;
		entity.motionY *= 0.96D;
		entity.motionZ *= 0.91D;

		double jet = Math.sin(z * 0.003D + y * 0.02D) * 0.28D;
		entity.motionX += jet;
		entity.motionZ += (world.rand.nextDouble() - 0.5D) * 0.10D;
		entity.motionY -= 0.006D;
		entity.fallDistance = 0F;
	}
}
