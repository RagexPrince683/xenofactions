package com.hfr.packet.client;

import com.hfr.tdm.TDMManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class TDMKitSelectPacket implements IMessage {

    private int kitIndex;

    public TDMKitSelectPacket() { }

    public TDMKitSelectPacket(int kitIndex) {
        this.kitIndex = kitIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        kitIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(kitIndex);
    }

    public static class Handler implements IMessageHandler<TDMKitSelectPacket, IMessage> {

        @Override
        public IMessage onMessage(TDMKitSelectPacket message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (!TDMManager.selectKit(player, message.kitIndex)) {
                player.addChatMessage(new ChatComponentText("Unable to select that TDM kit."));
            }
            return null;
        }
    }
}
