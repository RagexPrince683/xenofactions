package com.hfr.packet.effect;

import com.hfr.inventory.gui.GUITDMKitSelect;
import com.hfr.tdm.TDMManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

/** Authoritative response to one kit-selection request. */
public class TDMKitSelectResultPacket implements IMessage {
    private TDMManager.KitSelectionResult result;

    public TDMKitSelectResultPacket() { }
    public TDMKitSelectResultPacket(TDMManager.KitSelectionResult result) { this.result=result; }
    public void fromBytes(ByteBuf buf){int ordinal=buf.readUnsignedByte();TDMManager.KitSelectionResult[] values=TDMManager.KitSelectionResult.values();result=ordinal<values.length?values[ordinal]:TDMManager.KitSelectionResult.INVALID_SELECTION;}
    public void toBytes(ByteBuf buf){buf.writeByte(result.ordinal());}

    public static class Handler implements IMessageHandler<TDMKitSelectResultPacket,IMessage> {
        public IMessage onMessage(final TDMKitSelectResultPacket message,MessageContext context){handle(message);return null;}
        @SideOnly(Side.CLIENT)
        private void handle(final TDMKitSelectResultPacket message){Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){if(message.result==TDMManager.KitSelectionResult.SUCCESS)com.hfr.main.EventHandlerClient.clearMandatoryKitGui(true);else if(Minecraft.getMinecraft().currentScreen instanceof GUITDMKitSelect)((GUITDMKitSelect)Minecraft.getMinecraft().currentScreen).receiveSelectionResult(message.result);}});}
    }
}
