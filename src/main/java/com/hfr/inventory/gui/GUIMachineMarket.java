package com.hfr.inventory.gui;

import com.hfr.data.MarketData;
import com.hfr.lib.RefStrings;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.shop.XShopActionPacket;
import java.util.List;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Existing XShop trading presentation, now fed exclusively by server pages and sessions. */
public final class GUIMachineMarket extends GuiScreen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_shop.png");
    private NBTTagCompound data;
    private List<ItemStack[]> offers;
    private int left, top;
    public GUIMachineMarket(NBTTagCompound data) { this.data = data; offers = MarketData.offersFromNBT(data); }
    public String token() { return data.getString("token"); }
    public void receive(NBTTagCompound data) { this.data = data; offers = MarketData.offersFromNBT(data); initGui(); }
    @Override public void initGui() {
        left = (width - 176) / 2; top = (height - 230) / 2 + 12; buttonList.clear();
        GuiButton previous = new GuiButton(1, left + 25, top + 7, 18, 18, "<"); previous.enabled = data.getInteger("page") > 0;
        GuiButton next = new GuiButton(2, left + 132, top + 7, 18, 18, ">"); next.enabled = data.getInteger("page") + 1 < data.getInteger("pages");
        buttonList.add(previous); buttonList.add(next);
        for (int i = 0; i < offers.size(); i++) {
            GuiButton buy = new GuiButton(10 + i, left + 133, top + 34 + i * 27, 18, 18, "+");
            buy.enabled = data.getBoolean("canTrade") && offers.get(i)[0] != null; buttonList.add(buy);
        }
        boolean navigation = data.getBoolean("configure") || data.getBoolean("back");
        if (navigation) buttonList.add(new GuiButton(3, left, top + 198, 85, 20, data.getBoolean("configure") ? "Configure" : "Back"));
        buttonList.add(new GuiButton(4, left + (navigation ? 91 : 0), top + 198, navigation ? 85 : 176, 20, "Close"));
    }
    private void send(String action, int value) {
        PacketDispatcher.wrapper.sendToServer(new XShopActionPacket(token(), data.getLong("revision"), action, "", "", value));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1 || button.id == 2) send("page", data.getInteger("page") + (button.id == 1 ? -1 : 1));
        else if (button.id == 3) send(data.getBoolean("configure") ? "configure" : "back", 0);
        else if (button.id == 4) close();
        else if (button.id >= 10) send("buy", data.getInteger("page") * 6 + button.id - 10);
    }
    private void close() { send("close", 0); mc.displayGuiScreen(null); }
    @Override protected void keyTyped(char character, int key) {
        if (key == 1 || key == mc.gameSettings.keyBindInventory.getKeyCode()) close();
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground(); GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(TEXTURE); drawTexturedModalRect(left, top, 0, 0, 176, 194);
        String title = data.getBoolean("canTrade") ? (data.getInteger("page") + 1) + "/" + data.getInteger("pages") : "Unavailable";
        drawCenteredString(fontRendererObj, title, left + 88, top + 10, 0xffffff);
        drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(data.getString("name"), 176), left + 88, top - 12, 0xffffff);
        ItemStack hovered = null;
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < offers.size(); i++) {
            ItemStack[] row = offers.get(i); int y = top + 35 + i * 27;
            if (row[0] == null) { fontRendererObj.drawString("Unavailable", left + 27, y + 5, 0x777777); continue; }
            for (int j = 0; j < 4; j++) {
                if (row[j] == null) continue;
                int x = j == 0 ? left + 98 : left + 8 + 18 * j;
                itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), row[j], x, y);
                itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), row[j], x, y);
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) hovered = row[j];
            }
            fontRendererObj.drawString("#" + (data.getInteger("page") * 6 + i), left + 6, y + 5, 0x404040);
        }
        RenderHelper.disableStandardItemLighting(); super.drawScreen(mouseX, mouseY, partial);
        if (hovered != null) renderToolTip(hovered, mouseX, mouseY);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
