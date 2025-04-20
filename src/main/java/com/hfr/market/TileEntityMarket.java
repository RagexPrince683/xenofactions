package com.hfr.market;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMarket extends TileEntity {
    private String selectedItem = "";
    private int selectedItemPrice = 0;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.selectedItem = nbt.getString("SelectedItem");
        this.selectedItemPrice = nbt.getInteger("SelectedItemPrice");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("SelectedItem", this.selectedItem);
        nbt.setInteger("SelectedItemPrice", this.selectedItemPrice);
    }

    public void setSelectedItem(String item) {
        this.selectedItem = item;
        this.selectedItemPrice = MarketData.getItemPrice(item);
        markDirty();
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    public int getSelectedItemPrice() {
        return selectedItemPrice;
    }
}