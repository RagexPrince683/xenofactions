# TDM BOMB kit economy

BOMB maps can enable a per-map saved-kit economy with `/tdm map economy <map> <true|false>`. Configure enemy-kill and completed CT-defuse rewards with `killscore` and `defusescore`. This currency is personal, server authoritative, separate from team scores and round wins, survives death and bomb-round boundaries, and resets when the map match ends.

`/tdm kit add <red|blue> [map|global] [cost]` saves a whole loadout. Cost `0` is free. Existing `tdm_kits.txt` entries omit `cost` and therefore deserialize as `0`; map-specific lists continue to take precedence over stable-order global fallbacks.

Hardcore-respawn BOMB maps start each round with the protected 20-second global buying period, and the live timer starts afterward. Non-hardcore BOMB maps skip `PRE_ROUND` and its countdown entirely: the round becomes `LIVE` immediately and each living player receives a mandatory per-life kit selection instead. The same mandatory selection is created after every real Forge respawn on non-hardcore maps, including DEATHMATCH and respawns during `LIVE` or `BOMB_PLANTED`.

`PRE_ROUND` is world-level global buy time; `RESPAWN_LOCK` is a per-player kit-selection context and may coexist with live play. Both protections use server-authoritative position anchors that zero motion and correct displacement. A respawn-locked player is invisible, damage-immune, and unable to attack, interact, plant, or pick up items. Its mandatory GUI queues behind Minecraft's death screen, ignores Escape, and is restored if displaced; successful selection immediately removes the kit lock and its effects. If no kit is affordable, the first kit is applied deterministically with a server warning rather than permanently trapping the player.

Fresh objective assignment on a non-hardcore BOMB round is deferred until an eligible Terrorist completes mandatory selection. A live recovery check preserves the one-objective invariant and prevents later kit inventory replacement from deleting the bomb.

Buy protection exists only while the authoritative BOMB state is `PRE_ROUND`. It temporarily applies invisibility, slowness, resistance, and regeneration to eligible living players, and Xenofactions explicitly removes all four effects before changing the state to `LIVE`. The pending-kit set has the narrower meaning that a player still needs to choose a kit; it cannot extend protection into `LIVE`, `BOMB_PLANTED`, `ROUND_END`, team waiting, objective recovery, or map voting. Expired selections, including a round with no free fallback kit, close the buy GUI and clear both the pending entry and all TDM-owned buy effects without purchasing a paid kit.

Hardcore observer invisibility and buy-phase invisibility are independent systems. Hardcore observation uses the entity spectator flag for a player eliminated during the active round, while buy protection uses a potion effect and is cleaned at every buy-phase boundary regardless of the map's `hardcoreRespawns` setting. Dead entities are neither teleported nor prompted during buy setup; the normal Forge respawn events queue setup for Minecraft's replacement live player entity.

The buy phase (`PRE_ROUND`) and the brief `ROUND_END` intermission freeze ordinary world interaction. `WAITING_FOR_TEAMS` and `DISABLED` do not freeze world editing, and map voting does not inherit a stale BOMB interaction lock. Other protection systems, including faction territory, Safezones, and Warzones, continue to apply normally.

Old TDM map NBT has no economy keys and loads with economy disabled and both rewards at zero.

Once Xenofactions has accepted and tracked an HBM CSGO charge planted in configured bombsite A or B, the authoritative server tick watches that exact block in its tracked world and dimension. If the exact CSGO charge disappears during `BOMB_PLANTED`, Xenofactions treats it as a detonation and gives Terrorists the round through the normal result and intermission path; correctness does not depend on Forge reporting an explosion at the block coordinates. A successful CT defuse and Xenofactions lifecycle cleanup invalidate the tracker before removing the objective, so those intentional removals are not classified as detonations. The tracked planted bomb remains protected from ordinary player breaking.

Leaving a world or server clears the client TDM HUD, scores, timers, BOMB state, and spectator camera. The next server must send its own status before any TDM HUD is shown.

# Runtime mode and single-player testing

Changing the selected, enabled map with `/tdm map mode <map> <deathmatch|bomb>` starts a clean match in the requested mode immediately. Changing an unselected map only updates that map's configuration.

BOMB normally waits for at least one eligible RED player and one eligible BLUE player. Operators may explicitly enable the transient development override with `/tdm bombtest on`; this permits one eligible player to exercise the round lifecycle without creating a fake opponent or changing team persistence. `/tdm bombtest off` safely returns a one-team test round to the normal waiting state, and the override resets when the server lifecycle initializes.
