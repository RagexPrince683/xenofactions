package com.hfr.render.util;

import com.hfr.lib.RefStrings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class RenderAccessoryUtility {

	public static ResourceLocation hbm = new ResourceLocation(RefStrings.MODID + ":textures/models/CapeHbm.png");
	public static ResourceLocation fire = new ResourceLocation(RefStrings.MODID + ":textures/models/CapeFire.png");
	public static ResourceLocation test = new ResourceLocation(RefStrings.MODID + ":textures/models/CapeTest.png");
	
	public static ResourceLocation getCloakFromPlayer(EntityPlayer player) {
		
		String uuid = player.getUniqueID().toString();
		String name = player.getDisplayName();
		if(name.startsWith("Player")) {
			return test;
		}
		
		return null;
	}

}
