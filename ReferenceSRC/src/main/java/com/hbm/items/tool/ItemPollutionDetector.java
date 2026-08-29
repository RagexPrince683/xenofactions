package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionData;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemPollutionDetector extends Item {

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {
		
		if(!(entity instanceof EntityPlayerMP) || world.getTotalWorldTime() % 10 != 0) return;
		
		PollutionData data = PollutionHandler.getPollutionData(world, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ));
		if(data == null) data = new PollutionData();

		float soot = data.pollution[PollutionType.SOOT.ordinal()];
		float poison = data.pollution[PollutionType.POISON.ordinal()];
		float heavymetal = data.pollution[PollutionType.HEAVYMETAL.ordinal()];
		float co2 = poison;
		int monoxide = countNearbyBlocks(world, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ), ModBlocks.gas_monoxide, 4);
		//float fallout = data.pollution[PollutionType.FALLOUT.ordinal()];

		soot = ((int) (soot * 100)) / 100F;
		poison = ((int) (poison * 100)) / 100F;
		heavymetal = ((int) (heavymetal * 100)) / 100F;
		co2 = ((int) (co2 * 100)) / 100F;
		//fallout = ((int) (fallout * 100)) / 100F;
		
		PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("Soot: " + soot).color(EnumChatFormatting.YELLOW).flush(), 100, 4000), (EntityPlayerMP) entity);
		PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("Poison: " + poison).color(EnumChatFormatting.YELLOW).flush(), 101, 4000), (EntityPlayerMP) entity);
		PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("Heavy metal: " + heavymetal).color(EnumChatFormatting.YELLOW).flush(), 102, 4000), (EntityPlayerMP) entity);
		PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("CO: " + monoxide).color(monoxide > 0 ? EnumChatFormatting.RED : EnumChatFormatting.YELLOW).flush(), 103, 4000), (EntityPlayerMP) entity);
		PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("CO2/asphyxiant: " + co2).color(co2 > 0 ? EnumChatFormatting.GOLD : EnumChatFormatting.YELLOW).flush(), 104, 4000), (EntityPlayerMP) entity);
		//PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("Fallout: " + fallout).color(EnumChatFormatting.YELLOW).flush(), 105, 4000), (EntityPlayerMP) entity);
	}

	private int countNearbyBlocks(World world, int x, int y, int z, Block target, int radius) {
		int count = 0;
		for(int ix = x - radius; ix <= x + radius; ix++) {
			for(int iy = y - radius; iy <= y + radius; iy++) {
				for(int iz = z - radius; iz <= z + radius; iz++) {
					if(world.getBlock(ix, iy, iz) == target) count++;
				}
			}
		}
		return count;
	}
}
