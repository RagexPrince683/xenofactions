package com.hfr.builder;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;

/** Persistent, entity-independent construction progress. */
public final class BuilderJob {
	public UUID jobId = UUID.randomUUID(), factionId, builderId, creatorId;
	public String cityId = "", schematicId = "";
	public int dimension, originX, originY, originZ, depotX, depotY, depotZ, blockIndex;
	public int rotation;
	public boolean mirrored;
	public int blockedX, blockedY, blockedZ;
	public BuilderState state = BuilderState.PAUSED;

	public NBTTagCompound write() {
		NBTTagCompound n = new NBTTagCompound();
		n.setString("job", jobId.toString()); put(n,"faction",factionId); put(n,"builder",builderId); put(n,"creator",creatorId);
		n.setString("city", cityId); n.setString("schematic", schematicId); n.setInteger("dimension",dimension);
		n.setInteger("x",originX); n.setInteger("y",originY); n.setInteger("z",originZ);
		n.setInteger("depotX",depotX); n.setInteger("depotY",depotY); n.setInteger("depotZ",depotZ);
		n.setInteger("index",blockIndex); n.setString("state",state.name());
		n.setByte("rotation",(byte)(rotation&3)); n.setBoolean("mirrored",mirrored);
		n.setInteger("blockedX",blockedX); n.setInteger("blockedY",blockedY); n.setInteger("blockedZ",blockedZ); return n;
	}
	public static BuilderJob read(NBTTagCompound n) {
		BuilderJob j=new BuilderJob(); j.jobId=id(n,"job",j.jobId); j.factionId=id(n,"faction",null); j.builderId=id(n,"builder",null); j.creatorId=id(n,"creator",null);
		j.cityId=n.getString("city"); j.schematicId=n.getString("schematic"); j.dimension=n.getInteger("dimension");
		j.rotation=n.getByte("rotation")&3; j.mirrored=n.getBoolean("mirrored");
		j.originX=n.getInteger("x"); j.originY=n.getInteger("y"); j.originZ=n.getInteger("z"); j.depotX=n.getInteger("depotX"); j.depotY=n.getInteger("depotY"); j.depotZ=n.getInteger("depotZ"); j.blockIndex=n.getInteger("index");
		try { j.state=BuilderState.valueOf(n.getString("state")); } catch(Exception e) { j.state=BuilderState.PAUSED; }
		j.blockedX=n.getInteger("blockedX"); j.blockedY=n.getInteger("blockedY"); j.blockedZ=n.getInteger("blockedZ"); return j;
	}
	private static void put(NBTTagCompound n,String k,UUID v){if(v!=null)n.setString(k,v.toString());}
	private static UUID id(NBTTagCompound n,String k,UUID d){try{return UUID.fromString(n.getString(k));}catch(Exception e){return d;}}
}
