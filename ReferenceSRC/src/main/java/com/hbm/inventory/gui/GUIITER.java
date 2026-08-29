package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerITER;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.tileentity.machine.TileEntityITER;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIITER extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/reactors/gui_fusion_multiblock.png");

	private TileEntityITER iter;

	public GUIITER(InventoryPlayer invPlayer, TileEntityITER iter) {

		super(new ContainerITER(invPlayer, iter));

		this.iter = iter;

		this.xSize = 176;
		this.ySize = 222;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {

		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(
			this,
			mouseX,
			mouseY,
			guiLeft + 71,
			guiTop + 108,
			34,
			16,
			iter.power,
			iter.maxPower
		);

		iter.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 54, 16, 52);       // legacy water / compatibility
		iter.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 54, 16, 52);      // legacy steam / compatibility
		iter.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 16, guiTop + 54, 16, 52);       // coolant
		iter.tanks[3].renderTankInfo(this, mouseX, mouseY, guiLeft + 154, guiTop + 54, 16, 52);      // hot coolant
		iter.plasma.renderTankInfo(this, mouseX, mouseY, guiLeft + 71, guiTop + 54, 34, 34);         // plasma

		String magnetText =
			"Magnets are " + (iter.areMagnetsPowered() ? "ON" : "OFF");

		String powerText =
			"Magnet draw: " + iter.getActualPowerReq() + " HE/t";

		String shieldText =
			iter.hasValidShield()
				? "Shield limit: " + iter.getShield() + "°C"
				: "No fusion shield installed";

		String tempText =
			"Plasma temp: " + iter.plasma.getTankType().temperature + "°C";

		String tempState =
			iter.isTemperatureSafe()
				? "Thermal state: stable"
				: "Thermal state: over limit";

		String coolingText =
			iter.hasCoolingAvailable()
				? "Cooling path: available"
				: "Cooling path: blocked/starved";

		this.drawCustomInfoStat(
			mouseX,
			mouseY,
			guiLeft + 76,
			guiTop + 94,
			24,
			12,
			mouseX,
			mouseY,
			new String[] {
				magnetText,
				powerText,
				shieldText,
				tempText,
				tempState,
				coolingText
			}
		);

		this.drawCustomInfoStat(
			mouseX,
			mouseY,
			guiLeft + 44,
			guiTop + 22,
			17,
			7,
			mouseX,
			mouseY,
			new String[] {
				"Breeder progress: " + iter.progress + "/" + iter.duration
			}
		);

		this.drawCustomInfoStat(
			mouseX,
			mouseY,
			guiLeft + 115,
			guiTop + 22,
			17,
			7,
			mouseX,
			mouseY,
			new String[] {
				"Heat stress: " + iter.getHeatStressScaled(100) + "%"
			}
		);
	}

	@Override
	protected void mouseClicked(int x, int y, int i) {

		super.mouseClicked(x, y, i);

		if(guiLeft + 52 <= x && guiLeft + 52 + 18 > x && guiTop + 107 < y && guiTop + 107 + 18 >= y) {

			mc.getSoundHandler().playSound(
				PositionedSoundRecord.func_147674_a(
					new ResourceLocation("gui.button.press"),
					1.0F
				)
			);

			PacketDispatcher.wrapper.sendToServer(
				new AuxButtonPacket(iter.xCoord, iter.yCoord, iter.zCoord, 0, 0)
			);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {

		String name = this.iter.hasCustomInventoryName()
			? this.iter.getInventoryName()
			: I18n.format(this.iter.getInventoryName());

		this.fontRendererObj.drawString(
			name,
			this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2,
			6,
			4210752
		);

		this.fontRendererObj.drawString(
			I18n.format("container.inventory"),
			8,
			this.ySize - 96 + 2,
			4210752
		);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(iter.isOn) {
			drawTexturedModalRect(guiLeft + 52, guiTop + 107, 176, 0, 18, 18);
		}

		/*
		 * This now matches server logic.
		 * Old GUI used iter.power >= iter.powerReq, which ignored shield power drain.
		 */
		if(iter.areMagnetsPowered()) {
			drawTexturedModalRect(guiLeft + 76, guiTop + 94, 194, 0, 24, 12);
		}

		/*
		 * Shield/temperature status indicator.
		 */
		if(iter.hasValidShield() && iter.isTemperatureSafe()) {
			drawTexturedModalRect(guiLeft + 97, guiTop + 17, 218, 0, 18, 18);
		}

		int power = (int) iter.getPowerScaled(34);
		drawTexturedModalRect(guiLeft + 71, guiTop + 108, 176, 25, power, 16);

		int breeder = (int) iter.getProgressScaled(17);
		drawTexturedModalRect(guiLeft + 44, guiTop + 22, 176, 18, breeder, 7);

		/*
		 * New heat stress bar.
		 * This uses the same small 17x7 style as the breeder bar.
		 *
		 */
		int stress = (int) iter.getHeatStressScaled(17);
		drawTexturedModalRect(guiLeft + 115, guiTop + 22, 176, 18, stress, 7);

		for(int t = 0; t < 2; t++) {
			iter.tanks[t].renderTank(guiLeft + 26 + 108 * t, guiTop + 106, this.zLevel, 16, 52);
		}

		iter.tanks[2].renderTank(guiLeft + 16, guiTop + 86, this.zLevel, 6, 32);
		iter.tanks[3].renderTank(guiLeft + 154, guiTop + 86, this.zLevel, 6, 32);

		iter.plasma.renderTank(guiLeft + 71, guiTop + 88, this.zLevel, 34, 34);
	}
}
