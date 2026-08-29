package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMSurvivorChoicePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GUITDMSurvivorChoice extends GuiScreen {
    public void initGui(){buttonList.clear();buttonList.add(new GuiButton(0,width/2-100,height/2-12,200,20,I18n.format("gui.hfr.tdm.survivor.keep")));buttonList.add(new GuiButton(1,width/2-100,height/2+14,200,20,I18n.format("gui.hfr.tdm.survivor.buy")));}
    protected void actionPerformed(GuiButton button){PacketDispatcher.wrapper.sendToServer(new TDMSurvivorChoicePacket(button.id==0));mc.displayGuiScreen(null);}
    protected void keyTyped(char character,int key) { }
    public void drawScreen(int x,int y,float partial){drawDefaultBackground();drawCenteredString(fontRendererObj,I18n.format("gui.hfr.tdm.survivor.title"),width/2,height/2-38,0xFFFFFF);drawCenteredString(fontRendererObj,I18n.format("gui.hfr.tdm.survivor.help"),width/2,height/2-26,0xCCCCCC);super.drawScreen(x,y,partial);}
    public boolean doesGuiPauseGame(){return false;}
}
