# Faction Builders

The faction Builder is a persistent server-side worker attached to a faction, a Builder Depot, and an optional construction job. Builders use ordinary Minecraft navigation, do not naturally despawn, do not teleport to work, and pause their job when killed. A replacement Builder can be assigned the same job UUID.

## Builder Depot

The existing Builder machine and tile-entity registry IDs are retained. Its legacy slot remains slot 0, so old inventory NBT is readable, and slots 1 through 27 now hold construction materials. The depot records its faction UUID, assigned Builder UUID, active job UUID, and queued job UUIDs.

## Jobs and materials

Jobs live in overworld `WorldSavedData`, separately from entities and chunks. Each record includes job/faction/player/Builder UUIDs, optional city ID, dimension, schematic identifier, origin, depot, state, and persistent block index. The worker examines no more than the configured scan budget and performs at most one world modification per work cycle.

Survival construction resolves every desired block to an item. The Builder uses its inventory before its depot, walks to the depot to collect missing stock, and consumes stock only after placement succeeds. Unsupported block-to-item mappings stop as missing materials rather than granting modded blocks for free. Instant `SchematicPronter` behavior remains only on the old explicit compatibility path and is not used by Builder jobs.

## Faction safety

The Builder consults `ClowderTerritory` immediately before every placement or destruction. Its own faction claims are allowed. Wilderness is denied by default; SAFEZONE and WARZONE are always denied. Another faction must grant the Builder faction `BUILD` for placement and `DESTROY` for removal/replacement. Bedrock, flags, conquest blocks, and territory-provider tile entities are never destroyed by normal jobs. A permission or protection failure records the coordinate and pauses work.

## Configuration

Settings are under `XENOFACTIONS_19_BUILDERS`:

* `enableFactionBuilders` (default `true`) enables worker updates.
* `builderWorkIntervalTicks` (default `10`) controls work-cycle frequency.
* `builderBlockScanBudget` (default `64`) bounds schematic entries examined per cycle.
* `builderAllowWilderness` (default `false`) opts into wilderness construction.

Schematic import, preview transformation, mirroring, rotation, and Schematica compatibility are intentionally outside this implementation.
