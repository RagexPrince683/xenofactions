# TDM BOMB kit economy

BOMB maps can enable a per-map saved-kit economy with `/tdm map economy <map> <true|false>`. Configure enemy-kill and completed CT-defuse rewards with `killscore` and `defusescore`. This currency is personal, server authoritative, separate from team scores and round wins, survives death and bomb-round boundaries, and resets when the map match ends.

`/tdm kit add <red|blue> [map|global] [cost]` saves a whole loadout. Cost `0` is free. Existing `tdm_kits.txt` entries omit `cost` and therefore deserialize as `0`; map-specific lists continue to take precedence over stable-order global fallbacks.

Every BOMB round starts with a protected 20-second buying period. The live timer starts afterward. A player may purchase one eligible team kit. If none is selected, the first free eligible kit is assigned; paid kits are never assigned free while economy rules are enabled. Economy-disabled BOMB maps retain the phase but treat every kit as free. DEATHMATCH behavior is unchanged.

The buy phase (`PRE_ROUND`) and the brief `ROUND_END` intermission freeze ordinary world interaction. `WAITING_FOR_TEAMS` and `DISABLED` do not freeze world editing, and map voting does not inherit a stale BOMB interaction lock. Other protection systems, including faction territory, Safezones, and Warzones, continue to apply normally.

Old TDM map NBT has no economy keys and loads with economy disabled and both rewards at zero.
# Runtime mode and single-player testing

Changing the selected, enabled map with `/tdm map mode <map> <deathmatch|bomb>` starts a clean match in the requested mode immediately. Changing an unselected map only updates that map's configuration.

BOMB normally waits for at least one eligible RED player and one eligible BLUE player. Operators may explicitly enable the transient development override with `/tdm bombtest on`; this permits one eligible player to exercise the round lifecycle without creating a fake opponent or changing team persistence. `/tdm bombtest off` safely returns a one-team test round to the normal waiting state, and the override resets when the server lifecycle initializes.
