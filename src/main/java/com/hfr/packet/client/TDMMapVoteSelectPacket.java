package com.hfr.packet.client;

import com.hfr.tdm.TDMManager;
import com.hfr.tdm.TDMServerTaskQueue;
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
        public IMessage onMessage(final TDMMapVoteSelectPacket message, MessageContext ctx) {
            final EntityPlayer player = ctx.getServerHandler().playerEntity;
            TDMServerTaskQueue.schedule(new Runnable() { public void run() {
                if (player == null || player.worldObj == null || !player.worldObj.playerEntities.contains(player)) return;
                String label = message.mapName == null || message.mapName.length() > 96 ? null
                        : TDMManager.voteForMap(player.worldObj, player.getCommandSenderName(), message.mapName);
                player.addChatMessage(new ChatComponentText(label == null
                        ? "Unable to vote for that map and gamemode pairing." : "Voted for " + label + "."));
            }});
            return null;
        }
    }
}
