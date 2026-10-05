package com.hfr.packet.shop;

import com.hfr.shop.XShopService;
import com.hfr.tdm.TDMServerTaskQueue;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public final class XShopActionPacket implements IMessage {
    public String token = "", action = "", shopId = "", text = "";
    public int value;
    public long revision;
    public XShopActionPacket() { }
    public XShopActionPacket(String token, long revision, String action, String shopId, String text, int value) {
        this.token = token; this.revision = revision; this.action = action; this.shopId = shopId; this.text = text; this.value = value;
    }
    public void fromBytes(ByteBuf buf) {
        token = read(buf, 36); action = read(buf, 16); shopId = read(buf, 36); text = read(buf, 80);
        value = buf.readInt(); revision = buf.readLong();
    }
    private static String read(ByteBuf buf, int maximum) {
        String value = ByteBufUtils.readUTF8String(buf);
        if (value.length() > maximum) throw new IllegalArgumentException("Oversize shop argument");
        return value;
    }
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, token); ByteBufUtils.writeUTF8String(buf, action);
        ByteBufUtils.writeUTF8String(buf, shopId); ByteBufUtils.writeUTF8String(buf, text);
        buf.writeInt(value); buf.writeLong(revision);
    }
    public static final class Handler implements IMessageHandler<XShopActionPacket, IMessage> {
        public IMessage onMessage(final XShopActionPacket message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            TDMServerTaskQueue.schedule(new Runnable() { public void run() { XShopService.action(player, message); } });
            return null;
        }
    }
}
