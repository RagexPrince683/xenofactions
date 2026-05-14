package com.hfr.packet.client;

import com.hfr.tdm.TDMManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class TDMMapVoteSelectPacket implements IMessage {

    private String mapName;

    public TDMMapVoteSelectPacket() { }

    public TDMMapVoteSelectPacket(String mapName) {
        this.mapName = mapName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mapName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, mapName);
    }

    public static class Handler implements IMessageHandler<TDMMapVoteSelectPacket, IMessage> {

        @Override
        public IMessage onMessage(TDMMapVoteSelectPacket message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (!TDMManager.isMapVoteActive(player.worldObj) || TDMManager.voteForMap(player.worldObj, player.getCommandSenderName(), message.mapName) == null) {
                player.addChatMessage(new ChatComponentText("Unable to vote for that TDM map."));
            } else {
                player.addChatMessage(new ChatComponentText("Voted for TDM map " + TDMManager.normalizeMapName(message.mapName) + "."));
            }
            return null;
        }
    }
}
