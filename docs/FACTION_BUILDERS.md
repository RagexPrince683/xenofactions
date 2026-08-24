# Faction Builders

## Starting a Builder

The existing city progression awards a **Builder Contract**. Use it on a valid Builder Depot belonging to that city to create the city's persistent Builder. A Depot owns one Builder assignment; unloading the entity does not create a replacement, while confirmed death allows the normal replacement flow to resume the persistent job.

Right-click either the **Builder Depot** or its assigned **Builder NPC** to open the same management screen. The NPC resolves its Depot on the logical server and refuses stale, missing, cross-dimension, or otherwise invalid assignments instead of opening a second copy of job state.

## Management pages

### Overview

Shows the assigned Builder identifier, faction, city, persistent `BuilderState`, active schematic, block progress, progress bar, and the first blocked coordinate. Valid controls recall the Builder to the Depot using normal navigation, pause work, revalidate and resume paused work, or cancel future work. Cancelling never removes blocks already placed.

### Build

Select a schematic from the scrollable Xenofactions library, enter the exact X/Y/Z origin, choose 0/90/180/270-degree rotation and horizontal mirroring, and preview the transformed result client-side. **Validate** reports input problems in the screen; **Start** repeats all authoritative validation on the server, including configured dimensions/block limits and the complete transformed territory footprint. It starts immediately when idle or joins the Depot queue when another job is active.

Native server/bundled `.schematic` files use Xenofactions' strict normalized schematic loader. If optional classic Schematica is installed, **Use Active Schematica** imports its active client schematic. Schematica Plus uses the same `Schematica` mod ID, so Plus-only feature detection controls optional `.litematic` decoding. Neither mod is a server dependency, and all optional-mod reflection remains client-only.

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
