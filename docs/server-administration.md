# Server Administration Guide

## First startup checklist

1. Install Xenofactions on both server and clients.
2. Start the Forge server once to generate `config/hfr.cfg`.
3. Stop the server and review the `XENOFACTIONS_01_MODULES` toggles first.
4. Decide whether war starts enabled with `warEnabledDefault` or is controlled manually by `/xc warenable` and `/xc wardisable`.
5. Review prestige generation, upkeep, and bankruptcy values before opening a public world.
6. If using Dynmap, install Dynmap separately and keep `enableDynmapIntegration=true`.
7. JourneyMap is never required on the server. Clients may optionally use JourneyMap 6.0.x (API 2.0.0) or the retained 5.2.x hook. Xenofactions synchronizes permission-scoped, current-dimension map data and claim snapshots; incompatible clients use the standalone TDM map.
7. If allowing custom flags, review `allowedImageHosts`, image dimensions, file size, redirects, timeout, and rate limits.
8. If using TDM, keep `enableTDM=true`; otherwise disable it to avoid registering `/tdm`.

## Recommended policy decisions

- **War windows:** Because admins can globally enable/disable war declarations, many servers pair `/xc warenable` with scheduled war periods.
- **Custom flags:** Restrict hosts to image CDNs you trust. The default allowlist only includes Postimages hosts.
- **New-player protection:** Disabled by default. Enable it before launch if your rules require starter PvP/keep-inventory grace.
- **Free raid:** The legacy `freeRaid=true` default ignores raidability checks. Review this setting carefully for protected-claim servers.
- **Radar bridge:** Leave `FxR_enableRadar=false` unless you have tested the FMU+/vehicle radar integration with your modpack.
- **Debug logging:** Leave `enableDebugLogging=false` for normal servers. Enable it only while diagnosing noisy systems such as registration confirmations, OreDictionary integration chatter, market packet traces, background task confirmations, and other developer diagnostics; warnings, errors, and important operational events still log when it is disabled.

## Common staff commands

```text
/xc help
/xc warenable
/xc wardisable
/xc addprestige <faction> <amount>
/xc setclaim <wild/safe/war> <s/c> <radius>
/xc factiontimeoutcreationreset <playername>
/stonedrop list
/invsee <player>
/xmute <player> <seconds|perm> [reason]
```

The shared area editor also supports `/xc editor select <safezone|warzone|wilderness|border_exempt>`, then `/xc editor point a`, `/xc editor point b`, and `/xc editor commit` (or `cancel`). The active type is shown in `/xc editor status`. Starting any area type gives the shared Admin Selection Wand when possible: left-click a block for A and right-click one for B. TDM administrators can open the draggable panel with `/tdm editor gui` or the configurable F10 key. Its nearby safe/war territory preview and block-accurate map bounds remain separate data; bounds never become Clowder claims. `/tdm map border <map> <on|off>` optionally enforces the selected map bounds' horizontal footprint for active match players and automatically publishes its boundary to players in the map dimension while enabled.

One physical TDM map can offer several voting pairings. New maps start excluded from votes so administrators can configure them during a live match; existing saved maps stay voteable. Use `/tdm map voteable <map> <on|off>` to enable or disable the entire map, or `/tdm map voteable <map> <tdm|sd|ffa> <on|off>` to control its individual mode offerings. The editor's Maps page has both controls, and its Spawns page maintains three independent spawn sets. The map-wide voting switch does not stop an active match or block explicit admin map selection. Search and Destroy sites are shared stored map data but render only during that pairing. Old map spawns migrate by category and former mode; `/tdm map legacyspawns <map>` lists ambiguous preserved entries so an administrator can assign them deliberately. Saved kits are available in all modes by default; `/tdm kit mode` can disable a kit for a specific mode without copying it, and saved buy-score costs matter only in Search and Destroy.

`/xc factiontimeoutcreationreset <playername>` is admin-only and resets only the specified player's faction creation cooldown. It does not alter faction membership or any other cooldown, and it supports stored offline players.

## XShops and faction market terminals

XShop offers belong to one server catalog, not to placed blocks. All administration uses permission level 3. Right-clicking an XShop opens its linked trading screen for players and administrators alike. Administrators can press **Configure** to open the block configuration panel, including when the shop is unlinked or disabled. Select a shop to link the block, use **Edit** for its settings and offers, or **Unlink block** to clear the reference. `/xshop edit <ID/name>` opens the offer/settings editor directly. A renamed nametag remains a convenient admin-only way to link an existing block to an existing shop name or ID; it does not create a separate shop.

```text
/xshop create General Supplies
/xshop list
/xshop add <shop ID>
/xshop edit <shop ID>
/xshop set <shop ID> visible on
/xshop link <shop ID> <x> <y> <z>
```

For **Add offer**, put the sold item and quantity in hotbar slot 1, and one to three currency stacks in slots 2-4. Items are copied into the definition, not consumed during editing. The editor can rename the shop, toggle enabled/faction visibility/admin-only settings, and remove zero-based offers. **Faction: Shown/Hidden** controls whether the shop appears in faction Global Market Terminals; it does not change access through a directly linked XShop block. The shop must also be enabled and not admin-only to appear in faction terminals. The original item-and-metadata currency matching is retained. Repeated costs for the same currency must be paid in full, and inventory overflow drops only the uninserted purchased items.

New and migrated shops are enabled but excluded from faction terminals until explicitly made visible. Disabled and admin-only shops never appear there. The original admin block remains non-craftable; the separate **Global Market Terminal** is craftable when survival recipes are enabled. Officers and leaders may place one in their own faction's designated capital. Every member of that faction can browse and trade there. Both catalogs and offers use six-row pages; catalog search filters by display name.

The first successfully founded city becomes the designated capital, independently of its upgrade level. `/c capital` reports it; only leaders can run `/c capital set <owned city>`. The `XENOFACTIONS_05_CLAIMS_CITIES.capitalChangeCooldownHours` default is 168 hours between changes. Changing the designation clears the old terminal registration and invalidates an old home outside the new capital; an Officer or leader must run `/c sethome` inside the new capital. Losing the capital does not automatically designate another city. The leader must choose a replacement, subject to the saved cooldown. Moving a City Center preserves its capital identity, but a terminal outside its resulting territory stops functioning.

**Persistence and upgrades:** `config/marketdata.json` now stores schema version 2, UUID-keyed shop definitions, display names, enabled/visible/admin-only flags, offers, and reserved category/sort-order fields. Old name-keyed JSON is backed up to `marketdata.json.legacy.bak` before migration. Existing item definitions, quantities, metadata, and NBT remain stored. Missing mod items leave an unavailable offer rather than creating a free trade or discarding its definition. A malformed/unsupported catalog disables shop edits and preserves the original file. Stop the server before editing JSON manually; the catalog loads at server startup and writes changes through a temporary file and replacement.

Existing admin blocks retain their tile registration and migrate their saved `name` to `shopId` when accessed. Historical name-to-ID aliases survive renames and deletions, so an unloaded block remains linked across a rename and cannot silently attach to a newly created shop with a deleted shop's old name. Unconfigured legacy names remain inactive until explicitly linked.

Faction world data stores the designated `capitalCityId`, designation marker, and `capitalChangeAfter` timestamp, plus a `FactionMarketTerminals` list containing each faction UUID, dimension, block coordinates, registration version, and unique terminal token. Existing factions migrate to the owned city containing their old home, otherwise the first owned city in deterministic dimension/X/Y/Z order; the historical founding city cannot be reconstructed from saves that do not record founding order. Saved city UUIDs now survive claim loading. Legacy capital claim coordinates reconcile with the City Center's saved ID when available.

Terminal validation looks up only that faction's registration and the recorded block. Placement can load that one recorded chunk to distinguish an unloaded terminal from WorldEdit deletion; an unavailable dimension remains unknown and blocks a duplicate until it can be checked. Normal break clears the matching registration. Use and replacement placement clear stale/out-of-capital registrations. A unique token prevents old blocks from becoming active after a replacement, even if the capital later returns. WorldEdit-copied or unregistered terminals stay inactive; break them and place an item normally to register one. Faction disband/merge clears the removed faction's registration.

GUI requests require a server-issued player session and fresh catalog revision. Permissions, distance, dimension, block identity, faction membership, capital ownership, and shop eligibility are checked on the server before navigation, configuration, or trades. Requests run on the server thread. Session state expires after five idle minutes and clears on death, logout, dimension change, or world reset. Update both clients and servers together for the new shop screens.

## Backups

Back up the world and config directory before:

- Updating Xenofactions.
- Running `/xc deletedata`, `/xc forcedisband`, mass claim commands, or other destructive faction commands.
- Editing `config/stonedrops.json` by hand.
- Changing city radii, city spacing, or upkeep rules on an established map.

## Updating from older builds

- Compare your old config against the new generated `XENOFACTIONS_*` categories.
- Re-check feature toggles because new systems may default to enabled for compatibility with the maintained fork.
- Test faction commands, prestige ticks, claims, and war declarations on a staging copy before updating a live server.

## Multi-dimension persistence checks

When validating an update, test faction claims, City Centers, homes, faction warps, and ally warps in every enabled dimension. Legacy saves that predate dimension-aware faction locations are migrated to dimension `0`, and the server log should include a migration message for each legacy location type that is encountered.

Runtime admin war toggles such as `/xc warenable`, `/xc wardisable`, and war-check bypass toggles are saved with faction data and restored on restart. The `warEnabledDefault` config value only applies before a runtime value has been saved for the world.

For non-overworld claims, specifically verify warp tents, medical tents, statues, and other prestige buildings. These structures should resolve the claim in their own dimension, and City Center GUIs should show prestige generation changes immediately while the actual prestige accrual interval remains unchanged. These checks do not require vanilla sky visibility, so Nether/End/LOTR dimensions with ceilings can still use them as long as the structure footprint has valid foundation blocks and the obstruction plane above it is clear. Also run `/c info` after city upgrades and prestige-building changes; it should report the upgraded city radius plus current generation/net-per-hour immediately, matching the City Center GUI. Use `/c info` or `/c allies` to verify current allies after diplomacy changes and restarts.

## Runtime Earth boundary administration

Xenofactions extends its existing rectangular Earth boundary; it does not use the post-1.7.10 vanilla world-border API. All commands remain under the operator-only `/xc` tree:

- `/xc worldborder on` and `/xc worldborder off` immediately enable or disable enforcement.
- `/xc worldborder status` shows the effective state, configured center, X/Z radii, safety margin, and exemption count.
- `/xc worldborder wand` starts an exemption selection when idle and gives an in-game administrator the shared **Admin Selection Wand** if needed. Left-click a block for point A and right-click a block for point B; both clicks suppress the normal block action. Coordinate selection is still recorded when faction or TDM protection has already canceled the interaction, without reopening the protected block action.
- After running `/xc worldborder wand`, left-click position 1, right-click position 2, then run `/xc worldborder exempt` to save the selected rectangle without supplying a name.
- `/xc worldborder clearexemptions` removes every saved exemption without changing enforcement state or configured geometry.

Selections are per administrator and temporary. Saved regions persist automatically as inclusive, dimension-specific block rectangles covering **all Y levels**; coordinate comparisons cover the full selected blocks (`min <= coordinate < max + 1`). They apply only to Earth boundary enforcement in dimension 0: they do not bypass faction claims, Safezones, Warzones, block protection, TDM restrictions, or damage rules. Before any destination/chunk lookup, the server checks the entity's footprint and its observed movement intersection with each crossed boundary. A selection touching a boundary opens precisely that span along the edge and continues outward perpendicular to it. Overshooting the selection in one tick therefore causes no delayed wrap, while movement immediately outside its edge span still wraps. At corners both crossed edges must be exempt. Rectangles are spatially indexed per dimension and the index is rebuilt only when regions change; movement history is transient and cleared on logout/world unload. Mounts own wrapping, vanilla passengers follow once, and MC Heli child seats defer to their parent vehicle without replaying entity ticks.

Leaving an exemption's edge span while still outside the map returns a player safely to the configured Earth map center, as does clearing an active player exemption. Moving into the valid map needs no teleport, and ordinary non-exempt boundary crossings continue to use the existing wrap behavior. Very large non-exempt displacements are reduced to an in-bounds destination in one operation instead of repeatedly wrapping on subsequent ticks. The saved region/NBT format is unchanged.

`earthBoundaryEnabled` is the default only when a world has no saved runtime choice. Runtime on/off changes and exemptions persist in the world's `xenofactions_earth_boundary` saved data; legacy named exemptions remain compatible. Changing runtime state never changes the configured center, radii, or safety margin. Disabling enforcement leaves exemptions intact, ready for later re-enabling.
