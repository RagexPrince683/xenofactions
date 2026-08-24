package com.hfr.inventory.container;

import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Builder Depot inventory. Tile slot zero remains deliberately inaccessible. */
public class ContainerMachineBuilder extends Container {
    public static final int DEPOT_SLOTS=27, PLAYER_START=27, SLOT_HIDDEN=-10000;
    private final TileEntityMachineBuilder depot;
    private final int[] realX=new int[63],realY=new int[63];
    private boolean materialsVisible;

    public ContainerMachineBuilder(InventoryPlayer player,TileEntityMachineBuilder depot){
        this.depot=depot;
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)add(new Slot(depot,1+col+row*9,12+col*18,72+row*18));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)add(new Slot(player,col+row*9+9,12+col*18,140+row*18));
        for(int col=0;col<9;col++)add(new Slot(player,col,12+col*18,198));
        // The server keeps its authoritative slots active; only the client presentation is paged.
        setMaterialsVisible(depot.getWorldObj()!=null&&!depot.getWorldObj().isRemote);
    }
    private void add(Slot slot){realX[inventorySlots.size()]=slot.xDisplayPosition;realY[inventorySlots.size()]=slot.yDisplayPosition;addSlotToContainer(slot);}
    /** Moving slots well outside the GUI prevents both rendering and GuiContainer hit testing. */
    public void setMaterialsVisible(boolean visible){materialsVisible=visible;for(int i=0;i<inventorySlots.size();i++){Slot s=(Slot)inventorySlots.get(i);s.xDisplayPosition=visible?realX[i]:SLOT_HIDDEN;s.yDisplayPosition=visible?realY[i]:SLOT_HIDDEN;}}
    public boolean areMaterialsVisible(){return materialsVisible;}

    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
        if(!materialsVisible||index<0||index>=inventorySlots.size())return null;
        Slot source=(Slot)inventorySlots.get(index);if(source==null||!source.getHasStack())return null;
        ItemStack stack=source.getStack(),original=stack.copy();
        boolean moved=index<DEPOT_SLOTS?mergeRespectingSlots(stack,PLAYER_START,inventorySlots.size(),true):mergeRespectingSlots(stack,0,DEPOT_SLOTS,false);
        if(!moved)return null;
        if(stack.stackSize==0)source.putStack(null);else source.onSlotChanged();
        if(stack.stackSize==original.stackSize)return null;
        source.onPickupFromSlot(player,stack);
        return original;
    }
    /** Original two-pass merge: partial compatible stacks first, then valid empty slots. */
    private boolean mergeRespectingSlots(ItemStack moving,int start,int end,boolean reverse){
        boolean changed=false;int i=reverse?end-1:start,step=reverse?-1:1;
        while(moving.stackSize>0&&i>=start&&i<end){Slot slot=(Slot)inventorySlots.get(i);ItemStack existing=slot.getStack();
            if(existing!=null&&slot.isItemValid(moving)&&existing.isItemEqual(moving)&&ItemStack.areItemStackTagsEqual(existing,moving)){
                int limit=Math.min(slot.getSlotStackLimit(),moving.getMaxStackSize()),space=limit-existing.stackSize;
                if(space>0){int amount=Math.min(space,moving.stackSize);existing.stackSize+=amount;moving.stackSize-=amount;slot.onSlotChanged();changed=true;}}
            i+=step;
        }
        i=reverse?end-1:start;
        while(moving.stackSize>0&&i>=start&&i<end){Slot slot=(Slot)inventorySlots.get(i);
            if(!slot.getHasStack()&&slot.isItemValid(moving)){int amount=Math.min(moving.stackSize,Math.min(slot.getSlotStackLimit(),moving.getMaxStackSize()));ItemStack placed=moving.copy();placed.stackSize=amount;slot.putStack(placed);slot.onSlotChanged();moving.stackSize-=amount;changed=true;}
            i+=step;
        }
        return changed;
    }
    @Override public boolean canInteractWith(EntityPlayer player){return depot.isUseableByPlayer(player);}
}
