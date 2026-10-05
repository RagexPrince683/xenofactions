package com.hfr.packet.shop;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public final class XShopSnapshotPacket implements IMessage {
    private NBTTagCompound data;
    public XShopSnapshotPacket() { }
    public XShopSnapshotPacket(NBTTagCompound data) { this.data = data; }
    public void fromBytes(ByteBuf buf) { data = ByteBufUtils.readTag(buf); }
    public void toBytes(ByteBuf buf) { ByteBufUtils.writeTag(buf, data); }
    public static final class Handler implements IMessageHandler<XShopSnapshotPacket, IMessage> {
        public IMessage onMessage(final XShopSnapshotPacket message, MessageContext context) { receive(message.data); return null; }
        @SideOnly(Side.CLIENT) private void receive(final NBTTagCompound data) {
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable() {
                public void run() {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
                    if (data == null || mc.thePlayer == null) return;
                    boolean current = mc.currentScreen instanceof com.hfr.inventory.gui.GUIXShopManager
                        && ((com.hfr.inventory.gui.GUIXShopManager)mc.currentScreen).token().equals(data.getString("token"))
                        || mc.currentScreen instanceof com.hfr.inventory.gui.GUIMachineMarket
                        && ((com.hfr.inventory.gui.GUIMachineMarket)mc.currentScreen).token().equals(data.getString("token"));
                    if (!data.getBoolean("opening") && !current) return;
                    if (data.getString("mode").equals("close")) {
                        if (mc.currentScreen instanceof com.hfr.inventory.gui.GUIXShopManager
                            || mc.currentScreen instanceof com.hfr.inventory.gui.GUIMachineMarket) mc.displayGuiScreen(null);
                        mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("[XShop] " + data.getString("reason")));
                    } else if (data.getString("mode").equals("trade")) {
                        if (mc.currentScreen instanceof com.hfr.inventory.gui.GUIMachineMarket)
                            ((com.hfr.inventory.gui.GUIMachineMarket)mc.currentScreen).receive(data);
                        else mc.displayGuiScreen(new com.hfr.inventory.gui.GUIMachineMarket(data));
                    } else {
                        if (mc.currentScreen instanceof com.hfr.inventory.gui.GUIXShopManager)
                            ((com.hfr.inventory.gui.GUIXShopManager)mc.currentScreen).receive(data);
                        else mc.displayGuiScreen(new com.hfr.inventory.gui.GUIXShopManager(data));
                    }
                }
            });
        }
    }
}
