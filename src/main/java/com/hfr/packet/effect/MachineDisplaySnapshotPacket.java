package com.hfr.packet.effect;

import com.hfr.tileentity.machine.MachineDisplaySnapshot;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class MachineDisplaySnapshotPacket implements IMessage {
    private static final int FIELD_COUNT = 14;
    public MachineDisplaySnapshot snapshot;
    public MachineDisplaySnapshotPacket() { }
    public MachineDisplaySnapshotPacket(MachineDisplaySnapshot snapshot) { this.snapshot = snapshot; }
    public void toBytes(ByteBuf buf) {
        MachineDisplaySnapshot s = snapshot == null ? MachineDisplaySnapshot.fromRuntime() : snapshot;
        // Field order: fishing rates (super/good/average/crap), fishing jam, whale chance, university rate/jam, factory rate/RF/jam, temple rate, coal rate/jam.
        buf.writeInt(s.superFishrate); buf.writeInt(s.goodFishrate); buf.writeInt(s.averageFishrate); buf.writeInt(s.crapFishrate);
        buf.writeInt(s.jamRate); buf.writeInt(s.whaleChance); buf.writeInt(s.uniRate); buf.writeInt(s.uniJamRate);
        buf.writeInt(s.factoryRate); buf.writeInt(s.factoryConsumption); buf.writeInt(s.factoryJamRate); buf.writeInt(s.temple);
        buf.writeInt(s.coalRate); buf.writeInt(s.coalJamRate);
    }
    public void fromBytes(ByteBuf buf) {
        if (buf.readableBytes() < FIELD_COUNT * 4) {
            snapshot = null;
            return;
        }
        MachineDisplaySnapshot decoded = new MachineDisplaySnapshot(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
        snapshot = decoded;
    }
    public static class Handler implements IMessageHandler<MachineDisplaySnapshotPacket, IMessage> {
        public IMessage onMessage(MachineDisplaySnapshotPacket message, MessageContext ctx) {
            if (ctx != null && ctx.side != Side.CLIENT) {
                return null;
            }
            if (message != null && message.snapshot != null) {
                MachineDisplaySnapshot.replaceClientSnapshot(message.snapshot);
            }
            return null;
        }
    }
}
