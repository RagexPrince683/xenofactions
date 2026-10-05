package com.hfr.blocks.machine;

import com.hfr.blocks.ModBlocks;
import com.hfr.data.MarketData;
import com.hfr.lib.RefStrings;
import com.hfr.shop.FactionMarketRegistry;
import com.hfr.shop.XShopService;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class MachineMarket extends BlockContainer {
    @SideOnly(Side.CLIENT) private IIcon iconTop;
    @SideOnly(Side.CLIENT) private IIcon iconBottom;
    private final boolean factionTerminal;
    public MachineMarket(Material material) { this(material, false); }
    public MachineMarket(Material material, boolean factionTerminal) { super(material); this.factionTerminal = factionTerminal; }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister icons) {
        iconTop = icons.registerIcon(RefStrings.MODID + ":market_top");
        iconBottom = icons.registerIcon(RefStrings.MODID + ":market_bottom");
        blockIcon = icons.registerIcon(RefStrings.MODID + ":market_side");
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side, int metadata) {
        return side == 1 ? iconTop : side == 0 ? iconBottom : blockIcon;
    }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                             int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity raw = world.getTileEntity(x, y, z);
        if (!(raw instanceof TileEntityMarket) || !(player instanceof EntityPlayerMP)) return true;
        TileEntityMarket tile = (TileEntityMarket)raw;
        if (!factionTerminal && XShopService.isAdmin(player) && player.getHeldItem() != null
            && player.getHeldItem().getItem() == Items.name_tag && player.getHeldItem().hasDisplayName()) {
            MarketData.Shop shop = MarketData.resolve(player.getHeldItem().getDisplayName());
            if (shop == null) player.addChatMessage(new ChatComponentText("[XShop] Unknown shop. Create it with /xshop create first."));
            else { tile.link(shop.id); player.addChatMessage(new ChatComponentText("[XShop] Linked to " + shop.displayName)); }
            return true;
        }
        XShopService.openBlock((EntityPlayerMP)player, tile);
        return true;
    }
    @Override public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntity raw = world.getTileEntity(x, y, z);
        if (factionTerminal && raw instanceof TileEntityMarket) FactionMarketRegistry.broken(world, (TileEntityMarket)raw);
        super.breakBlock(world, x, y, z, block, metadata);
    }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityMarket(); }

    /** The legacy tile registration and name tag are retained for lazy world migration. */
    public static class TileEntityMarket extends TileEntity {
        private String legacyName = "";
        public String shopId = "", factionId = "", terminalToken = "";
        public String resolveShopId() {
            if (shopId.isEmpty() && !legacyName.isEmpty() && !worldObj.isRemote) {
                String migrated = MarketData.legacyId(legacyName);
                if (migrated != null) { shopId = migrated; legacyName = ""; markDirty(); }
            }
            return shopId;
        }
        public void link(String id) { shopId = id; legacyName = ""; markDirty(); }
        @Override public boolean canUpdate() { return false; }
        @Override public void readFromNBT(NBTTagCompound tag) {
            super.readFromNBT(tag); legacyName = tag.getString("name"); shopId = tag.getString("shopId");
            factionId = tag.getString("factionId"); terminalToken = tag.getString("terminalToken");
        }
        @Override public void writeToNBT(NBTTagCompound tag) {
            super.writeToNBT(tag); tag.setString("name", legacyName); tag.setString("shopId", shopId);
            tag.setString("factionId", factionId); tag.setString("terminalToken", terminalToken);
        }
    }
}
