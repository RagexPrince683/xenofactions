package com.hfr.tdm;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMKitGuiPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TDMManager {

    public static boolean tdmEnabled = false;
    private static final Set<String> pendingKitSelection = new HashSet<String>();

    public enum Team {
        RED("red"),
        BLUE("blue");

        public final String name;

        Team(String name) {
            this.name = name;
        }

        public static Team fromName(String name) {
            if (name == null) {
                return null;
            }

            for (Team team : values()) {
                if (team.name.equalsIgnoreCase(name)) {
                    return team;
                }
            }

            return null;
        }
    }

    public static class SpawnPoint {
        public final Team team;
        public final int dim;
        public final int x, y, z;

        public SpawnPoint(Team team, int dim, int x, int y, int z) {
            this.team = team;
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static void init() {
        tdmEnabled = false;
        pendingKitSelection.clear();
    }

    public static boolean isEnabled(World world) {
        TDMData data = TDMData.get(world);
        tdmEnabled = data.enabled;
        return data.enabled;
    }

    public static boolean toggle(World world) {
        TDMData data = TDMData.get(world);
        data.enabled = !data.enabled;
        tdmEnabled = data.enabled;
        data.markDirty();
        return data.enabled;
    }

    public static boolean isFriendlyFireEnabled(World world) {
        return TDMData.get(world).friendlyFireEnabled;
    }

    public static void setFriendlyFireEnabled(World world, boolean enabled) {
        TDMData data = TDMData.get(world);
        data.friendlyFireEnabled = enabled;
        data.markDirty();
    }

    public static boolean isAutoBalanceEnabled(World world) {
        return TDMData.get(world).autoBalanceEnabled;
    }

    public static void setAutoBalanceEnabled(World world, boolean enabled) {
        TDMData data = TDMData.get(world);
        data.autoBalanceEnabled = enabled;
        data.markDirty();
    }

    public static void addSpawn(World world, Team team, int dim, int x, int y, int z) {
        TDMData data = TDMData.get(world);
        data.spawns.add(new SpawnPoint(team, dim, x, y, z));
        data.markDirty();
    }

    public static void clearSpawns(World world) {
        TDMData data = TDMData.get(world);
        data.spawns.clear();
        data.markDirty();
    }

    public static int getSpawnCount(World world) {
        return TDMData.get(world).spawns.size();
    }

    public static int getSpawnCount(World world, Team team) {
        int count = 0;
        for (SpawnPoint spawn : TDMData.get(world).spawns) {
            if (spawn.team == team) {
                count++;
            }
        }
        return count;
    }

    public static void setPlayerTeam(World world, String playerName, Team team) {
        TDMData data = TDMData.get(world);
        data.playerTeams.put(playerName.toLowerCase(), team);
        data.markDirty();
    }

    public static Team getPlayerTeam(World world, String playerName) {
        return TDMData.get(world).playerTeams.get(playerName.toLowerCase());
    }

    public static Team getOrAssignPlayerTeam(EntityPlayer player) {
        TDMData data = TDMData.get(player.worldObj);
        String playerName = player.getCommandSenderName().toLowerCase();
        Team team = data.playerTeams.get(playerName);

        if (team != null) {
            return team;
        }

        team = getSmallestTeam(data);
        data.playerTeams.put(playerName, team);
        data.markDirty();
        return team;
    }

    private static Team getSmallestTeam(TDMData data) {
        int red = 0;
        int blue = 0;
        List<EntityPlayerMP> onlinePlayers = getOnlinePlayers();

        if (!onlinePlayers.isEmpty()) {
            for (EntityPlayerMP player : onlinePlayers) {
                Team team = data.playerTeams.get(player.getCommandSenderName().toLowerCase());
                if (team == Team.RED) {
                    red++;
                } else if (team == Team.BLUE) {
                    blue++;
                }
            }
        } else {
            for (Team team : data.playerTeams.values()) {
                if (team == Team.RED) {
                    red++;
                } else if (team == Team.BLUE) {
                    blue++;
                }
            }
        }

        return red <= blue ? Team.RED : Team.BLUE;
    }

    public static int balanceTeams(World world) {
        TDMData data = TDMData.get(world);
        if (!data.enabled) {
            return 0;
        }

        List<EntityPlayerMP> onlinePlayers = getOnlinePlayers();
        if (onlinePlayers.size() < 2) {
            return 0;
        }

        boolean assigned = false;
        for (EntityPlayerMP player : onlinePlayers) {
            String playerName = player.getCommandSenderName().toLowerCase();
            if (!data.playerTeams.containsKey(playerName)) {
                data.playerTeams.put(playerName, getSmallestTeam(data));
                assigned = true;
            }
        }

        int moved = 0;
        while (Math.abs(getOnlineTeamCount(data, Team.RED) - getOnlineTeamCount(data, Team.BLUE)) > 1) {
            Team larger = getOnlineTeamCount(data, Team.RED) > getOnlineTeamCount(data, Team.BLUE) ? Team.RED : Team.BLUE;
            Team smaller = larger == Team.RED ? Team.BLUE : Team.RED;
            EntityPlayerMP playerToMove = getLastOnlinePlayerOnTeam(data, larger);
            if (playerToMove == null) {
                break;
            }

            data.playerTeams.put(playerToMove.getCommandSenderName().toLowerCase(), smaller);
            pendingKitSelection.remove(getPlayerKey(playerToMove));
            playerToMove.addChatMessage(new ChatComponentText("You were moved to " + smaller.name + " to balance TDM teams."));
            promptForKit(playerToMove);
            moved++;
        }

        if (assigned || moved > 0) {
            data.markDirty();
        }

        return moved;
    }

    private static int getOnlineTeamCount(TDMData data, Team team) {
        int count = 0;
        for (EntityPlayerMP player : getOnlinePlayers()) {
            if (data.playerTeams.get(player.getCommandSenderName().toLowerCase()) == team) {
                count++;
            }
        }
        return count;
    }

    private static EntityPlayerMP getLastOnlinePlayerOnTeam(TDMData data, Team team) {
        List<EntityPlayerMP> onlinePlayers = getOnlinePlayers();
        for (int i = onlinePlayers.size() - 1; i >= 0; i--) {
            EntityPlayerMP player = onlinePlayers.get(i);
            if (data.playerTeams.get(player.getCommandSenderName().toLowerCase()) == team) {
                return player;
            }
        }
        return null;
    }

    private static List<EntityPlayerMP> getOnlinePlayers() {
        List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) {
            return players;
        }

        for (Object player : server.getConfigurationManager().playerEntityList) {
            if (player instanceof EntityPlayerMP) {
                players.add((EntityPlayerMP) player);
            }
        }
        return players;
    }

    public static void promptForKit(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        Team team = getOrAssignPlayerTeam(player);
        if (TDMKitManager.getKitCount(team) <= 0) {
            player.addChatMessage(new net.minecraft.util.ChatComponentText("No TDM kits have been saved for " + team.name + ". Ask an admin to use /kit " + team.name + "."));
            return;
        }

        pendingKitSelection.add(getPlayerKey(player));
        PacketDispatcher.wrapper.sendTo(new TDMKitGuiPacket(team.name, TDMKitManager.getKitNames(team)), (EntityPlayerMP) player);
    }

    public static void tickKitSelection(EntityPlayer player) {
        if (!pendingKitSelection.contains(getPlayerKey(player))) {
            return;
        }

        if (!isEnabled(player.worldObj)) {
            pendingKitSelection.remove(getPlayerKey(player));
            return;
        }

        player.addPotionEffect(new PotionEffect(Potion.resistance.id, 40, 4, true));
        player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 40, 4, true));
    }

    public static boolean selectKit(EntityPlayer player, int kitIndex) {
        if (!pendingKitSelection.contains(getPlayerKey(player))) {
            return false;
        }

        if (!isEnabled(player.worldObj)) {
            pendingKitSelection.remove(getPlayerKey(player));
            return false;
        }

        Team team = getOrAssignPlayerTeam(player);
        if (!TDMKitManager.applyKit(team, kitIndex, player)) {
            return false;
        }

        pendingKitSelection.remove(getPlayerKey(player));
        player.removePotionEffect(Potion.resistance.id);
        player.removePotionEffect(Potion.regeneration.id);
        return true;
    }

    private static String getPlayerKey(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }

    public static boolean respawnPlayer(EntityPlayer player, Random rand) {
        SpawnPoint spawn = getRandomSpawn(player, rand);
        if (spawn == null) {
            return false;
        }

        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            playerMP.playerNetServerHandler.setPlayerLocation(
                    spawn.x + 0.5,
                    spawn.y,
                    spawn.z + 0.5,
                    playerMP.rotationYaw,
                    playerMP.rotationPitch
            );
        } else {
            player.setPositionAndUpdate(
                    spawn.x + 0.5,
                    spawn.y,
                    spawn.z + 0.5
            );
        }

        return true;
    }

    public static SpawnPoint getRandomSpawn(EntityPlayer player, Random rand) {
        Team team = getOrAssignPlayerTeam(player);
        return getRandomSpawn(player.worldObj, team, rand);
    }

    public static SpawnPoint getRandomSpawn(World world, Team team, Random rand) {
        List<SpawnPoint> valid = new ArrayList<SpawnPoint>();
        for (SpawnPoint spawn : TDMData.get(world).spawns) {
            if (spawn.team == team && spawn.dim == world.provider.dimensionId) {
                valid.add(spawn);
            }
        }

        if (valid.isEmpty()) {
            return null;
        }

        return valid.get(rand.nextInt(valid.size()));
    }
}
