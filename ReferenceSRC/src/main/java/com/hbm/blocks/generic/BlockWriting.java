package com.hbm.blocks.generic;

import com.hbm.blocks.machine.BlockPillar;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class BlockWriting extends BlockPillar {

	public BlockWriting(Material mat, String top) {
		super(mat, top);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;

		} else if(!player.isSneaking()) {

			ChatStyle red = new ChatStyle().setColor(EnumChatFormatting.RED);
			player.addChatMessage(new ChatComponentText("This place is a message... and part of a system of messages... pay attention to it!").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("Sending this message was important to us. We considered ourselves to be a powerful culture.").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("This place is not a place of honor... no highly esteemed deed is commemorated here... nothing valued is here.").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("What is here was dangerous and repulsive to us. This message is a warning about danger.").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("The danger is in a particular location... it increases towards a center... the center of danger is here... of a particular size and shape, and below us.").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("The danger is still present, in your time, as it was in ours.").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("The danger is to the body, and it can kill.").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("The form of the danger is an emanation of energy.").setChatStyle(red));
			player.addChatMessage(new ChatComponentText("The danger is unleashed only if you substantially disturb this place physically. This place is best shunned and left uninhabited.").setChatStyle(red));
			return true;

		} else {
			return false;
		}
	}
}
