package com.hfr.tdm;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMSpectatorPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/** Temporary 1.7.10 observer state; RED/BLUE player-name identity remains authoritative. */
public final class TDMSpectatorManager {
    private static final Set<String> observers = new HashSet<String>();
    private static final Map<String, Boolean> oldNoClip = new HashMap<String, Boolean>();
    private static final Map<String, Boolean> oldInvisible = new HashMap<String, Boolean>();
    private static final Map<String, Boolean> oldDisableDamage = new HashMap<String, Boolean>();

    private TDMSpectatorManager() { }

    public static boolean isObserving(EntityPlayer player) {
        return observers.contains(key(player));
    }

    public static void observe(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        String playerKey = key(player);
        if (observers.add(playerKey)) {
            oldNoClip.put(playerKey, Boolean.valueOf(player.noClip));
            oldInvisible.put(playerKey, Boolean.valueOf(player.isInvisible()));
            oldDisableDamage.put(playerKey, Boolean.valueOf(player.capabilities.disableDamage));
        }
        player.noClip = true;
        player.setInvisible(true);
        player.capabilities.disableDamage = true;
        sendTarget((EntityPlayerMP) player);
    }

    public static void restore(EntityPlayer player) {
        String playerKey = key(player);
        if (observers.remove(playerKey)) {
            player.noClip = value(oldNoClip.remove(playerKey));
            player.setInvisible(value(oldInvisible.remove(playerKey)));
            player.capabilities.disableDamage = value(oldDisableDamage.remove(playerKey));
        }
        if (player instanceof EntityPlayerMP) {
            PacketDispatcher.wrapper.sendTo(new TDMSpectatorPacket(-1, ""), (EntityPlayerMP) player);
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
        for (EntityPlayerMP player : TDMManager.getOnlinePlayers()) restore(player);
        observers.clear();
        oldNoClip.clear();
        oldInvisible.clear();
        oldDisableDamage.clear();
    }

    public static void tick(World world) {
        for (EntityPlayerMP player : TDMManager.getOnlinePlayers()) {
            if (isObserving(player)) {
                player.noClip = true;
                player.setInvisible(true);
                player.capabilities.disableDamage = true;
                if (player.ticksExisted % 20 == 0) sendTarget(player);
            }
        }
    }
    private static void sendTarget(EntityPlayerMP observer){TDMManager.Team team=TDMManager.getOrAssignPlayerTeam(observer);for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p!=observer&&p.dimension==observer.dimension&&TDMManager.getOrAssignPlayerTeam(p)==team&&!isObserving(p)&&!TDMBombManager.isEliminated(p)){PacketDispatcher.wrapper.sendTo(new TDMSpectatorPacket(p.getEntityId(),p.getCommandSenderName()),observer);return;}PacketDispatcher.wrapper.sendTo(new TDMSpectatorPacket(0,""),observer);}
    private static String key(EntityPlayer p){return p.getCommandSenderName().toLowerCase();}
    private static boolean value(Boolean b){return b!=null&&b.booleanValue();}
}
