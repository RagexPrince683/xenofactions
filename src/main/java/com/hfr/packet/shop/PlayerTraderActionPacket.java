package com.hfr.packet.shop;

import com.hfr.shop.PlayerTraderService;
import com.hfr.tdm.TDMServerTaskQueue;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public final class PlayerTraderActionPacket implements IMessage {
    public String token = "", action = "";
    public long revision;
    public int slot, quantity;
    public PlayerTraderActionPacket() { }
    public PlayerTraderActionPacket(String token, String action, long revision, int slot, int quantity) {
        this.token = token; this.action = action; this.revision = revision; this.slot = slot; this.quantity = quantity;
    }
    @Override public void fromBytes(ByteBuf buf) {
        token = ByteBufUtils.readUTF8String(buf); action = ByteBufUtils.readUTF8String(buf);
        if (token.length() > 36 || action.length() > 16) throw new IllegalArgumentException("Oversize trader action");
        revision = buf.readLong(); slot = buf.readInt(); quantity = buf.readInt();
    }
    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, token); ByteBufUtils.writeUTF8String(buf, action);
        buf.writeLong(revision); buf.writeInt(slot); buf.writeInt(quantity);
    }
    public static final class Handler implements IMessageHandler<PlayerTraderActionPacket, IMessage> {
        @Override public IMessage onMessage(final PlayerTraderActionPacket packet, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            TDMServerTaskQueue.schedule(new Runnable() { @Override public void run() {
                PlayerTraderService.action(player, packet.token, packet.action, packet.revision, packet.slot, packet.quantity);
            }});
            return null;
        }
    }
}
