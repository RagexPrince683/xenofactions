package com.hbm.items.block;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemBlockNukeInfo extends ItemBlockBase {

	public ItemBlockNukeInfo(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(stack, player, list, bool);
		float yield = getYieldKt(field_150939_a);
		if(yield > 0F) {
			list.add(EnumChatFormatting.BOLD + "Yield: " + EnumChatFormatting.GRAY + formatYield(yield));
		}
	}

	private float getYieldKt(Block block) {
		if(block == ModBlocks.nuke_gadget) return 22F;
		if(block == ModBlocks.nuke_boy) return 15F;
		if(block == ModBlocks.nuke_man) return 21F;
		if(block == ModBlocks.nuke_mike) return 10_400F;
		if(block == ModBlocks.nuke_shrimp) return 15_000F;
		if(block == ModBlocks.nuke_tsar) return BombConfig.tsarRadius == BombConfig.tsarlegitrad ? 50_000F : 100_000F;
		return 0F;
	}

	private String formatYield(float kilotons) {
		if(kilotons >= 1_000F) return (kilotons / 1_000F) + " Mt (" + Math.round(kilotons) + " kt)";
		return Math.round(kilotons) + " kt";
	}
}
