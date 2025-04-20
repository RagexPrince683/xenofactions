package com.hfr.market.block;

import com.hfr.market.MarketData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMachineMarket extends TileEntity {
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

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
	}

	public void setSelectedItem(String item) {
		this.selectedItem = item;
		this.selectedItemPrice = MarketData.getItemPrice(item);
		markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}