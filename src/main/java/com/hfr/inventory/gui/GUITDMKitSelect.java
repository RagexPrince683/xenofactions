package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMKitSelectPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class GUITDMKitSelect extends GuiScreen {

    private final String team;
    private final String[] kitNames; private final int[] costs;private final boolean economy,buying;private final int balance,seconds;

    public GUITDMKitSelect(String team, String[] kitNames) {
        this(team,kitNames,new int[kitNames.length],false,0,0,false);
    }

    public GUITDMKitSelect(String team,String[] names,int[] costs,boolean economy,int balance,int seconds,boolean buying){this.team=team;this.kitNames=names;this.costs=costs;this.economy=economy;this.balance=balance;this.seconds=seconds;this.buying=buying;}

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
        PacketDispatcher.wrapper.sendToServer(new TDMKitSelectPacket(button.id));
        if(!buying)this.mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // Buying and legacy selection remain server-controlled.
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        String title = EnumChatFormatting.BOLD + "Select a " + team + " TDM Kit";
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2, this.height / 2 - (kitNames.length * 24) / 2 - 28, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, buying?("Buy time: "+seconds+"s"+(economy?"  Balance: "+balance:"")):"You have Resistance and Regeneration until you pick one.", this.width / 2, this.height / 2 - (kitNames.length * 24) / 2 - 16, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
