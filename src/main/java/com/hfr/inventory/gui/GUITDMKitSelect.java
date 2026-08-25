package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMKitSelectPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ChatComponentText;

public class GUITDMKitSelect extends GuiScreen {

    private final String team;
    private final String[] kitNames; private final int[] costs;private final boolean economy,buying,mandatory;private final int balance,seconds;
    private final long buyEndMillis;
    private boolean awaitingSelectionResult;

    public GUITDMKitSelect(String team, String[] kitNames) {
        this(team,kitNames,new int[kitNames.length],false,0,0,false,false);
    }

    public GUITDMKitSelect(String team,String[] names,int[] costs,boolean economy,int balance,int seconds,boolean buying){this(team,names,costs,economy,balance,seconds,buying,false);}
    public GUITDMKitSelect(String team,String[] names,int[] costs,boolean economy,int balance,int seconds,boolean buying,boolean mandatory){this.team=team;this.kitNames=names;this.costs=costs;this.economy=economy;this.balance=balance;this.seconds=seconds;this.buying=buying;this.mandatory=mandatory;this.buyEndMillis=System.currentTimeMillis()+seconds*1000L;}

    @Override
    public void initGui() {
        this.buttonList.clear();
        int x = this.width / 2 - 100;
        int y = this.height / 2 - (kitNames.length * 24) / 2;

        for (int i = 0; i < kitNames.length; i++) {
            this.buttonList.add(new GuiButton(i, x, y + i * 24, 200, 20, kitNames[i]+" - "+(costs[i]==0?"FREE":Integer.toString(costs[i]))));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(awaitingSelectionResult)return;
        if(buying||mandatory){awaitingSelectionResult=true;setButtonsEnabled(false);}
        PacketDispatcher.wrapper.sendToServer(new TDMKitSelectPacket(button.id));
        if(!buying&&!mandatory)this.mc.displayGuiScreen(null);
    }

    public void receiveSelectionResult(com.hfr.tdm.TDMManager.KitSelectionResult result){
        if(result==com.hfr.tdm.TDMManager.KitSelectionResult.SUCCESS){awaitingSelectionResult=true;com.hfr.main.EventHandlerClient.clearMandatoryKitGui(true);return;}
        awaitingSelectionResult=false;setButtonsEnabled(true);
        String message=result==com.hfr.tdm.TDMManager.KitSelectionResult.INSUFFICIENT_FUNDS?"You cannot afford that kit.":result==com.hfr.tdm.TDMManager.KitSelectionResult.INVALID_SELECTION?"That kit is no longer available.":result==com.hfr.tdm.TDMManager.KitSelectionResult.ALREADY_SELECTED?"You already selected a kit for this round.":"Kit selection is no longer active.";
        if(this.mc.thePlayer!=null)this.mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }

    private void setButtonsEnabled(boolean enabled){for(Object entry:this.buttonList)((GuiButton)entry).enabled=enabled;}

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(mandatory&&keyCode==1)return;super.keyTyped(typedChar,keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        String title = EnumChatFormatting.BOLD + "Select a " + team + " TDM Kit";
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2, this.height / 2 - (kitNames.length * 24) / 2 - 28, 0xFFFFFF);
        int remaining=buying?(int)Math.max(0L,(buyEndMillis-System.currentTimeMillis()+999L)/1000L):seconds;
        this.drawCenteredString(this.fontRendererObj, buying?("Buy time: "+remaining+"s"+(economy?"  Balance: "+balance:"")):"You have Resistance and Regeneration until you pick one.", this.width / 2, this.height / 2 - (kitNames.length * 24) / 2 - 16, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
