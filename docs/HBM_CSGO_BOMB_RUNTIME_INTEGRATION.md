# HBM CSGO bomb runtime integration

HBM owns the lifecycle of its CSGO charge (`hbm:tile.charge_c4csgo`, with
`hbm:charge_c4csgo` retained as Xenofactions's legacy registry fallback).
Xenofactions owns the result and scoring of the active TDM BOMB round. In
particular, Xenofactions does **not** infer whether a charge was defused or
detonated when its block disappears.

## Runtime IMC contract

HBM sends a Forge 1.7.10 runtime IMC message to the `hfr` mod with key:

```text
xenofactions_tdm_csgo_bomb_result
```

The message value is an `NBTTagCompound` containing:

| Field | NBT type | Meaning |
| --- | --- | --- |
| `result` | string | Exactly `DEFUSED` or `DETONATED`. |
| `dimension` | int | Dimension containing the charge. |
| `x` | int | Charge block X coordinate. |
| `y` | int | Charge block Y coordinate. |
| `z` | int | Charge block Z coordinate. |
| `player_uuid` | string, optional | UUID of the player who completed a defuse. |
| `player_name` | string, optional | Name of the player who completed a defuse. |

The sender must use `FMLInterModComms.sendRuntimeMessage(...)` only in the
CSGO charge's authoritative successful-defuse and committed-detonation
branches. Starting or interrupting a defuse, removing a block for cleanup, and
other HBM explosives are not results. HBM may first check
`Loader.isModLoaded("hfr")`; Xenofactions is not an HBM dependency.

Xenofactions fetches runtime messages with its real `@Mod.Instance`, accepts
only sender `hbm`, validates all required NBT types, and matches the dimension
and exact coordinates against the currently planted A/B objective. Duplicate,
conflicting, unrelated, and stale messages therefore cannot finish another
round. The optional player identity is used only to award the existing defuse
buy score when that player can be resolved safely.

If the tracked block disappears before its message is consumed, Xenofactions
holds the objective during a bounded grace period. If no result arrives, it
logs one warning and leaves the round paused for safe administrative recovery;
it never guesses either team as the winner.
