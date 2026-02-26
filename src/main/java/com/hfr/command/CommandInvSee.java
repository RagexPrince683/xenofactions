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
        return "/invsee <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP only
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length != 1)
            throw new WrongUsageException(getCommandUsage(sender));

        final EntityPlayerMP viewer = getCommandSenderAsPlayer(sender);

        final EntityPlayerMP target = MinecraftServer.getServer()
                .getConfigurationManager()
                .func_152612_a(args[0]);

        if (target == null)
            throw new PlayerNotFoundException();

        // Close any existing container
        viewer.closeContainer();

        // Create a wrapper inventory that delegates to the target's real inventory
        final InvSeeInventory inv = new InvSeeInventory(target);

        /*
         * Use displayGUIChest(IInventory). In 1.7.10 this creates a ContainerChest
         * and opens a chest GUI for the viewer that matches a vanilla chest window.
         * Because InvSeeInventory delegates to target.inventory, all edits operate
         * on the target's inventory live.
         */
        viewer.displayGUIChest(inv);

        // viewer.openContainer is now the ContainerChest created by displayGUIChest
        // No manual packet sending required.
    }
}