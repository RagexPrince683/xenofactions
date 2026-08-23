package com.hfr.packet.wallart;
import java.nio.charset.Charset;
import com.hfr.wallart.WallArtConstants;
import com.hfr.wallart.WallArtSavedData;
import com.hfr.wallart.WallArtService;
import com.hfr.packet.PacketDispatcher;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
public final class WallArtImageRequestPacket implements IMessage {
  private String hash = "";
  private boolean valid;
  public WallArtImageRequestPacket() {}
  public WallArtImageRequestPacket(String h) {
    hash = h;
    valid = true;
  }
  public void fromBytes(ByteBuf b) {
    valid = false;
    if (b.readableBytes() != 64)
      return;
    byte[] v = new byte[64];
    b.readBytes(v);
    hash = new String(v, Charset.forName("US-ASCII"));
    valid = WallArtConstants.validHash(hash);
  }
  public void toBytes(ByteBuf b) {
    b.writeBytes(hash.getBytes(Charset.forName("US-ASCII")));
  }
  public static final class Handler
      implements IMessageHandler<WallArtImageRequestPacket, IMessage> {
    public IMessage onMessage(final WallArtImageRequestPacket m,
                              MessageContext c) {
      final EntityPlayerMP p = c.getServerHandler().playerEntity;
      WallArtService.enqueueMainThread(new Runnable() {
        public void run() {
          if (!m.valid || !WallArtSavedData.get(p.worldObj).references(m.hash))
            return;
          try {
            byte[] all = WallArtService.readImage(m.hash);
            int total = (all.length + WallArtConstants.CHUNK_BYTES - 1) /
                        WallArtConstants.CHUNK_BYTES;
            for (int i = 0; i < total; i++) {
              int n = Math.min(WallArtConstants.CHUNK_BYTES,
                               all.length - i * WallArtConstants.CHUNK_BYTES);
              byte[] part = new byte[n];
              System.arraycopy(all, i * WallArtConstants.CHUNK_BYTES, part, 0,
                               n);
              PacketDispatcher.wrapper.sendTo(
                  new WallArtImageChunkPacket(m.hash, i, total, all.length,
                                              part),
                  p);
            }
          } catch (Exception ignored) {
          }
        }
      });
      return null;
    }
  }
}
