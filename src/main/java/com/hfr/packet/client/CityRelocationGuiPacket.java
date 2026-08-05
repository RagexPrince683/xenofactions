package com.hfr.packet.client;

import com.hfr.inventory.gui.GUICityRelocationConfirm;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side; import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class CityRelocationGuiPacket implements IMessage {
    private int d,x,y,z; private String city="";
    public CityRelocationGuiPacket(){}
    public CityRelocationGuiPacket(int d,int x,int y,int z,String city){this.d=d;this.x=x;this.y=y;this.z=z;this.city=city==null?"":city;}
    public void toBytes(ByteBuf b){b.writeInt(d);b.writeInt(x);b.writeInt(y);b.writeInt(z);ByteBufUtils.writeUTF8String(b,city);}
    public void fromBytes(ByteBuf b){d=b.readInt();x=b.readInt();y=b.readInt();z=b.readInt();city=ByteBufUtils.readUTF8String(b);if(city.length()>64)city=city.substring(0,64);}
    public static class Handler implements IMessageHandler<CityRelocationGuiPacket,IMessage>{@SideOnly(Side.CLIENT) public IMessage onMessage(final CityRelocationGuiPacket m,MessageContext c){Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){Minecraft.getMinecraft().displayGuiScreen(new GUICityRelocationConfirm(m.d,m.x,m.y,m.z,m.city));}});return null;}}
}
