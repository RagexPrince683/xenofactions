package com.hfr.packet.effect;

import java.util.ArrayList;
import java.util.List;

import com.hfr.clowder.ClaimOverlayData.Claim;
import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

/** A bounded part of an authoritative dimension snapshot. */
public class ClaimOverlayPacket implements IMessage {
	public static final int MAX_CLAIMS = 512;
	private int dimensionId, generation, part, parts;
	private List<Claim> claims = new ArrayList<Claim>();

	public ClaimOverlayPacket() { }
	public ClaimOverlayPacket(int dimensionId, int generation, int part, int parts, List<Claim> claims) {
		this.dimensionId = dimensionId; this.generation = generation; this.part = part; this.parts = parts; this.claims = claims;
	}
	@Override public void toBytes(ByteBuf buf) {
		buf.writeInt(dimensionId); buf.writeInt(generation); buf.writeShort(part); buf.writeShort(parts); buf.writeShort(claims.size());
		for(Claim claim : claims) {
			buf.writeInt(claim.chunkX); buf.writeInt(claim.chunkZ); buf.writeInt(claim.color);
			byte[] id = claim.groupId.getBytes(java.nio.charset.Charset.forName("UTF-8"));
			int length = Math.min(id.length, 128); buf.writeByte(length); buf.writeBytes(id, 0, length);
		}
	}
	@Override public void fromBytes(ByteBuf buf) {
		claims = new ArrayList<Claim>();
		if(buf.readableBytes() < 14) return;
		dimensionId = buf.readInt(); generation = buf.readInt(); part = buf.readUnsignedShort(); parts = buf.readUnsignedShort();
		int count = buf.readUnsignedShort();
		if(parts < 1 || parts > 4096 || part >= parts || count > MAX_CLAIMS) { claims.clear(); parts = 0; return; }
		for(int i = 0; i < count && buf.readableBytes() >= 13; i++) {
			int x = buf.readInt(), z = buf.readInt(), color = buf.readInt(), length = buf.readUnsignedByte();
			if(length > 128 || buf.readableBytes() < length) { claims.clear(); parts = 0; return; }
			byte[] idBytes = new byte[length];
			buf.readBytes(idBytes);
			String id = new String(idBytes, java.nio.charset.Charset.forName("UTF-8"));
			if(id.length() > 0) claims.add(new Claim(dimensionId, x, z, id, color));
		}
		if(claims.size() != count) { claims.clear(); parts = 0; }
	}
	public static class Handler implements IMessageHandler<ClaimOverlayPacket, IMessage> {
		@Override @SideOnly(Side.CLIENT) public IMessage onMessage(final ClaimOverlayPacket message, MessageContext ctx) {
			if(message.parts > 0) MainRegistry.proxy.receiveClaimOverlay(message.dimensionId, message.generation, message.part, message.parts, message.claims);
			return null;
		}
	}
}
