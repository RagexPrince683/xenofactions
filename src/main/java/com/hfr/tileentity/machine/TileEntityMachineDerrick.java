package com.hfr.tileentity.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.hfr.blocks.ModBlocks;
import com.hfr.items.ModItems;
import com.hfr.main.MainRegistry;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.tile.AuxElectricityPacket;
import com.hfr.packet.tile.AuxGaugePacket;
import com.hfr.util.PipeUtil;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityMachineDerrick extends TileEntity implements ISidedInventory, IEnergyHandler {

	private ItemStack slots[];

	public EnergyStorage storage = new EnergyStorage(MainRegistry.derrickBuffer, MainRegistry.derrickBuffer / 100, MainRegistry.derrickBuffer / 100);

	public int oil;
	public static final int maxOil = 32000;
	public int gas;
	public static final int maxGas = 32000;
	
	public int warning;
	public int warning2;
	public int age = 0;
	
	//drill token that allows to drill one (1) empty oil deposit
	//is granted every time there is no more oil to be found
	public boolean token = false;
	
	private static final int[] slots_top = new int[] {1};
	private static final int[] slots_bottom = new int[] {2, 0};
	private static final int[] slots_side = new int[] {0};
	Random rand = new Random();
	
	private String customName;
	
	public TileEntityMachineDerrick() {
		slots = new ItemStack[5];
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return slots[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(slots[i] != null)
		{
			ItemStack itemStack = slots[i];
			slots[i] = null;
			return itemStack;
		} else {
		return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		slots[i] = itemStack;
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit())
		{
			itemStack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.oilWell";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <=128;
		}
	}
	
	//You scrubs aren't needed for anything (right now)
	@Override
	public void openInventory() {}
	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return false;
	}
	
	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null)
		{
			if(slots[i].stackSize <= j)
			{
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].stackSize == 0)
			{
				slots[i] = null;
			}
			
			return itemStack1;
		} else {
			return null;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);
		
		this.storage.readFromNBT(nbt);
		this.age = nbt.getInteger("age");
		this.oil = nbt.getInteger("oil");
		this.gas = nbt.getInteger("gas");
		
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
		
		this.storage.writeToNBT(nbt);
		nbt.setInteger("age", age);
		nbt.setInteger("oil", oil);
		nbt.setInteger("gas", gas);
		
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
	public int[] getAccessibleSlotsFromSide(int p_94128_1_)
    {
        return p_94128_1_ == 0 ? slots_bottom : (p_94128_1_ == 1 ? slots_top : slots_side);
    }

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return false;
	}
	
	public long getPowerScaled(long i) {
		return (storage.getEnergyStored() * i) / storage.getMaxEnergyStored();
	}
	
	public long getOilScaled(int i) {
		return (oil * i) / maxOil;
	}
	
	public long getGasScaled(int i) {
		return (gas * i) / maxGas;
	}
	
	@Override
	public void updateEntity() {
		
		int timer = MainRegistry.derrickTimer;
		
		age++;
		if(age >= timer)
			age = 0;
		

		
	}
	

	
	public boolean isOre(Block b, int meta) {
		
		int[] ids = OreDictionary.getOreIDs(new ItemStack(b, 1, meta));
		
		for(int i = 0; i < ids.length; i++) {
			
			String s = OreDictionary.getOreName(ids[i]);
			
			if(s.length() > 3 && s.substring(0, 3).equals("ore"))
				return true;
		}
		
		return false;
	}

	int steps = 0;
	public boolean succ(int x, int y, int z) {
		
		list.clear();

		steps = 0;
		succ1(x, y, z);
		steps = 0;
		succ2(x, y, z);
		
		if(!list.isEmpty()) {
			
			int i = rand.nextInt(list.size());
			int a = list.get(i)[0];
			int b = list.get(i)[1];
			int c = list.get(i)[2];
			

		}
		
		return false;
	}
	
	public void succInit1(int x, int y, int z) {

		List<Integer> indices = new ArrayList() {{ add(0); add(1); add(2); add(3); add(4); add(5); }};
		Collections.shuffle(indices);
		
		for(Integer i : indices) {
			switch(i) {
			case 0: succ1(x + 1, y, z); break;
			case 1: succ1(x - 1, y, z); break;
			case 2: succ1(x, y + 1, z); break;
			case 3: succ1(x, y - 1, z); break;
			case 4: succ1(x, y, z + 1); break;
			case 5: succ1(x, y, z - 1); break;
			}
		}
	}
	
	public void succInit2(int x, int y, int z) {

		List<Integer> indices = new ArrayList() {{ add(0); add(1); add(2); add(3); add(4); add(5); }};
		Collections.shuffle(indices);
		
		for(Integer i : indices) {
			switch(i) {
			case 0: succ2(x + 1, y, z); break;
			case 1: succ2(x - 1, y, z); break;
			case 2: succ2(x, y + 1, z); break;
			case 3: succ2(x, y - 1, z); break;
			case 4: succ2(x, y, z + 1); break;
			case 5: succ2(x, y, z - 1); break;
			}
		}
	}
	
	List<int[]> list = new ArrayList<int[]>();
	
	public void succ1(int x, int y, int z) {
		
		steps++;
		if(steps > MainRegistry.derrickLimiter)
			return;
		

	}
	
	public void succ2(int x, int y, int z) {
		
		steps++;
		if(steps > MainRegistry.derrickLimiter)
			return;
		

	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return from != ForgeDirection.UP && from != ForgeDirection.UNKNOWN;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		return storage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return storage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return storage.getMaxEnergyStored();
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {

		double toSend = Math.min(storage.getEnergyStored(), storage.getMaxExtract());

		if (!simulate) {
			storage.setEnergyStored(storage.getEnergyStored() - (int) Math.round(toSend));
		}

		return (int) Math.round(toSend);
	}
}
