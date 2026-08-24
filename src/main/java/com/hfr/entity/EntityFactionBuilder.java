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
	public void setJob(UUID id){jobId=id;state=id==null?BuilderState.IDLE:BuilderState.LOAD_JOB;}
	public void pauseWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null)pause(j,BuilderState.PAUSED);else state=BuilderState.PAUSED;getNavigator().clearPathEntity();}
	public void resumeWork(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null){state=BuilderState.LOAD_JOB;j.state=state;d.markDirty();}}
	public void recallToDepot(){getNavigator().clearPathEntity();state=BuilderState.GETTING_MATERIALS;getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);syncState();}
	public void clearJob(){jobId=null;state=BuilderState.IDLE;getNavigator().clearPathEntity();}
	@Override public boolean interact(EntityPlayer player){if(worldObj.isRemote)return true;TileEntityMachineBuilder d=depot();if(d==null||!getUniqueID().equals(d.getAssignedBuilderId())){player.addChatMessage(new ChatComponentTranslation("builder.depot.missing"));return true;}FMLNetworkHandler.openGui(player,MainRegistry.instance,ModBlocks.guiID_builder,worldObj,depotX,depotY,depotZ);return true;}
	public void detachFromDepot(){BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob job=data==null?null:data.get(jobId);if(job!=null)pause(job,BuilderState.PAUSED);jobId=null;state=BuilderState.PAUSED;}
	@Override public void onLivingUpdate(){super.onLivingUpdate();if(!worldObj.isRemote&&XFConfig.enableFactionBuilders&&ticksExisted%XFConfig.builderWorkIntervalTicks==0)work();}
	private void work(){
		BuilderJobData data=BuilderJobData.get(worldObj); BuilderJob job=data==null?null:data.get(jobId);
		if(job==null){state=BuilderState.IDLE;return;} if(job.dimension!=worldObj.provider.dimensionId){pause(job,BuilderState.PAUSED);return;}
		job.builderId=getUniqueID(); Schematic s=find(job.schematicId); if(s==null){pause(job,BuilderState.PAUSED);return;}
		int total=s.width*s.height*s.length, scans=0;
		while(job.blockIndex<total&&scans++<XFConfig.builderBlockScanBudget){
			int i=job.blockIndex,z=i%s.length,y=(i/s.length)%s.height,x=i/s.length/s.height;
			int[] transformed=com.hfr.schematic.SchematicTransform.position(s,x,y,z,job.rotation,job.mirrored);
			int wx=job.originX+transformed[0],wy=job.originY+transformed[1],wz=job.originZ+transformed[2]; Block wanted=s.resolveBlock(x,y,z); int meta=com.hfr.schematic.SchematicTransform.metadata(wanted,s.getMetadata(x,y,z),job.rotation,job.mirrored);
			if(!worldObj.blockExists(wx,wy,wz)){pause(job,BuilderState.WAITING_FOR_CHUNK);return;}
			if(wanted==null){pause(job,BuilderState.PAUSED);return;}
			Block existing=worldObj.getBlock(wx,wy,wz); if(existing==wanted&&worldObj.getBlockMetadata(wx,wy,wz)==meta){job.blockIndex++;continue;}
			if(getDistanceSq(wx+.5,wy+.5,wz+.5)>16){state=BuilderState.MOVE_TO_SITE;if(getNavigator().noPath()&&!getNavigator().tryMoveToXYZ(wx+.5,wy,wz+.5,1)){if(++pathFailures>=5)pause(job,BuilderState.PATHFINDING_ERROR);}return;}
			pathFailures=0;
			if(existing!=Blocks.air){state=BuilderState.BREAKING_BLOCK;if(BuilderPlacement.protectedBlock(worldObj,wx,wy,wz)||!BuilderPlacement.breakBlock(worldObj,wx,wy,wz,factionId)){blocked(job,wx,wy,wz);return;}data.markDirty();return;}
			if(wanted==Blocks.air){job.blockIndex++;data.markDirty();continue;}
			ItemStack need=BuilderMaterialResolver.resolve(wanted,meta); if(need==null){pause(job,BuilderState.WAITING_FOR_MATERIALS);return;}
			int slot=findMaterial(materials,need); TileEntityMachineBuilder depot=depot();
			if(slot<0){if(getDistanceSq(depotX+.5,depotY+.5,depotZ+.5)>16){state=BuilderState.GETTING_MATERIALS;getNavigator().tryMoveToXYZ(depotX+.5,depotY,depotZ+.5,1);return;} if(depot!=null&&depot.takeMaterial(need)){add(need.copy());slot=findMaterial(materials,need);} }
			if(slot<0){pause(job,BuilderState.WAITING_FOR_MATERIALS);return;}
			state=BuilderState.PLACING_BLOCK;if(BuilderPlacement.place(worldObj,wx,wy,wz,wanted,meta,factionId)){if(--materials[slot].stackSize<=0)materials[slot]=null;job.blockIndex++;data.markDirty();return;}blocked(job,wx,wy,wz);return;
		}
		if(job.blockIndex>=total){state=BuilderState.COMPLETE;job.state=state;data.markDirty();TileEntityMachineBuilder d=depot();if(d!=null){d.setActiveJob(null);clearJob();BuilderDepotService.advance(d);}}else{state=BuilderState.FIND_NEXT_BLOCK;job.state=state;data.markDirty();}
	}
	private Schematic find(String id){SchematicStoreData store=SchematicStoreData.get(worldObj);Schematic stored=store==null?null:store.getSchematic(id);if(stored!=null)return stored;for(Schematic s:MainRegistry.schems)if(s!=null&&s.name.equals(id))return s;return null;}
	private TileEntityMachineBuilder depot(){return worldObj.provider.dimensionId==depotDimension&&worldObj.getTileEntity(depotX,depotY,depotZ) instanceof TileEntityMachineBuilder?(TileEntityMachineBuilder)worldObj.getTileEntity(depotX,depotY,depotZ):null;}
	private static int findMaterial(ItemStack[] a,ItemStack n){for(int i=0;i<a.length;i++)if(a[i]!=null&&a[i].isItemEqual(n))return i;return -1;}
	private void add(ItemStack n){for(int i=0;i<materials.length;i++)if(materials[i]==null){materials[i]=n;return;}}
	private void pause(BuilderJob j,BuilderState s){state=s;j.state=s;BuilderJobData.get(worldObj).markDirty();}
	private void syncState(){BuilderJobData d=BuilderJobData.get(worldObj);BuilderJob j=d==null?null:d.get(jobId);if(j!=null){j.state=state;d.markDirty();}}
	private void blocked(BuilderJob j,int x,int y,int z){j.blockedX=x;j.blockedY=y;j.blockedZ=z;pause(j,BuilderState.INVALID_TERRITORY);}
	@Override public void onDeath(DamageSource d){if(!worldObj.isRemote){BuilderJobData data=BuilderJobData.get(worldObj);BuilderJob j=data==null?null:data.get(jobId);if(j!=null){j.builderId=null;pause(j,BuilderState.PAUSED);}TileEntityMachineBuilder depot=depot();if(depot!=null)depot.clearBuilder(getUniqueID());}super.onDeath(d);}
	@Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);put(n,"FactionUUID",factionId);put(n,"JobUUID",jobId);n.setInteger("DepotX",depotX);n.setInteger("DepotY",depotY);n.setInteger("DepotZ",depotZ);n.setInteger("DepotDimension",depotDimension);n.setString("BuilderState",state.name());NBTTagList l=new NBTTagList();for(int i=0;i<materials.length;i++)if(materials[i]!=null){NBTTagCompound c=new NBTTagCompound();c.setByte("Slot",(byte)i);materials[i].writeToNBT(c);l.appendTag(c);}n.setTag("BuilderInventory",l);}
	@Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);factionId=id(n,"FactionUUID");jobId=id(n,"JobUUID");depotX=n.getInteger("DepotX");depotY=n.getInteger("DepotY");depotZ=n.getInteger("DepotZ");depotDimension=n.hasKey("DepotDimension")?n.getInteger("DepotDimension"):dimension;try{state=BuilderState.valueOf(n.getString("BuilderState"));}catch(Exception e){state=BuilderState.IDLE;}NBTTagList l=n.getTagList("BuilderInventory",10);for(int i=0;i<l.tagCount();i++){NBTTagCompound c=l.getCompoundTagAt(i);int k=c.getByte("Slot");if(k>=0&&k<materials.length)materials[k]=ItemStack.loadItemStackFromNBT(c);}}
	private static void put(NBTTagCompound n,String k,UUID v){if(v!=null)n.setString(k,v.toString());}private static UUID id(NBTTagCompound n,String k){try{return UUID.fromString(n.getString(k));}catch(Exception e){return null;}}
}
