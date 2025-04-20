package com.hfr.market;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class MarketDataSyncPacket implements IMessage {
    private String jsonData;

    public MarketDataSyncPacket() {}

    public MarketDataSyncPacket(String jsonData) {
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

    public static class Handler implements IMessageHandler<MarketDataSyncPacket, IMessage> {
        @Override
        public IMessage onMessage(MarketDataSyncPacket message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    MarketData.updateClientData(message.jsonData);
                });
            }
            return null;
        }
    }
}