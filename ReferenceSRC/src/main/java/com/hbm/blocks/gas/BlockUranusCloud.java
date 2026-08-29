package com.hbm.blocks.gas;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockUranusCloud extends Block {

	@SideOnly(Side.CLIENT)
	private IIcon icon;

	public BlockUranusCloud() {

		super(Material.air);

		this.setBlockName(
			"uranusCloud"
		);

		this.setHardness(-1.0F);
		this.setResistance(6000000F);

		this.setLightOpacity(1);

		this.setTickRandomly(false);
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public AxisAlignedBB
	getCollisionBoundingBoxFromPool(
		World world,
		int x,
		int y,
		int z
	) {
		return null;
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
	public int getRenderType() {
		return 4;
	}

	@Override
	public void onEntityCollidedWithBlock(
		World world,
		int x,
		int y,
		int z,
		Entity entity
	) {

		entity.motionX *= 0.98D;
		entity.motionZ *= 0.98D;

		// Uranus zonal winds
		double wind =
			Math.sin(z * 0.002D)
				* 0.04D;

		entity.motionX += wind;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(
		IIconRegister reg
	) {
		this.icon =
			reg.registerIcon(
				"minecraft:glass"
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
