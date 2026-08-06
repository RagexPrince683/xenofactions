package com.hfr.packet.client;

import com.hfr.blocks.ModBlocks;
import com.hfr.inventory.gui.GUIFlag;
import com.hfr.tileentity.clowder.TileEntityFlag;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

/** Opens the informational City Center screen without replacing the player's container. */
public class CityCenterGuiPacket implements IMessage {
    private int dimension;
    private int x;
    private int y;
    private int z;

    public CityCenterGuiPacket() { }

    public CityCenterGuiPacket(int dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<CityCenterGuiPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final CityCenterGuiPacket message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    Minecraft minecraft = Minecraft.getMinecraft();
                    if(minecraft.theWorld == null || minecraft.theWorld.provider.dimensionId != message.dimension
                        || minecraft.theWorld.getBlock(message.x, message.y, message.z) != ModBlocks.clowder_flag)
                        return;
                    TileEntity tile = minecraft.theWorld.getTileEntity(message.x, message.y, message.z);
                    if(tile instanceof TileEntityFlag)
                        minecraft.displayGuiScreen(new GUIFlag((TileEntityFlag)tile));
                }
            });
            return null;
        }
    }
}
