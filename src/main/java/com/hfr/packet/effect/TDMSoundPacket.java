package com.hfr.packet.effect;

import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class TDMSoundPacket implements IMessage {
    private String eventId = "";

    public TDMSoundPacket() { }
    public TDMSoundPacket(String eventId) { this.eventId = eventId == null ? "" : eventId; }

    @Override public void fromBytes(ByteBuf buf) { eventId = ByteBufUtils.readUTF8String(buf); }
    @Override public void toBytes(ByteBuf buf) { ByteBufUtils.writeUTF8String(buf, eventId); }

    public static class Handler implements IMessageHandler<TDMSoundPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final TDMSoundPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override public void run() {
                    Minecraft minecraft = Minecraft.getMinecraft();
                    if (minecraft.thePlayer == null || message.eventId.length() == 0) return;
                    warnIfBundledAssetMissing(minecraft, message.eventId);
                    minecraft.thePlayer.playSound(message.eventId, 1.0F, 1.0F);
                    if (XFConfig.enableDebugLogging && MainRegistry.logger != null)
                        MainRegistry.logger.info("TDM SOUND client playback: event={}, route=explicit-player-packet", message.eventId);
                }
            });
            return null;
        }

        private void warnIfBundledAssetMissing(Minecraft minecraft, String eventId) {
            String path = null;
            if ("hfr:tdm.ct_win1".equals(eventId)) path = "sounds/tdm/tdm_ct_win_1.ogg";
            else if ("hfr:tdm.t_win1".equals(eventId)) path = "sounds/tdm/tdm_t_win1.ogg";
            else if ("hfr:tdm.ct_round_start1".equals(eventId)) path = "sounds/tdm/tdm_ct_round_start1.ogg";
            else if ("hfr:tdm.t_round_start1".equals(eventId)) path = "sounds/tdm/tdm_t_round_start1.ogg";
            else if ("hfr:tdm.bomb_plant1".equals(eventId)) path = "sounds/tdm/tdm_bomb_plant1.ogg";
            if (path == null) return;
            try { minecraft.getResourceManager().getResource(new ResourceLocation("hfr", path)); }
            catch (Exception e) { if (MainRegistry.logger != null) MainRegistry.logger.warn("TDM SOUND bundled event {} has no readable client resource {}", eventId, path); }
        }
    }
}
