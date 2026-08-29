package com.hbm.blocks.bomb;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.turret.TurretBase;
//import com.hbm.entity.particle.EntityGasFlameFX;
import com.hbm.lib.ModDamageSource;
import com.hbm.tileentity.bomb.TileEntityTurretCIWS;

import com.hbm.blocks.ITooltipProvider;

import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TurretCIWS extends TurretBase {

	public TurretCIWS(Material mat) {
		super(mat);
	}
	Random rand = new Random();

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityTurretCIWS();
	}

	@Override
	public boolean executeHoldAction(World world, int i, double yaw, double pitch, int x, int y, int z) {

		TileEntityTurretCIWS te = (TileEntityTurretCIWS)world.getTileEntity(x, y, z);
		boolean flag = false;

		if(!te.hasPower()) {
			te.spin = 0;
			return false;
		}

		if(pitch < -60)
			pitch = -60;
		if(pitch > 30)
			pitch = 30;



		if(te.spin < 35)
			te.spin += 5;

		if(te.spin > 25 && i % 2 == 0 && te.hasPowerForShot()) {
			Vec3 vector = Vec3.createVectorHelper(
				-Math.sin(yaw / 180.0F * (float) Math.PI) * Math.cos(pitch / 180.0F * (float) Math.PI),
				-Math.sin(pitch / 180.0F * (float) Math.PI),
				Math.cos(yaw / 180.0F * (float) Math.PI) * Math.cos(pitch / 180.0F * (float) Math.PI));

			vector.normalize();

			if(!world.isRemote) {

				rayShot(world, vector, x + vector.xCoord * 2.5 + 0.5, y + vector.yCoord * 2.5 + 0.5, z + vector.zCoord * 2.5 + 0.5, 100, 40.0F, 40); //40% accuracy should be, 100 for debugging

				//EntityGasFlameFX smoke = new EntityGasFlameFX(world);
				//smoke.posX = x + vector.xCoord * 2.5 + 0.5;
				//smoke.posY = y + vector.yCoord * 2.5 + 1.5;
				//smoke.posZ = z + vector.zCoord * 2.5 + 0.5;
//
				//smoke.motionX = vector.xCoord * 0.25;
				//smoke.motionY = vector.yCoord * 0.25;
				//smoke.motionZ = vector.zCoord * 0.25;

				//world.spawnEntityInWorld(smoke);
				//TODO add back in
			}

			te.consumeShotPower();

			world.playSoundEffect(x, y, z, "hbm:weapon.gun_m61a1_snd", 5.0F, 1.25F);

			flag = true;
		}

		return flag;
	}

	private void rayShot(World world, Vec3 vec, double posX, double posY, double posZ, int range, float damage, int hitPercent) {
		for(float i = 0; i < range; i += 0.25F) {
			double pX = posX + vec.xCoord * i;
			double pY = posY + vec.yCoord * i;
			double pZ = posZ + vec.zCoord * i;

			if(world.getBlock((int)pX, (int)pY, (int)pZ).getMaterial() != Material.air)
				break;

			List<Entity> hit =
				world.getEntitiesWithinAABBExcludingEntity(
					null,
					AxisAlignedBB.getBoundingBox(
						pX - 0.125,
						pY - 0.125,
						pZ - 0.125,
						pX + 0.125,
						pY + 0.125,
						pZ + 0.125
					)
				);

			for(int j = 0; j < hit.size(); j++) {
				Entity ent = hit.get(j);


				if(rand.nextInt(100) < hitPercent) {
					ent.attackEntityFrom(ModDamageSource.shrapnel, 40.0F);
				}
			}
		}
	}


	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(EnumChatFormatting.YELLOW + "Requires power");
		list.add(EnumChatFormatting.YELLOW + "Uses 20x102 mm ammunition");
		list.add(EnumChatFormatting.YELLOW + "Intercepts incoming missiles");
	}

	@Override
	public void executeReleaseAction(World world, int i, double yaw, double pitch, int x, int y, int z) { }
}
