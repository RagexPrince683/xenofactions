package com.hfr.packet.effect;

import com.hfr.main.EventHandlerClient;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class TDMSpectatorPacket implements IMessage {
    private int entityId; private String name="";
    public TDMSpectatorPacket(){}
    public TDMSpectatorPacket(int id,String name){entityId=id;this.name=name==null?"":name;}
    public void fromBytes(ByteBuf b){entityId=b.readInt();name=ByteBufUtils.readUTF8String(b);}
    public void toBytes(ByteBuf b){b.writeInt(entityId);ByteBufUtils.writeUTF8String(b,name);}
    public static class Handler implements IMessageHandler<TDMSpectatorPacket,IMessage>{@SideOnly(Side.CLIENT)public IMessage onMessage(TDMSpectatorPacket m,MessageContext c){EventHandlerClient.updateTDMSpectator(m.entityId,m.name);return null;}}
}
