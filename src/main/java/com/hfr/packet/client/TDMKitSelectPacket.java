package com.hfr.packet.client;

import com.hfr.tdm.TDMManager;
import com.hfr.tdm.TDMServerTaskQueue;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMKitSelectResultPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class TDMKitSelectPacket implements IMessage {

    private int kitIndex;
    private String kitPool = "";

    public TDMKitSelectPacket() { }

    public TDMKitSelectPacket(int kitIndex, String kitPool) {
        this.kitIndex = kitIndex;
        this.kitPool = kitPool == null ? "" : kitPool;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        kitIndex = buf.readInt();
        int poolLength = buf.readUnsignedByte();
        if (poolLength > 4 || buf.readableBytes() < poolLength) {
            throw new IllegalArgumentException("Invalid TDM kit pool");
        }
        byte[] poolBytes = new byte[poolLength];
        buf.readBytes(poolBytes);
        kitPool = new String(poolBytes, java.nio.charset.Charset.forName("US-ASCII"));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(kitIndex);
        byte[] poolBytes = kitPool.getBytes(java.nio.charset.Charset.forName("US-ASCII"));
        if (poolBytes.length > 4) {
            throw new IllegalArgumentException("Invalid TDM kit pool");
        }
        buf.writeByte(poolBytes.length);
        buf.writeBytes(poolBytes);
    }

    public static class Handler implements IMessageHandler<TDMKitSelectPacket, IMessage> {

        @Override
        public IMessage onMessage(final TDMKitSelectPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TDMServerTaskQueue.schedule(new Runnable() {
                @Override
                public void run() {
                    if (!isPlayerConnected(player)) {
                        return;
                    }

                    TDMManager.KitSelectionResult result =
                            TDMManager.selectKit(player, message.kitIndex,
                                    TDMManager.Team.fromName(message.kitPool));
                    if (result != TDMManager.KitSelectionResult.SUCCESS) {
                        PacketDispatcher.wrapper.sendTo(
                                new TDMKitSelectResultPacket(result), player);
                    }
                }
            });
            return null;
        }

        private boolean isPlayerConnected(EntityPlayerMP player) {
            return player != null && player.worldObj != null
                    && player.worldObj.playerEntities.contains(player)
                    && player.playerNetServerHandler != null;
        }
    }
}
