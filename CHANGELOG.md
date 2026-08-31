# Pull request: prevent BOMB buy state from leaking into DEATHMATCH

- Stopped the active BOMB lifecycle before changing the selected map's mode, then started DEATHMATCH only after buy, waiting, spectator, objective, timer, and kit-selection ownership was released.
- Made kit-selection replacement and cancellation release its freeze anchor and TDM-owned protection effects on every path, while retaining independent DEATHMATCH respawn and FFA round-start selections.
- Cleared stale mandatory BOMB buy GUIs when the client receives a non-BOMB status without disabling legitimate mandatory kit selections in the new mode.

# Pull request: restart single-map TDM matches at round end

- Restarted the selected DEATHMATCH map through the full new-match lifecycle when automatic rotation has no alternative map, instead of repeatedly attempting a map vote.
- Preserved normal multi-map voting and single-map skip-vote refusal while clearing stale votes, scores, statistics, buy score, timers, and transient player state during the same-map restart.
- Clarified that DEATHMATCH score limits are score points and that each kill awards 100 points; BOMB limits remain round wins.

# Pull request: schedule TDM packet GUIs on the client thread

- Scheduled the `/tdm menu` GUI transition through Minecraft's Forge 1.7.10 client task queue instead of changing the current screen from the network handler thread.
- Applied the same client-thread scheduling to the TDM map vote GUI packet while preserving packet formats, registration order, and all existing menu behavior.
- Audited TDM GUI packet handlers and confirmed the kit GUI and kit-result transitions already use the client task queue.

# Pull request: fix dedicated-server TDM food restoration

- Replaced client-side-only `FoodStats` setter calls in TDM new-life restoration with the common-side NBT read/write path required by Forge 1.7.10 dedicated servers.
- Kept the existing food tick timer while restoring full hunger and saturation, clearing exhaustion, and preserving all other TDM respawn behavior.

# Pull request: persist bomb-round survivor inventories and enforce round-win limits

- Kept exact survivor inventory and armor stacks between rounds of the same BOMB match while sending every player who died during the live round through the normal fresh-loadout flow.
- Added transient per-round death and explicit first-round lifecycle state, independent of hardcore elimination, and reset it at match, map, mode, disable, vote, and cleanup boundaries.
- Continued purging the CSGO objective bomb on every transition so it can never persist as survivor equipment.
- Defined BOMB score limits as first-team-to-N round wins, retained the default of 13 and existing per-map NBT override, and clarified configuration feedback.
- Ended the match through the single normal map-vote entry point immediately after either team records the winning round.

# Pull request: restore visible TDM bomb-round participants

- Restored every genuine TDM round start and respawn to maximum health, full hunger and saturation, zero exhaustion, and cleared TDM-owned fire and fall state after team-spawn placement.
- Kept eliminated hardcore late joiners as spectators without granting a restored combat life.
- Removed invisibility from server-side buy and respawn-lock protection while preserving freeze, slowdown, resistance, regeneration, interaction, and damage protection behavior.
- Stopped TDM cleanup from removing invisibility effects supplied by unrelated gameplay systems.

# Pull request: make selected TDM maps own player spawning

- Centralized login, respawn, match-start, map-rotation, and bomb-round placement through the selected map's team spawn lookup.
- Added immediate Forge login and respawn placement, with lifecycle retries only when immediate placement cannot complete.
- Made map-specific spawns authoritative, retained legacy global spawns only for maps without map spawn data, and honored cross-dimension spawn coordinates.
- Refused invalid round starts with actionable server errors instead of allowing vanilla worldspawn fallback.

# Pull request: remove TDM spectator camera overrides

- Removed all TDM camera targeting, ownership, and restoration so TDM never overrides the Minecraft camera.
- Kept eliminated-player observer gameplay state without forcing teammate views, preventing interference with vehicle and custom-camera mods such as MCHeli.
- Rewrote the affected TDM client state, HUD, observer manager, and compatibility packet registration for readability while preserving packet discriminator IDs.
# Pull request: preview complete TDM kit contents before selection

- Added slot-accurate, read-only previews of every eligible TDM kit's saved main inventory and armor contents.
- Sent copied item stacks with their metadata and NBT through bounded Forge 1.7.10 packet serialization so real modded tooltips remain available.
- Added a scalable hover preview panel, inventory layout, armor area, kit-list scrolling, empty-kit feedback, and visible unaffordable-kit disabling without changing server authority.

# Native earth2000 raster pregeneration

- Added an external, SHA-256-verified `earth2000` source format and bounded 512-block raster tile reader.
- Added clean Minecraft 1.7.10 terrain conversion and resumable, deterministic Anvil region generation with safe partial-world ownership and final verification.
- Added level-4 `/xc earth source` and `/xc earth pregen` administration commands plus external source-package tooling.
- Documented the dedicated-server bootstrap workflow; the normal JAR continues to contain generator code only, while existing `.xfmap` support remains intact.

# Pull request: isolate TDM spectator camera ownership

- Track the exact client camera Xenofactions assigns and restore the prior valid camera only while Xenofactions still owns the current view.
- Schedule spectator packets on the Minecraft client thread and ignore unchanged periodic targets, preventing camera reacquisition after another mod takes control.
- Preserve teammate spectating while making cleanup, disconnects, world changes, respawns, map votes, and mode transitions safe for vehicle and custom-camera mods such as MCHeli.

# Pull request: enforce hardcore BOMB buy-period isolation

- Made hardcore BOMB `PRE_ROUND` independently freeze and protect every living participant until the authoritative transition to `LIVE`, regardless of whether that player has already selected a kit.
- Made BUY_PHASE selection mandatory, enrolled late hardcore respawns with the correct context, and blocked outgoing attacks, entity interaction, pickups, movement, sprinting, and jumping throughout buy time.
- Preserved non-hardcore per-player `RESPAWN_LOCK` behavior and separated its protection ownership from global buy protection.

# Pull request: restore hardcore-aware TDM respawn kit lifecycle

- Kept the timed global BOMB buy phase exclusively for hardcore-respawn maps; non-hardcore rounds now enter `LIVE` directly and use independent mandatory per-player kit locks.
- Made every real non-hardcore respawn teleport, protect, freeze, and repeatedly enforce an inescapable kit GUI after the death screen, with authoritative damage, interaction, and pickup restrictions.
- Added server movement anchors for buy and respawn protection, explicit kit-selection contexts, safe fallback handling, and deferred one-objective BOMB assignment until an eligible Terrorist's inventory can no longer be replaced by kit application.

# Pull request: contain TDM BOMB buy protection to PRE_ROUND

- Separated the authoritative BOMB buy phase, pending kit choice, and temporary kit protection so stale pending selections can no longer refresh invisibility, slowness, resistance, or regeneration during live combat.
- Made the `PRE_ROUND` to `LIVE` transition resolve free fallbacks, expire unavailable choices, close buy GUIs, synchronize inventories, and remove every TDM-owned kit potion before combat begins.
- Prevented dead entities and late packets from entering kit selection, integrated cleanup with respawn and round boundaries independently of hardcore observer behavior, and documented the two invisibility systems.

# Pull request: classify same-tick HBM CSGO dismantles as CT defuses

- Added the configurable Xenofactions-only `tdmBombUnknownRemovalAsDefuse` compatibility fallback for HBM dismantles that remove `BlockChargeC4CSGO` before `TileEntityCharge`'s brief disarmed state can be sampled.
- Captured charge attachment metadata and enforced HBM's supporting-face solidity rule, while retaining strict `OBJECTIVE_ERROR` behavior for invalid supports, near-expiration removals, and disabled fallback mode.
- Preserved explicit and latched detonation precedence, added optional lifecycle diagnostics and comprehensive unknown-removal logging, and retained ordinary defuse cleanup and fresh one-bomb-per-round assignment.

# Pull request: hard-track the HBM CSGO bomb lifecycle

- Hard-track every accepted CSGO plant by exact block, coordinate, tile instance, lifecycle snapshot, plant tick, and round identity; bounded missing-block resolution prevents permanent `BOMB_PLANTED` rounds.
- Retain coordinate-matched runtime IMC as secondary confirmation, invalidate objectives before Xenofactions cleanup, and add warning-once unknown-removal recovery.
- Add operator recovery command `/tdm forceroundend <red|blue|terrorist|ct|counterterrorist|abort>` with explicit administrative wins or scoreless aborts.
- Document exact registry resolution, runtime lifecycle snapshots, cleanup protection, and recovery behavior.

# Pull request: use authoritative HBM CSGO bomb results

- Added an optional Forge 1.7.10 runtime IMC receiver for HBM's authoritative `DEFUSED` and `DETONATED` CSGO charge lifecycle results.
- Removed ordinary block breaking as a Xenofactions defuse mechanism and kept the tracked objective protected while HBM owns its interaction and removal.
- Replaced missing-block detonation scoring with a bounded, warning-once safe recovery watchdog; disappearance alone can no longer award a BOMB round.
- Documented the sender, message key, NBT fields, lifecycle ownership, validation, and duplicate/stale-message protection in `docs/HBM_CSGO_BOMB_RUNTIME_INTEGRATION.md`.

# Pull request: make tracked BOMB disappearance authoritative

- Detect a validated, planted HBM CSGO charge's disappearance from its tracked world coordinates during the authoritative server tick and complete the round once as a Terrorist detonation win.
- Invalidate the tracked objective before successful defuses and Xenofactions lifecycle cleanup remove its block, preventing those intentional removals from being misclassified as detonations.
- Remove the unreliable Forge explosion-origin match while retaining the exact optional HBM runtime-registry lookup and planted-bomb break protection.

# Pull request: finish BOMB detonation rounds and reset client TDM state

- Complete a planted HBM CSGO charge's round exactly once from its non-cancelled Forge detonation event, awarding Terrorists through the authoritative BOMB result path while preventing manual objective breaks.
- Reset all TDM HUD and spectator-camera state on disconnect, world exit, and authoritative disabled status packets, and apply status packets on the Minecraft client thread.

# Pull request: restore world-border wand selection and scope BOMB interaction freezes

- Made the operator world-border wand receive canceled Forge 1.7.10 interactions and retain cancellation after recording either coordinate.
- Replaced the blanket non-active BOMB world lock with an authoritative phase-specific lock for only `PRE_ROUND` and `ROUND_END`, while leaving `WAITING_FOR_TEAMS`, `DISABLED`, and map voting editable subject to normal protections.
- Added a narrow TDM bypass for the exact authorized world-border wand, preserved BOMB planting and defusing paths, and kept observer cleanup when returning to the team-waiting state.

# Pull request: reconcile runtime TDM modes and add explicit BOMB testing

- Added authoritative active-map mode transitions that reset the old lifecycle, scores, spectators, buy state, and objective state before immediately starting the newly selected mode; inactive-map configuration remains side-effect free.
- Added an operator-only, transient `/tdm bombtest on|off|status` override that relaxes only the minimum-player gate, preserves production two-team waiting, and suppresses absent-team elimination awards.
- Replaced raw BOMB lifecycle enum HUD text and distinguished waiting-state server player counts from BOMB round-win scores.

# Pull request: fix Forge 1.7.10 TDM packet scheduling

- Replaced the unavailable newer-version `MinecraftServer` scheduled-task call with a TDM-owned Java 8 task queue drained once at the start of each server tick.
- Kept kit purchases and menu actions server-authoritative while rejecting disconnected players before queued work mutates their state.
- Clear pending TDM packet work when the server overworld unloads so integrated-server sessions cannot inherit stale actions.

# Pull request: fix TDM bomb buy-menu selection lifecycle

- Added an authoritative server-to-client kit-selection result so successful bomb purchases close the buy GUI while recoverable failures keep it usable with specific feedback.
- Made buy-menu clicks wait for acknowledgement, restored Escape handling, added a server-timed visible countdown, and exposed buy-menu reopening through the existing TDM menu.
- Added an explicit one-team waiting state so bomb matches begin one clean buy phase only after both teams have an eligible player.

# Pull request: fix world-border exemption administration and exits

- Replaced named exemption management with the selection-driven `/xc worldborder exempt` workflow and collision-safe internal persistence keys.
- Added `/xc worldborder clearexemptions`, preserving runtime border state and safely returning affected out-of-map players to the configured center.
- Added UUID-based exemption transition tracking so exemption exits return players to a terrain-safe map center while normal crossings keep legacy wrapping.
- Retained backward-compatible loading of PR #199 named regions and documented persistent, inclusive, all-Y behavior.

# Pull request: add persistent runtime Earth boundary controls

- Added operator-only `/xc worldborder` runtime enable, disable, status, selection-wand, and named exemption administration.
- Persisted explicit runtime state and validated, dimension-specific inclusive X/Z exemption columns in world saved data, with the configured enable value retained as the old-world fallback.
- Integrated exemption checks into the existing authoritative wrap/recovery path without changing faction, protection, TDM, Builder, or JourneyMap behavior.
- Documented server commands, all-Y wand selection, persistence, and the Earth-boundary-only scope of exemptions.

# Pull request: assign the HBM CSGO bomb to a Terrorist

- Assigned one exact HBM CSGO charge to a randomized, eligible Terrorist after bomb-round kit setup and the authoritative transition to live play.
- Restricted carriers to active, non-spectating players on the configured Terrorist team in the bomb world's dimension, with randomized retries for full inventories.
- Added cached, optional inventory-item resolution for both supported HBM CSGO charge registry names without linking HBM classes.

# Pull request: add TDM bomb rounds and hardcore observers

- Added per-map DEATHMATCH/BOMB modes while retaining RED and BLUE as player teams and deriving fixed Terrorist/Counter-Terrorist roles from map configuration.
- Added persistent A/B bombsite cuboids, separate bomb win/loss scoring and mode-specific score/timer overrides (13 wins and 120 seconds by default).
- Added server-authoritative hardcore elimination, restricted 1.7.10 observer behavior, teammate camera packets, bomb HUD synchronization, and cleanup at round/match/map-vote boundaries.
- Preserved PR #195's exact optional HBM registry lookup and Warzone exception; BOMB maps fail closed when `hbm:tile.charge_c4csgo` is unavailable.

# Pull request: allow HBM CSGO bombs in Warzones and add per-map TDM limits

- Added an optional, cached registry lookup for HBM's exact CSGO charge block and allowed only that block through Warzone placement protection.
- Added backward-compatible per-map TDM score-limit and round-timer overrides with global fallbacks of 10,000 points and 20 minutes.
- Added `/tdm map scorelimit` and `/tdm map timer` administration commands, effective-setting map listings, help text, and documentation.

# Pull request: fix bottom-up reachable Builder construction

- Versioned Builder work order and migrated active jobs to deterministic bottom-up Y/Z/X scheduling using the current world as construction truth.
- Deferred temporarily unreachable targets while scanning bounded layer passes, with complete-pass failure diagnostics and Resume state reset.
- Expanded collision-aware stair/slab work positions, face-based reach checks, placement self-collision protection, path diagnostics, and world-derived GUI progress.

# Pull request: make Builder NPC GUI opening race-safe

- Made the client construct the fixed 63-slot Builder NPC container from GUI-open coordinates even when Depot assignment, tile, or entity synchronization is late.
- Kept placeholder containers open client-side while preserving strict real-Builder, Depot, range, and faction validation on the server.
- Added debug-only GUI lifecycle diagnostics and structural checks for the vanilla 45-slot player-to-63-slot Builder window transition.

# Pull request: fix Builder NPC container synchronization

- Unified Builder inventory sizing and container slot boundaries around the persistent 27-slot worker inventory.
- Added shared Depot assignment resolution and a 27-slot client synchronization inventory for the short entity-tracking race during GUI opening.
- Hardened Builder shift-click bounds and server interaction validation while retaining the complete status and control screen.

# Pull request: fix Builder navigation and schematic preview alignment

- Enabled the Forge 1.7.10 `EntityLiving` AI lifecycle so vanilla navigation and movement helpers physically move Builder NPCs exactly once per tick.
- Configured swimming/water traversal, broadened feet-position searches, added direct reach actions, bounded distinct path retries, and movement/path diagnostics.
- Aligned inventory-style preview geometry to exact world block cubes and documented the normalized local origin shared by preview and construction.

# Pull request: fix Builder reach-based construction and movement stalls

- Replaced exact-target navigation with collision-safe reachable work positions and a bounded movement watchdog with alternate-path retries.
- Added pre-site bulk material fetching, remaining-quantity requests, automatic temporary-state recovery, and placement-only consumption.
- Added precise break, placement, territory, unsupported-block, and path failure diagnostics to persistent jobs and both Builder status screens.
- Documented the four-block work reach and that Builders currently require no tools.

# Pull request: fix Builder Depot coordinate editing and preview placement

- Initialized new and unselected legacy Builder plans near their owning Depot while preserving deliberately selected world-origin plans.
- Made absolute world-coordinate fields editable and debounce-persistent, with Depot reset and per-axis adjustment controls.
- Moved persistent previews into camera-relative world rendering and made final native jobs consume the same server-owned plan.
- Added placement diagnostics, loading feedback, localization, and Builder planning documentation.

# Pull request: live-refresh native Builder schematics

- Added one server-owned native schematic library rooted at Forge’s `config/schematics/` directory, with snapshot-based live reload and actionable load diagnostics.
- Synchronized native schematic metadata and stable IDs to the Builder selector, including automatic polling and a manual Refresh control.
- Removed numeric underscore filename parsing while preserving strict schematic validation and persistent job copies.
- Documented arbitrary native filenames, the runtime directory, and live refresh behavior.

# Builder GUI redesign

- Split Builder worker status/control from Builder Depot planning, materials, and queue management.
- Added page-aware inventory slots, safe two-way Depot shift-click merging, real GUI buttons, searchable schematic selection, localized state text, and coordinate-qualified client snapshots.
- Documented the new Builder screens and selection workflow.

# Pull request: add the Builder city start flow

- Granted one Builder Contract to a founder only after successful XF city creation, with a safe full-inventory drop fallback.
- Restored the existing Builder machine as an obtainable, craftable Builder Depot and connected contracts to faction-authorized persistent Builder assignment.
- Persisted the two-way depot, faction, Builder, dimension, city-job association and safely paused jobs on Builder death or depot removal.
- Documented that the XF city remains the Town Hall equivalent and no additional Town Hall is required.

# Pull request: fix Builder NPC Forge 1.7.10 compilation

- Resolved the shared Builder job database through Forge 1.7.10's server-side dimension manager and made unavailable or client-world lookups fail safely.
- Restored the Minecraft item import used by the Builder's existing door and bed material mappings.

# Pull request: secure and distribute Wall Art controllers

- Made Wall Art configuration opening server-authoritative and restricted it to the controller's UUID owner, with localized denial feedback.
- Added the missing Wall Art block display name and a localized `/xwallart` confirmation.
- Added the permission-free `/xwallart` player command, including safe item dropping when the inventory is full.

# Pull request: fix Wall Art collision and wall orientation

- Made Wall Art non-colliding like vanilla 1.7.10 vines while retaining its thin metadata-oriented selection and ray-trace bounds.
- Derived the controller's supporting-wall facing from the clicked horizontal face, rejected vertical attachment, and stopped player yaw from overwriting placement metadata.
- Added clicked-side support validation, centralized placement/support direction mapping, and replaced invalid metadata's full-cube bounds with a safe thin panel.

# Pull request: synchronize Wall Art controller visibility

- Invalidated cached client block geometry when synchronized Wall Art image, facing, or display dimensions change, so configured controllers disappear and cleared controllers reappear immediately.
- Explicitly synchronized newly placed Wall Art tile metadata after server-side ownership and facing initialization.
- Preserved hash-driven controller visibility, thin interaction and inventory geometry, and the existing tile renderer and server update path.

# Pull request: fix Wall Art block rendering

- Hid configured Wall Art controller geometry while preserving the downloaded-image TESR and the logical block's interaction bounds.
- Added a dedicated Forge 1.7.10 block renderer for metadata-oriented thin fallback panels in the world.
- Rendered the Wall Art inventory item as a fixed, thin vertical panel using its neutral fallback texture.

# Pull request: re-audit Wall Art importing against ImageFrame

- Added an independent Wall Art source policy with correct Postimages page/CDN defaults, bounded source dimensions and bytes, manual redirect limits, and separate success/failure cooldowns.
- Added bounded HTML image-page resolution for secure OpenGraph metadata and a limited image fallback, while revalidating every resolved URL and redirect with the existing SSRF protections.
- Switched Wall Art requests to a browser-compatible user agent, retained byte-level PNG/JPEG verification and memory-efficient source subsampling, and improved actionable HTTP, resolver, timeout, size, and decode errors.
- Refactored the asynchronous import lifecycle and all Wall Art-specific source files for readable Java, accurate in-progress/cooldown messages, short failure retries, sanitized diagnostics, and main-thread-only world updates.

# Pull request: fix Wall Art image importing

- Separated bounded Wall Art source downloads and source-dimension safety checks from the much smaller custom faction flag import limits.
- Added metadata-first PNG/JPEG decoding with source subsampling so normal high-resolution photographs are efficiently scaled into the selected 128x128 through 640x640 Wall Art canvas.
- Improved direct HTTPS image handling with byte-based format detection, query-safe parsed-host validation, per-redirect SSRF checks, bounded streamed downloads, image-oriented request headers, and clearer errors for HTML or unsupported payloads.
- Retained server-worker processing, final-image hashing, bounded PNG storage and transfer, and the existing HTTPS host whitelist.

# Pull request: fix Wall Art block and configuration GUI

- Centered and restored the Wall Art URL, display-size, and download controls by separating display dimensions from the inherited GUI screen dimensions.
- Preserved URL text and selected dimensions across GUI resizing, and added an inline prompt for empty submissions.
- Replaced the missing fallback texture reference with the existing neutral concrete panel texture while retaining dynamic image rendering.
- Made wall-mounted controllers drop when their supporting wall is removed using Forge 1.7.10 side-solid checks.

# Pull request: fix Forge 1.7.10 Wall Art compilation

- Updated Wall Art support checks to use Forge 1.7.10 `ForgeDirection` values for each neighboring wall face.
- Removed the unreferenced legacy image-placement helper that still wrote deleted URL, name, and string-owner fields directly into Wall Art tile entities.
- Preserved the server-authoritative Wall Art service, metadata-only tile entities, and the `wall_image_block` registry identity.

# Pull request: fix development runClient identities

- Restored GTNH's blank-name `Developer<random numbers>` behavior for default development clients.
- Assigned one fresh random UUID during each Gradle invocation before the run task finalizes its properties, while retaining validated independent `devUsername` and `devUuid` overrides.
- Kept `runClient` identity logging read-only so task execution no longer attempts to mutate finalized Gradle properties.

# Pull request: isolate faction saves and randomize development players

- Reset all faction, membership, diplomacy, claim, cooldown, protection, and overlay runtime state at the logical-server overworld lifecycle boundary, while retaining one shared database across dimensions.
- Bound cached faction data and auxiliary JSON persistence to the active save so a new or reopened world cannot inherit another world's state.
- Give each `runClient` execution a validated random development username and UUID by default, while preserving independent `devUsername` and `devUuid` overrides.

# Pull request: fix JourneyMap minimap claim clipping

- Kept JourneyMap's depth-based minimap mask active while faction claim fills, borders, and labels render, preventing overlay geometry from escaping circular and rectangular minimap interiors without adding a separate clipping system.
- Preserved fullscreen claim rendering and the corrected legacy territory-coordinate bounds by limiting the change to Xenofactions-owned OpenGL state.

# Pull request: fix JourneyMap faction claim alignment

- Corrected JourneyMap claim polygons, exposed borders, and territory-label bounds to use the exact inverse of Xenofactions' legacy `+1` and truncating integer coordinate conversion instead of treating stored territory coordinates as vanilla chunks.
- Covered positive, negative, axis-crossing, and near-origin territory bounds with focused regression tests while preserving real flag-block label positions and the reflection-only JourneyMap integration.

# Pull request: show command-specific help in the Xenofactions GUI

- Replaced generic legacy-execution text in the Xenofactions command GUI with concise descriptions of each command's actual behavior.
- Aligned faction and administrator GUI help with the current grouped `/c` and `/xc` commands and clarified utility-command documentation.

# Pull request: complete Xenofactions command GUI localization

- Completed localization for the remaining Admin and TDM command GUI entries, including City Center cancel-move and recover-move commands.
- Added source-level validation to require an English name and description for every command catalog entry.

# Pull request: fix Earth chunk-check command compilation

- Qualified the Minecraft command integer parser used by `/xc earth check`, allowing the standalone Earth command handler to compile while retaining standard invalid-coordinate command errors.

# Pull request: restore canonical faction roles after UUID identity refactor

- Fixed global chat and the XF menu to resolve live players through their canonical `FactionMemberRecord`, so UUID-mode owners and officers no longer appear as citizens.
- Centralized role display, owner name, member count, and member-list data on canonical records rather than deprecated username collections.
- Corrected promote, demote, ownership-target, kick, leave, merge, disband, and online membership paths so permissions and membership changes take effect immediately and persist from UUID records.
- Preserved NAME and AUTO identity behavior by resolving both modes to the same member record before checking its `FactionRole`; username-only permission lookup remains fail-closed outside NAME mode.
- Retained legacy leader/member/officer fields strictly as old-save migration and compatibility mirrors.

# PR: Command namespace cleanup

- Grouped recent player faction commands under `/c` and administrator/Earth commands under `/xc`, removing their standalone roots.

# Pull request: complete the Earth Map pack installer and bundled release workflow

- Renamed and expanded the dimension-0 world type to `earthmap`, added its Singleplayer customization and asynchronous create/install flow, and retained non-generating missing-chunk and profile-border behavior.
- Added the secure, hashed `.xfmap` repository, bundled cache, verifier, cancellation-safe installer, template `level.dat` patcher, local/external provider support, and content-mod compatibility checks.
- Added deterministic Python map-pack tooling and tests, opt-in bundled-release Gradle tasks, private map input documentation, and full user/operator/format documentation without committing map binaries.

# Pull request: add template-backed XenoFactions Earth world type

- Registered the `xf_earth` dimension-0 world type and a non-generating missing-chunk fallback for externally exported WorldPainter Anvil saves.
- Added validated, save-cached Earth profiles, startup adoption checks, profile-backed reuse of the existing border, operator status/check commands, localization, configuration, and installation documentation.
- Kept Earth in the ordinary overworld without a custom dimension or world provider and without bundling map binaries.

# Pull request: add faction creation cooldown reset command

- Added the admin-only `/xc factiontimeoutcreationreset <playername>` command to reset only one online or stored offline player's faction creation cooldown with immediate persistence.

# Pull request: rebuild territory after City Center relocation

- City Center relocation now rebuilds the city's circular territory around the destination and removes stale territory around the old center.

# Pull request: remove orphaned City Centers safely

- Orphaned City Centers left behind by disbanded factions can now be broken and removed without dropping a City Center item or starting relocation.

# Pull request: prevent relocated City Center GUI crashes

- Changed the slotless City Center interface into an informational screen so opening it no longer replaces the player's active inventory container or crashes after relocation.
- Made successful relocation token consumption clear and immediately synchronize the authorized server inventory slot, without consuming tokens for failed moves.

# Pull request: configurable development identity and cooldown administration

- Added centralized `AUTO`/`UUID`/`NAME` faction identity resolution while retaining UUID-secure packaged online production behavior.
- Added stable `devUsername`/`devUuid` client properties and documented GTNH/RFG development launch behavior.
- Added `/xc clearcreationcooldown <player-or-uuid>` and its reset alias with offline lookup, ambiguity protection, completion, and immediate persistence.
- Made creation cooldown lookup and expired-entry cleanup follow the effective identity mode.

## Safe two-phase City Center relocation

- Replaced normal owner City Center breaking with an owner-only confirmation popup and persistent, server-authoritative pending relocation. The old center and claims remain active until an atomic placement succeeds.
- Added stable city UUIDs, bound/recoverable relocation tokens, same-dimension and full-radius validation, protected-zone/city collision checks, rollback, preserved tile inventory and city level/name, guarded old-block removal, and translated faction homes.
- Guaranteed that a faction with zero live cities may found its first settlement for zero Prestige and without spare upkeep capacity; normal settlement upkeep is still added after placement.
- Added `/c city cancelmove` and `/c city recovermove` to command help, completion, command GUI data, and documentation.

## Build grace home validation, city Prestige scaling, and regular disband penalties

- Added server-side build grace home validation so `/c gracebuild` requires an explicitly set faction home that still resolves to owned faction territory, and active grace ends when that home claim is lost or transferred.
- Corrected City Center founding cost scaling to use the current distinct owned city count instead of stale historical `citiesFounded` data, with city removal and transfer paths reconciling ownership.
- Finished regular `/c disband <faction name>` for faction leaders and added UUID-backed faction creation cooldown persistence: 7 real days for the disbanding leader and 3 real days for other current members, blocking only faction creation.

## Fix NEI machine page text layout

- Moved selected machine NEI information text below the item row while preserving slot, arrow, recipe, usage, catalyst, and machine value behavior.
- Trimmed below-row machine information to avoid the NEI side controls and kept Foundry and Stone Drops layouts unchanged.

## Fix NEI machine handler reflection constructors

- Split the shared Xenofactions machine NEI implementation into public per-category handlers with zero-argument constructors so GTNH NEI can recreate registered handlers reflectively during recipe and usage lookup.
- Kept the shared machine recipe behavior, overlay identifiers, catalysts, rendering, copied `ItemStack` handling, and Stone Drops NEI handler behavior intact.

## Fix NEI machine handler compile error

- Added explicit braces to the machine NEI recipe loading control flow to prevent dangling `else` binding in Foundry Casting and Fishing Net lookups.
- Preserved existing Blast Furnace, Foundry, Fishing Net, Windmill, and Stone Drop NEI display behavior.


## Remove Stone Drops NEI crafting grid background

- Replaced the inherited vanilla crafting table recipe background on Xenofactions Stone Drops NEI pages with a texture-free custom slot-and-arrow background.
- Applied the same explicit background to the custom Xenofactions machine NEI handler that also inherited the unrelated vanilla crafting table texture.

## Optional Not Enough Items integration

- Added development-only Not Enough Items and CodeChickenCore dependencies through GTNH Convention's non-publishable configuration while preserving the existing `devmods` remapping flow.
- Added an optional, client-only NEI integration entry point and a Xenofactions stone-drop recipe/usage category.
- Added server-authoritative stone-drop display snapshot synchronization for multiplayer NEI displays.
- Added the `enableNEIIntegration` module option and documented the new configuration key.

# Xenofactions command GUI and first-join tutorial

- Added a client-side Xenofactions command GUI with category navigation, command search, command preview, dangerous-command confirmation, Open in Chat, and Copy Usage actions.
- Added a configurable `Open Xenofactions Menu` key binding in the Xenofactions controls category, defaulting to `K`.
- Added server-backed menu data packets for safe dynamic command options and operator/TDM/config availability.
- Added a first-join tutorial explaining the GUI, dynamic forms, command preview, server-side permission validation, and continued support for legacy slash commands.
- The tutorial completion flag is stored exactly at `new File(Minecraft.getMinecraft().mcDataDir, "tutorialflag")`, outside the `config` directory and without an extension.
- Legacy commands remain supported and server permissions remain authoritative; the GUI sends commands through the normal chat command path.

# Enemy faction relations and Prestige rewards

- Added persistent one-way enemy faction relations using faction UUIDs, with `/c enemy <faction>` and delayed `/c unenemy <faction>` commands for leaders.
- Added configured Prestige rewards for killing active enemy faction members.
- Updated nameplates so own/allied/c enemy/neutral factions render green/blue/red/yellow.

# Add public stone drops list command
Added `/c stonedrops [page]` as a read-only player-accessible command for viewing configured custom stone drops without changing the administrator `/stonedrop` command.
- Documented chance percentages, raw chance values, item registry metadata, stack amounts, and Y ranges in the paginated stone-drop output.

# Fix automatic HBM stone-drop integration

- Fixed the real automatic HBM stone-drop failure: Xenofactions now resolves HBM's documented `modid:item metadata minimumAmount maximumAmount` specifications through the Forge 1.7.10 item/block registries after HBM post-initialization instead of relying on fragile direct item lookups.
- Added safe recovery for missing, empty, malformed, and intentionally empty `config/stonedrops.json` states, including atomic replacement and malformed-file backup behavior.
- Preserved administrator-defined stone drops while appending missing automatic HBM drops without duplicate item/metadata/NBT entries, and kept the parallel runtime drop lists size-aligned.
- Logged exact invalid HBM specifications, debug details for resolved entries, and the success message `[XF] Registered <count> automatic HBM stone drops from MiningConfig.excavatorBedrockDrops.`.
- Prevented custom stone drops from being produced on the client side or by creative-mode stone breaking while retaining the existing stone-only, chance, Silk Touch, and Fortune behavior.

# Fix flag beacon renderer state restoration

- Reworked `FlagBeamRenderer` to use OpenGL attribute stacks instead of querying individual render states, removing the `GL_CURRENT_COLOR`/`FloatBuffer` path that could crash LWJGL 2.
- Kept the full-height flag beacon centered on the flag block with tile-entity-relative translation and local beam vertices around X/Z `0.5`.

# Fix flag beam color buffer collision

- Renamed the saved OpenGL color buffer in `FlagBeamRenderer` to avoid colliding with the existing render color parameter while preserving beam rendering and color restoration.

# Fix flag beacon coordinate space

- Moved normal and large flag beacon rendering before the flag model translation and metadata rotation so tile entity render coordinates are applied exactly once.
- Removed the duplicate large-flag `renderFlare` beacon implementation in favor of the shared full-height `FlagBeamRenderer`.
- Preserved the shared beam world Y 0 through Y 256 span and restored OpenGL render state after beam rendering.

# Fix duplicate flag render bounding boxes

- Removed duplicate flag tile entity render bounding box overrides that returned an infinite extent.
- Kept the beacon-sized render bounding boxes from world Y 0 through Y 256 for both city and territory flags.

# Fix flag obstruction checks and add flag beams

- Fixed flag obstruction placement checks to compare the placed block Y coordinate against nearby flag block Y coordinates instead of using the column surface height, allowing blocks below flags while preserving above-flag obstruction protection.
- Added client-only translucent beacon beams for loaded city and territory flags using the vanilla beacon beam texture and the owning faction color.
- Expanded flag tile entity render bounds from world Y 0 through Y 256 so the full beam remains visible after culling and chunk reloads.

# Add JourneyMap city territory labels

- Added JourneyMap city territory labels that render once per city territory on enabled minimap and fullscreen claim overlays, using city names instead of internal faction/group IDs.
- Extended authoritative claim overlay snapshots with bounded city-label text and stable label positions, including flag positions when available and territory-center fallbacks.
- Hardened claim overlay packet decoding for bounded labels and malformed multipart data while preserving the optional JourneyMap 5.2.x reflection bridge.

# Remap production mods in the development client

- Migrated the legacy ForgeGradle 1.2 build to the GTNH convention and
  RetroFuturaGradle setup used by MCH-mocmaster while preserving Minecraft
  1.7.10, Forge 10.13.4.1614, MCP stable 12, and Java 8 output.
- Added the `devmods` workflow for automatic SRG-to-MCP remapping, with explicit
  `devmods/deobf` handling for jars that are already development-mapped.
- Kept generated jars under `build`, excluded development mods from publishing,
  and added a Gradle-backed IDE client run configuration and documentation.

# Pull request: restore safe, repeatable City Center relocation

- Restored the City Center break warning popup, including its confirmation and cancel actions, while retaining the safe read-only right-click City Center GUI and inventory synchronization.
- Restricted starting and completing relocation to the faction owner using persisted UUID identity; officers, members, outsiders, fake players, and automation are rejected.
- Limited each stable city ID to three successful moves in a rolling seven-day (168-hour) window, with a 30-minute wait between the second and third successful moves.
- Changed relocation pricing to zero Prestige through 10 horizontal blocks and `ceil(horizontal distance - 10) * 30` Prestige beyond that distance, with no base charge.
- Required the destination to remain within territory belonging to the moved stable city ID and prevented overlap with other cities, factions, safe zones, and war zones.
- Preserved the stable city ID, faction UUID, city name, city level, inventory, flag state, and all matching city metadata across repeated relocation; rollback restores the original City Center without charging Prestige, consuming the token, or recording a move.
- Restored the pre-`f3a05f5` territory coordinate lookup, claim generation, and city-spacing behavior and removed the coordinate-boundary regression test introduced with that failed fix.

# Fix claim overlay packet decoding for Forge 1.7.10

- Replaced an unsupported Netty `ByteBufUtil.getBytes` call with sequential `ByteBuf.readBytes` decoding while preserving packet validation and wire format.

# Seed stone drops from an installed HBM MiningConfig

- Added optional, reflection-only discovery of HBM's `com.hbm.config.MiningConfig` and its `excavatorBedrockDrops` list.
- Automatically creates `config/stonedrops.json` only when the file does not already exist, preserving all administrator customizations and intentionally empty configurations.
- Assigned harder HBM minerals to deeper Y ranges and progressively softer deposits to higher ranges, with a one-percent stone-drop chance.
- Documented HBM default seeding and its non-overwriting behavior in the configuration reference.

# Implement optional JourneyMap 1.7.10 faction claim overlays

- Added an optional, client-only reflection bridge for legacy JourneyMap 5.2.x/5.2.8 without imports, binaries, patches, or a required dependency. It preserves separate minimap and fullscreen `DrawStep` proxies across JourneyMap list clears and soft resets.
- Added authoritative, bounded server-to-client dimension snapshots on login, respawn, dimension change, and debounced territory changes. Client snapshots are immutable, dimension-indexed, size-limited, and use packed chunk keys for constant-time perimeter checks.
- Rendered faction-colored 16x16 claim fills and exposed city/faction-group perimeter edges inside JourneyMap's active transform and clipping mask, with viewport culling and restoration of changed OpenGL state.
- Added minimap/fullscreen toggles, fill/border opacity, and border width configuration. Missing or incompatible JourneyMap internals log once and disable only the optional overlay.
- Manual verification remains required with Forge 1.7.10 and JourneyMap 5.2.8 for dedicated/client startup with and without JourneyMap; multiplayer snapshot loading; faction colors and chunk alignment; rectangular/circular clipping; rotation, zoom, follow, fullscreen pan; live claim refresh; dimension/disconnect cache clearing; soft reset preservation; simulated reflection failure; and entity/waypoint ordering. `git diff --check` was run separately.

# Gate noisy runtime logging behind debug config

- Added `enableDebugLogging` in `XENOFACTIONS_17_DEBUG_LOGGING` for verbose development and runtime trace messages.
- Routed registration confirmations, OreDictionary success chatter, market packet traces, PON4 task confirmations, multiblock diagnostics, and other repetitive debug prints through the new debug gate.
- Kept warnings/errors and useful operational events visible without debug logging, including missing optional MCHeli OreDictionary integration warnings and market rename notices.
- Updated server administration documentation to explain the normal-vs-debug logging policy.

# Persist war state and make faction claims dimension-aware

- Fixed warp tents and medical tents in non-overworld dimensions by resolving their owning claim with the tile entity's actual world dimension instead of the implicit overworld.
- Fixed statue and other prestige-building owner detection in non-overworld dimensions so prestige generation is credited to the faction that owns that dimension's claim.
- Fixed prestige-building break cleanup to use the block's world dimension when subtracting generation, preventing non-overworld buildings from looking up overworld claims.
- Added immediate City Center prestige UI refresh when a faction's prestige generation changes, without changing the prestige tick/earning interval.
- Added a City Center GUI `Gain: <amount>/h` line so players can see whether statues and other prestige buildings changed generation.
- Restored ally object references from saved ally names during Clowder NBT load and synchronized ally names before saving, fixing alliances disappearing from runtime commands after server restart.
- Kept the prior dimension-aware city/admin-claim and war-runtime persistence work from this PR.

- Removed dimension-hostile sky visibility checks from tents, statues, and foundation-based machines; operational checks now require the foundation footprint and a clear obstruction plane above the structure instead of vanilla sky access.
- Refreshed city metadata from loaded City Center tile entities before listing cities so `/c info` reports upgraded city levels/radii immediately and includes current prestige generation/net per hour.
- Added allies to `/c info` and added quick `/c allies` and `/c allylist` aliases for checking current allies and pending alliance offers.

## Manual verification notes

- In a Nether/End/LOTR/Middle-earth claim, place a warp tent on valid foundation blocks with the area above the structure clear, run `/c setwarp <name>`, restart, and verify `/c warp <name>` still targets that dimension.
- In a non-overworld city claim, place a statue and each enabled prestige building, then open the City Center GUI and run `/c info`; verify the GUI `Gain` line and `/c info` generation/net lines update within a tick or two without waiting for the hourly prestige interval.
- Break those prestige buildings in the same non-overworld claim and verify the City Center GUI and `/c info` generation drop immediately.
- Upgrade a city, run `/c info`, and verify the city level and radius match the upgraded City Center immediately.
- Create and accept an alliance, restart the server, and verify `/c info`, `/c allies`, `/c alliance`, `/c allywarp`, and ally war defense checks still recognize the ally.
- Load a legacy world missing dimension fields and verify legacy homes/warps still migrate to dimension `0` with clear server log messages.

## Remaining risky assumptions

- Some legacy helper overloads still intentionally default to dimension `0`; new gameplay code should prefer `World`/dimension-aware overloads.
- The City Center GUI now displays prestige generation (`/h`) immediately, but actual prestige balance accrual still follows the configured prestige update interval by design. Foundation-based structures no longer require vanilla sky visibility, but they still require their footprint foundation blocks and a clear obstruction plane above the structure.

## JourneyMap Claim Overlay Rendering Fix

- Fixed the JourneyMap claim overlay OpenGL color backup to satisfy LWJGL 2 generic `glGetFloat` buffer sizing requirements.
- Kept JourneyMap claim overlays active after recoverable render-state failures while preserving fatal reflection compatibility shutdown behavior.
- Hardened the JourneyMap draw-step list wrapper so JourneyMap list refreshes cannot remove or replace the claim overlay.

# Extend optional NEI machine integration

- Added optional NEI machine categories for Blast Furnace, Steel Foundry melting, Steel Foundry casting, Fishing Net, and Windmill while preserving the existing stone drop integration.
- Exposed shared machine display and recipe data for NEI without requiring NEI outside `com.hfr.nei`.
- Added server-authoritative machine display snapshot synchronization and client disconnect clearing.
- Added localization keys for the new NEI categories and labels.

# NEI Machine Integration Update

- Added NEI categories, catalysts, display data, and server-synchronized configuration values for the Grain Mill, University, Production Line, Temple, and Coal Mine.
- Removed Windmill NEI category, handler registration, handler class, localization, and display snapshot field while leaving Windmill gameplay and registration unchanged.
- Added display snapshot packet validation for incomplete payloads and documented packet field order.
## UUID faction identity refactor

- Added faction identity data version `2`, with UUID-keyed member and application records containing display-only last-known names, join timestamps, and explicit roles.
- Faction lookup and permission APIs now authorize `OWNER`, `OFFICER`, and `MEMBER` roles by UUID. Username-only permission calls fail closed so a renamed or recycled account name cannot inherit access.
- Claimed starter-flag identities and disband cooldown member snapshots now use UUIDs.
- New-format loading validates UUID strings and ignores malformed records rather than granting access. Legacy name fields remain import-only staging data and are not consulted by player lookup.
- Offline-mode servers continue to use UUIDs, but their UUIDs are derived from usernames; consequently, offline mode cannot reliably preserve identity across a username change.
- Operator recovery syntax for unresolved migration entries is `/xc migration list <faction>` and `/xc migration bind <faction> <legacy-name> <online-player>`.
# Server-safe Wall Art system

- Replaced the legacy client-side Wall Image downloader and per-player URL list with a server-authoritative Wall Art controller.
- Added 1x1 through 5x5 display sizing, per-save UUID ownership and a 30-display quota shared across dimensions.
- Added bounded asynchronous HTTPS validation, save-local SHA-256 image deduplication, indexed overlap/reference records, and orphan cleanup.
- Added validated, chunked image transfer and a shared 64 MiB client texture cache with disconnect cleanup.
- Preserved the `wall_image_block` registry name while migrating unsupported legacy tile metadata to an unconfigured display.

# (55d7c30 Make core Xenofactions infrastructure survival-craftable)

- Audited registered blocks/items against faction, city, claim, war, prestige, machine, GUI, command, and tile-entity usage instead of generating recipes from the registries.
- Added eleven centralized, ordinary Forge recipes for City Centers, Conquest Flags, Officer Chests, Medical and Warp Tents, Coal Mines, Production Lines, Universities, Federal Reserves, Temples, and Statues.
- Added the default-enabled `enableSurvivalRecipes` server toggle; disabling it omits only the new recipes and preserves the previous free `/c claim` token workflow for command/shop-oriented servers.
- Made the enabled survival workflow consume one crafted, untagged City Center when `/c claim <city name>` creates its named placement token, without consuming relocation or already-named tokens.
- Documented recipe layouts, balance tiers, dependency safety, the survival-worthy audit, and deliberate exclusions.

# Xenofactions territory permissions and hopper automation

## a4543aa Add faction permissions and hopper automation

- Deprecated the registered Federal Reserve without removing its save-compatible registry entry; removed its recipe, creative exposure, Prestige generation, output, lore, mace role, and GUI access.
- Added the centralized, config-gated four-Foundation survival recipe and made vanilla hoppers valid through the shared foundation predicate.
- Added safe sided hopper contracts for the Grain Mill, Blast Furnace, Coal Mine, and Production Line after auditing the existing Foundry and unfinished/non-processing machines.
- Added faction-owned ally and neutral BUILD, DESTROY, CONTAINER, INTERACT, and SWITCH policies, persistent migration-safe defaults, centralized event resolution, officer commands, and tab completion.
- Documented recipes, legacy behavior, automation coverage, permission defaults, precedence, commands, and migration behavior.
# Faction Builder NPC system

* Added persistent faction Builder entities and entity-independent construction jobs.
* Converted the existing Builder machine into a 27-slot material depot while retaining its registry and legacy slot/NBT data.
* Added bounded, navigated survival construction with material consumption, protected-block checks, and Xenofactions territory permissions.
* Added Builder configuration, localization, and operator documentation. Schematic client compatibility remains reserved for Task 2.
# Pull request: add secure Builder schematic integrations and transforms

- Normalized Builder schematics around registry identities with strict native `.schematic` validation and bounded registry-name preview payloads.
- Added shared rotation/mirroring transforms, conservative schematic limits, protected-block checks, and bounded upload sessions with expiry, duplicate rejection, and server hashes.
- Added optional client-only reflective adapters for classic Schematica and Schematica Plus, including conditional Plus-provided `.litematic` loading without a hard dependency.
- Documented the depot workflow, integration availability, limits, transforms, and deliberately unsupported tile/entity data.
# Builder Depot management and persistent job flow repair

- Replaced the obsolete Builder wrench/offer chest screen with original Overview, Build, Materials, and Queue management pages.
- Added server-authoritative Builder snapshots and actions for job creation, validation, pause, resume, cancel, recall, and queue management.
- Added Builder NPC interaction with its assigned Depot and persistent normalized schematic storage for restart-safe jobs.
- Moved preview placement, Y origin, rotation, and mirroring into the Build page and removed sneak-click preview movement.
- Kept Depot slot 0 as hidden legacy storage while exposing material slots 1 through 27 only.

# Pull request: persist and explain the Builder planning workflow

- Persisted server-authoritative Depot draft selection, origin, transform, preview, and validation state with validated synchronization packets.
- Added bounded, chunked, integrity-checked native schematic preview delivery and client caching.
- Added canonical identity-based material/unsupported-block accounting and unsupported mapping preflight.
- Persisted detailed Builder targets, required materials, chunks, path failures, and localized status reasons with automatic temporary-failure recovery and load-time job reattachment.
- Improved translucent preview visibility and bounded large-schematic rendering, and documented planning, diagnostics, and recovery behavior.

# Pull request: canonical Builder materials and persistent worker inventory

- Canonicalized schematic block states to inventory item identity so orientation metadata cannot split equivalent requirements.
- Added a persistent 27-slot Builder inventory with secure GUI transfer, nearby dropped-item pickup, migration, and death drops.
- Counted Depot and Builder stock together, added atomic capacity-aware fetching and a distinct inventory-full diagnostic.
- Updated Builder documentation for inventory persistence, pickup, and canonical matching.

## TDM bomb kit economy and buy phase

- Added per-map BOMB economy configuration, match-scoped player buy score, kill and successful-defuse rewards, and server-authoritative saved-kit purchases.
- Added saved-kit costs with backward-compatible zero-cost loading and a 20-second protected buy phase before every bomb round.
- Added automatic first-free-kit fallback, economy HUD/GUI synchronization, commands, and map diagnostics.

# Pull request: finalize HBM CSGO BOMB objective custody

- Observe the live HBM charge's inherited `started` and `timer` fields through a per-tile-class reflection cache, latching disarm and imminent detonation before HBM removes its tile.
- Replace unknown-removal scoring with an explicit operator-recoverable objective error and retain coordinate-matched runtime IMC as secondary confirmation.
- Enforce exact-item bomb custody, purge stale inventory/world drops at round boundaries, and create exactly one fresh objective for each playable round.
2026-08-29 00:00 — TDM multiplayer round-flow improvements

- Initialized every competitive player through the round-boundary kit flow, including survivors, so a death is no longer required to receive a complete fresh loadout.
- Kept planted bombs authoritative over Terrorist elimination and added centralized detonation economy rewards.
- Added configurable variant lists for team win, team round-start, and bomb-planted sounds.
- Added an admin-only `/tdm teamless` observer command, explicit Blue/Red menu rosters, and remappable TDM menu/buy-menu keys.
- Source inspection was completed; multiplayer and dedicated-server runtime validation remains required.

2026-08-29 00:00 — TDM round waiting, survivor economy, and buy authorization

- Added explicit server-authoritative round-waiting state so eliminated respawns and mid-round joiners are placed at team spawns, frozen, protected, excluded from combat, and released at the next round.
- Replaced the incomplete survivor inventory carryover with a dedicated keep-current-kit or buy-different-kit choice; free restores reconstruct the authoritative saved kit without spending buy score.
- Corrected the buy key route and consolidated server validation around active buy time plus a bounded four-block team-spawn check.
- Removed the alternate infinite-respawn BOMB lifecycle and migrated legacy BOMB maps to round elimination while retaining the deathmatch respawn setting.
- Source-level call-site and state-cleanup inspection was completed; dedicated-server multiplayer runtime validation remains required.

2026-08-29 00:00 — TDM transition placement, built-in sounds, FFA, and map economy

- Separated TDM spawn placement eligibility from combat/round-waiting eligibility so login, respawn, voting, intermission, and waiting phases retain selected-map spatial ownership.
- Corrected the built-in CT victory sound declaration to the bundled `tdm_ct_win_1.ogg` asset.
- Added explicit FFA mode data, neutral spawn persistence, hostile-player kill handling, elimination winner resolution, and a neutral single-roster menu.
- Added persisted per-map round-start, kill, and round-win buy-score rewards with backward-compatible defaults of 1, 2, and 3 and non-negative admin settings.
- Runtime Forge/client-server validation remains required; source-only checks were used for this pass.
2026-08-29 00:00 — Runtime-safe TDM sound dispatch and diagnostics

- Replaced implicit server-side player sound calls with an explicit per-recipient client packet, normalized configured event IDs without rewriting custom namespaces, and added debug-gated dispatch/client diagnostics.
- Added the operator-only `/tdm testsound` command through the gameplay resolver/dispatcher and documented event-ID configuration semantics. Bundled declarations retain extensionless paths, including the intentionally underscored CT victory filename. In-game audio playback remains to be validated with the command.

2026-08-29 21:55 — Repair TDM sound configuration defaults

- Added a one-time TDM sound-config version migration that repairs legacy empty lists to the five bundled event IDs without overwriting non-empty custom values.
- Separated immutable Forge defaults from live loaded arrays so reloads cannot turn an earlier empty configured value into the default for a subsequently missing property.
- Expanded disabled `/tdm testsound` feedback with the event type, property name, raw variants, and normalized effective variants. Source inspection was completed; the command and client playback still require in-game validation.

2026-08-29 23:30 — TDM team placement, respawn, and round economy fixes

- Re-resolved team spawns and rebuilt waiting/buy freeze anchors immediately after automatic balancing, while retry entries now retain only lifecycle context and always use persisted current team membership.
- Routed Forge respawn events for replacement player entities directly through immediate selected-map placement with bounded name-keyed retries, including RED, BLUE, FFA, and eliminated waiting placement.
- Replaced the universal round-start score with a result-time losing-side bonus, including captured-participant filtering and deterministic all-loser FFA draws.
- Migrated legacy `roundStartBuyScoreReward` data to `roundLossBuyScoreReward`, added the configurable `bombPlantBuyScoreReward`, and awarded it once at the authoritative unplanted-to-planted transition.
- Updated TDM map commands and documentation. Source-level lifecycle, persistence, and call-site inspection was completed; dedicated-server multiplayer runtime validation remains required.

2026-08-29 23:59 — Make HBM radiation integration runtime-optional

- Removed direct HBM class references from always-loaded safezone and common event handlers, eliminating their HBM-driven classloading failure path.
- Added a one-time, reflection-backed radiation compatibility bridge selected only when Forge reports mod ID `hbm`; absent or incompatible HBM installations degrade to a no-op with at most one warning per failure boundary.
- Preserved the existing safezone radaway, Rad-X, radiation reset, and radiation-potion removal behavior when the expected HBM API is available. HBM-free and HBM-present dedicated-server runtime validation remains required.

2026-08-29 23:59 — Add TDM skip voting and current-map exclusion

- Added `/tdm skip [yes|no|status]` as a 60-second, strict-majority vote for active competitors, with duplicate prevention, disconnect-aware eligibility, result feedback, and lifecycle cleanup.
- Routed successful skips through the existing map-vote transition so combat, objective, freeze, waiting, kit, and buy-phase state are cleaned without producing a team, FFA, bomb, or economy result.
- Kept the stable normalized map key visible but disabled in the map-vote GUI, rejected current-map selections authoritatively, excluded it from winner resolution, and retained the current match when no alternative exists.
- Updated command help, completion, command-menu localization, and TDM documentation. Source-level validation was completed; multiplayer and dedicated-server runtime validation remains required.

## Pull request: restore non-hardcore BOMB respawns and FFA kit pools

- Restored persisted per-map hardcore respawn settings for BOMB maps.
- Kept timed buy phases exclusive to hardcore BOMB while adding protected per-respawn kit selection to non-hardcore BOMB.
- Added mandatory FFA round-start loadouts backed by existing RED and BLUE map kit pools.
- Added an FFA-only RED/BLUE pool switch to the kit selection GUI and server-authoritative pool validation.

## Pull request: fix TDM cross-mode elimination ownership softlock

- Stopped FFA mode changes from persisting `hardcoreRespawns=true`; FFA round elimination is now derived from its active mode.
- Migrated the legacy forced hardcore flag to `false` only while loading FFA maps, preventing already-polluted FFA configuration from leaking into a later DEATHMATCH mode without changing DEATHMATCH or BOMB settings.
- Isolated BOMB, FFA, and hardcore DEATHMATCH eliminated-player state and made respawn, login, retry placement, and protection enforcement validate the current mode.
- Rejected `hardcorerespawns` configuration while a map is FFA and clarified that FFA always uses round elimination.

2026-08-30 00:00 — Separate continuous-mode loadouts and command help

- Split `/tdm help` into permission-aware General, Match, Teams, Kits, Maps, and Administration pages, and added server-authoritative completion for commands, subcommands, players, maps, teams, modes, settings, and fixed argument values.
- Introduced an explicit economy-free Deathmatch/FFA loadout-selection lifecycle separate from competitive BOMB buy and survivor-kit state; the shared kit GUI now labels respawn loadouts without displaying prices.
- Made Deathmatch and FFA login/respawn paths enter continuous gameplay immediately rather than inheriting competitive round-waiting/elimination policy, while retaining active hardcore BOMB late-join waiting.
- Restricted buy-score awards, economy configuration, and hardcore round-elimination configuration to BOMB maps, and retained mode-transition cleanup of incompatible transient state. Source/call-site inspection and static checks were completed; multiplayer runtime validation remains required.

2026-08-31 00:00 — Restore competitive BOMB initialization and skip-vote feedback

- Routed a persisted enabled BOMB server through full match initialization before its first round so first-round buy score is zero while later authoritative economy rewards remain unchanged.
- Kept each hardcore BOMB buy-time freeze anchor stable across enforcement ticks instead of replacing it with the player's already-moved position, retained map-specific and legacy spawn support without touching the separate Deathmatch/FFA loadout path, and explicitly clears TDM-owned freeze and protection state on logout.
- Replaced implementation-oriented skip-vote tallies with player-facing start instructions, YES progress, pass, failure, and expiry announcements.
- Source-level lifecycle and call-site inspection was completed; multiplayer and dedicated-server runtime validation remains required.

2026-08-31 00:00 — Isolate TDM score domains and configurable purchases

- Split DEATHMATCH team point score and FFA player point score from persisted kill statistics, BOMB buy score, and the new per-player spendable kill score; valid-kill awarding and match resets now mutate each domain explicitly.
- Added immediate point-threshold and timer round completion through a guarded non-BOMB round-end path, plus the `pointlimit` map-setting name while retaining `scorelimit` compatibility.
- Added persisted, inventory-defined Utility and Killstreak purchases with server-authoritative mode, buy-time, selection-context, and balance validation. Utility consumes only BOMB buy score; queued killstreak rewards consume only kill score and are applied after respawn kit reconstruction.
- Added opt-in per-map killstreak support/reward settings, mode-aware menu buttons and kill-score display, administration commands, player list/buy commands, permission-aware completion, and task-oriented help/documentation. Source inspection and static consistency checks were completed; dedicated-server and multiplayer runtime validation remains required.

2026-08-31 00:00 — Integrate TDM chat identity and faction membership restrictions

- Routed live Blue, Red, Terrorist, Counter-Terrorist, and FFA labels through the existing Xenofactions global-chat prefix formatter; teamless and observer players retain neutral/faction behavior, and disabling TDM immediately restores faction prefixes without persisted chat state.
- Added a shared TDM membership guard at the authoritative faction creation/member-add operations and their create, apply, accept, and administrative force-join entry paths, with clear command and help feedback while leaving existing memberships untouched.
- Source-level call-site and lifecycle inspection was completed; dedicated-server chat, team/map/mode transition, and faction-command runtime validation remains required.
