package com.hbm.blocks.fluid;

import com.hbm.blocks.gas.BlockGasBase;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDenseCloud extends BlockGasBase {

	public BlockDenseCloud() {
		super(0.85F, 0.78F, 0.68F);

		this.setBlockName("cloud_dense");
		this.setBlockTextureName(
			"hbm:dense_stormcloud"
		);
	}

	@Override
	public boolean isStaticGas() {
		return true;
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

		// very slow drift
		if(world.rand.nextInt(8) == 0)
			return ForgeDirection.UP;

		return randomHorizontal(world);
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return ForgeDirection.UNKNOWN;
	}

	@Override
	public int getDelay(World world) {
		return 8;
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
		entity.motionX *= 0.85D;
		entity.motionZ *= 0.85D;

		// strong buoyancy
		if(entity.motionY < -0.1D)
			entity.motionY *= 0.35D;

		entity.fallDistance = 0F;
	}
}
