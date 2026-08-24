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
import net.minecraft.item.ItemStack;

/** Coordinate-qualified, immutable-on-receipt view data shared by both Builder screens. */
public class BuilderDepotSnapshotPacket implements IMessage {
    private static final Map<String,BuilderDepotSnapshotPacket> CLIENT=new HashMap<String,BuilderDepotSnapshotPacket>();
    public int dimension,x,y,z;public String builderId="",builderName="",faction="",city="",state="IDLE",schematic="",carried="",statusKey="builder.status.no_job",detail="",planSource="",planId="",planName="",planValidation="";
    public int planX,planY,planZ,planRotation,targetX,targetY,targetZ,missing,unsupported;public boolean planMirror,planPreview;
    public float health,maxHealth;public int progress,total,bx,by,bz;public boolean blocked;public final List<String> queue=new ArrayList<String>();
    public final List<ItemStack> builderInventory=new ArrayList<ItemStack>();
    public BuilderDepotSnapshotPacket(){}
    public static BuilderDepotSnapshotPacket create(TileEntityMachineBuilder d){
        BuilderDepotSnapshotPacket s=new BuilderDepotSnapshotPacket();s.dimension=d.getWorldObj().provider.dimensionId;s.x=d.xCoord;s.y=d.yCoord;s.z=d.zCoord;
        BuilderPlan plan=d.getPlan();s.planSource=plan.source;s.planId=plan.schematicId;s.planName=plan.displayName;s.planValidation=plan.validationKey;s.planX=plan.originX;s.planY=plan.originY;s.planZ=plan.originZ;s.planRotation=plan.rotation;s.planMirror=plan.mirrored;s.planPreview=plan.preview;
        EntityFactionBuilder builder=d.getLoadedBuilder();s.builderId=d.getAssignedBuilderId()==null?"":d.getAssignedBuilderId().toString();
        if(builder!=null){s.builderName=builder.getBuilderDisplayName();s.health=builder.getHealth();s.maxHealth=builder.getMaxHealth();s.carried=builder.getCarriedMaterialSummary();for(ItemStack stack:builder.copyInventory())if(stack!=null)s.builderInventory.add(stack);}
        else s.builderName=s.builderId.isEmpty()?"": "Builder";
        Clowder c=d.getFactionId()==null?null:Clowder.getClowderFromUUID(d.getFactionId().toString());s.faction=c==null?"":c.name;s.city=cityName(c,d.getCityId());
        BuilderJobData data=BuilderJobData.get(d.getWorldObj());SchematicStoreData store=SchematicStoreData.get(d.getWorldObj());BuilderJob j=data==null?null:data.get(d.getActiveJobId());
        if(j!=null){s.state=j.state.name();s.statusKey=j.statusKey;s.detail=j.failureDetail;s.targetX=j.targetX;s.targetY=j.targetY;s.targetZ=j.targetZ;s.missing=j.missingQuantity;s.unsupported=j.unsupportedBlock.isEmpty()?0:1;Schematic sc=store==null?null:store.getSchematic(j.schematicId);s.schematic=sc==null?j.schematicId:sc.name;s.progress=j.blockIndex;s.total=sc==null?0:sc.size();s.bx=j.blockedX;s.by=j.blockedY;s.bz=j.blockedZ;s.blocked=j.state==BuilderState.INVALID_TERRITORY;}
        else if(builder!=null)s.state=builder.getBuilderState().name();
        for(UUID id:d.getQueuedJobs()){BuilderJob q=data==null?null:data.get(id);if(q!=null){Schematic qs=store==null?null:store.getSchematic(q.schematicId);s.queue.add((qs==null?q.schematicId:qs.name)+" | "+q.state.name()+" | "+q.blockIndex+"/"+(qs==null?0:qs.size()));}}
        return s;
    }
    private static String cityName(Clowder c,String id){if(c!=null&&id!=null)for(ClowderTerritory.TerritoryMeta m:ClowderTerritory.getCityClaims(c))if(id.equals(m.cityId))return m.cityName;return "";}
    public static BuilderDepotSnapshotPacket get(int dimension,int x,int y,int z){synchronized(CLIENT){return CLIENT.get(key(dimension,x,y,z));}}
    private static String key(int d,int x,int y,int z){return d+":"+x+":"+y+":"+z;}
    @SideOnly(Side.CLIENT) public static void refreshPreviews(){net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();if(mc.theWorld==null)return;synchronized(CLIENT){for(BuilderDepotSnapshotPacket s:CLIENT.values()){if(s.dimension!=mc.theWorld.provider.dimensionId)continue;net.minecraft.tileentity.TileEntity te=mc.theWorld.getTileEntity(s.x,s.y,s.z);if(!(te instanceof TileEntityMachineBuilder))continue;TileEntityMachineBuilder d=(TileEntityMachineBuilder)te;Schematic cached=BuilderSchematicDataChunkPacket.ClientCache.get(s.planId);d.preview=s.planPreview?cached:null;if(d.preview!=null){d.previewX=s.planX;d.previewY=s.planY;d.previewZ=s.planZ;d.previewRotation=s.planRotation;d.previewMirrored=s.planMirror;}}}}
    public void fromBytes(ByteBuf b){dimension=b.readInt();x=b.readInt();y=b.readInt();z=b.readInt();builderId=str(b);builderName=str(b);faction=str(b);city=str(b);state=str(b);schematic=str(b);carried=str(b);statusKey=str(b);detail=str(b);planSource=str(b);planId=str(b);planName=str(b);planValidation=str(b);planX=b.readInt();planY=b.readInt();planZ=b.readInt();planRotation=b.readByte();planMirror=b.readBoolean();planPreview=b.readBoolean();targetX=b.readInt();targetY=b.readInt();targetZ=b.readInt();missing=b.readInt();unsupported=b.readInt();health=b.readFloat();maxHealth=b.readFloat();progress=b.readInt();total=b.readInt();blocked=b.readBoolean();bx=b.readInt();by=b.readInt();bz=b.readInt();int n=b.readUnsignedShort();for(int i=0;i<n;i++)queue.add(str(b));n=b.readUnsignedByte();for(int i=0;i<n;i++)builderInventory.add(ByteBufUtils.readItemStack(b));}
    public void toBytes(ByteBuf b){b.writeInt(dimension);b.writeInt(x);b.writeInt(y);b.writeInt(z);put(b,builderId);put(b,builderName);put(b,faction);put(b,city);put(b,state);put(b,schematic);put(b,carried);put(b,statusKey);put(b,detail);put(b,planSource);put(b,planId);put(b,planName);put(b,planValidation);b.writeInt(planX);b.writeInt(planY);b.writeInt(planZ);b.writeByte(planRotation);b.writeBoolean(planMirror);b.writeBoolean(planPreview);b.writeInt(targetX);b.writeInt(targetY);b.writeInt(targetZ);b.writeInt(missing);b.writeInt(unsupported);b.writeFloat(health);b.writeFloat(maxHealth);b.writeInt(progress);b.writeInt(total);b.writeBoolean(blocked);b.writeInt(bx);b.writeInt(by);b.writeInt(bz);b.writeShort(queue.size());for(String q:queue)put(b,q);b.writeByte(builderInventory.size());for(ItemStack stack:builderInventory)ByteBufUtils.writeItemStack(b,stack);}
    private static String str(ByteBuf b){return ByteBufUtils.readUTF8String(b);}private static void put(ByteBuf b,String s){ByteBufUtils.writeUTF8String(b,s==null?"":s);}
    public static class Handler implements IMessageHandler<BuilderDepotSnapshotPacket,IMessage>{public IMessage onMessage(final BuilderDepotSnapshotPacket m,MessageContext c){receive(m);return null;}
        @SideOnly(Side.CLIENT)private void receive(final BuilderDepotSnapshotPacket m){net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){synchronized(CLIENT){CLIENT.put(key(m.dimension,m.x,m.y,m.z),m);}refreshPreviews();}});}}
}
