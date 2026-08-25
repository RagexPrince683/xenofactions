package com.hfr.packet.client;

import com.hfr.tdm.TDMManager;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMKitSelectResultPacket;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class TDMKitSelectPacket implements IMessage {

    private int kitIndex;

    public TDMKitSelectPacket() { }

    public TDMKitSelectPacket(int kitIndex) {
        this.kitIndex = kitIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        kitIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(kitIndex);
    }

    public static class Handler implements IMessageHandler<TDMKitSelectPacket, IMessage> {

        @Override
        public IMessage onMessage(final TDMKitSelectPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            MinecraftServer.getServer().func_152344_a(new Runnable() { public void run() {
                TDMManager.KitSelectionResult result=TDMManager.selectKit(player,message.kitIndex);
                if(result!=TDMManager.KitSelectionResult.SUCCESS)PacketDispatcher.wrapper.sendTo(new TDMKitSelectResultPacket(result),player);
            }});
            return null;
        }
    }
}
