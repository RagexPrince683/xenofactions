package com.hfr.inventory.container;

import com.hfr.builder.BuilderDepotService;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** The assigned worker's inventory; the Depot inventory remains on its own screen. */
public class ContainerBuilderNPC extends Container {
    private final TileEntityMachineBuilder depot;
    private final EntityFactionBuilder builder;
    public ContainerBuilderNPC(InventoryPlayer player,TileEntityMachineBuilder depot,EntityFactionBuilder builder){
        this.depot=depot;this.builder=builder;
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(builder,col+row*9,8+col*18,72+row*18));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col+row*9+9,8+col*18,140+row*18));
        for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col,8+col*18,198));
    }
    @Override public boolean canInteractWith(EntityPlayer player){return depot!=null&&builder!=null&&depot.getWorldObj()==player.worldObj&&depot.getAssignedBuilderId()!=null&&depot.getAssignedBuilderId().equals(builder.getUniqueID())&&depot.getLoadedBuilder()==builder&&(player.worldObj.isRemote||(player instanceof net.minecraft.entity.player.EntityPlayerMP&&BuilderDepotService.mayBuild((net.minecraft.entity.player.EntityPlayerMP)player,depot)))&&builder.isUseableByPlayer(player);}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){ItemStack result=null;Slot slot=(Slot)inventorySlots.get(index);if(slot!=null&&slot.getHasStack()){ItemStack stack=slot.getStack();result=stack.copy();if(index<27){if(!mergeItemStack(stack,27,63,true))return null;}else if(!mergeItemStack(stack,0,27,false))return null;if(stack.stackSize==0)slot.putStack(null);else slot.onSlotChanged();if(stack.stackSize==result.stackSize)return null;slot.onPickupFromSlot(player,stack);}return result;}
}
