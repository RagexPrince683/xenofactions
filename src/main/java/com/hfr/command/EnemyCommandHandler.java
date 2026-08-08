package com.hfr.command;

import com.hfr.clowder.Clowder;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public final class EnemyCommandHandler {
	private EnemyCommandHandler() { }
	public static void execute(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayer)) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Only players can use /c enemy.")); return; }
		if(args.length < 1) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Usage: /c enemy <faction>")); return; }
		EntityPlayer player = (EntityPlayer)sender;
		Clowder me = Clowder.getClowderFromPlayer(player);
		if(me == null) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "You are not in a faction!")); return; }
		if(me.getPermLevel(player) <= 2) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Only faction leaders can mark enemies.")); return; }
		Clowder target = Clowder.getClowderFromName(String.join(" ", args));
		if(target == null) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Unknown faction.")); return; }
		if(target.uuid.equals(me.uuid)) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "You cannot mark your own faction as an enemy.")); return; }
		if(me.alliesS.containsKey(target.name)) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "You cannot mark an allied faction as an enemy.")); return; }
		boolean pending = me.getEnemyRemovalRemaining(target, System.currentTimeMillis()) >= 0L;
		boolean added = me.addEnemyFaction(target, player.worldObj);
		if(!added && !pending) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + target.name + " is already marked as an enemy.")); return; }
		if(pending) me.cancelEnemyRemoval(target, player.worldObj);
		me.notifyAll(player.worldObj, new ChatComponentText(CommandClowder.CRITICAL + target.name + " is now marked as an enemy."));
		target.notifyAll(player.worldObj, new ChatComponentText(CommandClowder.CRITICAL + me.name + " has marked your faction as an enemy."));
	}
}
