package com.hbm.blocks.fluid;

import com.hbm.blocks.gas.BlockGasBase;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockCloud extends BlockGasBase {

	public BlockCloud() {
		super(0.95F, 0.92F, 0.85F);

		this.setBlockName("cloud");
		this.setLightOpacity(0);
		this.setBlockTextureName(
			"hbm:stormcloud"
		);
	}

	@Override
	public boolean isStaticGas() {
		return true;
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

		// weak turbulence
		if(world.rand.nextInt(4) == 0)
			return ForgeDirection.UP;

		return ForgeDirection.getOrientation(
			world.rand.nextInt(6)
		);
	}

	@Override
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {
		entity.motionY *= 0.98;
		entity.fallDistance *= 0.9F;
	}
}
