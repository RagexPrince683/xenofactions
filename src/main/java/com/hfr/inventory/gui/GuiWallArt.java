package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.wallart.WallArtConfigurePacket;
import com.hfr.tileentity.TileEntityWallImage;
import com.hfr.wallart.WallArtConstants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class GuiWallArt extends GuiScreen {
    private static final int BUTTON_DOWNLOAD = 0;
    private static final int BUTTON_WIDTH = 1;
    private static final int BUTTON_HEIGHT = 2;
    private static final int CONTROL_WIDTH = 240;

    private final int dimension;
    private final int controllerX;
    private final int controllerY;
    private final int controllerZ;

    private int displayWidth;
    private int displayHeight;
    private GuiTextField urlField;
    private String statusMessage = "";

    public GuiWallArt(TileEntityWallImage tile) {
        dimension = tile.getWorldObj().provider.dimensionId;
        controllerX = tile.xCoord;
        controllerY = tile.yCoord;
        controllerZ = tile.zCoord;
        displayWidth = tile.getDisplayWidth();
        displayHeight = tile.getDisplayHeight();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        String existingUrl = urlField == null ? "" : urlField.getText();
        int left = this.width / 2 - CONTROL_WIDTH / 2;
        int top = this.height / 2 - 70;

        urlField = new GuiTextField(fontRendererObj, left, top + 38, CONTROL_WIDTH, 20);
        urlField.setMaxStringLength(WallArtConstants.MAX_URL_BYTES);
        urlField.setText(existingUrl);
        urlField.setFocused(true);

        buttonList.add(new GuiButton(BUTTON_WIDTH, left, top + 78, 115, 20,
            widthButtonText()));
        buttonList.add(new GuiButton(BUTTON_HEIGHT, left + 125, top + 78, 115, 20,
            heightButtonText()));
        buttonList.add(new GuiButton(BUTTON_DOWNLOAD, this.width / 2 - 60, top + 108,
            120, 20, "Download"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_WIDTH) {
            displayWidth = displayWidth % WallArtConstants.MAX_SIZE + 1;
            button.displayString = widthButtonText();
        } else if (button.id == BUTTON_HEIGHT) {
            displayHeight = displayHeight % WallArtConstants.MAX_SIZE + 1;
            button.displayString = heightButtonText();
        } else if (button.id == BUTTON_DOWNLOAD) {
            String enteredUrl = urlField.getText().trim();
            if (enteredUrl.length() == 0) {
                statusMessage = "Enter an HTTPS image URL.";
                return;
            }

            PacketDispatcher.wrapper.sendToServer(new WallArtConfigurePacket(
                dimension,
                controllerX,
                controllerY,
                controllerZ,
                displayWidth,
                displayHeight,
                enteredUrl));
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        urlField.textboxKeyTyped(character, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        urlField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        urlField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int top = this.height / 2 - 70;
        drawCenteredString(fontRendererObj, "Wall Art", this.width / 2, top, 0xFFFFFF);
        drawString(fontRendererObj, "HTTPS Image URL:", this.width / 2 - CONTROL_WIDTH / 2,
            top + 25, 0xA0A0A0);
        urlField.drawTextBox();
        drawCenteredString(fontRendererObj, "Display Size:", this.width / 2, top + 66,
            0xA0A0A0);
        if (statusMessage.length() > 0) {
            drawCenteredString(fontRendererObj, statusMessage, this.width / 2, top + 133,
                0xFF8080);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private String widthButtonText() {
        return "Width: " + displayWidth;
    }

    private String heightButtonText() {
        return "Height: " + displayHeight;
    }
}
