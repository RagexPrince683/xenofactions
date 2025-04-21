package com.hfr.market;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class MarketBlock extends BlockContainer {
    public MarketBlock() {
        super(Material.wood);
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
                // Example interaction: select an item
                String item = "example_item"; // Replace with actual logic to determine item
                tile.setSelectedItem(item);
                player.addChatMessage(new ChatComponentText("Selected Item: " + item + " with price: " + tile.getSelectedItemPrice()));
            }
        }
        return true;
    }
}