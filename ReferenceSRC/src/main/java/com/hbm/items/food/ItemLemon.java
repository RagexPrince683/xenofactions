package com.hbm.items.food;

import java.util.List;


import com.hbm.entity.effect.EntityVortex;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ModItems;
import com.hbm.packet.PacketDispatcher;
import com.hbm.potion.HbmPotion;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemLemon extends ItemFood {

	public ItemLemon(int p_i45339_1_, float p_i45339_2_, boolean p_i45339_3_) {
		super(p_i45339_1_, p_i45339_2_, p_i45339_3_);

		if(this == ModItems.med_ipecac || this == ModItems.med_ptsd || this == ModItems.galaxygas || this == ModItems.crystal_meth) {
			this.setAlwaysEdible();
		}
		//hey dumbass this thing doesnt work
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		//if(this == ModItems.lemon) {
		//	list.add("Eh, good enough.");
		//}

		if(this == ModItems.med_ipecac) {
			list.add("Bitter juice that will cause your stomach");
			list.add("to forcefully eject its contents.");
		}

		if(this == ModItems.galaxygas) {
			list.add("All the kids are doing it, it must be safe!");
		}

		if(this == ModItems.med_ptsd) {
			list.add("This isn't even PTSD mediaction, it's just");
			list.add("Ipecac in a different bottle!");
		}

		if(this == ModItems.med_schizophrenia) {
			list.add("Makes the voices go away. Just for a while.");
			list.add("");
			list.add("...");
			list.add("Better not take it.");
		}

		if(this == ModItems.med_schizophrenia) {
			list.add("Makes the voices go away. Just for a while.");
			list.add("");
			list.add("...");
			list.add("Better not take it.");
		}

		if(this == ModItems.loops) {
			list.add("A unfunny redditor was here once. Now resides a copyist German.");
		}

		if(this == ModItems.loop_stew) {
			list.add("Gamer girl bath water and corn flakes!");
		}

		if(this == ModItems.twinkie) {
			list.add("Diabetus and corn starch");
		}

		if(this == ModItems.pudding) {
			list.add("A lot like eating cheap lotion.");
			//list.add("What if he did?");
			//list.add("What if he didn't?");
			//list.add("What if the world was made of pudding?");
		}

		if(this == ModItems.ingot_semtex) {
			list.add("Semtex H Plastic Explosive");
			list.add("Performant explosive for many applications.");
			list.add("Edible");
		}

		if(this == ModItems.peas) {
			list.add("skibidi toilet grigma rizz gyatt in ohio fantumtax kai cenat rizzler rizzmas ohioburger");
		}

		if(this == ModItems.quesadilla) {
			list.add("si senor!");
		}
		if(this == ModItems.flesh_burger) {
			list.add("juicy!.");
		}
		if(this == ModItems.flesh) {
			list.add("JE's special island jerky!");
		}
	}


    @Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player)
    {

		if(this == ModItems.flesh || this == ModItems.squirrel) {
			//hunger, side effects of cannibalism... or eating a squirrel
			player.addPotionEffect(new PotionEffect(Potion.hunger.id, 20 * 30, 0));
			if (this == ModItems.squirrel) {
				world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:entity.chipmunkdeath", 1.0F, 1.0F);
			}
		}

		if(this == ModItems.med_ipecac || this == ModItems.med_ptsd) {
			player.addPotionEffect(new PotionEffect(Potion.hunger.id, 50, 49));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway.id, 1, 20));
			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if (props.nitanCount > 0){
				player.removePotionEffect(HbmPotion.nitan.id);
				props.nitanCount = 0;
			}
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("type", "vomit");
			nbt.setInteger("entity", player.getEntityId());
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 25));

			world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:entity.vomit", 1.0F, 1.0F);
		}

		if(this == ModItems.med_schizophrenia) {
		}

		if(this == ModItems.loop_stew) {
			//player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 20 * 20, 1));
			//player.addPotionEffect(new PotionEffect(Potion.resistance.id, 60 * 20, 2));
			//player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 60 * 20, 1));
			//player.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 20 * 20, 2));
		}
		if(this == ModItems.s_cream) {
			//player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 20 * 20, 2));
		}

		if(this == ModItems.chocolate) {
			HbmLivingProps.incrementRadiation(player, 0.2F);
		}

		//if(this == ModItems.crystal_meth) {
		//	player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 60 * 20, 3));
		//	//give off the appearance of being on meth by adjusting the FOV and adding a slight blur effect
//
		//	//also schedule a task to apply slowness after a long time to simulate withdrawal... and if you take it again you lose with drawal. oh yeah and add overdosing lol
//
		//	NBTTagCompound data = player.getEntityData();
//
		//	long time = player.worldObj.getTotalWorldTime();
//
		//	data.setLong("MethLastUse", time);
//
		//	int dose = data.getInteger("MethDose");
		//	data.setInteger("MethDose", dose + 1);
//
		//}
		//we cant use this here

    }

    public ItemStack onEaten(ItemStack stack, World worldObj, EntityPlayer player)
    {
        ItemStack sta = super.onEaten(stack, worldObj, player);

        if(this == ModItems.loop_stew)
        	return new ItemStack(Items.bowl);

        return sta;
    }
}
