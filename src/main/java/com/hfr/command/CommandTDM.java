package com.hfr.command;

import com.hfr.tdm.TDMManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class CommandTDM extends CommandBase {

    @Override
    public String getCommandName() {
        return "tdm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tdm <toggle|friendlyfire <on|off>|autobalance <on|off|now>|addspawn <red|blue>|setteam <player> <red|blue>|clear>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        World world = sender.getEntityWorld();

        if (args[0].equalsIgnoreCase("toggle")) {
            boolean enabled = TDMManager.toggle(world);
            sender.addChatMessage(new ChatComponentText("TDM: " + enabled));
            return;
        }

        if (args[0].equalsIgnoreCase("friendlyfire")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Friendly fire is " + TDMManager.isFriendlyFireEnabled(world) + ". Usage: /tdm friendlyfire <on|off>"));
                return;
            }

            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm friendlyfire <on|off>"));
                return;
            }

            TDMManager.setFriendlyFireEnabled(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("TDM friendly fire damage: " + (enabled.booleanValue() ? "on" : "off")));
            return;
        }

        if (args[0].equalsIgnoreCase("autobalance")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Auto balance is " + TDMManager.isAutoBalanceEnabled(world) + ". Usage: /tdm autobalance <on|off|now>"));
                return;
            }

            if (args[1].equalsIgnoreCase("now")) {
                int moved = TDMManager.balanceTeams(world);
                sender.addChatMessage(new ChatComponentText("TDM team balance complete. Players moved: " + moved));
                return;
            }

            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm autobalance <on|off|now>"));
                return;
            }

            TDMManager.setAutoBalanceEnabled(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("TDM auto balance: " + (enabled.booleanValue() ? "on" : "off")));
            return;
        }

        if (args[0].equalsIgnoreCase("addspawn")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm addspawn <red|blue>"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[1]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[1]));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            TDMManager.addSpawn(
                    world,
                    team,
                    player.dimension,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ
            );

            sender.addChatMessage(new ChatComponentText(
                    "Spawn added for " + team.name + ". Total: " + TDMManager.getSpawnCount(world)
                            + " (red: " + TDMManager.getSpawnCount(world, TDMManager.Team.RED)
                            + ", blue: " + TDMManager.getSpawnCount(world, TDMManager.Team.BLUE) + ")"
            ));
            return;
        }

        if (args[0].equalsIgnoreCase("setteam")) {
            if (args.length < 3) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm setteam <player> <red|blue>"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[2]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[2]));
                return;
            }

            TDMManager.setPlayerTeam(world, args[1], team);
            sender.addChatMessage(new ChatComponentText(args[1] + " assigned to " + team.name));
            return;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            TDMManager.clearSpawns(world);
            sender.addChatMessage(new ChatComponentText("TDM spawns cleared"));
            return;
        }

        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
    }

    private Boolean parseToggle(String value) {
        if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("enabled")) {
            return Boolean.TRUE;
        }

        if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("disabled")) {
            return Boolean.FALSE;
        }

        return null;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
