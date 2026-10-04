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
import com.hfr.clowder.TerritoryCoordinateBounds;
import com.hfr.main.MainRegistry;
import com.hfr.tdm.TDMManager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;

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
	private static String lastTdmSignature;
	private static Object markerSet = null;
	private static Object cityIcon = null;
	private static Method createAreaMarkerMethod = null;
	private static Method createMarkerMethod = null;
	private static Method createPolyLineMarkerMethod = null;
	private static Method setLineStyleMethod = null;
	private static Method setFillStyleMethod = null;
	private static Method setRangeYMethod = null;
	private static final Map<Integer, String> worldNames = new HashMap<Integer, String>();

	public static void markDirty() {
		dirty = true;
		com.hfr.journeymap.ClaimOverlaySync.markAllDirty();
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(!event.world.isRemote) dirty = true;
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(!event.world.isRemote) dirty = true;
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase != TickEvent.Phase.END)
			return;

		tickCounter++;
		if(tickCounter < XFConfig.dynmapUpdateIntervalTicks)
			return;

		tickCounter = 0;
		// A small map-only signature catches legacy command paths without rescanning
		// every Clowder claim whenever match scores change.
		String signature = tdmSignature();
		if(dirty || !signature.equals(lastTdmSignature)) updateMarkers();
	}

	public static void updateMarkers() {
		try {
			if(!XFConfig.enableDynmapIntegration)
				return;
			Object markerApi = getMarkerApi();
			if(markerApi == null)
				return;
			worldNames.clear();

			markerSet = getOrCreateMarkerSet(markerApi);
			if(markerSet == null)
				return;

			cityIcon = getMarkerIcon(markerApi, "tower");
			if(cityIcon == null)
				cityIcon = getMarkerIcon(markerApi, "king");
			if(cityIcon == null)
				cityIcon = getMarkerIcon(markerApi, "default");

			cacheMarkerMethods();
			clearMarkerSet(markerSet, "xf_claim_", "xf_border_", "xf_city_");
			HashSet<Integer> renderedDims = new HashSet();
			for(CoordPair coord : ClowderTerritory.territories.keySet()) {
				if(!renderedDims.add(Integer.valueOf(coord.dimensionId)))
					continue;
				World dimWorld = DimensionManager.getWorld(coord.dimensionId);
				String worldName = getWorldName(coord.dimensionId);
				if(dimWorld != null && worldName != null && !worldName.isEmpty())
					createCityMarkers(dimWorld, worldName);
			}
			Object citySet = markerSet;
			markerSet = getOrCreateMarkerSet(markerApi, "xenofactions_claims", "Clowder Faction Claims", false);
			if(markerSet != null) { clearMarkerSet(markerSet, "xf_faction_"); createFactionClaimMarkers(); }
			markerSet = getOrCreateMarkerSet(markerApi, "xenofactions_zones", "Clowder Safezones and Warzones", false);
			if(markerSet != null) { clearMarkerSet(markerSet, "xf_zone_"); createZoneMarkers(); }
			markerSet = getOrCreateMarkerSet(markerApi, "xenofactions_tdm", "Active TDM Map", false);
			if(markerSet != null) { clearMarkerSet(markerSet, "xf_map_", "xf_bomb_", "xf_spawn_"); createTdmMarkers(markerApi); }
			markerSet = citySet;
			lastTdmSignature = tdmSignature();
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

	private static String tdmSignature() {
		World world = DimensionManager.getWorld(0);
		if(world == null) return "unloaded";
		StringBuilder out = new StringBuilder();
		out.append(TDMManager.isEnabled(world)).append('|').append(TDMManager.getSelectedMap(world));
		TDMManager.TDMMap map = TDMManager.getSelectedMapData(world);
		if(map == null) return out.toString();
		out.append('|').append(TDMManager.getGameMode(world)).append('|').append(XFConfig.dynmapShowTdmSpawns);
		appendArea(out, map.bounds); appendArea(out, map.bombsiteA); appendArea(out, map.bombsiteB);
		if(XFConfig.dynmapShowTdmSpawns) for(TDMManager.SpawnPoint spawn : map.resolvedSpawns(TDMManager.getGameMode(world)))
			out.append('|').append(spawn.dim).append(':').append(spawn.x).append(':').append(spawn.y).append(':').append(spawn.z).append(':').append(spawn.team);
		return out.toString();
	}
	private static void appendArea(StringBuilder out, TDMManager.Bombsite a) {
		out.append('|').append(a.hasPos1).append(':').append(a.hasPos2).append(':').append(a.dimension)
			.append(':').append(a.x1).append(':').append(a.y1).append(':').append(a.z1)
			.append(':').append(a.x2).append(':').append(a.y2).append(':').append(a.z2);
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
		return getOrCreateMarkerSet(markerApi, XFConfig.dynmapMarkerSetId, XFConfig.dynmapMarkerSetLabel, false);
	}

	private static Object getOrCreateMarkerSet(Object markerApi, String id, String label, boolean hidden) throws Exception {
		Method getMarkerSet = markerApi.getClass().getMethod("getMarkerSet", String.class);
		Object set = getMarkerSet.invoke(markerApi, id);
		if(set == null) {
			Method createMarkerSet = markerApi.getClass().getMethod("createMarkerSet", String.class, String.class, Set.class, boolean.class);
			set = createMarkerSet.invoke(markerApi, id, label, null, false);
		}
		if(set != null) {
			callIfPresent(set, "setMarkerSetLabel", new Class[] { String.class }, new Object[] { label });
			callIfPresent(set, "setLayerPriority", new Class[] { int.class }, new Object[] { Integer.valueOf(10) });
			callIfPresent(set, "setHideByDefault", new Class[] { boolean.class }, new Object[] { Boolean.valueOf(hidden) });
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

	private static void clearMarkerSet(Object set, String... prefixes) throws Exception {
		deleteAll(set, "getAreaMarkers", prefixes);
		deleteAll(set, "getMarkers", prefixes);
		deleteAll(set, "getPolyLineMarkers", prefixes);
		deleteAll(set, "getCircleMarkers", prefixes);
	}

	private static void deleteAll(Object set, String getterName, String[] prefixes) throws Exception {
		Method getter = set.getClass().getMethod(getterName);
		getter.setAccessible(true);
		Set markers = new HashSet((Set)getter.invoke(set));
		for(Object marker : markers) {
			Method getId = marker.getClass().getMethod("getMarkerID");
			getId.setAccessible(true);
			String id = (String)getId.invoke(marker);
			boolean owned = false;
			for(String prefix : prefixes) if(id != null && id.startsWith(prefix)) { owned = true; break; }
			if(!owned) continue;
			Method deleteMarker = marker.getClass().getMethod("deleteMarker");
			deleteMarker.setAccessible(true);
			deleteMarker.invoke(marker);
		}
	}

	private static void createCityMarkers(World world, String worldName) throws Exception {
		HashMap<String, CitySummary> cities = new HashMap();
		for(Map.Entry<CoordPair, TerritoryMeta> entry : ClowderTerritory.territories.entrySet()) {
			TerritoryMeta meta = entry.getValue();
			if(meta == null || meta.owner == null || meta.owner.zone != Zone.FACTION || meta.owner.owner == null || !meta.isCityClaim())
				continue;

			CoordPair coords = entry.getKey();
			if(coords.dimensionId != ClowderTerritory.getDimensionId(world))
				continue;
			Clowder owner = meta.owner.owner;
			int color = owner.color & 0xFFFFFF;
			String cityId = safeCityId(meta);
			String markerId = "xf_claim_" + coords.dimensionId + "_" + coords.x + "_" + coords.z;
			String label = buildClaimLabel(meta, owner, coords);
			TerritoryCoordinateBounds.Bounds claimX = TerritoryCoordinateBounds.forCoordinate(coords.x);
			TerritoryCoordinateBounds.Bounds claimZ = TerritoryCoordinateBounds.forCoordinate(coords.z);
			double[] x = new double[] { claimX.minInclusive, claimX.maxExclusive };
			double[] z = new double[] { claimZ.minInclusive, claimZ.maxExclusive };

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

	private static void createZoneMarkers() throws Exception {
		for(Map.Entry<CoordPair, TerritoryMeta> entry : ClowderTerritory.territories.entrySet()) {
			CoordPair coord = entry.getKey(); TerritoryMeta meta = entry.getValue();
			if(coord == null || meta == null || meta.owner == null) continue;
			Zone zone = meta.owner.zone;
			if(zone != Zone.SAFEZONE && zone != Zone.WARZONE) continue;
			String worldName = getWorldName(coord.dimensionId);
			if(worldName == null || worldName.isEmpty()) continue;
			int color = zone == Zone.SAFEZONE ? ClowderTerritory.SAFEZONE_COLOR : ClowderTerritory.WARZONE_COLOR;
			TerritoryCoordinateBounds.Bounds x = TerritoryCoordinateBounds.forCoordinate(coord.x);
			TerritoryCoordinateBounds.Bounds z = TerritoryCoordinateBounds.forCoordinate(coord.z);
			area("xf_zone_" + coord.dimensionId + "_" + coord.x + "_" + coord.z,
					zone == Zone.SAFEZONE ? "Safezone" : "Warzone", worldName,
					x.minInclusive, z.minInclusive, x.maxExclusive, z.maxExclusive,
					color, 0.16D, 64D, 64D);
		}
	}

	private static void createFactionClaimMarkers() throws Exception {
		for(Map.Entry<CoordPair, TerritoryMeta> entry : ClowderTerritory.territories.entrySet()) {
			CoordPair coord = entry.getKey(); TerritoryMeta meta = entry.getValue();
			if(coord == null || meta == null || meta.owner == null || meta.owner.zone != Zone.FACTION
					|| meta.owner.owner == null || meta.isCityClaim()) continue;
			String worldName = getWorldName(coord.dimensionId);
			if(worldName == null || worldName.isEmpty()) continue;
			TerritoryCoordinateBounds.Bounds x = TerritoryCoordinateBounds.forCoordinate(coord.x);
			TerritoryCoordinateBounds.Bounds z = TerritoryCoordinateBounds.forCoordinate(coord.z);
			String label = "Faction claim: " + escapeHtml(meta.owner.owner.name);
			if(meta.name != null && !meta.name.isEmpty()) label += " (" + escapeHtml(meta.name) + ")";
			area("xf_faction_" + coord.dimensionId + "_" + coord.x + "_" + coord.z, label, worldName,
					x.minInclusive, z.minInclusive, x.maxExclusive, z.maxExclusive,
					meta.owner.owner.color & 0xFFFFFF, 0.14D, 64D, 64D);
		}
	}

	private static void createTdmMarkers(Object markerApi) throws Exception {
		World world = DimensionManager.getWorld(0);
		if(world == null || !TDMManager.isEnabled(world)) return;
		TDMManager.TDMMap map = TDMManager.getSelectedMapData(world);
		if(map == null) return;
		String id = sanitizeId(map.name);
		if(map.mapBorderEnabled) mapArea("xf_map_" + id, "TDM map: " + escapeHtml(map.name), map.bounds, 0x6BC8FF, 0.04D);
		if(TDMManager.isBombMode(world)) {
			mapArea("xf_bomb_a_" + id, "Search and Destroy site A: " + escapeHtml(map.name), map.bombsiteA, 0xFFD262, 0.23D);
			mapArea("xf_bomb_b_" + id, "Search and Destroy site B: " + escapeHtml(map.name), map.bombsiteB, 0xFF965E, 0.23D);
		}
		if(!XFConfig.dynmapShowTdmSpawns) return;
		Object icon = getMarkerIcon(markerApi, "default");
		if(icon == null) return;
		java.util.List<TDMManager.SpawnPoint> spawns = map.resolvedSpawns(TDMManager.getGameMode(world));
		for(int i = 0; i < spawns.size(); i++) {
			TDMManager.SpawnPoint spawn = spawns.get(i);
			String worldName = getWorldName(spawn.dim);
			if(worldName == null || worldName.isEmpty()) continue;
			String type = spawn.team == null ? "FFA" : spawn.team.name.toUpperCase();
			createMarkerMethod.invoke(markerSet, "xf_spawn_" + id + "_" + i, "TDM " + escapeHtml(map.name) + " " + type + " spawn " + (i + 1),
					Boolean.TRUE, worldName, spawn.x + 0.5D, (double)spawn.y, spawn.z + 0.5D, icon, Boolean.FALSE);
		}
	}

	private static void mapArea(String id, String label, TDMManager.Bombsite bounds, int color, double opacity) throws Exception {
		if(!bounds.isComplete()) return;
		String worldName = getWorldName(bounds.dimension);
		if(worldName == null || worldName.isEmpty()) return;
		com.hfr.tdm.BlockAreaEdges edges = com.hfr.tdm.BlockAreaEdges.of(bounds.x1, bounds.z1, bounds.x2, bounds.z2);
		area(id, label, worldName, edges.minX, edges.minZ, edges.maxXExclusive, edges.maxZExclusive,
				color, opacity, Math.min(bounds.y1, bounds.y2), Math.max(bounds.y1, bounds.y2) + 1D);
	}

	private static void area(String id, String label, String worldName, double x1, double z1, double x2, double z2,
			int color, double opacity, double y1, double y2) throws Exception {
		Object marker = createAreaMarkerMethod.invoke(markerSet, id, label, Boolean.TRUE, worldName,
				new double[] { x1, x2 }, new double[] { z1, z2 }, Boolean.FALSE);
		if(marker == null) return;
		Method line = marker.getClass().getMethod("setLineStyle", int.class, double.class, int.class); line.setAccessible(true);
		Method fill = marker.getClass().getMethod("setFillStyle", double.class, int.class); fill.setAccessible(true);
		Method range = marker.getClass().getMethod("setRangeY", double.class, double.class); range.setAccessible(true);
		line.invoke(marker, Integer.valueOf(2), Double.valueOf(0.85D), Integer.valueOf(color));
		fill.invoke(marker, Double.valueOf(opacity), Integer.valueOf(color));
		range.invoke(marker, Double.valueOf(y1), Double.valueOf(y2));
	}

	private static void createCityBorders(String worldName, CitySummary city) throws Exception {
		int edge = 0;
		for(String claim : city.claims) {
			String[] parts = claim.split(",", 2);
			int chunkX = Integer.parseInt(parts[0]);
			int chunkZ = Integer.parseInt(parts[1]);
			TerritoryCoordinateBounds.Bounds x = TerritoryCoordinateBounds.forCoordinate(chunkX);
			TerritoryCoordinateBounds.Bounds z = TerritoryCoordinateBounds.forCoordinate(chunkZ);
			if(!city.claims.contains(chunkKey(chunkX, chunkZ - 1)))
				createBorderEdge(worldName, city, edge++, x.minInclusive, z.minInclusive, x.maxExclusive, z.minInclusive);
			if(!city.claims.contains(chunkKey(chunkX, chunkZ + 1)))
				createBorderEdge(worldName, city, edge++, x.maxExclusive, z.maxExclusive, x.minInclusive, z.maxExclusive);
			if(!city.claims.contains(chunkKey(chunkX - 1, chunkZ)))
				createBorderEdge(worldName, city, edge++, x.minInclusive, z.maxExclusive, x.minInclusive, z.minInclusive);
			if(!city.claims.contains(chunkKey(chunkX + 1, chunkZ)))
				createBorderEdge(worldName, city, edge++, x.maxExclusive, z.minInclusive, x.maxExclusive, z.maxExclusive);
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

	private static String getWorldName(int dimension) throws Exception {
		Integer key = Integer.valueOf(dimension);
		if(worldNames.containsKey(key)) return worldNames.get(key);
		String name = resolveWorldName(dimension);
		worldNames.put(key, name);
		return name;
	}

	private static String resolveWorldName(int dimension) throws Exception {
		String configured = XFConfig.dynmapWorldNameForDimension(dimension);
		if(configured == null || configured.isEmpty()) return configured;

		// Old generated configs used Bukkit-style names even on Forge saves with
		// custom names. Resolve those stock entries too, without replacing overrides.
		boolean stock = (dimension == 0 && "world".equals(configured))
				|| (dimension == -1 && "world_nether".equals(configured))
				|| (dimension == 1 && "world_the_end".equals(configured));
		String name = configured;
		if("auto".equals(configured) || stock) {
			World world = DimensionManager.getWorld(dimension);
			if(world == null) return null;
			Class forgeWorldClass = Class.forName("org.dynmap.forge.ForgeWorld");
			Method getWorldName = forgeWorldClass.getMethod("getWorldName", World.class);
			name = (String)getWorldName.invoke(null, world);
		}
		if(name == null || name.isEmpty()) return null;
		// GTNH 0.3.47's JSON writer compares getWorld() to the normalized world
		// filename. Newer versions compare getNormalizedWorld(). Supply the canonical
		// name so both versions publish the same geometry, including names with /[].
		Class dynmapWorldClass = Class.forName("org.dynmap.DynmapWorld");
		Method normalize = dynmapWorldClass.getMethod("normalizeWorldName", String.class);
		return (String)normalize.invoke(null, name);
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
