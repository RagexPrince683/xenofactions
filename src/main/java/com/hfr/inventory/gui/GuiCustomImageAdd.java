package com.hfr.inventory.gui;

import com.hfr.data.CustomImageStorage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

//import java.io.IOException;

public class GuiCustomImageAdd extends GuiScreen {
    private GuiTextField nameField;
    private GuiTextField urlField;
    private final EntityPlayer player;

    public GuiCustomImageAdd(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int mid = this.width / 2;
        int y = this.height / 2 - 30;

        this.nameField = new GuiTextField(this.fontRendererObj, mid - 100, y, 200, 20);
        this.urlField  = new GuiTextField(this.fontRendererObj, mid - 100, y + 30, 200, 20);

        this.buttonList.add(new GuiButton(0, mid - 100, y + 60, 200, 20, "Save"));
        this.buttonList.add(new GuiButton(1, mid - 100, y + 90, 200, 20, "Cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            String name = nameField.getText();
            String url = urlField.getText();

            if (name.isEmpty() || url.isEmpty()) {
                player.addChatMessage(new ChatComponentText("Name and URL required."));
            } else {
                CustomImageStorage storage = CustomImageStorage.get(player.worldObj);
                boolean ok = storage.addImage(player.getUniqueID(), name, url);
                player.addChatMessage(new ChatComponentText(ok ? "Image added." : "Add failed: max 5 images."));
            }
            this.mc.displayGuiScreen(null);
        }
        if (button.id == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.nameField.drawTextBox();
        this.urlField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!this.nameField.textboxKeyTyped(typedChar, keyCode) &&
                !this.urlField.textboxKeyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        this.urlField.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
