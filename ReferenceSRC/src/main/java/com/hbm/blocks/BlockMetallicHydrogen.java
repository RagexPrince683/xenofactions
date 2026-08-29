package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class BlockMetallicHydrogen extends Block {

	public BlockMetallicHydrogen() {

		super(Material.water);

		this.setBlockName(
			"metallic_hydrogen"
		);

		this.setBlockTextureName(
			"minecraft:water_still"
		);

		this.setHardness(-1.0F);
		this.setResistance(
			999999F
		);

		this.setLightLevel(
			0.05F
		);
		this.setLightOpacity(10);
	}





	@Override
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {

		// Metallic hydrogen is a conductive degenerate fluid under extreme pressure.
		entity.motionX *= 0.02D;
		entity.motionY *= 0.02D;
		entity.motionZ *= 0.02D;
		entity.motionY -= 0.02D;
		entity.fallDistance = 0F;

		if(entity instanceof EntityLivingBase) {

			((EntityLivingBase)entity).attackEntityFrom(
				DamageSource.generic,
				20.0F
			);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(
		World world,
		int x,
		int y,
		int z
	) {
		return null;
	}

	@Override
	public boolean canCollideCheck(
		int meta,
		boolean hitIfLiquid
	) {
		return false;
	}


}
