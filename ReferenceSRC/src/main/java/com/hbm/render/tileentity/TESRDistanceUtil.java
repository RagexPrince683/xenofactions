package com.hbm.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public final class TESRDistanceUtil {

	public static final double DETAIL_RENDER_DISTANCE = 35D;
	public static final double DETAIL_RENDER_DISTANCE_SQ = DETAIL_RENDER_DISTANCE * DETAIL_RENDER_DISTANCE;

	private TESRDistanceUtil() { }

	public static boolean shouldRenderDetails(TileEntity tile) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		return player == null || player.getDistanceSq(tile.xCoord + 0.5D, tile.yCoord + 0.5D, tile.zCoord + 0.5D) < DETAIL_RENDER_DISTANCE_SQ;
	}
}
