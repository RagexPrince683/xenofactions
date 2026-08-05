package com.hfr.packet.effect;

import com.hfr.tileentity.machine.MachineDisplaySnapshot;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MachineDisplaySnapshotPacket implements IMessage {
    public MachineDisplaySnapshot snapshot;
    public MachineDisplaySnapshotPacket() { }
    public MachineDisplaySnapshotPacket(MachineDisplaySnapshot snapshot) { this.snapshot = snapshot; }
    public void toBytes(ByteBuf buf) {
        MachineDisplaySnapshot s = snapshot == null ? MachineDisplaySnapshot.fromRuntime() : snapshot;
        buf.writeInt(s.superFishrate); buf.writeInt(s.goodFishrate); buf.writeInt(s.averageFishrate); buf.writeInt(s.crapFishrate);
        buf.writeInt(s.jamRate); buf.writeInt(s.whaleChance); buf.writeInt(s.windmillProduction);
    }
    public void fromBytes(ByteBuf buf) {
        snapshot = new MachineDisplaySnapshot(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }
    public static class Handler implements IMessageHandler<MachineDisplaySnapshotPacket, IMessage> {
        public IMessage onMessage(MachineDisplaySnapshotPacket message, MessageContext ctx) {
            if (message != null && message.snapshot != null) MachineDisplaySnapshot.replaceClientSnapshot(message.snapshot);
            return null;
        }
    }
}
