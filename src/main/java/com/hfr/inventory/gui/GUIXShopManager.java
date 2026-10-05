package com.hfr.inventory.gui;

import com.hfr.data.MarketData;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.shop.XShopActionPacket;
import java.util.List;
import net.minecraft.client.gui.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import org.lwjgl.input.Keyboard;

/** Paginated server catalog, block configuration panel, and administrator offer editor. */
public final class GUIXShopManager extends GuiScreen {
    private NBTTagCompound data;
    private GuiTextField text;
    private List<ItemStack[]> offers;
    private int left, top;
    public GUIXShopManager(NBTTagCompound data) { this.data = data; offers = MarketData.offersFromNBT(data); }
    public String token() { return data.getString("token"); }
    public void receive(NBTTagCompound data) { this.data = data; offers = MarketData.offersFromNBT(data); initGui(); }
    private boolean editor() { return data.getString("mode").equals("edit"); }
    private boolean admin() { return data.getString("mode").equals("admin"); }
    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true); buttonList.clear(); left = (width - 310) / 2; top = (height - 240) / 2;
        text = new GuiTextField(fontRendererObj, left + 5, top + (editor() ? 60 : 43), 224, 18);
        text.setMaxStringLength(80); text.setText(data.getString(editor() ? "name" : "filter"));
        buttonList.add(new GuiButton(1, left + 235, text.yPosition - 1, 70, 20, editor() ? "Rename" : "Search"));
        if (editor()) {
            buttonList.add(new GuiButton(2, left + 5, top + 36, 98, 18, "Enabled: " + data.getBoolean("enabled")));
            buttonList.add(new GuiButton(3, left + 107, top + 36, 98, 18, "Market: " + data.getBoolean("visible")));
            buttonList.add(new GuiButton(4, left + 209, top + 36, 98, 18, "Admin: " + data.getBoolean("restricted")));
            for (int i = 0; i < offers.size(); i++) buttonList.add(new GuiButton(10 + i, left + 235, top + 86 + i * 20, 70, 18, "Remove"));
            buttonList.add(new GuiButton(5, left + 5, top + 204, 198, 18, "Add offer from hotbar slots 1-4"));
            buttonList.add(new GuiButton(6, left + 209, top + 204, 98, 18, "Back"));
        } else {
            NBTTagList shops = data.getTagList("shops", 10);
            for (int i = 0; i < shops.tagCount(); i++) {
                String name = fontRendererObj.trimStringToWidth(shops.getCompoundTagAt(i).getString("name"), admin() ? 212 : 282);
                buttonList.add(new GuiButton(10 + i, left + 5, top + 70 + i * 22, admin() ? 224 : 300, 20, name));
                if (admin()) buttonList.add(new GuiButton(20 + i, left + 235, top + 70 + i * 22, 70, 20, "Edit"));
            }
            if (admin() && data.getBoolean("block")) {
                buttonList.add(new GuiButton(7, left + 5, top + 204, 98, 18, "Unlink block"));
                buttonList.add(new GuiButton(8, left + 107, top + 204, 98, 18, "Open shop"));
            }
        }
        GuiButton previous = new GuiButton(30, left + 5, top + 224, 75, 16, "<");
        GuiButton next = new GuiButton(31, left + 235, top + 224, 75, 16, ">");
        previous.enabled = data.getInteger("page") > 0; next.enabled = data.getInteger("page") + 1 < data.getInteger("pages");
        buttonList.add(previous); buttonList.add(next);
    }
    private void send(String action, String id, String value, int number) {
        PacketDispatcher.wrapper.sendToServer(new XShopActionPacket(token(), data.getLong("revision"), action, id, value, number));
    }
    @Override protected void actionPerformed(GuiButton button) {
        int id = button.id;
        if (id == 30 || id == 31) send("page", "", "", data.getInteger("page") + (id == 30 ? -1 : 1));
        else if (id == 1) send(editor() ? "rename" : "search", "", text.getText(), 0);
        else if (editor()) {
            if (id == 2) send("enabled", "", "", 0);
            else if (id == 3) send("visible", "", "", 0);
            else if (id == 4) send("restricted", "", "", 0);
            else if (id == 5) send("add", "", "", 0);
            else if (id == 6) send("back", "", "", 0);
            else if (id >= 10 && id < 16) send("remove", "", "", data.getInteger("page") * 6 + id - 10);
        } else if (id == 7) send("unlink", "", "", 0);
        else if (id == 8) send("view", "", "", 0);
        else if (id >= 10 && id < 26) {
            boolean editing = id >= 20; int row = id - (editing ? 20 : 10);
            String shop = data.getTagList("shops", 10).getCompoundTagAt(row).getString("id");
            send(editing || admin() && !data.getBoolean("block") ? "edit" : admin() ? "link" : "select", shop, "", 0);
        }
    }
    @Override protected void mouseClicked(int x, int y, int button) { super.mouseClicked(x, y, button); text.mouseClicked(x, y, button); }
    @Override protected void keyTyped(char character, int key) {
        if (key == 1) { send("close", "", "", 0); mc.displayGuiScreen(null); }
        else if (key == 28) send(editor() ? "rename" : "search", "", text.getText(), 0);
        else text.textboxKeyTyped(character, key);
    }
    @Override public void updateScreen() { text.updateCursorCounter(); }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override public void drawScreen(int x, int y, float partial) {
        drawDefaultBackground(); drawRect(left, top, left + 310, top + 240, 0xe0202020);
        drawCenteredString(fontRendererObj, editor() ? "Edit XShop" : admin() ? data.getBoolean("block") ? "Configure XShop block" : "Configured XShops" : "Faction Global Market", left + 155, top + 3, 0xffffff);
        String subtitle = editor() ? data.getString("name") : admin() && data.getBoolean("block") ? "Linked: " + data.getString("linked") : "Choose a shop";
        drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(subtitle, 300), left + 155, top + 20, 0xcccccc);
        if (editor()) {
            for (int i = 0; i < offers.size(); i++) {
                ItemStack[] offer = offers.get(i);
                String line = "#" + (data.getInteger("page") * 6 + i) + " ";
                if (offer[0] == null) line += "Unavailable item";
                else {
                    line += offer[0].stackSize + "x " + offer[0].getDisplayName() + " for ";
                    for (int j = 1; j < 4; j++) if (offer[j] != null) line += offer[j].stackSize + "x " + offer[j].getDisplayName() + " ";
                }
                fontRendererObj.drawString(fontRendererObj.trimStringToWidth(line, 220), left + 5, top + 92 + i * 20, 0xffffff);
            }
        }
        text.drawTextBox();
        drawCenteredString(fontRendererObj, (data.getInteger("page") + 1) + "/" + data.getInteger("pages"), left + 155, top + 228, 0xffffff);
        super.drawScreen(x, y, partial);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
