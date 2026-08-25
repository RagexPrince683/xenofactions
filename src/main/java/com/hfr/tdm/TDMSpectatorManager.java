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
    private static final Set<String> observers=new HashSet<String>();
    private static final Map<String,Boolean> oldNoClip=new HashMap<String,Boolean>(),oldInvisible=new HashMap<String,Boolean>(),oldDisableDamage=new HashMap<String,Boolean>();
    private TDMSpectatorManager(){}
    public static boolean isObserving(EntityPlayer p){return observers.contains(key(p));}
    public static void observe(EntityPlayer p){if(!(p instanceof EntityPlayerMP))return;String k=key(p);if(observers.add(k)){oldNoClip.put(k,Boolean.valueOf(p.noClip));oldInvisible.put(k,Boolean.valueOf(p.isInvisible()));oldDisableDamage.put(k,Boolean.valueOf(p.capabilities.disableDamage));}p.noClip=true;p.setInvisible(true);p.capabilities.disableDamage=true;sendTarget((EntityPlayerMP)p);}
    public static void restore(EntityPlayer p){String k=key(p);if(observers.remove(k)){p.noClip=value(oldNoClip.remove(k));p.setInvisible(value(oldInvisible.remove(k)));p.capabilities.disableDamage=value(oldDisableDamage.remove(k));}if(p instanceof EntityPlayerMP)PacketDispatcher.wrapper.sendTo(new TDMSpectatorPacket(-1,""),(EntityPlayerMP)p);}
    public static void restoreAll(){for(EntityPlayerMP p:TDMManager.getOnlinePlayers())restore(p);observers.clear();oldNoClip.clear();oldInvisible.clear();oldDisableDamage.clear();}
    public static void tick(World world){for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(isObserving(p)){p.noClip=true;p.setInvisible(true);p.capabilities.disableDamage=true;if(p.ticksExisted%20==0)sendTarget(p);}}
    private static void sendTarget(EntityPlayerMP observer){TDMManager.Team team=TDMManager.getOrAssignPlayerTeam(observer);for(EntityPlayerMP p:TDMManager.getOnlinePlayers())if(p!=observer&&p.dimension==observer.dimension&&TDMManager.getOrAssignPlayerTeam(p)==team&&!isObserving(p)&&!TDMBombManager.isEliminated(p)){PacketDispatcher.wrapper.sendTo(new TDMSpectatorPacket(p.getEntityId(),p.getCommandSenderName()),observer);return;}PacketDispatcher.wrapper.sendTo(new TDMSpectatorPacket(0,""),observer);}
    private static String key(EntityPlayer p){return p.getCommandSenderName().toLowerCase();}
    private static boolean value(Boolean b){return b!=null&&b.booleanValue();}
}
