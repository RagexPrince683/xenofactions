package com.hfr.tdm;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TDMData extends WorldSavedData {

    public static final String DATA_NAME = "xenofactions_tdm";

    public boolean enabled = false;
    public boolean friendlyFireEnabled = true;
    public boolean autoBalanceEnabled = true;
    public String selectedMap = "";
    public int redScore = 0;
    public int blueScore = 0;
    public int redBombWins = 0;
    public int blueBombWins = 0;
    public int redBombLosses = 0, blueBombLosses = 0;
    public long roundEndTick = 0;
    public boolean mapVoteActive = false;
    public long mapVoteEndTick = 0;
    public final List<TDMManager.SpawnPoint> spawns = new ArrayList<TDMManager.SpawnPoint>();
    public final Map<String, TDMManager.TDMMap> maps = new LinkedHashMap<String, TDMManager.TDMMap>();
    public final Map<String, TDMManager.Team> playerTeams = new HashMap<String, TDMManager.Team>();
    public final Map<String, String> mapVotes = new HashMap<String, String>();
    public final Map<String, Integer> playerKills = new HashMap<String, Integer>();
    public final Map<String, Integer> playerDeaths = new HashMap<String, Integer>();

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
        friendlyFireEnabled = !nbt.hasKey("friendlyFireEnabled") || nbt.getBoolean("friendlyFireEnabled");
        autoBalanceEnabled = !nbt.hasKey("autoBalanceEnabled") || nbt.getBoolean("autoBalanceEnabled");
        selectedMap = nbt.hasKey("selectedMap") ? nbt.getString("selectedMap").toLowerCase() : "";
        redScore = nbt.getInteger("redScore");
        blueScore = nbt.getInteger("blueScore");
        redBombWins=nbt.getInteger("redBombWins"); blueBombWins=nbt.getInteger("blueBombWins");redBombLosses=nbt.getInteger("redBombLosses");blueBombLosses=nbt.getInteger("blueBombLosses");
        roundEndTick = nbt.hasKey("roundEndTick") ? nbt.getLong("roundEndTick") : 0;
        mapVoteActive = nbt.getBoolean("mapVoteActive");
        mapVoteEndTick = nbt.hasKey("mapVoteEndTick") ? nbt.getLong("mapVoteEndTick") : 0;
        spawns.clear();
        maps.clear();
        playerTeams.clear();
        mapVotes.clear();
        playerKills.clear();
        playerDeaths.clear();

        int spawnCount = nbt.getInteger("spawnCount");
        for (int i = 0; i < spawnCount; i++) {
            NBTTagCompound spawnTag = nbt.getCompoundTag("spawn" + i);
            TDMManager.SpawnPoint spawn = readSpawn(spawnTag);
            if (spawn != null) {
                spawns.add(spawn);
            }
        }

        int mapCount = nbt.getInteger("mapCount");
        for (int i = 0; i < mapCount; i++) {
            NBTTagCompound mapTag = nbt.getCompoundTag("map" + i);
            String mapName = mapTag.getString("name").toLowerCase();
            if (mapName.length() == 0) {
                continue;
            }

            TDMManager.TDMMap map = new TDMManager.TDMMap(mapName);
            map.scoreLimitOverride = mapTag.hasKey("scoreLimit") ? Math.max(0, mapTag.getInteger("scoreLimit")) : 0;
            map.roundTicksOverride = mapTag.hasKey("roundTicks") ? Math.max(0, mapTag.getInteger("roundTicks")) : 0;
            try { map.mode=TDMManager.TDMGameMode.valueOf(mapTag.hasKey("mode")?mapTag.getString("mode"):"DEATHMATCH"); } catch(IllegalArgumentException e){map.mode=TDMManager.TDMGameMode.DEATHMATCH;}
            TDMManager.Team terrorist=TDMManager.Team.fromName(mapTag.hasKey("terroristTeam")?mapTag.getString("terroristTeam"):"red");map.terroristTeam=terrorist==null?TDMManager.Team.RED:terrorist;
            map.hardcoreRespawns=mapTag.hasKey("hardcoreRespawns")&&mapTag.getBoolean("hardcoreRespawns");
            map.bombScoreLimitOverride=mapTag.hasKey("bombScoreLimit")?Math.max(0,mapTag.getInteger("bombScoreLimit")):0;
            map.bombRoundTicksOverride=mapTag.hasKey("bombRoundTicks")?Math.max(0,mapTag.getInteger("bombRoundTicks")):0;
            readBombsite(mapTag,"bombsiteA",map.bombsiteA);readBombsite(mapTag,"bombsiteB",map.bombsiteB);
            int mapSpawnCount = mapTag.getInteger("spawnCount");
            for (int j = 0; j < mapSpawnCount; j++) {
                TDMManager.SpawnPoint spawn = readSpawn(mapTag.getCompoundTag("spawn" + j));
                if (spawn != null) {
                    map.spawns.add(spawn);
                }
            }
            maps.put(map.name, map);
        }

        if (selectedMap.length() > 0 && !maps.containsKey(selectedMap)) {
            selectedMap = "";
        }

        int playerCount = nbt.getInteger("playerCount");
        for (int i = 0; i < playerCount; i++) {
            String player = nbt.getString("player" + i);
            TDMManager.Team team = TDMManager.Team.fromName(nbt.getString("playerTeam" + i));
            if (player.length() > 0 && team != null) {
                playerTeams.put(player.toLowerCase(), team);
            }
        }


        int statCount = nbt.getInteger("statCount");
        for (int i = 0; i < statCount; i++) {
            String player = nbt.getString("statPlayer" + i).toLowerCase();
            if (player.length() == 0) {
                continue;
            }
            playerKills.put(player, Integer.valueOf(nbt.getInteger("statKills" + i)));
            playerDeaths.put(player, Integer.valueOf(nbt.getInteger("statDeaths" + i)));
        }

        int voteCount = nbt.getInteger("voteCount");
        for (int i = 0; i < voteCount; i++) {
            String player = nbt.getString("votePlayer" + i).toLowerCase();
            String map = nbt.getString("voteMap" + i).toLowerCase();
            if (player.length() > 0 && maps.containsKey(map)) {
                mapVotes.put(player, map);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("enabled", enabled);
        nbt.setBoolean("friendlyFireEnabled", friendlyFireEnabled);
        nbt.setBoolean("autoBalanceEnabled", autoBalanceEnabled);
        nbt.setString("selectedMap", selectedMap);
        nbt.setInteger("redScore", redScore);
        nbt.setInteger("blueScore", blueScore);
        nbt.setInteger("redBombWins",redBombWins);nbt.setInteger("blueBombWins",blueBombWins);nbt.setInteger("redBombLosses",redBombLosses);nbt.setInteger("blueBombLosses",blueBombLosses);
        nbt.setLong("roundEndTick", roundEndTick);
        nbt.setBoolean("mapVoteActive", mapVoteActive);
        nbt.setLong("mapVoteEndTick", mapVoteEndTick);
        nbt.setInteger("spawnCount", spawns.size());

        for (int i = 0; i < spawns.size(); i++) {
            nbt.setTag("spawn" + i, writeSpawn(spawns.get(i)));
        }

        int mapIndex = 0;
        for (TDMManager.TDMMap map : maps.values()) {
            NBTTagCompound mapTag = new NBTTagCompound();
            mapTag.setString("name", map.name);
            if (map.scoreLimitOverride > 0) mapTag.setInteger("scoreLimit", map.scoreLimitOverride);
            if (map.roundTicksOverride > 0) mapTag.setInteger("roundTicks", map.roundTicksOverride);
            mapTag.setString("mode",map.mode.name());mapTag.setString("terroristTeam",map.terroristTeam.name);mapTag.setBoolean("hardcoreRespawns",map.hardcoreRespawns);
            if(map.bombScoreLimitOverride>0)mapTag.setInteger("bombScoreLimit",map.bombScoreLimitOverride);if(map.bombRoundTicksOverride>0)mapTag.setInteger("bombRoundTicks",map.bombRoundTicksOverride);
            writeBombsite(mapTag,"bombsiteA",map.bombsiteA);writeBombsite(mapTag,"bombsiteB",map.bombsiteB);
            mapTag.setInteger("spawnCount", map.spawns.size());
            for (int i = 0; i < map.spawns.size(); i++) {
                mapTag.setTag("spawn" + i, writeSpawn(map.spawns.get(i)));
            }
            nbt.setTag("map" + mapIndex, mapTag);
            mapIndex++;
        }
        nbt.setInteger("mapCount", mapIndex);

        int playerIndex = 0;
        for (Map.Entry<String, TDMManager.Team> entry : playerTeams.entrySet()) {
            nbt.setString("player" + playerIndex, entry.getKey());
            nbt.setString("playerTeam" + playerIndex, entry.getValue().name);
            playerIndex++;
        }
        nbt.setInteger("playerCount", playerIndex);


        int statIndex = 0;
        for (Map.Entry<String, Integer> entry : playerKills.entrySet()) {
            String player = entry.getKey();
            nbt.setString("statPlayer" + statIndex, player);
            nbt.setInteger("statKills" + statIndex, entry.getValue().intValue());
            Integer deaths = playerDeaths.get(player);
            nbt.setInteger("statDeaths" + statIndex, deaths == null ? 0 : deaths.intValue());
            statIndex++;
        }
        nbt.setInteger("statCount", statIndex);

        int voteIndex = 0;
        for (Map.Entry<String, String> entry : mapVotes.entrySet()) {
            nbt.setString("votePlayer" + voteIndex, entry.getKey());
            nbt.setString("voteMap" + voteIndex, entry.getValue());
            voteIndex++;
        }
        nbt.setInteger("voteCount", voteIndex);
    }

    private TDMManager.SpawnPoint readSpawn(NBTTagCompound spawnTag) {
        TDMManager.Team team = TDMManager.Team.fromName(spawnTag.getString("team"));
        if (team == null) {
            return null;
        }

        return new TDMManager.SpawnPoint(
                team,
                spawnTag.getInteger("dim"),
                spawnTag.getInteger("x"),
                spawnTag.getInteger("y"),
                spawnTag.getInteger("z")
        );
    }

    private NBTTagCompound writeSpawn(TDMManager.SpawnPoint spawn) {
        NBTTagCompound spawnTag = new NBTTagCompound();
        spawnTag.setString("team", spawn.team.name);
        spawnTag.setInteger("dim", spawn.dim);
        spawnTag.setInteger("x", spawn.x);
        spawnTag.setInteger("y", spawn.y);
        spawnTag.setInteger("z", spawn.z);
        return spawnTag;
    }

    private void readBombsite(NBTTagCompound parent,String key,TDMManager.Bombsite site){if(!parent.hasKey(key))return;NBTTagCompound t=parent.getCompoundTag(key);site.dimension=t.getInteger("dim");site.hasPos1=t.getBoolean("hasPos1");site.hasPos2=t.getBoolean("hasPos2");site.x1=t.getInteger("x1");site.y1=t.getInteger("y1");site.z1=t.getInteger("z1");site.x2=t.getInteger("x2");site.y2=t.getInteger("y2");site.z2=t.getInteger("z2");}
    private void writeBombsite(NBTTagCompound parent,String key,TDMManager.Bombsite site){if(!site.hasPos1&&!site.hasPos2)return;NBTTagCompound t=new NBTTagCompound();t.setInteger("dim",site.dimension);t.setBoolean("hasPos1",site.hasPos1);t.setBoolean("hasPos2",site.hasPos2);t.setInteger("x1",site.x1);t.setInteger("y1",site.y1);t.setInteger("z1",site.z1);t.setInteger("x2",site.x2);t.setInteger("y2",site.y2);t.setInteger("z2",site.z2);parent.setTag(key,t);}
}
