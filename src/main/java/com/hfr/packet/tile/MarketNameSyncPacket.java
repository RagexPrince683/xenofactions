package com.hfr.packet.tile;

import com.hfr.blocks.machine.MachineMarket;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MarketNameSyncPacket implements IMessage {
    //not used?
    private int x, y, z;
    private NBTTagCompound data;

    public MarketNameSyncPacket() {}

    public MarketNameSyncPacket(int x, int y, int z, NBTTagCompound data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<MarketNameSyncPacket, IMessage> {
        @Override
        public IMessage onMessage(MarketNameSyncPacket message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            if (world != null) {
                TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
                if (tileEntity instanceof MachineMarket.TileEntityMarket) {
                    ((MachineMarket.TileEntityMarket) tileEntity).readFromNBT(message.data);
                }
            }
            return null;
        }
    }
}