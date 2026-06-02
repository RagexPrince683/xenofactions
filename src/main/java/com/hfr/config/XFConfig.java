package com.hfr.config;

import java.util.HashSet;
import java.util.Set;

import com.hfr.clowder.CityLevel;
import com.hfr.command.CommandClowderAdmin;
import com.hfr.main.MainRegistry;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Central server-owner configuration for Xenofactions gameplay systems.
 *
 * Defaults intentionally mirror the pre-2.1 hardcoded values so existing worlds keep behaving the same
 * until a server owner opts into changes.
 */
public final class XFConfig {

	private XFConfig() { }

	public static boolean enableDynmapIntegration = true;
	public static boolean enableTDM = true;
	public static boolean enableCustomFactionFlags = true;
	public static boolean enableNewPlayerProtection = false;
	public static boolean enableConquestFlagsCommand = true;
	public static boolean warEnabledDefault = false;

	public static float startingPrestige = 250F;
	public static float basePrestigeGen = 25F;
	public static float prestigeGenCap = 2500F;
	public static float warpCost = 125F;
	public static float warpTentUpkeep = 75F;
	public static float medTentUpkeep = 5F;
	public static float blastFurnacePrestige = 5F;
	public static float grainmillPrestige = 3F;
	public static float universityPrestige = 60F;
	public static float federalReservePrestige = 30F;
	public static float templePrestige = 90F;
	public static float statuePrestige = 15F;
	public static float cityCenterPrestige = 0F;
	public static float claimFlagUpkeep = 1F;
	public static float cityClaimUpkeep = 10F;
	public static float warDeclarationBaseCost = 150F;
	public static float warDeclarationTargetPrestigeFactor = 0.15F;
	public static float activeWarUpkeep = 75F;
	public static float warUpkeepHourlyGrowth = 0.25F;
	public static float warUpkeepHourlyGrowthSquared = 0.05F;
	public static float surrenderPrestigeTransferPercent = 0.50F;
	public static long surrenderTributeDurationMs = 84L * 60L * 60L * 1000L;
	public static boolean enableNegativePrestigePenalties = true;
	public static float financialCrisisThreshold = 0F;
	public static float nationalCollapseThreshold = -500F;
	public static float fallenNationThreshold = -1000F;
	public static float financialCrisisUpkeepMult = 1.25F;
	public static float nationalCollapseUpkeepMult = 1.50F;
	public static float fallenNationUpkeepMult = 2.00F;

	public static int maxCityRadius = 6;
	public static int minCitySpacingChunks = 13;
	public static int claimNameMinLength = 1;
	public static int claimNameMaxLength = 32;
	public static boolean claimNameRequireUnique = true;
	public static boolean claimRenameOfficersAllowed = true;
	public static boolean peaceCityTransfersEnabled = true;
	public static boolean surrenderTransfersCities = true;
	public static int[] cityRadii = new int[] { 2, 3, 4, 5, 6 };
	public static float[] cityUpgradeCosts = new float[] { 75F, 150F, 300F, 600F, 1000F };
	public static float[] cityUpkeep = new float[] { 10F, 25F, 50F, 90F, 140F };
	public static float cityFoundingCostGrowth = 0.50F;

	public static int warOnlinePlayerThreshold = 2;
	public static long raidGraceAfterOnlineDropMs = 30L * 60L * 1000L;
	public static long surrenderCooldownMs = 84L * 60L * 60L * 1000L;
	public static long peaceCooldownMs = 84L * 60L * 60L * 1000L;
	public static long ceasefireCooldownMs = 24L * 60L * 60L * 1000L;
	public static long allianceBreakCooldownMs = 24L * 60L * 60L * 1000L;
	public static boolean alliesCanJoinWars = true;
	public static boolean alliesCanDeclareWarOnEachOther = false;

	public static long pvpGraceDurationMs = 4L * 60L * 60L * 1000L;
	public static long keepInventoryDurationMs = 24L * 60L * 60L * 1000L;
	public static boolean graceBuildEnabled = true;
	public static boolean graceBuildOneTimeUse = true;
	public static long graceBuildDurationMs = 48L * 60L * 60L * 1000L;

	public static String[] customFlagAllowedHosts = new String[] { "postimages.org", "i.postimg.cc" };
	public static Set<String> customFlagAllowedHostSet = new HashSet<String>();
	public static int customFlagMaxWidth = 1024;
	public static int customFlagMaxHeight = 1024;
	public static int customFlagMaxFileSizeBytes = 1024 * 1024;
	public static int customFlagTimeoutMs = 5000;
	public static int customFlagMaxRedirects = 3;
	public static long customFlagRateLimitMs = 60000L;
	public static boolean customFlagReloadMissingClearsMetadata = true;

	public static String dynmapMarkerSetId = "xenofactions_cities";
	public static String dynmapMarkerSetLabel = "Faction Cities";
	public static int dynmapUpdateIntervalTicks = 20 * 30;
	public static double dynmapClaimFillOpacity = 0.18D;
	public static double dynmapBorderLineOpacity = 0.9D;
	public static int dynmapBorderLineWeight = 3;
	public static double dynmapClaimLineOpacity = 0.0D;
	public static int dynmapClaimLineWeight = 0;
	public static boolean dynmapShowCityCenterMarkers = true;
	public static boolean dynmapShowClaimDetails = true;
	public static boolean dynmapShowPrestigeDetails = true;

	public static void load(Configuration config) {
		String modules = "XENOFACTIONS_MODULES";
		enableDynmapIntegration = bool(config, modules, "enableDynmapIntegration", enableDynmapIntegration, "Enables optional Dynmap markers. Safe no-op when Dynmap is absent.");
		enableTDM = bool(config, modules, "enableTDM", enableTDM, "Enables Xenofactions TDM commands and event hooks.");
		enableCustomFactionFlags = bool(config, modules, "enableCustomFactionFlags", enableCustomFactionFlags, "Enables imported custom faction flags via /c flag seturl.");
		enableNewPlayerProtection = bool(config, modules, "enableNewPlayerProtection", enableNewPlayerProtection, "Enables starter PvP/keep-inventory protection for first-time players.");
		enableConquestFlagsCommand = bool(config, modules, "enableConquestFlagsCommand", enableConquestFlagsCommand, "Enables the /xflags command that grants conquest flags while wars are enabled.");

		String econ = "XENOFACTIONS_PRESTIGE_ECONOMY";
		startingPrestige = flt(config, econ, "startingPrestige", startingPrestige, 0F, 1000000F, "Prestige granted to newly-created factions.");
		basePrestigeGen = flt(config, econ, "basePrestigeGeneration", basePrestigeGen, 0F, 100000F, "Base hourly prestige generation for factions.");
		prestigeGenCap = flt(config, econ, "prestigeGenerationCap", prestigeGenCap, 0F, 1000000F, "Maximum hourly prestige generation counted for a faction.");
		warpCost = flt(config, econ, "warpCreationCost", warpCost, 0F, 100000F, "Prestige cost to create a faction warp.");
		warpTentUpkeep = flt(config, econ, "warpTentUpkeep", warpTentUpkeep, 0F, 100000F, "Hourly prestige upkeep consumed by warp tents.");
		medTentUpkeep = flt(config, econ, "medicalTentUpkeep", medTentUpkeep, 0F, 100000F, "Hourly prestige upkeep consumed by medical tents.");
		blastFurnacePrestige = flt(config, econ, "blastFurnacePrestigeGeneration", blastFurnacePrestige, 0F, 100000F, "Hourly prestige generated by blast furnaces.");
		grainmillPrestige = flt(config, econ, "grainmillPrestigeGeneration", grainmillPrestige, 0F, 100000F, "Hourly prestige generated by grainmills.");
		universityPrestige = flt(config, econ, "universityPrestigeGeneration", universityPrestige, 0F, 100000F, "Hourly prestige generated by universities.");
		federalReservePrestige = flt(config, econ, "federalReservePrestigeGeneration", federalReservePrestige, 0F, 100000F, "Hourly prestige generated by federal reserves.");
		templePrestige = flt(config, econ, "templePrestigeGeneration", templePrestige, 0F, 100000F, "Hourly prestige generated by temples.");
		statuePrestige = flt(config, econ, "statuePrestigeGeneration", statuePrestige, 0F, 100000F, "Hourly prestige generated by statues.");
		cityCenterPrestige = flt(config, econ, "cityCenterPrestigeGeneration", cityCenterPrestige, 0F, 100000F, "Hourly prestige generated by city centers.");
		claimFlagUpkeep = flt(config, econ, "claimFlagUpkeep", claimFlagUpkeep, 0F, 100000F, "Hourly upkeep per legacy conquest claim flag cost unit.");
		cityClaimUpkeep = flt(config, econ, "settlementCityUpkeep", cityClaimUpkeep, 0F, 100000F, "Hourly upkeep for settlement city centers; per-level values are in the claims category.");
		warDeclarationBaseCost = flt(config, econ, "warDeclarationBaseCost", warDeclarationBaseCost, 0F, 1000000F, "Base prestige cost to declare war.");
		warDeclarationTargetPrestigeFactor = flt(config, econ, "warDeclarationTargetPrestigeFactor", warDeclarationTargetPrestigeFactor, 0F, 10F, "Extra war declaration cost as a fraction of target prestige.");
		activeWarUpkeep = flt(config, econ, "activeWarUpkeep", activeWarUpkeep, 0F, 1000000F, "Starting hourly prestige upkeep per active war.");
		warUpkeepHourlyGrowth = flt(config, econ, "warUpkeepHourlyGrowth", warUpkeepHourlyGrowth, 0F, 100F, "Linear hourly growth multiplier for active war upkeep.");
		warUpkeepHourlyGrowthSquared = flt(config, econ, "warUpkeepHourlyGrowthSquared", warUpkeepHourlyGrowthSquared, 0F, 100F, "Quadratic hourly growth multiplier for active war upkeep.");
		surrenderPrestigeTransferPercent = flt(config, econ, "surrenderPrestigeTransferPercent", surrenderPrestigeTransferPercent, 0F, 1F, "Fraction of surrendered faction generation paid as tribute.");
		surrenderTributeDurationMs = hours(config, econ, "surrenderTributeDurationHours", 84D, 0D, 24D * 365D, "Duration of surrender tribute in hours.");
		enableNegativePrestigePenalties = bool(config, econ, "enableNegativePrestigePenalties", enableNegativePrestigePenalties, "Enables bankruptcy/collapse/fallen-nation penalties.");
		financialCrisisThreshold = flt(config, econ, "financialCrisisThreshold", financialCrisisThreshold, -1000000F, 1000000F, "Prestige threshold for Financial Crisis.");
		nationalCollapseThreshold = flt(config, econ, "nationalCollapseThreshold", nationalCollapseThreshold, -1000000F, 1000000F, "Prestige threshold for National Collapse.");
		fallenNationThreshold = flt(config, econ, "fallenNationThreshold", fallenNationThreshold, -1000000F, 1000000F, "Prestige threshold for Fallen Nation.");
		financialCrisisUpkeepMult = flt(config, econ, "financialCrisisUpkeepMultiplier", financialCrisisUpkeepMult, 0F, 100F, "Upkeep multiplier during Financial Crisis.");
		nationalCollapseUpkeepMult = flt(config, econ, "nationalCollapseUpkeepMultiplier", nationalCollapseUpkeepMult, 0F, 100F, "Upkeep multiplier during National Collapse.");
		fallenNationUpkeepMult = flt(config, econ, "fallenNationUpkeepMultiplier", fallenNationUpkeepMult, 0F, 100F, "Upkeep multiplier during Fallen Nation.");

		String claims = "XENOFACTIONS_CLAIMS_CITIES";
		maxCityRadius = integer(config, claims, "maxCityRadius", maxCityRadius, 1, 32, "Maximum city radius in chunks. Existing claims are not deleted when lowered.");
		minCitySpacingChunks = integer(config, claims, "minimumCitySpacingChunks", minCitySpacingChunks, 0, 128, "Minimum chunk distance between city centers to prevent overlap.");
		claimNameMinLength = integer(config, claims, "claimNameMinLength", claimNameMinLength, 1, 64, "Minimum city/claim name length.");
		claimNameMaxLength = integer(config, claims, "claimNameMaxLength", claimNameMaxLength, claimNameMinLength, 64, "Maximum city/claim name length.");
		claimNameRequireUnique = bool(config, claims, "claimNameRequireUnique", claimNameRequireUnique, "Requires city names to be unique server-wide.");
		claimRenameOfficersAllowed = bool(config, claims, "claimRenameOfficersAllowed", claimRenameOfficersAllowed, "Allows officers to rename city claims; false restricts to leaders/admins.");
		peaceCityTransfersEnabled = bool(config, claims, "peaceCityTransfersEnabled", peaceCityTransfersEnabled, "Allows peace offers to include a city transfer.");
		surrenderTransfersCities = bool(config, claims, "surrenderTransfersCities", surrenderTransfersCities, "Transfers all loser cities to victor when surrender is accepted.");
		cityFoundingCostGrowth = flt(config, claims, "cityFoundingCostGrowth", cityFoundingCostGrowth, 0F, 100F, "Additional settlement founding cost per previously-founded city.");
		cityRadii = intList(config, claims, "cityRadii", cityRadii, 1, maxCityRadius, "City radii by level: settlement,town,city,metropolis,capital.");
		cityUpgradeCosts = floatList(config, claims, "cityUpgradeCosts", cityUpgradeCosts, 0F, 1000000F, "City upgrade/founding prestige costs by level.");
		cityUpkeep = floatList(config, claims, "cityUpkeep", cityUpkeep, 0F, 1000000F, "Hourly city upkeep by level.");

		String war = "XENOFACTIONS_WAR_DIPLOMACY";
		warEnabledDefault = bool(config, war, "warEnabledDefault", warEnabledDefault, "Whether war declarations start enabled after server boot.");
		warOnlinePlayerThreshold = integer(config, war, "onlinePlayerThreshold", warOnlinePlayerThreshold, 0, 100, "Online target faction members required for war/raid eligibility.");
		raidGraceAfterOnlineDropMs = minutes(config, war, "raidGraceAfterOnlineDropMinutes", 30D, 0D, 24D * 60D, "How long a faction remains raidable after dropping below the online threshold.");
		surrenderCooldownMs = hours(config, war, "surrenderCooldownHours", 84D, 0D, 24D * 365D, "No-war cooldown after accepted surrender.");
		peaceCooldownMs = hours(config, war, "peaceCooldownHours", 84D, 0D, 24D * 365D, "No-war cooldown after accepted peace.");
		ceasefireCooldownMs = hours(config, war, "ceasefireCooldownHours", 24D, 0D, 24D * 365D, "No-war cooldown after accepted ceasefire.");
		allianceBreakCooldownMs = hours(config, war, "allianceBreakCooldownHours", 24D, 0D, 24D * 365D, "No-war cooldown after breaking an alliance.");
		alliesCanJoinWars = bool(config, war, "alliesCanJoinWars", alliesCanJoinWars, "Allows /c defendally to join active wars for allies.");
		alliesCanDeclareWarOnEachOther = bool(config, war, "alliesCanDeclareWarOnEachOther", alliesCanDeclareWarOnEachOther, "Allows allied factions to declare war on each other. Default false.");

		String prot = "XENOFACTIONS_NEW_PLAYER_PROTECTION";
		pvpGraceDurationMs = hours(config, prot, "pvpGraceDurationHours", 4D, 0D, 24D * 365D, "PvP grace duration for first-time players.");
		keepInventoryDurationMs = hours(config, prot, "keepInventoryDurationHours", 24D, 0D, 24D * 365D, "Keep-inventory duration recorded for first-time players.");
		graceBuildEnabled = bool(config, prot, "graceBuildEnabled", graceBuildEnabled, "Enables /c gracebuild.");
		graceBuildOneTimeUse = bool(config, prot, "graceBuildOneTimeUse", graceBuildOneTimeUse, "Restricts build grace to one activation per faction.");
		graceBuildDurationMs = hours(config, prot, "graceBuildDurationHours", 48D, 0D, 24D * 365D, "Build grace duration in hours.");

		String flags = "XENOFACTIONS_CUSTOM_FLAGS";
		customFlagAllowedHosts = stringList(config, flags, "allowedImageHosts", customFlagAllowedHosts, "HTTPS image host whitelist. Defaults preserve Postimages/i.postimg.cc behavior.");
		rebuildHostSet();
		customFlagMaxWidth = integer(config, flags, "maxImageWidth", customFlagMaxWidth, 1, 4096, "Maximum imported image width in pixels.");
		customFlagMaxHeight = integer(config, flags, "maxImageHeight", customFlagMaxHeight, 1, 4096, "Maximum imported image height in pixels.");
		customFlagMaxFileSizeBytes = integer(config, flags, "maxFileSizeBytes", customFlagMaxFileSizeBytes, 1024, 8 * 1024 * 1024, "Maximum downloaded image size in bytes.");
		customFlagTimeoutMs = integer(config, flags, "downloadTimeoutMs", customFlagTimeoutMs, 500, 60000, "HTTP connect/read timeout for flag imports.");
		customFlagMaxRedirects = integer(config, flags, "maxRedirects", customFlagMaxRedirects, 0, 10, "Maximum HTTPS redirects while importing a flag.");
		customFlagRateLimitMs = seconds(config, flags, "importRateLimitSeconds", 60D, 0D, 3600D, "Per-player per-faction flag import rate limit.");
		customFlagReloadMissingClearsMetadata = bool(config, flags, "reloadMissingFileClearsMetadata", customFlagReloadMissingClearsMetadata, "When /c flag reload finds no cached file, clear stale metadata.");

		String dynmap = "XENOFACTIONS_DYNMAP";
		dynmapMarkerSetId = string(config, dynmap, "markerSetId", dynmapMarkerSetId, "Dynmap marker set ID.");
		dynmapMarkerSetLabel = string(config, dynmap, "markerSetLabel", dynmapMarkerSetLabel, "Dynmap marker set label.");
		dynmapUpdateIntervalTicks = integer(config, dynmap, "updateIntervalTicks", dynmapUpdateIntervalTicks, 20, 20 * 60 * 60, "Dynmap marker update interval in ticks.");
		dynmapClaimFillOpacity = dbl(config, dynmap, "claimFillOpacity", dynmapClaimFillOpacity, 0D, 1D, "Claim area fill opacity.");
		dynmapClaimLineOpacity = dbl(config, dynmap, "claimLineOpacity", dynmapClaimLineOpacity, 0D, 1D, "Per-claim outline opacity; default hidden.");
		dynmapClaimLineWeight = integer(config, dynmap, "claimLineWeight", dynmapClaimLineWeight, 0, 10, "Per-claim outline weight; default 0.");
		dynmapBorderLineOpacity = dbl(config, dynmap, "borderLineOpacity", dynmapBorderLineOpacity, 0D, 1D, "City border line opacity.");
		dynmapBorderLineWeight = integer(config, dynmap, "borderLineWeight", dynmapBorderLineWeight, 0, 10, "City border line weight.");
		dynmapShowCityCenterMarkers = bool(config, dynmap, "showCityCenterMarkers", dynmapShowCityCenterMarkers, "Shows a marker at each City Center.");
		dynmapShowClaimDetails = bool(config, dynmap, "showClaimDetailsInLabels", dynmapShowClaimDetails, "Includes city/claim chunk details in Dynmap labels.");
		dynmapShowPrestigeDetails = bool(config, dynmap, "showPrestigeDetailsInLabels", dynmapShowPrestigeDetails, "Includes prestige/upkeep details in Dynmap labels.");

		MainRegistry.warpCost = Math.round(warpCost);
		CommandClowderAdmin.WARENABLED = warEnabledDefault;
		com.hfr.clowder.ClowderEvents.newPlayerProtectionEnabled = enableNewPlayerProtection;
	}

	public static int cityRadius(CityLevel level) { return cityRadii[index(level)]; }
	public static float cityUpgradeCost(CityLevel level) { return cityUpgradeCosts[index(level)]; }
	public static float cityUpkeep(CityLevel level) { return cityUpkeep[index(level)]; }
	private static int index(CityLevel level) { int i = level == null ? 0 : level.ordinal(); return Math.max(0, Math.min(4, i)); }

	private static void rebuildHostSet() {
		customFlagAllowedHostSet.clear();
		for(int i = 0; i < customFlagAllowedHosts.length; i++) {
			String h = customFlagAllowedHosts[i];
			if(h != null && h.trim().length() > 0)
				customFlagAllowedHostSet.add(h.trim().toLowerCase());
		}
	}

	private static boolean bool(Configuration c, String cat, String name, boolean def, String comment) { Property p = c.get(cat, name, def); p.comment = comment; return p.getBoolean(def); }
	private static String string(Configuration c, String cat, String name, String def, String comment) { Property p = c.get(cat, name, def); p.comment = comment; String v = p.getString(); return v == null || v.trim().isEmpty() ? def : v.trim(); }
	private static String[] stringList(Configuration c, String cat, String name, String[] def, String comment) { Property p = c.get(cat, name, def); p.comment = comment; return p.getStringList(); }
	private static int integer(Configuration c, String cat, String name, int def, int min, int max, String comment) { Property p = c.get(cat, name, def); p.comment = comment; return clamp(name, p.getInt(def), min, max); }
	private static float flt(Configuration c, String cat, String name, float def, float min, float max, String comment) { Property p = c.get(cat, name, (double)def); p.comment = comment; return (float)clamp(name, p.getDouble(def), min, max); }
	private static double dbl(Configuration c, String cat, String name, double def, double min, double max, String comment) { Property p = c.get(cat, name, def); p.comment = comment; return clamp(name, p.getDouble(def), min, max); }
	private static long hours(Configuration c, String cat, String name, double def, double min, double max, String comment) { return (long)(dbl(c, cat, name, def, min, max, comment) * 60D * 60D * 1000D); }
	private static long minutes(Configuration c, String cat, String name, double def, double min, double max, String comment) { return (long)(dbl(c, cat, name, def, min, max, comment) * 60D * 1000D); }
	private static long seconds(Configuration c, String cat, String name, double def, double min, double max, String comment) { return (long)(dbl(c, cat, name, def, min, max, comment) * 1000D); }
	private static int[] intList(Configuration c, String cat, String name, int[] def, int min, int max, String comment) {
		String[] defaults = new String[def.length]; for(int i = 0; i < def.length; i++) defaults[i] = Integer.toString(def[i]);
		String[] raw = stringList(c, cat, name, defaults, comment); int[] out = new int[def.length];
		for(int i = 0; i < out.length; i++) out[i] = clamp(name + "[" + i + "]", parseInt(raw, i, def[i]), min, max);
		return out;
	}
	private static float[] floatList(Configuration c, String cat, String name, float[] def, float min, float max, String comment) {
		String[] defaults = new String[def.length]; for(int i = 0; i < def.length; i++) defaults[i] = Float.toString(def[i]);
		String[] raw = stringList(c, cat, name, defaults, comment); float[] out = new float[def.length];
		for(int i = 0; i < out.length; i++) out[i] = (float)clamp(name + "[" + i + "]", parseFloat(raw, i, def[i]), min, max);
		return out;
	}
	private static int parseInt(String[] raw, int i, int def) { try { return raw != null && i < raw.length ? Integer.parseInt(raw[i].trim()) : def; } catch(Exception e) { warn("Invalid integer for config list entry; using default " + def); return def; } }
	private static float parseFloat(String[] raw, int i, float def) { try { return raw != null && i < raw.length ? Float.parseFloat(raw[i].trim()) : def; } catch(Exception e) { warn("Invalid decimal for config list entry; using default " + def); return def; } }
	private static int clamp(String name, int value, int min, int max) { if(value < min) { warn(name + " was below " + min + "; corrected to " + min); return min; } if(value > max) { warn(name + " was above " + max + "; corrected to " + max); return max; } return value; }
	private static double clamp(String name, double value, double min, double max) { if(value < min) { warn(name + " was below " + min + "; corrected to " + min); return min; } if(value > max) { warn(name + " was above " + max + "; corrected to " + max); return max; } return value; }
	private static void warn(String message) { if(MainRegistry.logger != null) MainRegistry.logger.warn("Xenofactions config: " + message); }
}
