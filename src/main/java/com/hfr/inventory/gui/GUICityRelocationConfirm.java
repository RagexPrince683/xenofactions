package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.CityRelocationResponsePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GUICityRelocationConfirm extends GuiScreen {
    private final int dimension, x, y, z;
    private final String city;
    public GUICityRelocationConfirm(int dimension,int x,int y,int z,String city) {
        this.dimension=dimension;this.x=x;this.y=y;this.z=z;this.city=city;
    }
    @Override public void initGui(){buttonList.add(new GuiButton(0,width/2-105,height/2+55,100,20,"Start Move"));buttonList.add(new GuiButton(1,width/2+5,height/2+55,100,20,"Cancel"));}
    @Override protected void actionPerformed(GuiButton b){PacketDispatcher.wrapper.sendToServer(new CityRelocationResponsePacket(dimension,x,y,z,b.id==0));Minecraft.getMinecraft().displayGuiScreen(null);}
    @Override public void drawScreen(int mx,int my,float partial){drawDefaultBackground();drawCenteredString(fontRendererObj,"Move City Center: "+city,width/2,height/2-75,0xffffff);String[] lines={"The current City Center and territory remain active until the move succeeds.","The destination must remain inside this city's existing territory.","Moves beyond 10 blocks cost 30 Prestige per additional block.","Each City Center can move 3 times within a rolling 7-day window.","The third move must wait 30 minutes after the second move.","Canceling leaves the city unchanged."};int yy=height/2-55;for(String line:lines){drawCenteredString(fontRendererObj,line,width/2,yy,0xdddddd);yy+=14;}super.drawScreen(mx,my,partial);}
}
