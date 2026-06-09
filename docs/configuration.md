# Configuration Reference

Xenofactions uses Forge's `Configuration` system. The main generated file is normally:

```text
config/hfr.cfg
```

Restart the server after changing configuration unless the generated config comment explicitly says otherwise.

## Main feature toggles

Category: `XENOFACTIONS_01_MODULES`

| Key | Default | Meaning |
| --- | ---: | --- |
| `enableDynmapIntegration` | `true` | Try to publish faction city/claim markers through Dynmap when Dynmap is installed. |
| `enableTDM` | `true` | Register and initialize the optional team-deathmatch module. |
| `enableCustomFactionFlags` | `true` | Allow `/c flag seturl`, `clear`, and `reload` custom flag workflows. |
| `enableNewPlayerProtection` | `false` | Enable starter protection systems. |
| `enableConquestFlagsCommand` | `true` | Enable conquest flag command/system toggle. |
| `enableGuideBook` | `true` | Enable guide-book integration/fallback references. |

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
| `federalReservePrestigeGeneration` | `30` | Federal Reserve contribution. |
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
