package com.hfr.packet.shop;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public final class PlayerTraderSnapshotPacket implements IMessage {
    private NBTTagCompound data;
    public PlayerTraderSnapshotPacket() { }
    public PlayerTraderSnapshotPacket(NBTTagCompound data) { this.data = data; }
    @Override public void fromBytes(ByteBuf buf) { data = ByteBufUtils.readTag(buf); }
    @Override public void toBytes(ByteBuf buf) { ByteBufUtils.writeTag(buf, data); }
    public static final class Handler implements IMessageHandler<PlayerTraderSnapshotPacket, IMessage> {
        @Override public IMessage onMessage(final PlayerTraderSnapshotPacket packet, MessageContext context) {
            receive(packet.data); return null;
        }
        @SideOnly(Side.CLIENT) private void receive(final NBTTagCompound tag) {
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable() { @Override public void run() {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
                if (tag == null || mc.thePlayer == null) return;
                if (tag.getBoolean("closed")) {
                    if (mc.currentScreen instanceof com.hfr.inventory.gui.GUIPlayerTrader
                        && ((com.hfr.inventory.gui.GUIPlayerTrader)mc.currentScreen).token().equals(tag.getString("token")))
                        mc.displayGuiScreen(null);
                    return;
                }
                if (tag.getBoolean("opening")) mc.displayGuiScreen(new com.hfr.inventory.gui.GUIPlayerTrader(tag));
                else if (mc.currentScreen instanceof com.hfr.inventory.gui.GUIPlayerTrader) {
                    com.hfr.inventory.gui.GUIPlayerTrader gui = (com.hfr.inventory.gui.GUIPlayerTrader)mc.currentScreen;
                    if (gui.token().equals(tag.getString("token"))) gui.receive(tag);
                }
            }});
        }
    }
}
