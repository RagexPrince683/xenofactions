package com.hfr.builder;

import java.io.IOException;
import java.util.UUID;
import com.hfr.clowder.Clowder;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.main.MainRegistry;
import com.hfr.schematic.*;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/** Server-authoritative Builder Depot operations shared by packets and job advancement. */
public final class BuilderDepotService {
    private BuilderDepotService(){}
    public static boolean mayView(EntityPlayerMP p,TileEntityMachineBuilder d){Clowder c=Clowder.getClowderFromPlayer(p);return c!=null&&d.getFactionId()!=null&&d.getFactionId().toString().equals(c.uuid);}
    public static boolean mayBuild(EntityPlayerMP p,TileEntityMachineBuilder d){return mayView(p,d)&&Clowder.getClowderFromPlayer(p).getRole(p)!=null;}
    public static String accept(EntityPlayerMP p,TileEntityMachineBuilder d,Schematic schematic,int x,int y,int z,int rotation,boolean mirror){
        if(!mayBuild(p,d))return "You do not have BUILD permission for this Depot.";if(schematic==null)return "No schematic selected.";
        try{SchematicLoader.validateDimensions(schematic.width,schematic.height,schematic.length);}catch(IOException e){return e.getMessage();}
        UUID faction=d.getFactionId();int width=SchematicTransform.width(schematic,rotation),length=SchematicTransform.length(schematic,rotation);
        for(int dx=0;dx<width;dx++)for(int dz=0;dz<length;dz++)if(!BuilderTerritory.mayChange(p.worldObj,x+dx,z+dz,faction,false))return "Invalid territory at "+(x+dx)+", "+y+", "+(z+dz);
        SchematicStoreData store=SchematicStoreData.get(p.worldObj);BuilderJobData jobs=BuilderJobData.get(p.worldObj);if(store==null||jobs==null)return "Builder persistence is unavailable.";
        BuilderJob job=new BuilderJob();job.factionId=faction;job.creatorId=p.getUniqueID();job.cityId=d.getCityId();job.dimension=p.dimension;job.schematicId=store.putSchematic(schematic);job.originX=x;job.originY=y;job.originZ=z;job.rotation=rotation&3;job.mirrored=mirror;job.depotX=d.xCoord;job.depotY=d.yCoord;job.depotZ=d.zCoord;job.state=BuilderState.LOAD_JOB;jobs.put(job);
        if(d.getActiveJobId()==null){d.setActiveJob(job.jobId);activate(d,job);}else d.queueJob(job.jobId);return null;
    }
    public static Schematic bundled(String name){for(Schematic s:MainRegistry.schems)if(s!=null&&s.name.equals(name))return s;return null;}
    public static void activate(TileEntityMachineBuilder d,BuilderJob job){if(job==null)return;EntityFactionBuilder b=d.getLoadedBuilder();if(b!=null){job.builderId=b.getUniqueID();b.setJob(job.jobId);job.state=BuilderState.LOAD_JOB;}BuilderJobData data=BuilderJobData.get(d.getWorldObj());if(data!=null)data.markDirty();d.markDirty();}
    public static void advance(TileEntityMachineBuilder d){BuilderJobData data=BuilderJobData.get(d.getWorldObj());if(data==null)return;UUID next=d.advanceQueue();BuilderJob job=data.get(next);while(next!=null&&(job==null||job.state==BuilderState.COMPLETE)){next=d.advanceQueue();job=data.get(next);}if(job!=null)activate(d,job);}
}
