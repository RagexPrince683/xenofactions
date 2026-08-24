package com.hfr.builder;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public final class BuilderJobData extends WorldSavedData {
	private static final String ID="hfr_builder_jobs";
	private final Map<UUID,BuilderJob> jobs=new LinkedHashMap<UUID,BuilderJob>();
	public BuilderJobData(){super(ID);} public BuilderJobData(String id){super(id);}
	public static BuilderJobData get(World world){ World root=world.getMinecraftServer().worldServerForDimension(0); BuilderJobData d=(BuilderJobData)root.perWorldStorage.loadData(BuilderJobData.class,ID); if(d==null){d=new BuilderJobData();root.perWorldStorage.setData(ID,d);} return d; }
	public BuilderJob get(UUID id){return id==null?null:jobs.get(id);} public Collection<BuilderJob> all(){return Collections.unmodifiableCollection(jobs.values());}
	public void put(BuilderJob j){jobs.put(j.jobId,j);markDirty();}
	@Override public void readFromNBT(NBTTagCompound n){jobs.clear();NBTTagList l=n.getTagList("jobs",10);for(int i=0;i<l.tagCount();i++){BuilderJob j=BuilderJob.read(l.getCompoundTagAt(i));jobs.put(j.jobId,j);}}
	@Override public void writeToNBT(NBTTagCompound n){NBTTagList l=new NBTTagList();for(BuilderJob j:jobs.values())l.appendTag(j.write());n.setTag("jobs",l);}
}
