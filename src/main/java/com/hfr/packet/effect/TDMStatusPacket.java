package com.hfr.packet.effect;

import com.hfr.main.EventHandlerClient;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class TDMStatusPacket implements IMessage {

    private boolean enabled;
    private boolean voting;
    private int roundSeconds;
    private int voteSeconds;
    private int redScore;
    private int blueScore;
    private String mapName;

    public TDMStatusPacket() { }

    public TDMStatusPacket(boolean enabled, boolean voting, int roundSeconds, int voteSeconds, int redScore, int blueScore, String mapName) {
        this.enabled = enabled;
        this.voting = voting;
        this.roundSeconds = roundSeconds;
        this.voteSeconds = voteSeconds;
        this.redScore = redScore;
        this.blueScore = blueScore;
        this.mapName = mapName == null ? "" : mapName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        enabled = buf.readBoolean();
        voting = buf.readBoolean();
        roundSeconds = buf.readInt();
        voteSeconds = buf.readInt();
        redScore = buf.readInt();
        blueScore = buf.readInt();
        mapName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(enabled);
        buf.writeBoolean(voting);
        buf.writeInt(roundSeconds);
        buf.writeInt(voteSeconds);
        buf.writeInt(redScore);
        buf.writeInt(blueScore);
        ByteBufUtils.writeUTF8String(buf, mapName);
    }

    public static class Handler implements IMessageHandler<TDMStatusPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TDMStatusPacket message, MessageContext ctx) {
            EventHandlerClient.updateTDMStatus(message.enabled, message.voting, message.roundSeconds, message.voteSeconds, message.redScore, message.blueScore, message.mapName);
            return null;
        }
    }
}
