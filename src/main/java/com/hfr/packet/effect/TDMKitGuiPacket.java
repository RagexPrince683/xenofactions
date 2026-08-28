package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUITDMKitSelect;
import com.hfr.main.EventHandlerClient;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class TDMKitGuiPacket implements IMessage {

    private static final int MAX_KITS = 128;
    private static final int INVENTORY_SLOT_COUNT = 40;
    private static final int MAX_NAME_LENGTH = 256;
    private static final int MAX_TEAM_LENGTH = 32;

    private String team = "";
    private String[] kitNames = new String[0];
    private int[] costs = new int[0];
    private ItemStack[][] kitPreviews = new ItemStack[0][INVENTORY_SLOT_COUNT];
    private boolean economy;
    private boolean buying;
    private boolean mandatory;
    private int balance;
    private int seconds;

    public TDMKitGuiPacket() {
    }

    public TDMKitGuiPacket(String team, String[] kitNames) {
        this(team, kitNames, new int[kitNames.length],
                new ItemStack[kitNames.length][INVENTORY_SLOT_COUNT],
                false, 0, 0, false, false);
    }

    public TDMKitGuiPacket(String team, String[] names, int[] costs,
            ItemStack[][] kitPreviews, boolean economy, int balance, int seconds,
            boolean buying, boolean mandatory) {
        validateKitArrayLengths(names, costs, kitPreviews);
        this.team = team;
        this.kitNames = names;
        this.costs = costs;
        this.kitPreviews = copyPreviews(kitPreviews);
        this.economy = economy;
        this.balance = balance;
        this.seconds = seconds;
        this.buying = buying;
        this.mandatory = mandatory;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        team = readBoundedString(buffer, MAX_TEAM_LENGTH, "team");
        requireReadable(buffer, 4);
        int kitCount = buffer.readInt();
        if (kitCount < 0 || kitCount > MAX_KITS) {
            throw new IllegalArgumentException("Invalid TDM kit count: " + kitCount);
        }

        kitNames = new String[kitCount];
        costs = new int[kitCount];
        kitPreviews = new ItemStack[kitCount][INVENTORY_SLOT_COUNT];

        for (int kitIndex = 0; kitIndex < kitCount; kitIndex++) {
            kitNames[kitIndex] = readBoundedString(buffer, MAX_NAME_LENGTH, "kit name");
            requireReadable(buffer, 4);
            costs[kitIndex] = buffer.readInt();

            requireReadable(buffer, 1);
            int itemCount = buffer.readUnsignedByte();
            if (itemCount > INVENTORY_SLOT_COUNT) {
                throw new IllegalArgumentException("Invalid TDM preview item count: " + itemCount);
            }

            boolean[] occupiedSlots = new boolean[INVENTORY_SLOT_COUNT];
            for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                requireReadable(buffer, 1);
                int slot = buffer.readUnsignedByte();
                if (slot >= INVENTORY_SLOT_COUNT || occupiedSlots[slot]) {
                    throw new IllegalArgumentException("Invalid TDM preview slot: " + slot);
                }
                occupiedSlots[slot] = true;
                kitPreviews[kitIndex][slot] = ByteBufUtils.readItemStack(buffer);
            }
        }

        requireReadable(buffer, 11);
        economy = buffer.readBoolean();
        balance = buffer.readInt();
        seconds = buffer.readInt();
        buying = buffer.readBoolean();
        mandatory = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        validateKitArrayLengths(kitNames, costs, kitPreviews);
        if (kitNames.length > MAX_KITS) {
            throw new IllegalArgumentException("Too many TDM kits: " + kitNames.length);
        }

        writeBoundedString(buffer, team, MAX_TEAM_LENGTH, "team");
        buffer.writeInt(kitNames.length);
        for (int kitIndex = 0; kitIndex < kitNames.length; kitIndex++) {
            writeBoundedString(buffer, kitNames[kitIndex], MAX_NAME_LENGTH, "kit name");
            buffer.writeInt(costs[kitIndex]);
            writePreview(buffer, kitPreviews[kitIndex]);
        }

        buffer.writeBoolean(economy);
        buffer.writeInt(balance);
        buffer.writeInt(seconds);
        buffer.writeBoolean(buying);
        buffer.writeBoolean(mandatory);
    }

    private static void writePreview(ByteBuf buffer, ItemStack[] preview) {
        if (preview == null || preview.length > INVENTORY_SLOT_COUNT) {
            throw new IllegalArgumentException("Invalid TDM kit preview size");
        }

        int itemCount = 0;
        for (ItemStack stack : preview) {
            if (stack != null) {
                itemCount++;
            }
        }
        buffer.writeByte(itemCount);

        for (int slot = 0; slot < preview.length; slot++) {
            ItemStack stack = preview[slot];
            if (stack == null) {
                continue;
            }
            buffer.writeByte(slot);
            ByteBufUtils.writeItemStack(buffer, stack);
        }
    }

    private static ItemStack[][] copyPreviews(ItemStack[][] source) {
        ItemStack[][] copy = new ItemStack[source.length][INVENTORY_SLOT_COUNT];
        for (int kitIndex = 0; kitIndex < source.length; kitIndex++) {
            ItemStack[] preview = source[kitIndex];
            if (preview == null || preview.length > INVENTORY_SLOT_COUNT) {
                throw new IllegalArgumentException("Invalid TDM kit preview size");
            }
            for (int slot = 0; slot < preview.length; slot++) {
                if (preview[slot] != null) {
                    copy[kitIndex][slot] = preview[slot].copy();
                }
            }
        }
        return copy;
    }

    private static void validateKitArrayLengths(String[] names, int[] costs,
            ItemStack[][] previews) {
        if (names == null || costs == null || previews == null
                || names.length != costs.length || names.length != previews.length) {
            throw new IllegalArgumentException("TDM kit packet arrays must have matching lengths");
        }
    }

    private static String readBoundedString(ByteBuf buffer, int maximumLength, String fieldName) {
        String value = ByteBufUtils.readUTF8String(buffer);
        if (value == null || value.length() > maximumLength) {
            throw new IllegalArgumentException("Invalid TDM " + fieldName);
        }
        return value;
    }

    private static void writeBoundedString(ByteBuf buffer, String value,
            int maximumLength, String fieldName) {
        if (value == null || value.length() > maximumLength) {
            throw new IllegalArgumentException("Invalid TDM " + fieldName);
        }
        ByteBufUtils.writeUTF8String(buffer, value);
    }

    private static void requireReadable(ByteBuf buffer, int byteCount) {
        if (buffer.readableBytes() < byteCount) {
            throw new IllegalArgumentException("Truncated TDM kit GUI packet");
        }
    }

    public static class Handler implements IMessageHandler<TDMKitGuiPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final TDMKitGuiPacket message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    if (message.kitNames.length == 0) {
                        EventHandlerClient.clearMandatoryKitGui(true);
                        return;
                    }

                    GUITDMKitSelect gui = new GUITDMKitSelect(
                            message.team,
                            message.kitNames,
                            message.costs,
                            message.kitPreviews,
                            message.economy,
                            message.balance,
                            message.seconds,
                            message.buying,
                            message.mandatory);
                    if (message.mandatory) {
                        EventHandlerClient.setMandatoryKitGui(gui);
                    } else {
                        Minecraft.getMinecraft().displayGuiScreen(gui);
                    }
                }
            });
            return null;
        }
    }
}
