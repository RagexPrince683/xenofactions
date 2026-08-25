package com.hfr.command;

import java.util.List;

import com.hfr.config.XFConfig;
import com.hfr.items.ItemWorldBorderWand;
import com.hfr.items.ItemWorldBorderWand.Selection;
import com.hfr.items.ModItems;
import com.hfr.saveddata.EarthBoundarySavedData;
import com.hfr.saveddata.EarthBoundarySavedData.Region;
import com.hfr.world.border.EarthBoundaryManager;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

/** Parser for /xc worldborder; all authorization and mutations remain server-side. */
public final class WorldBorderCommandHandler {
    public static final String USAGE = "/xc worldborder <on|off|status|wand|exempt>";
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
            ItemStack wand = new ItemStack(ModItems.world_border_wand);
            if (!player.inventory.addItemStackToInventory(wand)) player.dropPlayerItemWithRandomChoice(wand, false);
            msg(sender, "Given one World Border Exemption Wand. Left-click sets position 1; right-click sets position 2.");
            return;
        }
        if (args.length >= 2 && "exempt".equalsIgnoreCase(args[0])) {
            exempt(sender, world, args);
            return;
        }
        error(sender, "Usage: " + USAGE);
    }

    private static void exempt(ICommandSender sender, World world, String[] args) {
        EarthBoundarySavedData data = EarthBoundarySavedData.get(world);
        if (args.length == 2 && "list".equalsIgnoreCase(args[1])) {
            List<Region> regions = data.getRegions();
            if (regions.isEmpty()) { msg(sender, "There are no world border exemption regions."); return; }
            for (Region r : regions) msg(sender, r.name + ": dimension=" + r.dimension + ", X=" + r.minX + ".." + r.maxX + ", Z=" + r.minZ + ".." + r.maxZ);
            return;
        }
        if (args.length == 3 && "remove".equalsIgnoreCase(args[1])) {
            String name = EarthBoundarySavedData.normalizeName(args[2]);
            if (!EarthBoundarySavedData.isValidName(name)) { error(sender, "Invalid exemption name; use only letters, numbers, _, and -."); return; }
            if (!data.removeRegion(name)) { error(sender, "Unknown world border exemption region: " + name + "."); return; }
            msg(sender, "Removed world border exemption region " + name + "."); return;
        }
        if (args.length == 3 && "add".equalsIgnoreCase(args[1])) {
            if (!(sender instanceof EntityPlayerMP)) { error(sender, "Adding an exemption requires an in-game administrator with a wand selection."); return; }
            EntityPlayerMP player = (EntityPlayerMP)sender;
            String name = EarthBoundarySavedData.normalizeName(args[2]);
            if (!EarthBoundarySavedData.isValidName(name)) { error(sender, "Invalid exemption name; use only letters, numbers, _, and -."); return; }
            if (data.getRegion(name) != null) { error(sender, "A world border exemption named " + name + " already exists."); return; }
            Selection selection = ItemWorldBorderWand.get(player);
            if (selection == null || !selection.hasPos1 || !selection.hasPos2) { error(sender, "Set both wand positions before adding an exemption."); return; }
            if (selection.dimension != player.dimension) { error(sender, "Both positions must be selected in the same dimension."); return; }
            if (!data.addRegion(name, selection.dimension, selection.x1, selection.z1, selection.x2, selection.z2)) { error(sender, "Could not add that exemption region."); return; }
            Region region = data.getRegion(name);
            ItemWorldBorderWand.clear(player);
            msg(sender, "Created " + region.name + ": dimension=" + region.dimension + ", X=" + region.minX + ".." + region.maxX + ", Z=" + region.minZ + ".." + region.maxZ + ".");
            return;
        }
        error(sender, "Usage: /xc worldborder exempt <add <name>|remove <name>|list>");
    }
    private static void msg(ICommandSender sender, String text) { sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + text)); }
    private static void error(ICommandSender sender, String text) { sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + text)); }
}
