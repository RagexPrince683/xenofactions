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
    private String currentMap;

    public TDMMapVoteGuiPacket() { }

    public TDMMapVoteGuiPacket(String[] mapNames, int voteSeconds, String currentMap) {
        this.mapNames = mapNames;
        this.voteSeconds = voteSeconds;
        this.currentMap = currentMap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        voteSeconds = buf.readInt();
        currentMap = ByteBufUtils.readUTF8String(buf);
        int count = buf.readInt();
        mapNames = new String[count];
        for (int i = 0; i < count; i++) {
            mapNames[i] = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(voteSeconds);
        ByteBufUtils.writeUTF8String(buf, currentMap);
        buf.writeInt(mapNames.length);
        for (int i = 0; i < mapNames.length; i++) {
            ByteBufUtils.writeUTF8String(buf, mapNames[i]);
        }
    }

    public static class Handler implements IMessageHandler<TDMMapVoteGuiPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final TDMMapVoteGuiPacket message, MessageContext ctx) {
            // Forge 1.7.10 SimpleNetworkWrapper handlers can run off-thread, so GUI state must change on the client thread.
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    Minecraft.getMinecraft().displayGuiScreen(
                            new GUITDMMapVote(message.mapNames, message.voteSeconds, message.currentMap)
                    );
                }
            });
            return null;
        }
    }
}
