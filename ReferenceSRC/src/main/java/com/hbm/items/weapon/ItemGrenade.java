package com.hbm.items.weapon;

import java.util.List;

import com.hbm.entity.grenade.*;
import com.hbm.items.ModItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemGrenade extends Item {

	public int fuse = 4;

	protected static final float MAX_DRAW_TIME = 20.0F;
	protected static final float MIN_DRAW_POWER = 0.1F;
	protected static final float FULL_DRAW_VELOCITY = 1.25F;
	protected static final float DEFAULT_GRENADE_VELOCITY = 1.5F;

	public ItemGrenade(int fuse) {
		this.maxStackSize = 1;
		this.fuse = fuse;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.bow;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft) {
		int charge = this.getMaxItemUseDuration(stack) - timeLeft;
		float power = charge / MAX_DRAW_TIME;
		power = (power * power + power * 2.0F) / 3.0F;

		if (power < MIN_DRAW_POWER) {
			return;
		}

		power = Math.min(power, 1.0F);

		if (!player.capabilities.isCreativeMode) {
			--stack.stackSize;
		}

		world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		//TODO:
		/*
		 * kill all this stupid bullshit
		 * make a PROPER grenade entity base class
		 * have all the grenade items be an NBT stat in the entity instead of having new entities for every fucking grenade type
		 * register explosion effects with some lambdas to save on LOC
		 * jesus christ why do i keep doing this
		 */
		if (!world.isRemote) {
			if (this == ModItems.grenade_generic) {
				spawnGrenade(world, new EntityGrenadeGeneric(world, player), power);
			}
			if (this == ModItems.grenade_strong) {
				spawnGrenade(world, new EntityGrenadeStrong(world, player), power);
			}
			if (this == ModItems.grenade_frag) {
				EntityGrenadeFrag frag = new EntityGrenadeFrag(world, player);
				frag.shooter = player;
				spawnGrenade(world, frag, power);
			}
			if (this == ModItems.grenade_fire) {
				EntityGrenadeFire fire = new EntityGrenadeFire(world, player);
				fire.shooter = player;
				spawnGrenade(world, fire, power);
			}
			if (this == ModItems.grenade_cluster) {
				spawnGrenade(world, new EntityGrenadeCluster(world, player), power);
			}
			if (this == ModItems.grenade_flare) {
				spawnGrenade(world, new EntityGrenadeFlare(world, player), power);
			}
			if (this == ModItems.grenade_electric) {
				spawnGrenade(world, new EntityGrenadeElectric(world, player), power);
			}
			if (this == ModItems.grenade_poison) {
				spawnGrenade(world, new EntityGrenadePoison(world, player), power);
			}
			if (this == ModItems.grenade_gas) {
				spawnGrenade(world, new EntityGrenadeGas(world, player), power);
			}
			if (this == ModItems.grenade_schrabidium) {
				spawnGrenade(world, new EntityGrenadeSchrabidium(world, player), power);
			}
			if (this == ModItems.grenade_nuke) {
				spawnGrenade(world, new EntityGrenadeNuke(world, player), power);
			}
			if (this == ModItems.grenade_nuclear) {
				spawnGrenade(world, new EntityGrenadeNuclear(world, player), power);
			}
			if (this == ModItems.grenade_pulse) {
				spawnGrenade(world, new EntityGrenadePulse(world, player), power);
			}
			if (this == ModItems.grenade_plasma) {
				spawnGrenade(world, new EntityGrenadePlasma(world, player), power);
			}
			if (this == ModItems.grenade_tau) {
				spawnGrenade(world, new EntityGrenadeTau(world, player), power);
			}
			//if (this == ModItems.grenade_lemon) {
			//	spawnGrenade(world, new EntityGrenadeLemon(world, player), power);
			//}
			if (this == ModItems.grenade_mk2) {
				spawnGrenade(world, new EntityGrenadeMk2(world, player), power);
			}
			if (this == ModItems.grenade_aschrab) {
				spawnGrenade(world, new EntityGrenadeASchrab(world, player), power);
			}
			if (this == ModItems.grenade_zomg) {
				spawnGrenade(world, new EntityGrenadeZOMG(world, player), power);
			}
			if (this == ModItems.grenade_shrapnel) {
				spawnGrenade(world, new EntityGrenadeShrapnel(world, player), power);
			}
			if (this == ModItems.grenade_black_hole) {
				spawnGrenade(world, new EntityGrenadeBlackHole(world, player), power);
			}
			if (this == ModItems.grenade_gascan) {
				spawnGrenade(world, new EntityGrenadeGascan(world, player), power);
			}
			if (this == ModItems.grenade_cloud) {
				spawnGrenade(world, new EntityGrenadeCloud(world, player), power);
			}
			if (this == ModItems.grenade_pink_cloud) {
				spawnGrenade(world, new EntityGrenadePC(world, player), power);
			}
			if (this == ModItems.grenade_smart) {
				spawnGrenade(world, new EntityGrenadeSmart(world, player), power);
			}
			if (this == ModItems.grenade_mirv) {
				spawnGrenade(world, new EntityGrenadeMIRV(world, player), power);
			}
			if (this == ModItems.grenade_breach) {
				spawnGrenade(world, new EntityGrenadeBreach(world, player), power);
			}
			if (this == ModItems.grenade_burst) {
				spawnGrenade(world, new EntityGrenadeBurst(world, player), power);
			}

			if (this == ModItems.grenade_if_generic) {
				spawnGrenade(world, new EntityGrenadeIFGeneric(world, player), power);
			}
			if (this == ModItems.grenade_if_he) {
				spawnGrenade(world, new EntityGrenadeIFHE(world, player), power);
			}
			if (this == ModItems.grenade_if_bouncy) {
				spawnGrenade(world, new EntityGrenadeIFBouncy(world, player), power);
			}
			if (this == ModItems.grenade_if_sticky) {
				spawnGrenade(world, new EntityGrenadeIFSticky(world, player), power);
			}
			if (this == ModItems.grenade_if_impact) {
				spawnGrenade(world, new EntityGrenadeIFImpact(world, player), power);
			}
			if (this == ModItems.grenade_if_incendiary) {
				spawnGrenade(world, new EntityGrenadeIFIncendiary(world, player), power);
			}
			if (this == ModItems.grenade_if_toxic) {
				spawnGrenade(world, new EntityGrenadeIFToxic(world, player), power);
			}
			if (this == ModItems.grenade_if_concussion) {
				spawnGrenade(world, new EntityGrenadeIFConcussion(world, player), power);
			}
			if (this == ModItems.grenade_if_brimstone) {
				spawnGrenade(world, new EntityGrenadeIFBrimstone(world, player), power);
			}
			if (this == ModItems.grenade_if_mystery) {
				spawnGrenade(world, new EntityGrenadeIFMystery(world, player), power);
			}
			if (this == ModItems.grenade_if_spark) {
				spawnGrenade(world, new EntityGrenadeIFSpark(world, player), power);
			}
			if (this == ModItems.grenade_if_hopwire) {
				spawnGrenade(world, new EntityGrenadeIFHopwire(world, player), power);
			}
			if (this == ModItems.grenade_if_null) {
				spawnGrenade(world, new EntityGrenadeIFNull(world, player), power);
			}
			//if (this == ModItems.nuclear_waste_pearl) {
			//	spawnGrenade(world, new EntityWastePearl(world, player), power);
			//}
			if (this == ModItems.stick_dynamite) {
				spawnGrenade(world, new EntityGrenadeDynamite(world, player), power);
			}
		}
	}

	protected void spawnGrenade(World world, Entity grenade, float drawPower) {
		float velocityMultiplier = drawPower * FULL_DRAW_VELOCITY / DEFAULT_GRENADE_VELOCITY;
		grenade.motionX *= velocityMultiplier;
		grenade.motionY *= velocityMultiplier;
		grenade.motionZ *= velocityMultiplier;
		world.spawnEntityInWorld(grenade);
	}

	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {

		if (this == ModItems.grenade_schrabidium || this == ModItems.grenade_aschrab || this == ModItems.grenade_cloud) {
			return EnumRarity.rare;
		}

		if (this == ModItems.grenade_plasma || this == ModItems.grenade_zomg || this == ModItems.grenade_black_hole || this == ModItems.grenade_pink_cloud) {
			return EnumRarity.epic;
		}

		if (this == ModItems.grenade_nuke || this == ModItems.grenade_nuclear || this == ModItems.grenade_tau || this == ModItems.grenade_mk2 || this == ModItems.grenade_pulse || this == ModItems.grenade_gascan) {
			return EnumRarity.uncommon;
		}

		return EnumRarity.common;
	}

	private String translateFuse() {
		if(fuse == -1)
			return "Impact";

		if(fuse == 0)
			return "Instant";

		return fuse + "s";
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add("Fuse: " + translateFuse());

		if (this == ModItems.grenade_smart) {
			list.add("");
			list.add("\"Why did it not blow up????\"");
			list.add(EnumChatFormatting.ITALIC + "If it didn't blow up it means it worked.");
		}

		if (this == ModItems.grenade_if_generic) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"How do you like " + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + "them" + EnumChatFormatting.ITALIC + " apples?\"");
		}
		if (this == ModItems.grenade_if_he) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"You better run, you better take cover!\"");
		}
		if (this == ModItems.grenade_if_bouncy) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"Boing!\"");
		}
		if (this == ModItems.grenade_if_sticky) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"This one is the booger grenade.\"");
		}
		if (this == ModItems.grenade_if_impact) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"Tossable boom.\"");
		}
		if (this == ModItems.grenade_if_incendiary) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"Flaming wheel of destruction!\"");
		}
		if (this == ModItems.grenade_if_toxic) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"TOXIC SHOCK\"");
		}
		if (this == ModItems.grenade_if_concussion) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"Oof ouch owie, my bones!\"");
		}
		if (this == ModItems.grenade_if_brimstone) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"Zoop!\"");
		}
		if (this == ModItems.grenade_if_mystery) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"It's a mystery!\"");
		}
		if (this == ModItems.grenade_if_spark) {
			list.add("");
			//list.add(EnumChatFormatting.ITALIC + "\"31-31-31-31-31-31-31-31-31-31-31-31-31\"");
			list.add(EnumChatFormatting.ITALIC + "\"We can't rewind, we've gone too far.\"");
		}
		if (this == ModItems.grenade_if_hopwire) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "\"All I ever wished for was a bike that didn't fall over.\"");
		}
		if (this == ModItems.grenade_if_null) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + "java.lang.NullPointerException");
		}
	}

	public static int getFuseTicks(Item grenade) {
		return ((ItemGrenade)grenade).fuse * 20;
	}
}
