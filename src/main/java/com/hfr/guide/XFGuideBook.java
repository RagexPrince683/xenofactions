package com.hfr.guide;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.hfr.config.XFConfig;
import com.hfr.lib.RefStrings;
import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.Loader;
import net.minecraft.util.StatCollector;

/**
 * Optional Guide-API integration for the Xenofactions Handbook.
 *
 * This class intentionally references Guide-API classes by name only so the base mod can load
 * when Guide-API is not installed.
 */
public final class XFGuideBook {

	private static final String GUIDE_API_MODID = "guideapi";
	private static final String BOOK_KEY = "guide.xenofactions.book";
	private static final String CATEGORY_KEY = "guide.xenofactions.category.";
	private static final String ENTRY_KEY = "guide.xenofactions.entry.";
	private static final String PAGE_KEY = "guide.xenofactions.page.";

	private static boolean registered = false;
	private static boolean attempted = false;

	private XFGuideBook() { }

	public static void register() {
		attempted = true;

		if(!XFConfig.enableGuideBook) {
			logInfo("Xenofactions Handbook registration disabled in config.");
			return;
		}

		if(!Loader.isModLoaded(GUIDE_API_MODID)) {
			logInfo("Guide-API missing; Xenofactions Handbook will not be registered. Install Guide-API for the in-game handbook.");
			return;
		}

		try {
			Object book = createBook();
			Class guideRegistry = Class.forName("amerifrance.guideapi.api.GuideRegistry");
			Class bookClass = Class.forName("amerifrance.guideapi.api.base.Book");
			Method registerBook = guideRegistry.getMethod("registerBook", bookClass);
			registerBook.invoke(null, book);
			registered = true;
			logInfo("Xenofactions Handbook registered with Guide-API.");
		} catch(Throwable t) {
			registered = false;
			logWarn("Xenofactions Handbook registration failed. Guide-API is installed, but the book could not be created.", t);
		}
	}

	public static boolean isAvailable() {
		return XFConfig.enableGuideBook && registered;
	}

	public static String getFallbackHelp() {
		if(registered)
			return "Open the Xenofactions Handbook for quick faction help.";
		if(!XFConfig.enableGuideBook)
			return "In-game handbook is disabled in the config.";
		if(!Loader.isModLoaded(GUIDE_API_MODID))
			return "Install Guide-API to enable the Xenofactions Handbook item.";
		if(attempted)
			return "Xenofactions Handbook registration failed; check the server log.";
		return "Xenofactions Handbook will load if Guide-API is installed.";
	}

	private static Object createBook() throws Exception {
		Class bookBuilderClass = Class.forName("amerifrance.guideapi.api.util.BookBuilder");
		Object builder = bookBuilderClass.newInstance();

		invokeBuilder(builder, bookBuilderClass, "setCategories", List.class, createCategories());
		invokeBuilder(builder, bookBuilderClass, "setUnlocBookTitle", String.class, BOOK_KEY + ".title");
		invokeBuilder(builder, bookBuilderClass, "setUnlocDisplayName", String.class, BOOK_KEY + ".name");
		invokeBuilder(builder, bookBuilderClass, "setUnlocWelcomeMessage", String.class, BOOK_KEY + ".welcome");
		invokeBuilder(builder, bookBuilderClass, "setAuthor", String.class, "Xenofactions");
		invokeBuilder(builder, bookBuilderClass, "setBookColor", Color.class, new Color(80, 35, 135));
		invokeBuilder(builder, bookBuilderClass, "setSpawnWithBook", Boolean.TYPE, false);
		invokeBuilder(builder, bookBuilderClass, "setIsLostBook", Boolean.TYPE, false);
		invokeBuilder(builder, bookBuilderClass, "setItemTexture", String.class, RefStrings.MODID + ":designator_manual");

		return bookBuilderClass.getMethod("build").invoke(builder);
	}

	private static void invokeBuilder(Object builder, Class builderClass, String methodName, Class argType, Object arg) throws Exception {
		builderClass.getMethod(methodName, argType).invoke(builder, arg);
	}

	private static List createCategories() throws Exception {
		List categories = new ArrayList();
		categories.add(category("getting_started", entry("getting_started", "getting_started"), entry("first_commands", "first_commands")));
		categories.add(category("factions_commands", entry("faction_lifecycle", "faction_lifecycle"), entry("member_commands", "member_commands")));
		categories.add(category("city_claims", entry("city_centers", "city_centers"), entry("claims", "claims", configText("claims_config"))));
		categories.add(category("prestige_upkeep", entry("prestige", "prestige", configText("prestige_config")), entry("upkeep", "upkeep", configText("upkeep_config"))));
		categories.add(category("grace", entry("new_player_grace", "new_player_grace", configText("new_player_grace_config")), entry("build_grace", "build_grace", configText("build_grace_config"))));
		categories.add(category("diplomacy", entry("allies", "allies"), entry("diplomacy_commands", "diplomacy_commands", configText("diplomacy_config"))));
		categories.add(category("war", entry("war_raiding", "war_raiding", configText("war_config")), entry("peace", "peace", configText("peace_config"))));
		categories.add(category("admin", entry("admin_notes", "admin_notes"), entry("server_owner", "server_owner")));
		categories.add(category("faq", entry("troubleshooting", "troubleshooting"), entry("quick_reference", "quick_reference")));
		return categories;
	}

	private static Object category(String key, Object... entries) throws Exception {
		List entryList = new ArrayList();
		for(int i = 0; i < entries.length; i++)
			entryList.add(entries[i]);

		Class categoryClass = Class.forName("amerifrance.guideapi.api.base.CategoryBase");
		Constructor constructor = categoryClass.getConstructor(List.class, String.class);
		return constructor.newInstance(entryList, CATEGORY_KEY + key);
	}

	private static Object entry(String entryKey, String pageKey) throws Exception {
		return entry(entryKey, pageKey, null);
	}

	private static Object entry(String entryKey, String pageKey, String extraPageText) throws Exception {
		List pages = new ArrayList();
		pages.add(pageUnloc(pageKey));
		if(extraPageText != null && extraPageText.length() > 0)
			pages.add(pageLoc(extraPageText));

		Class entryClass = Class.forName("amerifrance.guideapi.api.base.EntryBase");
		Constructor constructor = entryClass.getConstructor(List.class, String.class);
		return constructor.newInstance(pages, ENTRY_KEY + entryKey);
	}

	private static Object pageUnloc(String key) throws Exception {
		Class pageClass = Class.forName("amerifrance.guideapi.pages.PageUnlocText");
		Constructor constructor = pageClass.getConstructor(String.class, Integer.TYPE);
		return constructor.newInstance(PAGE_KEY + key, 10);
	}

	private static Object pageLoc(String text) throws Exception {
		Class pageClass = Class.forName("amerifrance.guideapi.pages.PageLocText");
		Constructor constructor = pageClass.getConstructor(String.class, Integer.TYPE);
		return constructor.newInstance(text, 10);
	}

	private static String configText(String key) {
		String template = StatCollector.translateToLocal(PAGE_KEY + key);
		if((PAGE_KEY + key).equals(template))
			return fallbackConfigText(key);

		if("claims_config".equals(key))
			return String.format(template, XFConfig.maxCityRadius, XFConfig.minCitySpacingChunks, XFConfig.cityRadii[0], XFConfig.cityRadii[XFConfig.cityRadii.length - 1]);
		if("prestige_config".equals(key))
			return String.format(template, format(XFConfig.startingPrestige), format(XFConfig.basePrestigeGen), format(XFConfig.prestigeGenCap));
		if("upkeep_config".equals(key))
			return String.format(template, format(XFConfig.warpTentUpkeep), format(XFConfig.medTentUpkeep), format(XFConfig.cityClaimUpkeep), format(XFConfig.activeWarUpkeep));
		if("new_player_grace_config".equals(key))
			return String.format(template, hours(XFConfig.pvpGraceDurationMs), hours(XFConfig.keepInventoryDurationMs));
		if("build_grace_config".equals(key))
			return String.format(template, hours(XFConfig.graceBuildDurationMs), XFConfig.graceBuildOneTimeUse ? "one use per faction" : "repeatable");
		if("diplomacy_config".equals(key))
			return String.format(template, hours(XFConfig.allianceBreakCooldownMs), XFConfig.alliesCanJoinWars ? "can" : "cannot");
		if("war_config".equals(key))
			return String.format(template, XFConfig.warOnlinePlayerThreshold, minutes(XFConfig.raidGraceAfterOnlineDropMs), format(XFConfig.warDeclarationBaseCost), format(XFConfig.warDeclarationTargetPrestigeFactor * 100F));
		if("peace_config".equals(key))
			return String.format(template, hours(XFConfig.surrenderCooldownMs), hours(XFConfig.peaceCooldownMs), hours(XFConfig.ceasefireCooldownMs), format(XFConfig.surrenderPrestigeTransferPercent * 100F), hours(XFConfig.surrenderTributeDurationMs));
		return template;
	}

	private static String fallbackConfigText(String key) {
		if("claims_config".equals(key))
			return "Configured city claim radius cap: " + XFConfig.maxCityRadius + " chunks. Minimum spacing: " + XFConfig.minCitySpacingChunks + " chunks.";
		if("prestige_config".equals(key))
			return "Configured starting prestige: " + format(XFConfig.startingPrestige) + ". Base hourly generation: " + format(XFConfig.basePrestigeGen) + ".";
		if("upkeep_config".equals(key))
			return "Configured upkeep includes warp tents at " + format(XFConfig.warpTentUpkeep) + " and active wars at " + format(XFConfig.activeWarUpkeep) + " prestige per hour.";
		if("new_player_grace_config".equals(key))
			return "Configured starter grace: " + hours(XFConfig.pvpGraceDurationMs) + "h PvP and " + hours(XFConfig.keepInventoryDurationMs) + "h keep-inventory.";
		if("build_grace_config".equals(key))
			return "Configured build grace lasts " + hours(XFConfig.graceBuildDurationMs) + "h.";
		if("diplomacy_config".equals(key))
			return "Configured alliance break cooldown: " + hours(XFConfig.allianceBreakCooldownMs) + "h.";
		if("war_config".equals(key))
			return "Configured war online threshold: " + XFConfig.warOnlinePlayerThreshold + " players. Raid grace after online drop: " + minutes(XFConfig.raidGraceAfterOnlineDropMs) + " minutes.";
		if("peace_config".equals(key))
			return "Configured surrender/peace cooldowns: " + hours(XFConfig.surrenderCooldownMs) + "h / " + hours(XFConfig.peaceCooldownMs) + "h.";
		return "See the Xenofactions config for current server values.";
	}

	private static String format(float value) {
		if(value == (long)value)
			return Long.toString((long)value);
		return Float.toString(value);
	}

	private static long hours(long ms) {
		return ms / (60L * 60L * 1000L);
	}

	private static long minutes(long ms) {
		return ms / (60L * 1000L);
	}

	private static void logInfo(String message) {
		if(MainRegistry.logger != null)
			MainRegistry.logger.info(message);
		else
			System.out.println("[XF] " + message);
	}

	private static void logWarn(String message, Throwable t) {
		if(MainRegistry.logger != null)
			MainRegistry.logger.warn(message, t);
		else {
			System.out.println("[XF] " + message);
			t.printStackTrace();
		}
	}
}
