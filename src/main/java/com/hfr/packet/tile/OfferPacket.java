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
 * OfferPacket - request: name + null NBT
 *               reply: name + NBT containing offers
 *
 * Debug prints included. Remove prints after debugging.
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
		ByteBufUtils.writeTag(buf, nbt); // safely handles null
	}

	// -------------------------
	// Server-side handler
	// -------------------------
	public static class ServerHandler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(final OfferPacket msg, final MessageContext ctx) {
			try {
				// Debug - confirm server received request and who sent it
				EntityPlayerMP player = ctx.getServerHandler().playerEntity;
				System.out.println("[OfferPacket.ServerHandler] Received request for shop '" + msg.name + "' from player: "
						+ (player != null ? player.getCommandSenderName() : "UNKNOWN"));

				// Load data, build offers list
				MarketData.loadMarketData(); // safe: refresh from disk
				List<ItemStack[]> offers = MarketData.getOffers(msg.name);
				if (offers == null) {
					offers = new ArrayList<ItemStack[]>();
				}
				System.out.println("[OfferPacket.ServerHandler] Server found " + offers.size() + " offers for '" + msg.name + "'");

				// Optionally mutate server-side machine state
				MachineMarket.name = msg.name;

				// Serialize offers into NBT and send back to the requesting player
				NBTTagCompound response = MarketData.offersToNBT(offers);
				PacketDispatcher.wrapper.sendTo(new OfferPacket(msg.name, response), player);
				System.out.println("[OfferPacket.ServerHandler] Sent reply to player "
						+ (player != null ? player.getCommandSenderName() : "UNKNOWN")
						+ " for shop '" + msg.name + "' (nbt tags: " + (response != null ? response.getTagList("offers", 10).tagCount() : "null") + ")");

			} catch (Exception e) {
				System.err.println("[OfferPacket.ServerHandler] Exception:");
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
			try {
				System.out.println("[OfferPacket.ClientHandler] Received packet for shop='" + msg.name + "' on client");

				if (msg.nbt == null) {
					System.out.println("[OfferPacket.ClientHandler] msg.nbt == null");
				} else {
					//System.out.println("[OfferPacket.ClientHandler] NBT keys: " + java.util.Arrays.toString(msg.nbt.getKeySet().toArray()));
					//getKeySet() is not a real fucking method.
					if (msg.nbt.hasKey("debugMessage")) {
						System.out.println("[OfferPacket.ClientHandler] debugMessage -> " + msg.nbt.getString("debugMessage"));
					}
					if (msg.nbt.hasKey("offers")) {
						System.out.println("[OfferPacket.ClientHandler] offers tag count = " + msg.nbt.getTagList("offers", 10).tagCount());
					}
				}

				List<ItemStack[]> offers;
				if (msg.nbt != null && msg.nbt.hasKey("offers")) {
					offers = MarketData.offersFromNBT(msg.nbt);
					System.out.println("[OfferPacket.ClientHandler] Parsed " + offers.size() + " offers from NBT for '" + msg.name + "'");
				} else {
					MarketData.loadMarketData();
					offers = MarketData.getOffers(msg.name);
					System.out.println("[OfferPacket.ClientHandler] Fallback loaded " + offers.size() + " offers for '" + msg.name + "'");
				}

				if (offers == null) offers = new java.util.ArrayList<ItemStack[]>();
				MachineMarket.name = msg.name;
				GUIMachineMarket.offers = offers;

// ADD THESE LINES (paste exactly)
				net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
				if (mc.currentScreen instanceof com.hfr.inventory.gui.GUIMachineMarket) {
					((com.hfr.inventory.gui.GUIMachineMarket) mc.currentScreen).refreshOffers();
					System.out.println("[OfferPacket.ClientHandler] GUIMachineMarket.refreshOffers() called");
				}

				// Extra debug: print first few items
				for (int i = 0; i < Math.min(5, offers.size()); i++) {
					ItemStack[] arr = offers.get(i);
					StringBuilder sb = new StringBuilder();
					sb.append("[OfferPacket.ClientHandler] Offer ").append(i).append(": ");
					if (arr != null) {
						for (int j = 0; j < arr.length; j++) {
							ItemStack s = arr[j];
							sb.append("(").append(j).append("=" + (s == null ? "null" : s.getDisplayName())).append(") ");
						}
					} else {
						sb.append("null-array");
					}
					System.out.println(sb.toString());
				}

			} catch (Exception e) {
				System.err.println("[OfferPacket.ClientHandler] Exception while handling packet:");
				e.printStackTrace();
			}
			return null;
		}
	}

}
