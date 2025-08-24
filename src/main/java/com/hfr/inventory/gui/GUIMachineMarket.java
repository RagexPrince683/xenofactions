package com.hfr.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hfr.blocks.machine.MachineMarket;
import com.hfr.blocks.machine.MachineMarket.TileEntityMarket;
import com.hfr.lib.RefStrings;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.AuxButtonPacket;
import com.hfr.tileentity.machine.TileEntityMachineEMP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GUIMachineMarket extends GuiScreen {

	public static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_shop.png");
	private final EntityPlayer player;
	protected int guiLeft;
	protected int guiTop;
	protected int xSize = 176;
	protected int ySize = 194;
	public static List<ItemStack[]> offers = new ArrayList();
	int page;
	TileEntityMarket market;

	// fields
	private boolean requestSent = false;


	// call to request server
	private void sendRequestIfNeeded() {
		if (market == null) {
			System.out.println("[GUIMachineMarket] market is null when trying to request");
			return;
		}
		if (requestSent) {
			// already requested and waiting for reply
			return;
		}
		requestSent = true;
		PacketDispatcher.wrapper.sendToServer(new com.hfr.packet.tile.OfferPacket(market.xCoord, market.yCoord, market.zCoord, market.name));
		System.out.println("[GUIMachineMarket] Sent request OfferPacket for market='" + market.name + "' coords=("
				+ market.xCoord + "," + market.yCoord + "," + market.zCoord + ")");
	}

	// called by client handler when a reply arrives
	public void onOffersReceived() {
		requestSent = false;
		refreshOffers();
	}

	public GUIMachineMarket(EntityPlayer player, TileEntityMarket market) {

		this.market = market;
		this.player = player;
	}

	/**
	 * Keep GUI page bounds valid after offers change.
	 * Call this after GUIMachineMarket.offers is replaced.
	 */
	public void refreshOffers() {
		if (offers == null) offers = new ArrayList<ItemStack[]>();
		// clamp page to available pages
		int maxPage = Math.max(1, (offers.size() + 5) / 6);
		if (page < 1) page = 1;
		if (page > maxPage) page = maxPage;
		// force a simple client redraw next tick by resetting the mouse-over cached value
		this.last = null;
	}


	// OLD
	//public void refreshOffers() {
	//	// Called when offers data changes.
	//	// Keep page within bounds and reinitialize whatever UI state depends on 'offers'.
	//	if (offers == null) offers = new ArrayList<ItemStack[]>();
	//	if (page < 1) page = 1;
	//	int maxPage = Math.max(1, (int)Math.ceil((double)offers.size() / 6.0));
	//	if (page > maxPage) page = maxPage;
//
	//	// If your GUI builds slot lists or widgets at init, call that here.
	//	// The quick & dirty approach is to call initGui() which recalculates positions:
	//	// (safe in 1.7.10 for small GUIs)
	//	this.initGui();
	//}

	// modify initGui() to clear offers and request server if needed:
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		page = 1;

		// keep client side offers empty until server replies
		// DO NOT overwrite server-sent data if already filled
		if (offers == null || offers.isEmpty()) {
			offers = new ArrayList<ItemStack[]>();
		}

		// Try to request fresh data from server if we have a market tile reference
		try {
			if (market != null) {
				try {
					// send coords + (maybe-empty) name so server can fallback to coords
					// inside initGui(), replace the PacketDispatcher.wrapper.sendToServer(...) block with:
					sendRequestIfNeeded();

					System.out.println("[GUIMachineMarket] Sent request OfferPacket for market='" + market.name + "' coords=("
							+ market.xCoord + "," + market.yCoord + "," + market.zCoord + ")");
				} catch (Exception e) {
					System.err.println("[GUIMachineMarket] Exception while sending OfferPacket request:");
					e.printStackTrace();
				}
			} else {
				System.out.println("[GUIMachineMarket] market is null on client when initGui()");
			}
		} catch (Exception e) {
			System.err.println("[GUIMachineMarket] Exception while sending OfferPacket request:");
			e.printStackTrace();
		}
	}


	protected void mouseClicked(int x, int y, int i) {

		if(guiLeft + 25 <= x && guiLeft + 25 + 18 > x && guiTop + 7 < y && guiTop + 7 + 18 >= y) {

			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			if(page > 1) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
				page--;
			}
			return;
		}

		if(guiLeft + 113 + 18 <= x && guiLeft + 113 + 36 > x && guiTop + 7 < y && guiTop + 7 + 18 >= y) {

			if(page < pagecount()) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
				page++;
			}
			return;
		}

		for(int j = 0; j < 6; j++) {
			if(guiLeft + 133 <= x && guiLeft + 133 + 18 > x && guiTop + 34 + 27 * j < y && guiTop + 34 + 27 * j + 18 >= y) {

				ItemStack[] offer = getOffer(j);

				if(offer != null) {
					PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(market.xCoord, market.yCoord, market.zCoord, (page - 1) * 6 + j, 999));
				}

				return;
			}
		}
	}

	public ItemStack[] getOffer(int index) {

		int i = (page - 1) * 6 + index;

		if(i < offers.size()) {

			ItemStack[] offer = offers.get(i);

			return offer;
		}

		return null;
	}

	public void drawScreen(int mouseX, int mouseY, float f)
	{
		this.drawDefaultBackground();
		this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		GL11.glDisable(GL11.GL_LIGHTING);
		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
		GL11.glEnable(GL11.GL_LIGHTING);

		if(market == null || market.isInvalid())
			this.mc.thePlayer.closeScreen();
	}

	protected void drawGuiContainerForegroundLayer(int i, int j) {

		String s = MachineMarket.name + " " + page + "/" + pagecount();
		this.fontRendererObj.drawString(s, guiLeft + this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, guiTop + 10, 4210752);

		for(int k = 0; k < 6; k++) {

			ItemStack[] offer = getOffer(k);

			if(offer != null) {
				int index = offers.indexOf(offer);

				this.fontRendererObj.drawString("#" + index, guiLeft + 6, guiTop + 40 + 27 * k, 4210752);
			}
		}

		if(last != null)
			this.renderToolTip(last, i, j);
	}

	ItemStack last = null;

	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		last = null;

		short short1 = 240;
		short short2 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)short1 / 1.0F, (float)short2 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		for(int k = 0; k < 6; k++) {

			ItemStack[] offer = getOffer(k);

			if(offer != null) {

				for(int l = 1; l < 4; l++) {

					if(offer[l] != null) {

						int posX = guiLeft + 8 + 18 * l;
						int posY = guiTop + 35 + 27 * k;

						if(posX < x && posX + 16 > x && posY < y && posY + 16 > y)
							last = offer[l];

						RenderHelper.disableStandardItemLighting();
						itemRender.renderItemAndEffectIntoGUI(fontRendererObj, this.mc.getTextureManager(), offer[l], posX, posY);
						itemRender.renderItemOverlayIntoGUI(fontRendererObj, this.mc.getTextureManager(), offer[l], posX, posY);
					}
				}

				int posX = guiLeft + 98;
				int posY = guiTop + 35 + 27 * k;

				RenderHelper.disableStandardItemLighting();
				itemRender.renderItemAndEffectIntoGUI(fontRendererObj, this.mc.getTextureManager(), offer[0], posX, posY);
				itemRender.renderItemOverlayIntoGUI(fontRendererObj, this.mc.getTextureManager(), offer[0], posX, posY);

				if(posX < x && posX + 16 > x && posY < y && posY + 16 > y)
					last = offer[0];
			}
		}
	}

	public int pagecount() {
		return (int)Math.ceil((double) offers.size() / 6D);
	}

	protected void keyTyped(char p_73869_1_, int p_73869_2_)
	{
		if (p_73869_2_ == 1 || p_73869_2_ == this.mc.gameSettings.keyBindInventory.getKeyCode())
		{
			this.mc.thePlayer.closeScreen();
		}
	}

	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
