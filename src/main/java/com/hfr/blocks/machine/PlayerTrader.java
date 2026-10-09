package com.hfr.blocks.machine;

import com.hfr.blocks.ModBlocks;
import com.hfr.shop.PlayerTraderService;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** A stocked, single-offer shop. Its inventory is deliberately not exposed to automation. */
public class PlayerTrader extends BlockContainer {
    public PlayerTrader() { super(Material.iron); }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new Tile(); }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && placer instanceof EntityPlayer) {
            Tile tile = (Tile)world.getTileEntity(x, y, z);
            if (tile != null) { tile.owner = ((EntityPlayer)placer).getUniqueID().toString(); tile.markDirty(); }
        }
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                                 int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof Tile) PlayerTraderService.open((EntityPlayerMP)player, (Tile)tile);
        }
        return true;
    }

    @Override public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        if (!world.isRemote) {
            TileEntity raw = world.getTileEntity(x, y, z);
            if (raw instanceof Tile) {
                Tile tile = (Tile)raw;
                PlayerTraderService.broken(tile);
                for (ItemStack stack : tile.stock) drop(world, x, y, z, stack);
                for (ItemStack stack : tile.payments) drop(world, x, y, z, stack);
                tile.stock = new ItemStack[9]; tile.payments = new ItemStack[9];
            }
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }

    private void drop(World world, int x, int y, int z, ItemStack stack) {
        if (stack == null || stack.stackSize <= 0) return;
        world.spawnEntityInWorld(new EntityItem(world, x + .5, y + .5, z + .5, stack.copy()));
    }

    public static class Tile extends TileEntity {
        public String owner = "";
        public ItemStack sale, payment;
        public int saleQuantity = 1, paymentQuantity = 1;
        public long offerRevision;
        public ItemStack[] stock = new ItemStack[9], payments = new ItemStack[9];

        @Override public boolean canUpdate() { return false; }
        @Override public void readFromNBT(NBTTagCompound tag) {
            super.readFromNBT(tag);
            owner = tag.getString("OwnerUUID");
            sale = tag.hasKey("Sale", 10) ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Sale")) : null;
            payment = tag.hasKey("Payment", 10) ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Payment")) : null;
            if (sale != null) sale.stackSize = 1;
            if (payment != null) payment.stackSize = 1;
            saleQuantity = tag.getInteger("SaleQuantity"); paymentQuantity = tag.getInteger("PaymentQuantity");
            offerRevision = tag.getLong("OfferRevision");
            stock = new ItemStack[9]; payments = new ItemStack[9];
            NBTTagList items = tag.getTagList("Items", 10);
            for (int i = 0; i < items.tagCount(); i++) {
                NBTTagCompound entry = items.getCompoundTagAt(i);
                int slot = entry.getByte("Slot") & 255;
                ItemStack stack = ItemStack.loadItemStackFromNBT(entry);
                if (stack != null && stack.stackSize > 0) {
                    if (slot < 9) stock[slot] = stack;
                    else if (slot < 18) payments[slot - 9] = stack;
                }
            }
        }
        @Override public void writeToNBT(NBTTagCompound tag) {
            super.writeToNBT(tag);
            tag.setString("OwnerUUID", owner);
            if (sale != null) { NBTTagCompound n = new NBTTagCompound(); sale.writeToNBT(n); tag.setTag("Sale", n); }
            if (payment != null) { NBTTagCompound n = new NBTTagCompound(); payment.writeToNBT(n); tag.setTag("Payment", n); }
            tag.setInteger("SaleQuantity", saleQuantity); tag.setInteger("PaymentQuantity", paymentQuantity);
            tag.setLong("OfferRevision", offerRevision);
            NBTTagList items = new NBTTagList();
            for (int i = 0; i < 18; i++) {
                ItemStack stack = i < 9 ? stock[i] : payments[i - 9];
                if (stack != null) { NBTTagCompound n = new NBTTagCompound(); n.setByte("Slot", (byte)i); stack.writeToNBT(n); items.appendTag(n); }
            }
            tag.setTag("Items", items);
        }
        public boolean valid() { return !isInvalid() && worldObj != null && worldObj.getBlock(xCoord, yCoord, zCoord) == ModBlocks.player_trader
                && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this; }
    }
}
