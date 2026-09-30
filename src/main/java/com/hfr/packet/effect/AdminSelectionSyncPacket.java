package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUIAdminEditor;
import com.hfr.tdm.AdminSelectionManager.Selection;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

/** Read-only selection points for the client preview; authority remains server-side. */
public final class AdminSelectionSyncPacket implements IMessage {
    private NBTTagCompound selection = new NBTTagCompound();
    public AdminSelectionSyncPacket() { }
    public AdminSelectionSyncPacket(Selection value) {
        if (value == null) return;
        selection.setString("type", value.type.name()); selection.setString("map", value.map);
        selection.setInteger("dim", value.dimension);
        selection.setBoolean("hasA", value.hasA); selection.setBoolean("hasB", value.hasB);
        selection.setInteger("ax", value.ax); selection.setInteger("ay", value.ay); selection.setInteger("az", value.az);
        selection.setInteger("bx", value.bx); selection.setInteger("by", value.by); selection.setInteger("bz", value.bz);
    }
    public void fromBytes(ByteBuf buf) { selection = ByteBufUtils.readTag(buf); if (selection == null) selection = new NBTTagCompound(); }
    public void toBytes(ByteBuf buf) { ByteBufUtils.writeTag(buf, selection); }
    public static final class Handler implements IMessageHandler<AdminSelectionSyncPacket, IMessage> {
        @Override @SideOnly(Side.CLIENT) public IMessage onMessage(final AdminSelectionSyncPacket message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() { public void run() {
                GUIAdminEditor.updateSelection(message.selection);
            }});
            return null;
        }
    }
}
