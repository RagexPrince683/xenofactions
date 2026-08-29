package com.hbm.blocks.bomb;

import com.hbm.config.GeneralConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.BombSiteSavedData;
import com.hbm.tileentity.bomb.TileEntityCharge;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;

import java.util.List;

public class BlockChargeC4CSGO extends BlockChargeC4 {

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		return isInBombSiteForSide(world, x, y, z, side) && super.canPlaceBlockOnSide(world, x, y, z, side);
	}

	private boolean isInBombSiteForSide(World world, int x, int y, int z, int side) {
		if(world.isRemote) {
			return true;
		}

		if(BombSiteSavedData.isBombSite(world, x, y, z)) {
			return true;
		}

		ForgeDirection dir = ForgeDirection.getOrientation(side);
		return BombSiteSavedData.isBombSite(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {
			safe = true;
			world.setBlockToAir(x, y, z);
			safe = false;

			ExplosionVNT xnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 30F).makeStandard();
			xnt.setBlockAllocator(null);
			xnt.setBlockProcessor(null);
			xnt.explode();
			ExplosionLarge.spawnParticles(world, x + 0.5, y + 0.5, z + 0.5, 250);

			return BombReturnCode.DETONATED;
		}

		return BombReturnCode.UNDEFINED;
	}

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType() {
		return renderID;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		//list.add(EnumChatFormatting.BLUE + "Does not drop blocks.");
		list.add(EnumChatFormatting.RED + "Can only be placed inside defined bomb sites.");
	}
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
	if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
			MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getCommandSenderName());
		}
	}
	}

	//@Override
	//public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side,
	//					   float fX, float fY, float fZ, ToolType tool) {
	//	if(tool != ToolType.DEFUSER)
	//		return false;
//
	//	if(world.isRemote)
	//		return true;
//
	//	TileEntityCharge charge = (TileEntityCharge) world.getTileEntity(x, y, z);
//
	//	if(charge.started) {
	//		charge.defusePending = true;
	//		charge.defusePendingTicks = TileEntityCharge.DEFUSE_DELAY_TICKS;
	//		charge.defusingPlayer = player.getCommandSenderName();
//
	//		world.scheduleBlockUpdate(x, y, z, this, 1);
	//		world.markBlockForUpdate(x, y, z);
	//		world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "hbm:weapon.fstbmbStart", 1.0F, 1.0F);
//
	//		charge.markDirty();
	//	} else {
	//		safe = true;
	//		this.dismantlenodrop(world, x, y, z);
	//		safe = false;
	//	}
//
	//	return true;
	//}
	//doesn't work :c

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else {

			TileEntityCharge charge = (TileEntityCharge) world.getTileEntity(x, y, z);

			if(!charge.started) {

				if(player.isSneaking()) {

					if(charge.timer > 0) {

						//todo defuser code here since this fucking mod is retarded and special needs because we need
						// 2 GODDAMN METHODS FOR THE EXACT SAME FUCKING BULLSHIT FOR SOME FUCKING REASON
						// I'm so fucking confused bro

						//if(player.getHeldItem() = ModItems.defuser)

						charge.started = true;
						world.playSoundEffect(x, y, z, "hbm:weapon.fstbmbStart", 1.0F, 1.0F);
					}
				} else {


					//if(charge.timer == 600) { charge.timer = 1200; }
					if(charge.timer == 0) { charge.timer = 1200; }

					world.playSoundEffect(x, y, z, "hbm:item.techBoop", 1.0F, 1.0F);
				}

				charge.markDirty();
			}

			return false;
		}
	}
}
