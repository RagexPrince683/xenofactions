package com.hfr.guide;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.hfr.config.XFConfig;
import com.hfr.items.ModItems;
import com.hfr.lib.RefStrings;
import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public final class XFGuideIntegration {

	private static final String GUIDE_API_MODID = "guideapi";
	private static final String BOOK_KEY_PREFIX = "guide.xenofactions.";
	private static boolean registrationAttempted = false;
	private static boolean registered = false;
	private static ItemStack handbookStack;

	private XFGuideIntegration() { }

	public static void registerGuideBook() {
		if(registrationAttempted)
			return;

		registrationAttempted = true;

		if(!XFConfig.enableGuideBook) {
			MainRegistry.logger.info("Xenofactions handbook registration disabled by config.");
			return;
		}

		if(!Loader.isModLoaded(GUIDE_API_MODID)) {
			MainRegistry.logger.info("Guide-API missing; Xenofactions handbook will not be registered. Install Guide-API for the in-game handbook.");
			return;
		}

		try {
			Reflection r = new Reflection();
			Object book = r.newBook(buildCategories(r), key("title"), key("welcome"), key("item.name"), RefStrings.NAME, new Color(0x32, 0x6A, 0xA8));
			r.registerBook(book);
			handbookStack = r.getItemStackForBook(book);
			if(handbookStack != null) {
				GameRegistry.addShapelessRecipe(handbookStack.copy(), Items.book, Items.paper, ModItems.province_point);
			}
			registered = true;
			MainRegistry.logger.info("Registered Xenofactions handbook with Guide-API.");
		} catch(Throwable t) {
			registered = false;
			handbookStack = null;
			MainRegistry.logger.error("Failed to register Xenofactions handbook with Guide-API.", t);
		}
	}

	public static boolean isHandbookAvailable() {
		return registered && handbookStack != null;
	}

	public static ItemStack getHandbookStack() {
		return handbookStack == null ? null : handbookStack.copy();
	}

	public static String getFallbackHelpMessage() {
		if(!XFConfig.enableGuideBook)
			return tr("guide.xenofactions.fallback.disabled");
		if(!Loader.isModLoaded(GUIDE_API_MODID))
			return tr("guide.xenofactions.fallback.missing");
		if(!isHandbookAvailable())
			return tr("guide.xenofactions.fallback.failed");
		return tr("guide.xenofactions.fallback.available");
	}

	private static List buildCategories(Reflection r) throws Exception {
		List categories = new ArrayList();
		categories.add(category(r, "getting_started", new ItemStack(Items.compass), entry(r, "first_steps", new ItemStack(Items.wooden_axe),
				page(r, "getting_started.first_steps.1", fmt(XFConfig.startingPrestige), fmt(XFConfig.cityUpgradeCosts, 0)),
				page(r, "getting_started.first_steps.2"))));
		categories.add(category(r, "factions_commands", new ItemStack(Items.paper), entry(r, "daily_commands", new ItemStack(Items.paper),
				page(r, "factions_commands.daily.1"),
				page(r, "factions_commands.daily.2", fmt(MainRegistry.warpCost)))));
		categories.add(category(r, "city_claims", new ItemStack(Blocks.beacon), entry(r, "claiming", new ItemStack(ModItems.province_point),
				page(r, "city_claims.claiming.1", Integer.toString(XFConfig.cityRadii[0]), Integer.toString(XFConfig.maxCityRadius), Integer.toString(XFConfig.minCitySpacingChunks)),
				page(r, "city_claims.claiming.2", fmt(XFConfig.cityUpkeep, 0)))));
		categories.add(category(r, "prestige_upkeep", new ItemStack(ModItems.cog), entry(r, "prestige", new ItemStack(ModItems.coin),
				page(r, "prestige_upkeep.prestige.1", fmt(XFConfig.basePrestigeGen), fmt(XFConfig.prestigeGenCap)),
				page(r, "prestige_upkeep.prestige.2", fmt(XFConfig.financialCrisisThreshold), fmt(XFConfig.nationalCollapseThreshold), fmt(XFConfig.fallenNationThreshold)))));
		categories.add(category(r, "grace", new ItemStack(Items.clock), entry(r, "protection", new ItemStack(Items.clock),
				page(r, "grace.protection.1", hours(XFConfig.pvpGraceDurationMs), hours(XFConfig.keepInventoryDurationMs)),
				page(r, "grace.protection.2", hours(XFConfig.graceBuildDurationMs)))));
		categories.add(category(r, "diplomacy", new ItemStack(Items.name_tag), entry(r, "allies", new ItemStack(Items.name_tag),
				page(r, "diplomacy.allies.1"),
				page(r, "diplomacy.allies.2", hours(XFConfig.allianceBreakCooldownMs)))));
		categories.add(category(r, "war", new ItemStack(Items.iron_sword), entry(r, "raiding", new ItemStack(Items.iron_sword),
				page(r, "war.raiding.1", fmt(XFConfig.warDeclarationBaseCost), fmt(XFConfig.warDeclarationTargetPrestigeFactor * 100F), Integer.toString(XFConfig.warOnlinePlayerThreshold)),
				page(r, "war.raiding.2", hours(XFConfig.surrenderCooldownMs), hours(XFConfig.peaceCooldownMs), fmt(XFConfig.surrenderPrestigeTransferPercent * 100F)))));
		categories.add(category(r, "admin", new ItemStack(Blocks.command_block), entry(r, "server_owner", new ItemStack(Blocks.command_block),
				page(r, "admin.server_owner.1"),
				page(r, "admin.server_owner.2"))));
		categories.add(category(r, "faq", new ItemStack(Items.book), entry(r, "troubleshooting", new ItemStack(Items.book),
				page(r, "faq.troubleshooting.1"),
				page(r, "faq.troubleshooting.2"))));
		return categories;
	}

	private static Object category(Reflection r, String name, ItemStack icon, Object entry) throws Exception {
		List entries = new ArrayList();
		entries.add(entry);
		return r.newCategory(entries, key("category." + name), icon);
	}

	private static Object entry(Reflection r, String name, ItemStack icon, Object page1, Object page2) throws Exception {
		List pages = new ArrayList();
		pages.add(page1);
		pages.add(page2);
		return r.newEntry(pages, key("entry." + name), icon);
	}

	private static Object page(Reflection r, String name, Object... args) throws Exception {
		return r.newPageText(tr(key("page." + name), args));
	}

	private static String key(String suffix) {
		return BOOK_KEY_PREFIX + suffix;
	}

	public static String translate(String key, Object... args) {
		return tr(key, args);
	}

	private static String tr(String key, Object... args) {
		return StatCollector.translateToLocalFormatted(key, args);
	}

	private static String fmt(float value) {
		if(Math.abs(value - Math.round(value)) < 0.001F)
			return Integer.toString(Math.round(value));
		return String.format(java.util.Locale.US, "%.2f", value);
	}

	private static String fmt(float[] values, int index) {
		if(values == null || values.length == 0)
			return "0";
		return fmt(values[Math.max(0, Math.min(values.length - 1, index))]);
	}

	private static String hours(long ms) {
		return Long.toString(ms / (60L * 60L * 1000L));
	}

	private static final class Reflection {
		private final Class bookClass;
		private final Class registryClass;
		private final Constructor bookCtor;
		private final Constructor categoryCtor;
		private final Constructor entryCtor;
		private final Constructor pageTextCtor;
		private final Method registerBook;
		private final Method getItemStackForBook;

		private Reflection() throws Exception {
			bookClass = Class.forName("amerifrance.guideapi.api.base.Book");
			registryClass = Class.forName("amerifrance.guideapi.api.GuideRegistry");
			bookCtor = bookClass.getConstructor(List.class, String.class, String.class, String.class, Color.class, boolean.class);
			categoryCtor = Class.forName("amerifrance.guideapi.categories.CategoryItemStack").getConstructor(List.class, String.class, ItemStack.class);
			entryCtor = Class.forName("amerifrance.guideapi.entries.EntryItemStack").getConstructor(List.class, String.class, ItemStack.class);
			pageTextCtor = Class.forName("amerifrance.guideapi.pages.PageText").getConstructor(String.class);
			registerBook = registryClass.getMethod("registerBook", bookClass);
			getItemStackForBook = registryClass.getMethod("getItemStackForBook", bookClass);
		}

		private Object newBook(List categories, String title, String welcome, String displayName, String author, Color color) throws Exception {
			Object book = bookCtor.newInstance(categories, title, welcome, displayName, color, false);
			bookClass.getField("author").set(book, author);
			return book;
		}

		private Object newCategory(List entries, String title, ItemStack icon) throws Exception {
			return categoryCtor.newInstance(entries, title, icon);
		}

		private Object newEntry(List pages, String title, ItemStack icon) throws Exception {
			return entryCtor.newInstance(pages, title, icon);
		}

		private Object newPageText(String text) throws Exception {
			return pageTextCtor.newInstance(text);
		}

		private void registerBook(Object book) throws Exception {
			registerBook.invoke(null, book);
		}

		private ItemStack getItemStackForBook(Object book) throws Exception {
			return (ItemStack)getItemStackForBook.invoke(null, book);
		}
	}
}
