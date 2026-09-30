package com.hfr.packet.effect;

import com.hfr.client.journeymap.ClientTDMMapOverlay;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public final class TDMMapOverlayPacket implements IMessage {
    private NBTTagCompound data = new NBTTagCompound();
    public TDMMapOverlayPacket() { }
    public TDMMapOverlayPacket(NBTTagCompound data) { this.data = data; }
    @Override public void fromBytes(ByteBuf buf) { data = ByteBufUtils.readTag(buf); if (data == null) data = new NBTTagCompound(); }
    @Override public void toBytes(ByteBuf buf) { ByteBufUtils.writeTag(buf, data); }

    public static final class Handler implements IMessageHandler<TDMMapOverlayPacket, IMessage> {
        @Override @SideOnly(Side.CLIENT) public IMessage onMessage(final TDMMapOverlayPacket packet, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() { @Override public void run() {
                ClientTDMMapOverlay.accept(packet.data);
            } });
            return null;
        }
    }
}
