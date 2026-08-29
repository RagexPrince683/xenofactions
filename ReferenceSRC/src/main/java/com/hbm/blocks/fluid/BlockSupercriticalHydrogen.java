package com.hbm.blocks.fluid;

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

public class BlockSupercriticalHydrogen extends Block {

	@SideOnly(Side.CLIENT)
	protected IIcon icon;

	public BlockSupercriticalHydrogen() {

		super(Material.water);

		this.setBlockName(
			"supercritical_hydrogen"
		);

		this.setHardness(-1.0F);
		this.setResistance(6000000F);
		this.setLightOpacity(2);
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

		// Dense nonpolar fluid: very low viscosity chemically, but crushing pressure dominates here.
		entity.motionX *= 0.08D;
		entity.motionY *= 0.08D;
		entity.motionZ *= 0.08D;
		entity.motionY -= 0.008D;
		entity.fallDistance = 0F;

		if(entity instanceof EntityLivingBase) {

			((EntityLivingBase)entity).attackEntityFrom(
				DamageSource.generic,
				4.0F
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
	public boolean canCollideCheck(
		int meta,
		boolean hitIfLiquid
	) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(
		IIconRegister reg
	) {
		icon = reg.registerIcon(
			"minecraft:water_still"
		);
	}

	@Override
	public IIcon getIcon(
		int side,
		int meta
	) {
		return icon;
	}
}
