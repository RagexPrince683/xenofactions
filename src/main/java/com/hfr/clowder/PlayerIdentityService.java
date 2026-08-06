package com.hfr.clowder;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.mojang.authlib.GameProfile;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/** Single authority for faction player identity decisions. Never performs a network profile lookup. */
public final class PlayerIdentityService {
    public enum Mode { AUTO, UUID, NAME }
    private static Mode configuredMode = Mode.UUID;
    private static Mode effectiveMode = Mode.UUID;

    private PlayerIdentityService() { }

    public static void initialize(MinecraftServer server) {
        String configured = XFConfig.playerIdentityMode == null ? "" : XFConfig.playerIdentityMode.trim();
        try { configuredMode = Mode.valueOf(configured.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException invalid) {
            configuredMode = Mode.UUID;
            log("Invalid playerIdentityMode '" + configured + "'; failing closed to UUID.");
        }
        effectiveMode = configuredMode;
        if(configuredMode == Mode.AUTO) {
            boolean deobfuscated = Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment"));
            boolean online = server != null && server.isServerInOnlineMode();
            effectiveMode = deobfuscated || !online ? Mode.NAME : Mode.UUID;
        }
        log("Player identity mode: configured=" + configuredMode + ", effective=" + effectiveMode + ".");
        if(effectiveMode == Mode.NAME)
            log("WARNING: NAME PLAYER IDENTITY IS ENABLED. Usernames authorize faction access and do not protect against username reuse or changes. Use UUID on online production servers.");
    }

    public static Mode getEffectiveMode() { return effectiveMode; }
    public static boolean usesNames() { return effectiveMode == Mode.NAME; }
    public static String normalizeName(String name) { return name == null ? "" : name.trim().toLowerCase(Locale.ROOT); }
    public static String profileName(EntityPlayer player) {
        GameProfile profile = player == null ? null : player.getGameProfile();
        return profile == null ? "" : profile.getName();
    }
    public static UUID uuid(EntityPlayer player) {
        GameProfile profile = player == null ? null : player.getGameProfile();
        return profile == null ? null : profile.getId();
    }
    public static GameProfile cachedProfile(String name) {
        if(normalizeName(name).isEmpty() || MinecraftServer.getServer() == null) return null;
        try {
            Object cache = MinecraftServer.getServer().func_152358_ax();
            Method method = cache.getClass().getMethod("func_152655_a", String.class);
            Object value = method.invoke(cache, name.trim());
            return value instanceof GameProfile ? (GameProfile)value : null;
        } catch(Exception failure) {
            log("Could not read cached profile for '" + name + "': " + failure.getClass().getSimpleName());
            return null;
        }
    }
    private static void log(String message) { if(MainRegistry.logger != null) MainRegistry.logger.warn(message); }
}
