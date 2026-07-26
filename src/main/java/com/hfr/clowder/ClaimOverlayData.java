package com.hfr.clowder;

import java.util.ArrayList;
import java.util.Collections;
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
		for(Map.Entry<CoordPair, TerritoryMeta> entry : ClowderTerritory.territories.entrySet()) {
			CoordPair coord = entry.getKey();
			TerritoryMeta meta = entry.getValue();
			if(coord == null || coord.dimensionId != dimensionId || meta == null || meta.owner == null
					|| meta.owner.zone != Zone.FACTION || meta.owner.owner == null || !meta.isCityClaim())
				continue;
			Clowder faction = meta.owner.owner;
			String factionId = faction.uuid == null || faction.uuid.length() == 0 ? faction.name : faction.uuid;
			String cityId = meta.cityId == null || meta.cityId.length() == 0
					? dimensionId + ":" + meta.flagX + ":" + meta.flagY + ":" + meta.flagZ : meta.cityId;
			claims.add(new Claim(dimensionId, coord.x, coord.z, factionId + "/" + cityId, faction.color & 0xFFFFFF));
		}
		return Collections.unmodifiableList(claims);
	}

	public static long chunkKey(int x, int z) {
		return ((long)x << 32) ^ (z & 0xffffffffL);
	}

	public static final class Claim {
		public final int dimensionId, chunkX, chunkZ, color;
		public final String groupId;
		public Claim(int dimensionId, int chunkX, int chunkZ, String groupId, int color) {
			this.dimensionId = dimensionId; this.chunkX = chunkX; this.chunkZ = chunkZ;
			this.groupId = groupId; this.color = color & 0xFFFFFF;
		}
	}
}
