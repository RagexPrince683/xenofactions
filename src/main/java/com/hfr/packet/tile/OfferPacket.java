package com.hfr.packet.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.hfr.blocks.machine.MachineMarket;
import com.hfr.data.MarketData;
import com.hfr.data.MarketData.Offer;
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
import net.minecraft.network.PacketBuffer;

public class OfferPacket implements IMessage {

	private String name;
	private NBTTagCompound nbt; // direct, no need for PacketBuffer field

	public OfferPacket() {}

	public OfferPacket(String name, NBTTagCompound nbt) {
		this.name = name;
		this.nbt = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
		this.nbt = ByteBufUtils.readTag(buf); // handles NBTTagCompound directly
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
		ByteBufUtils.writeTag(buf, nbt);
	}

	public static class Handler implements IMessageHandler<OfferPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(OfferPacket m, MessageContext ctx) {
			try {
				MarketData data = new MarketData();
				data.offers.clear();
				data.readMarketFromPacket(m.nbt);

				MachineMarket.name = m.name;
				List<Offer> offers = data.offers.get(m.name);

				if (offers == null)
					offers = new ArrayList<>();

				GUIMachineMarket.offers = offers;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}

