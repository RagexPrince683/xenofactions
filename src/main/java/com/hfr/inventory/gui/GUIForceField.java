package com.hfr.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hfr.inventory.container.ContainerForceField;
import com.hfr.lib.RefStrings;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.AuxButtonPacket;
import com.hfr.tileentity.TileEntityForceField;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIForceField extends GuiContainer {

	public static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_field.png");
	private TileEntityForceField diFurnace;
	
	public GUIForceField(InventoryPlayer invPlayer, TileEntityForceField tedf) {
		super(new ContainerForceField(invPlayer, tedf));
		diFurnace = tedf;

		this.xSize = 176;
		this.ySize = 168;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.diFurnace.hasCustomInventoryName() ? this.diFurnace.getInventoryName() : I18n.format(this.diFurnace.getInventoryName());
		
		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

    protected void mouseClicked(int x, int y, int i) {
    	super.mouseClicked(x, y, i);
		
    	if(guiLeft + 142 <= x && guiLeft + 142 + 18 > x && guiTop + 34 < y && guiTop + 34 + 18 >= y) {

			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(diFurnace.xCoord, diFurnace.yCoord, diFurnace.zCoord, 0, 0));
    	}
    }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int i = (int)diFurnace.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 8, guiTop + 69 - i, 176, 52 - i, 16, i);
		
		int j = diFurnace.getHealthScaled(52);
		drawTexturedModalRect(guiLeft + 62, guiTop + 69 - j, 192, 52 - j, 16, j);
		
		if(diFurnace.isOn) {
			drawTexturedModalRect(guiLeft + 142, guiTop + 34, 176, 52, 18, 18);
		}
	}
}
