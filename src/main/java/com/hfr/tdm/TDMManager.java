package com.hfr.tdm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TDMManager {

    public static boolean tdmEnabled = false;

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

        for (Team team : data.playerTeams.values()) {
            if (team == Team.RED) {
                red++;
            } else if (team == Team.BLUE) {
                blue++;
            }
        }

        return red <= blue ? Team.RED : Team.BLUE;
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
