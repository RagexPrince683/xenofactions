package com.hfr.packet.tile;

import java.util.ArrayList;
import java.util.List;

import com.hfr.blocks.machine.MachineMarket;
import com.hfr.data.MarketData;
import com.hfr.inventory.gui.GUIMachineMarket;
import com.hfr.packet.PacketDispatcher; // (optional)

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * OfferPacket - send a market name and optional NBT payload.
 * Register ServerHandler for Side.SERVER and ClientHandler for Side.CLIENT.
 */
public class OfferPacket implements IMessage {

	private String name;
	private NBTTagCompound nbt;

	// required empty ctor
	public OfferPacket() {}

	public OfferPacket(String name, NBTTagCompound nbt) {
		this.name = name;
		this.nbt = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		// read name
		this.name = ByteBufUtils.readUTF8String(buf);
		// read optional NBT (null if none)
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name == null ? "" : name);
		ByteBufUtils.writeTag(buf, nbt); // handles null
	}

	// -------------------------
	// Server-side handler
	// -------------------------
	public static class ServerHandler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(final OfferPacket msg, final MessageContext ctx) {
			// NOTE: in many 1.7.10 setups SimpleNetworkWrapper will call this on the main thread,
			// but if you ever see thread issues, schedule this on the server main thread.
			try {
				// Example: client asked server for offers with name -> server should load data + reply
				MarketData.loadMarketData(); // server-side load
				List<ItemStack[]> offers = MarketData.getOffers(msg.name);
				if (offers == null) offers = new ArrayList<ItemStack[]>();

				// If MachineMarket is server-side data you want to set:
				MachineMarket.name = msg.name;

				// If you want to send the offers back to the client, serialize them into NBT and send:
				// (You need to implement MarketData.writeOffersToNBT if you want full serialization)
				NBTTagCompound response = new NBTTagCompound();
				// TODO: serialize offers into 'response' if desired
				// e.g. MarketData.writeOffersToNBT(response, offers);

				// Send back to the requesting player:
				EntityPlayerMP player = ctx.getServerHandler().playerEntity;
				PacketDispatcher.wrapper.sendTo(new OfferPacket(msg.name, response), player);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// -------------------------
	// Client-side handler
	// -------------------------
	public static class ClientHandler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(final OfferPacket msg, final MessageContext ctx) {
			// Schedule on client main thread
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					try {
						// If server sent an NBT payload, parse it; otherwise reload MarketData locally.
						if (msg.nbt != null && !msg.nbt.hasNoTags()) {
							// TODO: parse msg.nbt into MarketData / offers if you serialized them server-side
							// e.g. MarketData.readOffersFromNBT(msg.nbt);
						} else {
							MarketData.loadMarketData(); // fallback: client loads JSON
						}

						List<ItemStack[]> offers = MarketData.getOffers(msg.name);
						if (offers == null) offers = new ArrayList<ItemStack[]>();

						MachineMarket.name = msg.name;
						GUIMachineMarket.offers = offers;

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return null;
		}
	}
}
