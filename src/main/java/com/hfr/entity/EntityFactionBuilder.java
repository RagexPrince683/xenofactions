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
import net.minecraft.pathfinding.PathEntity;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;

/** Persistent faction worker. All work and inventory mutation occurs on the logical server. */
public class EntityFactionBuilder extends EntityLiving implements net.minecraft.inventory.IInventory {
    public static final int INVENTORY_SIZE = 27;
    private static final int WORK_REACH=4,STALL_TICKS=80;
    private static final double USEFUL_MOVE_SQ=.04D,USEFUL_CLOSER=.05D;
    private UUID factionId,jobId; private int depotX,depotY,depotZ,depotDimension,pathFailures,candidateIndex,pathTargetX=Integer.MIN_VALUE,pathTargetY,pathTargetZ,lastProgressTick;
    private double lastX,lastY,lastZ,lastDistance=Double.MAX_VALUE;
    private int[][] workCandidates;
    private BuilderState state=BuilderState.IDLE;
    private final ItemStack[] inventory=new ItemStack[INVENTORY_SIZE];
    public EntityFactionBuilder(World w){super(w);setSize(.6F,1.8F);getNavigator().setAvoidsWater(false);getNavigator().setCanSwim(true);stepHeight=1.0F;}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.28);}
    @Override protected boolean canDespawn(){return false;}
    /** EntityLiving only ticks its navigator and move helper through the new-AI branch. */
    @Override protected boolean isAIEnabled(){return true;}
    public void assign(UUID faction,UUID job,int x,int y,int z,int dimension){factionId=faction;jobId=job;depotX=x;depotY=y;depotZ=z;depotDimension=dimension;state=job==null?BuilderState.IDLE:BuilderState.LOAD_JOB;resetPath();}
    public BuilderState getBuilderState(){return state;} public UUID getJobId(){return jobId;} public UUID getFactionId(){return factionId;}
    public int getDepotX(){return depotX;} public int getDepotY(){return depotY;} public int getDepotZ(){return depotZ;} public int getDepotDimension(){return depotDimension;}
    public String getBuilderDisplayName(){return hasCustomNameTag()?getCustomNameTag():"Builder";}
    public String getCarriedMaterialSummary(){StringBuilder out=new StringBuilder();for(ItemStack stack:inventory)if(stack!=null){if(out.length()>0)out.append(", ");out.append(stack.getDisplayName()).append(" x").append(stack.stackSize);}return out.toString();}
    public void setJob(UUID id){jobId=id;state=id==null?BuilderState.IDLE:BuilderState.LOAD_JOB;resetPath();}
    public void pauseWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null)pause(j,BuilderState.PAUSED);else state=BuilderState.PAUSED;resetPath();}
    public void resumeWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null){resetPath();j.failureDetail="";j.pathFailures=0;j.passUnresolved=0;Schematic s=find(j.schematicId);if(s!=null)j.blockIndex=j.constructionLayer*s.width*s.length;state=BuilderState.LOAD_JOB;j.state=state;d.markDirty();}}
    public void recallToDepot(){resetPath();BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob job=d==null?null:d.get(jobId);setWorkState(job,BuilderState.GETTING_MATERIALS,"builder.status.getting_materials","");getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);}
    public void clearJob(){jobId=null;state=BuilderState.IDLE;resetPath();}
    @Override public boolean interact(EntityPlayer player){if(worldObj.isRemote)return true;TileEntityMachineBuilder d=depot();if(BuilderGuiResolver.getAssignedBuilder(d)!=this){player.addChatMessage(new ChatComponentTranslation("builder.depot.missing"));return true;}com.hfr.util.XFLog.debug("[XF Builder] Opening NPC GUI server window: builder="+getUniqueID()+", depot="+depotX+","+depotY+","+depotZ+", slots="+com.hfr.inventory.container.ContainerBuilderNPC.TOTAL_SLOTS);FMLNetworkHandler.openGui(player,MainRegistry.instance,ModBlocks.guiID_builder_npc,worldObj,depotX,depotY,depotZ);return true;}
    public void detachFromDepot(){BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob job=data==null?null:data.get(jobId);if(job!=null)pause(job,BuilderState.PAUSED);jobId=null;state=BuilderState.PAUSED;resetPath();}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!worldObj.isRemote&&XFConfig.enableFactionBuilders){if(ticksExisted%10==0&&jobId!=null&&state!=BuilderState.PAUSED)pickUpNearbyItems();if(ticksExisted%XFConfig.builderWorkIntervalTicks==0)work();}}

    private void work(){
        BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob job=data==null?null:data.get(jobId);
        if(job==null){reattach();data=BuilderJobData.get(worldObj);job=data==null?null:data.get(jobId);if(job==null){state=BuilderState.IDLE;return;}}
        if(job.dimension!=worldObj.provider.dimensionId){pause(job,BuilderState.PAUSED);return;}
        if(job.state==BuilderState.PAUSED||job.state==BuilderState.UNSUPPORTED_BLOCK||job.state==BuilderState.INVALID_TERRITORY||job.state==BuilderState.PATHFINDING_ERROR){state=job.state;return;}
        job.builderId=getUniqueID();Schematic s=find(job.schematicId);if(s==null){setWorkState(job,BuilderState.PAUSED,"builder.status.schematic_missing","Schematic is missing.");return;}
        int total=s.size(),perLayer=s.width*s.length;
        if(job.workOrderVersion!=BuilderJob.CURRENT_WORK_ORDER){job.workOrderVersion=BuilderJob.CURRENT_WORK_ORDER;job.blockIndex=0;job.constructionLayer=0;job.passUnresolved=0;job.completedBlocks=0;resetPath();data.markDirty();XFLog.debug("Builder migrated job "+job.jobId+" to bottom-up work order; world will be rescanned");}
        if(job.constructionLayer>=s.height){setWorkState(job,BuilderState.COMPLETE,"builder.status.complete","Construction complete.");TileEntityMachineBuilder d=depot();if(d!=null){d.setActiveJob(null);clearJob();BuilderDepotService.advance(d);}return;}
        int layerEnd=(job.constructionLayer+1)*perLayer,scans=0;
        if(job.blockIndex<job.constructionLayer*perLayer||job.blockIndex>layerEnd)job.blockIndex=job.constructionLayer*perLayer;
        while(job.blockIndex<layerEnd&&scans++<XFConfig.builderBlockScanBudget){
            int i=job.blockIndex,y=i/perLayer,within=i%perLayer,z=within%s.length,x=within/s.length;
            int[] world=com.hfr.schematic.SchematicTransform.worldPosition(s,x,y,z,job.rotation,job.mirrored,job.originX,job.originY,job.originZ);int wx=world[0],wy=world[1],wz=world[2];
            if(wx!=job.targetX||wy!=job.targetY||wz!=job.targetZ)resetPath();job.targetX=wx;job.targetY=wy;job.targetZ=wz;job.schematicX=x;job.schematicY=y;job.schematicZ=z;
            Block wanted=s.resolveBlock(x,y,z);int meta=com.hfr.schematic.SchematicTransform.metadata(wanted,s.getMetadata(x,y,z),job.rotation,job.mirrored);
            if(!worldObj.blockExists(wx,wy,wz)){job.waitingChunkX=wx>>4;job.waitingChunkZ=wz>>4;setWorkState(job,BuilderState.WAITING_FOR_CHUNK,"builder.status.waiting_chunk","Waiting for chunk "+job.waitingChunkX+", "+job.waitingChunkZ+" containing "+coord(wx,wy,wz)+".");return;}
            if(wanted==null){job.unsupportedBlock=s.getBlockName(x,y,z);job.unsupportedMeta=s.getMetadata(x,y,z);setWorkState(job,BuilderState.UNSUPPORTED_BLOCK,"builder.status.unsupported","Unsupported block "+job.unsupportedBlock+":"+job.unsupportedMeta+" at "+coord(wx,wy,wz)+".");return;}
            Block existing=worldObj.getBlock(wx,wy,wz);if(existing==wanted&&worldObj.getBlockMetadata(wx,wy,wz)==meta){job.blockIndex++;job.completedBlocks=Math.min(total,job.completedBlocks+1);data.markDirty();resetPath();continue;}
            XFLog.debug("Builder target index="+i+" local="+x+","+y+","+z+" world="+coord(wx,wy,wz)+" action="+(wanted==Blocks.air?"BREAK":"PLACE "+blockName(wanted)+":"+meta)+" constructionLayer="+(job.originY+y));
            ItemStack need=null;if(wanted!=Blocks.air){need=BuilderMaterialResolver.resolve(wanted,meta);if(need==null){job.unsupportedBlock=blockName(wanted);job.unsupportedMeta=meta;setWorkState(job,BuilderState.UNSUPPORTED_BLOCK,"builder.status.unsupported","Unsupported block "+job.unsupportedBlock+":"+meta+" at "+coord(wx,wy,wz)+".");return;}}
            int move=wanted==Blocks.air&&canWork(wx,wy,wz)?1:moveToWork(job,wx,wy,wz,wanted!=Blocks.air);
            if(move==0)return;if(move<0){job.passUnresolved++;job.blockIndex++;data.markDirty();resetPath();XFLog.debug("Deferred target "+coord(wx,wy,wz)+"; scanning alternate in layer "+(job.originY+y));continue;}
            pathFailures=0;candidateIndex=0;getNavigator().clearPathEntity();
            if(existing!=Blocks.air){setWorkState(job,BuilderState.BREAKING_BLOCK,"builder.status.breaking","Clearing "+blockName(existing)+" at "+coord(wx,wy,wz));BuilderPlacement.Result result=BuilderPlacement.breakBlockResult(worldObj,wx,wy,wz,factionId);if(result!=BuilderPlacement.Result.SUCCESS){placementFailure(job,result,wx,wy,wz,"break");return;}data.markDirty();resetPath();return;}
            if(wanted==Blocks.air){job.blockIndex++;job.completedBlocks=Math.min(total,job.completedBlocks+1);data.markDirty();resetPath();continue;}
            if(intersectsTarget(wx,wy,wz,wanted)){resetPath();return;}
            setRequirement(job,need,remainingNeed(s,job,need));if(findMaterial(inventory,need)<0&&!fetchMaterial(job,need))return;int slot=findMaterial(inventory,need);if(slot<0)return;
            setWorkState(job,BuilderState.PLACING_BLOCK,"builder.status.building","Placing "+need.getDisplayName()+" at "+coord(wx,wy,wz));BuilderPlacement.Result result=BuilderPlacement.placeResult(worldObj,wx,wy,wz,wanted,meta,factionId);
            if(result==BuilderPlacement.Result.SUCCESS){if(--inventory[slot].stackSize<=0)inventory[slot]=null;job.blockIndex=job.constructionLayer*perLayer;job.completedBlocks=Math.min(total,job.completedBlocks+1);job.missingQuantity=0;job.passUnresolved=0;data.markDirty();resetPath();return;}placementFailure(job,result,wx,wy,wz,"place");return;
        }
        if(job.blockIndex>=layerEnd){if(job.passUnresolved>0){int unresolved=job.passUnresolved;job.blockIndex=job.constructionLayer*perLayer;job.passUnresolved=0;pathError(job,"No reachable construction targets remain. Lowest unresolved layer: Y="+(job.originY+job.constructionLayer)+". Unresolved blocks: "+unresolved+". Nearest unresolved target: "+coord(job.targetX,job.targetY,job.targetZ)+".");}else{job.constructionLayer++;job.blockIndex=job.constructionLayer*perLayer;job.passUnresolved=0;resetPath();data.markDirty();setWorkState(job,BuilderState.FIND_NEXT_BLOCK,"builder.status.finding","Advancing to construction layer "+(job.originY+job.constructionLayer)+".");}}else setWorkState(job,BuilderState.FIND_NEXT_BLOCK,"builder.status.finding","Scanning for a reachable target in layer "+(job.originY+job.constructionLayer)+".");
    }

    private boolean fetchMaterial(BuilderJob job,ItemStack need){TileEntityMachineBuilder d=depot();if(d==null){setWorkState(job,BuilderState.WAITING_FOR_MATERIALS,"builder.status.waiting_materials","Builder Depot is unavailable; waiting for "+need.getDisplayName()+" x"+job.missingQuantity+".");return false;}if(getDistanceSq(depotX+.5,depotY+.5,depotZ+.5)>16){setWorkState(job,BuilderState.GETTING_MATERIALS,"builder.status.getting_materials","Fetching "+need.getDisplayName()+" from Depot at "+coord(depotX,depotY,depotZ));if(getNavigator().noPath())getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);return false;}int capacity=capacityFor(need);if(capacity<=0&&d.countMaterial(need)>0){setWorkState(job,BuilderState.BUILDER_INVENTORY_FULL,"builder.status.inventory_full","Builder inventory is full.");return false;}int amount=Math.min(Math.min(need.getMaxStackSize(),job.missingQuantity),capacity);int taken=d.takeMaterial(need,amount);if(taken>0){add(need,taken);XFLog.debug("Fetched "+job.requiredItem+":"+job.requiredMeta+" x"+taken);return true;}setWorkState(job,BuilderState.WAITING_FOR_MATERIALS,"builder.status.waiting_materials",waiting(job,need));return false;}
    /** 1 ready, 0 path in progress, -1 defer this target for the current layer pass. */
    private int moveToWork(BuilderJob job,int tx,int ty,int tz,boolean placingSolid){
        if(canWork(tx,ty,tz)&&!(placingSolid&&boundingBox.intersectsWith(AxisAlignedBB.getBoundingBox(tx,ty,tz,tx+1,ty+1,tz+1)))){getNavigator().clearPathEntity();return 1;}boolean sameTarget=pathTargetX==tx&&pathTargetY==ty&&pathTargetZ==tz;
        if(!sameTarget)workCandidates=candidates(tx,ty,tz,placingSolid);int[][] choices=workCandidates==null?new int[0][]:workCandidates;
        if(sameTarget&&!getNavigator().noPath()){double moved=distanceSq(posX,posY,posZ,lastX,lastY,lastZ),distance=getDistanceSq(job.workX+.5,job.workY,job.workZ+.5);if(canWork(tx,ty,tz))return 1;if(moved>=USEFUL_MOVE_SQ||distance<lastDistance-USEFUL_CLOSER){lastX=posX;lastY=posY;lastZ=posZ;lastDistance=distance;lastProgressTick=ticksExisted;return 0;}if(ticksExisted-lastProgressTick<STALL_TICKS)return 0;XFLog.debug("Builder path stalled; target="+coord(tx,ty,tz)+" work="+coord(job.workX,job.workY,job.workZ));getNavigator().clearPathEntity();candidateIndex++;}
        if(!sameTarget){candidateIndex=0;pathFailures=0;pathTargetX=tx;pathTargetY=ty;pathTargetZ=tz;}
        if(candidateIndex<choices.length){int[] c=choices[candidateIndex++];job.workX=c[0];job.workY=c[1];job.workZ=c[2];PathEntity path=getNavigator().getPathToXYZ(c[0]+.5,c[1],c[2]+.5);boolean ok=path!=null&&getNavigator().setPath(path,1);if(!ok){pathFailures++;job.pathFailures=pathFailures;XFLog.debug("Candidate "+coord(c[0],c[1],c[2])+" path=false");return 0;}lastX=posX;lastY=posY;lastZ=posZ;lastDistance=getDistanceSq(c[0]+.5,c[1],c[2]+.5);lastProgressTick=ticksExisted;job.pathFailures=pathFailures;setWorkState(job,BuilderState.MOVE_TO_SITE,"builder.status.moving","Moving to build site: "+coord(tx,ty,tz)+"; work position: "+coord(c[0],c[1],c[2]));XFLog.debug(pathDebug(tx,ty,tz,c[0],c[1],c[2],true)+" usable=true pathNodes="+path.getCurrentPathLength());return 0;}
        job.pathFailures=pathFailures;return -1;
    }
    private int[][] candidates(int tx,int ty,int tz,boolean placingSolid){List<int[]> out=new ArrayList<int[]>();for(int r=1;r<=WORK_REACH;r++)for(int dy=-2;dy<=2;dy++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;int x=tx+dx,y=ty+dy,z=tz+dz;if(placingSolid&&x==tx&&y==ty&&z==tz)continue;if(distanceSq(x+.5,y+getEyeHeight(),z+.5,tx+.5,ty+.5,tz+.5)>WORK_REACH*WORK_REACH)continue;if(!validStand(x,y,z)){XFLog.debug("Candidate "+coord(x,y,z)+" rejected: collision/support");continue;}if(!canWorkFrom(x+.5,y,z+.5,tx,ty,tz)){XFLog.debug("Candidate "+coord(x,y,z)+" rejected: target face obstructed");continue;}out.add(new int[]{x,y,z});}Collections.sort(out,new Comparator<int[]>(){public int compare(int[] a,int[] b){return Double.compare(getDistanceSq(a[0]+.5,a[1],a[2]+.5),getDistanceSq(b[0]+.5,b[1],b[2]+.5));}});return out.toArray(new int[out.size()][]);}
    private boolean validStand(int x,int y,int z){if(y<=0||y+1>=worldObj.getHeight()||!worldObj.blockExists(x,y,z))return false;AxisAlignedBB body=AxisAlignedBB.getBoundingBox(x+.2,y,z+.2,x+.8,y+1.8,z+.8);if(!worldObj.getCollidingBoundingBoxes(this,body).isEmpty())return false;AxisAlignedBB support=AxisAlignedBB.getBoundingBox(x+.25,y-.60,z+.25,x+.75,y+.02,z+.75);return !worldObj.getCollidingBoundingBoxes(this,support).isEmpty();}
    private boolean canWork(int x,int y,int z){return canWorkFrom(posX,posY,posZ,x,y,z);}
    private boolean canWorkFrom(double px,double py,double pz,int x,int y,int z){double eyeY=py+getEyeHeight();double[][] points={{.5,.5,.5},{.01,.5,.5},{.99,.5,.5},{.5,.01,.5},{.5,.99,.5},{.5,.5,.01},{.5,.5,.99}};for(double[] p:points){double tx=x+p[0],ty=y+p[1],tz=z+p[2];if(distanceSq(px,eyeY,pz,tx,ty,tz)>WORK_REACH*WORK_REACH)continue;MovingObjectPosition hit=worldObj.rayTraceBlocks(Vec3.createVectorHelper(px,eyeY,pz),Vec3.createVectorHelper(tx,ty,tz));if(hit==null||(hit.blockX==x&&hit.blockY==y&&hit.blockZ==z))return true;}return false;}
    private boolean intersectsTarget(int x,int y,int z,Block wanted){AxisAlignedBB collision=wanted.getCollisionBoundingBoxFromPool(worldObj,x,y,z);return collision!=null&&collision.intersectsWith(boundingBox);}
    private String pathDebug(int tx,int ty,int tz,int x,int y,int z,boolean path){Block under=worldObj.getBlock(MathHelper.floor_double(posX),MathHelper.floor_double(boundingBox.minY-.01),MathHelper.floor_double(posZ));return "Builder pos="+posX+","+posY+","+posZ+" under="+blockName(under)+" target="+tx+","+ty+","+tz+" work="+x+","+y+","+z+" path="+path;}
    private void pathError(BuilderJob job,String detail){setWorkState(job,BuilderState.PATHFINDING_ERROR,"builder.status.cannot_reach",detail);getNavigator().clearPathEntity();XFLog.debug(detail);}

    private void placementFailure(BuilderJob job,BuilderPlacement.Result result,int x,int y,int z,String action){job.blockedX=x;job.blockedY=y;job.blockedZ=z;BuilderState next=result==BuilderPlacement.Result.INVALID_TERRITORY||result==BuilderPlacement.Result.PROTECTED_BLOCK?BuilderState.INVALID_TERRITORY:result==BuilderPlacement.Result.UNSUPPORTED_BLOCK?BuilderState.UNSUPPORTED_BLOCK:BuilderState.PAUSED;setWorkState(job,next,"builder.status."+action+"_failed",capitalize(result.name().toLowerCase().replace('_',' '))+" while attempting to "+action+" at "+coord(x,y,z)+".");}
    private int remainingNeed(Schematic s,BuilderJob job,ItemStack need){int count=0,total=s.size(),perLayer=s.width*s.length;for(int i=0;i<total;i++){int y=i/perLayer,within=i%perLayer,z=within%s.length,x=within/s.length;Block b=s.resolveBlock(x,y,z);if(b==null||b==Blocks.air)continue;int m=com.hfr.schematic.SchematicTransform.metadata(b,s.getMetadata(x,y,z),job.rotation,job.mirrored);ItemStack n=BuilderMaterialResolver.resolve(b,m);int[] w=com.hfr.schematic.SchematicTransform.worldPosition(s,x,y,z,job.rotation,job.mirrored,job.originX,job.originY,job.originZ);if(n!=null&&BuilderMaterialResolver.matches(n,need)&&(worldObj.getBlock(w[0],w[1],w[2])!=b||worldObj.getBlockMetadata(w[0],w[1],w[2])!=m))count++;}return Math.max(0,count-countMaterial(inventory,need));}
    private void setRequirement(BuilderJob job,ItemStack need,int missing){job.requiredItem=String.valueOf(GameData.getItemRegistry().getNameForObject(need.getItem()));job.requiredMeta=need.getItemDamage();job.missingQuantity=Math.max(1,missing);}
    private String waiting(BuilderJob job,ItemStack need){return "Waiting for: "+need.getDisplayName()+" x"+job.missingQuantity;}
    private Schematic find(String id){SchematicStoreData store=SchematicStoreData.get(worldObj);Schematic stored=store==null?null:store.getSchematic(id);if(stored!=null)return stored;for(Schematic s:MainRegistry.schems)if(s!=null&&s.name.equals(id))return s;return null;}
    private TileEntityMachineBuilder depot(){return worldObj.provider.dimensionId==depotDimension&&worldObj.getTileEntity(depotX,depotY,depotZ) instanceof TileEntityMachineBuilder?(TileEntityMachineBuilder)worldObj.getTileEntity(depotX,depotY,depotZ):null;}
    private static int findMaterial(ItemStack[] a,ItemStack n){for(int i=0;i<a.length;i++)if(a[i]!=null&&BuilderMaterialResolver.matches(a[i],n))return i;return -1;}
    private static int countMaterial(ItemStack[] a,ItemStack n){int count=0;for(ItemStack s:a)if(s!=null&&BuilderMaterialResolver.matches(s,n))count+=s.stackSize;return count;}
    private int capacityFor(ItemStack n){int capacity=0;for(ItemStack s:inventory)if(s==null)capacity+=n.getMaxStackSize();else if(BuilderMaterialResolver.matches(s,n))capacity+=Math.max(0,s.getMaxStackSize()-s.stackSize);return capacity;}
    private void add(ItemStack n,int amount){int left=amount;for(ItemStack s:inventory)if(s!=null&&BuilderMaterialResolver.matches(s,n)&&left>0){int add=Math.min(left,s.getMaxStackSize()-s.stackSize);s.stackSize+=add;left-=add;}for(int i=0;i<inventory.length&&left>0;i++)if(inventory[i]==null){inventory[i]=n.copy();inventory[i].stackSize=Math.min(left,n.getMaxStackSize());left-=inventory[i].stackSize;}}
    private void pause(BuilderJob j,BuilderState s){setWorkState(j,s,"builder.status.paused","Work paused.");}
    private void setWorkState(BuilderJob j,BuilderState next,String reason,String detail){if(state!=next)XFLog.debug("Builder "+getUniqueID()+" state "+state+" -> "+next+(detail.isEmpty()?"":" ("+detail+")"));state=next;if(j!=null){j.state=next;j.statusKey=reason;j.failureDetail=detail;BuilderJobData d=BuilderJobData.get(worldObj);if(d!=null)d.markDirty();}}
    private void reattach(){TileEntityMachineBuilder d=depot();if(d==null||!getUniqueID().equals(d.getAssignedBuilderId())||d.getActiveJobId()==null)return;BuilderJobData jobs=BuilderJobData.get(worldObj);BuilderJob j=jobs==null?null:jobs.get(d.getActiveJobId());if(j!=null&&(j.builderId==null||getUniqueID().equals(j.builderId))){jobId=j.jobId;j.builderId=getUniqueID();setWorkState(j,BuilderState.LOAD_JOB,"builder.status.loading","Loading Builder job.");}}
    private void resetPath(){getNavigator().clearPathEntity();pathFailures=0;candidateIndex=0;pathTargetX=Integer.MIN_VALUE;lastDistance=Double.MAX_VALUE;workCandidates=null;}
    private static double distanceSq(double ax,double ay,double az,double bx,double by,double bz){double x=ax-bx,y=ay-by,z=az-bz;return x*x+y*y+z*z;}
    private static String coord(int x,int y,int z){return x+", "+y+", "+z;}
    private static String blockName(Block b){return String.valueOf(GameData.getBlockRegistry().getNameForObject(b));}
    private static String capitalize(String s){return s.length()==0?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    @Override public void onDeath(DamageSource d){if(!worldObj.isRemote){for(int i=0;i<inventory.length;i++)if(inventory[i]!=null){entityDropItem(inventory[i],0);inventory[i]=null;}BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob j=data==null?null:data.get(jobId);if(j!=null){j.builderId=null;pause(j,BuilderState.PAUSED);}TileEntityMachineBuilder depot=depot();if(depot!=null)depot.clearBuilder(getUniqueID());}super.onDeath(d);}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);put(n,"FactionUUID",factionId);put(n,"JobUUID",jobId);n.setInteger("DepotX",depotX);n.setInteger("DepotY",depotY);n.setInteger("DepotZ",depotZ);n.setInteger("DepotDimension",depotDimension);n.setString("BuilderState",state.name());NBTTagList l=new NBTTagList();for(int i=0;i<inventory.length;i++)if(inventory[i]!=null){NBTTagCompound c=new NBTTagCompound();c.setByte("Slot",(byte)i);inventory[i].writeToNBT(c);l.appendTag(c);}n.setTag("BuilderInventory",l);}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);factionId=id(n,"FactionUUID");jobId=id(n,"JobUUID");depotX=n.getInteger("DepotX");depotY=n.getInteger("DepotY");depotZ=n.getInteger("DepotZ");depotDimension=n.hasKey("DepotDimension")?n.getInteger("DepotDimension"):dimension;try{state=BuilderState.valueOf(n.getString("BuilderState"));}catch(Exception e){state=BuilderState.IDLE;}NBTTagList l=n.getTagList("BuilderInventory",10);for(int i=0;i<l.tagCount();i++){NBTTagCompound c=l.getCompoundTagAt(i);int k=c.getByte("Slot");if(k>=0&&k<inventory.length)inventory[k]=ItemStack.loadItemStackFromNBT(c);}resetPath();}
    private void pickUpNearbyItems(){AxisAlignedBB area=boundingBox.expand(2,1,2);for(Object value:worldObj.getEntitiesWithinAABB(net.minecraft.entity.item.EntityItem.class,area)){net.minecraft.entity.item.EntityItem item=(net.minecraft.entity.item.EntityItem)value;if(item.isDead||item.delayBeforeCanPickup>0)continue;ItemStack stack=item.getEntityItem();int inserted=insert(stack);if(inserted>0){stack.stackSize-=inserted;if(stack.stackSize<=0)item.setDead();else item.setEntityItemStack(stack);}}}
    /** Inserts as much as possible, returning the inserted count. */
    public int insert(ItemStack source){if(source==null||source.stackSize<=0)return 0;int before=source.stackSize,left=before;for(ItemStack stack:inventory)if(stack!=null&&BuilderMaterialResolver.matches(stack,source)&&left>0){int n=Math.min(left,Math.min(stack.getMaxStackSize(),getInventoryStackLimit())-stack.stackSize);if(n>0){stack.stackSize+=n;left-=n;}}for(int i=0;i<inventory.length&&left>0;i++)if(inventory[i]==null){inventory[i]=source.copy();inventory[i].stackSize=Math.min(left,Math.min(source.getMaxStackSize(),getInventoryStackLimit()));left-=inventory[i].stackSize;}return before-left;}
    public ItemStack[] copyInventory(){ItemStack[] copy=new ItemStack[inventory.length];for(int i=0;i<copy.length;i++)copy[i]=inventory[i]==null?null:inventory[i].copy();return copy;}
    @Override public int getSizeInventory(){return INVENTORY_SIZE;}
    @Override public ItemStack getStackInSlot(int slot){return slot>=0&&slot<inventory.length?inventory[slot]:null;}
    @Override public ItemStack decrStackSize(int slot,int amount){ItemStack stack=getStackInSlot(slot);if(stack==null)return null;if(stack.stackSize<=amount){inventory[slot]=null;return stack;}ItemStack split=stack.splitStack(amount);if(stack.stackSize<=0)inventory[slot]=null;return split;}
    @Override public ItemStack getStackInSlotOnClosing(int slot){ItemStack stack=getStackInSlot(slot);if(stack!=null)inventory[slot]=null;return stack;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot<0||slot>=inventory.length)return;inventory[slot]=stack;if(stack!=null&&stack.stackSize>getInventoryStackLimit())stack.stackSize=getInventoryStackLimit();}
    @Override public String getInventoryName(){return "container.builder_npc";}
    @Override public boolean hasCustomInventoryName(){return false;}
    @Override public int getInventoryStackLimit(){return 64;}
    @Override public void markDirty(){}
    @Override public boolean isUseableByPlayer(EntityPlayer player){return !isDead&&player.dimension==dimension&&player.getDistanceSqToEntity(this)<=64;}
    @Override public void openInventory(){}
    @Override public void closeInventory(){}
    @Override public boolean isItemValidForSlot(int slot,ItemStack stack){return true;}
    private static void put(NBTTagCompound n,String k,UUID v){if(v!=null)n.setString(k,v.toString());}private static UUID id(NBTTagCompound n,String k){try{return UUID.fromString(n.getString(k));}catch(Exception e){return null;}}
}
