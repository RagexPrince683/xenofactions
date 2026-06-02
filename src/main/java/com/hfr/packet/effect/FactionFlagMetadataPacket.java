package com.hfr.packet.effect;

import com.hfr.client.flag.FactionFlagTextureManager;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.flag.CustomFlagService;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class FactionFlagMetadataPacket implements IMessage {

	public String factionName;
	public String hash;

	public FactionFlagMetadataPacket() { }

	public FactionFlagMetadataPacket(String factionName, String hash) {
		this.factionName = factionName == null ? "" : factionName;
		this.hash = hash == null ? "" : hash;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		factionName = ByteBufUtils.readUTF8String(buf);
		hash = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, factionName == null ? "" : factionName);
		ByteBufUtils.writeUTF8String(buf, hash == null ? "" : hash);
	}

	public static class ClientHandler implements IMessageHandler<FactionFlagMetadataPacket, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(FactionFlagMetadataPacket m, MessageContext ctx) {
			FactionFlagTextureManager.handleMetadata(m.factionName, m.hash);
			return null;
		}
	}

	public static class ServerHandler implements IMessageHandler<FactionFlagMetadataPacket, IMessage> {
		@Override
		public IMessage onMessage(FactionFlagMetadataPacket m, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			Clowder clowder = Clowder.getClowderFromName(m.factionName);
			if(player != null && m.hash != null && m.hash.length() > 0 && clowder != null && clowder.customFlagHash != null && clowder.customFlagHash.equals(m.hash))
				CustomFlagService.sendFlagBytes(player, clowder);
			return null;
		}
	}
}
