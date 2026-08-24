package com.hfr.blocks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/** Central definition used by every foundation-based multiblock. */
public final class FoundationSupport {
	private FoundationSupport() { }

	public static boolean isValid(Block block) {
		return block instanceof BlockSpeedy || block == Blocks.hopper;
	}
}
