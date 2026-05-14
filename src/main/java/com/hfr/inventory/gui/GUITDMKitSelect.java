package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMKitSelectPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class GUITDMKitSelect extends GuiScreen {

    private final String team;
    private final String[] kitNames;

    public GUITDMKitSelect(String team, String[] kitNames) {
        this.team = team;
        this.kitNames = kitNames;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int x = this.width / 2 - 100;
        int y = this.height / 2 - (kitNames.length * 24) / 2;

        for (int i = 0; i < kitNames.length; i++) {
            this.buttonList.add(new GuiButton(i, x, y + i * 24, 200, 20, kitNames[i]));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        PacketDispatcher.wrapper.sendToServer(new TDMKitSelectPacket(button.id));
        this.mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // Do not allow the menu to be escaped before a kit is selected.
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        String title = EnumChatFormatting.BOLD + "Select a " + team + " TDM Kit";
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2, this.height / 2 - (kitNames.length * 24) / 2 - 28, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "You have Resistance and Regeneration until you pick one.", this.width / 2, this.height / 2 - (kitNames.length * 24) / 2 - 16, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
