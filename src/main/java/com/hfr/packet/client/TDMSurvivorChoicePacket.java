package com.hfr.packet.client;

import com.hfr.tdm.TDMManager;
import com.hfr.tdm.TDMServerTaskQueue;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class TDMSurvivorChoicePacket implements IMessage {
    private boolean keep;
    public TDMSurvivorChoicePacket() { }
    public TDMSurvivorChoicePacket(boolean keep){this.keep=keep;}
    public void fromBytes(ByteBuf buffer){keep=buffer.readBoolean();}
    public void toBytes(ByteBuf buffer){buffer.writeBoolean(keep);}
    public static class Handler implements IMessageHandler<TDMSurvivorChoicePacket,IMessage>{public IMessage onMessage(final TDMSurvivorChoicePacket message,MessageContext context){final EntityPlayerMP player=context.getServerHandler().playerEntity;TDMServerTaskQueue.schedule(new Runnable(){public void run(){TDMManager.handleSurvivorChoice(player,message.keep);}});return null;}}
}
