package com.hfr.market.gui;

import com.hfr.market.ContainerMachineMarket;
import com.hfr.market.block.TileEntityMachineMarket;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiMachineMarket extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("xenofactions", "textures/gui/machine_market.png");
    private final TileEntityMachineMarket tile;

    public GuiMachineMarket(InventoryPlayer inventory, TileEntityMachineMarket tile) {
        super(new ContainerMachineMarket(inventory, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString("Machine Market", 8, 6, 4210752);
        this.fontRenderer.drawString("Selected: " + tile.getSelectedItem(), 8, 40, 4210752);
        this.fontRenderer.drawString("Price: " + tile.getSelectedItemPrice(), 8, 60, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
}