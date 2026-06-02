package com.hfr.tileentity.machine;

import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.items.ModItems;
import com.hfr.main.MainRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityMachineTemple extends TileEntityMachineBase {

	public Clowder owner = null;

	public TileEntityMachineTemple() {
		super(1);
	}

	@Override
	public String getName() {
		return "container.temple";
	}

	@Override
	public void updateEntity() {
		
		if(!worldObj.isRemote) {

			Ownership o = ClowderTerritory.getOwnerFromInts(xCoord, zCoord);
			if(o != null && o.zone == Zone.FACTION) {
				if(owner != o.owner) {
					if(owner != null)
						owner.addPrestigeGen(-Clowder.TempleRate(), worldObj);
					owner = o.owner;
					owner.addPrestigeGen(Clowder.TempleRate(), worldObj);
					this.markDirty();
				}
			} else if(owner != null) {
				owner.addPrestigeGen(-Clowder.TempleRate(), worldObj);
				owner = null;
				this.markDirty();
			}

			if(worldObj.rand.nextInt(MainRegistry.temple * 20) == 0) {
				
				if(slots[0] == null) {
					slots[0] = new ItemStack(ModItems.scroll);
					
				} else if(slots[0].getItem() == ModItems.scroll && slots[0].stackSize < 64) {
					slots[0].stackSize++;
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		owner = Clowder.getClowderFromName(nbt.getString("owner"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(owner != null)
			nbt.setString("owner", owner.name);
	}
}
