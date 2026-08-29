package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityMoltenSaltReactor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerMoltenSaltReactor extends Container {

	private TileEntityMoltenSaltReactor reactor;

	public ContainerMoltenSaltReactor(TileEntityMoltenSaltReactor reactor) {
		this.reactor = reactor;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return reactor.isUseableByPlayer(player);
	}
}
