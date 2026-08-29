package com.hbm.blocks.gas;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSupercriticalWater extends Block {

	@SideOnly(Side.CLIENT)
	private IIcon icon;

	public BlockSupercriticalWater() {

		super(Material.water);

		this.setBlockName(
			"supercriticalWater"
		);

		this.setHardness(-1.0F);
		this.setResistance(6000000F);
		this.setLightOpacity(4);
		this.setTickRandomly(false);
	}

	@Override
	public int getRenderType() {
		return 4;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
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
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {

		// Supercritical water has gas-like diffusivity but liquid-like density.
		entity.motionX *= 0.58D;
		entity.motionY *= 0.58D;
		entity.motionZ *= 0.58D;
		entity.motionY -= 0.012D;
		entity.fallDistance = 0F;

		if(entity instanceof EntityLivingBase) {

			((EntityLivingBase)entity).attackEntityFrom(
				DamageSource.generic,
				2.0F
			);
		}
	}

	@Override
	public boolean shouldSideBeRendered(
		IBlockAccess world,
		int x,
		int y,
		int z,
		int side
	) {
		Block adjacent =
			world.getBlock(x, y, z);

		return adjacent != this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(
		IIconRegister reg
	) {

		this.icon =
			reg.registerIcon(
				"minecraft:water_still"
			);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(
		int side,
		int meta
	) {
		return this.icon;
	}
}
