package com.hfr.inventory.gui;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.shop.PlayerTraderActionPacket;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** The screen contains previews only; every operation is resolved against server inventories. */
public final class GUIPlayerTrader extends GuiScreen {
    private NBTTagCompound data;
    private GuiTextField hotbar, quantity, storage;
    private int left, top;
    public GUIPlayerTrader(NBTTagCompound data) { this.data = data; }
    public String token() { return data.getString("token"); }
    public void receive(NBTTagCompound updated) { data = updated; }

    @Override public void initGui() {
        left = (width - 320) / 2; top = (height - 280) / 2;
        buttonList.clear();
        buttonList.add(new GuiButton(1, left + 224, top + 56, 88, 20, "Buy one"));
        if (data.getBoolean("manager")) {
            hotbar = field(left + 8, top + 198, "1");
            quantity = field(left + 55, top + 198, "1");
            storage = field(left + 102, top + 198, "1");
            buttonList.add(new GuiButton(2, left + 8, top + 222, 96, 20, "Set sale"));
            buttonList.add(new GuiButton(3, left + 112, top + 222, 96, 20, "Set payment"));
            buttonList.add(new GuiButton(4, left + 216, top + 222, 96, 20, "Deposit"));
            buttonList.add(new GuiButton(5, left + 8, top + 250, 149, 20, "Take stock"));
            buttonList.add(new GuiButton(6, left + 163, top + 250, 149, 20, "Take payments"));
        }
    }
    private GuiTextField field(int x, int y, String value) {
        GuiTextField field = new GuiTextField(fontRendererObj, x, y, 39, 18);
        field.setMaxStringLength(4); field.setText(value); return field;
    }
    private int number(GuiTextField field) {
        try { return Integer.parseInt(field.getText()); } catch (NumberFormatException e) { return -1; }
    }
    private void send(String action, int slot, int count) {
        PacketDispatcher.wrapper.sendToServer(new PlayerTraderActionPacket(token(), action, data.getLong("revision"), slot, count));
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1) send("buy", 0, 0);
        else if (button.id == 2) send("sale", number(hotbar) - 1, number(quantity));
        else if (button.id == 3) send("payment", number(hotbar) - 1, number(quantity));
        else if (button.id == 4) send("deposit", number(hotbar) - 1, 0);
        else if (button.id == 5) send("stock", number(storage) - 1, 0);
        else if (button.id == 6) send("collect", number(storage) - 1, 0);
    }
    @Override protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        if (hotbar != null) { hotbar.mouseClicked(x, y, button); quantity.mouseClicked(x, y, button); storage.mouseClicked(x, y, button); }
    }
    @Override protected void keyTyped(char key, int code) {
        if (code == 1 || code == mc.gameSettings.keyBindInventory.getKeyCode()) {
            send("close", 0, 0); mc.displayGuiScreen(null); return;
        }
        if (hotbar != null && (hotbar.textboxKeyTyped(key, code) || quantity.textboxKeyTyped(key, code)
            || storage.textboxKeyTyped(key, code))) return;
        super.keyTyped(key, code);
    }
    private ItemStack item(String key) {
        return data.hasKey(key, 10) ? ItemStack.loadItemStackFromNBT(data.getCompoundTag(key)) : null;
    }
    private ItemStack[] items(String key) {
        ItemStack[] result = new ItemStack[9];
        NBTTagList list = data.getTagList(key, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound n = list.getCompoundTagAt(i); int slot = n.getByte("Slot") & 255;
            if (slot < 9) result[slot] = ItemStack.loadItemStackFromNBT(n);
        }
        return result;
    }
    private ItemStack hover;
    private void icon(ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (stack == null) return;
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) hover = stack;
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground(); drawRect(left, top, left + 320, top + (data.getBoolean("manager") ? 280 : 125), 0xe0202020);
        drawCenteredString(fontRendererObj, "Player Trader", left + 160, top + 8, 0xffffff);
        ItemStack sale = item("sale"), payment = item("payment"); hover = null;
        RenderHelper.enableGUIStandardItemLighting();
        icon(sale, left + 25, top + 40, mouseX, mouseY);
        icon(payment, left + 25, top + 77, mouseX, mouseY);
        RenderHelper.disableStandardItemLighting();
        fontRendererObj.drawString("Sale: " + (sale == null ? "not set" : "x" + data.getInteger("saleQuantity")), left + 50, top + 44, 0xffffff);
        fontRendererObj.drawString("Payment: " + (payment == null ? "not set" : "x" + data.getInteger("paymentQuantity")), left + 50, top + 81, 0xffffff);
        fontRendererObj.drawString("Offers in stock: " + data.getInteger("available"), left + 8, top + 107, 0xffffff);
        if (data.getBoolean("manager")) {
            fontRendererObj.drawString("Stock slots 1-9", left + 8, top + 120, 0xffffff);
            fontRendererObj.drawString("Payment slots 1-9", left + 8, top + 155, 0xffffff);
            fontRendererObj.drawString("Hotbar", left + 8, top + 186, 0xffffff);
            fontRendererObj.drawString("Qty", left + 55, top + 186, 0xffffff);
            fontRendererObj.drawString("Storage", left + 102, top + 186, 0xffffff);
            hotbar.drawTextBox(); quantity.drawTextBox(); storage.drawTextBox();
            ItemStack[] stock = items("stock"), payments = items("payments"), bar = items("hotbar");
            RenderHelper.enableGUIStandardItemLighting();
            for (int i = 0; i < 9; i++) {
                int x = left + 145 + i * 19, y = top + 116;
                icon(stock[i], x, y, mouseX, mouseY);
                icon(payments[i], x, top + 151, mouseX, mouseY);
                fontRendererObj.drawString("" + (i + 1), x + 4, top + 136, 0xcccccc);
            }
            RenderHelper.disableStandardItemLighting();
            // The selected hotbar slot is previewed from the latest server snapshot.
            int slot = number(hotbar) - 1;
            if (slot >= 0 && slot < 9) {
                RenderHelper.enableGUIStandardItemLighting(); icon(bar[slot], left + 145, top + 198, mouseX, mouseY);
                RenderHelper.disableStandardItemLighting();
            }
        }
        super.drawScreen(mouseX, mouseY, partial);
        if (hover != null) renderToolTip(hover, mouseX, mouseY);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
