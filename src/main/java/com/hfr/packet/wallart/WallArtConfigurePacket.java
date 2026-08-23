package com.hfr.packet.wallart;

import com.hfr.tileentity.TileEntityWallImage;
import com.hfr.wallart.WallArtConstants;
import com.hfr.wallart.WallArtSavedData;
import com.hfr.wallart.WallArtService;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

public final class WallArtConfigurePacket implements IMessage {
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private int dimension, x, y, z, width, height;
  private String url = "";
  private boolean valid;
  public WallArtConfigurePacket() {}
  public WallArtConfigurePacket(int d, int x, int y, int z, int w, int h,
                                String url) {
    dimension = d;
    this.x = x;
    this.y = y;
    this.z = z;
    width = w;
    height = h;
    this.url = url;
    valid = true;
  }
  public void fromBytes(ByteBuf b) {
    valid = false;
    if (b.readableBytes() < 28)
      return;
    dimension = b.readInt();
    x = b.readInt();
    y = b.readInt();
    z = b.readInt();
    width = b.readInt();
    height = b.readInt();
    int length = b.readInt();
    if (length < 1 || length > WallArtConstants.MAX_URL_BYTES ||
        length > b.readableBytes())
      return;
    byte[] bytes = new byte[length];
    b.readBytes(bytes);
    url = new String(bytes, UTF8);
    valid = b.readableBytes() == 0 && bytes.length == url.getBytes(UTF8).length;
  }
  public void toBytes(ByteBuf b) {
    byte[] bytes = url.getBytes(UTF8);
    b.writeInt(dimension);
    b.writeInt(x);
    b.writeInt(y);
    b.writeInt(z);
    b.writeInt(width);
    b.writeInt(height);
    b.writeInt(bytes.length);
    b.writeBytes(bytes);
  }
  public static final class Handler
      implements IMessageHandler<WallArtConfigurePacket, IMessage> {
    public IMessage onMessage(final WallArtConfigurePacket m,
                              MessageContext c) {
      final EntityPlayerMP p = c.getServerHandler().playerEntity;
      WallArtService.enqueueMainThread(new Runnable() {
        public void run() {
          if (!m.valid || !WallArtConstants.validSize(m.width, m.height) ||
              m.dimension != p.worldObj.provider.dimensionId ||
              p.getDistanceSq(m.x + .5, m.y + .5, m.z + .5) > 64) {
            p.addChatMessage(new ChatComponentText(
                "[Wall Art] Invalid configuration request."));
            return;
          }
          TileEntity raw = p.worldObj.getTileEntity(m.x, m.y, m.z);
          if (!(raw instanceof TileEntityWallImage))
            return;
          TileEntityWallImage tile = (TileEntityWallImage)raw;
          if (tile.getDisplayId() == null || tile.getOwnerId() == null ||
              !tile.getOwnerId().equals(p.getUniqueID())) {
            p.addChatMessage(new ChatComponentText(
                "[Wall Art] You do not own this display."));
            return;
          }
          WallArtSavedData data = WallArtSavedData.get(p.worldObj);
          if (data.get(tile.getDisplayId()) == null &&
              data.count(p.getUniqueID()) >= WallArtConstants.MAX_PER_PLAYER) {
            p.addChatMessage(new ChatComponentText(
                "[Wall Art] Wall Art limit reached (30 displays)."));
            return;
          }
          long generation = tile.getRequestGeneration() + 1L;
          if (WallArtService.submit(p, m.dimension, m.x, m.y, m.z,
                                    tile.getDisplayId(), generation, m.width,
                                    m.height, m.url)) {
            tile.beginRequest();
          }
        }
      });
      return null;
    }
  }
}
