package com.hfr.tileentity;

import java.util.List;

import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderFlag;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.data.ClowderData;
import com.hfr.items.ModItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
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
	public float speed = 0.005F * 0.5F;
	public int mode = 0;
	
	private int timer = 0;
	
	@SideOnly(Side.CLIENT)
	public ClowderFlag flag;
	@SideOnly(Side.CLIENT)
	public int color;
	@SideOnly(Side.CLIENT)
	public float prestige;
	@SideOnly(Side.CLIENT)
	public float prestigeReq;

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
			if(!Clowder.clowders.contains(owner))
				owner = null;
			
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
				mode = 0;
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
							mode = 0;
							timer = 0;
						}
						
						this.markDirty();
					}
				}
			}
			
			if(prev == 1F && height != 1F) {
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hfr:block.flagCapture", 100.0F, 1.0F);
				
				if(owner != null)
					owner.notifyCapture(worldObj, xCoord, zCoord, "flags");
			}
			
			if(prevC != owner)
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hfr:block.flagChange", 3.0F, 1.0F);
			
			if(prev != 1F && height == 1F) {
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hfr:block.flagHoist", 3.0F, 1.0F);
				generateClaim();
			}
			
			/// CAPTURE END ///
			
			if(!canSeeSky()) {
				isClaimed = false;
				setOwner(null);
				
				if(height >= speed * 2)
					height -= speed * 2;
			} else if(owner != null) {
				//generateClaim();
			}
			
			if(owner != null) {
				this.updateGauge(owner.flag.ordinal(), 0, 100);
				this.updateGauge(owner.color, 1, 100);
				this.updateGauge(Float.floatToIntBits(owner.getPrestige()), 4, 20);
				this.updateGauge(Float.floatToIntBits(owner.getPrestigeReq()), 5, 20);
			} else {
				this.updateGauge(ClowderFlag.NONE.ordinal(), 0, 100);
				this.updateGauge(0xFFFFFF, 1, 100);
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
		}
	}
	
	public float getGenRate() {
		
		return getGenRateFromMode(mode);
	}
	
	public static float getGenRateFromMode(int mode) {

		/*if(mode == 1)
			return Clowder.flagRate * 3;
		if(mode == 2)
			return Clowder.flagRate * 2;
		if(mode == 3)
			return Clowder.flagRate * 1;*/
		
		if(mode != 0)
			return Clowder.flagRate;
		
		return 0;
	}
	
	public float getCost() {
		
		return getCostFromMode(mode);
	}
	
	public static float getCostFromMode(int mode) {

		if(mode == 1)
			return Clowder.flagReq * 3;
		if(mode == 2)
			return Clowder.flagReq * 2;
		if(mode == 3)
			return Clowder.flagReq * 1;
		
		return 0;
	}
	
	public void setOwner(Clowder c) {
		
		if(owner != null) {
			owner.addPrestigeGen(-getGenRate(), worldObj);
			owner.addPrestigeReq(-getCost(), worldObj);

			owner.multPrestige(0.95F, worldObj);
		}
		
		owner = c;

		if(owner != null) {
			owner.addPrestigeGen(getGenRate(), worldObj);
			owner.addPrestigeReq(getCost(), worldObj);
		}
	}
	
	public void setMode(int mode) {
		
		//if there's no owner, the mode cannot be set
		if(owner == null) {
			this.mode = 0;
			return;
		}

		float beforeGen = getGenRateFromMode(this.mode);
		float afterGen = getGenRateFromMode(mode);

		float beforeCost = getCostFromMode(this.mode);
		float afterCost = getCostFromMode(mode);
		
		//if the new requirement is above the prestige level, the mode will not change
		if(owner.getPrestigeReq() - beforeCost + afterCost > owner.getPrestige())
			return;

		//subtract old amounts
		owner.addPrestigeGen(-beforeGen, worldObj);
		owner.addPrestigeReq(-beforeCost, worldObj);
		
		this.mode = mode;
		
		//...and add the new ones
		owner.addPrestigeGen(afterGen, worldObj);
		owner.addPrestigeReq(afterCost, worldObj);
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
		
		switch(mode) {
		case 1: return 10;
		case 2: return 5;
		case 3: return 2;
		default: return 0;
		}
	}

	@Override
	public Clowder getOwner() {
		return owner;
	}
	
	public void generateClaim() {
		
		TerritoryMeta center = ClowderTerritory.getMetaFromCoords(ClowderTerritory.getCoordPair(xCoord, zCoord));
		boolean explode = false;
		
		if(center != null && center.owner.zone != Zone.FACTION)
			explode = true;
		if(center != null && center.owner.zone == Zone.FACTION && center.owner.owner != this.owner)
			explode = true;
		
		if(explode) {
			worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
			worldObj.newExplosion(null, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 5.0F, true, true);
			return;
		}
		
		int rad = getRadius();
		
		for(int x = -rad; x <= rad; x++) {
			for(int z = -rad; z <= rad; z++) {

				int posX = xCoord + x * 16;
				int posZ = zCoord + z * 16;
				CoordPair loc = ClowderTerritory.getCoordPair(posX, posZ);
				
				TerritoryMeta meta = ClowderTerritory.getMetaFromCoords(loc);
				
				if(meta == null || !meta.checkPersistence(worldObj, loc))
					if(Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)) < rad)
						ClowderTerritory.setOwnerForCoord(worldObj, loc, owner, xCoord, yCoord, zCoord);
			}
		}
	}
	
	public boolean canSeeSky() {

		for(int i = -2; i <= 2; i++)
			for(int j = -2; j <= 2; j++)
				if(worldObj.getBlock(xCoord + i, yCoord + 1, zCoord + j).getMaterial() != Material.air && !(i == 0 && j == 0) ||
					!worldObj.canBlockSeeTheSky(xCoord + i, yCoord + 1, zCoord + j))
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

		this.owner = Clowder.getClowderFromName(nbt.getString("owner"));
		this.isClaimed = nbt.getBoolean("isClaimed");
		this.height = nbt.getFloat("height");
		this.mode = nbt.getInteger("mode");
		this.timer = nbt.getInteger("timer");
		
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
		
		if(owner != null)
			generateClaim();
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if(owner != null)
			nbt.setString("owner", owner.name);
		
		nbt.setBoolean("isClaimed", isClaimed);
		nbt.setFloat("height", height);
		nbt.setInteger("mode", mode);
		nbt.setInteger("timer", timer);
		
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

}
