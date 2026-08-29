package com.hbm.blocks.bomb;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import org.apache.logging.log4j.Level;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.main.MainRegistry;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockFissureBomb extends BlockTNTBase {

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		world.spawnEntityInWorld(EntityNukeExplosionMK5.statFac(world, 69, x, y, z)); //31/2
		EntityNukeTorex.statFac(world, x, y, z, 69); //pakistani weapon 45kt

		//int range = 5;

		//for(int i = -range; i <= range; i++) {
		//	for(int j = -range; j <= range; j++) {
		//		for(int k = -range; k <= range; k++) {
//
		//			//int a = (int) Math.floor(x + i);
		//			//int b = (int) Math.floor(y + j);
		//			//int c = (int) Math.floor(z + k);
//
		//			//Block block = world.getBlock(a, b, c);
//
		//			//if(block == ModBlocks.ore_bedrock) {
		//			//	world.setBlock(a, b, c, ModBlocks.ore_volcano);
		//			//} else if(block == ModBlocks.ore_bedrock_oil) {
		//			//	world.setBlock(a, b, c, Blocks.bedrock);
		//			//}
		//			//wtf no wtf oh my god that's all this fucking for loop does???
		//		}
		//	}
		//}
	}
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
	if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
			MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getCommandSenderName());
		}
	}
	}
}
