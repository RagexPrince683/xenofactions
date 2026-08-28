package com.hfr.tdm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/** Maintains server-side gameplay restrictions for eliminated TDM players. */
public final class TDMSpectatorManager {
    private static final Set<String> observers = new HashSet<String>();
    private static final Map<String, Boolean> oldNoClip = new HashMap<String, Boolean>();
    private static final Map<String, Boolean> oldInvisible = new HashMap<String, Boolean>();
    private static final Map<String, Boolean> oldDisableDamage = new HashMap<String, Boolean>();

    private TDMSpectatorManager() {
    }

    public static boolean isObserving(EntityPlayer player) {
        return observers.contains(key(player));
    }

    public static void observe(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        String playerKey = key(player);
        if (observers.add(playerKey)) {
            oldNoClip.put(playerKey, Boolean.valueOf(player.noClip));
            oldInvisible.put(playerKey, Boolean.valueOf(player.isInvisible()));
            oldDisableDamage.put(playerKey, Boolean.valueOf(player.capabilities.disableDamage));
        }
        player.noClip = true;
        player.setInvisible(true);
        player.capabilities.disableDamage = true;
    }

    public static void restore(EntityPlayer player) {
        String playerKey = key(player);
        if (observers.remove(playerKey)) {
            player.noClip = value(oldNoClip.remove(playerKey));
            player.setInvisible(value(oldInvisible.remove(playerKey)));
            player.capabilities.disableDamage = value(oldDisableDamage.remove(playerKey));
        }
    }

    public static void forget(EntityPlayer player) {
        String playerKey = key(player);
        observers.remove(playerKey);
        oldNoClip.remove(playerKey);
        oldInvisible.remove(playerKey);
        oldDisableDamage.remove(playerKey);
    }

    public static void restoreAll() {
        for (EntityPlayerMP player : TDMManager.getOnlinePlayers()) {
            restore(player);
        }

        observers.clear();
        oldNoClip.clear();
        oldInvisible.clear();
        oldDisableDamage.clear();
    }

    public static void tick() {
        for (EntityPlayerMP player : TDMManager.getOnlinePlayers()) {
            if (isObserving(player)) {
                player.noClip = true;
                player.setInvisible(true);
                player.capabilities.disableDamage = true;
            }
        }
    }

    private static String key(EntityPlayer player) {
        return player.getCommandSenderName().toLowerCase();
    }

    private static boolean value(Boolean savedValue) {
        return savedValue != null && savedValue.booleanValue();
    }
}
