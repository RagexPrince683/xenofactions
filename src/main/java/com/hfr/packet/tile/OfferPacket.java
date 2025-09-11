package com.hfr.packet.tile;

import java.util.ArrayList;
import java.util.List;

import com.hfr.blocks.machine.MachineMarket;
import com.hfr.blocks.machine.MachineMarket.TileEntityMarket;
import com.hfr.data.MarketData;
import com.hfr.inventory.gui.GUIMachineMarket;
import com.hfr.packet.PacketDispatcher;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * OfferPacket now carries: x,y,z + name + optional NBT payload.
 * Client sends packet with coords (and name if available). Server will use name if non-empty,
 * otherwise will lookup tile at coords and use its name.
 */
public class OfferPacket implements IMessage {

	public int x, y, z;
	public String name;
	public NBTTagCompound nbt;

	public OfferPacket() {}

	// add this new constructor (client->server request already exists)
	public OfferPacket(int x, int y, int z, String name, NBTTagCompound nbt) {
		this.x = x; this.y = y; this.z = z;
		this.name = name == null ? "" : name;
		this.nbt = nbt;
	}

	// keep existing request ctor and old reply ctor if you want, but use the new one for server replies.
	public OfferPacket(int x, int y, int z, String name) {
		this.x = x; this.y = y; this.z = z;
		this.name = name == null ? "" : name;
		this.nbt = null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.name = ByteBufUtils.readUTF8String(buf);
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeUTF8String(buf, name == null ? "" : name);
		ByteBufUtils.writeTag(buf, nbt);
	}

	// -------------------------
	// Server handler
	// -------------------------
	public static class ServerHandler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(final OfferPacket msg, final MessageContext ctx) {
			try {
				EntityPlayerMP player = ctx.getServerHandler().playerEntity;
				String marketName = msg.name == null ? "" : msg.name;
				World world = player.worldObj;

				// If name is empty, try to look up tile entity at coords
				if (marketName.isEmpty()) {
					try {
						TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
						if (te instanceof TileEntityMarket) {
							marketName = ((TileEntityMarket) te).name;
						}
					} catch (Exception ex) {
						// ignore - leave name empty if lookup fails
					}
				}

				System.out.println("[OfferPacket.ServerHandler] Received request for shop '" + marketName + "' from player: "
						+ (player != null ? player.getCommandSenderName() : "UNKNOWN") + " coords=(" + msg.x + "," + msg.y + "," + msg.z + ")");

				MarketData.loadMarketData();
				List<ItemStack[]> offers = MarketData.getOffers(marketName);
				if (offers == null) offers = new ArrayList<ItemStack[]>();

				// send reply with serialized offers
				NBTTagCompound response = MarketData.offersToNBT(offers);
				PacketDispatcher.wrapper.sendTo(new OfferPacket(msg.x, msg.y, msg.z, marketName, response), player);

				System.out.println("[OfferPacket.ServerHandler] Sent reply to player "
						+ (player != null ? player.getCommandSenderName() : "UNKNOWN")
						+ " for shop '" + marketName + "' (offers=" + offers.size() + ")");

			} catch (Exception e) {
				System.err.println("[OfferPacket.ServerHandler] Exception:");
				e.printStackTrace();
			}
			return null;
		}
	}

	// -------------------------
	// Client handler
	// -------------------------
	public static class ClientHandler implements IMessageHandler<OfferPacket, IMessage> {

		// at top of ClientHandler class (static)
		private static long lastReplyTime = 0;
		private static int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE, lastZ = Integer.MIN_VALUE;
		private static String lastName = "";
		@Override
		public IMessage onMessage(final OfferPacket msg, final MessageContext ctx) {

			long now = System.currentTimeMillis();
			if (msg.x == lastX && msg.y == lastY && msg.z == lastZ && msg.name != null && msg.name.equals(lastName) && (now - lastReplyTime) < 250) {
				System.out.println("[OfferPacket.ClientHandler] Ignoring duplicate quick reply for " + msg.name);
				return null;
			}
			lastReplyTime = now; lastX = msg.x; lastY = msg.y; lastZ = msg.z; lastName = msg.name;

			try {
				System.out.println("[OfferPacket.ClientHandler] Received packet for shop '" + msg.name + "' on client (coords: "
						+ msg.x + "," + msg.y + "," + msg.z + ")");

				List<ItemStack[]> offers;
				if (msg.nbt != null && msg.nbt.hasKey("offers")) {
					offers = MarketData.offersFromNBT(msg.nbt);
					System.out.println("[OfferPacket.ClientHandler] Parsed " + offers.size() + " offers from NBT for '" + msg.name + "'");
				} else {
					// Fallback: client JSON (less deterministic)
					MarketData.loadMarketData();
					offers = MarketData.getOffers(msg.name);
					System.out.println("[OfferPacket.ClientHandler] Fallback loaded " + offers.size() + " offers for '" + msg.name + "'");
				}

				if (offers == null) offers = new ArrayList<ItemStack[]>();
				MachineMarket.name = msg.name;
				GUIMachineMarket.offers = offers;

				// Force GUI refresh if it's open
				Minecraft mc = Minecraft.getMinecraft();
				if (mc.currentScreen instanceof GUIMachineMarket) {
					((GUIMachineMarket) mc.currentScreen).refreshOffers();
					GUIMachineMarket gui = (GUIMachineMarket) mc.currentScreen;
					gui.onOffersReceived(); // clears request flag + refreshOffers()
					System.out.println("[OfferPacket.ClientHandler] GUIMachineMarket.refreshOffers() called");
				}



			} catch (Exception e) {
				System.err.println("[OfferPacket.ClientHandler] Exception while handling packet:");
				e.printStackTrace();
			}
			return null;
		}
	}
}
