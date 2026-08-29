package com.hfr.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.hfr.util.XFLog;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * Runtime-isolated HBM radiation integration. HBM class names are resolved only
 * after Forge reports that the mod is loaded, and failures permanently degrade
 * this bridge to a no-op instead of affecting core safezone processing.
 */
public final class OptionalHbmRadiationCompat {
	private static final String HBM_MOD_ID = "hbm";
	private static final RadiationCompat NOOP = new RadiationCompat() {
		@Override
		public void clearRadiation(EntityPlayer player) { }
	};
	private static volatile RadiationCompat implementation;
	private static volatile boolean initialized;

	private OptionalHbmRadiationCompat() { }

	public static void clearRadiation(EntityPlayer player) {
		getImplementation().clearRadiation(player);
	}

	private static RadiationCompat getImplementation() {
		if(initialized) return implementation;
		return initialize();
	}

	private static synchronized RadiationCompat initialize() {
		if(initialized) return implementation;
		RadiationCompat selected = NOOP;
		if(!Loader.isModLoaded(HBM_MOD_ID)) {
			implementation = selected;
			initialized = true;
			return selected;
		}

		try {
			selected = new ReflectiveHbmRadiationCompat();
			XFLog.info("[XF] Optional HBM radiation safezone integration initialized.");
		} catch(Exception e) {
			warnInitializationFailure(e);
		} catch(LinkageError e) {
			warnInitializationFailure(e);
		}
		implementation = selected;
		initialized = true;
		return selected;
	}

	private static void warnInitializationFailure(Throwable failure) {
		XFLog.warn("[XF] HBM is loaded, but its radiation API is incompatible; safezone radiation clearing is disabled: "
				+ failure.getClass().getSimpleName() + ": " + String.valueOf(failure.getMessage()));
	}

	private static final class ReflectiveHbmRadiationCompat implements RadiationCompat {
		private final Method getRadiation;
		private final Method incrementRadiation;
		private final Potion radaway;
		private final Potion radx;
		private final Potion radiation;
		private boolean disabled;

		private ReflectiveHbmRadiationCompat() throws Exception {
			ClassLoader loader = OptionalHbmRadiationCompat.class.getClassLoader();
			Class<?> livingProps = Class.forName("com.hbm.extprop.HbmLivingProps", true, loader);
			Class<?> hbmPotion = Class.forName("com.hbm.potion.HbmPotion", true, loader);
			getRadiation = livingProps.getMethod("getRadiation", EntityLivingBase.class);
			incrementRadiation = livingProps.getMethod("incrementRadiation", EntityLivingBase.class, Float.TYPE);
			radaway = readPotion(hbmPotion, "radaway");
			radx = readPotion(hbmPotion, "radx");
			radiation = readPotion(hbmPotion, "radiation");
		}

		private static Potion readPotion(Class<?> hbmPotion, String name) throws Exception {
			Field field = hbmPotion.getField(name);
			Object value = field.get(null);
			if(!(value instanceof Potion)) throw new IllegalStateException("HbmPotion." + name + " is unavailable");
			return (Potion) value;
		}

		@Override
		public void clearRadiation(EntityPlayer player) {
			if(disabled) return;
			try {
				player.addPotionEffect(new PotionEffect(radaway.id, 50));
				player.addPotionEffect(new PotionEffect(radx.id, 110));
				Number currentRadiation = (Number) getRadiation.invoke(null, player);
				incrementRadiation.invoke(null, player, -currentRadiation.floatValue());
				player.removePotionEffect(radiation.id);
			} catch(Exception e) {
				disableAfterInvocationFailure(e);
			} catch(LinkageError e) {
				disableAfterInvocationFailure(e);
			}
		}

		private void disableAfterInvocationFailure(Throwable failure) {
			disabled = true;
			XFLog.warn("[XF] HBM radiation safezone integration failed and has been disabled: "
					+ failure.getClass().getSimpleName() + ": " + String.valueOf(failure.getMessage()));
		}
	}
}
