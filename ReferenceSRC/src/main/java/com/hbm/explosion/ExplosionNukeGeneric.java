package com.hbm.explosion;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.util.ForgeDirection;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.VersatileConfig;
import com.hbm.entity.grenade.EntityGrenadeASchrab;
import com.hbm.entity.grenade.EntityGrenadeNuclear;
import com.hbm.entity.missile.EntityMIRV;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.entity.projectile.EntityExplosiveBeam;
import com.hbm.interfaces.Spaghetti;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ArmorUtil;

import api.hbm.energymk2.IEnergyHandlerMK2;
import cofh.api.energy.IEnergyProvider;

@Spaghetti("this sucks ass")
public class ExplosionNukeGeneric {

	private final static Random random = new Random();
	/**
	 * Nuclear blast effects spread primarily across the ground. Keeping the upper
	 * edge below a full blast radius prevents aircraft from being damaged simply
	 * because they are somewhere above a detonation, while the larger lower edge
	 * still lets an airburst affect targets beneath it.
	 */
	private static final double BLAST_HEIGHT_ABOVE_FACTOR = 0.5D;
	private static final double BLAST_HEIGHT_BELOW_FACTOR = 10.0D;
	private static final int BLAST_FIRE_SECONDS = 15;

	public static void empBlast(World world, int x, int y, int z, int bombStartStrength) {
		int r = bombStartStrength;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22) {
						emp(world, X, Y, Z);
					}
				}
			}
		}
	}

	public static void dealDamage(World world, double x, double y, double z, double radius) {
		dealDamage(world, x, y, z, radius, 250F);
	}

	/** Legacy immediate wrapper. MK5 uses dealDamageFront so terrain work cannot repeat exposure. */
	public static void dealDamage(World world, double x, double y, double z, double radius, float maxDamage) {
		dealDamageFront(world, x, y, z, 0.0D, radius, radius, maxDamage);
	}

	/** Applies an expanding pressure front once. Radius is the 5-psi gameplay radius. */
	public static void dealDamageFront(World world, double x, double y, double z, double previousRadius, double currentRadius, double fivePsiRadius, float maxDamage) {
		if(currentRadius <= previousRadius || fivePsiRadius <= 0.0D) return;
		double heightAbove = getBlastHeightAbove(currentRadius);
		double heightBelow = getBlastHeightBelow(currentRadius);
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(x - currentRadius, y - heightBelow, z - currentRadius, x + currentRadius, y + heightAbove, z + currentRadius));
		for(Entity e : list) {
			double horizontalDistance = getHorizontalDistance(e, x, z);
			double entityHeight = e.posY + e.getEyeHeight() - y;
			// The initial shell must include the hypocenter. Using <= here permanently
			// excluded entities directly below an airburst because every later shell has
			// already passed horizontal distance zero.
			if(horizontalDistance < previousRadius || horizontalDistance > currentRadius || entityHeight < -heightBelow || entityHeight > heightAbove || isExplosionExempt(e)) continue;
			double exposure = Library.isObstructed(world, x, y, z, e.posX, e.posY + e.getEyeHeight(), e.posZ) ? 0.20D : 1.0D;
			double psi = getOverpressurePsi(horizontalDistance / fivePsiRadius) * exposure;
			if(psi <= 0.0D) continue;
			float damage = (float)Math.min(maxDamage, psi * 2.5D);
			e.attackEntityFrom(ModDamageSource.nuclearBlast, damage);
			Vec3 knock = Vec3.createVectorHelper(e.posX - x, e.posY + e.getEyeHeight() - y, e.posZ - z).normalize();
			double impulse = Math.min(1.8D, psi * 0.035D) * exposure;
			e.motionX += knock.xCoord * impulse;
			e.motionY += knock.yCoord * impulse;
			e.motionZ += knock.zCoord * impulse;
		}
	}

	/** Localized seismic damage for contained shots; it never projects an atmospheric front onto the surface. */
	public static void dealGroundShock(World world, double x, double y, double z, double radius, double burialDepth) {
		if(radius <= 0D) return;
		double surfaceY = y + burialDepth;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(x - radius, y - radius, z - radius, x + radius, Math.min(surfaceY - 1D, y + radius), z + radius));
		for(Entity entity : list) {
			if(isExplosionExempt(entity)) continue;
			double distance = entity.getDistance(x, y, z);
			if(distance > radius) continue;
			float damage = (float)(30D * (1D - distance / radius));
			if(damage > 0F) entity.attackEntityFrom(ModDamageSource.nuclearBlast, damage);
		}
	}

	/** Piecewise log-friendly gameplay curve anchored at 50/20/5/2/1 psi. */
	public static double getOverpressurePsi(double relativeDistance) {
		final double[] distances = { 0.30D, 0.50D, 1.00D, 1.80D, 2.50D };
		final double[] pressures = { 50.0D, 20.0D, 5.0D, 2.0D, 1.0D };
		if(relativeDistance <= distances[0]) return pressures[0];
		if(relativeDistance >= distances[distances.length - 1]) return 0.0D;
		for(int i = 1; i < distances.length; i++) {
			if(relativeDistance <= distances[i]) {
				double t = (relativeDistance - distances[i - 1]) / (distances[i] - distances[i - 1]);
				return pressures[i - 1] + (pressures[i] - pressures[i - 1]) * t;
			}
		}
		return 0.0D;
	}

	public static double getBlastHeightAbove(double radius) {
		return radius * BLAST_HEIGHT_ABOVE_FACTOR;
	}

	public static double getBlastHeightBelow(double radius) {
		return radius * BLAST_HEIGHT_BELOW_FACTOR;
	}

	private static double getHorizontalDistance(Entity entity, double x, double z) {
		double deltaX = entity.posX - x;
		double deltaZ = entity.posZ - z;
		return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
	}

	@Spaghetti("just look at it")
	private static boolean isExplosionExempt(Entity e) {

		//if (e instanceof EntityOcelot ||
		//		e instanceof EntityMIRV ||
		//		e instanceof EntityGrenadeASchrab ||
		//		e instanceof EntityGrenadeNuclear ||
		//		e instanceof EntityExplosiveBeam ||
		//		e instanceof EntityBulletBaseNT ||
		//		e instanceof EntityPlayer &&
		//		//ArmorUtil.checkArmor((EntityPlayer) e, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
		//	return true;
		//}
		//god i fucking hate this shitty mod

		if (e instanceof EntityPlayerMP && ((EntityPlayerMP)e).theItemInWorldManager.getGameType() == GameType.CREATIVE) {
			return true;
		}

		return false;
	}

	public static void vapor(World world, int x, int y, int z, int bombStartStrength) {
		int r = bombStartStrength * 2;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22)
						vaporDest(world, X, Y, Z);
				}
			}
		}
	}

	public static final float LIQUID_RESISTANCE_THRESHOLD = 200f; // keep blocks with high resistance
	public static final float PROTECTION_DIVISOR = 300f;
	private static final Random RANDOM = new Random();

	public static int destruction(World world, int x, int y, int z) {
		if (world.isRemote) return 0; // server-only

		Block block = world.getBlock(x, y, z);
		if (block == null) return 0;

		// skip liquids (don't destroy oceans/rivers)
		Material mat = block.getMaterial();
		if (mat.isLiquid()) {
			return 0;
		}

		// cache resistance once
		float resistance = block.getExplosionResistance(null);

		// If very resistant, we treat it as "protected" and possibly replace with scraps/gravel/etc.
		if (resistance >= LIQUID_RESISTANCE_THRESHOLD) {
			int protection = Math.max(1, (int) (resistance / PROTECTION_DIVISOR)); // at least 1 so division by zero avoided

			// Special-case certain blocks
			if (block == ModBlocks.brick_concrete) {
				if (RANDOM.nextInt(8) == 0) {
					replaceBlockPreservingTile(world, x, y, z, Blocks.gravel, 0);
					return 0;
				}
			} else if (block == ModBlocks.brick_light) {
				int r = RANDOM.nextInt(3);
				if (r == 0) {
					replaceBlockPreservingTile(world, x, y, z, ModBlocks.waste_planks, 0);
					return 0;
				} else if (r == 1) {
					replaceBlockPreservingTile(world, x, y, z, ModBlocks.block_scrap, 0);
					return 0;
				}
			} else if (block == ModBlocks.brick_obsidian) {
				if (RANDOM.nextInt(20) == 0) {
					replaceBlockPreservingTile(world, x, y, z, Blocks.obsidian, 0);
					return protection;
				}
			} else if (block == Blocks.obsidian) {
				replaceBlockPreservingTile(world, x, y, z, ModBlocks.gravel_obsidian, 0);
				return 0;
			} else {
				// generic protected-block behavior
				if (RANDOM.nextInt(protection + 3) == 0) {
					replaceBlockPreservingTile(world, x, y, z, ModBlocks.block_scrap, 0);
				}
			}

			return protection;
		}

		// otherwise kill the block
		// remove tile-entity (if any) before setting to air to avoid stale TEs
		if (world.getTileEntity(x, y, z) != null) {
			world.removeTileEntity(x, y, z);
		}
		//can use setBlockToAir, but setBlock(x,y,z,Blocks.air,0,2) is fine too.
		world.setBlockToAir(x, y, z);
		return 0;
	}

	/** helper to set a block while removing any existing tile entity first */
	private static void replaceBlockPreservingTile(World world, int x, int y, int z, Block newBlock, int meta) {
		if (world.getTileEntity(x, y, z) != null) {
			world.removeTileEntity(x, y, z);
		}
		world.setBlock(x, y, z, newBlock, meta, 3);
	}


	public static int vaporDest(World world, int x, int y, int z) {
		if (!world.isRemote) {
			Block b = world.getBlock(x,y,z);

			if (b.getMaterial().isLiquid()) {
				if (random.nextInt(50) == 0) { // 1 in 50 chance
					world.setBlock(x, y, z, Blocks.air, 0, 2);
				}
			} else if (b.getExplosionResistance(null)<0.5f //most light things
					|| b == Blocks.web || b == ModBlocks.red_cable
					) { //|| b instanceof BlockLiquid WHY WOULD THIS BE INTENTIONAL????? WTF
				world.setBlock(x, y, z, Blocks.air,0, 2);
				return 0;
			} else if (b.getExplosionResistance(null)<=3.0f && !b.isOpaqueCube()){
				if(b != Blocks.chest && b != Blocks.farmland){
					//destroy all medium resistance blocks that aren't chests or farmland
					world.setBlock(x, y, z, Blocks.air,0,2);
					return 0;
				}
			}

			if (b.isFlammable(world, x, y, z, ForgeDirection.UP)
					&& world.getBlock(x, y + 1, z) == Blocks.air) {
				world.setBlock(x, y + 1, z, Blocks.fire,0,2);
			}
			return (int)( b.getExplosionResistance(null)/300f);
		}
		return 0;
	}

	public static void waste(World world, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22 + world.rand.nextInt(r22 / 5)) {
						if (world.getBlock(X, Y, Z) != Blocks.air || world.getBlock(X, Y, Z) != Blocks.water)
							wasteDest(world, X, Y, Z);
					}
				}
			}
		}
	}

	public static void wasteDest(World world, int x, int y, int z) {
		if (!world.isRemote) {
			int rand;
			Block b = world.getBlock(x,y,z);
			if (b == Blocks.wooden_door || b == Blocks.iron_door) {
				world.setBlock(x, y, z, Blocks.air,0,2);
			}

			else if (b == Blocks.grass) {
				world.setBlock(x, y, z, ModBlocks.waste_earth);
			}

			else if (b == Blocks.mycelium) {
				world.setBlock(x, y, z, ModBlocks.waste_mycelium);
			}

			else if (b == Blocks.sand) {
				rand = random.nextInt(20);
				if (rand == 1 && world.getBlockMetadata(x, y, z) == 0) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite);
				}
				if (rand == 1 && world.getBlockMetadata(x, y, z) == 1) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite_red);
				}
			}

			else if (b == Blocks.clay) {
				world.setBlock(x, y, z, Blocks.hardened_clay);
			}

			else if (b == Blocks.mossy_cobblestone) {
				world.setBlock(x, y, z, Blocks.coal_ore);
			}

			else if (b == Blocks.coal_ore) {
				rand = random.nextInt(50);
				if (rand == 1) {
					world.setBlock(x, y, z, Blocks.diamond_ore);
				}
				if (rand == 9) {
					world.setBlock(x, y, z, Blocks.emerald_ore);
				}
			}

			else if (b == Blocks.log || b == Blocks.log2) {
				world.setBlock(x, y, z, ModBlocks.waste_log);
			}

			else if (b == Blocks.brown_mushroom_block) {
				if (world.getBlockMetadata(x, y, z) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					world.setBlock(x, y, z, Blocks.air,0,2);
				}
			}

			else if (b == Blocks.red_mushroom_block) {
				if (world.getBlockMetadata(x, y, z) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					world.setBlock(x, y, z, Blocks.air,0,2);
				}
			}

			else if (b.getMaterial() == Material.wood && b.isOpaqueCube() && b != ModBlocks.waste_log) {
				world.setBlock(x, y, z, ModBlocks.waste_planks);
			}

			else if (b == ModBlocks.ore_uranium) {
				rand = random.nextInt(VersatileConfig.getSchrabOreChance());
				if (rand == 1) {
					world.setBlock(x, y, z, ModBlocks.ore_uranium_scorched);
				}
			}

			else if (b == ModBlocks.ore_nether_uranium) {
				rand = random.nextInt(VersatileConfig.getSchrabOreChance());
				if (rand == 1) {
					//world.setBlock(x, y, z, ModBlocks.ore_nether_schrabidium);
					world.setBlock(x, y, z, ModBlocks.ore_nether_uranium_scorched);
				}
			}

			else if (b == ModBlocks.ore_gneiss_uranium) {
				rand = random.nextInt(VersatileConfig.getSchrabOreChance());
				if (rand == 1) {
					world.setBlock(x, y, z, ModBlocks.ore_gneiss_uranium_scorched);
				}
			}

		}
	}

	public static void wasteNoSchrab(World world, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22 + world.rand.nextInt(r22 / 5)) {
						if (world.getBlock(X, Y, Z) != Blocks.air)
							wasteDestNoSchrab(world, X, Y, Z);
					}
				}
			}
		}
	}

	public static void wasteDestNoSchrab(World world, int x, int y, int z) {
		if (!world.isRemote) {
			int rand;

			if (world.getBlock(x, y, z) == Blocks.glass || world.getBlock(x, y, z) == Blocks.stained_glass
					|| world.getBlock(x, y, z) == Blocks.wooden_door || world.getBlock(x, y, z) == Blocks.iron_door
					|| world.getBlock(x, y, z) == Blocks.leaves || world.getBlock(x, y, z) == Blocks.leaves2) {
				world.setBlock(x, y, z, Blocks.air);
			}

			else if (world.getBlock(x, y, z) == Blocks.grass) {
				world.setBlock(x, y, z, ModBlocks.waste_earth);
			}

			else if (world.getBlock(x, y, z) == Blocks.mycelium) {
				world.setBlock(x, y, z, ModBlocks.waste_mycelium);
			}

			else if (world.getBlock(x, y, z) == Blocks.sand) {
				rand = random.nextInt(20);
				if (rand == 1 && world.getBlockMetadata(x, y, z) == 0) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite);
				}
				if (rand == 1 && world.getBlockMetadata(x, y, z) == 1) {
					world.setBlock(x, y, z, ModBlocks.waste_trinitite_red);
				}
			}

			else if (world.getBlock(x, y, z) == Blocks.clay) {
				world.setBlock(x, y, z, Blocks.hardened_clay);
			}

			else if (world.getBlock(x, y, z) == Blocks.mossy_cobblestone) {
				world.setBlock(x, y, z, Blocks.coal_ore);
			}

			else if (world.getBlock(x, y, z) == Blocks.coal_ore) {
				rand = random.nextInt(80);
				if (rand == 1) {
					world.setBlock(x, y, z, Blocks.diamond_ore);
				}
				if (rand == 29) {
					world.setBlock(x, y, z, Blocks.emerald_ore);
				}
			}

			else if (world.getBlock(x, y, z) == Blocks.log || world.getBlock(x, y, z) == Blocks.log2) {
				world.setBlock(x, y, z, ModBlocks.waste_log);
			}

			else if (world.getBlock(x, y, z) == Blocks.planks) {
				world.setBlock(x, y, z, ModBlocks.waste_planks);
			}

			else if (world.getBlock(x, y, z) == Blocks.brown_mushroom_block) {
				if (world.getBlockMetadata(x, y, z) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					world.setBlock(x, y, z, Blocks.air,0,2);
				}
			}

			else if (world.getBlock(x, y, z) == Blocks.red_mushroom_block) {
				if (world.getBlockMetadata(x, y, z) == 10) {
					world.setBlock(x, y, z, ModBlocks.waste_log);
				} else {
					world.setBlock(x, y, z, Blocks.air,0,2);
				}
			}
		}
	}

	public static void emp(World world, int x, int y, int z) {
		if (!world.isRemote) {

			Block b = world.getBlock(x,y,z);
			TileEntity te = world.getTileEntity(x, y, z);

			if (te != null && te instanceof IEnergyHandlerMK2) {
				((IEnergyHandlerMK2)te).setPower(0);
				if(random.nextInt(5) < 1) world.setBlock(x, y, z, ModBlocks.block_electrical_scrap);
			}
			if (te != null && te instanceof IEnergyProvider) {

				((IEnergyProvider)te).extractEnergy(ForgeDirection.UP, ((IEnergyProvider)te).getEnergyStored(ForgeDirection.UP), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.DOWN, ((IEnergyProvider)te).getEnergyStored(ForgeDirection.DOWN), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.NORTH, ((IEnergyProvider)te).getEnergyStored(ForgeDirection.NORTH), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.SOUTH, ((IEnergyProvider)te).getEnergyStored(ForgeDirection.SOUTH), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.EAST, ((IEnergyProvider)te).getEnergyStored(ForgeDirection.EAST), false);
				((IEnergyProvider)te).extractEnergy(ForgeDirection.WEST, ((IEnergyProvider)te).getEnergyStored(ForgeDirection.WEST), false);

				if(random.nextInt(5) <= 1)
					world.setBlock(x, y, z, ModBlocks.block_electrical_scrap);
			}
			if((b == ModBlocks.fusion_conductor || b == ModBlocks.fusion_motor || b == ModBlocks.fusion_heater) && random.nextInt(10) == 0)
				world.setBlock(x, y, z, ModBlocks.block_electrical_scrap);
		}
	}

	public static void solinium(World world, int x, int y, int z) {
		if (!world.isRemote) {
			Block b = world.getBlock(x,y,z);
			Material m = b.getMaterial();

			if(b == Blocks.grass || b == Blocks.mycelium || b == ModBlocks.waste_earth || b == ModBlocks.waste_mycelium) {
				world.setBlock(x, y, z, Blocks.dirt);
				return;
			}

			if(m == Material.cactus || m == Material.coral || m == Material.leaves || m == Material.plants || m == Material.sponge || m == Material.vine || m == Material.gourd || m == Material.wood) {
				world.setBlockToAir(x, y, z);
			}
		}
	}
}
