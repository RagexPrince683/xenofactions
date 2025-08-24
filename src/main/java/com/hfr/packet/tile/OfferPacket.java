package com.hfr.packet.tile;

import com.hfr.data.MarketData;
import com.hfr.data.MarketData.Offer;
import com.hfr.inventory.gui.GUIMachineMarket;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple packet carrying an NBTTagCompound built from MarketData.writeOffersToNBT(...)
 * and the market name.
 */
public class OfferPacket implements IMessage {

	private String name;
	private NBTTagCompound nbt;

	public OfferPacket() {}

	public OfferPacket(String name, NBTTagCompound nbt) {
		this.name = name;
		this.nbt = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
		ByteBufUtils.writeTag(buf, nbt);
	}

	public static class Handler implements IMessageHandler<OfferPacket, IMessage> {

		@Override
		public IMessage onMessage(OfferPacket msg, MessageContext ctx) {
			try {
				// Reconstruct MarketData from the received NBT (client-side copy used only for GUI)
				MarketData tmp = new MarketData();
				tmp.readMarketFromPacket(msg.nbt);

				List<Offer> offers = tmp.offers.get(msg.name);
				if (offers == null) offers = new ArrayList<Offer>();

				// Assign to GUI holder. GUIMachineMarket should read this on opening.
				GUIMachineMarket.offers = offers;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
