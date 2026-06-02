package com.hfr.packet.client;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMMenuDataPacket;
import com.hfr.tdm.TDMManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class TDMMenuActionPacket implements IMessage {
    private boolean requestSwap;
    public TDMMenuActionPacket() {}
    public TDMMenuActionPacket(boolean requestSwap) { this.requestSwap = requestSwap; }
    public void fromBytes(ByteBuf buf){ requestSwap = buf.readBoolean(); }
    public void toBytes(ByteBuf buf){ buf.writeBoolean(requestSwap); }

    public static class Handler implements IMessageHandler<TDMMenuActionPacket, IMessage> {
        public IMessage onMessage(TDMMenuActionPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!TDMManager.isEnabled(player.worldObj)) {
                return null;
            }

            if (message.requestSwap) {
                TDMManager.changePlayerTeamWithCooldown(player);
            }

            PacketDispatcher.wrapper.sendTo(new TDMMenuDataPacket(player, TDMManager.getTeamChangeCooldownSeconds(player)), player);
            return null;
        }
    }
}
