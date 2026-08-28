package com.hfr.inventory.gui;

import com.hfr.main.EventHandlerClient;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.TDMKitSelectPacket;
import com.hfr.tdm.TDMManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Mouse;

public class GUITDMKitSelect extends GuiScreen {

    private static final int SLOT_SIZE = 18;
    private static final int MAIN_COLUMN_COUNT = 9;
    private static final int INVENTORY_SLOT_COUNT = 40;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final String team;
    private final String[] kitNames;
    private final int[] costs;
    private final ItemStack[][] kitPreviews;
    private final boolean economy;
    private final boolean buying;
    private final boolean mandatory;
    private final int balance;
    private final int seconds;
    private final long buyEndMillis;

    private int firstVisibleKit;
    private int visibleKitCount;
    private int selectedPreviewKit;
    private int listX;
    private int listWidth;
    private int contentX;
    private int contentY;
    private boolean awaitingSelectionResult;

    public GUITDMKitSelect(String team, String[] names, int[] costs,
            ItemStack[][] kitPreviews, boolean economy, int balance, int seconds,
            boolean buying, boolean mandatory) {
        this.team = team;
        this.kitNames = names;
        this.costs = costs;
        this.kitPreviews = kitPreviews;
        this.economy = economy;
        this.balance = balance;
        this.seconds = seconds;
        this.buying = buying;
        this.mandatory = mandatory;
        this.buyEndMillis = System.currentTimeMillis() + seconds * 1000L;
    }

    @Override
    public void initGui() {
        listWidth = Math.max(104, Math.min(180, width - 208));
        int totalWidth = listWidth + 8 + 188;
        listX = Math.max(4, (width - totalWidth) / 2);
        contentX = Math.min(width - 190, listX + listWidth + 8);
        contentY = 42;

        int availableButtonHeight = Math.max(BUTTON_HEIGHT, height - contentY - 12);
        visibleKitCount = Math.max(1, availableButtonHeight / BUTTON_SPACING);
        visibleKitCount = Math.min(visibleKitCount, kitNames.length);
        clampScroll();
        rebuildButtons();
    }

    private void rebuildButtons() {
        buttonList.clear();
        for (int row = 0; row < visibleKitCount; row++) {
            int kitIndex = firstVisibleKit + row;
            GuiButton button = new GuiButton(
                    kitIndex,
                    listX,
                    contentY + row * BUTTON_SPACING,
                    listWidth,
                    BUTTON_HEIGHT,
                    getButtonLabel(kitIndex));
            button.enabled = !awaitingSelectionResult && canAfford(kitIndex);
            buttonList.add(button);
        }
    }

    private String getButtonLabel(int kitIndex) {
        String price = costs[kitIndex] == 0 ? "FREE" : Integer.toString(costs[kitIndex]);
        String label = kitNames[kitIndex] + " - " + price;
        if (economy && !canAfford(kitIndex)) {
            label += " (Cannot afford)";
        }
        return fontRendererObj.trimStringToWidth(label, listWidth - 8);
    }

    private boolean canAfford(int kitIndex) {
        return !economy || costs[kitIndex] <= balance;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (awaitingSelectionResult || !button.enabled) {
            return;
        }
        if (buying || mandatory) {
            awaitingSelectionResult = true;
            setButtonsEnabled(false);
        }
        PacketDispatcher.wrapper.sendToServer(new TDMKitSelectPacket(button.id));
        if (!buying && !mandatory) {
            mc.displayGuiScreen(null);
        }
    }

    public void receiveSelectionResult(TDMManager.KitSelectionResult result) {
        if (result == TDMManager.KitSelectionResult.SUCCESS) {
            awaitingSelectionResult = true;
            EventHandlerClient.clearMandatoryKitGui(true);
            return;
        }

        awaitingSelectionResult = false;
        rebuildButtons();
        String message;
        if (result == TDMManager.KitSelectionResult.INSUFFICIENT_FUNDS) {
            message = "You cannot afford that kit.";
        } else if (result == TDMManager.KitSelectionResult.INVALID_SELECTION) {
            message = "That kit is no longer available.";
        } else if (result == TDMManager.KitSelectionResult.ALREADY_SELECTED) {
            message = "You already selected a kit for this round.";
        } else {
            message = "Kit selection is no longer active.";
        }
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        for (Object entry : buttonList) {
            GuiButton button = (GuiButton) entry;
            button.enabled = enabled && canAfford(button.id);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheelMovement = Mouse.getEventDWheel();
        if (wheelMovement == 0 || kitNames.length <= visibleKitCount) {
            return;
        }

        firstVisibleKit += wheelMovement < 0 ? 1 : -1;
        clampScroll();
        rebuildButtons();
    }

    private void clampScroll() {
        int maximumFirstKit = Math.max(0, kitNames.length - visibleKitCount);
        firstVisibleKit = Math.max(0, Math.min(firstVisibleKit, maximumFirstKit));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (mandatory && keyCode == 1) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        updateHoveredKit(mouseX, mouseY);
        drawHeader();
        drawKitContents();
        drawScrollHints();
        super.drawScreen(mouseX, mouseY, partialTicks);

        ItemStack hoveredStack = getHoveredPreviewStack(mouseX, mouseY);
        if (hoveredStack != null) {
            renderToolTip(hoveredStack, mouseX, mouseY);
        }
    }

    private void updateHoveredKit(int mouseX, int mouseY) {
        for (Object entry : buttonList) {
            GuiButton button = (GuiButton) entry;
            if (mouseX >= button.xPosition && mouseX < button.xPosition + button.width
                    && mouseY >= button.yPosition && mouseY < button.yPosition + button.height) {
                selectedPreviewKit = button.id;
                return;
            }
        }
    }

    private void drawHeader() {
        String title = EnumChatFormatting.BOLD + "Select a " + team + " TDM Kit";
        drawCenteredString(fontRendererObj, title, width / 2, 10, 0xFFFFFF);

        int remaining = buying
                ? (int) Math.max(0L, (buyEndMillis - System.currentTimeMillis() + 999L) / 1000L)
                : seconds;
        String status = buying
                ? "Buy time: " + remaining + "s" + (economy ? "  Balance: " + balance : "")
                : "You have Resistance and Regeneration until you pick one."
                        + (economy ? "  Balance: " + balance : "");
        drawCenteredString(fontRendererObj, status, width / 2, 24, 0xA0A0A0);
    }

    private void drawScrollHints() {
        if (firstVisibleKit > 0) {
            drawCenteredString(fontRendererObj, "^ more kits ^", listX + listWidth / 2,
                    contentY - 10, 0xC0C0C0);
        }
        if (firstVisibleKit + visibleKitCount < kitNames.length) {
            int hintY = contentY + visibleKitCount * BUTTON_SPACING - 2;
            drawCenteredString(fontRendererObj, "v scroll for more v", listX + listWidth / 2,
                    Math.min(height - 10, hintY), 0xC0C0C0);
        }
    }

    private void drawKitContents() {
        int panelRight = contentX + 188;
        int panelBottom = contentY + 118;
        drawGradientRect(contentX, contentY, panelRight, panelBottom, 0xE0101010, 0xE0202020);

        int kitIndex = Math.max(0, Math.min(selectedPreviewKit, kitNames.length - 1));
        String price = costs[kitIndex] == 0 ? "FREE" : Integer.toString(costs[kitIndex]);
        String heading = kitNames[kitIndex] + " - " + price;
        drawCenteredString(fontRendererObj,
                fontRendererObj.trimStringToWidth(heading, 180),
                contentX + 94,
                contentY + 5,
                canAfford(kitIndex) ? 0xFFFFFF : 0xFF6060);

        ItemStack[] preview = kitPreviews[kitIndex];
        drawMainInventory(preview);
        drawArmorInventory(preview);

        if (!containsAnyItem(preview)) {
            drawCenteredString(fontRendererObj, "This kit contains no items.",
                    contentX + 94, contentY + 101, 0xB0B0B0);
        }
    }

    private void drawMainInventory(ItemStack[] preview) {
        for (int inventoryRow = 0; inventoryRow < 3; inventoryRow++) {
            for (int column = 0; column < MAIN_COLUMN_COUNT; column++) {
                int slot = 9 + inventoryRow * MAIN_COLUMN_COUNT + column;
                drawPreviewSlot(preview[slot], contentX + 2 + column * SLOT_SIZE,
                        contentY + 20 + inventoryRow * SLOT_SIZE, false);
            }
        }

        for (int column = 0; column < MAIN_COLUMN_COUNT; column++) {
            drawPreviewSlot(preview[column], contentX + 2 + column * SLOT_SIZE,
                    contentY + 78, true);
        }
    }

    private void drawArmorInventory(ItemStack[] preview) {
        for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
            drawPreviewSlot(preview[36 + armorIndex], contentX + 168,
                    contentY + 20 + (3 - armorIndex) * SLOT_SIZE, false);
        }
        drawCenteredString(fontRendererObj, "Armor", contentX + 177, contentY + 95, 0x909090);
    }

    private void drawPreviewSlot(ItemStack stack, int x, int y, boolean hotbar) {
        int borderColor = hotbar ? 0xFFD080 : 0x707070;
        drawRect(x, y, x + SLOT_SIZE, y + SLOT_SIZE, borderColor);
        drawRect(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0x40202020);
        if (stack != null) {
            itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x + 1, y + 1);
            itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x + 1, y + 1);
        }
    }

    private ItemStack getHoveredPreviewStack(int mouseX, int mouseY) {
        int kitIndex = Math.max(0, Math.min(selectedPreviewKit, kitPreviews.length - 1));
        ItemStack[] preview = kitPreviews[kitIndex];

        for (int inventoryRow = 0; inventoryRow < 3; inventoryRow++) {
            for (int column = 0; column < MAIN_COLUMN_COUNT; column++) {
                int slot = 9 + inventoryRow * MAIN_COLUMN_COUNT + column;
                if (isInsideSlot(mouseX, mouseY, contentX + 2 + column * SLOT_SIZE,
                        contentY + 20 + inventoryRow * SLOT_SIZE)) {
                    return preview[slot];
                }
            }
        }
        for (int column = 0; column < MAIN_COLUMN_COUNT; column++) {
            if (isInsideSlot(mouseX, mouseY, contentX + 2 + column * SLOT_SIZE, contentY + 78)) {
                return preview[column];
            }
        }
        for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
            if (isInsideSlot(mouseX, mouseY, contentX + 168,
                    contentY + 20 + (3 - armorIndex) * SLOT_SIZE)) {
                return preview[36 + armorIndex];
            }
        }
        return null;
    }

    private boolean isInsideSlot(int mouseX, int mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE
                && mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }

    private boolean containsAnyItem(ItemStack[] preview) {
        for (int slot = 0; slot < Math.min(preview.length, INVENTORY_SLOT_COUNT); slot++) {
            if (preview[slot] != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
