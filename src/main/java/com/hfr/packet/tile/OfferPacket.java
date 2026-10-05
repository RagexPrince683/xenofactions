package com.hfr.packet.tile;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

/** Retired wire discriminator. Legacy requests cannot bypass XShop sessions. */
public final class OfferPacket implements IMessage {
    private int x, y, z;
    private String name = "";
    private NBTTagCompound nbt;
    public OfferPacket() { }
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt(); y = buf.readInt(); z = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf); nbt = ByteBufUtils.readTag(buf);
    }
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x); buf.writeInt(y); buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, name); ByteBufUtils.writeTag(buf, nbt);
    }
    public static final class ServerHandler implements IMessageHandler<OfferPacket, IMessage> {
        public IMessage onMessage(OfferPacket message, MessageContext context) { return null; }
    }
    public static final class ClientHandler implements IMessageHandler<OfferPacket, IMessage> {
        public IMessage onMessage(OfferPacket message, MessageContext context) { return null; }
    }
}
