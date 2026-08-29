# Configuration Reference

Xenofactions uses Forge's `Configuration` system. The main generated file is normally:

```text
config/hfr.cfg
```

Restart the server after changing configuration unless the generated config comment explicitly says otherwise.

## Player identity

`XENOFACTIONS_09C_PLAYER_IDENTITY.playerIdentityMode` accepts case-insensitive `AUTO`, `UUID`, or `NAME` and defaults to `AUTO`. `AUTO` selects `NAME` in Forge's deobfuscated development environment or with `online-mode=false`, and `UUID` on a packaged online-mode server. Invalid values fail closed to `UUID`. Startup logs both modes and warns prominently for `NAME`; debug logging does not affect selection.

`UUID` uses the authenticated UUID and is secure production behavior; last-known names never authorize. `NAME` uses the trimmed, `Locale.ROOT`-lowercase `GameProfile` name so test membership survives transient UUID changes, but cannot protect against username reuse or changes. Ambiguous stored names fail closed.

Every independent `runClient` invocation uses GTNH's credential-free `Developer<random numbers>` name and a fresh random UUID. For deterministic multiplayer or faction testing, override either value with `gradle runClient -PdevUsername=PlayerOne -PdevUuid=<standard-uuid>`; an omitted username keeps the GTNH-generated name and an omitted UUID remains random. Explicit usernames must contain at most 16 letters, digits, or underscores.

## Main feature toggles

Category: `XENOFACTIONS_01_MODULES`

| Key | Default | Meaning |
| --- | ---: | --- |
| `enableSurvivalRecipes` | `true` | Register the audited survival recipes for faction, prestige, production, and support infrastructure. Set `false` to preserve command/shop-only distribution; registrations and unrelated recipes are unchanged. |
| `enableDynmapIntegration` | `true` | Try to publish faction city/claim markers through Dynmap when Dynmap is installed. |
| `enableJourneyMapIntegration` | `true` | Enable optional client claim overlays for legacy JourneyMap 5.2.x on Minecraft 1.7.10. |
| `enableTDM` | `true` | Register and initialize the optional team-deathmatch module. |
| `enableCustomFactionFlags` | `true` | Allow `/c flag seturl`, `clear`, and `reload` custom flag workflows. |
| `enableNewPlayerProtection` | `false` | Enable starter protection systems. |
| `enableConquestFlagsCommand` | `true` | Enable conquest flag command/system toggle. |
| `enableGuideBook` | `true` | Enable guide-book integration/fallback references. |
| `enableNEIIntegration` | `true` | Register optional Not Enough Items handlers when NEI is installed; ordinary gameplay recipes and stone-drop behavior are unchanged. |

## TDM compatibility

Category: `XENOFACTIONS_20_TDM`

| Key | Default | Meaning |
| --- | ---: | --- |
| `tdmBombUnknownRemovalAsDefuse` | `true` | Treat a tracked HBM CSGO charge that disappears safely before its brief disarmed state can be observed as a CT defuse, but only with a valid support face, a safely positive timer, and no detonation evidence. Set `false` to preserve strict `OBJECTIVE_ERROR` handling. |
| `tdmBombLifecycleDebug` | `false` | Log detailed reflected HBM lifecycle transitions. Compatibility warnings and objective errors remain visible when disabled. |

## Prestige generation

Category: `XENOFACTIONS_02_PRESTIGE_GENERATION`

| Key | Default | Meaning |
| --- | ---: | --- |
| `startingPrestige` | `250` | Prestige granted to new factions. |
| `basePrestigeGeneration` | `25` | Base prestige generation amount. |
| `prestigeGenerationCap` | `2500` | Cap for generated prestige. |
| `blastFurnacePrestigeGeneration` | `5` | Blast furnace contribution. |
| `grainmillPrestigeGeneration` | `3` | Grain mill contribution. |
| `universityPrestigeGeneration` | `60` | University contribution. |
| `federalReservePrestigeGeneration` | `30` | Legacy compatibility key; the deprecated Federal Reserve is inert and does not contribute Prestige. |
| `templePrestigeGeneration` | `90` | Temple contribution. |
| `statuePrestigeGeneration` | `15` | Statue contribution. |
| `cityCenterPrestigeGeneration` | `0` | City center contribution. |

## Costs, upkeep, and bankruptcy

Categories: `XENOFACTIONS_03_PRESTIGE_COSTS_UPKEEP` and `XENOFACTIONS_04_BANKRUPTCY`

| Key | Default | Meaning |
| --- | ---: | --- |
| `warpCreationCost` | `125` | Cost to create a faction warp. |
| `warpTentUpkeep` | `75` | Warp tent upkeep. |
| `medicalTentUpkeep` | `5` | Medical tent upkeep. |
| `claimFlagUpkeep` | `1` | Upkeep for claim flags. |
| `settlementCityUpkeep` | `10` | Settlement city upkeep baseline. |
| `warDeclarationBaseCost` | `150` | Base cost to declare war. |
| `warDeclarationTargetPrestigeFactor` | `0.15` | Additional war cost factor based on target prestige. |
| `activeWarUpkeep` | `75` | Ongoing active-war upkeep. |
| `enemyKillPrestigeReward` | `25` | Prestige awarded for killing a member of a faction your faction explicitly marks as an enemy. |
| `warUpkeepHourlyGrowth` | `0.25` | Linear hourly active-war upkeep growth. |
| `warUpkeepHourlyGrowthSquared` | `0.05` | Squared hourly active-war upkeep growth. |
| `surrenderPrestigeTransferPercent` | `0.50` | Prestige transfer percent for surrender tribute. |
| `surrenderTributeDurationHours` | `84` | Surrender tribute duration. |
| `enableNegativePrestigePenalties` | `true` | Enable negative-prestige penalties. |
| `financialCrisisThreshold` | `0` | First penalty threshold. |
| `nationalCollapseThreshold` | `-500` | Second penalty threshold. |
| `fallenNationThreshold` | `-1000` | Third penalty threshold. |
| `financialCrisisUpkeepMultiplier` | `1.25` | Upkeep multiplier at crisis. |
| `nationalCollapseUpkeepMultiplier` | `1.50` | Upkeep multiplier at collapse. |
| `fallenNationUpkeepMultiplier` | `2.00` | Upkeep multiplier at fallen-nation status. |

## Claims and cities

Category: `XENOFACTIONS_05_CLAIMS_CITIES`

| Key | Default | Meaning |
| --- | ---: | --- |
| `maxCityRadius` | `6` | Maximum city radius. |
| `minimumCitySpacingChunks` | `13` | Minimum spacing between city centers. |
| `claimNameMinLength` | `1` | Minimum claim-name length. |
| `claimNameMaxLength` | `32` | Maximum claim-name length. |
| `claimNameRequireUnique` | `true` | Require unique claim names. |
| `claimRenameOfficersAllowed` | `true` | Allow officers to rename claims. |
| `peaceCityTransfersEnabled` | `true` | Allow city transfer terms in peace. |
| `surrenderTransfersCities` | `true` | Allow city transfer behavior on surrender. |
| `cityRadii` | `[2,3,4,5,6]` | City radius per city level. |
| `cityUpgradeCosts` | `[75,150,300,600,1000]` | Upgrade cost per city level. |
| `cityUpkeep` | `[10,25,50,90,140]` | Upkeep per city level. |
| `cityFoundingCostGrowth` | `0.50` | Cost growth for founding additional cities. |
| `cityRelocationEnabled` | `true` | Enable owner-only, two-phase City Center relocation. |
| `cityRelocationFreeDistanceBlocks` | `10` | Free horizontal move distance. |
| `cityRelocationBasePrestigeCost` | `0` | Legacy base-cost key; the default relocation formula has no base cost. |
| `cityRelocationPrestigePerExtraBlock` | `30` | Cost per additional horizontal block, rounded up. |
| `cityRelocationMaxDistanceBlocks` | `256` | Maximum horizontal move distance. |
| `cityRelocationMoveLimit` | `3` | Successful moves allowed per stable city ID in the rolling window. |
| `cityRelocationWindowHours` | `168` | Rolling move-frequency window (seven days). |
| `cityRelocationRepeatCooldownMinutes` | `30` | Delay after the second successful move before the third move. |
| `cityRelocationPendingMinutes` | `30` | Pending transaction lifetime; expiration leaves the old city unchanged. |
| `warpCost` | `125` | Legacy alias still read in this category. |
| `territoryDelay` | `5` | Ticks between territory validation operations. |
| `territoryAmount` | `50` | Chunks checked per territory operation. |
| `prestigeDelay` | `72000` | Ticks between prestige updates; default is one hour. |
| `disableChests` | `true` | Prevent placing chests outside claims. |
| `mold` | `360000` | Ticks before loaded cardboard boxes rot. |
| `freeRaid` | `true` | Ignore raidability checks and make everyone raidable. |

## War and diplomacy

Category: `XENOFACTIONS_06_WAR_DIPLOMACY`

| Key | Default | Meaning |
| --- | ---: | --- |
| `warEnabledDefault` | `false` | Whether war declarations begin enabled on startup. |
| `onlinePlayerThreshold` | `2` | Online-player threshold for war/raid checks. |
| `raidGraceAfterOnlineDropMinutes` | `30` | Grace after online count drops. |
| `surrenderCooldownHours` | `84` | Surrender cooldown. |
| `peaceCooldownHours` | `84` | Peace cooldown. |
| `ceasefireCooldownHours` | `24` | Ceasefire cooldown. |
| `allianceBreakCooldownHours` | `24` | Cooldown after breaking alliances. |
| `alliesCanJoinWars` | `true` | Allow allies to join wars. |
| `alliesCanDeclareWarOnEachOther` | `false` | Allow allies to declare war against each other. |

## New-player and build protection

Category: `XENOFACTIONS_07_NEW_PLAYER_PROTECTION`

| Key | Default | Meaning |
| --- | ---: | --- |
| `pvpGraceDurationHours` | `4` | PvP grace duration. |
| `keepInventoryDurationHours` | `24` | Keep-inventory grace duration. |
| `graceBuildEnabled` | `true` | Enable faction build grace. |
| `graceBuildOneTimeUse` | `true` | Restrict build grace to one use. |
| `graceBuildDurationHours` | `48` | Build grace duration. |

## Custom flags

Category: `XENOFACTIONS_08_CUSTOM_FLAGS`

| Key | Default | Meaning |
| --- | ---: | --- |
| `allowedImageHosts` | `postimages.org`, `i.postimg.cc` | Hosts allowed for imported faction flag images. |
| `maxImageWidth` | `1024` | Maximum image width. |
| `maxImageHeight` | `1024` | Maximum image height. |
| `maxFileSizeBytes` | `1048576` | Maximum download size. |
| `downloadTimeoutMs` | `5000` | HTTP timeout. |
| `maxRedirects` | `3` | Maximum HTTPS redirects. |
| `importRateLimitSeconds` | `60` | Per-player/faction import rate limit. |
| `reloadMissingFileClearsMetadata` | `true` | Clear stale metadata if reload cannot find cached image. |

## Wall Art

Category: `XENOFACTIONS_08B_WALL_ART`

Wall Art has an independent source policy; existing custom-flag host settings do not
restrict Wall Art. Hosts are matched exactly after lowercase and trailing-dot
normalization. Every HTML-resolved image and redirect is checked again.

| Key | Default | Meaning |
| --- | ---: | --- |
| `allowedImageHosts` | `postimg.cc`, `i.postimg.cc`, `postimages.org` | Exact HTTPS page and image hosts allowed for Wall Art imports. |
| `maxSourceBytes` | `16777216` | Maximum remote image size; HTML resolution is separately capped at 512 KiB. |
| `maxSourceDimension` | `8192` | Maximum source width or height before decode. |
| `maxSourcePixels` | `67108864` | Maximum source pixel count before decode. |
| `downloadTimeoutMs` | `10000` | Connect and read timeout. |
| `maxRedirects` | `3` | Maximum manually validated HTTPS redirects. |
| `successCooldownSeconds` | `5` | Delay after a successful import. |
| `failureCooldownSeconds` | `1` | Short retry delay after a failed import. |

The importer accepts either a direct PNG/JPEG resource or a bounded HTML image page.
For a page it prefers `og:image:secure_url`, then `og:image`, then the first usable
`img` source. It never executes page scripts, and clients only receive the processed,
server-stored PNG.

## Dynmap

Category: `XENOFACTIONS_09_DYNMAP`

| Key | Default | Meaning |
| --- | ---: | --- |
| `markerSetId` | `xenofactions_cities` | Dynmap marker set ID. |
| `markerSetLabel` | `Faction Cities` | Dynmap marker set label. |
| `updateIntervalTicks` | `600` | Marker refresh interval. |
| `claimFillOpacity` | `0.18` | Claim area fill opacity. |
| `claimLineOpacity` | `0.0` | Claim area outline opacity. |
| `claimLineWeight` | `0` | Claim outline weight. |
| `borderLineOpacity` | `0.9` | City border line opacity. |
| `borderLineWeight` | `3` | City border line weight. |
| `showCityCenterMarkers` | `true` | Show a point marker at each city center. |
| `showClaimDetailsInLabels` | `true` | Include claim details in Dynmap labels. |
| `showPrestigeDetailsInLabels` | `true` | Include prestige/upkeep details in labels. |

## JourneyMap client overlay

JourneyMap is optional. Xenofactions targets the legacy JourneyMap 5.2.x implementation for Minecraft 1.7.10 (especially 5.2.8) through reflection; it does not use the modern JourneyMap API and has no JourneyMap dependency. Missing or changed JourneyMap internals disable only this overlay for the client session.

Category: `XENOFACTIONS_09B_JOURNEYMAP_CLIENT`

| Key | Default | Meaning |
|---|---:|---|
| `showMinimapClaims` | `true` | Render faction city claims on rectangular or circular minimaps. |
| `showFullscreenClaims` | `true` | Render faction city claims on the fullscreen map. |
| `journeyMapShowTerritoryLabels` | `true` | Render one readable city-name label per city territory on enabled JourneyMap claim overlays. Labels use the city name, not internal faction or city IDs. |
| `claimFillOpacity` | `0.18` | Faction-color fill opacity, clamped to `0.0–1.0`. |
| `claimBorderOpacity` | `0.80` | Exposed perimeter opacity, clamped to `0.0–1.0`. |
| `claimBorderWidth` | `1.0` | Perimeter width, clamped to `0.25–10.0` pixels. |

## Legacy categories

The inherited xRadar/HFR systems are still configured under ordered legacy categories:

- `XENOFACTIONS_10_MACHINES_POWER`
- `XENOFACTIONS_11_RADAR_FORCEFIELD`
- `XENOFACTIONS_12_WEAPONS_EXPLOSIVES`
- `XENOFACTIONS_13_WORLD_MOBS_BORDER`
- `XENOFACTIONS_14_STOCK_MARKET`
- `XENOFACTIONS_15_CHAT_FILTER`
- `XENOFACTIONS_16_GENERAL_LEGACY`
- `XENOFACTIONS_17_DEBUG_LOGGING`

Important defaults include:

| Key | Default | Category | Meaning |
| --- | ---: | --- | --- |
| `FxR_enableRadar` | `false` | Radar/forcefield | Enable FMU+/Flan-style radar bridge; disabled for MCHeli compatibility. |
| `radarRange` | `1000` | Radar/forcefield | Radar range setting. |
| `radarBuffer` | `30` | Radar/forcefield | Height buffer above radar for detection. |
| `radarAltitude` | `55` | Radar/forcefield | Minimum Y height for radar operation. |
| `radarConsumptionNew` | `2000` | Radar/forcefield | RF/t required by radar. |
| `freeRadar` | `false` | General legacy | Make radar and shield free to use. |
| `craftingDifficulty` | `0` | General legacy | Recipe difficulty from easy to hard. |
| `enableChatFilter` | `true` | Chat filter | Enable chat swear filter. |
| `enableStocks` | `true` | Stock market | Enable the stock market. |
| `updateInterval` | `600` | Stock market | Seconds between market updates. |
| `stockCap` | `50` | Stock market | Shares a player can own per stock. |

Use the generated comments in `hfr.cfg` for the many remaining legacy weapon, machine, entity, world, and market values.

## Custom stone drops JSON

`/stonedrop` saves entries to:

```text
config/stonedrops.json
```

Each saved entry stores item registry name, metadata, stack size, chance, optional NBT string, and optional `minY`/`maxY` range.

During post-initialization, Xenofactions checks the Forge mod loader for the optional HBM mod.
When HBM is loaded, Xenofactions reads `com.hbm.config.MiningConfig.excavatorBedrockDrops`
with reflection after HBM has initialized it. Entries must use HBM's documented
`modid:item metadata minimumAmount maximumAmount` format, are resolved through the Forge 1.7.10
item registry (including block items such as `hbm:tile.ore_uranium`), and are added at a
one-percent chance per normal-stone block within the generated material-based Y range.

Administrator entries loaded from `config/stonedrops.json` are preserved. Missing automatic HBM
entries are appended without duplicating an existing item/metadata/NBT combination. An existing
empty JSON list is treated as an intentional administrator configuration and is not overwritten.
An empty file or malformed JSON is logged as a recovery case; if valid HBM entries are available,
the malformed file is backed up and `stonedrops.json` is rewritten safely with the recovered
automatic entries. The success log message is:

```text
[XF] Registered <count> automatic HBM stone drops from MiningConfig.excavatorBedrockDrops.
```

## Faction creation cooldown data

Regular leader-run `/c disband <faction name>` writes faction creation penalties to `clowder_faction_creation_cooldowns.json` in the server root. Entries are absolute real-time expiration timestamps keyed by player UUID where possible, with last-known names and normalized-name fallbacks for unresolved offline profiles. These cooldowns block only `/c create`; applications, invitations, and joining are unchanged.


The cooldown JSON contains `uuids` and normalized `nameFallbacks`; entries contain epoch-millisecond `expiresAt` and `lastKnownName`. `/xc clearcreationcooldown <player-or-uuid>` (alias `resetcreationcooldown`) works for online/offline targets and saves successful changes immediately via temporary-file replacement.

## XENOFACTIONS_18_EARTH_WORLD

Phase 1's template-backed Earth type is controlled by `enableEarthWorldType`, `earthWorldTypeName` (canonical value `xf_earth`), `earthRequireProfile`, `earthMissingChunkPolicy` (`FAIL` or `VOID`), `earthBoundaryMode` (`OFF`, `PROFILE`, or `CONFIG`), `earthAllowProfileMinecraftVersion`, `earthLogFallbackChunks`, and `earthBoundarySafetyMargin`. `PROFILE` derives the dimension-0 border from the validated save profile; `CONFIG` preserves the legacy administrator border coordinates; `OFF` does not activate a profile border. See [XFEARTH_INSTALLATION.md](XFEARTH_INSTALLATION.md).
# Builder schematic limits

Builder schematic processing is bounded by `builderMaxSchematicBlocks`, `builderMaxSchematicWidth`, `builderMaxSchematicHeight`, `builderMaxSchematicLength`, and `builderMaxUploadBytes` in `XENOFACTIONS_19_BUILDERS`. Defaults are 262144 blocks, 256×128×256 dimensions, and 8 MiB encoded upload data. Lower these values for smaller servers; existing persisted jobs are not replaced.
# TDM event sounds

The `20_tdm` configuration category accepts string lists for `ctWinSounds`,
`terroristWinSounds`, `ctRoundStartSounds`, `terroristRoundStartSounds`, and
`bombPlantedSounds`. Entries are Minecraft sound IDs; one non-blank entry is
selected randomly for each authoritative event. Empty lists disable that event.

## TDM FFA and economy

`/tdm map mode <map> ffa` selects round-elimination Free-For-All. Add neutral spawn locations with `/tdm map addspawn <map> ffa`; these are stored independently from RED/BLUE spawns. Existing map saves remain valid and maps without FFA spawn records retain their team-mode behavior.

TDM maps persist three non-negative economy rewards. Missing fields use these defaults:

- `roundStartBuyScoreReward`: `1`
- `killBuyScoreReward`: `2`
- `roundWinBuyScoreReward`: `3`

Configure them with `/tdm map roundstartscore <map> <amount>`, `/tdm map killscore <map> <amount>`, and `/tdm map roundwinscore <map> <amount>`. Zero disables an individual reward. `/tdm map economy <map> false` disables map economy as a whole.

Bundled sound event IDs are `hfr:tdm.ct_win1`, `hfr:tdm.t_win1`, `hfr:tdm.ct_round_start1`, `hfr:tdm.t_round_start1`, and `hfr:tdm.bomb_plant1`. Each configured event accepts a variant list; blank entries are ignored and an empty list disables playback.
The values name sound **events**, not `.ogg` files or paths. An ID without a namespace uses `hfr`; an explicit namespace (for example `othermod:custom.roundwin`) is preserved. The server selects one variant and sends an explicit playback packet to each eligible player in the same world/dimension. Operators can exercise that same path with `/tdm testsound <ctwin|twin|ctstart|tstart|bombplant>`; start tests target only the operator, while win and plant tests use normal global TDM recipients. Enable legacy `enableDebugLogging` to log the trigger, configured variants, normalized event ID, recipient count, dispatch route, and client playback.
