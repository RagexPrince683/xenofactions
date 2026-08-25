# HBM CSGO bomb runtime integration

Xenofactions resolves the objective by exact registry identity: primary
`hbm:tile.charge_c4csgo`, then legacy `hbm:charge_c4csgo`. It retains the actual
returned `Block`; display and unlocalized names and generic C4 blocks are never
used.

## Hard-tracked lifecycle

An accepted plant records its world, dimension, exact coordinates and block, bombsite, planter, Terrorist role, plant tick, and round identity. It also records the charge metadata, `ForgeDirection`, and supporting-block coordinate. The audited HBM `BlockChargeC4CSGO` inherits `BlockChargeBase`'s attachment behavior without overriding its support check: metadata selects the direction and the supporting face is validated with `world.isSideSolid(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir)`. Xenofactions applies that same Forge 1.7.10 rule without importing HBM classes.

The no-link adapter reads the concrete `TileEntityCharge` hierarchy's actual `started` and `timer` Java fields through cached reflection; it does not use serialized `defuse` data or manufacture terminal tile states. Xenofactions latches a stopped armed fuse or an imminent detonation while the tile still exists, so the conclusion survives HBM removing the block.

Every server tick checks that exact coordinate. Removal begins a five-tick event-ordering grace period. Runtime IMC remains secondary confirmation. HBM can complete a safe dismantle and remove `BlockChargeC4CSGO` between two Xenofactions server observations, so the brief `started: true -> false` state cannot always be sampled. A real defuse has been observed as `timer: 910 -> 908 -> block gone` while the concrete tile was still `TileEntityCharge`.

After explicit results and proven detonation/disarm evidence are considered, an otherwise unknown removal may use the narrowly scoped compatibility fallback. It requires a known last timer of at least five ticks, a still-valid supporting face, an active non-invalidated tracker, and the enabled compatibility setting. Five ticks is only a small margin beyond the existing `timer <= 1` detonation latch: HBM decrements the fuse once per tick and explodes at zero. Proven detonation always wins before this fallback. A missing or non-solid support instead enters recoverable `OBJECTIVE_ERROR`, preventing destruction of the attachment surface from producing a CT victory. Ordinary players remain unable to break the tracked objective.

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

With `XENOFACTIONS_20_TDM.tdmBombUnknownRemovalAsDefuse=true` (the default), a support-valid, safely pre-expiration unknown removal is warning-logged and completed as `BOMB_DEFUSED` for the Counter-Terrorists. The fallback exists specifically because HBM can safely dismantle and remove its CSGO charge between Xenofactions observations. Xenofactions purges the nearby dismantle drop and uses the ordinary round-end path, so the next live round still assigns exactly one fresh bomb to a Terrorist. No individual defuse reward is required when HBM did not identify the defuser.

Set `tdmBombUnknownRemovalAsDefuse=false` for strict integration debugging. The same unknown removal then enters `OBJECTIVE_ERROR`, logs the complete lifecycle and attachment diagnostic, awards neither team, and awaits `/tdm forceroundend`. `tdmBombLifecycleDebug=false` by default and controls detailed transition logs only; warnings and objective errors are never hidden by it.
