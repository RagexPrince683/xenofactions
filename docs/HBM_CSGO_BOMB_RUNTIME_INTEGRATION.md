# HBM CSGO bomb runtime integration

Xenofactions resolves the objective by exact registry identity: primary
`hbm:tile.charge_c4csgo`, then legacy `hbm:charge_c4csgo`. It retains the actual
returned `Block`; display and unlocalized names and generic C4 blocks are never
used.

## Hard-tracked lifecycle

An accepted plant records its world, dimension, exact coordinates and block, bombsite, planter, Terrorist role, plant tick, and round identity. The no-link adapter reads the concrete tile hierarchy's actual `started` and `timer` Java fields through cached reflection; it does not use serialized `defuse` data or manufacture terminal tile states. Xenofactions latches a stopped armed fuse or an imminent detonation while the tile still exists, so the conclusion survives HBM removing the block.

Every server tick checks that exact coordinate. Removal begins a five-tick event-ordering grace period. Runtime IMC remains secondary confirmation. If neither IMC nor a proven live transition establishes a result, the lifecycle enters the recoverable `OBJECTIVE_ERROR` state and awards nobody rather than guessing. Ordinary players remain unable to break the tracked objective.

## Optional runtime IMC

HBM may send `xenofactions_tdm_csgo_bomb_result` from sender `hbm` with string
`result` (`DEFUSED` or `DETONATED`) and integer `dimension`, `x`, `y`, and `z`.
Optional string `player_uuid`/`player_name` identifies a defuser. Xenofactions
accepts it only for the currently tracked coordinate; stale messages cannot
finish a later round.

## Recovery command

Operators can run:

`/tdm forceroundend <red|blue|terrorist|ct|counterterrorist|abort>`

A winner uses the explicit `ADMIN_FORCED` reason. `abort` changes no score. Both
paths invalidate and remove an objective, clear transient state, enter normal
round-end intermission, and then proceed to buy time or team waiting.

## Live charge observation and custody

The installed CSGO registration resolves to HBM's CSGO charge block and its `TileEntityCharge`-based tile. Xenofactions deliberately does not link those HBM classes: it caches reflective access to the tile hierarchy's real public runtime fields `started` and `timer`. A `true -> false` `started` transition while `timer > 0` latches disarm; because the tile decrements once and explodes at `timer <= 0` in the same update, an armed observation at `timer <= 1` latches imminent detonation. No fictitious terminal tile state or `defuse` NBT key is used.

Bomb stacks are matched by exact registered item identity. Counter-Terrorists and spectators cannot possess them. Stale player stacks and loose objective entities are purged at safe round boundaries, and each normal playable `LIVE` round creates one fresh stack, retrying Terrorist inventories before dropping that single stack at an eligible Terrorist's feet. HBM dismantle drops are removed narrowly around the tracked plant.

If the tracked block disappears without authoritative IMC, a latched disarm, or a latched detonation, Xenofactions enters `OBJECTIVE_ERROR`, logs the complete last observation, awards neither team, and awaits `/tdm forceroundend`. Unknown removal is never treated as an automatic Terrorist detonation.
