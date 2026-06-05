package com.hfr.handler;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.AuxParticlePacketNT;
import com.hfr.packet.effect.ExplosionSoundPacket;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ExplosionSound {

	public static final double max = 8;
	private static final String MCHELI_EXPLOSION_CLASS = "mcheli.MCH_Explosion";

	public static void handleExplosion(World world, Explosion explosion) {

		if(explosion == null)
			return;

		handleExplosion(world, explosion.explosionX, explosion.explosionY, explosion.explosionZ, explosion.explosionSize, true);
	}

	/**
	 * Compatibility hook for MCHeli explosions. MCHeli creates its own
	 * Explosion subclass directly instead of going through World#newExplosion,
	 * so Forge's ExplosionEvent.Detonate is not always fired and the normal
	 * Forge event listener never gets a chance to send the far explosion
	 * sound packet.
	 *
	 * Add this call in MCH_Explosion after doExplosionB(true):
	 * ExplosionSound.handleMCHeliExplosion(w, exp);
	 *
	 * This sends only the far explosion sound packet. It intentionally skips
	 * Xenofactions' AuxParticlePacketNT because MCHeli already spawns its own
	 * explosion particles.
	 */
	public static void handleMCHeliExplosion(World world, Explosion explosion) {
		if(explosion == null)
			return;

		if(!MCHELI_EXPLOSION_CLASS.equals(explosion.getClass().getName()))
			return;

		if(!isMCHeliExplosionSoundEnabled(explosion))
			return;

		handleExplosion(world, explosion.explosionX, explosion.explosionY, explosion.explosionZ, explosion.explosionSize, false);
	}

	/**
	 * Coordinate-only MCHeli hook for patched MCH_Explosion sources that do not
	 * want to expose the Explosion instance.
	 */
	public static void handleMCHeliExplosion(World world, double x, double y, double z, float pow) {
		handleExplosion(world, x, y, z, pow, false);
	}

	private static void handleExplosion(World world, double x, double y, double z, float pow, boolean spawnParticles) {

		if(world == null || world.isRemote)
			return;

		if(pow < 3)
			return;

		if(pow > max)
			pow = (float)max;

		double farRange = 1000D * pow / max;
		PacketDispatcher.wrapper.sendToAllAround(new ExplosionSoundPacket((int)x, (int)y, (int)z, pow), new TargetPoint(world.provider.dimensionId, x, y, z, farRange));

		if(spawnParticles) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "explosion");
			data.setFloat("strength", pow);
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.dimensionId, x, y, z, 150));
		}
	}

	private static boolean isMCHeliExplosionSoundEnabled(Explosion explosion) {
		try {
			return explosion.getClass().getField("isPlaySound").getBoolean(explosion);
		} catch(Exception ignored) {
			return true;
		}
	}

	public static void handleClient(EntityPlayer player, int x, int y, int z, float pow) {

		World world = player.worldObj;
		double closeRange = 50D * pow / max;
		double mediumRange = 250D * pow / max;
		double farRange = 1000D * pow / max;

		double distance = Math.sqrt(Math.pow(player.posX - x, 2) + Math.pow(player.posY - y, 2) + Math.pow(player.posZ - z, 2));

		if(distance <= closeRange) {
			world.playSound(player.posX, player.posY, player.posZ, "hfr:explosion.close", 100, 0.8F + world.rand.nextFloat() * 0.4F, false);
		} else if(distance <= mediumRange) {
			world.playSound(player.posX, player.posY, player.posZ, "hfr:explosion.medium", 100, 0.8F + world.rand.nextFloat() * 0.4F, false);
		} else if(distance <= farRange) {
			world.playSound(player.posX, player.posY, player.posZ, "hfr:explosion.rumble", 100, 0.8F + world.rand.nextFloat() * 0.4F, false);
		}
	}

}
