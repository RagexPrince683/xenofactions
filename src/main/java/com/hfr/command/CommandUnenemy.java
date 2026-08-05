package com.hfr.command;

import com.hfr.clowder.Clowder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class CommandUnenemy extends CommandBase {
	@Override public String getCommandName() { return "unenemy"; }
	@Override public String getCommandUsage(ICommandSender sender) { return "/unenemy <faction>"; }
	@Override public int getRequiredPermissionLevel() { return 0; }
	@Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
	@Override public void processCommand(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayer)) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Only players can use /unenemy.")); return; }
		if(args.length < 1) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Usage: /unenemy <faction>")); return; }
		EntityPlayer player = (EntityPlayer)sender;
		Clowder me = Clowder.getClowderFromPlayer(player);
		if(me == null) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "You are not in a faction!")); return; }
		if(me.getPermLevel(player) <= 2) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Only faction leaders can remove enemies.")); return; }
		Clowder target = Clowder.getClowderFromName(String.join(" ", args));
		if(target == null) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Unknown faction.")); return; }
		if(!me.isEnemyFaction(target, player.worldObj)) { sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + target.name + " is not marked as an enemy.")); return; }
		long remaining = me.getEnemyRemovalRemaining(target, System.currentTimeMillis());
		if(remaining >= 0L) { sender.addChatMessage(new ChatComponentText(CommandClowder.INFO + "Enemy removal is already pending: " + formatTime(remaining) + " remaining.")); return; }
		me.scheduleEnemyRemoval(target, player.worldObj);
		me.notifyAll(player.worldObj, new ChatComponentText(CommandClowder.INFO + "Enemy removal for " + target.name + " has started. The relation remains active for 72 hours."));
		target.notifyAll(player.worldObj, new ChatComponentText(CommandClowder.INFO + me.name + " will stop marking your faction as an enemy after 72 hours."));
	}
	public static String formatTime(long ms) {
		long sec = ms / 1000L, days = sec / 86400L; sec %= 86400L; long hrs = sec / 3600L; sec %= 3600L; long min = sec / 60L; sec %= 60L;
		return days + "d " + hrs + "h " + min + "m " + sec + "s";
	}
}
