package com.hfr.data;

import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.command.CommandClowderAdmin;
import com.hfr.config.XFConfig;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.hfr.clowder.Clowder.initializeDiplomacy;

public class ClowderData extends WorldSavedData {

	private final Set<UUID> claimedPlayers = new HashSet<UUID>();
    private final java.util.Map<String, com.hfr.shop.FactionMarketRegistry.Registration> marketTerminals =
        new java.util.HashMap<String, com.hfr.shop.FactionMarketRegistry.Registration>();

    public com.hfr.shop.FactionMarketRegistry.Registration getMarketTerminal(String factionId) { return marketTerminals.get(factionId); }
    public void setMarketTerminal(String factionId, com.hfr.shop.FactionMarketRegistry.Registration terminal) {
        marketTerminals.put(factionId, terminal); markDirty();
    }
    public void clearMarketTerminal(String factionId) { if (marketTerminals.remove(factionId) != null) markDirty(); }

	public ClowderData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		
		Clowder.readFromNBT(nbt);
		ClowderTerritory.readFromNBT(nbt);
        for (Clowder faction : Clowder.clowders) if (faction.migrateCapital()) markDirty();
        marketTerminals.clear();
        NBTTagList terminals = nbt.getTagList("FactionMarketTerminals", 10);
        for (int i = 0; i < terminals.tagCount(); i++) {
            NBTTagCompound tag = terminals.getCompoundTagAt(i);
            com.hfr.shop.FactionMarketRegistry.Registration registration = com.hfr.shop.FactionMarketRegistry.Registration.read(tag);
            if (registration != null && !marketTerminals.containsKey(tag.getString("factionId")))
                marketTerminals.put(tag.getString("factionId"), registration);
        }
		for(Clowder clowder : Clowder.clowders) {
			clowder.reconcileCitiesFounded(null);
			if(clowder.buildGraceUntil > System.currentTimeMillis() && !clowder.hasValidBuildGraceHome())
				clowder.buildGraceUntil = 0L;
		}

		// Load claimed players
		NBTTagList claimedList = nbt.getTagList("ClaimedPlayers", 8); // 8 = String
		claimedPlayers.clear();
		for (int i = 0; i < claimedList.tagCount(); i++) {
			try { claimedPlayers.add(UUID.fromString(claimedList.getStringTagAt(i))); } catch (IllegalArgumentException ignored) { }
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		Clowder.writeToNBT(nbt);
		ClowderTerritory.writeToNBT(nbt);
        NBTTagList terminals = new NBTTagList();
        for (java.util.Map.Entry<String, com.hfr.shop.FactionMarketRegistry.Registration> entry : marketTerminals.entrySet()) {
            NBTTagCompound tag = entry.getValue().write(); tag.setString("factionId", entry.getKey()); terminals.appendTag(tag);
        }
        nbt.setTag("FactionMarketTerminals", terminals);

		// Save claimed players
		NBTTagList claimedList = new NBTTagList();
		for (UUID playerUuid : claimedPlayers)
			claimedList.appendTag(new NBTTagString(playerUuid.toString()));
		nbt.setTag("ClaimedPlayers", claimedList);

	}

	// Check if a player has claimed a flag
	public boolean hasPlayerClaimedFlag(UUID playerUuid) {
		return claimedPlayers.contains(playerUuid);
	}

	// Mark a player as having claimed a flag
	public void markPlayerClaimedFlag(UUID playerUuid) {
		if (!claimedPlayers.contains(playerUuid)) {
			claimedPlayers.add(playerUuid);
			markDirty(); // Mark data as dirty to save it
		}
	}

	
	private static ClowderData data = null;
	private static World storageOwner = null;

	/** Clears every process-static value which belongs to one Minecraft save. */
	public static void resetWorldState() {
		data = null;
		storageOwner = null;
		Clowder.resetWorldState();
		ClowderTerritory.resetWorldState();
		CommandClowderAdmin.WARENABLED = XFConfig.warEnabledDefault;
		CommandClowderAdmin.WAR_COOLDOWNS_DISABLED = false;
		CommandClowderAdmin.WAR_ONLINE_CHECK_DISABLED = false;
		CommandClowderAdmin.WAR_STATE_CHECK_DISABLED = false;
		CommandClowderAdmin.LEGACY_WAR_ENABLED = false;
		com.hfr.clowder.ClowderEvents.resetWorldState();
		com.hfr.clowder.PlayerProtectionData.resetWorldState();
		com.hfr.clowder.FactionCreationCooldownData.resetWorldState();
		com.hfr.journeymap.ClaimOverlaySync.resetWorldState();
		com.hfr.dynmap.XFDynmapIntegration.markDirty();
        com.hfr.shop.XShopService.clearSessions();
	}

	public static void release(World world) {
		if(world != null && world == storageOwner)
			resetWorldState();
	}
	
	public static ClowderData getData(World worldObj) {

		//initializeDiplomacy(worldObj);
		//there's no way this works but let's try it
		if(worldObj == null)
			return null;

		World storageWorld = worldObj;
		if(worldObj.provider != null && worldObj.provider.dimensionId != 0) {
			World overworld = DimensionManager.getWorld(0);
			if(overworld != null)
				storageWorld = overworld;
		}

		if(storageOwner != storageWorld) {
			resetWorldState();
			storageOwner = storageWorld;
		}
		if(data != null)
			return data;

		data = (ClowderData)storageWorld.perWorldStorage.loadData(ClowderData.class, "hfr_clowder");
		if(data == null) {
			data = new ClowderData("hfr_clowder");
			storageWorld.perWorldStorage.setData("hfr_clowder", data);
			data.markDirty();
		}

		return data;
	}

}
