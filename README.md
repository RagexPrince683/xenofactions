# Xenofactions

Xenofactions is a Minecraft Forge **1.7.10** factions-and-warfare mod derived from the older xRadar/HFR codebase. It is built for multiplayer geopolitical, war, towny, and factions servers that want claims, faction diplomacy, prestige economy, radar/defense systems, server shops, custom flags, and optional map/team-gameplay utilities in one legacy Forge mod.

The mod is opinionated toward competitive survival servers: factions create cities, claim land, earn and spend prestige, manage members and alliances, declare wars, negotiate peace or surrender, and can expose claims through optional Dynmap markers.

> **Status:** This is a maintained fork of a legacy 1.7.10 mod. Some item/block names and internals still use `hfr`, `clowder`, or xRadar-era terminology.

## Features

- **Faction system (`/clowder`, `/c`)** - create factions, apply to join, manage members, set MOTDs/colors/flags, list factions, and view faction info.
- **Claims and cities** - claim named faction cities, upgrade city radius, name individual claims, and enforce configurable city spacing/radius rules.
- **Prestige economy** - configurable starting prestige, passive generation, building-based generation, upkeep, war costs, warps, and bankruptcy penalties.
- **Diplomacy and war** - alliances, ally warps, declarations of war, ceasefires, peace offers, surrender terms, ally defense, cooldowns, and admin war controls.
- **Custom faction flags** - set built-in flags or import HTTPS images from allow-listed hosts with size, redirect, and rate-limit protections.
- **Server administration tools** - faction admin commands, inventory viewing, muting, ignoring, stock-market controls, custom stone drops, and claim map distribution.
- **Optional Dynmap integration** - reflects into Dynmap when present to publish faction city/claim markers without making Dynmap a hard dependency.
- **Optional TDM module** - map/team/kit voting commands for servers that enable the bundled team-deathmatch system.
- **Legacy warfare systems** - radar, forcefield, missile, EMP, nuke, oil/power, market, mob/world, and chat-filter settings from the inherited xRadar/HFR codebase.

## Requirements and Compatibility

| Requirement | Details |
| --- | --- |
| Minecraft | 1.7.10 |
| Mod loader | Minecraft Forge 1.7.10. The build script targets Forge `1.7.10-10.13.4.1614-1.7.10`. |
| Java | Java 8 is recommended for building and running legacy Forge 1.7.10 servers. |
| Side | Install on both client and server for normal multiplayer use. Dedicated servers need it for commands, claims, world data, and gameplay logic; clients need it for blocks, items, GUIs, renderers, and assets. |
| Hard dependencies | None declared in `mcmod.info`; the Forge mod annotation only orders Xenofactions after GuideAPI if GuideAPI is present. |
| Optional integrations | Dynmap is optional. The integration uses reflection and retries when Dynmap is absent or not ready. |
| Known compatibility note | FMU+/Flan-style radar support is disabled by default with `FxR_enableRadar=false`, which helps avoid MCHeli-era compatibility crashes. |

## Installation

### Players

1. Install a Forge 1.7.10 client that matches your server.
2. Download the Xenofactions jar supplied by your server or from the project release page.
3. Place the jar in `.minecraft/mods/`.
4. Launch the Forge profile and join the server.

### Server owners

1. Install a Forge 1.7.10 dedicated server.
2. Stop the server.
3. Place the Xenofactions jar in the server `mods/` folder.
4. Start once to generate `config/hfr.cfg` and related data files.
5. Stop the server and review the configuration. Start with the documented `XENOFACTIONS_*` categories.
6. Restart after making configuration changes.

Data files created or used by the mod include faction/world data managed by the mod, `config/stonedrops.json` for custom stone drops, and market/player-protection JSON files loaded by server startup systems.

## Quick Start

```text
/c create Example Nation
/c claim Capital
/c sethome
/c setwarp market
/c flag listflags
/c balance
```

A typical faction flow is:

1. Create or join a faction with `/c create <name>` or `/c apply <faction>`.
2. Claim a city with `/c claim <city name>`.
3. Build prestige-producing structures and keep enough prestige for upkeep.
4. Use `/c sethome`, `/c setwarp <name>`, and `/c allywarp <ally>` for travel.
5. Manage members with `/c applicants`, `/c accept <player>`, `/c promote <player>`, and `/c kick <player>`.
6. Use diplomacy commands such as `/c ally`, `/c declarewar`, `/c ceasefire`, `/c peace`, and `/c surrender` when server rules allow.

For a fuller walkthrough, see [`docs/getting-started.md`](docs/getting-started.md).

## Configuration

Configuration is generated in `config/hfr.cfg`. The modernized Xenofactions categories are ordered as:

1. `XENOFACTIONS_01_MODULES` - feature toggles such as Dynmap, TDM, custom flags, guide book, conquest flags, and new-player protection.
2. `XENOFACTIONS_02_PRESTIGE_GENERATION` - starting prestige, passive generation, generation cap, and structure generation values.
3. `XENOFACTIONS_03_PRESTIGE_COSTS_UPKEEP` - warps, tents, claim upkeep, war declaration cost, active-war upkeep, and surrender tribute.
4. `XENOFACTIONS_04_BANKRUPTCY` - negative-prestige thresholds and upkeep multipliers.
5. `XENOFACTIONS_05_CLAIMS_CITIES` - city radii, spacing, claim names, city transfer, upgrades, and upkeep.
6. `XENOFACTIONS_06_WAR_DIPLOMACY` - war enable defaults, online thresholds, raid grace, cooldowns, and alliance behavior.
7. `XENOFACTIONS_07_NEW_PLAYER_PROTECTION` - PvP grace, keep-inventory grace, and faction build grace.
8. `XENOFACTIONS_08_CUSTOM_FLAGS` - imported image host allowlist, size limits, timeout, redirects, and rate limit.
9. `XENOFACTIONS_09_DYNMAP` - marker set labels, update interval, opacity, line weights, and label details.
10. `XENOFACTIONS_10_*` through `XENOFACTIONS_17_*` - legacy machine, radar, weapon, world, market, chat, general, and debug settings.

See [`docs/configuration.md`](docs/configuration.md) for defaults and server-owner notes.

## Commands

Most player-facing faction commands are under `/clowder` or `/c`:

```text
/c help
/c create <name>
/c apply <faction>
/c claim <city name>
/c city upgrade
/c declarewar <faction>
```

Administrative faction commands are under `/xclowder` or `/xc` and require permission level 3 by default. Other command families include `/cc`, `/xflags`, `/xmap`, `/stonedrop`, `/xmarket`, `/xshop`, `/xmute`, `/xignore`, `/invsee`, and optional `/tdm`.

See [`docs/commands.md`](docs/commands.md) for command syntax verified from source.

## Resources

- GitHub: use this repository's Issues and Releases pages for source, releases, and bug reports.
- Discord: `https://discord.gg/EfrnP8WJtj` is listed in the mod metadata.
- Wiki: no dedicated external wiki was found in this repository; the `/docs` folder is the current bundled documentation.
- CurseForge/Modrinth: no official project URLs were present in the codebase during this documentation pass. Add them here when available.

## Troubleshooting and FAQ

### Do clients need the mod?
Yes. The mod adds blocks, items, GUIs, renderers, sounds, and client assets, so normal multiplayer servers should require the same Xenofactions jar on clients and server.

### Where is the config file?
Forge generates it at `config/hfr.cfg` after the first startup.

### Why are FMU+/vehicle radars not working?
The compatibility bridge is disabled by default through `FxR_enableRadar=false` in `XENOFACTIONS_11_RADAR_FORCEFIELD`. Server owners can enable it, but the default is intentionally conservative for MCHeli compatibility.

### Why is Dynmap not showing markers?
Confirm `enableDynmapIntegration=true`, Dynmap is installed and initialized, and `XENOFACTIONS_09_DYNMAP` settings are valid. The integration is optional and logs a retry warning when Dynmap is unavailable at startup.

### Can custom flags use any image host?
No. Imported custom flags are restricted by `allowedImageHosts` and default to `postimages.org` and `i.postimg.cc`. Use `/c flag seturl <https URL>` after enabling custom flags.

### Is `/c withdraw` available?
The command is parsed, but source currently returns “This command is currently disabled!” instead of withdrawing prestige.

### Is there a guide book?
The code has a guide-book toggle and fallback help references. If guide content or distribution differs on your server, use `/c help` and the documentation in this repository as the authoritative fallback.
