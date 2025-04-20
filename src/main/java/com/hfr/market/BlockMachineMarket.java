package com.hfr.market;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMachineMarket extends BlockContainer {
    public BlockMachineMarket() {
        super(Material.iron);
        setBlockName("machineMarketBlock");
        setBlockTextureName("xenofactions:machine_market_block");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMachineMarket();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntityMachineMarket tile = (TileEntityMachineMarket) world.getTileEntity(x, y, z);
            if (tile != null) {
                // Example interaction: Select an item or open GUI
                player.openGui(YourMod.instance, YourMod.GUI_MACHINE_MARKET, world, x, y, z);
            }
        }
        return true;
    }
}