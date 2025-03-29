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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;

public class OfferPacket implements IMessage {
	private String marketName;
	private NBTTagCompound nbt;

	// Default constructor (needed for packet handling)
	public OfferPacket() {}

	// Constructor for sending data
	public OfferPacket(String marketName, NBTTagCompound nbt) {
		this.marketName = marketName;
		this.nbt = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.marketName = ByteBufUtils.readUTF8String(buf);
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, marketName);
		ByteBufUtils.writeTag(buf, nbt);
	}

	public static class Handler implements IMessageHandler<OfferPacket, IMessage> {
		@Override
		public IMessage onMessage(OfferPacket message, MessageContext ctx) {
			if (ctx.side.isServer()) {
				// Process data on the SERVER
				EntityPlayerMP player = ctx.getServerHandler().playerEntity;

				MinecraftServer server = MinecraftServer.getServer();
				server.getConfigurationManager().addScheduledTask(new Runnable() {
					@Override
					public void run() {
						// Handle the packet on the server
					}
				});
			}
			return null;
		}
	}
}
