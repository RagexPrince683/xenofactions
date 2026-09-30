package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUIAdminEditor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

/** Removes an open admin preview immediately when server authorization is revoked. */
public class AdminEditorClosePacket implements IMessage {
    public void fromBytes(ByteBuf buf) { }
    public void toBytes(ByteBuf buf) { }
    public static class Handler implements IMessageHandler<AdminEditorClosePacket,IMessage> {
        @Override @SideOnly(Side.CLIENT) public IMessage onMessage(AdminEditorClosePacket message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() { public void run() {
                if (Minecraft.getMinecraft().currentScreen instanceof GUIAdminEditor) Minecraft.getMinecraft().displayGuiScreen(null);
            }});
            return null;
        }
    }
}
