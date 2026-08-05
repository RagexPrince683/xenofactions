package com.hfr.packet.client;

import com.hfr.clowder.CityCenterRelocationManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage; import cpw.mods.fml.common.network.simpleimpl.IMessageHandler; import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class CityRelocationResponsePacket implements IMessage {
    private int d,x,y,z; private boolean confirm;
    public CityRelocationResponsePacket(){} public CityRelocationResponsePacket(int d,int x,int y,int z,boolean c){this.d=d;this.x=x;this.y=y;this.z=z;confirm=c;}
    public void toBytes(ByteBuf b){b.writeInt(d);b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeBoolean(confirm);}
    public void fromBytes(ByteBuf b){if(b.readableBytes()!=17)throw new IllegalArgumentException("Invalid relocation response");d=b.readInt();x=b.readInt();y=b.readInt();z=b.readInt();confirm=b.readBoolean();}
    public static class Handler implements IMessageHandler<CityRelocationResponsePacket,IMessage>{public IMessage onMessage(final CityRelocationResponsePacket m,MessageContext c){final EntityPlayerMP p=c.getServerHandler().playerEntity;CityCenterRelocationManager.schedule(new Runnable(){public void run(){if(m.confirm)CityCenterRelocationManager.start(p,m.d,m.x,m.y,m.z);}});return null;}}
}
