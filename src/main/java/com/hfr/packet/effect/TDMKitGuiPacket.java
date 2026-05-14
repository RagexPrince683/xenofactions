package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUITDMKitSelect;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class TDMKitGuiPacket implements IMessage {

    private String team;
    private String[] kitNames;

    public TDMKitGuiPacket() { }

    public TDMKitGuiPacket(String team, String[] kitNames) {
        this.team = team;
        this.kitNames = kitNames;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        team = ByteBufUtils.readUTF8String(buf);
        int count = buf.readInt();
        kitNames = new String[count];
        for (int i = 0; i < count; i++) {
            kitNames[i] = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, team);
        buf.writeInt(kitNames.length);
        for (int i = 0; i < kitNames.length; i++) {
            ByteBufUtils.writeUTF8String(buf, kitNames[i]);
        }
    }

    public static class Handler implements IMessageHandler<TDMKitGuiPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TDMKitGuiPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().displayGuiScreen(new GUITDMKitSelect(message.team, message.kitNames));
            return null;
        }
    }
}
