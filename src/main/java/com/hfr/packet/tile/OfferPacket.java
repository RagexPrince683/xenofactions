package com.hfr.packet.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.hfr.blocks.machine.MachineMarket;
import com.hfr.data.MarketData;
import com.hfr.inventory.gui.GUIMachineMarket;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;

public class OfferPacket implements IMessage {

	String name;
	PacketBuffer buffer;
	private NBTTagCompound data;

	//public OfferPacket() {}

	public OfferPacket() {}

	public OfferPacket(NBTTagCompound data) {
		this.data = data;
	}



	public OfferPacket(String name, NBTTagCompound nbt) {

		this.name = name;
		this.buffer = new PacketBuffer(Unpooled.buffer());

		try {
			buffer.writeNBTTagCompoundToBuffer(nbt);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//@Override
	//public void fromBytes(ByteBuf buf) {
	//
	//	this.name = ByteBufUtils.readUTF8String(buf);
	//
	//	if (buffer == null) {
	//		buffer = new PacketBuffer(Unpooled.buffer());
	//	}
	//	buffer.writeBytes(buf);
	//}
//
	//@Override
	//public void toBytes(ByteBuf buf) {
	//
	//	ByteBufUtils.writeUTF8String(buf, name);
//
	//	if (buffer == null) {
	//		buffer = new PacketBuffer(Unpooled.buffer());
	//	}
	//	buf.writeBytes(buffer);
	//}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, this.data);
	}

	public static class Handler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(OfferPacket message, MessageContext ctx) {
			NBTTagCompound nbt = message.data;
			if (nbt == null) return null;

			String market = nbt.getString("market");
			if (market == null || market.isEmpty()) return null;

			int offerCount = nbt.getInteger("offercount");

			ItemStack[] offers = new ItemStack[0];
			for (int i = 0; i < offerCount; i++) {
				NBTTagList list = nbt.getTagList("items" + i, 10);
				ItemStack[] offerArray = new ItemStack[list.tagCount()];

				for (int j = 0; j < list.tagCount(); j++) {
					NBTTagCompound itemTag = list.getCompoundTagAt(j);
					offerArray[j] = ItemStack.loadItemStackFromNBT(itemTag);
				}

				offers.add(offerArray);
			}

			MarketData.addOffer(market, offers);
			return null;
		}
	}
}
