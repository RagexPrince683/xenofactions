package com.hfr.packet.client;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMMenuDataPacket;
import com.hfr.tdm.TDMManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class TDMMenuActionPacket implements IMessage {
    private boolean requestSwap,requestBuyMenu;
    public TDMMenuActionPacket() {}
    public TDMMenuActionPacket(boolean requestSwap) { this.requestSwap = requestSwap; }
    public TDMMenuActionPacket(boolean requestSwap,boolean requestBuyMenu){this.requestSwap=requestSwap;this.requestBuyMenu=requestBuyMenu;}
    public void fromBytes(ByteBuf buf){ requestSwap = buf.readBoolean();requestBuyMenu=buf.readBoolean(); }
    public void toBytes(ByteBuf buf){ buf.writeBoolean(requestSwap);buf.writeBoolean(requestBuyMenu); }

    public static class Handler implements IMessageHandler<TDMMenuActionPacket, IMessage> {
        public IMessage onMessage(final TDMMenuActionPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            MinecraftServer.getServer().func_152344_a(new Runnable(){public void run(){
            if (!TDMManager.isEnabled(player.worldObj)) {
                return;
            }

            if (message.requestSwap) {
                TDMManager.changePlayerTeamWithCooldown(player);
            }
            if(message.requestBuyMenu&&TDMManager.isBombMode(player.worldObj)&&com.hfr.tdm.TDMBombManager.getState()==com.hfr.tdm.TDMBombManager.BombRoundState.PRE_ROUND&&!TDMManager.hasSelectedKit(player))TDMManager.promptForKit(player);

            PacketDispatcher.wrapper.sendTo(new TDMMenuDataPacket(player, TDMManager.getTeamChangeCooldownSeconds(player)), player);
            }});
            return null;
        }
    }
}
