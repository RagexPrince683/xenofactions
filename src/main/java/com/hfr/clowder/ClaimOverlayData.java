package com.hfr.clowder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;

/** Side-neutral, immutable claim data shared by optional map integrations. */
public final class ClaimOverlayData {
	private ClaimOverlayData() { }

	public static List<Claim> snapshot(int dimensionId) {
		ArrayList<Claim> claims = new ArrayList<Claim>();
		HashMap<String, Bounds> bounds = new HashMap<String, Bounds>();
		for(Map.Entry<CoordPair, TerritoryMeta> entry : ClowderTerritory.territories.entrySet()) {
			CoordPair coord = entry.getKey();
			TerritoryMeta meta = entry.getValue();
			if(coord == null || coord.dimensionId != dimensionId || meta == null || meta.owner == null
					|| meta.owner.zone != Zone.FACTION || meta.owner.owner == null || !meta.isCityClaim())
				continue;
			String groupId = groupId(dimensionId, meta);
			Bounds bound = bounds.get(groupId);
			if(bound == null) { bound = new Bounds(meta); bounds.put(groupId, bound); }
			bound.include(coord.x, coord.z);
		}
		for(Map.Entry<CoordPair, TerritoryMeta> entry : ClowderTerritory.territories.entrySet()) {
			CoordPair coord = entry.getKey();
			TerritoryMeta meta = entry.getValue();
			if(coord == null || coord.dimensionId != dimensionId || meta == null || meta.owner == null
					|| meta.owner.zone != Zone.FACTION || meta.owner.owner == null || !meta.isCityClaim())
				continue;
			Clowder faction = meta.owner.owner;
			String groupId = groupId(dimensionId, meta);
			Bounds bound = bounds.get(groupId);
			int labelX = bound == null ? coord.x * 16 + 8 : bound.labelX();
			int labelZ = bound == null ? coord.z * 16 + 8 : bound.labelZ();
			claims.add(new Claim(dimensionId, coord.x, coord.z, groupId, faction.color & 0xFFFFFF, cleanLabel(meta.cityName), labelX, labelZ));
		}
		return Collections.unmodifiableList(claims);
	}

	private static String groupId(int dimensionId, TerritoryMeta meta) {
		return meta.cityId == null || meta.cityId.length() == 0 ? dimensionId + ":" + meta.flagX + ":" + meta.flagY + ":" + meta.flagZ : meta.cityId;
	}

	private static String cleanLabel(String label) {
		if(label == null) return "";
		String trimmed = label.trim();
		int max = Math.max(1, com.hfr.config.XFConfig.claimNameMaxLength);
		return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
	}

	private static final class Bounds {
		final TerritoryMeta meta; int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		Bounds(TerritoryMeta meta) { this.meta = meta; }
		void include(int x, int z) { if(x < minX) minX = x; if(z < minZ) minZ = z; if(x > maxX) maxX = x; if(z > maxZ) maxZ = z; }
		boolean hasValidFlag() { return meta.flagY >= 0; }
		int labelX() { return hasValidFlag() ? meta.flagX : (minX * 16 + (maxX - minX + 1) * 8); }
		int labelZ() { return hasValidFlag() ? meta.flagZ : (minZ * 16 + (maxZ - minZ + 1) * 8); }
	}

	public static long chunkKey(int x, int z) {
		return ((long)x << 32) ^ (z & 0xffffffffL);
	}

	public static final class Claim {
		public final int dimensionId, chunkX, chunkZ, color, labelX, labelZ;
		public final String groupId, label;
		public Claim(int dimensionId, int chunkX, int chunkZ, String groupId, int color) {
			this(dimensionId, chunkX, chunkZ, groupId, color, "", chunkX * 16 + 8, chunkZ * 16 + 8);
		}
		public Claim(int dimensionId, int chunkX, int chunkZ, String groupId, int color, String label, int labelX, int labelZ) {
			this.dimensionId = dimensionId; this.chunkX = chunkX; this.chunkZ = chunkZ;
			this.groupId = groupId == null ? "" : groupId; this.color = color & 0xFFFFFF;
			this.label = label == null ? "" : label; this.labelX = labelX; this.labelZ = labelZ;
		}
	}
}
