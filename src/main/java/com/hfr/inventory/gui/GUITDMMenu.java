package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMMenuActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GUITDMMenu extends GuiScreen {
    private final String currentTeam;
    private final int cooldownSeconds;
    private final String[] friendlyLines;
    private final String[] enemyLines;
    private final boolean canOpenBuyMenu;
    private final boolean ffa;
    public GUITDMMenu(String currentTeam, int cooldownSeconds, String[] friendlyLines, String[] enemyLines,boolean canOpenBuyMenu,boolean ffa){
        this.currentTeam = currentTeam;
        this.cooldownSeconds = cooldownSeconds;
        this.friendlyLines = friendlyLines == null ? new String[0] : friendlyLines;
        this.enemyLines = enemyLines == null ? new String[0] : enemyLines;
        this.canOpenBuyMenu=canOpenBuyMenu;
        this.ffa=ffa;
    }
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width/2-100, this.height-40, 200, 20, ffa ? "FFA has no teams" : (cooldownSeconds > 0 ? "Swap Team ("+cooldownSeconds+"s)" : "Swap Team")));
        ((GuiButton)this.buttonList.get(0)).enabled = !ffa && cooldownSeconds <= 0;
        if(canOpenBuyMenu)this.buttonList.add(new GuiButton(1,this.width/2-100,this.height-64,200,20,"Reopen Buy Menu"));
    }
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            PacketDispatcher.wrapper.sendToServer(new TDMMenuActionPacket(true));
        } else if(button.id==1){
            PacketDispatcher.wrapper.sendToServer(new TDMMenuActionPacket(false,true));
        }
    }
    public void drawScreen(int mx,int my,float pt){
        this.drawDefaultBackground();
        int panelTop = 18;
        int panelBottom = this.height - 52;
        int panelHeight = panelBottom - panelTop;
        int gutter = 8;
        int panelWidth = Math.min(260, (this.width - 40 - gutter) / 2);
        int leftX = this.width / 2 - panelWidth - gutter / 2;
        int rightX = this.width / 2 + gutter / 2;

        drawCenteredString(this.fontRendererObj, "TDM Scoreboard", this.width/2, 5, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, "Current team: " + currentTeam.toUpperCase(), this.width/2, 14, 0xDDDDDD);

        if(ffa){int x=this.width/2-panelWidth;int w=panelWidth*2;drawRect(x,panelTop,x+w,panelBottom,0x90333333);drawRect(x,panelTop,x+w,panelTop+14,0xCC777777);drawString(this.fontRendererObj,"FFA PLAYERS",x+5,panelTop+3,0xFFFFFF);drawString(this.fontRendererObj,"PLAYER | K/D | KDR",x+5,panelTop+18,0xDDDDDD);drawRosterColumn(friendlyLines,x+5,panelTop+30,panelHeight-35,0xF0F0F0);super.drawScreen(mx,my,pt);return;}

        drawRect(leftX, panelTop, leftX + panelWidth, panelBottom, 0x9020334A);
        drawRect(rightX, panelTop, rightX + panelWidth, panelBottom, 0x904A2020);

        drawRect(leftX, panelTop, leftX + panelWidth, panelTop + 14, 0xCC2E6DA4);
        drawRect(rightX, panelTop, rightX + panelWidth, panelTop + 14, 0xCC9C2D2D);

        drawString(this.fontRendererObj, "BLUE", leftX + 5, panelTop + 3, 0xFFFFFF);
        drawString(this.fontRendererObj, "RED", rightX + 5, panelTop + 3, 0xFFFFFF);
        drawString(this.fontRendererObj, "PLAYER | K/D | KDR", leftX + 5, panelTop + 18, 0x8FD3FF);
        drawString(this.fontRendererObj, "PLAYER | K/D | KDR", rightX + 5, panelTop + 18, 0xFF8C8C);

        drawRosterColumn(friendlyLines, leftX + 5, panelTop + 30, panelHeight - 35, 0xE8F6FF);
        drawRosterColumn(enemyLines, rightX + 5, panelTop + 30, panelHeight - 35, 0xFFE9E9);
        super.drawScreen(mx,my,pt);
    }

    private void drawRosterColumn(String[] lines, int x, int y, int maxHeight, int color) {
        int maxRows = maxHeight / 10;
        int rows = Math.min(maxRows, lines.length);
        for (int i = 0; i < rows; i++) {
            drawString(this.fontRendererObj, lines[i], x, y + (i * 10), color);
        }
        if (lines.length == 0) {
            drawString(this.fontRendererObj, "(none)", x, y, 0x888888);
        }
    }
    public boolean doesGuiPauseGame(){ return false; }
}
