package com.hfr.tileentity.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.HashBiMap;
import com.hfr.items.ModItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

//registers all foundry recipes here

public class TileEntityFoundry extends TileEntityMachineBase {

	@SideOnly(Side.CLIENT)
	public float tilt;
	@SideOnly(Side.CLIENT)
	public float lastTilt;
	
	public float steel;
	public static final float maxSteel = 32;
	public int heat;
	public static final int maxHeat = 4;
	public int smeltTimer;
	public int progress;
	public static final int castTime = 100;
	public int index = 0;


	//public TileEntity createNewTileEntity(World world, int meta) {
	//	// TEMPORARY: always create the foundry TE for testing
	//	//this only makes hoppers work with the 1 single block that's technically the placement block. Not all the foundry (multiblocks)
	//	//so this works with output, but not input. I wonder how the fuck we can get this shit to work with input?
	//	//we can't do an override here, not that that does anything, I already tried.
	//	return new TileEntityFoundry();
	//}


	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		// side: 0 = down, 1 = up, 2-5 = sides
		if (side == 0) { // bottom -> output
			return new int[] { 2 };
		} else if (side == 1) { // top -> steel input
			return new int[] { 0 };
		} else { // sides -> fuel
			return new int[] { 1 };
		}
	}

	//hopper compat:

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if (itemStack == null) return false;
		if (i == 0) { // steel input slot (top)
			return getSteel(itemStack) > 0.0F; // uses your existing getSteel(...) method
		} else if (i == 1) { // fuel slot (sides)
			return itemStack.getItem() == net.minecraft.init.Items.coal;
		} else { // output slot not insertable
			return false;
		}
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		// only allow insertion into slots that are exposed for that side AND valid for that slot
		int[] allowed = getAccessibleSlotsFromSide(side);
		for (int s : allowed) {
			if (s == slot) {
				return isItemValidForSlot(slot, stack);
			}
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		// allow extraction only from output slot (slot 2) from bottom side
		if (slot == 2 && side == 0) return true;
		return false;
	}

	//end

	//extra crap

	/**
	 * Attempt to push as many items as possible from slots[2] into the inventory directly below (y-1).
	 * Uses ISidedInventory if available (asks for side=1, i.e. top of below-inventory).
	 */
	private void tryPushOutputDown() {
		if (worldObj.isRemote) return;
		if (slots[2] == null) return;

		TileEntity teBelow = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
		if (teBelow == null) return;

		// copy of what's available to send
		ItemStack toSend = slots[2].copy();
		final int prevOutputCount = slots[2].stackSize;

		// helper inserter
		class Inserter {
			boolean insertIntoIInventory(net.minecraft.inventory.IInventory inv, int slotIndex, ItemStack stack, int sideFromBelow) {
				ItemStack dest = inv.getStackInSlot(slotIndex);

				// respect ISidedInventory rules
				if (inv instanceof ISidedInventory) {
					ISidedInventory sided = (ISidedInventory) inv;
					int[] accessible = sided.getAccessibleSlotsFromSide(sideFromBelow);
					boolean found = false;
					for (int s : accessible) if (s == slotIndex) { found = true; break; }
					if (!found) return false;
					if (!sided.canInsertItem(slotIndex, stack, sideFromBelow)) return false;
				} else {
					if (!inv.isItemValidForSlot(slotIndex, stack)) return false;
				}

				if (dest == null) {
					int move = Math.min(stack.stackSize, Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit()));
					ItemStack put = stack.copy();
					put.stackSize = move;
					inv.setInventorySlotContents(slotIndex, put);
					stack.stackSize -= move;
					inv.markDirty();
					return move > 0;
				} else if (dest.isItemEqual(stack) && dest.stackSize < Math.min(dest.getMaxStackSize(), inv.getInventoryStackLimit())) {
					int space = Math.min(dest.getMaxStackSize(), inv.getInventoryStackLimit()) - dest.stackSize;
					int move = Math.min(space, stack.stackSize);
					dest.stackSize += move;
					stack.stackSize -= move;
					inv.setInventorySlotContents(slotIndex, dest);
					inv.markDirty();
					return move > 0;
				}
				return false;
			}
		}

		Inserter ins = new Inserter();

		if (teBelow instanceof net.minecraft.inventory.IInventory) {
			net.minecraft.inventory.IInventory inv = (net.minecraft.inventory.IInventory) teBelow;
			if (inv instanceof ISidedInventory) {
				ISidedInventory sided = (ISidedInventory) inv;
				int[] slotsForTop = sided.getAccessibleSlotsFromSide(1); // 1 = UP for the below-inventory
				for (int slotIndex : slotsForTop) {
					if (toSend.stackSize <= 0) break;
					ins.insertIntoIInventory(inv, slotIndex, toSend, 1);
				}
			} else {
				for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); slotIndex++) {
					if (toSend.stackSize <= 0) break;
					ins.insertIntoIInventory(inv, slotIndex, toSend, 1);
				}
			}
		}

		// write remaining back to our output slot
		if (toSend.stackSize <= 0) {
			slots[2] = null;
		} else {
			slots[2].stackSize = toSend.stackSize;
		}

		// mark only if something changed
		int newOutputCount = (slots[2] == null) ? 0 : slots[2].stackSize;
		if (newOutputCount != prevOutputCount) {
			this.markDirty();
		}
	}

	//end
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.steel = nbt.getFloat("steel");
		this.index = nbt.getInteger("index");
		this.smeltTimer = nbt.getInteger("smeltTimer");
		this.progress = nbt.getInteger("progress");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setFloat("steel", steel);
		nbt.setInteger("index", index);
		nbt.setInteger("smeltTimer", smeltTimer);
		nbt.setInteger("progress", progress);
	}
	
	public static HashMap<String, Float> recipes = new HashMap();
	public static List<Item> options = new ArrayList();
	
	static {
		recipes.put(ModItems.ingot_steel.getUnlocalizedName(), 1F);
		recipes.put(ModItems.part_spring.getUnlocalizedName(), 0.5F);
		recipes.put(ModItems.part_rod.getUnlocalizedName(), 0.5F);
		recipes.put(ModItems.part_rifled_barrel.getUnlocalizedName(), 2F);
		recipes.put(ModItems.part_smoothbore_barrel.getUnlocalizedName(), 2F);
		recipes.put(ModItems.part_gear.getUnlocalizedName(), 2.5F);
		recipes.put(ModItems.part_plate.getUnlocalizedName(), 3F);
		recipes.put(ModItems.part_frame.getUnlocalizedName(), 5F);
		recipes.put(ModItems.part_grate.getUnlocalizedName(), 3F);
		recipes.put(ModItems.part_suspension.getUnlocalizedName(), 3F);
		recipes.put(ModItems.part_plating_1.getUnlocalizedName(), 5F);
		recipes.put(ModItems.part_hull_1.getUnlocalizedName(), 15F);
		recipes.put(ModItems.part_mechanism_1.getUnlocalizedName(), 4F);
		recipes.put(ModItems.part_steel_wheel.getUnlocalizedName(), 3F);
		recipes.put(ModItems.part_sawblade.getUnlocalizedName(), 2.5F);
		recipes.put(ModItems.part_track.getUnlocalizedName(), 2F);

		options.add(ModItems.ingot_steel);
		options.add(ModItems.part_spring);
		options.add(ModItems.part_rod);
		options.add(ModItems.part_rifled_barrel);
		options.add(ModItems.part_smoothbore_barrel);
		options.add(ModItems.part_gear);
		options.add(ModItems.part_plate);
		options.add(ModItems.part_frame);
		options.add(ModItems.part_grate);
		options.add(ModItems.part_suspension);
		options.add(ModItems.part_plating_1);
		options.add(ModItems.part_hull_1);
		options.add(ModItems.part_mechanism_1);
		options.add(ModItems.part_steel_wheel);
		options.add(ModItems.part_sawblade);
		options.add(ModItems.part_track);
	}

	public TileEntityFoundry() {
		super(3);
	}

	@Override
	public String getName() {
		return "container.foundry";
	}

	@Override
	public void updateEntity() {
		
		if(!worldObj.isRemote) {
			
			/// FIRE OVEN START ///
			if(heat == 0 && slots[1] != null && slots[1].getItem() == Items.coal) {
				this.decrStackSize(1, 1);
				heat = maxHeat;
				this.markDirty();
			}
			/// FIRE OVEN END ///
			
			/// SMELT DOWN STEEL START ///
			float steelContent = slots[0] != null ? getSteel(slots[0]) : 0.0F;
			
			if(heat > 0 && steelContent > 0 && steel + steelContent <= maxSteel) {
				smeltTimer++;
				
				if(smeltTimer > 20) {
					smeltTimer = 0;
					steel += steelContent;
					this.decrStackSize(0, 1);
					heat--;
					this.markDirty();
				}
			} else {
				smeltTimer = 0;
			}
			/// SMELT DOWN STEEL END ///

			/// RECTIFY INDEX START ///
			if(index < 0)
				index = 0;
			
			if(index >= options.size())
				index = 0;
			/// RECTIFY INDEX END ///

			/// CAST START ///
			Item target = options.get(index);
			float cost = recipes.get(target.getUnlocalizedName());
			
			if(canProcess(target, cost)) {
				
				progress++;
				
				if(progress >= castTime) {
					
					steel -= cost;
					progress = 0;
					
					if(slots[2] == null) {
						slots[2] = new ItemStack(target);
					} else {
						slots[2].stackSize++;
					}

					// Try to push the new output into the inventory below immediately:
					tryPushOutputDown();
					
					this.markDirty();
				}
			} else {
				progress = 0;
			}
			/// CAST END ///

			this.updateGauge((int) Math.round(steel * 10), 0, 50);
			this.updateGauge(heat, 1, 50);
			this.updateGauge(progress, 2, 150);
			this.updateGauge(index, 3, 50);
			
		} else {
			
			lastTilt = tilt;
			
			if(progress > 0) {
				
				if(tilt < 30)
					tilt++;
			} else {
				
				if(tilt > 0)
					tilt--;
			}
		}
	}
	
	public void processGauge(int val, int id) {
		
		switch(id) {
		case 0: steel = val / 10F; break;
		case 1: heat = val; break;
		case 2: progress = val; break;
		case 3: index = val; break;
		}
	}
	
	private boolean canProcess(Item target, float cost) {
		
		if(steel < cost)
			return false;
		
		if(slots[2] == null)
			return true;
		
		if(slots[2].getItem() == target && slots[2].stackSize < slots[2].getMaxStackSize())
			return true;
		
		return false;
	}
	
	private float getSteel(ItemStack stack) {
		
		if(stack == null)
			return 0.0F;
		
		int[] ids = OreDictionary.getOreIDs(stack);
		if(ids != null) for(int id : ids) {
			if("ingotSteel".equals(OreDictionary.getOreName(id))) {
				return 1.0F;
			}
		}
		
		Float steel = recipes.get(stack.getItem().getUnlocalizedName());
		
		if(steel == null)
			return 0.0F;
		
		return steel;
	}
	
	public void increment() {
		index++;
		
		if(index >= recipes.size())
			index = 0;
		
		this.markDirty();
	}
	
	public void decrement() {
		index--;
		
		if(index < 0)
			index = recipes.size() - 1;
		
		this.markDirty();
	}
	
	public int getSteelScaled(int i) {
		return (int)(steel * i / maxSteel);
	}
	
	public int getHeatScaled(int i) {
		return (int)(heat * i / maxHeat);
	}
	
	public int getProgressScaled(int i) {
		return (int)(progress * i / castTime);
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
}
