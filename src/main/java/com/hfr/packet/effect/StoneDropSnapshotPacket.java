package com.hfr.packet.effect;

import com.hfr.stonedrops.StoneDropDisplayEntry;
import com.hfr.stonedrops.StoneDropDisplaySnapshot;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class StoneDropSnapshotPacket implements IMessage {
    public List<StoneDropDisplayEntry> entries = new ArrayList<StoneDropDisplayEntry>();
    public StoneDropSnapshotPacket() { }
    public StoneDropSnapshotPacket(List<StoneDropDisplayEntry> entries) { this.entries = entries; }

    public void toBytes(ByteBuf buf) {
        int size = entries == null ? 0 : Math.min(entries.size(), StoneDropDisplaySnapshot.MAX_ENTRIES);
        buf.writeShort(size);
        for (int i = 0; i < size; i++) {
            StoneDropDisplayEntry e = entries.get(i);
            ByteBufUtils.writeUTF8String(buf, e.itemName);
            buf.writeInt(e.metadata); buf.writeByte(e.stackSize); buf.writeDouble(e.chance);
            buf.writeBoolean(e.minY != null); if (e.minY != null) buf.writeInt(e.minY.intValue());
            buf.writeBoolean(e.maxY != null); if (e.maxY != null) buf.writeInt(e.maxY.intValue());
            ByteBufUtils.writeTag(buf, e.nbt);
        }
    }

    public void fromBytes(ByteBuf buf) {
        entries = new ArrayList<StoneDropDisplayEntry>();
        int size = buf.readUnsignedShort();
        if (size > StoneDropDisplaySnapshot.MAX_ENTRIES) { buf.skipBytes(buf.readableBytes()); return; }
        for (int i = 0; i < size; i++) {
            String name = ByteBufUtils.readUTF8String(buf);
            int meta = buf.readInt(); int count = buf.readUnsignedByte(); double chance = buf.readDouble();
            Integer min = buf.readBoolean() ? Integer.valueOf(buf.readInt()) : null;
            Integer max = buf.readBoolean() ? Integer.valueOf(buf.readInt()) : null;
            NBTTagCompound tag = ByteBufUtils.readTag(buf);
            StoneDropDisplayEntry e = new StoneDropDisplayEntry(name, meta, count, chance, min, max, tag);
            if (StoneDropDisplaySnapshot.isValid(e)) entries.add(e);
        }
    }

    public static class Handler implements IMessageHandler<StoneDropSnapshotPacket, IMessage> {
        public IMessage onMessage(StoneDropSnapshotPacket message, MessageContext ctx) {
            StoneDropDisplaySnapshot.replaceClientSnapshot(message.entries);
            return null;
        }
    }
}
