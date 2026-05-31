package com.hfr.packet.client;

import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.tileentity.clowder.TileEntityFlag;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CityRenamePacket implements IMessage {

	private int x;
	private int y;
	private int z;
	private String name;

	public CityRenamePacket() { }

	public CityRenamePacket(int x, int y, int z, String name) {
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

	public static class Handler implements IMessageHandler<CityRenamePacket, IMessage> {

		@Override
		public IMessage onMessage(CityRenamePacket m, MessageContext ctx) {
			EntityPlayer player = ctx.getServerHandler().playerEntity;
			Clowder clowder = Clowder.getClowderFromPlayer(player);
			String newName = m.name == null ? "" : m.name.trim();

			if(clowder == null) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You are not in any faction!"));
				return null;
			}

			if(clowder.getPermLevel(player.getDisplayName()) < 2) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You lack the permissions to rename cities!"));
				return null;
			}

			if(newName.isEmpty()) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "City names cannot be blank."));
				return null;
			}

			TileEntity tile = player.worldObj.getTileEntity(m.x, m.y, m.z);
			if(!(tile instanceof TileEntityFlag)) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The City Center could not be found."));
				return null;
			}

			TileEntityFlag flag = (TileEntityFlag)tile;
			if(flag.owner != clowder) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You cannot rename a foreign city."));
				return null;
			}

			TerritoryMeta meta = ClowderTerritory.getMetaFromCoords(ClowderTerritory.getCoordPair(m.x, m.z));
			if(meta == null || !meta.isCityClaim() || meta.owner == null || meta.owner.owner != clowder) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "No owned city claims were found for this City Center."));
				return null;
			}

			if(!ClowderTerritory.isCityNameAvailable(newName, meta)) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "A city named " + newName + " already exists."));
				return null;
			}

			int renamed = ClowderTerritory.renameClaimsForCity(player.worldObj, m.x, m.y, m.z, newName);
			if(renamed > 0)
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "City renamed to " + newName + "."));
			else
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "No city claims were found for this City Center."));

			return null;
		}
	}
}
