package com.hfr.packet.tile;

import java.util.ArrayList;
import java.util.List;

import com.hfr.blocks.machine.MachineMarket;
import com.hfr.data.MarketData;
import com.hfr.inventory.gui.GUIMachineMarket;
import com.hfr.packet.PacketDispatcher;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * OfferPacket - carries a name + optional NBT.
 * Register ServerHandler for Side.SERVER and ClientHandler for Side.CLIENT.
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
		this.nbt = ByteBufUtils.readTag(buf); // may be null
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name == null ? "" : name);
		ByteBufUtils.writeTag(buf, nbt); // writes null safely
	}

	// -------------------------
	// Server-side handler
	// -------------------------
	// inside OfferPacket class (keep your fromBytes/toBytes as they are using ByteBufUtils.readTag/writeTag)
	public static class ServerHandler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(final OfferPacket msg, final MessageContext ctx) {
			try {
				// Server is guaranteed here in 1.7.10 if you registered ServerHandler for Side.SERVER
				MarketData.loadMarketData(); // optional, if you want to refresh from disk
				List<ItemStack[]> offers = MarketData.getOffers(msg.name);
				if (offers == null) offers = new ArrayList<ItemStack[]>();

				MachineMarket.name = msg.name;

				// Serialize offers into NBT and send back
				NBTTagCompound response = MarketData.offersToNBT(offers);
				EntityPlayerMP player = ctx.getServerHandler().playerEntity;
				PacketDispatcher.wrapper.sendTo(new OfferPacket(msg.name, response), player);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static class ClientHandler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(final OfferPacket msg, final MessageContext ctx) {
			try {
				// In 1.7.10 this runs on the client thread already.
				List<ItemStack[]> offers;
				if (msg.nbt != null && !msg.nbt.hasNoTags()) {
					offers = MarketData.offersFromNBT(msg.nbt);
				} else {
					// fallback (less reliable)
					MarketData.loadMarketData();
					offers = MarketData.getOffers(msg.name);
				}

				if (offers == null) offers = new ArrayList<ItemStack[]>();

				MachineMarket.name = msg.name;
				GUIMachineMarket.offers = offers;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}
