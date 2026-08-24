package com.hfr.inventory.container;

import com.hfr.builder.BuilderDepotService;
import com.hfr.builder.BuilderGuiResolver;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import com.hfr.util.XFLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** The assigned worker's inventory; the Depot inventory remains on its own screen. */
public class ContainerBuilderNPC extends Container {
    public static final int BUILDER_START=0;
    public static final int BUILDER_SLOTS=EntityFactionBuilder.INVENTORY_SIZE;
    public static final int PLAYER_MAIN_START=BUILDER_START+BUILDER_SLOTS;
    public static final int PLAYER_MAIN_SLOTS=27;
    public static final int PLAYER_HOTBAR_START=PLAYER_MAIN_START+PLAYER_MAIN_SLOTS;
    public static final int PLAYER_HOTBAR_SLOTS=9;
    public static final int TOTAL_SLOTS=PLAYER_HOTBAR_START+PLAYER_HOTBAR_SLOTS;
    // Vanilla 1.7.10 ContainerPlayer has 45 slots. Seeing Size: 45 while applying
    // this window's 63-stack S30 packet means the client never installed this container.
    private final TileEntityMachineBuilder depot;
    private final EntityFactionBuilder builder;

    public ContainerBuilderNPC(InventoryPlayer player,TileEntityMachineBuilder depot,EntityFactionBuilder builder){this(player,depot,builder,builder);}
    private ContainerBuilderNPC(InventoryPlayer player,TileEntityMachineBuilder depot,IInventory inventory,EntityFactionBuilder builder){
        this.depot=depot;this.builder=builder;
        if(inventory==null||inventory.getSizeInventory()!=BUILDER_SLOTS)throw new IllegalArgumentException("Builder inventory must expose "+BUILDER_SLOTS+" slots");
        for(int slot=BUILDER_START;slot<PLAYER_MAIN_START;slot++){int local=slot-BUILDER_START;addSlotToContainer(new Slot(inventory,local,8+(local%9)*18,72+(local/9)*18));}
        for(int slot=PLAYER_MAIN_START;slot<PLAYER_HOTBAR_START;slot++){int local=slot-PLAYER_MAIN_START;addSlotToContainer(new Slot(player,local+9,8+(local%9)*18,140+(local/9)*18));}
        for(int slot=PLAYER_HOTBAR_START;slot<TOTAL_SLOTS;slot++){int local=slot-PLAYER_HOTBAR_START;addSlotToContainer(new Slot(player,local,8+local*18,198));}
        if(inventorySlots.size()!=TOTAL_SLOTS)throw new IllegalStateException("Builder container slot layout is invalid");
        XFLog.debug("[XF Builder] Created Builder NPC container side="+(player.player.worldObj.isRemote?"CLIENT":"SERVER")+" builderSlots="+BUILDER_SLOTS+" totalSlots="+TOTAL_SLOTS);
    }
    /** Keep 63 network slots even if client entity tracking trails the GUI-open packet. */
    public static ContainerBuilderNPC createClient(InventoryPlayer player,TileEntityMachineBuilder depot,EntityFactionBuilder builder){
        if(builder!=null)return new ContainerBuilderNPC(player,depot,builder);
        XFLog.debug("[XF Builder] Assigned Builder is not tracked client-side; using a "+BUILDER_SLOTS+"-slot synchronization inventory");
        return new ContainerBuilderNPC(player,depot,new InventoryBasic("container.builder_npc.sync",false,BUILDER_SLOTS),null);
    }
    @Override public boolean canInteractWith(EntityPlayer player){
        // A client container is only the fixed 63-slot network mirror.  In particular, it
        // must survive while the Depot tile/entity tracking catches up with FML's open-GUI
        // packet.  All authorization remains on the logical server.
        if(player.worldObj.isRemote)return true;
        return builder!=null&&depot!=null&&BuilderGuiResolver.getAssignedBuilder(depot)==builder&&depot.getWorldObj()==player.worldObj&&player instanceof EntityPlayerMP&&BuilderDepotService.mayBuild((EntityPlayerMP)player,depot)&&builder.isUseableByPlayer(player);
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
        if(index<0||index>=inventorySlots.size())return null;ItemStack result=null;Slot slot=(Slot)inventorySlots.get(index);
        if(slot!=null&&slot.getHasStack()){ItemStack stack=slot.getStack();result=stack.copy();if(index<PLAYER_MAIN_START){if(!mergeItemStack(stack,PLAYER_MAIN_START,TOTAL_SLOTS,true))return null;}else if(!mergeItemStack(stack,BUILDER_START,PLAYER_MAIN_START,false))return null;if(stack.stackSize==0)slot.putStack(null);else slot.onSlotChanged();if(stack.stackSize==result.stackSize)return null;slot.onPickupFromSlot(player,stack);}return result;
    }
}
