package com.hfr.tileentity.machine;

import com.hfr.blocks.FoundationSupport;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.items.ModItems;
import com.hfr.main.MainRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityMachineFederalReserve extends TileEntityMachineBase {

    public Clowder owner = null;

    public TileEntityMachineFederalReserve() {
        super(6);
    }

    @Override
    public String getName() {
        return "container.machineRes";
    }

    @Override
    public void updateEntity() {
        // Deprecated compatibility tile: intentionally inert.
    }

    public boolean operational() {
        return false;
    }

    public boolean hasSpace() {

        for(int i = 0; i < 4; i++) {

            if((slots[i] == null || (slots[i] != null && slots[i].getItem() == ModItems.science && slots[i].stackSize < slots[i].getMaxStackSize())))
                return true;
        }

        return false;
    }

    private static final int[] access = new int[] { 0, 1, 2, 3, 4 };
    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return access;
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
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        owner = Clowder.getClowderFromName("owner");

        NBTTagList list = nbt.getTagList("items", 10);
        slots = new ItemStack[getSizeInventory()];

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt1 = list.getCompoundTagAt(i);
            byte b0 = nbt1.getByte("slot");
            if (b0 >= 0 && b0 < slots.length) {
                slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if(owner != null)
            nbt.setString("owner", owner.name);

        NBTTagList list = new NBTTagList();

        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                NBTTagCompound nbt1 = new NBTTagCompound();
                nbt1.setByte("slot", (byte) i);
                slots[i].writeToNBT(nbt1);
                list.appendTag(nbt1);
            }
        }
        nbt.setTag("items", list);
    }

}
