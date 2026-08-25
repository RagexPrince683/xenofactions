# HBM CSGO bomb runtime integration

Xenofactions resolves the objective by exact registry identity: primary
`hbm:tile.charge_c4csgo`, then legacy `hbm:charge_c4csgo`. It retains the actual
returned `Block`; display and unlocalized names and generic C4 blocks are never
used.

## Hard-tracked lifecycle

An accepted plant records its world, dimension, exact coordinates and block,
bombsite, planter, Terrorist team/role, plant tick/round identity, and the live
tile instance and runtime class. `HbmCsgoChargeIntegration` snapshots HBM's
serialized `timer` fuse and `defuse` hold-progress state while the block exists.
The adapter deliberately has no HBM imports. It logs the concrete production
block and tile Java names once when an installed jar supplies them (production
obfuscation means these names are properties of that jar, not a stable API).

Every server tick checks that exact coordinate. Removal begins a five-tick,
event-ordering-only grace period. A matching runtime result is secondary
confirmation. After the grace period the saved lifecycle/IMC result is applied;
an otherwise unknown removal is warning-logged once and resolves as detonation,
so a valid objective can never leave the HUD or round in `BOMB_PLANTED` forever.
Xenofactions cleanup invalidates the tracker before removing the block and can
therefore never award a cleanup as a result. Ordinary players remain unable to
break the tracked objective.

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
