package com.hfr.client.journeymap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hfr.clowder.ClaimOverlayData;
import com.hfr.clowder.ClaimOverlayData.Claim;

/** Client-thread-owned, copy-on-write claim snapshots. */
public final class ClientClaimOverlayCache {
	private static volatile Map<Integer, Snapshot> snapshots = Collections.emptyMap();
	private static final Map<Integer, Assembly> assemblies = new HashMap<Integer, Assembly>();
	private ClientClaimOverlayCache() { }

	public static void accept(int dimension, int generation, int part, int parts, List<Claim> claims) {
		Assembly assembly = assemblies.get(Integer.valueOf(dimension));
		if(assembly == null || assembly.generation != generation) {
			if(part != 0) return;
			assembly = new Assembly(generation, parts); assemblies.put(Integer.valueOf(dimension), assembly);
		}
		if(part != assembly.nextPart || parts != assembly.parts || assembly.claims.size() + claims.size() > 100000) return;
		assembly.claims.addAll(claims); assembly.nextPart++;
		if(assembly.nextPart == parts) {
			HashMap<Long, Claim> lookup = new HashMap<Long, Claim>();
			HashMap<String, GroupBuilder> groups = new HashMap<String, GroupBuilder>();
			for(Claim claim : assembly.claims) {
				lookup.put(Long.valueOf(ClaimOverlayData.chunkKey(claim.chunkX, claim.chunkZ)), claim);
				GroupBuilder group = groups.get(claim.groupId);
				if(group == null) { group = new GroupBuilder(claim.groupId, claim.label, claim.labelX, claim.labelZ); groups.put(claim.groupId, group); }
				group.include(claim.chunkX, claim.chunkZ);
			}
			HashMap<Integer, Snapshot> copy = new HashMap<Integer, Snapshot>(snapshots);
			copy.clear(); // The server sends only the player's authoritative current dimension.
			copy.put(Integer.valueOf(dimension), new Snapshot(assembly.claims, lookup, groups)); snapshots = Collections.unmodifiableMap(copy);
			assemblies.remove(Integer.valueOf(dimension));
		}
	}
	public static Snapshot get(int dimension) { return snapshots.get(Integer.valueOf(dimension)); }
	public static void clear() { snapshots = Collections.emptyMap(); assemblies.clear(); }

	public static final class Snapshot {
		public final List<Claim> claims; public final Map<Long, Claim> byChunk; public final List<TerritoryGroup> groups;
		private Snapshot(List<Claim> claims, Map<Long, Claim> byChunk, Map<String, GroupBuilder> groups) {
			this.claims = Collections.unmodifiableList(new ArrayList<Claim>(claims));
			this.byChunk = Collections.unmodifiableMap(byChunk);
			ArrayList<TerritoryGroup> built = new ArrayList<TerritoryGroup>();
			for(GroupBuilder group : groups.values()) built.add(group.build());
			this.groups = Collections.unmodifiableList(built);
		}
		public boolean sameGroup(int x, int z, String group) {
			Claim neighbor = byChunk.get(Long.valueOf(ClaimOverlayData.chunkKey(x, z)));
			return neighbor != null && group.equals(neighbor.groupId);
		}
	}
	public static final class TerritoryGroup {
		public final String groupId, label; public final int labelX, labelZ, minChunkX, minChunkZ, maxChunkX, maxChunkZ;
		private TerritoryGroup(String groupId, String label, int labelX, int labelZ, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
			this.groupId = groupId; this.label = label; this.labelX = labelX; this.labelZ = labelZ;
			this.minChunkX = minChunkX; this.minChunkZ = minChunkZ; this.maxChunkX = maxChunkX; this.maxChunkZ = maxChunkZ;
		}
	}
	private static final class GroupBuilder {
		final String groupId, label; final int labelX, labelZ; int minChunkX = Integer.MAX_VALUE, minChunkZ = Integer.MAX_VALUE, maxChunkX = Integer.MIN_VALUE, maxChunkZ = Integer.MIN_VALUE;
		GroupBuilder(String groupId, String label, int labelX, int labelZ) { this.groupId = groupId; this.label = label; this.labelX = labelX; this.labelZ = labelZ; }
		void include(int x, int z) { if(x < minChunkX) minChunkX = x; if(z < minChunkZ) minChunkZ = z; if(x > maxChunkX) maxChunkX = x; if(z > maxChunkZ) maxChunkZ = z; }
		TerritoryGroup build() { return new TerritoryGroup(groupId, label, labelX, labelZ, minChunkX, minChunkZ, maxChunkX, maxChunkZ); }
	}
	private static final class Assembly {
		final int generation, parts; int nextPart; final ArrayList<Claim> claims = new ArrayList<Claim>();
		Assembly(int generation, int parts) { this.generation = generation; this.parts = parts; }
	}
}
