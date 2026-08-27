package com.hfr.packet.effect;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Reserves the former TDM spectator packet discriminator without controlling the client camera.
 *
 * The legacy fields are still decoded so an in-flight packet cannot corrupt the channel, but
 * Xenofactions no longer sends this message or acts on its obsolete camera target.
 */
public class LegacyTDMSpectatorCompatibilityPacket implements IMessage {
    private int ignoredEntityId;
    private String ignoredPlayerName = "";

    public LegacyTDMSpectatorCompatibilityPacket() {
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        ignoredEntityId = buffer.readInt();
        ignoredPlayerName = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(ignoredEntityId);
        ByteBufUtils.writeUTF8String(buffer, ignoredPlayerName);
    }

    public static class Handler implements IMessageHandler<LegacyTDMSpectatorCompatibilityPacket, IMessage> {
        @Override
        public IMessage onMessage(LegacyTDMSpectatorCompatibilityPacket message, MessageContext context) {
            return null;
        }
    }
}
