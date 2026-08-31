package com.hfr.clowder;

import com.hfr.command.CommandClowder;
import com.hfr.tdm.TDMManager;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/** Central policy for faction membership operations which are incompatible with TDM. */
public final class FactionMembershipGuard {

	public static final String TDM_DISABLED_MESSAGE = "Factions are disabled while TDM is enabled.";

	private FactionMembershipGuard() { }

	public static boolean isBlocked(World world) {
		return world != null && TDMManager.isEnabled(world);
	}

	public static boolean rejectIfBlocked(ICommandSender sender) {
		if (sender == null || !isBlocked(sender.getEntityWorld())) return false;
		sender.addChatMessage(new ChatComponentText(CommandClowder.ERROR + TDM_DISABLED_MESSAGE));
		return true;
	}
}
