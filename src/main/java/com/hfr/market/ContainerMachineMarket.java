package com.hfr.market;

import com.hfr.market.block.TileEntityMachineMarket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class ContainerMachineMarket extends Container {
    private final TileEntityMachineMarket tile;

    public ContainerMachineMarket(InventoryPlayer playerInventory, TileEntityMachineMarket tile) {
        this.tile = tile;
        // Add player inventory slots here if needed
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tile.getWorldObj().getTileEntity(this.tile.xCoord, this.tile.yCoord, this.tile.zCoord) == this.tile;
    }
}