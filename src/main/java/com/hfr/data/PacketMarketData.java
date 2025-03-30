package com.hfr.data;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class PacketMarketData implements IMessage {
    private static final Gson GSON = new GsonBuilder().create();
    private HashMap<String, List<MarketData.ItemEntry[]>> offers;

    public PacketMarketData() {
        // Default constructor for network
    }

    public PacketMarketData(HashMap<String, List<MarketData.ItemEntry[]>> offers) {
        this.offers = offers;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        String json = GSON.toJson(offers);
        byte[] jsonData = json.getBytes();
        buf.writeInt(jsonData.length);
        buf.writeBytes(jsonData);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        byte[] jsonData = new byte[length];
        buf.readBytes(jsonData);
        String json = new String(jsonData);
        Type type = new TypeToken<HashMap<String, List<MarketData.ItemEntry[]>>>() {}.getType();
        this.offers = GSON.fromJson(json, type);
    }

    public static class Handler implements IMessageHandler<PacketMarketData, IMessage> {
        @Override
        public IMessage onMessage(PacketMarketData message, MessageContext ctx) {
            MarketData.offers.clear();
            MarketData.offers.putAll(message.offers);
            return null; // No response packet needed
        }
    }
}
