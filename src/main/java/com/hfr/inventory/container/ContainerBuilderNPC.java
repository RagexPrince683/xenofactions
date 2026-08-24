package com.hfr.inventory.container;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
/** Slotless server window used by the worker-focused Builder NPC screen. */
public class ContainerBuilderNPC extends Container { @Override public boolean canInteractWith(EntityPlayer player){return true;} }
