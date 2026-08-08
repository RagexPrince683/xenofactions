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
