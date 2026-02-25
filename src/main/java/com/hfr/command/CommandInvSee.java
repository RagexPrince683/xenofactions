package com.hfr.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.server.MinecraftServer;

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
        return 2;
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

        final InventoryBasic chest = new InventoryBasic(
                target.getCommandSenderName() + "'s Inventory",
                false,
                36
        );

        // MOVE stacks into chest (not copy reference blindly)
        for (int i = 0; i < 36; i++) {
            chest.setInventorySlotContents(i,
                    target.inventory.getStackInSlot(i) == null ? null :
                            target.inventory.getStackInSlot(i).copy());
        }

        viewer.displayGUIChest(chest);

        // Grab the container that was just opened
        final net.minecraft.inventory.Container container = viewer.openContainer;

        container.addCraftingToCrafters(viewer);

        // Hook close properly
        viewer.openContainer = new net.minecraft.inventory.ContainerChest(viewer.inventory, chest) {

            @Override
            public void onContainerClosed(net.minecraft.entity.player.EntityPlayer player) {
                super.onContainerClosed(player);

                for (int i = 0; i < 36; i++) {
                    target.inventory.setInventorySlotContents(i,
                            chest.getStackInSlot(i) == null ? null :
                                    chest.getStackInSlot(i).copy());
                }

                target.inventory.markDirty();
            }
        };
    }
}