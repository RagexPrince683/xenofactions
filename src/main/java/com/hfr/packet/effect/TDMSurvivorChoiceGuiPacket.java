package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUITDMSurvivorChoice;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class TDMSurvivorChoiceGuiPacket implements IMessage {
    public void fromBytes(ByteBuf buffer) { }
    public void toBytes(ByteBuf buffer) { }
    public static class Handler implements IMessageHandler<TDMSurvivorChoiceGuiPacket,IMessage> {
        public IMessage onMessage(TDMSurvivorChoiceGuiPacket message,MessageContext context){Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){Minecraft.getMinecraft().displayGuiScreen(new GUITDMSurvivorChoice());}});return null;}
    }
}
