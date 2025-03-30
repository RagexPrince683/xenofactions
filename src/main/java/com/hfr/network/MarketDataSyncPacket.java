package com.hfr.network;

import com.hfr.data.MarketData;
import com.hfr.data.MarketData.ItemEntry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.List;

public class MarketDataSyncPacket implements IMessage {
    private NBTTagCompound marketDataNBT;

    public MarketDataSyncPacket() {}

    public MarketDataSyncPacket(HashMap<String, List<ItemEntry[]>> offers) {
        marketDataNBT = new NBTTagCompound();
        for (String market : offers.keySet()) {
            NBTTagList offerList = new NBTTagList();
            for (ItemEntry[] offer : offers.get(market)) {
                NBTTagCompound entryTag = new NBTTagCompound();
                NBTTagList itemList = new NBTTagList();
                for (ItemEntry entry : offer) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    itemTag.setString("item", entry.itemName);
                    itemTag.setInteger("count", entry.count);
                    itemTag.setInteger("meta", entry.metadata);
                    if (entry.nbtData != null) {
                        itemTag.setString("nbt", entry.nbtData);
                    }
                    itemList.appendTag(itemTag);
                }
                entryTag.setTag("items", itemList);
                offerList.appendTag(entryTag);
            }
            marketDataNBT.setTag(market, offerList);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        marketDataNBT = new NBTTagCompound();
        marketDataNBT = NBTTagCompound.func_150294_a(buf); // Read NBT from buffer
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound.func_150296_a(marketDataNBT, buf); // Write NBT to buffer
    }

    public static class Handler implements IMessageHandler<MarketDataSyncPacket, IMessage> {
        @Override
        public IMessage onMessage(MarketDataSyncPacket message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                MarketData.loadFromNBT(message.marketDataNBT);
            }
            return null;
        }
    }
}
