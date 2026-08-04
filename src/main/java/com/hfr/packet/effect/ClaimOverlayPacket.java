package com.hfr.packet.effect;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;

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
	public static final int MAX_GROUP_ID_BYTES = 128;
	public static final int MAX_LABEL_BYTES = 256;
	public static final int MAX_LABEL_CHARS = 64;
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private int dimensionId, generation, part, parts;
	private List<Claim> claims = new ArrayList<Claim>();

	public ClaimOverlayPacket() { }
	public ClaimOverlayPacket(int dimensionId, int generation, int part, int parts, List<Claim> claims) {
		this.dimensionId = dimensionId; this.generation = generation; this.part = part; this.parts = parts; this.claims = claims;
	}
	@Override public void toBytes(ByteBuf buf) {
		buf.writeInt(dimensionId); buf.writeInt(generation); buf.writeShort(part); buf.writeShort(parts); buf.writeShort(claims.size());
		for(Claim claim : claims) {
			buf.writeInt(claim.chunkX); buf.writeInt(claim.chunkZ); buf.writeInt(claim.color); buf.writeInt(claim.labelX); buf.writeInt(claim.labelZ);
			writeString(buf, claim.groupId, MAX_GROUP_ID_BYTES);
			writeString(buf, claim.label, MAX_LABEL_BYTES);
		}
	}
	@Override public void fromBytes(ByteBuf buf) {
		claims = new ArrayList<Claim>();
		if(buf.readableBytes() < 14) return;
		dimensionId = buf.readInt(); generation = buf.readInt(); part = buf.readUnsignedShort(); parts = buf.readUnsignedShort();
		int count = buf.readUnsignedShort();
		if(parts < 1 || parts > 4096 || part >= parts || count > MAX_CLAIMS) { claims.clear(); parts = 0; return; }
		for(int i = 0; i < count && buf.readableBytes() >= 22; i++) {
			int x = buf.readInt(), z = buf.readInt(), color = buf.readInt(), labelX = buf.readInt(), labelZ = buf.readInt();
			String id = readString(buf, MAX_GROUP_ID_BYTES);
			String label = id == null ? null : readString(buf, MAX_LABEL_BYTES);
			if(id == null || label == null) { claims.clear(); parts = 0; return; }
			if(id.length() > 0) claims.add(new Claim(dimensionId, x, z, id, color, label, labelX, labelZ));
		}
		if(claims.size() != count) { claims.clear(); parts = 0; }
	}
	private static void writeString(ByteBuf buf, String value, int maxBytes) {
		byte[] bytes = (value == null ? "" : value).getBytes(UTF_8);
		int length = Math.min(bytes.length, maxBytes); buf.writeShort(length); buf.writeBytes(bytes, 0, length);
	}
	private static String readString(ByteBuf buf, int maxBytes) {
		if(buf.readableBytes() < 2) return null;
		int length = buf.readUnsignedShort();
		if(length < 0 || length > maxBytes || buf.readableBytes() < length) return null;
		byte[] bytes = new byte[length]; buf.readBytes(bytes);
		String value;
		try {
			value = UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(bytes)).toString();
		} catch(CharacterCodingException failure) { return null; }
		return value.length() > MAX_LABEL_CHARS && maxBytes == MAX_LABEL_BYTES ? null : value;
	}
	public static class Handler implements IMessageHandler<ClaimOverlayPacket, IMessage> {
		@Override @SideOnly(Side.CLIENT) public IMessage onMessage(final ClaimOverlayPacket message, MessageContext ctx) {
			if(message.parts > 0) MainRegistry.proxy.receiveClaimOverlay(message.dimensionId, message.generation, message.part, message.parts, message.claims);
			return null;
		}
	}
}
