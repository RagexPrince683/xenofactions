package com.hbm.blocks.gas;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class BlockUranusAtmosphere
	extends BlockUranusCloudDense {

	public BlockUranusAtmosphere() {

		super();

		this.setBlockName(
			"uranusAtmosphere"
		);

		this.setLightOpacity(3);
	}

	@Override
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {

		entity.motionX *= 0.90D;
		entity.motionZ *= 0.90D;

		entity.motionY *= 0.90D;

		// sinking
		entity.motionY -= 0.015D;

		// smooth zonal wind
		double wind =
			Math.sin(z * 0.0015D)
				* 0.12D;

		entity.motionX += wind;
	}
}
