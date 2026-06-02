package com.hfr.dynmap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hfr.clowder.Clowder;
import com.hfr.config.XFConfig;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.main.MainRegistry;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Optional Dynmap marker integration for Xenofactions.
 *
 * This class deliberately uses reflection so Xenofactions can still compile and run without Dynmap.
 */
public class XFDynmapIntegration {

	private static final String MARKER_SET_ID = "xenofactions_cities";
	private static final String MARKER_SET_LABEL = "Faction Cities";
	private static final int UPDATE_INTERVAL_TICKS = 20 * 30;
	private static final double CLAIM_FILL_OPACITY = 0.18D;
	private static final double CLAIM_LINE_OPACITY = 0.0D;
	private static final int CLAIM_LINE_WEIGHT = 0;
	private static final double MARKER_Y = 64.0D;

	private static boolean dirty = true;
	private static boolean dynmapUnavailableLogged = false;
	private static int tickCounter = 0;
	private static Object markerSet = null;
	private static Object cityIcon = null;
	private static Method createAreaMarkerMethod = null;
	private static Method createMarkerMethod = null;
	private static Method createPolyLineMarkerMethod = null;
	private static Method setLineStyleMethod = null;
	private static Method setFillStyleMethod = null;
	private static Method setRangeYMethod = null;

	public static void markDirty() {
		dirty = true;
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase != TickEvent.Phase.END)
			return;

		tickCounter++;
		if(tickCounter < XFConfig.dynmapUpdateIntervalTicks)
			return;

		tickCounter = 0;
		if(dirty)
			updateMarkers();
	}

	public static void updateMarkers() {
		try {
			if(!XFConfig.enableDynmapIntegration)
				return;
			Object markerApi = getMarkerApi();
			if(markerApi == null)
				return;

			World world = DimensionManager.getWorld(0);
			if(world == null)
				return;

			markerSet = getOrCreateMarkerSet(markerApi);
			if(markerSet == null)
				return;

			cityIcon = getMarkerIcon(markerApi, "tower");
			if(cityIcon == null)
				cityIcon = getMarkerIcon(markerApi, "king");
			if(cityIcon == null)
				cityIcon = getMarkerIcon(markerApi, "default");

			cacheMarkerMethods();
			clearMarkerSet(markerSet);
			createCityMarkers(world, getWorldName(world));
			dirty = false;
		} catch(Throwable t) {
			markerSet = null;
			if(!dynmapUnavailableLogged) {
				dynmapUnavailableLogged = true;
				if(MainRegistry.logger != null)
					MainRegistry.logger.warn("Dynmap marker integration is not available yet; faction city markers will retry later.", t);
			}
		}
	}

	private static Object getMarkerApi() throws Exception {
		Class listenerClass = Class.forName("org.dynmap.DynmapCommonAPIListener");
		Field apiField = listenerClass.getDeclaredField("dynmapapi");
		apiField.setAccessible(true);
		Object dynmapApi = apiField.get(null);
		if(dynmapApi == null)
			return null;

		Method markerApiInitialized = dynmapApi.getClass().getMethod("markerAPIInitialized");
		Object initialized = markerApiInitialized.invoke(dynmapApi);
		if(initialized instanceof Boolean && !((Boolean)initialized).booleanValue())
			return null;

		Method getMarkerApi = dynmapApi.getClass().getMethod("getMarkerAPI");
		return getMarkerApi.invoke(dynmapApi);
	}

	private static Object getOrCreateMarkerSet(Object markerApi) throws Exception {
		Method getMarkerSet = markerApi.getClass().getMethod("getMarkerSet", String.class);
		Object set = getMarkerSet.invoke(markerApi, XFConfig.dynmapMarkerSetId);
		if(set == null) {
			Method createMarkerSet = markerApi.getClass().getMethod("createMarkerSet", String.class, String.class, Set.class, boolean.class);
			set = createMarkerSet.invoke(markerApi, XFConfig.dynmapMarkerSetId, XFConfig.dynmapMarkerSetLabel, null, false);
		}
		if(set != null) {
			callIfPresent(set, "setMarkerSetLabel", new Class[] { String.class }, new Object[] { XFConfig.dynmapMarkerSetLabel });
			callIfPresent(set, "setLayerPriority", new Class[] { int.class }, new Object[] { Integer.valueOf(10) });
			callIfPresent(set, "setHideByDefault", new Class[] { boolean.class }, new Object[] { Boolean.FALSE });
		}
		return set;
	}

	private static Object getMarkerIcon(Object markerApi, String iconId) throws Exception {
		Method getMarkerIcon = markerApi.getClass().getMethod("getMarkerIcon", String.class);
		return getMarkerIcon.invoke(markerApi, iconId);
	}

	private static void cacheMarkerMethods() throws Exception {
		Class markerSetClass = markerSet.getClass();
		Class markerIconClass = Class.forName("org.dynmap.markers.MarkerIcon");
		createAreaMarkerMethod = markerSetClass.getMethod("createAreaMarker", String.class, String.class, boolean.class, String.class, double[].class, double[].class, boolean.class);
		createAreaMarkerMethod.setAccessible(true);
		createMarkerMethod = markerSetClass.getMethod("createMarker", String.class, String.class, boolean.class, String.class, double.class, double.class, double.class, markerIconClass, boolean.class);
		createMarkerMethod.setAccessible(true);
		createPolyLineMarkerMethod = markerSetClass.getMethod("createPolyLineMarker", String.class, String.class, boolean.class, String.class, double[].class, double[].class, double[].class, boolean.class);
		createPolyLineMarkerMethod.setAccessible(true);
	}

	private static void clearMarkerSet(Object set) throws Exception {
		deleteAll(set, "getAreaMarkers");
		deleteAll(set, "getMarkers");
		deleteAll(set, "getPolyLineMarkers");
		deleteAll(set, "getCircleMarkers");
	}

	private static void deleteAll(Object set, String getterName) throws Exception {
		Method getter = set.getClass().getMethod(getterName);
		getter.setAccessible(true);
		Set markers = (Set)getter.invoke(set);
		for(Object marker : markers) {
			Method deleteMarker = marker.getClass().getMethod("deleteMarker");
			deleteMarker.setAccessible(true);
			deleteMarker.invoke(marker);
		}
	}

	private static void createCityMarkers(World world, String worldName) throws Exception {
		HashMap<String, CitySummary> cities = new HashMap();
		for(Map.Entry<Long, TerritoryMeta> entry : ClowderTerritory.territories.entrySet()) {
			TerritoryMeta meta = entry.getValue();
			if(meta == null || meta.owner == null || meta.owner.zone != Zone.FACTION || meta.owner.owner == null || !meta.isCityClaim())
				continue;

			CoordPair coords = ClowderTerritory.codeToCoords(entry.getKey().longValue());
			Clowder owner = meta.owner.owner;
			int color = owner.color & 0xFFFFFF;
			String cityId = safeCityId(meta);
			String markerId = "xf_claim_" + Long.toUnsignedString(entry.getKey().longValue(), 16);
			String label = buildClaimLabel(meta, owner, coords);
			double[] x = new double[] { coords.x * 16.0D, coords.x * 16.0D + 16.0D };
			double[] z = new double[] { coords.z * 16.0D, coords.z * 16.0D + 16.0D };

			Object area = createAreaMarkerMethod.invoke(markerSet, markerId, label, Boolean.TRUE, worldName, x, z, Boolean.FALSE);
			if(area != null) {
				if(setLineStyleMethod == null) {
					setLineStyleMethod = area.getClass().getMethod("setLineStyle", int.class, double.class, int.class);
					setLineStyleMethod.setAccessible(true);
				}
				if(setFillStyleMethod == null) {
					setFillStyleMethod = area.getClass().getMethod("setFillStyle", double.class, int.class);
					setFillStyleMethod.setAccessible(true);
				}
				if(setRangeYMethod == null) {
					setRangeYMethod = area.getClass().getMethod("setRangeY", double.class, double.class);
					setRangeYMethod.setAccessible(true);
				}
				setLineStyleMethod.invoke(area, Integer.valueOf(XFConfig.dynmapClaimLineWeight), Double.valueOf(XFConfig.dynmapClaimLineOpacity), Integer.valueOf(color));
				setFillStyleMethod.invoke(area, Double.valueOf(XFConfig.dynmapClaimFillOpacity), Integer.valueOf(color));
				setRangeYMethod.invoke(area, Double.valueOf(64.0D), Double.valueOf(64.0D));
			}

			CitySummary summary = cities.get(cityId);
			if(summary == null) {
				summary = new CitySummary(meta, owner);
				cities.put(cityId, summary);
			}
			summary.claimCount++;
			summary.claims.add(chunkKey(coords.x, coords.z));
		}

		for(CitySummary city : cities.values()) {
			createCityBorders(worldName, city);
			if(XFConfig.dynmapShowCityCenterMarkers)
				createCityCenterMarker(worldName, city);
		}
	}

	private static void createCityBorders(String worldName, CitySummary city) throws Exception {
		int edge = 0;
		for(String claim : city.claims) {
			String[] parts = claim.split(",", 2);
			int chunkX = Integer.parseInt(parts[0]);
			int chunkZ = Integer.parseInt(parts[1]);
			if(!city.claims.contains(chunkKey(chunkX, chunkZ - 1)))
				createBorderEdge(worldName, city, edge++, chunkX * 16.0D, chunkZ * 16.0D, chunkX * 16.0D + 16.0D, chunkZ * 16.0D);
			if(!city.claims.contains(chunkKey(chunkX, chunkZ + 1)))
				createBorderEdge(worldName, city, edge++, chunkX * 16.0D + 16.0D, chunkZ * 16.0D + 16.0D, chunkX * 16.0D, chunkZ * 16.0D + 16.0D);
			if(!city.claims.contains(chunkKey(chunkX - 1, chunkZ)))
				createBorderEdge(worldName, city, edge++, chunkX * 16.0D, chunkZ * 16.0D + 16.0D, chunkX * 16.0D, chunkZ * 16.0D);
			if(!city.claims.contains(chunkKey(chunkX + 1, chunkZ)))
				createBorderEdge(worldName, city, edge++, chunkX * 16.0D + 16.0D, chunkZ * 16.0D, chunkX * 16.0D + 16.0D, chunkZ * 16.0D + 16.0D);
		}
	}

	private static void createBorderEdge(String worldName, CitySummary city, int edge, double x1, double z1, double x2, double z2) throws Exception {
		String markerId = "xf_border_" + sanitizeId(safeCityId(city.meta)) + "_" + edge;
		double[] x = new double[] { x1, x2 };
		double[] y = new double[] { 64.0D, 64.0D };
		double[] z = new double[] { z1, z2 };
		Object line = createPolyLineMarkerMethod.invoke(markerSet, markerId, buildCityLabel(city.meta, city.owner, city.claimCount), Boolean.TRUE, worldName, x, y, z, Boolean.FALSE);
		if(line != null) {
			Method setLineStyle = line.getClass().getMethod("setLineStyle", int.class, double.class, int.class);
			setLineStyle.setAccessible(true);
			setLineStyle.invoke(line, Integer.valueOf(XFConfig.dynmapBorderLineWeight), Double.valueOf(XFConfig.dynmapBorderLineOpacity), Integer.valueOf(city.owner.color & 0xFFFFFF));
		}
	}

	private static void createCityCenterMarker(String worldName, CitySummary city) throws Exception {
		TerritoryMeta meta = city.meta;
		String markerId = "xf_city_" + sanitizeId(safeCityId(meta));
		String label = buildCityLabel(meta, city.owner, city.claimCount);
		double y = meta.flagY >= 0 ? meta.flagY + 1.0D : MARKER_Y;
		createMarkerMethod.invoke(markerSet, markerId, label, Boolean.TRUE, worldName, meta.flagX + 0.5D, y, meta.flagZ + 0.5D, cityIcon, Boolean.FALSE);
	}

	private static String getWorldName(World world) {
		try {
			Class forgeWorldClass = Class.forName("org.dynmap.forge.ForgeWorld");
			Method getWorldName = forgeWorldClass.getMethod("getWorldName", World.class);
			Object name = getWorldName.invoke(null, world);
			if(name instanceof String && !((String)name).isEmpty())
				return (String)name;
		} catch(Throwable ignored) { }

		if(world.provider.dimensionId == 0)
			return world.getWorldInfo().getWorldName();
		return "DIM" + world.provider.dimensionId;
	}

	private static String buildClaimLabel(TerritoryMeta meta, Clowder owner, CoordPair coords) {
		String cityName = displayCityName(meta);
		String label = "<b>" + escapeHtml(cityName) + "</b>" + "<br/><b>Faction:</b> " + escapeHtml(owner.name);
		if(XFConfig.dynmapShowClaimDetails)
			label += "<br/><b>Level:</b> " + escapeHtml(meta.getCityLevel().displayName) + "<br/><b>Chunk:</b> " + coords.x + ", " + coords.z;
		if(XFConfig.dynmapShowPrestigeDetails)
			label += "<br/><b>Upkeep:</b> " + XFConfig.cityUpkeep(meta.getCityLevel());
		return label;
	}

	private static String buildCityLabel(TerritoryMeta meta, Clowder owner, int claimCount) {
		String label = "<b>City Center: " + escapeHtml(displayCityName(meta)) + "</b>" + "<br/><b>Faction:</b> " + escapeHtml(owner.name);
		if(XFConfig.dynmapShowClaimDetails)
			label += "<br/><b>Level:</b> " + escapeHtml(meta.getCityLevel().displayName) + "<br/><b>Claims:</b> " + claimCount;
		if(XFConfig.dynmapShowPrestigeDetails)
			label += "<br/><b>Upkeep:</b> " + XFConfig.cityUpkeep(meta.getCityLevel());
		label += "<br/><b>Faction Color:</b> #" + String.format("%06X", owner.color & 0xFFFFFF);
		return label;
	}

	private static String displayCityName(TerritoryMeta meta) {
		if(meta.cityName != null && !meta.cityName.trim().isEmpty())
			return meta.cityName;
		if(meta.name != null && !meta.name.trim().isEmpty())
			return meta.name;
		return "Unnamed City";
	}

	private static String safeCityId(TerritoryMeta meta) {
		if(meta.cityId != null && !meta.cityId.isEmpty())
			return meta.cityId;
		return meta.flagX + "," + meta.flagY + "," + meta.flagZ;
	}

	private static String sanitizeId(String id) {
		return id.replaceAll("[^A-Za-z0-9_.]", "_");
	}

	private static String chunkKey(int x, int z) {
		return x + "," + z;
	}

	private static String escapeHtml(String text) {
		if(text == null)
			return "";
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}

	private static void callIfPresent(Object target, String methodName, Class[] parameterTypes, Object[] args) {
		try {
			Method method = target.getClass().getMethod(methodName, parameterTypes);
			method.setAccessible(true);
			method.invoke(target, args);
		} catch(Throwable ignored) { }
	}

	private static class CitySummary {
		private final TerritoryMeta meta;
		private final Clowder owner;
		private final HashSet<String> claims = new HashSet();
		private int claimCount;

		private CitySummary(TerritoryMeta meta, Clowder owner) {
			this.meta = meta;
			this.owner = owner;
		}
	}
}
