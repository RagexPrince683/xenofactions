package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMMapVoteSelectPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class GUITDMMapVote extends GuiScreen {

    private final String[] mapNames;
    private final int voteSeconds;

    public GUITDMMapVote(String[] mapNames, int voteSeconds) {
        this.mapNames = mapNames;
        this.voteSeconds = voteSeconds;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int x = this.width / 2 - 100;
        int y = this.height / 2 - (mapNames.length * 24) / 2;

        for (int i = 0; i < mapNames.length; i++) {
            this.buttonList.add(new GuiButton(i, x, y + i * 24, 200, 20, mapNames[i]));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 0 && button.id < mapNames.length) {
            PacketDispatcher.wrapper.sendToServer(new TDMMapVoteSelectPacket(mapNames[button.id]));
        }
        this.mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int y = this.height / 2 - (mapNames.length * 24) / 2 - 32;
        this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.BOLD + "Vote for the next TDM map", this.width / 2, y, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Voting ends in " + voteSeconds + " seconds.", this.width / 2, y + 12, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
