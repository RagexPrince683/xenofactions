package com.hfr.entity;

import java.util.UUID;
import com.hfr.builder.*;
import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.hfr.blocks.ModBlocks;
import com.hfr.schematic.Schematic;
import com.hfr.schematic.SchematicStoreData;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;

/** Persistent faction worker. All work and inventory mutation occurs on the logical server. */
public class EntityFactionBuilder extends EntityLiving {
	private UUID factionId,jobId; private int depotX,depotY,depotZ,depotDimension,pathFailures; private BuilderState state=BuilderState.IDLE;
	private final ItemStack[] materials=new ItemStack[9];
	public EntityFactionBuilder(World w){super(w);setSize(.6F,1.8F);}
	@Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.28);}
	@Override protected boolean canDespawn(){return false;}
	public void assign(UUID faction,UUID job,int x,int y,int z,int dimension){factionId=faction;jobId=job;depotX=x;depotY=y;depotZ=z;depotDimension=dimension;state=job==null?BuilderState.IDLE:BuilderState.LOAD_JOB;}
	public BuilderState getBuilderState(){return state;} public UUID getJobId(){return jobId;} public UUID getFactionId(){return factionId;}
	public int getDepotX(){return depotX;} public int getDepotY(){return depotY;} public int getDepotZ(){return depotZ;} public int getDepotDimension(){return depotDimension;}
	public String getBuilderDisplayName(){return hasCustomNameTag()?getCustomNameTag():"Builder";}
	public String getCarriedMaterialSummary(){StringBuilder out=new StringBuilder();for(ItemStack stack:materials)if(stack!=null){if(out.length()>0)out.append(", ");out.append(stack.getDisplayName()).append(" x").append(stack.stackSize);}return out.toString();}
	public void setJob(UUID id){jobId=id;state=id==null?BuilderState.IDLE:BuilderState.LOAD_JOB;}
	public void pauseWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null)pause(j,BuilderState.PAUSED);else state=BuilderState.PAUSED;getNavigator().clearPathEntity();}
	public void resumeWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null){state=BuilderState.LOAD_JOB;j.state=state;d.markDirty();}}
	public void recallToDepot(){getNavigator().clearPathEntity();BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob job=d==null?null:d.get(jobId);setWorkState(job,BuilderState.GETTING_MATERIALS,"builder.status.getting_materials");getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);}
	public void clearJob(){jobId=null;state=BuilderState.IDLE;getNavigator().clearPathEntity();}
	@Override public boolean interact(EntityPlayer player){if(worldObj.isRemote)return true;TileEntityMachineBuilder d=depot();if(d==null||!getUniqueID().equals(d.getAssignedBuilderId())){player.addChatMessage(new ChatComponentTranslation("builder.depot.missing"));return true;}FMLNetworkHandler.openGui(player,MainRegistry.instance,ModBlocks.guiID_builder_npc,worldObj,depotX,depotY,depotZ);return true;}
	public void detachFromDepot(){BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob job=data==null?null:data.get(jobId);if(job!=null)pause(job,BuilderState.PAUSED);jobId=null;state=BuilderState.PAUSED;}
	@Override public void onLivingUpdate(){super.onLivingUpdate();if(!worldObj.isRemote&&XFConfig.enableFactionBuilders&&ticksExisted%XFConfig.builderWorkIntervalTicks==0)work();}
	private void work(){
		BuilderJobData data=BuilderJobData.get(worldObj); BuilderJob job=data==null?null:data.get(jobId);
		if(job==null){reattach();data=BuilderJobData.get(worldObj);job=data==null?null:data.get(jobId);if(job==null){state=BuilderState.IDLE;return;}} if(job.dimension!=worldObj.provider.dimensionId){pause(job,BuilderState.PAUSED);return;}
		if(job.state==BuilderState.PAUSED||job.state==BuilderState.UNSUPPORTED_BLOCK||job.state==BuilderState.INVALID_TERRITORY||job.state==BuilderState.PATHFINDING_ERROR){state=job.state;return;}
		job.builderId=getUniqueID(); Schematic s=find(job.schematicId); if(s==null){setWorkState(job,BuilderState.PAUSED,"builder.status.schematic_missing");return;}
		int total=s.width*s.height*s.length, scans=0;
		while(job.blockIndex<total&&scans++<XFConfig.builderBlockScanBudget){
			int i=job.blockIndex,z=i%s.length,y=(i/s.length)%s.height,x=i/s.length/s.height;
			int[] transformed=com.hfr.schematic.SchematicTransform.position(s,x,y,z,job.rotation,job.mirrored);
			int wx=job.originX+transformed[0],wy=job.originY+transformed[1],wz=job.originZ+transformed[2];job.targetX=wx;job.targetY=wy;job.targetZ=wz;job.schematicX=x;job.schematicY=y;job.schematicZ=z; Block wanted=s.resolveBlock(x,y,z); int meta=com.hfr.schematic.SchematicTransform.metadata(wanted,s.getMetadata(x,y,z),job.rotation,job.mirrored);
			if(!worldObj.blockExists(wx,wy,wz)){job.waitingChunkX=wx>>4;job.waitingChunkZ=wz>>4;setWorkState(job,BuilderState.WAITING_FOR_CHUNK,"builder.status.waiting_chunk");return;}
			if(wanted==null){job.unsupportedBlock=s.getBlockName(x,y,z);job.unsupportedMeta=s.getMetadata(x,y,z);setWorkState(job,BuilderState.UNSUPPORTED_BLOCK,"builder.status.unsupported");return;}
			Block existing=worldObj.getBlock(wx,wy,wz); if(existing==wanted&&worldObj.getBlockMetadata(wx,wy,wz)==meta){job.blockIndex++;continue;}
			if(getDistanceSq(wx+.5,wy+.5,wz+.5)>16){setWorkState(job,BuilderState.MOVE_TO_SITE,"builder.status.moving");if(getNavigator().noPath()&&!getNavigator().tryMoveToXYZ(wx+.5,wy,wz+.5,1)){if(++pathFailures>=5){job.pathFailures=pathFailures;setWorkState(job,BuilderState.PATHFINDING_ERROR,"builder.status.cannot_reach");};}return;}
			pathFailures=0;
			if(existing!=Blocks.air){setWorkState(job,BuilderState.BREAKING_BLOCK,"builder.status.breaking");if(BuilderPlacement.protectedBlock(worldObj,wx,wy,wz)||!BuilderPlacement.breakBlock(worldObj,wx,wy,wz,factionId)){blocked(job,wx,wy,wz);return;}data.markDirty();return;}
			if(wanted==Blocks.air){job.blockIndex++;data.markDirty();continue;}
			ItemStack need=BuilderMaterialResolver.resolve(wanted,meta); if(need==null){job.unsupportedBlock=String.valueOf(GameData.getBlockRegistry().getNameForObject(wanted));job.unsupportedMeta=meta;setWorkState(job,BuilderState.UNSUPPORTED_BLOCK,"builder.status.unsupported");return;}job.requiredItem=String.valueOf(GameData.getItemRegistry().getNameForObject(need.getItem()));job.requiredMeta=need.getItemDamage();
			int slot=findMaterial(materials,need); TileEntityMachineBuilder depot=depot();
			if(slot<0){if(getDistanceSq(depotX+.5,depotY+.5,depotZ+.5)>16){setWorkState(job,BuilderState.GETTING_MATERIALS,"builder.status.getting_materials");getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);return;} if(depot!=null&&depot.takeMaterial(need)){add(need.copy());slot=findMaterial(materials,need);} }
			if(slot<0){job.missingQuantity=1;setWorkState(job,BuilderState.WAITING_FOR_MATERIALS,"builder.status.waiting_materials");return;}
			setWorkState(job,BuilderState.PLACING_BLOCK,"builder.status.building");if(BuilderPlacement.place(worldObj,wx,wy,wz,wanted,meta,factionId)){if(--materials[slot].stackSize<=0)materials[slot]=null;job.blockIndex++;data.markDirty();return;}blocked(job,wx,wy,wz);return;
		}
		if(job.blockIndex>=total){setWorkState(job,BuilderState.COMPLETE,"builder.status.complete");TileEntityMachineBuilder d=depot();if(d!=null){d.setActiveJob(null);clearJob();BuilderDepotService.advance(d);}}else setWorkState(job,BuilderState.FIND_NEXT_BLOCK,"builder.status.finding");
	}
	private Schematic find(String id){SchematicStoreData store=SchematicStoreData.get(worldObj);Schematic stored=store==null?null:store.getSchematic(id);if(stored!=null)return stored;for(Schematic s:MainRegistry.schems)if(s!=null&&s.name.equals(id))return s;return null;}
	private TileEntityMachineBuilder depot(){return worldObj.provider.dimensionId==depotDimension&&worldObj.getTileEntity(depotX,depotY,depotZ) instanceof TileEntityMachineBuilder?(TileEntityMachineBuilder)worldObj.getTileEntity(depotX,depotY,depotZ):null;}
	private static int findMaterial(ItemStack[] a,ItemStack n){for(int i=0;i<a.length;i++)if(a[i]!=null&&a[i].isItemEqual(n))return i;return -1;}
	private void add(ItemStack n){for(int i=0;i<materials.length;i++)if(materials[i]==null){materials[i]=n;return;}}
	private void pause(BuilderJob j,BuilderState s){setWorkState(j,s,"builder.status.paused");}
	private void setWorkState(BuilderJob j,BuilderState next,String reason){state=next;if(j!=null){j.state=next;j.statusKey=reason;BuilderJobData d=BuilderJobData.get(worldObj);if(d!=null)d.markDirty();}}
	private void reattach(){TileEntityMachineBuilder d=depot();if(d==null||!getUniqueID().equals(d.getAssignedBuilderId())||d.getActiveJobId()==null)return;BuilderJobData jobs=BuilderJobData.get(worldObj);BuilderJob j=jobs==null?null:jobs.get(d.getActiveJobId());if(j!=null&&(j.builderId==null||getUniqueID().equals(j.builderId))){jobId=j.jobId;j.builderId=getUniqueID();setWorkState(j,BuilderState.LOAD_JOB,"builder.status.loading");}}
	private void syncState(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null){j.state=state;d.markDirty();}}
	private void blocked(BuilderJob j,int x,int y,int z){j.blockedX=x;j.blockedY=y;j.blockedZ=z;pause(j,BuilderState.INVALID_TERRITORY);}
	@Override public void onDeath(DamageSource d){if(!worldObj.isRemote){BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob j=data==null?null:data.get(jobId);if(j!=null){j.builderId=null;pause(j,BuilderState.PAUSED);}TileEntityMachineBuilder depot=depot();if(depot!=null)depot.clearBuilder(getUniqueID());}super.onDeath(d);}
	@Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);put(n,"FactionUUID",factionId);put(n,"JobUUID",jobId);n.setInteger("DepotX",depotX);n.setInteger("DepotY",depotY);n.setInteger("DepotZ",depotZ);n.setInteger("DepotDimension",depotDimension);n.setString("BuilderState",state.name());NBTTagList l=new NBTTagList();for(int i=0;i<materials.length;i++)if(materials[i]!=null){NBTTagCompound c=new NBTTagCompound();c.setByte("Slot",(byte)i);materials[i].writeToNBT(c);l.appendTag(c);}n.setTag("BuilderInventory",l);}
	@Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);factionId=id(n,"FactionUUID");jobId=id(n,"JobUUID");depotX=n.getInteger("DepotX");depotY=n.getInteger("DepotY");depotZ=n.getInteger("DepotZ");depotDimension=n.hasKey("DepotDimension")?n.getInteger("DepotDimension"):dimension;try{state=BuilderState.valueOf(n.getString("BuilderState"));}catch(Exception e){state=BuilderState.IDLE;}NBTTagList l=n.getTagList("BuilderInventory",10);for(int i=0;i<l.tagCount();i++){NBTTagCompound c=l.getCompoundTagAt(i);int k=c.getByte("Slot");if(k>=0&&k<materials.length)materials[k]=ItemStack.loadItemStackFromNBT(c);}}
	private static void put(NBTTagCompound n,String k,UUID v){if(v!=null)n.setString(k,v.toString());}private static UUID id(NBTTagCompound n,String k){try{return UUID.fromString(n.getString(k));}catch(Exception e){return null;}}
}
