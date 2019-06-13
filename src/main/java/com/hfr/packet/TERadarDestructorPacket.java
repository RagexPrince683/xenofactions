package com.hfr.packet;

import com.hfr.tileentity.TileEntityMachineRadar;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

public class TERadarDestructorPacket implements IMessage {

	int x;
	int y;
	int z;
	int mode;
	
	public TERadarDestructorPacket() {

	}

	public TERadarDestructorPacket(int x, int y, int z, int mode) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.mode = mode;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		mode = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(mode);
	}

	public static class Handler implements IMessageHandler<TERadarDestructorPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(TERadarDestructorPacket m, MessageContext ctx) {
			TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(m.x, m.y, m.z);

			try {
				if (te != null && te instanceof TileEntityMachineRadar) {

					TileEntityMachineRadar radar = (TileEntityMachineRadar) te;
					radar.nearbyMissiles.clear();
					radar.mode = m.mode;
				}
			} catch (Exception x) {
			}
			return null;
		}
	}
}
