# Persist war state and make faction claims dimension-aware

- Fixed warp tents and medical tents in non-overworld dimensions by resolving their owning claim with the tile entity's actual world dimension instead of the implicit overworld.
- Fixed statue and other prestige-building owner detection in non-overworld dimensions so prestige generation is credited to the faction that owns that dimension's claim.
- Fixed prestige-building break cleanup to use the block's world dimension when subtracting generation, preventing non-overworld buildings from looking up overworld claims.
- Added immediate City Center prestige UI refresh when a faction's prestige generation changes, without changing the prestige tick/earning interval.
- Added a City Center GUI `Gain: <amount>/h` line so players can see whether statues and other prestige buildings changed generation.
- Restored ally object references from saved ally names during Clowder NBT load and synchronized ally names before saving, fixing alliances disappearing from runtime commands after server restart.
- Kept the prior dimension-aware city/admin-claim and war-runtime persistence work from this PR.

- Removed dimension-hostile sky visibility checks from tents, statues, and foundation-based machines; operational checks now require the foundation footprint and a clear obstruction plane above the structure instead of vanilla sky access.
- Refreshed city metadata from loaded City Center tile entities before listing cities so `/c info` reports upgraded city levels/radii immediately and includes current prestige generation/net per hour.
- Added allies to `/c info` and added quick `/c allies` and `/c allylist` aliases for checking current allies and pending alliance offers.

## Manual verification notes

- In a Nether/End/LOTR/Middle-earth claim, place a warp tent on valid foundation blocks with the area above the structure clear, run `/c setwarp <name>`, restart, and verify `/c warp <name>` still targets that dimension.
- In a non-overworld city claim, place a statue and each enabled prestige building, then open the City Center GUI and run `/c info`; verify the GUI `Gain` line and `/c info` generation/net lines update within a tick or two without waiting for the hourly prestige interval.
- Break those prestige buildings in the same non-overworld claim and verify the City Center GUI and `/c info` generation drop immediately.
- Upgrade a city, run `/c info`, and verify the city level and radius match the upgraded City Center immediately.
- Create and accept an alliance, restart the server, and verify `/c info`, `/c allies`, `/c alliance`, `/c allywarp`, and ally war defense checks still recognize the ally.
- Load a legacy world missing dimension fields and verify legacy homes/warps still migrate to dimension `0` with clear server log messages.

## Remaining risky assumptions

- Some legacy helper overloads still intentionally default to dimension `0`; new gameplay code should prefer `World`/dimension-aware overloads.
- The City Center GUI now displays prestige generation (`/h`) immediately, but actual prestige balance accrual still follows the configured prestige update interval by design. Foundation-based structures no longer require vanilla sky visibility, but they still require their footprint foundation blocks and a clear obstruction plane above the structure.
