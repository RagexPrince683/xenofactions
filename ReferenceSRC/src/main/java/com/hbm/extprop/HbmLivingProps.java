package com.hbm.extprop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hbm.config.RadiationConfig;
import com.hbm.dim.trait.CBT_Atmosphere;
//import com.hbm.entity.mob.EntityDuck;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.food.ItemConserve;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

public class HbmLivingProps implements IExtendedEntityProperties {

	public static final String key = "NTM_EXT_LIVING";
	public static final UUID digamma_UUID = UUID.fromString("2a3d8aec-5ab9-4218-9b8b-ca812bdf378b");
	public EntityLivingBase entity;

	/// VALS ///
	private float radiation;
	private float digamma;
	private int asbestos;
	public static final int maxAsbestos = 60 * 60 * 20;
	private int blacklung;
	public static final int maxBlacklung = 2 * 60 * 60 * 20;
	private float radEnv;
	private float radBuf;
	private int bombTimer;
	private int contagion;
	private int oil;
	private float activation;
	private int temperature;
	private int oxygen = 100;
	private boolean frozen = false;
	private boolean burning = false;
	public int fire;
	public int phosphorus;
	public int balefire;
	public int radDeathTimer = 0;

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("radDeathTimer", radDeathTimer);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		radDeathTimer = nbt.getInteger("radDeathTimer");
	}

	public static HbmLivingProps get(EntityLivingBase entity) {
		return (HbmLivingProps) entity.getExtendedProperties("HbmProps");
	}


	private List<ContaminationEffect> contamination = new ArrayList();
	private CBT_Atmosphere atmosphere;
	private boolean gravity = false;

	public HbmLivingProps(EntityLivingBase entity) {
		this.entity = entity;
	}

	/// DATA ///
	public static HbmLivingProps registerData(EntityLivingBase entity) {

		entity.registerExtendedProperties(key, new HbmLivingProps(entity));
		return (HbmLivingProps) entity.getExtendedProperties(key);
	}

	public static HbmLivingProps getData(EntityLivingBase entity) {

		HbmLivingProps props = (HbmLivingProps) entity.getExtendedProperties(key);
		return props != null ? props : registerData(entity);
	}

	/// RADIATION ///
	private static boolean isCreativePlayer(EntityLivingBase entity) {
		return entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode;
	}

	/**
	 * Removes every persistent radiation burden carried by an entity. Creative players
	 * are cleared every tick so exposure cannot be hidden and restored by a game-mode
	 * change.
	 */
	public static void clearRadiation(EntityLivingBase entity) {
		HbmLivingProps data = getData(entity);
		data.radiation = 0;
		data.activation = 0;
		data.radEnv = 0;
		data.radBuf = 0;
		data.contamination.clear();
		data.radDeathTimer = 0;
	}

	public static float getRadiation(EntityLivingBase entity) {
		if(!RadiationConfig.enableContamination)
			return 0;

		if(isCreativePlayer(entity)) {
			clearRadiation(entity);
			return 0;
		}

		return getData(entity).radiation;
	}

	public static void setRadiation(EntityLivingBase entity, float rad) {
		if(isCreativePlayer(entity)) {
			getData(entity).radiation = 0;
		} else if(RadiationConfig.enableContamination) {
			getData(entity).radiation = rad;
		}
	}

	public static void incrementRadiation(EntityLivingBase entity, float rad) {
		if(!RadiationConfig.enableContamination)
			return;
		if(isCreativePlayer(entity))
			return;

		if (entity.getCreatureAttribute()==EnumCreatureAttribute.UNDEAD)
		{
			rad*=10;
		}

		HbmLivingProps data = getData(entity);
		float radiation = getData(entity).radiation + rad;

		if(radiation > 8000)
			radiation = 8000;
		if(radiation < 0)
			radiation = 0;

		data.setRadiation(entity, radiation);
	}

	/// NEUTRON ACTIVATION ///
	public static float getNeutronActivation(EntityLivingBase entity) {
		if(RadiationConfig.disableNeutron)
			return 0;
		if(isCreativePlayer(entity)) {
			getData(entity).activation = 0;
			return 0;
		}

		return getData(entity).activation;
	}

	public static void setNeutronActivation(EntityLivingBase entity, float rad) {
		if(isCreativePlayer(entity)) {
			getData(entity).activation = 0;
		} else if(!RadiationConfig.disableNeutron) {
			getData(entity).activation = rad;
		}
	}

	public static void incrementNeutronActivation(EntityLivingBase entity, float rad) {
		if(RadiationConfig.disableNeutron || isCreativePlayer(entity))
			return;

		HbmLivingProps data = getData(entity);
		float neutrons = getData(entity).activation + rad;

		if(neutrons < 0)
			neutrons = 0;

		data.setNeutronActivation(entity, neutrons);
	}

	/// RAD ENV ///
	public static float getRadEnv(EntityLivingBase entity) {
		return getData(entity).radEnv;
	}

	public static void setRadEnv(EntityLivingBase entity, float rad) {
		getData(entity).radEnv = rad;
	}

	/// RAD BUF ///
	public static float getRadBuf(EntityLivingBase entity) {
		return getData(entity).radBuf;
	}

	public static void setRadBuf(EntityLivingBase entity, float rad) {
		getData(entity).radBuf = rad;
	}

	/// CONTAMINATION ///
	public static List<ContaminationEffect> getCont(EntityLivingBase entity) {
		return getData(entity).contamination;
	}

	public static float getDoseRate(EntityLivingBase entity) {
		if(isCreativePlayer(entity))
			return 0;

		HbmLivingProps data = getData(entity);

		float env = data.radEnv;

		float cont = 0F;
		for(ContaminationEffect c : data.contamination) {
			cont += c.getRad();
		}

		float activation = data.activation * 0.001F; // scale down to mSv/s equivalent

		return env + cont + activation;
	}

	public static void addCont(EntityLivingBase entity, ContaminationEffect cont) {
		if(!isCreativePlayer(entity))
			getData(entity).contamination.add(cont);
	}

	/// DIGAMA ///
	//public static float getDigamma(EntityLivingBase entity) {
	//	return getData(entity).digamma;
	//}

	//public static void setDigamma(EntityLivingBase entity, float digamma) {
	//
	//	if(entity.worldObj.isRemote)
	//		return;
	//
	//	//if(entity instanceof EntityDuck)
	//	//	digamma = 0.0F;
	//
	//	getData(entity).digamma = digamma;
	//
	//	float healthMod = (float)Math.pow(0.5, digamma) - 1F;
	//
	//	IAttributeInstance attributeinstance = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth);
	//
	//	try {
	//		attributeinstance.removeModifier(attributeinstance.getModifier(digamma_UUID));
	//	} catch(Exception ex) { }
	//
	//	attributeinstance.applyModifier(new AttributeModifier(digamma_UUID, "digamma", healthMod, 2));
	//
	//	if(entity.getHealth() > entity.getMaxHealth() && entity.getMaxHealth() > 0) {
	//		entity.setHealth(entity.getMaxHealth());
	//	}
	//
	//	if((entity.getMaxHealth() <= 0 || digamma >= 10.0F) && entity.isEntityAlive()) {
	//		entity.setAbsorptionAmount(0);
	//		entity.attackEntityFrom(ModDamageSource.digamma, 500F);
	//		entity.setHealth(0);
	//		entity.onDeath(ModDamageSource.digamma);
	//
	//		NBTTagCompound data = new NBTTagCompound();
	//		data.setString("type", "sweat");
	//		data.setInteger("count", 50);
	//		data.setInteger("block", Block.getIdFromBlock(Blocks.soul_sand));
	//		data.setInteger("entity", entity.getEntityId());
	//		PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 50));
	//	}
	//
	//	if(entity instanceof EntityPlayer) {
	//
	//		float di = getData(entity).digamma;
//
	//		if(di > 0F)
	//			((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaSee);
	//		if(di >= 2F)
	//			((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaFeel);
	//		if(di >= 10F)
	//			((EntityPlayer) entity).triggerAchievement(MainRegistry.digammaKnow);
	//	}
	//}

	//public static void incrementDigamma(EntityLivingBase entity, float digamma) {
	//
	//	//if(entity instanceof EntityDuck)
	//	//	digamma = 0.0F;
	//
	//	HbmLivingProps data = getData(entity);
	//	float dRad = getDigamma(entity) + digamma;
	//
	//	if(dRad > 10)
	//		dRad = 10;
	//	if(dRad < 0)
	//		dRad = 0;
	//
	//	//data.setDigamma(entity, dRad);
	//}


	/// ASBESTOS ///
	public static int getAsbestos(EntityLivingBase entity) {
		if (RadiationConfig.disableAsbestos) return 0;
		return entity.getEntityData().getInteger("Asbestos");
	}

	public static void setAsbestos(EntityLivingBase entity, int asbestos) {
		if (RadiationConfig.disableAsbestos) return;

		NBTTagCompound tag = entity.getEntityData();
		asbestos = Math.min(asbestos, maxAsbestos);

		tag.setInteger("Asbestos", asbestos);
	}

	public static void updateAsbestos(EntityLivingBase entity) {
		if (RadiationConfig.disableAsbestos) return;
		if (entity.worldObj.isRemote) return;

		int level = getAsbestos(entity);
		if (level <= 0) return;

		// Tier 1
		if (level > maxAsbestos * 0.25 && entity.ticksExisted % 200 == 0) {
			entity.attackEntityFrom(ModDamageSource.asbestos, 1.0F);
		}

		// Tier 2
		if (level > maxAsbestos * 0.5 && entity.ticksExisted % 100 == 0) {
			entity.attackEntityFrom(ModDamageSource.asbestos, 2.0F);
		}

		// Tier 3
		if (level > maxAsbestos * 0.75 && entity.ticksExisted % 60 == 0) {
			entity.attackEntityFrom(ModDamageSource.asbestos, 3.0F);
		}

		// Extreme spike
		if (level >= maxAsbestos && entity.ticksExisted % 40 == 0) {
			if (entity.getRNG().nextFloat() < 0.01F) {
				entity.attackEntityFrom(ModDamageSource.asbestos, 20.0F);
			}
		}

		// --- DECAY ---
		if (entity.ticksExisted % 600 == 0) { // every 30 seconds
			setAsbestos(entity, level - 1);
		}
	}

	public static void incrementAsbestos(EntityLivingBase entity, int asbestos) {
		if (RadiationConfig.disableAsbestos) return;

		int newLevel = getAsbestos(entity) + asbestos;
		setAsbestos(entity, newLevel);

		if(entity instanceof EntityPlayerMP && asbestos > 0) {
			PacketDispatcher.wrapper.sendTo(
				new PlayerInformPacket(
					ChatBuilder.start("")
						.nextTranslation("info.asbestos")
						.color(EnumChatFormatting.RED)
						.flush(),
					MainRegistry.proxy.ID_GAS_HAZARD,
					3000
				),
				(EntityPlayerMP) entity
			);
		}
	}

	//ATMOSPHERE//
	public static int getOxy(EntityLivingBase entity) {
		return getData(entity).oxygen;
	}



	public static void setOxy(EntityLivingBase entity, int oxygen) {
		if(oxygen <= 0) {
			oxygen = 0;

			// Only damage every 4 ticks, giving the player more time to react
			if(entity.ticksExisted % 4 == 0) {
				entity.attackEntityFrom(ModDamageSource.oxyprime, 1);
			}
		}

		getData(entity).oxygen = oxygen;
	}

	/// BLACK LUNG DISEASE ///
	public static int getBlackLung(EntityLivingBase entity) {
		if(RadiationConfig.disableCoal) return 0;
		return getData(entity).blacklung;
	}

	public static void setBlackLung(EntityLivingBase entity, int blacklung) {
		if(RadiationConfig.disableCoal) return;
		getData(entity).blacklung = blacklung;

		if(blacklung >= maxBlacklung) {
			getData(entity).blacklung = 0;
			entity.attackEntityFrom(ModDamageSource.blacklung, 1000);
		}
	}

	public static void incrementBlackLung(EntityLivingBase entity, int blacklung) {
		if(RadiationConfig.disableCoal) return;
		setBlackLung(entity, getBlackLung(entity) + blacklung);

		if(entity instanceof EntityPlayerMP) {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("").nextTranslation("info.coaldust").color(EnumChatFormatting.RED).flush(), MainRegistry.proxy.ID_GAS_HAZARD, 3000), (EntityPlayerMP) entity);
		}
	}

	/// TIME BOMB ///
	public static int getTimer(EntityLivingBase entity) {
		return getData(entity).bombTimer;
	}

	public static void setTimer(EntityLivingBase entity, int bombTimer) {
		getData(entity).bombTimer = bombTimer;
	}

	/// CONTAGION ///
	public static int getContagion(EntityLivingBase entity) {
		return getData(entity).contagion;
	}

	public static void setContagion(EntityLivingBase entity, int contageon) {
		getData(entity).contagion = contageon;
	}

	/// OIL ///
	public static int getOil(EntityLivingBase entity) {
		return getData(entity).oil;
	}

	public static void setOil(EntityLivingBase entity, int oil) {
		getData(entity).oil = oil;
	}

	/// TEMPERATURE ///
	public static int getTemperature(EntityLivingBase entity) {
		return getData(entity).temperature;
	}

	public static void setTemperature(EntityLivingBase entity, int temperature) {
		HbmLivingProps data = getData(entity);
		temperature = MathHelper.clamp_int(temperature, -2500, 2500);
		data.temperature = temperature;
		if(temperature > 1000)  data.burning = true;
		if(temperature < 800)  data.burning = false;
		if(temperature < -1000)  data.frozen = true;
		if(temperature > -800)  data.frozen = false;
	}

	public static boolean isFrozen(EntityLivingBase entity) { return getData(entity).frozen; };
	public static boolean isBurning(EntityLivingBase entity) { return getData(entity).burning; };

	/// ATMOSPHERE ///
	public static CBT_Atmosphere getAtmosphere(EntityLivingBase entity) {
		return getData(entity).atmosphere;
	}

//todo gravity fall damage
	public static void setAtmosphere(EntityLivingBase entity, CBT_Atmosphere atmosphere) {
		HbmLivingProps data = getData(entity);
		data.atmosphere = atmosphere;
		data.gravity = atmosphere != null;
	}


	// and gravity (attached to atmospheres, for now)
	public static boolean hasGravity(EntityLivingBase entity) {
		return getData(entity).gravity;
	}

	@Override
	public void init(Entity entity, World world) { }

	@Override
	public void saveNBTData(NBTTagCompound nbt) {

		NBTTagCompound props = new NBTTagCompound();

		props.setFloat("hfr_radiation", radiation);
		props.setFloat("hfr_digamma", digamma);
		props.setInteger("hfr_asbestos", asbestos);
		props.setInteger("hfr_bomb", bombTimer);
		props.setInteger("hfr_contagion", contagion);
		props.setInteger("hfr_blacklung", blacklung);
		props.setInteger("hfr_oil", oil);
		props.setInteger("hfr_oxygen", oxygen);
		props.setFloat("hfr_activation", activation);
		props.setBoolean("hfr_gravity", gravity);
		props.setInteger("hfr_fire", fire);
		props.setInteger("hfr_phosphorus", phosphorus);
		props.setInteger("hfr_balefire", balefire);

		props.setInteger("hfr_cont_count", this.contamination.size());

		for(int i = 0; i < this.contamination.size(); i++) {
			this.contamination.get(i).save(props, i);
		}

		nbt.setTag("HbmLivingProps", props);
	}

	@Override
	public void loadNBTData(NBTTagCompound nbt) {

		NBTTagCompound props = (NBTTagCompound) nbt.getTag("HbmLivingProps");

		if(props != null) {
			radiation = props.getFloat("hfr_radiation");
			digamma = props.getFloat("hfr_digamma");
			asbestos = props.getInteger("hfr_asbestos");
			bombTimer = props.getInteger("hfr_bomb");
			contagion = props.getInteger("hfr_contagion");
			blacklung = props.getInteger("hfr_blacklung");
			oil = props.getInteger("hfr_oil");
			activation = props.getFloat("hfr_activation");
			oxygen = props.getInteger("hfr_oxygen");
			gravity = props.getBoolean("hfr_gravity");
			fire = props.getInteger("hfr_fire");
			phosphorus = props.getInteger("hfr_phosphorus");
			balefire = props.getInteger("hfr_balefire");

			int cont = props.getInteger("hfr_cont_count");

			for(int i = 0; i < cont; i++) {
				this.contamination.add(ContaminationEffect.load(props, i));
			}
		}
	}

	public static class ContaminationEffect {

		public float maxRad;
		public int maxTime;
		public int time;
		public boolean ignoreArmor;

		public ContaminationEffect(float rad, int time, boolean ignoreArmor) {
			this.maxRad = rad;
			this.maxTime = this.time = time;
			this.ignoreArmor = ignoreArmor;
		}

		public float getRad() {
			return maxRad * ((float)time / (float)maxTime);
		}

		public void save(NBTTagCompound nbt, int index) {
			NBTTagCompound me = new NBTTagCompound();
			me.setFloat("maxRad", this.maxRad);
			me.setInteger("maxTime", this.maxTime);
			me.setInteger("time", this.time);
			me.setBoolean("ignoreArmor", ignoreArmor);
			nbt.setTag("cont_" + index, me);
		}

		public static ContaminationEffect load(NBTTagCompound nbt, int index) {
			NBTTagCompound me = (NBTTagCompound) nbt.getTag("cont_" + index);
			float maxRad = me.getFloat("maxRad");
			int maxTime = nbt.getInteger("maxTime");
			int time = nbt.getInteger("time");
			boolean ignoreArmor = nbt.getBoolean("ignoreArmor");

			ContaminationEffect effect = new ContaminationEffect(maxRad, maxTime, ignoreArmor);
			effect.time = time;
			return effect;
		}
	}
}
