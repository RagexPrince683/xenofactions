package com.hfr.market;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketMarketDataSync implements IMessage {
    private String jsonData;

    public PacketMarketDataSync() {}

    public PacketMarketDataSync(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.jsonData = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.jsonData);
    }

    public static class Handler implements IMessageHandler<PacketMarketDataSync, IMessage> {
        @Override
        public IMessage onMessage(PacketMarketDataSync message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                MarketData.loadFromJson(message.jsonData); // Fixed reference
            }
            return null;
        }
    }
}