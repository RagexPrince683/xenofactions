# Gameplay permissions and automation

## Survival and legacy content

The Federal Reserve remains registered as `machine_fed` solely so existing chunks load without a missing-block remap. It is hidden from the creative tab, has no survival recipe or mace progression role, its tile entity is inert, and it no longer produces tax items or Prestige. Existing placed blocks may be removed manually but are not supported gameplay.

Foundation now crafts four blocks from `SIS / III / SIS` (`S` stone bricks, `I` iron ingots). A four-block yield makes the 3x3 and 9x9 structure footprints practical early in faction development while retaining an industrial iron cost. This recipe is part of the centralized survival recipe set and follows `enableSurvivalRecipes`.

Vanilla hoppers satisfy the same central foundation predicate as Foundation/other `BlockSpeedy` blocks. This applies consistently to Grain Mills, Blast Furnaces, Coal Mines, Production Lines, Universities, Windmills, tents and statues. A hopper only replaces the block at its own footprint position.

## Processor automation audit

* **Grain Mill:** wheat inserts into the four processing slots; only completed flour extracts downward.
* **Blast Furnace:** iron/iron ore inserts from the top, coal or coal blocks from a side, and only finished steel extracts downward.
* **Coal Mine:** miners insert from sides; supplies and canaries insert from above; only produced coal extracts downward. Workers, canaries, and supplies cannot be drained.
* **Production Line:** it has no item input recipe. Produced cogs extract downward; the jam/status slot remains protected.
* **Foundry:** already implemented sided insertion (steel above, coal at sides) and output extraction below; it was audited and retained. Its existing direct output push was not broadened.
* **Sawmill, electric furnace, coal generator, windmill, university, temple, and deprecated Federal Reserve:** excluded because they are unfinished, do not expose a legitimate item-processing loop, or are not active item processors. They were not resurrected by this pass.

## Territory permissions

Each territory-owning faction stores two policies: `ally` and `neutral`. Categories are `BUILD` (place blocks), `DESTROY` (break blocks), `CONTAINER` (inventory tile entities), `INTERACT` (other right-clicks), and `SWITCH` (doors, trapdoors, gates, buttons, levers, and pressure plates).

Defaults are:

| Relationship | BUILD | DESTROY | CONTAINER | INTERACT | SWITCH |
| --- | --- | --- | --- | --- | --- |
| Ally | false | false | false | true | true |
| Neutral | false | false | false | false | false |

Use `/c permissions` (or `/c perms`) to view both policies. Officers and the leader may use `/c permissions <ally|neutral> <build|destroy|container|interact|switch> <true|false>`, for example `/c permissions ally switch true`. Ordinary members may view but not modify policy. Relationship, permission, and boolean arguments support tab completion.

Resolution order is owner member behavior, existing war/enemy handling, the territory owner's ally policy, then its neutral policy. Policies are relationship-wide rather than per faction, so diplomacy changes take effect immediately. City Center relocation/destruction, Officer Chest rank checks, flag-foundation protection, Fallen Nation restrictions, raid/mace rules, safezones, and warzones remain stricter and are evaluated by their existing paths.

Permission keys are stored with each faction in existing Clowder NBT. Missing keys retain constructor defaults, making pre-update saves migrate safely. Every mutation marks Clowder saved data dirty.
