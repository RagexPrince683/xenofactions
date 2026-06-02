package com.hfr.tileentity.clowder;

import static net.minecraftforge.common.util.ForgeDirection.UP;

import java.util.List;

import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderFlag;
import com.hfr.clowder.CityLevel;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.items.ModItems;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.tile.CityCenterPacket;
import com.hfr.main.MainRegistry;
import com.hfr.tileentity.machine.TileEntityMachineBase;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityFlag extends TileEntityMachineBase implements ITerritoryProvider {

	public Clowder owner;
	public boolean isClaimed = true;
	public float height = 1.0F;
	public float speed = 1F / (20F * 30F);
	public int mode = 0;
	public String name = "";
	public String ownerName = "";
	public CityLevel cityLevel = CityLevel.SETTLEMENT;
	
	private int timer = 0;
	
	@SideOnly(Side.CLIENT)
	public ClowderFlag flag;
	@SideOnly(Side.CLIENT)
	public int color;
	@SideOnly(Side.CLIENT)
	public float prestige;
	@SideOnly(Side.CLIENT)
	public float prestigeReq;
	@SideOnly(Side.CLIENT)
	public String customFlagHash = "";

	public TileEntityFlag() {
		super(0);
	}

	@Override
	public String getName() {
		return "container.flagPole";
	}

	@Override
	public void updateEntity() {


		
		if(!worldObj.isRemote) {
			
			//remove disbanded clowders
			if(!Clowder.clowders.contains(owner) && owner != null) {
				MainRegistry.logger.info("Deleting clowder from flag " + xCoord + " " + yCoord + " " + zCoord + " due to clowder not being in the clowder list! (disband?)");
				owner = null;
			}
			
			/*if(Clowder.clowders.size() == 0) {
				ClowderData.getData(worldObj);
				return;
			}*/
			
			/// CAPTURE START ///

			float prev = height;
			Clowder prevC = owner;
			
			if(!isClaimed && canSeeSky()) {
				List<EntityPlayer> entities = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord - 4, yCoord - 1, zCoord - 4, xCoord + 5, yCoord + 2, zCoord + 5));
				
				Clowder capturer = null;
				for(EntityPlayer player : entities) {
					
					Clowder clow = Clowder.getClowderFromPlayer(player);
					
					if(clow != null) {
						capturer = clow;
						break;
					}
				}
				
				if(capturer != null) {
					
					//he who owns the flag now can raise it.
					//if the flag reaches the end of the pole, the ownership will be locked
					if(capturer == owner) {
						height += speed;
						
						if(height >= 1) {
							MainRegistry.logger.info("Locking flag " + xCoord + " " + yCoord + " " + zCoord + " for being hoisted! (captured and raised!)");
							isClaimed = true;
							height = 1;
							this.markDirty();
						}
						
					//he who does not own the flag can lower it
					//once it reaches the bottom, it will be his
					} else {
						
						height -= speed;
						
						if(height <= 0) {
							
							setOwner(capturer);
							height = 0;
							this.markDirty();
						}
					}
					
				//if there is nobody capturing the flag, it will simply descend
				} else {
					
					height -= speed;
					
					if(height <= 0) {
						height = 0;
					}
				}
			}
			
			if(!isClaimed || owner == null) {
				this.setMode(0);
				timer = 0;
				this.markDirty();
			} else {
				
				if(timer > 0)
					timer--;
				
				if(mode > 0) {
					
					if(timer <= 0) {
						
						if(consumeToken()) {
							timer = getTime();
						} else {
							this.setMode(0);
							timer = 0;
						}
						
						this.markDirty();
					}
				}
			}
			
			if(prev == 1F && height != 1F) {
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hfr:block.flagCapture", 100.0F, 1.0F);

				MainRegistry.logger.info("Unlocking flag " + xCoord + " " + yCoord + " " + zCoord + " for being unhoisted! (captured!)");
				
				if(owner != null)
					owner.notifyCapture(worldObj, xCoord, zCoord, "City Center " + name);
			}
			
			if(prevC != owner)
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hfr:block.flagChange", 3.0F, 1.0F);
			
			if(prev != 1F && height == 1F) {
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hfr:block.flagHoist", 3.0F, 1.0F);
				MainRegistry.logger.info("Hoisintg flag " + xCoord + " " + yCoord + " " + zCoord + "!");
				generateClaim();
			}
			
			/// CAPTURE END ///
			
			if(!canSeeSky()) {
				
				if(isClaimed) {
					MainRegistry.logger.info("Unlocking flag " + xCoord + " " + yCoord + " " + zCoord + " for being obstructed!");
				}
				
				isClaimed = false;
				setOwner(null);
				
				if(height >= speed * 2)
					height -= speed * 2;
			} else if(owner != null) {
				//generateClaim();
			}
			
			ownerName = owner == null ? "" : owner.name;
			if(worldObj.getTotalWorldTime() % 20 == 0)
				{
					CityCenterPacket packet = new CityCenterPacket(xCoord, yCoord, zCoord, name, ownerName);
					packet.flagHash = owner == null ? "" : owner.customFlagHash;
					PacketDispatcher.wrapper.sendToAllAround(packet, new TargetPoint(this.worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 250));
				}

			if(owner != null) {
				this.updateGauge(owner.flag.ordinal(), 0, 250);
				this.updateGauge(owner.color, 1, 250);
				this.updateGauge(Float.floatToIntBits(owner.getPrestige()), 4, 20);
				this.updateGauge(Float.floatToIntBits(owner.getPrestigeReq()), 5, 20);
				this.updateGauge(cityLevel.ordinal(), 6, 20);
			} else {
				this.updateGauge(ClowderFlag.NONE.ordinal(), 0, 250);
				this.updateGauge(0xFFFFFF, 1, 250);
				this.updateGauge(0, 4, 20);
				this.updateGauge(0, 5, 20);
			}
			this.updateGauge(mode, 2, 25);
			this.updateGauge((int) (height * 100F), 3, 100);
			
		} else {

			if(mode > 0) {
				double x = xCoord + 0.5 + worldObj.rand.nextGaussian() * 0.25D;
				double y = yCoord + 0.125 + worldObj.rand.nextDouble() * 0.5D;
				double z = zCoord + 0.5 + worldObj.rand.nextGaussian() * 0.25D;

			    float r = Math.max(((color & 0xFF0000) >> 16) / 256F, 0.01F);
			    float g = Math.max(((color & 0xFF00) >> 8) / 256F, 0.01F);
			    float b = Math.max((color & 0xFF) / 256F, 0.01F);
				
				worldObj.spawnParticle("reddust", x, y, z, r, g, b);
			}
		}
	}
	
	public void processGauge(int val, int id) {
		
		switch(id) {
		case 0: flag = ClowderFlag.values()[val]; break;
		case 1: color = val; break;
		case 2: mode = val; break;
		case 3: height = val * 0.01F; break;
		case 4: prestige = Float.intBitsToFloat(val); break;
		case 5: prestigeReq = Float.intBitsToFloat(val); break;
		case 6: cityLevel = CityLevel.byOrdinal(val); break;
		}
	}
	
	public float getGenRate() {
		return owner == null ? 0 : Clowder.flagRate;
	}
	
	public static float getGenRateFromMode(int mode) {
		
		if(mode != 0)
			return Clowder.flagRate;
		
		return 0;
	}
	
	public float getCost() {
		return cityLevel.upkeep;
	}
	
	public static float getCostFromMode(int mode) {
		return CityLevel.SETTLEMENT.upkeep;
	}
	
	public void setOwner(Clowder c) {
		
		if(owner != c)
			MainRegistry.logger.info("Changing owner for flag " + xCoord + " " + yCoord + " " + zCoord + " from " + (owner != null ? owner.name : "null") + " to " + (c != null ? c.name : "null"));
		
		if(owner != null) {
			
			//more of a failsafe since in all natural cases, the mode is 0 during this happening
			owner.addPrestigeGen(-getGenRate(), worldObj);
			owner.addPrestigeReq(-getCost(), worldObj);
			owner.flags--;

			owner.multPrestige(0.95F, worldObj);
		}
		
		owner = c;

		if(owner != null) {
			owner.addPrestigeGen(getGenRate(), worldObj);
			owner.addPrestigeReq(getCost(), worldObj);
			owner.flags++;
		}
		
		this.markDirty();
	}
	
	public void setMode(int mode) {
		// City Centers always use the current city level for radius/upkeep.
		this.mode = owner == null ? 0 : 1;
		this.markDirty();
	}

	public boolean upgradeCity() {
		if(owner == null)
			return false;
		CityLevel next = cityLevel.next();
		if(next == null)
			return false;
		float beforeCost = getCost();
		if(owner.getPrestige() < next.upgradeCost || owner.getPrestigeReq() - beforeCost + next.upkeep > owner.getPrestige() - next.upgradeCost)
			return false;
		owner.addPrestige(-next.upgradeCost, worldObj);
		owner.addPrestigeReq(-beforeCost, worldObj);
		cityLevel = next;
		owner.addPrestigeReq(getCost(), worldObj);
		generateClaim();
		markDirty();
		return true;
	}
	
	private boolean consumeToken() {
		
		for(int i = 0; i < slots.length; i++) {
			
			if(slots[i] != null && slots[i].getItem() == ModItems.province_point) {
				this.decrStackSize(i, 1);
				return true;
			}
		}
		
		return true;
	}
	
	private int getTime() {
		
		switch(mode) {
		case 1: return 200;
		case 2: return 500;
		case 3: return 1000;
		default: return 0;
		}
	}
	
	@Override
	public int getRadius() {
		
		return owner == null ? 0 : cityLevel.radius;
	}

	@Override
	public Clowder getOwner() {
		return owner;
	}
	
	public void generateClaim() {
		
		int rad = Math.min(getRadius(), CityLevel.maxRadius());
		String placementError = ClowderTerritory.getCityPlacementError(xCoord >> 4, zCoord >> 4);
		if(placementError != null && ClowderTerritory.getMetaFromIntCoords(xCoord, zCoord) == null)
			return;
		
		for(int x = -CityLevel.maxRadius(); x <= CityLevel.maxRadius(); x++) {
			for(int z = -CityLevel.maxRadius(); z <= CityLevel.maxRadius(); z++) {

				int posX = xCoord + x * 16;
				int posZ = zCoord + z * 16;
				CoordPair loc = ClowderTerritory.getCoordPair(posX, posZ);
				
				TerritoryMeta meta = ClowderTerritory.getMetaFromCoords(loc);
				
				double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
				if(dist < rad) {
					if(meta == null || !meta.checkPersistence(worldObj, loc) || (meta.flagX == xCoord && meta.flagY == yCoord && meta.flagZ == zCoord)) {
						ClowderTerritory.setOwnerForCoord(worldObj, loc, owner, xCoord, yCoord, zCoord, name);
						TerritoryMeta newMeta = ClowderTerritory.getMetaFromCoords(loc);
						if(newMeta != null) {
							newMeta.cityLevel = cityLevel.ordinal();
							newMeta.cityName = name;
						}
					}
				} else if(meta != null && meta.flagX == xCoord && meta.flagY == yCoord && meta.flagZ == zCoord) {
					ClowderTerritory.removeZoneForCoord(worldObj, loc);
				}
			}
		}
	}
	
	public boolean bordersWilderness() {
		
		//no longer needed
		return true;

		/*int rad = getRadius();
		
		for(int x = -rad; x <= rad; x++) {
			for(int z = -CityLevel.maxRadius(); z <= CityLevel.maxRadius(); z++) {
				
				double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
				
				if(dist < rad && dist > rad - 1) {
					
					if(checkBorder(xCoord + x * 16, zCoord + z * 16))
						return true;
				}
			}
		}
		
		return false;*/
	}
	
	public boolean checkBorder(int x, int z) {

		CoordPair loc = ClowderTerritory.getCoordPair(x, z);
		Ownership owner = ClowderTerritory.getOwnerFromCoords(loc);
		if(owner.zone != Zone.FACTION || owner.owner != this.owner)
			return false;
		
		CoordPair loc1 = ClowderTerritory.getCoordPair(x + 16, z);
		Ownership owner1 = ClowderTerritory.getOwnerFromCoords(loc1);
		if(owner1.zone == Zone.WILDERNESS || owner1.owner != this.owner)
			return true;
		
		CoordPair loc2 = ClowderTerritory.getCoordPair(x - 16, z);
		Ownership owner2 = ClowderTerritory.getOwnerFromCoords(loc2);
		if(owner2.zone == Zone.WILDERNESS || owner2.owner != this.owner)
			return true;
		
		CoordPair loc3 = ClowderTerritory.getCoordPair(x, z + 16);
		Ownership owner3 = ClowderTerritory.getOwnerFromCoords(loc3);
		if(owner3.zone == Zone.WILDERNESS || owner3.owner != this.owner)
			return true;
		
		CoordPair loc4 = ClowderTerritory.getCoordPair(x, z - 16);
		Ownership owner4 = ClowderTerritory.getOwnerFromCoords(loc4);
		if(owner4.zone == Zone.WILDERNESS || owner4.owner != this.owner)
			return true;
		
		return false;
	}
	
	public boolean canSeeSky() {

		for(int i = -2; i <= 2; i++)
			for(int j = -2; j <= 2; j++)
				
				if(!worldObj.canBlockSeeTheSky(xCoord + i, yCoord + 1, zCoord + j) ||
					!worldObj.getBlock(xCoord + i, yCoord - 1, zCoord + j).isSideSolid(worldObj, xCoord + i, yCoord - 1, zCoord + j, UP))
					return false;
		
		if(yCoord < 45)
			return false;
		if(yCoord > 200) //upped to 200, 100 was retarded
			return false;
		
		return true;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		String own = nbt.getString("owner");
		this.owner = Clowder.getClowderFromName(own);
		boolean isNull = nbt.getBoolean("isNull");
		
		if(owner == null) {

			String id = nbt.getString("clow_uuid");
			this.owner = Clowder.getClowderFromUUID(id);
			
			if(!isNull) {
				
				if(owner == null) {
					MainRegistry.logger.info("Owner (" + id + ") of flag " + xCoord + " " + yCoord + " " + zCoord + " was saved NN but finalized as null!");
				}
			}
			
			if(owner == null && !id.isEmpty())
				MainRegistry.logger.info("Owner (" + id + ") of flag " + xCoord + " " + yCoord + " " + zCoord + " was set in NBT but not found in te clowder list!");
		}
		
		this.isClaimed = nbt.getBoolean("isClaimed");
		this.height = nbt.getFloat("height");
		this.mode = nbt.getInteger("mode");
		this.timer = nbt.getInteger("timer");
		this.name = nbt.getString("name");
		this.ownerName = owner == null ? "" : owner.name;
		this.cityLevel = CityLevel.byOrdinal(nbt.getInteger("cityLevel"));
		
		slots = new ItemStack[getSizeInventory()];
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if(owner != null) {
			nbt.setBoolean("isNull", false);
			nbt.setString("clow_uuid", owner.uuid);
		} else {
			nbt.setBoolean("isNull", true);
		}
		
		nbt.setBoolean("isClaimed", isClaimed);
		nbt.setFloat("height", height);
		nbt.setInteger("mode", mode);
		nbt.setInteger("timer", timer);
		nbt.setString("name", name);
		nbt.setInteger("cityLevel", cityLevel.ordinal());
		
		NBTTagList list = new NBTTagList();
		
		for(int i = 0; i < slots.length; i++)
		{
			if(slots[i] != null)
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte)i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
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

	@Override
	public String getClaimName() {
		return name;
	}

	@Override
	public void setClaimName(String name) {
		this.name = name;
	}
}
