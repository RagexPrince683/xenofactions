package com.hbm.tileentity.machine.pile;

import com.hbm.blocks.ModBlocks;

public class TileEntityPileSource extends TileEntityPileBase {

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			int n =
				this.getBlockType() ==
					ModBlocks.block_graphite_source
					? 4 : 8;

			int rays = 8 + worldObj.rand.nextInt(5);

			for(int i = 0; i < rays; i++) {
				this.castRay(n, 5);
			}
		}
	}
}
