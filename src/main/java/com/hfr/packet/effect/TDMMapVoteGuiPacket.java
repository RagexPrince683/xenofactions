package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUITDMMapVote;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class TDMMapVoteGuiPacket implements IMessage {

    private String[] mapNames;
    private int voteSeconds;

    public TDMMapVoteGuiPacket() { }

    public TDMMapVoteGuiPacket(String[] mapNames, int voteSeconds) {
        this.mapNames = mapNames;
        this.voteSeconds = voteSeconds;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        voteSeconds = buf.readInt();
        int count = buf.readInt();
        mapNames = new String[count];
        for (int i = 0; i < count; i++) {
            mapNames[i] = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(voteSeconds);
        buf.writeInt(mapNames.length);
        for (int i = 0; i < mapNames.length; i++) {
            ByteBufUtils.writeUTF8String(buf, mapNames[i]);
        }
    }

    public static class Handler implements IMessageHandler<TDMMapVoteGuiPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TDMMapVoteGuiPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().displayGuiScreen(new GUITDMMapVote(message.mapNames, message.voteSeconds));
            return null;
        }
    }
}
