package com.hfr.inventory.gui;

import com.hfr.data.CustomImageStorage;
import com.hfr.packet.PacketAddImage;
import com.hfr.packet.PacketDispatcher;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

//import java.io.IOException;

public class GuiCustomImageAdd extends GuiScreen {
    private GuiTextField urlField;
    private final String imageName;

    public GuiCustomImageAdd(String name) {
        this.imageName = name;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.urlField = new GuiTextField(this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 10, 200, 20);
        this.urlField.setFocused(true);
        this.urlField.setMaxStringLength(8192); // allow long URLs

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + 20, 200, 20, "Save"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            String url = urlField.getText();
            if (!url.isEmpty()) {
                // send packet to server
                PacketDispatcher.wrapper.sendToServer(new PacketAddImage(imageName, url));
            }
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        this.urlField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Enter Image URL for: " + imageName, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        this.urlField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

