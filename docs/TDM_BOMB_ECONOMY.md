# TDM BOMB kit economy

BOMB maps can enable a per-map saved-kit economy with `/tdm map economy <map> <true|false>`. Configure enemy-kill and completed CT-defuse rewards with `killscore` and `defusescore`. This currency is personal, server authoritative, separate from team scores and round wins, survives death and bomb-round boundaries, and resets when the map match ends.

`/tdm kit add <red|blue> [map|global] [cost]` saves a whole loadout. Cost `0` is free. Existing `tdm_kits.txt` entries omit `cost` and therefore deserialize as `0`; map-specific lists continue to take precedence over stable-order global fallbacks.

The kit selection and purchase GUI previews the complete saved 36-slot inventory and four armor slots before a player chooses a kit. Preview icons and tooltips use copies of the real saved `ItemStack` data, including stack counts, metadata, and NBT, so modded names, ammunition, attachments, skins, enchantments, and other custom tooltip details remain visible.

BOMB has one round-elimination lifecycle. Every BOMB round starts with the protected 20-second global buying period, and the live timer starts afterward. Legacy BOMB maps saved with `hardcoreRespawns=false` migrate to this lifecycle; the setting remains available only to deathmatch maps.

Every living RED and BLUE participant is teleported to a team spawn and server-authoritatively frozen for the full `PRE_ROUND` countdown. A player who respawns after elimination, or joins during live combat, is also placed at a team spawn in an explicit waiting state. Waiting players are excluded from living-player calculations and cannot move, attack, interact, or pick up items until the next round setup releases them.

BUY_PHASE kit selection and global buy protection are independent. Players who died receive the normal purchase flow. A previous-round survivor with a valid selected kit instead receives a server-authorized choice: reconstruct that entire saved kit for free without changing buy score, or open the ordinary purchase GUI and use its normal pricing rules.

`PRE_ROUND` is world-level global buy time; `RESPAWN_LOCK` is a per-player kit-selection context and may coexist with live play. Both protections use server-authoritative position anchors that zero motion and correct displacement. A respawn-locked player is damage-immune and unable to attack, interact, plant, or pick up items. Its mandatory GUI queues behind Minecraft's death screen, ignores Escape, and is restored if displaced; successful selection immediately removes the kit lock and its effects. If no kit is affordable, the first kit is applied deterministically with a server warning rather than permanently trapping the player.

Buy protection exists only while the authoritative BOMB state is `PRE_ROUND`. It temporarily applies slowness, resistance, and regeneration to eligible living players, and Xenofactions explicitly removes all three effects before changing the state to `LIVE`. The pending-kit set has the narrower meaning that a player still needs to choose a kit; it cannot extend protection into `LIVE`, `BOMB_PLANTED`, `ROUND_END`, team waiting, objective recovery, or map voting. Expired selections, including a round with no free fallback kit, close the buy GUI and clear both the pending entry and all TDM-owned buy effects without purchasing a paid kit.

The buy-menu key and TDM menu button send the same request to the server. The server opens the purchase GUI only during authoritative `PRE_ROUND` buy time and within four blocks of a spawn assigned to the player's actual team; a client packet cannot bypass either check.

The buy phase (`PRE_ROUND`) and the brief `ROUND_END` intermission freeze ordinary world interaction. `WAITING_FOR_TEAMS` and `DISABLED` do not freeze world editing, and map voting does not inherit a stale BOMB interaction lock. Other protection systems, including faction territory, Safezones, and Warzones, continue to apply normally.

Old TDM map NBT has no economy keys and loads with economy disabled and both rewards at zero.

Once Xenofactions has accepted and tracked an HBM CSGO charge planted in configured bombsite A or B, the authoritative server tick watches that exact block in its tracked world and dimension. If the exact CSGO charge disappears during `BOMB_PLANTED`, Xenofactions treats it as a detonation and gives Terrorists the round through the normal result and intermission path; correctness does not depend on Forge reporting an explosion at the block coordinates. A successful CT defuse and Xenofactions lifecycle cleanup invalidate the tracker before removing the objective, so those intentional removals are not classified as detonations. The tracked planted bomb remains protected from ordinary player breaking.

Leaving a world or server clears the client TDM HUD, scores, timers, BOMB state, and spectator camera. The next server must send its own status before any TDM HUD is shown.

# Runtime mode and single-player testing

Changing the selected, enabled map with `/tdm map mode <map> <deathmatch|bomb>` starts a clean match in the requested mode immediately. Changing an unselected map only updates that map's configuration.

BOMB normally waits for at least one eligible RED player and one eligible BLUE player. Operators may explicitly enable the transient development override with `/tdm bombtest on`; this permits one eligible player to exercise the round lifecycle without creating a fake opponent or changing team persistence. `/tdm bombtest off` safely returns a one-team test round to the normal waiting state, and the override resets when the server lifecycle initializes.


## Round-result and plant rewards

The first buy period starts from zero buy score. At authoritative round completion, the winning side receives the configured round-win reward and captured active competitors on the losing side receive `roundLossBuyScoreReward` before the next `PRE_ROUND` period. Teamless observers and late non-participants are excluded. FFA pays the win reward only to the individual winner and the loss reward to every other captured competitor; a no-winner draw pays the loss reward to all captured competitors.

A successful HBM CSGO plant pays `bombPlantBuyScoreReward` to the planter only when Xenofactions accepts the placed block and transitions from `LIVE` to `BOMB_PLANTED`. Attempts, planted-state ticks, defuse/detonation processing, and explosion rewards do not repeat that payment. Defaults are kill **2**, round win **3**, round loss **1**, and bomb plant **1**.
