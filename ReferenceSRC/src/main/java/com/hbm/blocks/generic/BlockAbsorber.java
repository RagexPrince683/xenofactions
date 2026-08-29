package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;

import com.hbm.tileentity.machine.TileEntityAbsorber;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAbsorber extends Block {

	float absorb = 0;

	public BlockAbsorber(Material mat, float ab) {
		super(mat);
		this.setTickRandomly(true);
		absorb = ab;
	}
	public float getAbsorbRate() {
		return absorb;
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}
	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileEntityAbsorber();
	}


	@Override
	public int tickRate(World world) {

		return 10;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {

		TileEntity te = world.getTileEntity(x, y, z);

		if(te instanceof TileEntityAbsorber) {

			TileEntityAbsorber absorber =
				(TileEntityAbsorber) te;

			if(absorber.isFull()) {

				world.setBlock(
					x,
					y,
					z,
					ModBlocks.absorber_spent
				);

				return;
			}

			if(!absorber.isFull()) {

				float removed =
					Math.min(
						absorb,
						absorber.remainingCapacity()
					);

				ChunkRadiationManager.proxy.decrementRad(
					world,
					x,
					y,
					z,
					removed
				);

				absorber.storedRad += removed;
				absorber.markDirty();
			}
		}

		world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
	}

	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
	}
}
