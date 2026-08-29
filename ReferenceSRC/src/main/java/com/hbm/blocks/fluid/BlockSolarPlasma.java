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
import net.minecraft.world.World;

public class BlockSolarPlasma extends Block {

	public BlockSolarPlasma() {
		super(Material.lava);

		this.setBlockName("solar_plasma");
		this.setLightLevel(1.0F);

		this.setHardness(0.0F);   // behaves like fluid, not a solid block
		this.setResistance(6000.0F);

		this.setLightOpacity(0);  // IMPORTANT: prevents full block light blocking
	}

	// NOT SOLID
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	// NO FULL BLOCK COLLISION
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	// ENTITY INTERACTION STILL WORKS
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {

		entity.motionX *= 0.5;
		entity.motionZ *= 0.5;
		entity.motionY += 0.02;

		entity.setFire(10);

		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).attackEntityFrom(
				DamageSource.inFire,
				6.0F
			);
		}
	}

	@Override
	public boolean canCollideCheck(int meta, boolean hitIfLiquid) {
		return false;
	}

	// ===== LAVA-LIKE TEXTURE =====
	@SideOnly(Side.CLIENT)
	protected IIcon stillIcon;
	@SideOnly(Side.CLIENT)
	protected IIcon flowingIcon;

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister reg) {
		this.stillIcon = reg.registerIcon("lava_still");
		this.flowingIcon = reg.registerIcon("lava_flow");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int meta) {
		return (side == 0 || side == 1) ? stillIcon : flowingIcon;
	}
}
