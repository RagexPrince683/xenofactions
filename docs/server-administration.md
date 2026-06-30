# Server Administration Guide

## First startup checklist

1. Install Xenofactions on both server and clients.
2. Start the Forge server once to generate `config/hfr.cfg`.
3. Stop the server and review the `XENOFACTIONS_01_MODULES` toggles first.
4. Decide whether war starts enabled with `warEnabledDefault` or is controlled manually by `/xc warenable` and `/xc wardisable`.
5. Review prestige generation, upkeep, and bankruptcy values before opening a public world.
6. If using Dynmap, install Dynmap separately and keep `enableDynmapIntegration=true`.
7. If allowing custom flags, review `allowedImageHosts`, image dimensions, file size, redirects, timeout, and rate limits.
8. If using TDM, keep `enableTDM=true`; otherwise disable it to avoid registering `/tdm`.

## Recommended policy decisions

- **War windows:** Because admins can globally enable/disable war declarations, many servers pair `/xc warenable` with scheduled war periods.
- **Custom flags:** Restrict hosts to image CDNs you trust. The default allowlist only includes Postimages hosts.
- **New-player protection:** Disabled by default. Enable it before launch if your rules require starter PvP/keep-inventory grace.
- **Free raid:** The legacy `freeRaid=true` default ignores raidability checks. Review this setting carefully for protected-claim servers.
- **Radar bridge:** Leave `FxR_enableRadar=false` unless you have tested the FMU+/vehicle radar integration with your modpack.

## Common staff commands

```text
/xc help
/xc warenable
/xc wardisable
/xc addprestige <faction> <amount>
/xc setclaim <wild/safe/war> <s/c> <radius>
/stonedrop list
/invsee <player>
/xmute <player> <seconds|perm> [reason]
```

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
