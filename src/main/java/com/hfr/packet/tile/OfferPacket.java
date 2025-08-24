package com.hfr.packet.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.hfr.blocks.machine.MachineMarket;
import com.hfr.data.MarketData;
import com.hfr.inventory.gui.GUIMachineMarket;

import com.hfr.packet.PacketDispatcher;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

public class OfferPacket implements IMessage {

	String name;
	NBTTagCompound tag; // payload: empty/null for request, filled with offers for response

	public OfferPacket() { }

	// Request (client -> server) use this
	public OfferPacket(String name) {
		this.name = name;
		this.tag = null;
	}

	// Response (server -> client) use this
	public OfferPacket(String name, List<ItemStack[]> offers) {
		this.name = name;
		this.tag = offersToNBT(offers);
	}

	// Legacy ctor compatibility (if you already pass an NBT)
	public OfferPacket(String name, NBTTagCompound nbt) {
		this.name = name;
		this.tag = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
		PacketBuffer pb = new PacketBuffer(buf);
		try {
			this.tag = pb.readNBTTagCompoundFromBuffer(); // may be null on requests
		} catch (IOException e) {
			this.tag = null;
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
		PacketBuffer pb = new PacketBuffer(buf);
		try {
			pb.writeNBTTagCompoundToBuffer(tag); // may be null on requests
		} catch (IOException e) {
			// nothing: network will drop if truly broken
		}
	}

	public static class Handler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(OfferPacket m, MessageContext ctx) {
			if (ctx.side.isServer()) {
				// === SERVER: got request from client ===
				try {
					MarketData.loadMarketData();
					List<ItemStack[]> offers = MarketData.getOffers(m.name);
					if (offers == null) offers = new ArrayList<ItemStack[]>();

					// send same packet type back as response (with NBT payload)
					OfferPacket reply = new OfferPacket(m.name, offers);
					EntityPlayerMP player = ctx.getServerHandler().playerEntity;
					PacketDispatcher.sendTo(reply, player);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// === CLIENT: got response from server ===
				try {
					List<ItemStack[]> offers = nbtToOffers(m.tag);
					if (offers == null) offers = new ArrayList<ItemStack[]>();

					MachineMarket.name = m.name;
					GUIMachineMarket.offers = offers;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	// -------- NBT <-> offers helpers (1.7.10-safe) --------

	private static NBTTagCompound offersToNBT(List<ItemStack[]> offers) {
		NBTTagCompound out = new NBTTagCompound();
		NBTTagList list = new NBTTagList();

		for (ItemStack[] arr : offers) {
			NBTTagCompound arrTag = new NBTTagCompound();
			arrTag.setInteger("Size", arr.length);

			NBTTagList items = new NBTTagList();
			for (int i = 0; i < arr.length; i++) {
				NBTTagCompound it = new NBTTagCompound();
				it.setByte("Slot", (byte) i);
				if (arr[i] != null) {
					arr[i].writeToNBT(it); // 1.7.10
				} else {
					it.setBoolean("Empty", true);
				}
				items.appendTag(it);
			}
			arrTag.setTag("Items", items);
			list.appendTag(arrTag);
		}

		out.setTag("Offers", list);
		return out;
	}

	private static List<ItemStack[]> nbtToOffers(NBTTagCompound tag) {
		if (tag == null) return null;

		List<ItemStack[]> offers = new ArrayList<ItemStack[]>();
		NBTTagList list = tag.getTagList("Offers", 10); // 10 = NBTTagCompound

		for (int idx = 0; idx < list.tagCount(); idx++) {
			NBTTagCompound arrTag = list.getCompoundTagAt(idx);
			int size = arrTag.getInteger("Size");
			ItemStack[] arr = new ItemStack[Math.max(0, size)];

			NBTTagList items = arrTag.getTagList("Items", 10);
			for (int j = 0; j < items.tagCount(); j++) {
				NBTTagCompound it = items.getCompoundTagAt(j);
				int slot = it.getByte("Slot") & 255;
				if (slot >= 0 && slot < arr.length && !it.getBoolean("Empty")) {
					arr[slot] = ItemStack.loadItemStackFromNBT(it); // 1.7.10
				}
			}
			offers.add(arr);
		}
		return offers;
	}
}

