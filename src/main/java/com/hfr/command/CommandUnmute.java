package com.hfr.command;

public class CommandUnmute {

    public String getCommandName() {
        return "xunmute";
    }

    public String getCommandUsage() {
        return "/xunmute <player>";
    }

    public void processCommand(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: " + getCommandUsage());
            return;
        }

        String playerName = args[0];
        // Logic to unmute the player
        MuteManager.unmute(playerName);
        System.out.println("Player " + playerName + " has been unmuted.");
    }
}
