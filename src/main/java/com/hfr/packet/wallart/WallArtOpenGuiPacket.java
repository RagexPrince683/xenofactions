package com.hfr.packet.wallart;

import com.hfr.main.MainRegistry;
import com.hfr.tileentity.TileEntityWallImage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/** Server authorization for the client to open an existing Wall Art tile. */
public final class WallArtOpenGuiPacket implements IMessage {
  private int dimension, x, y, z;

  public WallArtOpenGuiPacket() {}

  public WallArtOpenGuiPacket(int dimension, int x, int y, int z) {
    this.dimension = dimension;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public void fromBytes(ByteBuf buffer) {
    dimension = buffer.readInt();
    x = buffer.readInt();
    y = buffer.readInt();
    z = buffer.readInt();
  }

  @Override
  public void toBytes(ByteBuf buffer) {
    buffer.writeInt(dimension);
    buffer.writeInt(x);
    buffer.writeInt(y);
    buffer.writeInt(z);
  }

  public static final class Handler
      implements IMessageHandler<WallArtOpenGuiPacket, IMessage> {
    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(final WallArtOpenGuiPacket message,
                              MessageContext context) {
      Minecraft.getMinecraft().func_152344_a(new Runnable() {
        @Override
        public void run() {
          EntityPlayer player = Minecraft.getMinecraft().thePlayer;
          if (player == null || player.worldObj == null ||
              player.worldObj.provider.dimensionId != message.dimension)
            return;
          TileEntity tile = player.worldObj.getTileEntity(
              message.x, message.y, message.z);
          if (tile instanceof TileEntityWallImage)
            MainRegistry.proxy.openWallArtGui((TileEntityWallImage)tile);
        }
      });
      return null;
    }
  }
}
