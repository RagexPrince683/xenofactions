package com.hfr.builder;

import java.util.UUID;
import com.hfr.clowder.*;
import com.hfr.config.XFConfig;
import net.minecraft.world.World;

public final class BuilderTerritory {
	private BuilderTerritory(){}
	public static boolean mayChange(World w,int x,int z,UUID faction,boolean destroy){
		Clowder actor=faction==null?null:Clowder.getClowderFromUUID(faction.toString());
		if(actor==null)return false;
		ClowderTerritory.Ownership o=ClowderTerritory.getOwnerFromInts(w,x,z);
		if(o==null||o.zone==ClowderTerritory.Zone.WILDERNESS)return XFConfig.builderAllowWilderness;
		if(o.zone!=ClowderTerritory.Zone.FACTION||o.owner==null)return false;
		if(o.owner==actor)return true;
		return o.owner.canVisitorAccess(actor,destroy?FactionPermission.DESTROY:FactionPermission.BUILD);
	}
}
