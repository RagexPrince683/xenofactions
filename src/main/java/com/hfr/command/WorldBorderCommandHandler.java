package com.hfr.command;

import java.util.ArrayList;
import java.util.List;

import com.hfr.config.XFConfig;
import com.hfr.items.ItemWorldBorderWand;
import com.hfr.tdm.AdminSelectionManager;
import com.hfr.tdm.AdminSelectionManager.Type;
import com.hfr.main.CommonEventHandler;
import com.hfr.saveddata.EarthBoundarySavedData;
import com.hfr.world.border.EarthBoundaryManager;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

/** Parser for /xc worldborder; all authorization and mutations remain server-side. */
public final class WorldBorderCommandHandler {
    public static final String USAGE = "/xc worldborder <on|off|status|wand|exempt|clearexemptions>";
    private WorldBorderCommandHandler() { }

    public static void execute(ICommandSender sender, String[] args) {
        if (!sender.canCommandSenderUseCommand(3, "xclowder")) { error(sender, "You do not have permission to administer the world border."); return; }
        World world = sender.getEntityWorld();
        if (args.length == 1 && ("on".equalsIgnoreCase(args[0]) || "off".equalsIgnoreCase(args[0]))) {
            boolean enabled = "on".equalsIgnoreCase(args[0]);
            EarthBoundarySavedData.get(world).setRuntimeEnabled(enabled);
            msg(sender, "Earth boundary enforcement is now " + (enabled ? "enabled" : "disabled") + ".");
            return;
        }
        if (args.length == 1 && "status".equalsIgnoreCase(args[0])) {
            EarthBoundarySavedData data = EarthBoundarySavedData.get(world);
            msg(sender, "Earth boundary effective state: " + (EarthBoundaryManager.isBoundaryEnabled(world) ? "enabled" : "disabled") + ".");
            msg(sender, "Center X=" + XFConfig.earthBoundaryCenterX + ", center Z=" + XFConfig.earthBoundaryCenterZ + ".");
            msg(sender, "X radius=" + XFConfig.earthBoundaryRadiusX + ", Z radius=" + XFConfig.earthBoundaryRadiusZ + ", safety margin=" + XFConfig.earthBoundarySafetyMargin + ".");
            msg(sender, "Exemption regions: " + data.getRegions().size() + ".");
            return;
        }
        if (args.length == 1 && "wand".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof EntityPlayerMP)) { error(sender, "The world border exemption wand can only be given to an in-game player."); return; }
            EntityPlayerMP player = (EntityPlayerMP)sender;
			if (AdminSelectionManager.get(player) == null) msg(sender, AdminSelectionManager.begin(player, Type.BORDER_EXEMPT, ""));
			else ItemWorldBorderWand.giveIfNeeded(player);
            return;
        }
        if (args.length == 1 && "exempt".equalsIgnoreCase(args[0])) {
            exempt(sender, world);
            return;
        }
        if (args.length == 1 && "clearexemptions".equalsIgnoreCase(args[0])) {
            clearExemptions(sender, world);
            return;
        }
        error(sender, "Usage: " + USAGE);
    }

    private static void exempt(ICommandSender sender, World world) {
        if (!(sender instanceof EntityPlayerMP)) { error(sender, "Creating an exemption requires an in-game administrator with a wand selection."); return; }
        EntityPlayerMP player = (EntityPlayerMP)sender;
        AdminSelectionManager.Selection selection = AdminSelectionManager.get(player);
        String result = AdminSelectionManager.commit(player, Type.BORDER_EXEMPT);
        if (!result.startsWith("Committed ")) { error(sender, result); return; }
        msg(sender, "Created world border exemption: dimension=" + selection.dimension
                + ", X=" + Math.min(selection.ax, selection.bx) + ".." + Math.max(selection.ax, selection.bx)
                + ", Z=" + Math.min(selection.az, selection.bz) + ".." + Math.max(selection.az, selection.bz) + ".");
    }

    private static void clearExemptions(ICommandSender sender, World world) {
        EarthBoundarySavedData data = EarthBoundarySavedData.get(world);
        if (data.getRegions().isEmpty()) { msg(sender, "There are no world border exemptions to clear."); return; }
        List<EntityPlayerMP> affected = new ArrayList<EntityPlayerMP>();
        for (Object object : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (object instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP)object;
                if (EarthBoundaryManager.isPositionExempt(player.worldObj, player.posX, player.posZ)) affected.add(player);
            }
        }
        int removed = data.clearRegions();
        CommonEventHandler.returnClearedExemptionPlayers(affected);
        msg(sender, "Cleared " + removed + " world border exemption" + (removed == 1 ? "" : "s") + ".");
    }
    private static void msg(ICommandSender sender, String text) { sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + text)); }
    private static void error(ICommandSender sender, String text) { sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + text)); }
}
