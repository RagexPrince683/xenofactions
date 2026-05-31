package com.hfr.clowder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hfr.data.ClowderData;
import com.hfr.main.MainRegistry;
import com.hfr.tileentity.clowder.ITerritoryProvider;
import com.hfr.tileentity.clowder.TileEntityConquerer;
import com.hfr.tileentity.clowder.TileEntityFlag;

import com.hfr.tileentity.clowder.TileEntityFlagBig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ClowderTerritory {

	//pre-made instances prevent pointless clutter
	//saves RAM, saves nerves
	public static final Ownership SAFEZONE = new Ownership(Zone.SAFEZONE);
	public static final Ownership WARZONE = new Ownership(Zone.WARZONE);
	public static final Ownership WILDERNESS = new Ownership(Zone.WILDERNESS);

	public static final int SAFEZONE_COLOR = 0xFF8000;
	public static final int WARZONE_COLOR = 0xFF0000;
	public static final int WILDERNESS_COLOR = 0xFFFFFF;
	
	public static HashMap<Long, TerritoryMeta> territories = new HashMap();
	
	//the chunk coords in the CodePair wrapper class
	public static CoordPair getCoordPair(int x, int z) {
		
		//why is this necessary? idk but it is
		x += 1;
		z += 1;
		
		return new CoordPair(x / 16, z / 16);
	}
	

	public static boolean canPlaceCityCenter(int chunkX, int chunkZ) {
		return getCityPlacementError(chunkX, chunkZ) == null;
	}

	public static String getCityPlacementError(int chunkX, int chunkZ) {
		for(TerritoryMeta meta : territories.values()) {
			if(meta != null && meta.owner != null && meta.owner.zone == Zone.FACTION && meta.isCityClaim()) {
				CoordPair other = getCoordPair(meta.flagX, meta.flagZ);
				double dist = Math.sqrt(Math.pow(chunkX - other.x, 2) + Math.pow(chunkZ - other.z, 2));
				int required = CityLevel.maxRadius() * 2 + 1;
				if(dist < required)
					return "City Center is too close to " + meta.cityName + ". City centers must be at least " + required + " chunks apart to make sure claims do not overlap.";
			}
		}
		return null;
	}

	public static List<TerritoryMeta> getCityClaims(Clowder owner) {
		HashMap<String, TerritoryMeta> cities = new HashMap();
		for(TerritoryMeta meta : territories.values()) {
			if(meta != null && meta.owner != null && meta.owner.zone == Zone.FACTION && meta.owner.owner == owner && meta.isCityClaim())
				cities.put(meta.cityId, meta);
		}
		return new ArrayList(cities.values());
	}

	public static TerritoryMeta getCityByName(Clowder owner, String cityName) {
		if(cityName == null)
			return null;
		for(TerritoryMeta meta : getCityClaims(owner)) {
			if(meta.cityName != null && meta.cityName.equalsIgnoreCase(cityName))
				return meta;
		}
		return null;
	}

	public static TerritoryMeta getCityByName(String cityName) {
		if(cityName == null)
			return null;
		String trimmed = cityName.trim();
		if(trimmed.isEmpty())
			return null;
		HashMap<String, TerritoryMeta> cities = new HashMap();
		for(TerritoryMeta meta : territories.values()) {
			if(meta != null && meta.owner != null && meta.owner.zone == Zone.FACTION && meta.isCityClaim())
				cities.put(meta.cityId, meta);
		}
		for(TerritoryMeta meta : cities.values()) {
			if(meta.cityName != null && meta.cityName.equalsIgnoreCase(trimmed))
				return meta;
		}
		return null;
	}

	public static boolean isCityNameAvailable(String cityName, TerritoryMeta ignoredCity) {
		TerritoryMeta existing = getCityByName(cityName);
		if(existing == null)
			return true;
		return ignoredCity != null && existing.cityId != null && existing.cityId.equals(ignoredCity.cityId);
	}

	public static int renameClaimsForCity(World world, int fX, int fY, int fZ, String name) {
		String id = fX + "," + fY + "," + fZ;
		int renamed = 0;
		for(TerritoryMeta meta : territories.values()) {
			if(meta != null && id.equals(meta.cityId)) {
				meta.name = name;
				meta.cityName = name;
				renamed++;
			}
		}
		if(renamed > 0 && world != null)
			ClowderData.getData(world).markDirty();
		return renamed;
	}

	public static int removeClaimsForCity(World world, int fX, int fY, int fZ) {
		String id = fX + "," + fY + "," + fZ;
		int removed = 0;
		List<Long> claims = new ArrayList(territories.keySet());
		for(Long code : claims) {
			TerritoryMeta meta = territories.get(code);
			if(meta != null && id.equals(meta.cityId)) {
				territories.remove(code);
				removed++;
			}
		}
		if(removed > 0 && world != null)
			ClowderData.getData(world).markDirty();
		return removed;
	}

	public static int transferCity(World world, TerritoryMeta city, Clowder newOwner) {
		if(city == null || !city.isCityClaim() || newOwner == null)
			return 0;
		String id = city.cityId;
		int moved = 0;
		for(TerritoryMeta meta : territories.values()) {
			if(meta != null && id.equals(meta.cityId) && meta.owner != null && meta.owner.zone == Zone.FACTION) {
				meta.owner.owner = newOwner;
				moved++;
			}
		}
		TileEntity te = world.getTileEntity(city.flagX, city.flagY, city.flagZ);
		if(te instanceof TileEntityFlag) {
			TileEntityFlag flag = (TileEntityFlag)te;
			Clowder oldOwner = flag.owner;
			if(oldOwner != null && oldOwner != newOwner) {
				oldOwner.addPrestigeGen(-flag.getGenRate(), world);
				oldOwner.addPrestigeReq(-flag.getCost(), world);
				oldOwner.flags--;
			}
			flag.owner = newOwner;
			newOwner.addPrestigeGen(flag.getGenRate(), world);
			newOwner.addPrestigeReq(flag.getCost(), world);
			newOwner.flags++;
			flag.markDirty();
		}
		ClowderData.getData(world).markDirty();
		return moved;
	}

	//sets the owner of a chunk to a clowder
	public static void setOwnerForCoord(World world, CoordPair coords, Clowder owner, int fX, int fY, int fZ, String name) {
		
		setOwnerForInts(world, coords.x, coords.z, owner, fX, fY, fZ, name);
	}
	
	//sets the owner of a chunk to a clowder
	public static void setOwnerForInts(World world, int x, int z, Clowder owner, int fX, int fY, int fZ, String name) {
		
		long code = intsToCode(x, z);
		//TerritoryMeta old = territories.get(code);
		
		territories.remove(code);

		//todo check
		Ownership o = new Ownership(Zone.FACTION, owner);
		TerritoryMeta metadata = new TerritoryMeta(o, fX, fY, fZ);
		metadata.name = name;
		metadata.cityName = name;
		metadata.cityId = fX + "," + fY + "," + fZ;
		//fuck this goddamn shithole of a mod
		TileEntity flag = world.getTileEntity(fX, fY, fZ);
		if(flag != null) {
			if(flag instanceof TileEntityFlagBig)
				((TileEntityFlagBig)flag).provinceName = name;
			else if(flag instanceof TileEntityConquerer)
				((TileEntityConquerer)flag).name = name;
		}
		
		territories.put(code, metadata);
		ClowderData.getData(world).markDirty();
	}
	
	//sets the owner of a chunk to a special zone
	public static void setZoneForCoord(World world, CoordPair coords, Zone zone) {
		
		setZoneForInts(world, coords.x, coords.z, zone);
	}
	
	//sets the owner of a chunk to a special zone
	public static void setZoneForInts(World world, int x, int z, Zone zone) {
		
		long code = intsToCode(x, z);
		
		territories.remove(code);
		
		//do not create wilderness k thx
		if(zone != Zone.WILDERNESS) {
			Ownership o = new Ownership(zone, null);
			TerritoryMeta metadata = new TerritoryMeta(o);
			
			territories.put(code, metadata);
		}
		ClowderData.getData(world).markDirty();
	}
	
	//removes territory metadata
	public static void removeZoneForCoord(World world, CoordPair coords) {

		removeZoneForInts(world, coords.x, coords.z);
	}

	//removes territory metadata
	public static void removeZoneForInts(World world, int x, int z) {

		long code = intsToCode(x, z);
		territories.remove(code);
		
		ClowderData.getData(world).markDirty();
	}
	
	//returns the ownership information of the chunk
	public static Ownership getOwnerFromCoords(CoordPair coords) {
		
		return getOwner(coords.x, coords.z);
	}
	
	//returns the ownership information of the chunk
	public static Ownership getOwnerFromInts(int x, int z) {

		z += 1;
		
		return getOwner(x / 16, z / 16);
	}
	
	//returns the ownership information of the chunk
	public static Ownership getOwner(int x, int z) {
		
		long code = intsToCode(x, z);
		
		TerritoryMeta meta = territories.get(code);
		
		if(meta == null)
			return WILDERNESS;
		
		Ownership owner = meta.owner;
		
		if(owner.zone == Zone.FACTION && owner.owner == null)
			return WILDERNESS;
		
		return owner == null ? WILDERNESS : owner;
	}
	
	//returns true if a player is in a clowder and standing in his home territory
	public static boolean isPlayerHome(EntityPlayer player) {
		
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		
		if(clowder == null)
			return false;
		
		Ownership owner = getOwnerFromInts((int)player.posX, (int)player.posZ);
		
		if(owner != null && owner.zone == Zone.FACTION && owner.owner == clowder)
			return true;
		
		return false;
		
	}
	
	//returns the ownership information of the chunk
	public static TerritoryMeta getMetaFromIntCoords(int x, int z) {

		z += 1;
		
		return getMetaFromInts(x / 16, z / 16);
	}
	
	//returns the ownership information of the chunk
	public static TerritoryMeta getMetaFromCoords(CoordPair coords) {
		
		return getMetaFromInts(coords.x, coords.z);
	}
	
	//returns the ownership information of the chunk
	public static TerritoryMeta getMetaFromInts(int x, int z) {
		
		long code = intsToCode(x, z);
		
		TerritoryMeta meta = territories.get(code);
		
		if(meta != null && meta.owner.zone == Zone.FACTION && meta.owner.owner == null)
			meta.owner = WILDERNESS;
		
		if(meta != null && meta.owner == WILDERNESS)
			return null;
		
		return meta;
	}

	//converts the UUID long code into a CoordPair instance
	public static CoordPair codeToCoords(long code) {

		return new CoordPair((int)(code >> 32), (int)code);
	}

	//converts a CoordPair instance into the UUID long code
	public static long coordsToCode(CoordPair coord) {
		try {
			return intsToCode(coord.x, coord.z);
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
	}

	public static long intsToCode(int x, int z) {
		
        return ((long)x & 0xFFFFFFFFL) << 32 | ((long)z & 0xFFFFFFFFL);
	}
	
	public static class Ownership {
		
		public Zone zone;
		public Clowder owner;
		
		public Ownership(Zone zone, Clowder owner) {
			this.zone = zone;
			
			if(zone == Zone.FACTION)
				this.owner = owner;
		}
		
		public Ownership(Zone zone) {
			this.zone = zone;
		}
		
		public void writeToNBT(NBTTagCompound nbt, String code) {
			
			if(zone == Zone.FACTION && owner == null)
				return;
			
			nbt.setInteger("ownership_" + code + "_zone", zone.ordinal());
			
			if(zone == Zone.FACTION)
				nbt.setString("ownership_" + code + "_owner", owner.name);
		}
		
		public static Ownership readFromNBT(NBTTagCompound nbt, String code) {
			
			Zone zone = Zone.values()[nbt.getInteger("ownership_" + code + "_zone")];
			
			Clowder clowder = null;
			
			if(zone == Zone.FACTION) {
				clowder = Clowder.getClowderFromName(nbt.getString("ownership_" + code + "_owner"));
			}
			
			if(zone == Zone.FACTION && clowder == null)
				return WILDERNESS;
			
			Ownership ownership = new Ownership (zone, clowder);
			
			return ownership;
		}
		
		public int getColor() {
				
			switch(zone) {
			case FACTION:
				return owner.color;
			case SAFEZONE:
				return SAFEZONE_COLOR;
			case WARZONE:
				return WARZONE_COLOR;
			case WILDERNESS:
				return WILDERNESS_COLOR;
			
			}
			
			return 0x000000;
		}
	}
	
	public static enum Zone {
		
		//no building, no pvp
		SAFEZONE,
		//no building
		WARZONE,
		//no restrictions
		WILDERNESS,
		//only the owning team can edit this terrain
		//pvp is disabled for team mates (?)
		FACTION
	}
	
	//it's just two integers in a wrapper
	//don't judge me vanilla minecraft does it too since 1.8 just with 3 integers

	//Hey bob, I don't give a shit what vanilla minecraft does
	// why are you basing all your fucking logic on a 10+ year old fucking game you fucking retard?
	public static class CoordPair {
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CoordPair other = (CoordPair) obj;
			if (x != other.x)
				return false;
			if (z != other.z)
				return false;
			return true;
		}

		public int x;
		public int z;
		
		public CoordPair(int x, int z) {
			this.x = x;
			this.z = z;
		}
	}
	
	public static class TerritoryMeta {
		
		public Ownership owner;
		public int flagX;
		public int flagY;
		public int flagZ;
		public String name;
		public String cityId;
		public String cityName;
		public int cityLevel;
		
		public TerritoryMeta(Ownership owner, int flagX, int flagY, int flagZ) {
			this.owner = owner;
			this.flagX = flagX;
			this.flagY = flagY;
			this.flagZ = flagZ;
			this.name = "";
			this.cityId = flagX + "," + flagY + "," + flagZ;
			this.cityName = "";
			this.cityLevel = 0;
		}

		public TerritoryMeta(Ownership owner, int flagX, int flagY, int flagZ, World world, String name) {
			this.owner = owner;
			this.flagX = flagX;
			this.flagY = flagY;
			this.flagZ = flagZ;
			TileEntityFlagBig flag = (TileEntityFlagBig) world.getTileEntity(flagX, flagY, flagZ);
			if(flag != null)
				flag.provinceName = name;
		}
		
		public TerritoryMeta(Ownership owner) {
			this(owner, -1, -1, -1);
		}
		
		public void writeToNBT(NBTTagCompound nbt, String code) {

			//nbt.setInteger("terr_" + code + "_flagX", flagX);
			//nbt.setInteger("terr_" + code + "_flagY", flagY);
			//nbt.setInteger("terr_" + code + "_flagZ", flagZ);

			owner.writeToNBT(nbt, code);
			nbt.setInteger(code + "X",flagX);
			nbt.setInteger(code + "Y",flagY);
			nbt.setInteger(code + "Z",flagZ);
			nbt.setString("name_" + code, name);
			nbt.setString("cityId_" + code, cityId == null ? "" : cityId);
			nbt.setString("cityName_" + code, cityName == null ? "" : cityName);
			nbt.setInteger("cityLevel_" + code, cityLevel);
		}
		
		public static TerritoryMeta readFromNBT(NBTTagCompound nbt, String code) {
			
			TerritoryMeta meta = new TerritoryMeta(
					Ownership.readFromNBT(nbt, code),
					nbt.getInteger(code + "X"),
					nbt.getInteger(code + "Y"),
					nbt.getInteger(code + "Z")
			);
			meta.name = nbt.getString("name_" + code);
			meta.cityId = nbt.getString("cityId_" + code);
			if(meta.cityId == null || meta.cityId.isEmpty())
				meta.cityId = meta.flagX + "," + meta.flagY + "," + meta.flagZ;
			meta.cityName = nbt.getString("cityName_" + code);
			if(meta.cityName == null || meta.cityName.isEmpty())
				meta.cityName = meta.name;
			meta.cityLevel = nbt.getInteger("cityLevel_" + code);
			return meta;
		}
		
		public boolean isCityClaim() {
			return cityId != null && !cityId.isEmpty() && flagY >= 0;
		}

		public CityLevel getCityLevel() {
			return CityLevel.byOrdinal(cityLevel);
		}

		public int getColor() {
			
			if(owner != null) {
				return owner.getColor();
			}
			
			return 0x000000;
		}
		
		//chunks will persist if there's an operational flag within its bounds or if the supposedly flag-bearing chunk is not loaded
		public boolean checkPersistence(World world, CoordPair claim) {
			
			if(owner.zone != Zone.FACTION)
				return true;
			
			if(flagY < 0)
				return false;
			
			Clowder own = owner.owner;
			CoordPair origin = getCoordPair(flagX, flagZ);
			
			if(world == null || world.getChunkProvider() == null)
				return true;
			
			Chunk chunk = world.getChunkProvider().provideChunk(flagX >> 4, flagZ >> 4);
			
			if(chunk != null) {
				
				TileEntity te = world.getTileEntity(flagX, flagY, flagZ);
				
				if(te instanceof ITerritoryProvider) {
					
					ITerritoryProvider flag = (ITerritoryProvider)te;
					
					int r = flag.getRadius();
					this.name = flag.getClaimName();
					this.cityName = flag.getClaimName();
					if(flag instanceof TileEntityFlag)
						this.cityLevel = ((TileEntityFlag)flag).cityLevel.ordinal();
					
					double dist = Math.sqrt(Math.pow(origin.x - claim.x, 2) + Math.pow(origin.z - claim.z, 2));
					
					if(flag.getOwner() != own) {
						return false;
					} else if(dist >= r) {
						
						if(flag instanceof TileEntityFlag && ((TileEntityFlag)flag).height > 0 && ((TileEntityFlag)flag).height < 1.0F)
							return true;
						
						if(flag instanceof TileEntityConquerer && ((TileEntityConquerer)flag).height > 0)
							return true;
						
						return false;
					} else {
						return true;
					}
				} else {
					return false;
				}
			}
			
			//return true is the chunk does not exist, i.e. is not loaded to prevent spontaneous claim-decay
			return true;
		}
	}
	
	//checks a part of the clowder claim data for persistence, will delete non-persistent ones
	public static void checkPersistence(World world, int cycle, int index) {
		
		List<Long> BOW = new ArrayList(territories.keySet());
		
		for(int i = index; i < BOW.size(); i += cycle) {
			
			long code = BOW.get(i);
			TerritoryMeta meta = territories.get(code);
			
			//code will be deleted IF
			// -the code has no value assigned to it (null-value)
			// -the code refers to a claim that is deemed non-persistent
			if(meta != null) {
				
				if(!meta.checkPersistence(world, codeToCoords(code))) {
					territories.remove(code);
					i--;
				}
				
			} else {
				territories.remove(code);
				i--;
			}
		}
	}
	
	private static final int cycle = MainRegistry.territoryAmount;
	private static int ptr = 0;
	
	//i called it an automaton because it mindlessly iterates through the persistence checks without the common handler having to do anything in addition
	public static void persistenceAutomaton(World world) {
		
		ptr++;
		ptr = ptr % cycle;
		
		checkPersistence(world, cycle, ptr);
	}
	
	public static void readFromNBT(NBTTagCompound nbt) {
		
		territories.clear();
		int count = nbt.getInteger("territory_count");
		
		for(int i = 0; i < count; i++) {
			
			long code = nbt.getLong("code_" + i);
			TerritoryMeta meta = TerritoryMeta.readFromNBT(nbt, "meta_" + i);
			
			if(meta != null && meta.owner.zone != Zone.WILDERNESS) //todo here
				territories.put(code, meta);
		}
	}
	
	public static void writeToNBT(NBTTagCompound nbt) {
		
		nbt.setInteger("territory_count", territories.size());
		int index = 0;
		
		for(long code : territories.keySet()) {
			
			TerritoryMeta meta = territories.get(code);
			
			//do not save wilderness
			//todo check that this isnt some bs
			if(meta.owner.zone != Zone.WILDERNESS) {
				nbt.setLong("code_" + index, code);
				meta.writeToNBT(nbt, "meta_" + index);
			}
			
			index++;
		}
	}
}
