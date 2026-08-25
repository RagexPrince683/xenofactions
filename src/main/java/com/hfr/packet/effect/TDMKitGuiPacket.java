package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUITDMKitSelect;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class TDMKitGuiPacket implements IMessage {

    private String team;
    private String[] kitNames;
    private int[] costs; private boolean economy,buying; private int balance,seconds;

    public TDMKitGuiPacket() { }

    public TDMKitGuiPacket(String team, String[] kitNames) {
        this(team,kitNames,new int[kitNames.length],false,0,0,false);
    }

    public TDMKitGuiPacket(String team,String[] names,int[] costs,boolean economy,int balance,int seconds,boolean buying){this.team=team;this.kitNames=names;this.costs=costs;this.economy=economy;this.balance=balance;this.seconds=seconds;this.buying=buying;}

    @Override
    public void fromBytes(ByteBuf buf) {
        team = ByteBufUtils.readUTF8String(buf);
        int count = buf.readInt();
        kitNames = new String[count];
        for (int i = 0; i < count; i++) {
            kitNames[i] = ByteBufUtils.readUTF8String(buf);
        } costs=new int[count];for(int i=0;i<count;i++)costs[i]=buf.readInt();economy=buf.readBoolean();balance=buf.readInt();seconds=buf.readInt();buying=buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, team);
        buf.writeInt(kitNames.length);
        for (int i = 0; i < kitNames.length; i++) {
            ByteBufUtils.writeUTF8String(buf, kitNames[i]);
        } for(int cost:costs)buf.writeInt(cost);buf.writeBoolean(economy);buf.writeInt(balance);buf.writeInt(seconds);buf.writeBoolean(buying);
    }

    public static class Handler implements IMessageHandler<TDMKitGuiPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final TDMKitGuiPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){
                if(message.kitNames.length==0){if(Minecraft.getMinecraft().currentScreen instanceof GUITDMKitSelect)Minecraft.getMinecraft().displayGuiScreen(null);}
                else Minecraft.getMinecraft().displayGuiScreen(new GUITDMKitSelect(message.team,message.kitNames,message.costs,message.economy,message.balance,message.seconds,message.buying));
            }});
            return null;
        }
    }
}
