package com.hfr.packet.client;

import com.hfr.inventory.gui.GUICityRename;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class CityRenameGuiPacket implements IMessage {

	private int x;
	private int y;
	private int z;
	private String name;

	public CityRenameGuiPacket() { }

	public CityRenameGuiPacket(int x, int y, int z, String name) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.name = name == null ? "" : name;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeUTF8String(buf, name == null ? "" : name);
	}

	public static class Handler implements IMessageHandler<CityRenameGuiPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(CityRenameGuiPacket m, MessageContext ctx) {
			Minecraft.getMinecraft().displayGuiScreen(new GUICityRename(m.x, m.y, m.z, m.name));
			return null;
		}
	}
}
