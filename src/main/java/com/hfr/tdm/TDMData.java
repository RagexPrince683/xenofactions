package com.hfr.tdm;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TDMData extends WorldSavedData {

    public static final String DATA_NAME = "xenofactions_tdm";

    public boolean enabled = false;
    public final List<TDMManager.SpawnPoint> spawns = new ArrayList<TDMManager.SpawnPoint>();
    public final Map<String, TDMManager.Team> playerTeams = new HashMap<String, TDMManager.Team>();

    public TDMData(String name) {
        super(name);
    }

    public static TDMData get(World world) {
        World storageWorld = getStorageWorld(world);
        MapStorage storage = storageWorld.mapStorage;
        TDMData data = (TDMData) storage.loadData(TDMData.class, DATA_NAME);

        if (data == null) {
            data = new TDMData(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }

        return data;
    }

    private static World getStorageWorld(World world) {
        World overworld = DimensionManager.getWorld(0);
        if (overworld != null) {
            return overworld;
        }

        return world;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        enabled = nbt.getBoolean("enabled");
        spawns.clear();
        playerTeams.clear();

        int spawnCount = nbt.getInteger("spawnCount");
        for (int i = 0; i < spawnCount; i++) {
            NBTTagCompound spawnTag = nbt.getCompoundTag("spawn" + i);
            TDMManager.Team team = TDMManager.Team.fromName(spawnTag.getString("team"));
            if (team == null) {
                continue;
            }

            spawns.add(new TDMManager.SpawnPoint(
                    team,
                    spawnTag.getInteger("dim"),
                    spawnTag.getInteger("x"),
                    spawnTag.getInteger("y"),
                    spawnTag.getInteger("z")
            ));
        }

        int playerCount = nbt.getInteger("playerCount");
        for (int i = 0; i < playerCount; i++) {
            String player = nbt.getString("player" + i);
            TDMManager.Team team = TDMManager.Team.fromName(nbt.getString("playerTeam" + i));
            if (player.length() > 0 && team != null) {
                playerTeams.put(player.toLowerCase(), team);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("enabled", enabled);
        nbt.setInteger("spawnCount", spawns.size());

        for (int i = 0; i < spawns.size(); i++) {
            TDMManager.SpawnPoint spawn = spawns.get(i);
            NBTTagCompound spawnTag = new NBTTagCompound();
            spawnTag.setString("team", spawn.team.name);
            spawnTag.setInteger("dim", spawn.dim);
            spawnTag.setInteger("x", spawn.x);
            spawnTag.setInteger("y", spawn.y);
            spawnTag.setInteger("z", spawn.z);
            nbt.setTag("spawn" + i, spawnTag);
        }

        int playerIndex = 0;
        for (Map.Entry<String, TDMManager.Team> entry : playerTeams.entrySet()) {
            nbt.setString("player" + playerIndex, entry.getKey());
            nbt.setString("playerTeam" + playerIndex, entry.getValue().name);
            playerIndex++;
        }
        nbt.setInteger("playerCount", playerIndex);
    }
}
