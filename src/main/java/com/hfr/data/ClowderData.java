package com.hfr.data;

import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.ArrayList;

public class ClowderData extends WorldSavedData {

	private final ArrayList claimedPlayers = new ArrayList();

	public ClowderData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		
		Clowder.readFromNBT(nbt);
		ClowderTerritory.readFromNBT(nbt);

		// Load claimed players
		NBTTagList claimedList = nbt.getTagList("ClaimedPlayers", 8); // 8 = String
		claimedPlayers.clear();
		for (int i = 0; i < claimedList.tagCount(); i++) {
			claimedPlayers.add(claimedList.getStringTagAt(i));
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		Clowder.writeToNBT(nbt);
		ClowderTerritory.writeToNBT(nbt);

		// Save claimed players
		NBTTagList claimedList = new NBTTagList();
		for (int i = 0; i < claimedPlayers.size(); i++) {
			claimedList.appendTag(new NBTTagString((String) claimedPlayers.get(i)));
		}
		nbt.setTag("ClaimedPlayers", claimedList);

	}

	// Check if a player has claimed a flag
	public boolean hasPlayerClaimedFlag(String playerName) {
		return claimedPlayers.contains(playerName);
	}

	// Mark a player as having claimed a flag
	public void markPlayerClaimedFlag(String playerName) {
		if (!claimedPlayers.contains(playerName)) {
			claimedPlayers.add(playerName);
			markDirty(); // Mark data as dirty to save it
		}
	}

	
	static ClowderData data = null;
	
	public static ClowderData getData(World worldObj) {
		
		if(worldObj.provider.dimensionId == 0) {
	
			data = (ClowderData)worldObj.perWorldStorage.loadData(ClowderData.class, "hfr_clowder");
		    if(data == null) {
		        worldObj.perWorldStorage.setData("hfr_clowder", new ClowderData("hfr_clowder"));
		        
		        data = (ClowderData)worldObj.perWorldStorage.loadData(ClowderData.class, "hfr_clowder");
		    }
		    
		    return data;
		}
		
	    return null;
	}

}
