package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMMenuActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GUITDMMenu extends GuiScreen {
    private final String currentTeam;
    private final int cooldownSeconds;
    private final String[] lines;
    public GUITDMMenu(String currentTeam, int cooldownSeconds, String[] lines){ this.currentTeam=currentTeam; this.cooldownSeconds=cooldownSeconds; this.lines=lines; }
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width/2-100, this.height-40, 200, 20, cooldownSeconds > 0 ? "Swap Team ("+cooldownSeconds+"s)" : "Swap Team"));
        ((GuiButton)this.buttonList.get(0)).enabled = cooldownSeconds <= 0;
    }
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            PacketDispatcher.wrapper.sendToServer(new TDMMenuActionPacket(true));
        }
    }
    public void drawScreen(int mx,int my,float pt){
        this.drawDefaultBackground();
        drawCenteredString(this.fontRendererObj, "TDM Menu", this.width/2, 12, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, "Current team: "+currentTeam.toUpperCase(), this.width/2, 26, 0xDDDDDD);
        drawString(this.fontRendererObj, "Player | K/D | KDR", this.width/2-150, 44, 0xAAAAAA);
        int y=56;
        for (int i=0;i<lines.length && i<24;i++) {
            int color = (lines[i].startsWith("Friendly") || lines[i].startsWith("Enemy")) ? 0x55FFFF : 0xFFFFFF;
            drawString(this.fontRendererObj, lines[i], this.width/2-150, y, color);
            y+=10;
        }
        super.drawScreen(mx,my,pt);
    }
    public boolean doesGuiPauseGame(){ return false; }
}
