package com.hfr.compat;

import net.minecraft.entity.player.EntityPlayer;

/** Optional bridge for radiation systems supplied by other mods. */
public interface RadiationCompat {
	void clearRadiation(EntityPlayer player);
}
