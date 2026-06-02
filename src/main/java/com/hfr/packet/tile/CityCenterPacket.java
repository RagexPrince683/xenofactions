package com.hfr.packet.tile;

import com.hfr.client.flag.FactionFlagTextureManager;
import com.hfr.tileentity.clowder.TileEntityFlag;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

public class CityCenterPacket implements IMessage {

	public int x;
	public int y;
	public int z;
	public String cityName;
	public String ownerName;
	public String flagHash;

	public CityCenterPacket() { }

	public CityCenterPacket(int x, int y, int z, String cityName, String ownerName) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.cityName = cityName == null ? "" : cityName;
		this.ownerName = ownerName == null ? "" : ownerName;
		this.flagHash = "";
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		cityName = ByteBufUtils.readUTF8String(buf);
		ownerName = ByteBufUtils.readUTF8String(buf);
		flagHash = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeUTF8String(buf, cityName == null ? "" : cityName);
		ByteBufUtils.writeUTF8String(buf, ownerName == null ? "" : ownerName);
		ByteBufUtils.writeUTF8String(buf, flagHash == null ? "" : flagHash);
	}

	public static class Handler implements IMessageHandler<CityCenterPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(CityCenterPacket m, MessageContext ctx) {
			try {
				TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(m.x, m.y, m.z);
				if(te instanceof TileEntityFlag) {
					TileEntityFlag flag = (TileEntityFlag)te;
					flag.name = m.cityName == null ? "" : m.cityName;
					flag.ownerName = m.ownerName == null ? "" : m.ownerName;
					flag.customFlagHash = m.flagHash == null ? "" : m.flagHash;
					if(flag.ownerName.length() > 0 && flag.customFlagHash.length() > 0)
						FactionFlagTextureManager.handleMetadata(flag.ownerName, flag.customFlagHash);
				}
			} catch(Exception e) { }
			return null;
		}
	}
}
