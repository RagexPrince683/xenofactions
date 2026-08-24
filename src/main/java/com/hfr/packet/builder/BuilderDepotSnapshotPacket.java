package com.hfr.packet.builder;

import java.util.*;
import com.hfr.builder.*;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.schematic.*;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.*;
import io.netty.buffer.ByteBuf;

/** Coordinate-qualified, immutable-on-receipt view data shared by both Builder screens. */
public class BuilderDepotSnapshotPacket implements IMessage {
    private static final Map<String,BuilderDepotSnapshotPacket> CLIENT=new HashMap<String,BuilderDepotSnapshotPacket>();
    public int dimension,x,y,z;public String builderId="",builderName="",faction="",city="",state="IDLE",schematic="",carried="";
    public float health,maxHealth;public int progress,total,bx,by,bz;public boolean blocked;public final List<String> queue=new ArrayList<String>();
    public BuilderDepotSnapshotPacket(){}
    public static BuilderDepotSnapshotPacket create(TileEntityMachineBuilder d){
        BuilderDepotSnapshotPacket s=new BuilderDepotSnapshotPacket();s.dimension=d.getWorldObj().provider.dimensionId;s.x=d.xCoord;s.y=d.yCoord;s.z=d.zCoord;
        EntityFactionBuilder builder=d.getLoadedBuilder();s.builderId=d.getAssignedBuilderId()==null?"":d.getAssignedBuilderId().toString();
        if(builder!=null){s.builderName=builder.getBuilderDisplayName();s.health=builder.getHealth();s.maxHealth=builder.getMaxHealth();s.carried=builder.getCarriedMaterialSummary();}
        else s.builderName=s.builderId.isEmpty()?"": "Builder";
        Clowder c=d.getFactionId()==null?null:Clowder.getClowderFromUUID(d.getFactionId().toString());s.faction=c==null?"":c.name;s.city=cityName(c,d.getCityId());
        BuilderJobData data=BuilderJobData.get(d.getWorldObj());SchematicStoreData store=SchematicStoreData.get(d.getWorldObj());BuilderJob j=data==null?null:data.get(d.getActiveJobId());
        if(j!=null){s.state=j.state.name();Schematic sc=store==null?null:store.getSchematic(j.schematicId);s.schematic=sc==null?j.schematicId:sc.name;s.progress=j.blockIndex;s.total=sc==null?0:sc.size();s.bx=j.blockedX;s.by=j.blockedY;s.bz=j.blockedZ;s.blocked=j.state==BuilderState.INVALID_TERRITORY;}
        else if(builder!=null)s.state=builder.getBuilderState().name();
        for(UUID id:d.getQueuedJobs()){BuilderJob q=data==null?null:data.get(id);if(q!=null){Schematic qs=store==null?null:store.getSchematic(q.schematicId);s.queue.add((qs==null?q.schematicId:qs.name)+" | "+q.state.name()+" | "+q.blockIndex+"/"+(qs==null?0:qs.size()));}}
        return s;
    }
    private static String cityName(Clowder c,String id){if(c!=null&&id!=null)for(ClowderTerritory.TerritoryMeta m:ClowderTerritory.getCityClaims(c))if(id.equals(m.cityId))return m.cityName;return "";}
    public static BuilderDepotSnapshotPacket get(int dimension,int x,int y,int z){synchronized(CLIENT){return CLIENT.get(key(dimension,x,y,z));}}
    private static String key(int d,int x,int y,int z){return d+":"+x+":"+y+":"+z;}
    public void fromBytes(ByteBuf b){dimension=b.readInt();x=b.readInt();y=b.readInt();z=b.readInt();builderId=str(b);builderName=str(b);faction=str(b);city=str(b);state=str(b);schematic=str(b);carried=str(b);health=b.readFloat();maxHealth=b.readFloat();progress=b.readInt();total=b.readInt();blocked=b.readBoolean();bx=b.readInt();by=b.readInt();bz=b.readInt();int n=b.readUnsignedShort();for(int i=0;i<n;i++)queue.add(str(b));}
    public void toBytes(ByteBuf b){b.writeInt(dimension);b.writeInt(x);b.writeInt(y);b.writeInt(z);put(b,builderId);put(b,builderName);put(b,faction);put(b,city);put(b,state);put(b,schematic);put(b,carried);b.writeFloat(health);b.writeFloat(maxHealth);b.writeInt(progress);b.writeInt(total);b.writeBoolean(blocked);b.writeInt(bx);b.writeInt(by);b.writeInt(bz);b.writeShort(queue.size());for(String q:queue)put(b,q);}
    private static String str(ByteBuf b){return ByteBufUtils.readUTF8String(b);}private static void put(ByteBuf b,String s){ByteBufUtils.writeUTF8String(b,s==null?"":s);}
    public static class Handler implements IMessageHandler<BuilderDepotSnapshotPacket,IMessage>{public IMessage onMessage(final BuilderDepotSnapshotPacket m,MessageContext c){receive(m);return null;}
        @SideOnly(Side.CLIENT)private void receive(final BuilderDepotSnapshotPacket m){net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){synchronized(CLIENT){CLIENT.put(key(m.dimension,m.x,m.y,m.z),m);}}});}}
}
