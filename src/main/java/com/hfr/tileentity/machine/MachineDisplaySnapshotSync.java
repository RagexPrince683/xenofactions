package com.hfr.tileentity.machine;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.MachineDisplaySnapshotPacket;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraft.entity.player.EntityPlayerMP;

public final class MachineDisplaySnapshotSync {
    public MachineDisplaySnapshotSync() { }
    @SubscribeEvent public void login(PlayerLoggedInEvent event) { if (event.player instanceof EntityPlayerMP) send((EntityPlayerMP) event.player); }
    public static void send(EntityPlayerMP player) { PacketDispatcher.wrapper.sendTo(new MachineDisplaySnapshotPacket(MachineDisplaySnapshot.fromRuntime()), player); }
}
