package com.hfr.inventory.gui;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.hfr.clowder.Clowder;
import com.hfr.config.XFConfig;
import com.hfr.inventory.container.ContainerFlag;
import com.hfr.lib.RefStrings;
import com.hfr.tileentity.clowder.TileEntityFlag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIFlag extends GuiContainer {

	public static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_flag_new.png");
	private static final int HELP_X = 10;
	private static final int HELP_Y = 9;
	private static final int HELP_SIZE = 18;
	private TileEntityFlag diFurnace;

	public GUIFlag(InventoryPlayer invPlayer, TileEntityFlag tedf) {
		super(new ContainerFlag(invPlayer, tedf));
		diFurnace = tedf;

		this.xSize = 216;
		this.ySize = 216;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {

		String name = I18n.format(diFurnace.getInventoryName());
		drawTextBackground(45, 3, 132, 14);
		this.fontRendererObj.drawString(name, 90 - this.fontRendererObj.getStringWidth(name) / 2, 6, 0xFFFFFF);

		float prestige = diFurnace.prestige;
		float prestigeReq = diFurnace.prestigeReq;
		int color = prestigeReq <= prestige ? 0x55FF55 : 0xFF5555;

		String cityName = diFurnace.name == null || diFurnace.name.isEmpty() ? "Unnamed" : diFurnace.name;
		String ownerName = diFurnace.ownerName == null || diFurnace.ownerName.isEmpty() ? "None" : diFurnace.ownerName;

		drawTextBackground(45, 16, 132, 70);
		this.fontRendererObj.drawString("City: " + this.fontRendererObj.trimStringToWidth(cityName, 92), 50, 20, 0xFFFFFF);
		this.fontRendererObj.drawString("Level: " + diFurnace.cityLevel.displayName, 50, 31, 0xFFFFFF);
		this.fontRendererObj.drawString("Radius: " + XFConfig.cityRadius(diFurnace.cityLevel) + " Upkeep: " + Clowder.round(XFConfig.cityUpkeep(diFurnace.cityLevel)), 50, 42, 0xFFFFFF);
		this.fontRendererObj.drawString("Owner: " + this.fontRendererObj.trimStringToWidth(ownerName, 82), 50, 53, 0xFFFFFF);
		this.fontRendererObj.drawString("Prestige: " + Clowder.round(prestige), 50, 64, 0xFFFFFF);
		this.fontRendererObj.drawString("Required: " + Clowder.round(prestigeReq), 50, 75, color);

		if(isHelpHovered(x, y)) {
			this.func_146283_a(Arrays.asList(new String[] {"Flag functions", "Claims are city-based", "Use /c city upgrade to level up", "Each level adds 1 chunk radius", "Capital max radius: 6 chunks", "Founding and upgrades require prestige"}), x - guiLeft, y - guiTop);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawScaledTexture(guiLeft, guiTop, xSize, ySize);
	}

	private boolean isHelpHovered(int x, int y) {
		return guiLeft + HELP_X <= x && guiLeft + HELP_X + HELP_SIZE > x && guiTop + HELP_Y <= y && guiTop + HELP_Y + HELP_SIZE > y;
	}

	private void drawTextBackground(int x, int y, int width, int height) {
		drawRect(x, y, x + width, y + height, 0x99000000);
	}

	private void drawScaledTexture(int x, int y, int width, int height) {
		float maxU = 1.0F;
		float maxV = 1.0F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + height, this.zLevel, 0.0D, maxV);
		tessellator.addVertexWithUV(x + width, y + height, this.zLevel, maxU, maxV);
		tessellator.addVertexWithUV(x + width, y, this.zLevel, maxU, 0.0D);
		tessellator.addVertexWithUV(x, y, this.zLevel, 0.0D, 0.0D);
		tessellator.draw();
	}
}
