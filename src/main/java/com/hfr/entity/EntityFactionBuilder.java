package com.hfr.entity;

import java.util.*;
import com.hfr.builder.*;
import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.hfr.blocks.ModBlocks;
import com.hfr.schematic.Schematic;
import com.hfr.schematic.SchematicStoreData;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import com.hfr.util.XFLog;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;

/** Persistent faction worker. All work and inventory mutation occurs on the logical server. */
public class EntityFactionBuilder extends EntityLiving {
    private static final int WORK_REACH=4,MAX_PATH_ATTEMPTS=5,STALL_TICKS=80;
    private static final double USEFUL_MOVE_SQ=.04D,USEFUL_CLOSER=.05D;
    private UUID factionId,jobId; private int depotX,depotY,depotZ,depotDimension,pathFailures,candidateIndex,pathTargetX=Integer.MIN_VALUE,pathTargetY,pathTargetZ,lastProgressTick;
    private double lastX,lastY,lastZ,lastDistance=Double.MAX_VALUE;
    private BuilderState state=BuilderState.IDLE;
    private final ItemStack[] materials=new ItemStack[9];
    public EntityFactionBuilder(World w){super(w);setSize(.6F,1.8F);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.28);}
    @Override protected boolean canDespawn(){return false;}
    public void assign(UUID faction,UUID job,int x,int y,int z,int dimension){factionId=faction;jobId=job;depotX=x;depotY=y;depotZ=z;depotDimension=dimension;state=job==null?BuilderState.IDLE:BuilderState.LOAD_JOB;resetPath();}
    public BuilderState getBuilderState(){return state;} public UUID getJobId(){return jobId;} public UUID getFactionId(){return factionId;}
    public int getDepotX(){return depotX;} public int getDepotY(){return depotY;} public int getDepotZ(){return depotZ;} public int getDepotDimension(){return depotDimension;}
    public String getBuilderDisplayName(){return hasCustomNameTag()?getCustomNameTag():"Builder";}
    public String getCarriedMaterialSummary(){StringBuilder out=new StringBuilder();for(ItemStack stack:materials)if(stack!=null){if(out.length()>0)out.append(", ");out.append(stack.getDisplayName()).append(" x").append(stack.stackSize);}return out.toString();}
    public void setJob(UUID id){jobId=id;state=id==null?BuilderState.IDLE:BuilderState.LOAD_JOB;resetPath();}
    public void pauseWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null)pause(j,BuilderState.PAUSED);else state=BuilderState.PAUSED;resetPath();}
    public void resumeWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null){resetPath();j.failureDetail="";j.pathFailures=0;state=BuilderState.LOAD_JOB;j.state=state;d.markDirty();}}
    public void recallToDepot(){resetPath();BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob job=d==null?null:d.get(jobId);setWorkState(job,BuilderState.GETTING_MATERIALS,"builder.status.getting_materials","");getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);}
    public void clearJob(){jobId=null;state=BuilderState.IDLE;resetPath();}
    @Override public boolean interact(EntityPlayer player){if(worldObj.isRemote)return true;TileEntityMachineBuilder d=depot();if(d==null||!getUniqueID().equals(d.getAssignedBuilderId())){player.addChatMessage(new ChatComponentTranslation("builder.depot.missing"));return true;}FMLNetworkHandler.openGui(player,MainRegistry.instance,ModBlocks.guiID_builder_npc,worldObj,depotX,depotY,depotZ);return true;}
    public void detachFromDepot(){BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob job=data==null?null:data.get(jobId);if(job!=null)pause(job,BuilderState.PAUSED);jobId=null;state=BuilderState.PAUSED;resetPath();}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!worldObj.isRemote&&XFConfig.enableFactionBuilders&&ticksExisted%XFConfig.builderWorkIntervalTicks==0)work();}

    private void work(){
        BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob job=data==null?null:data.get(jobId);
        if(job==null){reattach();data=BuilderJobData.get(worldObj);job=data==null?null:data.get(jobId);if(job==null){state=BuilderState.IDLE;return;}}
        if(job.dimension!=worldObj.provider.dimensionId){pause(job,BuilderState.PAUSED);return;}
        if(job.state==BuilderState.PAUSED||job.state==BuilderState.UNSUPPORTED_BLOCK||job.state==BuilderState.INVALID_TERRITORY||job.state==BuilderState.PATHFINDING_ERROR){state=job.state;return;}
        job.builderId=getUniqueID();Schematic s=find(job.schematicId);if(s==null){setWorkState(job,BuilderState.PAUSED,"builder.status.schematic_missing","Schematic is missing.");return;}
        int total=s.width*s.height*s.length,scans=0;
        while(job.blockIndex<total&&scans++<XFConfig.builderBlockScanBudget){
            int i=job.blockIndex,z=i%s.length,y=(i/s.length)%s.height,x=i/s.length/s.height;
            int[] transformed=com.hfr.schematic.SchematicTransform.position(s,x,y,z,job.rotation,job.mirrored);
            int wx=job.originX+transformed[0],wy=job.originY+transformed[1],wz=job.originZ+transformed[2];
            if(wx!=job.targetX||wy!=job.targetY||wz!=job.targetZ)resetPath();
            job.targetX=wx;job.targetY=wy;job.targetZ=wz;job.schematicX=x;job.schematicY=y;job.schematicZ=z;
            Block wanted=s.resolveBlock(x,y,z);int meta=com.hfr.schematic.SchematicTransform.metadata(wanted,s.getMetadata(x,y,z),job.rotation,job.mirrored);
            if(!worldObj.blockExists(wx,wy,wz)){job.waitingChunkX=wx>>4;job.waitingChunkZ=wz>>4;setWorkState(job,BuilderState.WAITING_FOR_CHUNK,"builder.status.waiting_chunk","Waiting for chunk "+job.waitingChunkX+", "+job.waitingChunkZ+" containing "+coord(wx,wy,wz)+".");return;}
            if(wanted==null){job.unsupportedBlock=s.getBlockName(x,y,z);job.unsupportedMeta=s.getMetadata(x,y,z);setWorkState(job,BuilderState.UNSUPPORTED_BLOCK,"builder.status.unsupported","Unsupported block "+job.unsupportedBlock+":"+job.unsupportedMeta+" at "+coord(wx,wy,wz)+".");return;}
            Block existing=worldObj.getBlock(wx,wy,wz);if(existing==wanted&&worldObj.getBlockMetadata(wx,wy,wz)==meta){job.blockIndex++;data.markDirty();resetPath();continue;}
            XFLog.debug("Builder "+getUniqueID()+" target "+wx+","+wy+","+wz+" -> "+blockName(wanted)+":"+meta);
            ItemStack need=null;
            if(wanted!=Blocks.air){need=BuilderMaterialResolver.resolve(wanted,meta);if(need==null){job.unsupportedBlock=blockName(wanted);job.unsupportedMeta=meta;setWorkState(job,BuilderState.UNSUPPORTED_BLOCK,"builder.status.unsupported","Unsupported block "+job.unsupportedBlock+":"+meta+" at "+coord(wx,wy,wz)+".");return;}setRequirement(job,need,remainingNeed(s,job,need));if(findMaterial(materials,need)<0&&!fetchMaterial(job,need)){return;}}
            else {job.requiredItem="";job.requiredMeta=0;job.missingQuantity=0;}
            if(!canWork(wx,wy,wz)){if(!moveToWork(job,wx,wy,wz))return;return;}
            pathFailures=0;candidateIndex=0;getNavigator().clearPathEntity();
            if(existing!=Blocks.air){String name=blockName(existing);setWorkState(job,BuilderState.BREAKING_BLOCK,"builder.status.breaking","Clearing "+name+" at "+coord(wx,wy,wz));BuilderPlacement.Result result=BuilderPlacement.breakBlockResult(worldObj,wx,wy,wz,factionId);if(result!=BuilderPlacement.Result.SUCCESS){placementFailure(job,result,wx,wy,wz,"break");return;}data.markDirty();XFLog.debug("Cleared target "+wx+","+wy+","+wz+"; retaining schematic index "+job.blockIndex);return;}
            if(wanted==Blocks.air){job.blockIndex++;data.markDirty();resetPath();continue;}
            int slot=findMaterial(materials,need);if(slot<0){setRequirement(job,need,Math.max(1,remainingNeed(s,job,need)));setWorkState(job,BuilderState.WAITING_FOR_MATERIALS,"builder.status.waiting_materials",waiting(job,need));return;}
            setWorkState(job,BuilderState.PLACING_BLOCK,"builder.status.building","Placing "+need.getDisplayName()+" at "+coord(wx,wy,wz));BuilderPlacement.Result result=BuilderPlacement.placeResult(worldObj,wx,wy,wz,wanted,meta,factionId);
            if(result==BuilderPlacement.Result.SUCCESS){if(--materials[slot].stackSize<=0)materials[slot]=null;job.blockIndex++;job.missingQuantity=0;data.markDirty();resetPath();XFLog.debug("Placed target; progress now "+job.blockIndex+"/"+total);return;}
            placementFailure(job,result,wx,wy,wz,"place");return;
        }
        if(job.blockIndex>=total){setWorkState(job,BuilderState.COMPLETE,"builder.status.complete","Construction complete.");TileEntityMachineBuilder d=depot();if(d!=null){d.setActiveJob(null);clearJob();BuilderDepotService.advance(d);}}else setWorkState(job,BuilderState.FIND_NEXT_BLOCK,"builder.status.finding","Scanning for the next unsatisfied schematic cell.");
    }

    private boolean fetchMaterial(BuilderJob job,ItemStack need){TileEntityMachineBuilder d=depot();if(d==null){setWorkState(job,BuilderState.WAITING_FOR_MATERIALS,"builder.status.waiting_materials","Builder Depot is unavailable; waiting for "+need.getDisplayName()+" x"+job.missingQuantity+".");return false;}if(getDistanceSq(depotX+.5,depotY+.5,depotZ+.5)>16){setWorkState(job,BuilderState.GETTING_MATERIALS,"builder.status.getting_materials","Fetching "+need.getDisplayName()+" from Depot at "+coord(depotX,depotY,depotZ));if(getNavigator().noPath())getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);return false;}int capacity=capacityFor(need);int amount=Math.min(Math.min(need.getMaxStackSize(),job.missingQuantity),capacity);int taken=d.takeMaterial(need,amount);if(taken>0){add(need,taken);XFLog.debug("Fetched "+job.requiredItem+":"+job.requiredMeta+" x"+taken);return true;}setWorkState(job,BuilderState.WAITING_FOR_MATERIALS,"builder.status.waiting_materials",waiting(job,need));return false;}
    private boolean moveToWork(BuilderJob job,int tx,int ty,int tz){
        int[][] candidates=candidates(tx,ty,tz);if(candidates.length==0){pathError(job,tx,ty,tz,"No safe reachable work position near");return false;}
        boolean sameTarget=pathTargetX==tx&&pathTargetY==ty&&pathTargetZ==tz;
        if(sameTarget&&!getNavigator().noPath()){
            double moved=(posX-lastX)*(posX-lastX)+(posY-lastY)*(posY-lastY)+(posZ-lastZ)*(posZ-lastZ);double distance=getDistanceSq(job.workX+.5,job.workY,job.workZ+.5);
            if(moved>=USEFUL_MOVE_SQ||distance<lastDistance-USEFUL_CLOSER){lastX=posX;lastY=posY;lastZ=posZ;lastDistance=distance;lastProgressTick=ticksExisted;return false;}
            if(ticksExisted-lastProgressTick<STALL_TICKS)return false;
            XFLog.debug("Builder movement stalled at "+posX+","+posY+","+posZ+"; trying alternate work position");getNavigator().clearPathEntity();
        }
        if(!sameTarget){candidateIndex=0;pathFailures=0;pathTargetX=tx;pathTargetY=ty;pathTargetZ=tz;}else if(getNavigator().noPath()&&pathFailures>0)candidateIndex++;
        while(candidateIndex<candidates.length&&pathFailures<MAX_PATH_ATTEMPTS){int[] c=candidates[candidateIndex];job.workX=c[0];job.workY=c[1];job.workZ=c[2];boolean ok=getNavigator().tryMoveToXYZ(c[0]+.5,c[1],c[2]+.5,1);pathFailures++;job.pathFailures=pathFailures;XFLog.debug("Selected work position "+c[0]+","+c[1]+","+c[2]+"; path request "+(ok?"succeeded":"failed"));if(ok){lastX=posX;lastY=posY;lastZ=posZ;lastDistance=getDistanceSq(c[0]+.5,c[1],c[2]+.5);lastProgressTick=ticksExisted;setWorkState(job,BuilderState.MOVE_TO_SITE,"builder.status.moving","Moving to build site: "+coord(tx,ty,tz)+"; work position: "+coord(c[0],c[1],c[2]));return false;}candidateIndex++;}
        pathError(job,tx,ty,tz,"Could not reach a work position within "+WORK_REACH+" blocks of");return false;
    }
    private int[][] candidates(int tx,int ty,int tz){List<int[]> out=new ArrayList<int[]>();for(int dy=-1;dy<=1;dy++)for(int r=1;r<=WORK_REACH;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;int x=tx+dx,y=ty+dy,z=tz+dz;if(distanceSq(x+.5,y+1.62,z+.5,tx+.5,ty+.5,tz+.5)<=WORK_REACH*WORK_REACH&&validStand(x,y,z))out.add(new int[]{x,y,z});}Collections.sort(out,new Comparator<int[]>(){public int compare(int[] a,int[] b){return Double.compare(getDistanceSq(a[0]+.5,a[1],a[2]+.5),getDistanceSq(b[0]+.5,b[1],b[2]+.5));}});return out.toArray(new int[out.size()][]);}
    private boolean validStand(int x,int y,int z){if(y<=0||y+1>=worldObj.getHeight()||!worldObj.blockExists(x,y,z))return false;Block floor=worldObj.getBlock(x,y-1,z);if(floor==Blocks.air||!floor.getMaterial().blocksMovement())return false;AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x+.2,y,z+.2,x+.8,y+1.8,z+.8);return worldObj.getCollidingBoundingBoxes(this,box).isEmpty();}
    private boolean canWork(int x,int y,int z){if(distanceSq(posX,posY+getEyeHeight(),posZ,x+.5,y+.5,z+.5)>WORK_REACH*WORK_REACH)return false;MovingObjectPosition hit=worldObj.rayTraceBlocks(Vec3.createVectorHelper(posX,posY+getEyeHeight(),posZ),Vec3.createVectorHelper(x+.5,y+.5,z+.5));return hit==null||(hit.blockX==x&&hit.blockY==y&&hit.blockZ==z);}
    private void pathError(BuilderJob job,int x,int y,int z,String reason){job.pathFailures=pathFailures;String attempted=job.workX+", "+job.workY+", "+job.workZ;setWorkState(job,BuilderState.PATHFINDING_ERROR,"builder.status.cannot_reach",reason+" "+x+", "+y+", "+z+" after "+pathFailures+" attempts. Last attempted work coordinate: "+attempted+".");getNavigator().clearPathEntity();XFLog.debug("No reachable work position near "+x+","+y+","+z);}
    private void placementFailure(BuilderJob job,BuilderPlacement.Result result,int x,int y,int z,String action){job.blockedX=x;job.blockedY=y;job.blockedZ=z;BuilderState next=result==BuilderPlacement.Result.INVALID_TERRITORY||result==BuilderPlacement.Result.PROTECTED_BLOCK?BuilderState.INVALID_TERRITORY:result==BuilderPlacement.Result.UNSUPPORTED_BLOCK?BuilderState.UNSUPPORTED_BLOCK:BuilderState.PAUSED;setWorkState(job,next,"builder.status."+action+"_failed",capitalize(result.name().toLowerCase().replace('_',' '))+" while attempting to "+action+" at "+coord(x,y,z)+".");}
    private int remainingNeed(Schematic s,BuilderJob job,ItemStack need){int count=0,total=s.width*s.height*s.length;for(int i=job.blockIndex;i<total;i++){int z=i%s.length,y=(i/s.length)%s.height,x=i/s.length/s.height;Block b=s.resolveBlock(x,y,z);if(b==null||b==Blocks.air)continue;int m=com.hfr.schematic.SchematicTransform.metadata(b,s.getMetadata(x,y,z),job.rotation,job.mirrored);ItemStack n=BuilderMaterialResolver.resolve(b,m);if(n!=null&&n.isItemEqual(need))count++;}return Math.max(0,count-countMaterial(materials,need));}
    private void setRequirement(BuilderJob job,ItemStack need,int missing){job.requiredItem=String.valueOf(GameData.getItemRegistry().getNameForObject(need.getItem()));job.requiredMeta=need.getItemDamage();job.missingQuantity=Math.max(1,missing);}
    private String waiting(BuilderJob job,ItemStack need){return "Waiting for: "+need.getDisplayName()+" x"+job.missingQuantity;}
    private Schematic find(String id){SchematicStoreData store=SchematicStoreData.get(worldObj);Schematic stored=store==null?null:store.getSchematic(id);if(stored!=null)return stored;for(Schematic s:MainRegistry.schems)if(s!=null&&s.name.equals(id))return s;return null;}
    private TileEntityMachineBuilder depot(){return worldObj.provider.dimensionId==depotDimension&&worldObj.getTileEntity(depotX,depotY,depotZ) instanceof TileEntityMachineBuilder?(TileEntityMachineBuilder)worldObj.getTileEntity(depotX,depotY,depotZ):null;}
    private static int findMaterial(ItemStack[] a,ItemStack n){for(int i=0;i<a.length;i++)if(a[i]!=null&&a[i].isItemEqual(n))return i;return -1;}
    private static int countMaterial(ItemStack[] a,ItemStack n){int count=0;for(ItemStack s:a)if(s!=null&&s.isItemEqual(n))count+=s.stackSize;return count;}
    private int capacityFor(ItemStack n){int capacity=0;for(ItemStack s:materials)if(s==null)capacity+=n.getMaxStackSize();else if(s.isItemEqual(n))capacity+=Math.max(0,s.getMaxStackSize()-s.stackSize);return capacity;}
    private void add(ItemStack n,int amount){int left=amount;for(ItemStack s:materials)if(s!=null&&s.isItemEqual(n)&&left>0){int add=Math.min(left,s.getMaxStackSize()-s.stackSize);s.stackSize+=add;left-=add;}for(int i=0;i<materials.length&&left>0;i++)if(materials[i]==null){materials[i]=n.copy();materials[i].stackSize=Math.min(left,n.getMaxStackSize());left-=materials[i].stackSize;}}
    private void pause(BuilderJob j,BuilderState s){setWorkState(j,s,"builder.status.paused","Work paused.");}
    private void setWorkState(BuilderJob j,BuilderState next,String reason,String detail){if(state!=next)XFLog.debug("Builder "+getUniqueID()+" state "+state+" -> "+next+(detail.isEmpty()?"":" ("+detail+")"));state=next;if(j!=null){j.state=next;j.statusKey=reason;j.failureDetail=detail;BuilderJobData d=BuilderJobData.get(worldObj);if(d!=null)d.markDirty();}}
    private void reattach(){TileEntityMachineBuilder d=depot();if(d==null||!getUniqueID().equals(d.getAssignedBuilderId())||d.getActiveJobId()==null)return;BuilderJobData jobs=BuilderJobData.get(worldObj);BuilderJob j=jobs==null?null:jobs.get(d.getActiveJobId());if(j!=null&&(j.builderId==null||getUniqueID().equals(j.builderId))){jobId=j.jobId;j.builderId=getUniqueID();setWorkState(j,BuilderState.LOAD_JOB,"builder.status.loading","Loading Builder job.");}}
    private void resetPath(){getNavigator().clearPathEntity();pathFailures=0;candidateIndex=0;pathTargetX=Integer.MIN_VALUE;lastDistance=Double.MAX_VALUE;}
    private static double distanceSq(double ax,double ay,double az,double bx,double by,double bz){double x=ax-bx,y=ay-by,z=az-bz;return x*x+y*y+z*z;}
    private static String coord(int x,int y,int z){return x+", "+y+", "+z;}
    private static String blockName(Block b){return String.valueOf(GameData.getBlockRegistry().getNameForObject(b));}
    private static String capitalize(String s){return s.length()==0?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    @Override public void onDeath(DamageSource d){if(!worldObj.isRemote){BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob j=data==null?null:data.get(jobId);if(j!=null){j.builderId=null;pause(j,BuilderState.PAUSED);}TileEntityMachineBuilder depot=depot();if(depot!=null)depot.clearBuilder(getUniqueID());}super.onDeath(d);}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);put(n,"FactionUUID",factionId);put(n,"JobUUID",jobId);n.setInteger("DepotX",depotX);n.setInteger("DepotY",depotY);n.setInteger("DepotZ",depotZ);n.setInteger("DepotDimension",depotDimension);n.setString("BuilderState",state.name());NBTTagList l=new NBTTagList();for(int i=0;i<materials.length;i++)if(materials[i]!=null){NBTTagCompound c=new NBTTagCompound();c.setByte("Slot",(byte)i);materials[i].writeToNBT(c);l.appendTag(c);}n.setTag("BuilderInventory",l);}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);factionId=id(n,"FactionUUID");jobId=id(n,"JobUUID");depotX=n.getInteger("DepotX");depotY=n.getInteger("DepotY");depotZ=n.getInteger("DepotZ");depotDimension=n.hasKey("DepotDimension")?n.getInteger("DepotDimension"):dimension;try{state=BuilderState.valueOf(n.getString("BuilderState"));}catch(Exception e){state=BuilderState.IDLE;}NBTTagList l=n.getTagList("BuilderInventory",10);for(int i=0;i<l.tagCount();i++){NBTTagCompound c=l.getCompoundTagAt(i);int k=c.getByte("Slot");if(k>=0&&k<materials.length)materials[k]=ItemStack.loadItemStackFromNBT(c);}resetPath();}
    private static void put(NBTTagCompound n,String k,UUID v){if(v!=null)n.setString(k,v.toString());}private static UUID id(NBTTagCompound n,String k){try{return UUID.fromString(n.getString(k));}catch(Exception e){return null;}}
}
