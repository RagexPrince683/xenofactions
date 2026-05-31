package com.hfr.inventory.gui;

import org.lwjgl.input.Keyboard;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.CityRenamePacket;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GUICityRename extends GuiScreen {

	private final int x;
	private final int y;
	private final int z;
	private final String currentName;
	private GuiTextField nameField;

	public GUICityRename(int x, int y, int z, String currentName) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.currentName = currentName == null ? "" : currentName;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.nameField = new GuiTextField(this.fontRendererObj, this.width / 2 - 100, this.height / 2 - 10, 200, 20);
		this.nameField.setFocused(true);
		this.nameField.setMaxStringLength(32);
		this.nameField.setText(currentName);
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + 20, 98, 20, "Rename"));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 2, this.height / 2 + 20, 98, 20, "Keep Name"));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			sendRename();
		} else if(button.id == 1) {
			this.mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if(keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
			sendRename();
			return;
		}
		this.nameField.textboxKeyTyped(typedChar, keyCode);
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.nameField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, "Rename Captured City", this.width / 2, this.height / 2 - 42, 0xFFFFFF);
		this.drawCenteredString(this.fontRendererObj, "Choose a new name for this City Center.", this.width / 2, this.height / 2 - 28, 0xAAAAAA);
		this.nameField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void sendRename() {
		String newName = this.nameField.getText() == null ? "" : this.nameField.getText().trim();
		if(!newName.isEmpty())
			PacketDispatcher.wrapper.sendToServer(new CityRenamePacket(x, y, z, newName));
		this.mc.displayGuiScreen(null);
	}
}
