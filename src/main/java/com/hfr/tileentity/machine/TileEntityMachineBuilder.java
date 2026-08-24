package com.hfr.tileentity.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hfr.items.ModItems;
import com.hfr.builder.BuilderJob;
import com.hfr.builder.BuilderJobData;
import com.hfr.builder.BuilderState;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.tile.AuxGaugePacket;
import com.hfr.packet.tile.BuilderPacket;
import com.hfr.schematic.Schematic;
import com.hfr.schematic.SchematicPronter;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.entity.Entity;

public class TileEntityMachineBuilder extends TileEntityMachineBase {
	
	public Schematic preview;
	public int ox = 1;
	public int oz = 1;
	public int lock = -1;
	public int previewRotation;
	public boolean previewMirrored;
	public static List<SchemOffer> offers = new ArrayList();
	private UUID factionId, assignedBuilderId, activeJobId;
	private String cityId = "";
	private final List<UUID> queuedJobs = new ArrayList<UUID>();

	public TileEntityMachineBuilder() {
		// Slot zero remains the legacy wrench slot; 1..27 are depot materials.
		super(28);
	}

	public boolean takeMaterial(ItemStack wanted) {
		for(int i=1;i<slots.length;i++) if(slots[i]!=null && slots[i].isItemEqual(wanted)) {
			if(--slots[i].stackSize<=0) slots[i]=null; markDirty(); return true;
		}
		return false;
	}
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot > 0 && slot < slots.length; }
	@Override public int[] getAccessibleSlotsFromSide(int side) { int[] result=new int[27]; for(int i=0;i<27;i++)result[i]=i+1; return result; }
	public void setFactionId(UUID id){factionId=id;markDirty();}
	public void assignBuilder(UUID id){assignedBuilderId=id;markDirty();}
	public void assign(UUID faction, UUID builder, String city){factionId=faction;assignedBuilderId=builder;cityId=city==null?"":city;markDirty();}
	public UUID getFactionId(){return factionId;}
	public UUID getAssignedBuilderId(){return assignedBuilderId;}
	public UUID getActiveJobId(){return activeJobId;}
	public boolean hasAssignedBuilder(){return assignedBuilderId!=null;}
	public EntityFactionBuilder getLoadedBuilder(){
		if(worldObj==null||assignedBuilderId==null)return null;
		for(Object object:worldObj.loadedEntityList)if(object instanceof EntityFactionBuilder&&assignedBuilderId.equals(((Entity)object).getUniqueID()))return (EntityFactionBuilder)object;
		return null;
	}
	/** Missing is stale only when death/depot invalidation or the persistent job confirms it. */
	public boolean clearAssignmentIfConfirmedStale(){
		EntityFactionBuilder loaded=getLoadedBuilder(); if(loaded!=null&&!loaded.isDead)return false;
		BuilderJobData data=worldObj==null?null:BuilderJobData.get(worldObj); BuilderJob job=data==null?null:data.get(activeJobId);
		if(loaded==null&&(activeJobId==null||job==null||job.builderId!=null))return false;
		assignedBuilderId=null;markDirty();return true;
	}
	public void clearBuilder(UUID expected){if(expected==null||expected.equals(assignedBuilderId)){assignedBuilderId=null;markDirty();}}
	public void onDepotRemoved(){
		EntityFactionBuilder builder=getLoadedBuilder(); if(builder!=null)builder.detachFromDepot();
		BuilderJobData data=worldObj==null?null:BuilderJobData.get(worldObj); BuilderJob job=data==null?null:data.get(activeJobId);
		if(job!=null){job.state=BuilderState.PAUSED;job.builderId=null;data.markDirty();}
		assignedBuilderId=null;markDirty();
	}
	public void setActiveJob(UUID id){activeJobId=id;markDirty();}
	public void queueJob(UUID id){if(id!=null&&!queuedJobs.contains(id)){queuedJobs.add(id);markDirty();}}
	@Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);previewRotation=n.getByte("PreviewRotation")&3;previewMirrored=n.getBoolean("PreviewMirrored");factionId=uuid(n,"FactionUUID");assignedBuilderId=uuid(n,"AssignedBuilderUUID");activeJobId=uuid(n,"ActiveJobUUID");cityId=n.getString("BuilderCityId");queuedJobs.clear();NBTTagList q=n.getTagList("QueuedBuilderJobs",8);for(int i=0;i<q.tagCount();i++)try{queuedJobs.add(UUID.fromString(q.getStringTagAt(i)));}catch(Exception ignored){}}
	@Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setByte("PreviewRotation",(byte)(previewRotation&3));n.setBoolean("PreviewMirrored",previewMirrored);put(n,"FactionUUID",factionId);put(n,"AssignedBuilderUUID",assignedBuilderId);put(n,"ActiveJobUUID",activeJobId);n.setString("BuilderCityId",cityId);NBTTagList q=new NBTTagList();for(UUID id:queuedJobs)q.appendTag(new net.minecraft.nbt.NBTTagString(id.toString()));n.setTag("QueuedBuilderJobs",q);}
	private static void put(NBTTagCompound n,String k,UUID id){if(id!=null)n.setString(k,id.toString());}
	private static UUID uuid(NBTTagCompound n,String k){try{return UUID.fromString(n.getString(k));}catch(Exception e){return null;}}

	@Override
	public String getName() {
		return "container.builder";
	}

	@Override
	public void updateEntity() {
		
		if(!worldObj.isRemote) {
			/*worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			
			if(!MainRegistry.schems.isEmpty())
				SchematicPronter.pront(worldObj, xCoord, yCoord, zCoord, MainRegistry.schems.get(worldObj.rand.nextInt(MainRegistry.schems.size())));*/
			
			if(preview != null) {
				if(ox > 1)
					ox = 1;
				if(oz > 1)
					oz = 1;
				if(ox < -preview.width)
					ox = -preview.width;
				if(oz < -preview.length)
					oz = -preview.length;
			} else {
				ox = 1;
				oz = 1;
			}
			
			PacketDispatcher.wrapper.sendToAllAround(new BuilderPacket(xCoord, yCoord, zCoord, ox, oz), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 150));
		}
	}
	
	public boolean doesIntersect() {
		
		if(ox <= 0 && oz <= 0 &&
				ox + preview.width > 0 && oz + preview.length > 0)
			return true;
		
		return false;
	}
	
	public void construct(Schematic schem) {
		
		int i = schem.value;
		//todo should require actual building materials, and list them in the GUI.
		// infact I don't even think this shit even works, I couldn't get it to work.
		
		if(slots[0] != null && slots[0].getItem() == ModItems.wrench && slots[0].stackSize >= i) {

			this.decrStackSize(0, i);
			worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hfr:block.buttonYes", 1.0F, 1.0F);
			SchematicPronter.pront(worldObj, xCoord + ox, yCoord, zCoord + oz, schem);
			
			preview = null;
			PacketDispatcher.wrapper.sendToAllAround(new AuxGaugePacket(xCoord, yCoord, zCoord, 0, 0), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 150));
		
		} else {

			worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hfr:block.buttonNo", 1.0F, 1.0F);
		}
	}
	
	public void deconstruct(Schematic schem) {
		
		SchematicPronter.delet(worldObj, xCoord + ox, yCoord, zCoord + oz, schem);
		preview = null;
		PacketDispatcher.wrapper.sendToAllAround(new AuxGaugePacket(xCoord, yCoord, zCoord, 0, 0), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 150));
	}
	
	public static class SchemOffer {
		public String name;
		public int value;
		
		public SchemOffer(String name, int value) {
			this.name = name;
			this.value = value;
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
