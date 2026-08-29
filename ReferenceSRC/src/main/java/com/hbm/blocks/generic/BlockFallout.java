package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmLivingProps.ContaminationEffect;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFallout extends Block {

	public BlockFallout(Material mat) {
		super(mat);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);

		// Do NOT use random ticking for falling.
		// Only salted fallout needs random ticking for radiation.
		if(this == ModBlocks.salted_fallout) {
			this.setTickRandomly(true);
		}
	}

	@Override
	public int tickRate(World world) {
		return 2;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		if(!world.isRemote) {
			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block b) {
		if(!world.isRemote) {
			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {

		int metadata = world.getBlockMetadata(x, y, z);

		if(this == ModBlocks.salted_fallout) {
			ChunkRadiationManager.proxy.incrementRad(world, x, y, z, 50 * (metadata + 1));
		}

		if(!world.isRemote) {
			this.tryFall(world, x, y, z);
		}
	}

	private void tryFall(World world, int x, int y, int z) {

		int meta = world.getBlockMetadata(x, y, z);

		Block below = world.getBlock(x, y - 1, z);

		// stack into fallout below
		if(below == this) {

			int belowMeta =
				world.getBlockMetadata(x, y - 1, z);

			if(belowMeta < 7) {

				world.setBlockMetadataWithNotify(
					x,
					y - 1,
					z,
					Math.min(7, belowMeta + meta + 1),
					3
				);

				world.setBlockToAir(
					x,
					y,
					z
				);

				return;
			}
		}

		// fall into air
		if(below.isAir(world, x, y - 1, z)) {

			world.setBlock(
				x,
				y - 1,
				z,
				this,
				meta,
				3
			);

			world.setBlockToAir(
				x,
				y,
				z
			);

			world.scheduleBlockUpdate(
				x,
				y - 1,
				z,
				this,
				2
			);
		}
	}

	private boolean canFallBelow(World world, int x, int y, int z) {

		if(y < 0) {
			return false;
		}

		Block block = world.getBlock(x, y, z);

		if(block.isAir(world, x, y, z)) {
			return true;
		}

		if(block == Blocks.fire) {
			return true;
		}

		Material mat = block.getMaterial();

		return mat == Material.water || mat == Material.lava;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		int metadata = world.getBlockMetadata(x, y, z);
		float height = (1 + metadata) / 8.0F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F);
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {

		Block block = world.getBlock(x, y, z);

		if(block == this) {

			int currentMeta = world.getBlockMetadata(x, y, z);

			if(currentMeta < 7) {

				world.setBlockMetadataWithNotify(x, y, z, currentMeta + 1, 3);

				return currentMeta + 1;
			}
		}

		return 0;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {

		Block block = world.getBlock(x, y, z);

		if(block == this) {
			int meta = world.getBlockMetadata(x, y, z);
			return meta < 7;
		}

		return super.canPlaceBlockOnSide(world, x, y, z, side);
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
	public boolean isNormalCube() {
		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {

	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return ModItems.fallout;
	}

	@Override
	public Item getItem(World world, int x, int y, int z) {
		return ModItems.fallout;
	}

	@Override
	public int damageDropped(int metadata) {
		return 0;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		int metadata = world.getBlockMetadata(x, y, z);
		float height = (1 + metadata) / 8.0F;

		if(metadata == 7) {
			return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
		}

		return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + height - 0.001F, z + 1);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
	}

	@Override
	public String getLocalizedName() {
		return "Fallout Layer";
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return 0;
	}

	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		return false;
	}

	@Override
	public int quantityDropped(int metadata, int fortune, Random random) {
		return metadata + 1;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {

		Block block = world.getBlock(x, y - 1, z);

		if(block == Blocks.ice || block == Blocks.packed_ice) {
			return false;
		}

		if(block.isLeaves(world, x, y - 1, z) && !block.isAir(world, x, y - 1, z)) {
			return true;
		}

		if(block == this) {
			return true;
		}

		return block.isOpaqueCube() && block.getMaterial().blocksMovement();
	}

	@Override
	public void onEntityWalking(World world, int x, int y, int z, Entity entity) {

		if(!world.isRemote && entity instanceof EntityLivingBase) {

			if(entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode) {
				return;
			}

			PotionEffect effect = new PotionEffect(HbmPotion.radiation.id, 10 * 60 * 20, 0);
			effect.setCurativeItems(new ArrayList());
			((EntityLivingBase) entity).addPotionEffect(effect);
		}
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {

		if(!world.isRemote) {
			HbmLivingProps.addCont(player, new ContaminationEffect(1F, 200, false));
		}
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
		return true;
	}
}
