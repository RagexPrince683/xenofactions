package com.hfr.market.block;

//import com.hfr.market.TileEntityMachineMarket;
//no dumbass wtf
import com.hfr.main.MainRegistry;
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
            player.openGui(MainRegistry.instance, MainRegistry.GUI_MACHINE_MARKET, world, x, y, z); // Open GUI
        }
        return true;
    }
}