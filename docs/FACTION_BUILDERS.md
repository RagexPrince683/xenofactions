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

## Schematic import workflow

The depot accepts native gzip-NBT `.schematic` files without any optional mod. Choose a schematic, inspect the preview and material/missing-material lists, adjust its origin, select rotation (0/90/180/270) and horizontal mirroring, then submit it. The server resolves every registry-name palette entry and validates dimensions, height, protected blocks, depot ownership, and faction territory before accepting the job. The first rejected world coordinate is reported to the UI. Jobs can be started, paused, resumed, cancelled, and queued; progress is persisted by the Task 1 job store.

Classic Schematica is detected using its actual `Schematica` mod ID. When installed, the client-only compatibility adapter can copy the player's active schematic into Xenofactions' normalized model. No Schematica object is retained by a job or referenced on a dedicated server. Internal class differences are handled reflectively rather than assuming classic and Plus use identical paths.

Schematica Plus uses the same `Schematica` mod ID. Plus-only support is therefore feature-detected by the presence of its `SchematicFormat` and `SchematicLitematica` classes. `.litematic` decoding is delegated to that implementation and is available only when those compatible features exist; otherwise the depot rejects it with the unavailable-format message. Native `.schematic` files remain available in every installation.

## Normalization and transforms

The Xenofactions model stores dimensions, registry identities, metadata, source name/format, and only explicitly approved tile data. Registry names, rather than numeric block IDs, are used on the preview wire path and are resolved before construction. One shared coordinate/metadata transform is used by fallback preview and construction. Vanilla stairs, doors, torches, ladders, signs, buttons, levers, trapdoors, rails, and beds are transformed; unknown modded metadata is retained and debug-logged rather than guessed.

Schematic entities are never imported or spawned. Inventory contents, energy, faction ownership, owner UUIDs, and arbitrary machine NBT are unsupported. Safe tile data requires an explicit handler (sign text is the intended safe extension). Command blocks, bedrock, faction flags, conquest/territory infrastructure, and administrative blocks are protected from normal jobs. Territory permission is checked again for every actual modification.

## Upload security

Uploads use random session IDs, 24 KiB bounded chunks, declared-size validation, duplicate rejection, one-minute incomplete-session expiry, and a server-generated SHA-256 hash. Dimensions and block counts use overflow-safe arithmetic. Malformed palette entries, array lengths, registry identities, chunks, and protected blocks are rejected without allocating their declared arbitrary sizes.

Additional conservative settings under `XENOFACTIONS_19_BUILDERS` are:

* `builderMaxSchematicBlocks` (default `262144`)
* `builderMaxSchematicWidth` (default `256`)
* `builderMaxSchematicHeight` (default `128`)
* `builderMaxSchematicLength` (default `256`)
* `builderMaxUploadBytes` (default `8388608`)
