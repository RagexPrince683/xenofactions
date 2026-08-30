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

    private String initialPool = "";
    private String[] redNames = new String[0];
    private int[] redCosts = new int[0];
    private ItemStack[][] redPreviews = new ItemStack[0][INVENTORY_SLOT_COUNT];
    private String[] blueNames = new String[0];
    private int[] blueCosts = new int[0];
    private ItemStack[][] bluePreviews = new ItemStack[0][INVENTORY_SLOT_COUNT];
    private boolean ffa;
    private boolean economy;
    private boolean buying;
    private boolean mandatory;
    private int balance;
    private int seconds;

    public TDMKitGuiPacket() { }

    public TDMKitGuiPacket(String team, String[] names) {
        this(team, names, new int[names.length], new ItemStack[names.length][INVENTORY_SLOT_COUNT],
                new String[0], new int[0], new ItemStack[0][INVENTORY_SLOT_COUNT], false,
                false, 0, 0, false, false);
    }

    public TDMKitGuiPacket(String initialPool, String[] redNames, int[] redCosts,
            ItemStack[][] redPreviews, String[] blueNames, int[] blueCosts,
            ItemStack[][] bluePreviews, boolean ffa, boolean economy, int balance,
            int seconds, boolean buying, boolean mandatory) {
        validatePool(redNames, redCosts, redPreviews);
        validatePool(blueNames, blueCosts, bluePreviews);
        this.initialPool = initialPool;
        this.redNames = redNames;
        this.redCosts = redCosts;
        this.redPreviews = copyPreviews(redPreviews);
        this.blueNames = blueNames;
        this.blueCosts = blueCosts;
        this.bluePreviews = copyPreviews(bluePreviews);
        this.ffa = ffa;
        this.economy = economy;
        this.balance = balance;
        this.seconds = seconds;
        this.buying = buying;
        this.mandatory = mandatory;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        initialPool = readBoundedString(buffer, MAX_TEAM_LENGTH, "pool");
        PoolData red = readPool(buffer);
        redNames = red.names;
        redCosts = red.costs;
        redPreviews = red.previews;
        PoolData blue = readPool(buffer);
        blueNames = blue.names;
        blueCosts = blue.costs;
        bluePreviews = blue.previews;
        requireReadable(buffer, 12);
        ffa = buffer.readBoolean();
        economy = buffer.readBoolean();
        balance = buffer.readInt();
        seconds = buffer.readInt();
        buying = buffer.readBoolean();
        mandatory = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        writeBoundedString(buffer, initialPool, MAX_TEAM_LENGTH, "pool");
        writePool(buffer, redNames, redCosts, redPreviews);
        writePool(buffer, blueNames, blueCosts, bluePreviews);
        buffer.writeBoolean(ffa);
        buffer.writeBoolean(economy);
        buffer.writeInt(balance);
        buffer.writeInt(seconds);
        buffer.writeBoolean(buying);
        buffer.writeBoolean(mandatory);
    }

    private static PoolData readPool(ByteBuf buffer) {
        requireReadable(buffer, 4);
        int count = buffer.readInt();
        if (count < 0 || count > MAX_KITS) {
            throw new IllegalArgumentException("Invalid TDM kit count: " + count);
        }
        String[] names = new String[count];
        int[] costs = new int[count];
        ItemStack[][] previews = new ItemStack[count][INVENTORY_SLOT_COUNT];
        for (int kitIndex = 0; kitIndex < count; kitIndex++) {
            names[kitIndex] = readBoundedString(buffer, MAX_NAME_LENGTH, "kit name");
            requireReadable(buffer, 5);
            costs[kitIndex] = buffer.readInt();
            int itemCount = buffer.readUnsignedByte();
            if (itemCount > INVENTORY_SLOT_COUNT) {
                throw new IllegalArgumentException("Invalid TDM preview item count: " + itemCount);
            }
            boolean[] occupied = new boolean[INVENTORY_SLOT_COUNT];
            for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                requireReadable(buffer, 1);
                int slot = buffer.readUnsignedByte();
                if (slot >= INVENTORY_SLOT_COUNT || occupied[slot]) {
                    throw new IllegalArgumentException("Invalid TDM preview slot: " + slot);
                }
                occupied[slot] = true;
                previews[kitIndex][slot] = ByteBufUtils.readItemStack(buffer);
            }
        }
        return new PoolData(names, costs, previews);
    }

    private static void writePool(ByteBuf buffer, String[] names, int[] costs,
            ItemStack[][] previews) {
        validatePool(names, costs, previews);
        if (names.length > MAX_KITS) {
            throw new IllegalArgumentException("Too many TDM kits: " + names.length);
        }
        buffer.writeInt(names.length);
        for (int kitIndex = 0; kitIndex < names.length; kitIndex++) {
            writeBoundedString(buffer, names[kitIndex], MAX_NAME_LENGTH, "kit name");
            buffer.writeInt(costs[kitIndex]);
            writePreview(buffer, previews[kitIndex]);
        }
    }

    private static void writePreview(ByteBuf buffer, ItemStack[] preview) {
        if (preview == null || preview.length > INVENTORY_SLOT_COUNT) {
            throw new IllegalArgumentException("Invalid TDM kit preview size");
        }
        int count = 0;
        for (ItemStack stack : preview) {
            if (stack != null) count++;
        }
        buffer.writeByte(count);
        for (int slot = 0; slot < preview.length; slot++) {
            if (preview[slot] != null) {
                buffer.writeByte(slot);
                ByteBufUtils.writeItemStack(buffer, preview[slot]);
            }
        }
    }

    private static ItemStack[][] copyPreviews(ItemStack[][] source) {
        ItemStack[][] copy = new ItemStack[source.length][INVENTORY_SLOT_COUNT];
        for (int kit = 0; kit < source.length; kit++) {
            if (source[kit] == null || source[kit].length > INVENTORY_SLOT_COUNT) {
                throw new IllegalArgumentException("Invalid TDM kit preview size");
            }
            for (int slot = 0; slot < source[kit].length; slot++) {
                if (source[kit][slot] != null) copy[kit][slot] = source[kit][slot].copy();
            }
        }
        return copy;
    }

    private static void validatePool(String[] names, int[] costs, ItemStack[][] previews) {
        if (names == null || costs == null || previews == null || names.length != costs.length
                || names.length != previews.length) {
            throw new IllegalArgumentException("TDM kit packet arrays must have matching lengths");
        }
    }

    private static String readBoundedString(ByteBuf buffer, int maximum, String field) {
        String value = ByteBufUtils.readUTF8String(buffer);
        if (value == null || value.length() > maximum) {
            throw new IllegalArgumentException("Invalid TDM " + field);
        }
        return value;
    }

    private static void writeBoundedString(ByteBuf buffer, String value, int maximum, String field) {
        if (value == null || value.length() > maximum) {
            throw new IllegalArgumentException("Invalid TDM " + field);
        }
        ByteBufUtils.writeUTF8String(buffer, value);
    }

    private static void requireReadable(ByteBuf buffer, int bytes) {
        if (buffer.readableBytes() < bytes) {
            throw new IllegalArgumentException("Truncated TDM kit GUI packet");
        }
    }

    private static final class PoolData {
        final String[] names;
        final int[] costs;
        final ItemStack[][] previews;

        PoolData(String[] names, int[] costs, ItemStack[][] previews) {
            this.names = names;
            this.costs = costs;
            this.previews = previews;
        }
    }

    public static class Handler implements IMessageHandler<TDMKitGuiPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final TDMKitGuiPacket message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    if (message.redNames.length == 0 && message.blueNames.length == 0) {
                        EventHandlerClient.clearMandatoryKitGui(true);
                        return;
                    }
                    GUITDMKitSelect gui = new GUITDMKitSelect(message.initialPool,
                            message.redNames, message.redCosts, message.redPreviews,
                            message.blueNames, message.blueCosts, message.bluePreviews,
                            message.ffa, message.economy, message.balance, message.seconds,
                            message.buying, message.mandatory);
                    if (message.mandatory) EventHandlerClient.setMandatoryKitGui(gui);
                    else Minecraft.getMinecraft().displayGuiScreen(gui);
                }
            });
            return null;
        }
    }
}
