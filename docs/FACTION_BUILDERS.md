# Faction Builders

## Starting a Builder

The existing city progression awards a **Builder Contract**. Use it on a valid Builder Depot belonging to that city to create the city's persistent Builder. A Depot owns one Builder assignment; unloading the entity does not create a replacement, while confirmed death allows the normal replacement flow to resume the persistent job.

Right-click either the **Builder Depot** or its assigned **Builder NPC** to open the same management screen. The NPC resolves its Depot on the logical server and refuses stale, missing, cross-dimension, or otherwise invalid assignments instead of opening a second copy of job state.

## Management pages

### Overview

Shows the assigned Builder identifier, faction, city, persistent `BuilderState`, active schematic, block progress, progress bar, and the first blocked coordinate. Valid controls recall the Builder to the Depot using normal navigation, pause work, revalidate and resume paused work, or cancel future work. Cancelling never removes blocks already placed.

### Build

Select a schematic from the scrollable Xenofactions library, enter the exact X/Y/Z origin, choose 0/90/180/270-degree rotation and horizontal mirroring, and preview the transformed result client-side. **Validate** reports input problems in the screen; **Start** repeats all authoritative validation on the server, including configured dimensions/block limits and the complete transformed territory footprint. It starts immediately when idle or joins the Depot queue when another job is active.

Native server `.schematic` files use Xenofactions' strict normalized schematic loader. Place them in the Forge runtime `config/schematics/` folder (the development run resolves this to `eclipse/config/schematics/`). Any normal filename is accepted, including spaces, multiple underscores, and case-insensitive `.schematic` extensions; the filename without its extension is the display name. The server library detects additions, removals, renames, replacements, and modifications while Builder selection is in use, and the selector also provides **Refresh** for an immediate rescan. If optional classic Schematica is installed, **Use Active Schematica** imports its active client schematic. Schematica Plus uses the same `Schematica` mod ID, so Plus-only feature detection controls optional `.litematic` decoding. Neither mod is a server dependency, and all optional-mod reflection remains client-only.

### Materials

This is the only page exposing inventory controls. Depot construction materials remain in slots **1–27**, followed by the player inventory; preserved slot 0 is hidden legacy storage and shift-click never targets it. The requirement snapshot is recalculated only when the selected schematic changes, marks unsupported mappings, and does not grant free blocks. Builders consume carried stock first, walk back for Depot stock, and consume an item only after placement succeeds.

### Queue

The active job is listed first, followed by queued jobs with state and progress. Authorized players may remove or reorder queued entries. Completing or cancelling an active job advances to the next valid entry and assigns it to the same Builder.

## Permissions and safety

Viewing and controlling a Depot requires canonical UUID-based faction membership. State-changing construction actions require the faction's canonical **BUILD** authority; material inventory interaction remains governed by canonical **CONTAINER** permission. Enemy and unrelated factions cannot control a Depot. The server rechecks dimension, coordinates, distance, tile type, ownership, permission, schematic limits, transformed territory, protected blocks, and territory access instead of trusting GUI or upload data.

Builders walk normally rather than teleporting and perform at most one world modification per configured work cycle. They retain real material consumption and continue to honor safezone, warzone, faction flag, conquest, bedrock, protected-tile, and claim protections. Missing chunks pause progress without losing the saved block index.

## Persistent schematics

When a server accepts a job, it normalizes and hashes the schematic into the server-owned `WorldSavedData` schematic store. `BuilderJob.schematicId` references that stable hash, so imported client schematics remain resolvable after restart and identical normalized schematics are deduplicated. Jobs never retain Schematica objects or arbitrary schematic entities, and the existing safe tile-data policy remains in force.

The old `SchematicPronter` methods remain only as legacy code for explicit debug/admin callers. Normal Builder Depot gameplay never invokes instant construction or deletion.

## Builder screens

The Builder user interface is intentionally split by responsibility:

- **Builder NPC GUI = worker status and control.** Right-click the worker to inspect its friendly name, health, faction/city assignment, Depot coordinates, current state and job, carried materials, and any blocked coordinate. This screen only provides recall, pause, and resume controls. `Open Depot` is available only while the player is within the Depot's normal interaction range.
- **Builder Depot GUI = construction planning, materials, and queue.** Right-click the Depot to use Overview, Build, Materials, and Queue. Inventory slots exist only on Materials; the other pages are planning/status views.

### Selecting and placing a schematic

On Build, choose **Select Schematic** to open the dedicated searchable library. Select a native Xenofactions `.schematic`, or use **Import Active** when the optional Schematica/Schematica Plus compatibility layer has an active schematic. The selection screen reports dimensions and a material summary. **Done** returns to the same Depot session without discarding its container.

New Builder plans default to a safe point near their owning Depot (`Depot X + 2`, `Depot Y`, `Depot Z + 2`). X, Y, and Z are absolute world coordinates, including zero and negative X/Z values: specifically, they are the world block position occupied by normalized schematic local block `(0, 0, 0)`. **Use Depot Position** restores that nearby default. The coordinate nudge controls and manual edits update the same persistent, server-authoritative plan used by both the world-space preview and the final Builder job, so construction occurs exactly where the hologram was shown.

MCEdit/Schematica `WEOrigin*` values describe the exporting world's selection and are not absolute placement instructions. `WEOffset*` and `WEOrigin*` are therefore intentionally discarded during import: the block array is normalized to local `(0, 0, 0)`, and the Builder Plan X/Y/Z is its sole world-space anchor. Preview geometry, its cyan outer edges and origin marker, and construction all use that normalized coordinate convention through the shared schematic transform.

The Build page presents the intended order directly: select a schematic, set X/Y/Z, preview, validate, then start construction. Rotation, mirroring, preview clearing, validation state, and missing-material information remain visible before the job is submitted to the existing server-authoritative Builder workflow.

## Persistent planning and diagnostics

The Depot now owns its draft plan. Its source type and stable native-library or stored-schematic ID, display name, origin, rotation, mirror choice, preview choice, and last validation result are saved in tile NBT and synchronized through validated Builder packets. Closing a screen, moving between pages, using Back in the selector, or reconnecting therefore does not discard a native selection. A missing library source is cleared explicitly rather than leaving a dangling Java object.

Native previews are normalized on the server and delivered on demand in palette-based chunks capped at 24 KiB. Transfers carry an independent session ID, total encoded size, chunk numbering, a 30-second assembly timeout, duplicate rejection, and a SHA-256 final integrity check. Completed schematics are cached by stable library ID on the client. Until assembly completes the Build page reports that the preview is loading.

Preview rendering uses the exact job origin, rotation, and mirror transform, skips air, uses a visible translucent blend with depth writes disabled, and restores render state. Large previews are sampled to a bounded 32,768 rendered cells rather than scanning/rendering millions of blocks without limit. The entered X/Y/Z is the construction origin; the transformed footprint identifies the structure placement before Start.

Material accounting uses a shared identity-based calculator. Air is excluded, item metadata and NBT participate in aggregation, and unmappable non-air blocks retain registry name, metadata, quantity, and first schematic coordinate. Start preflight rejects unsupported mappings, out-of-world-height plans, malformed rotations, invalid territory, and missing schematics; ordinary missing stock remains a recoverable waiting condition.

Every persistent job records its current world and schematic target, required item/meta, known missing quantity, unsupported block/meta, path retries, waiting chunk, blocked coordinate, and a structured localized status reason. Missing materials and unloaded chunks are temporary states and are polled at the configured work interval, resuming automatically. Unsupported mappings, invalid/protected territory, repeated path failure, and an explicit pause are permanent stops until an authorized Retry/Resume revalidates them. A loaded Builder also reattaches to its Depot's active UUID-owned job after an unload instead of losing activation.

## Work reach, recovery, and failure reporting

Builders currently require **construction materials only**; they do not require pickaxes, axes, shovels, wrenches, or other tools to place or clear schematic cells. A Builder first works immediately when its current eye position has reach and line of sight; otherwise it navigates to a safe standing position within a four-block reach of its target rather than trying to stand inside the target. Candidate feet positions include cardinal and diagonal approaches across a seven-block vertical range and must have collision-free feet/head space and solid footing. Vanilla navigation is enabled once through `EntityLiving`'s AI lifecycle, permits swimming and does not avoid ordinary water, allowing grass, dirt, slabs, shallow trenches, walls, ceilings, roofs, and upper floors without teleportation or flight. Lava remains hazardous.

Movement is watched for meaningful physical position or remaining-distance progress. A valid path is allowed to continue without being replaced every work cycle. When movement stalls, the Builder clears it and tries another distinct safe standing position. Candidate connectivity is checked by vanilla PathFinder one bounded attempt per work cycle, with up to 24 useful alternatives before the persistent job enters `PATHFINDING_ERROR`. Retry/Resume clears that permanent error and performs a fresh reachable-position search. Structures that have no naturally reachable work position currently stop with this explicit error; Builders do not silently create scaffolding.

For placement targets, the Builder resolves material needs before walking to the site. It carries materials in its nine-slot inventory and transfers a bounded stack-sized batch from the Depot without duplicating stock. If the material is absent, the recoverable `WAITING_FOR_MATERIALS` state reports the exact item and cached remaining missing count, and automatically retries when stock appears. Items leave the carried inventory only after successful placement. Air targets never request materials; matching air advances immediately, while occupied air targets are cleared from work reach.

The active status describes the current operation and coordinate (moving and work positions, placing, or clearing). Placement and breaking failures distinguish invalid height, unsupported blocks, occupied targets, protected blocks, invalid territory, and rejected world edits. The same exact failure detail is persisted in the job, synchronized in Builder snapshots, and displayed in both Builder status screens. Progress remains the saved count of completed or already-satisfied schematic cells; path attempts, successful clearing before replacement, and failed edits do not advance it.
