package com.hfr.packet.effect;

import com.hfr.tileentity.machine.MachineDisplaySnapshot;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MachineDisplaySnapshotPacketTest {
    @Test public void packetFieldOrderRoundTrips() { MachineDisplaySnapshot source = new MachineDisplaySnapshot(1,2,3,4,5,6,7,8,9,10,11,12,13,14); ByteBuf buf = Unpooled.buffer(); new MachineDisplaySnapshotPacket(source).toBytes(buf); MachineDisplaySnapshotPacket packet = new MachineDisplaySnapshotPacket(); packet.fromBytes(buf); assertEquals(1, packet.snapshot.superFishrate); assertEquals(6, packet.snapshot.whaleChance); assertEquals(10, packet.snapshot.factoryConsumption); assertEquals(14, packet.snapshot.coalJamRate); }
    @Test public void incompletePacketIsRejected() { ByteBuf buf = Unpooled.buffer(); buf.writeInt(1); MachineDisplaySnapshotPacket packet = new MachineDisplaySnapshotPacket(); packet.fromBytes(buf); assertNull(packet.snapshot); }
}
