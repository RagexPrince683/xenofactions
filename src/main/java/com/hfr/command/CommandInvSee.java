package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.server.MinecraftServer;

/**
 * Command that opens a chest GUI showing the target's live main inventory (36 slots).
 * Taking/moving items removes them from the target immediately because InvSeeInventory
 * delegates directly to the target InventoryPlayer.
 */
public class CommandInvSee extends CommandBase {

    @Override
    public String getCommandName() {
        return "invsee";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/invsee <player> <OPTIONAL armorslots>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP only
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length < 1)
            throw new WrongUsageException(getCommandUsage(sender));

        final EntityPlayerMP viewer = getCommandSenderAsPlayer(sender);

        final EntityPlayerMP target = MinecraftServer.getServer()
                .getConfigurationManager()
                .func_152612_a(args[0]);

        if (target == null)
            throw new PlayerNotFoundException();

        // If second arg is "armorslots", open armor GUI
        if (args.length >= 2 && "armorslots".equalsIgnoreCase(args[1])) {
            viewer.closeContainer();
            final InvSeeArmorInventory armorInv = new InvSeeArmorInventory(target);
            viewer.displayGUIChest(armorInv);
            return;
        }

        // Default behavior: open main 36-slot live inv (existing code)
        // Create a wrapper inventory that delegates to the target's real inventory (36 slots)
        final InvSeeInventory inv = new InvSeeInventory(target);

        viewer.closeContainer();
        viewer.displayGUIChest(inv);
    }
}