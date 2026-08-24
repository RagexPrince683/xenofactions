package com.hfr.builder;

import java.util.UUID;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Shared, side-neutral validation for the worker assigned to a Builder Depot. */
public final class BuilderGuiResolver {
    private BuilderGuiResolver() { }
    public static TileEntityMachineBuilder getDepot(World world,int x,int y,int z){if(world==null)return null;TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityMachineBuilder?(TileEntityMachineBuilder)tile:null;}
    public static EntityFactionBuilder getAssignedBuilder(TileEntityMachineBuilder depot){
        if(depot==null||depot.getWorldObj()==null)return null;UUID assigned=depot.getAssignedBuilderId();if(assigned==null)return null;
        EntityFactionBuilder builder=depot.getLoadedBuilder();
        if(builder==null||builder.isDead||!assigned.equals(builder.getUniqueID())||builder.worldObj!=depot.getWorldObj()||builder.dimension!=depot.getWorldObj().provider.dimensionId)return null;
        return builder.getDepotDimension()==depot.getWorldObj().provider.dimensionId&&builder.getDepotX()==depot.xCoord&&builder.getDepotY()==depot.yCoord&&builder.getDepotZ()==depot.zCoord?builder:null;
    }
}
