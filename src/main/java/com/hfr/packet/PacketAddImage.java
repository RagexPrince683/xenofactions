package com.hfr.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
//import .storage.CustomImageStorage;
import com.hfr.data.CustomImageStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class PacketAddImage implements IMessage {
    private String name;
    private String url;

    // Required zero-arg constructor
    public PacketAddImage() {}

    public PacketAddImage(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int nameLen = buf.readInt();
        byte[] nameBytes = new byte[nameLen];
        buf.readBytes(nameBytes);
        this.name = new String(nameBytes);

        int urlLen = buf.readInt();
        byte[] urlBytes = new byte[urlLen];
        buf.readBytes(urlBytes);
        this.url = new String(urlBytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        byte[] nameBytes = name.getBytes();
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);

        byte[] urlBytes = url.getBytes();
        buf.writeInt(urlBytes.length);
        buf.writeBytes(urlBytes);
    }

    // Handler
    public static class Handler implements IMessageHandler<PacketAddImage, IMessage> {
        @Override
        public IMessage onMessage(PacketAddImage message, MessageContext ctx) {
            // This is already server-side in 1.7.10
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            CustomImageStorage storage = CustomImageStorage.get(player.worldObj);
            boolean ok = storage.addImage(player.getUniqueID(), message.name, message.url);

            if (!ok) {
                player.addChatMessage(new ChatComponentText("Add failed: max 5 images."));
            } else {
                player.addChatMessage(new ChatComponentText("Image added: " + message.name));
            }

            return null;
        }
    }
}
