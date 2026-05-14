package com.hfr.command;

import com.hfr.tdm.TDMManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.Random;

public class CommandTeamChange extends CommandBase {

    @Override
    public String getCommandName() {
        return "teamchange";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/teamchange";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        if (!TDMManager.isEnabled(player.worldObj)) {
            sender.addChatMessage(new ChatComponentText("TDM is not enabled."));
            return;
        }

        TDMManager.Team currentTeam = TDMManager.getOrAssignPlayerTeam(player);
        TDMManager.Team newTeam = currentTeam == TDMManager.Team.RED ? TDMManager.Team.BLUE : TDMManager.Team.RED;
        TDMManager.setPlayerTeam(player.worldObj, player.getCommandSenderName(), newTeam);
        sender.addChatMessage(new ChatComponentText("You changed to the " + newTeam.name + " TDM team."));

        if (!TDMManager.respawnPlayer(player, new Random())) {
            sender.addChatMessage(new ChatComponentText("No spawn is available for your new team on this map."));
        }
        TDMManager.promptForKit(player);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
