package com.hbm.blocks.gas;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class BlockUranusCloudDense
	extends BlockUranusCloud {

	public BlockUranusCloudDense() {

		super();

		this.setBlockName(
			"uranusCloudDense"
		);

		this.setLightOpacity(2);
	}

	@Override
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {

		entity.motionX *= 0.94D;
		entity.motionZ *= 0.94D;

		entity.motionY *= 0.98D;

		double wind =
			Math.sin(z * 0.003D)
				* 0.08D;

		entity.motionX += wind;
	}
}
