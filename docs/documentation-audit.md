# Documentation Audit Notes

## Undocumented features, commands, configs, and systems discovered

The following items were present in source but were not meaningfully documented in the old README and are now covered in the README or docs:

- Modern `XENOFACTIONS_*` configuration categories for modules, prestige, claims/cities, war/diplomacy, new-player protection, custom flags, Dynmap, and legacy systems.
- Optional Dynmap marker integration through reflection.
- Custom faction flag imports using `/c flag seturl`, `clear`, and `reload` with host allowlists and safety limits.
- City levels, city upgrade costs/upkeep/radii, city spacing, peace city transfers, and surrender city-transfer toggles.
- New-player protection and faction build-grace configuration plus admin reset/end commands.
- War cooldown bypass/admin toggles and legacy war mode.
- `/stonedrop` custom drop system and `config/stonedrops.json` persistence with Y-range support.
- Optional `/tdm` command family controlled by `enableTDM`.
- `/xmap`, `/xflags`, `/xmulti`, `/xignore`, `/xmute`, `/xunmute`, `/invsee`, `/xmarket`, and `/xshop` utility/admin commands.
- The fact that `/c withdraw` is parsed but disabled.
- Registered-vs-unregistered distinction for `CommandOrewand`, `CommandXCum`, and `CommandXCustomImage`.

## Gaps that could not be fully completed

- No official CurseForge, Modrinth, wiki, or GitHub URL was present in metadata or the old README; placeholders now instruct maintainers to add them when available.
- Some legacy xRadar/HFR machine, weapon, stock, entity, and world-border settings are numerous and only partially summarized; generated config comments remain the most complete source for every legacy key.
- `/xplayer`, `/xdebug`, `/xflags`, and `/xmulti` need in-game validation for exact user-facing behavior beyond command registration and top-level syntax.
- TDM subcommand argument shapes depend on runtime map/kit data and manager behavior, so the docs list parsed subcommands but do not guarantee every argument combination.
- Guide-book distribution/content could not be fully validated from documentation alone; docs point users to `/c help` and bundled docs as fallback.
- Release/download links are repository-dependent and were not discoverable from source.
