# Survival Recipes and Content Audit

The optional recipes below are ordinary Forge crafting recipes, so standard crafting-table viewers such as NEI discover them automatically. They are registered only when
`XENOFACTIONS_01_MODULES.enableSurvivalRecipes=true` (the default).

## Survival-worthy content

The audit followed block/item registration into tile entities, GUIs, faction commands, claims, prestige accounting, war handling, and machine processing.

* **Core faction loop:** City Center (`clowder_flag`), Conquest Flag (`clowder_conquerer`), Officer Chest, Medical Tent, and Warp Tent. City Centers found named cities and claims; a crafted blank center is consumed by `/c claim <name>` and replaced with the named placement item. Conquest Flags are established war infrastructure. The chest and tents enforce faction roles or provide claimed-land services.
* **Developed production/prestige support:** Coal Mine, Production Line (`machine_factory`), University, Temple, and Statue. These have operational multiblocks/tile entities, player-facing lore or NEI processing information, and active outputs or prestige effects.
* **Already survival-obtainable and therefore not duplicated:** Grain Mill, Blast Furnace, Steel Foundry, wooden/scaffold/steel/mechanical/electronic/plating components, miner supplies, canary, faction banner, barricade, and box retain their pre-existing recipes. These existing recipes are intentionally not controlled by the new compatibility toggle.

## Recipes added

Each diagram is a 3x3 crafting grid; a space is empty.

| Output | Layout | Key | Balance rationale |
| --- | --- | --- | --- |
| Foundation (4) | `SIS / III / SIS` | S stone bricks, I iron ingots | Large multiblock footprints need an early industrial building material; four blocks per craft controls the iron cost. |
| City Center | `GEG / ODO / IRI` | G gold ingot, E emerald, O obsidian, D diamond, I iron ingot, R redstone | Major founding infrastructure has a substantial one-diamond, one-emerald investment, while later cities still remain replaceable. |
| Conquest Flag | `LWL / GRG / ISI` | L leather, W wool, G gold ingot, R redstone, I iron ingot, S stick | War infrastructure costs gold and iron, but destruction does not demand excessive diamond grinding. |
| Officer Chest | `I I / ICI / IRI` | I iron ingot, C chest, R redstone | A basic role-gated utility priced near other protected storage. |
| Medical Tent | `WWW / WRW / S S` | W wool, R golden apple, S stick | Accessible field infrastructure with a meaningful healing ingredient. |
| Warp Tent | `WWW / PEP / SRS` | W wool, P ender pearl, E eye of ender, S stick, R redstone | Teleport utility appropriately begins after access to Endermen and the Nether. |
| Coal Mine | `SMS / MCM / SIS` | S scaffold component, M mechanical component, C chest, I minecart | Moderate industrial entry cost aligned with its workforce/supply-driven coal output. |
| Production Line | `SCS / MEM / SPS` | S steel component, C crafting table, M mechanical component, E electronic component, P piston | Developed automated production requires the established steel/component chain. |
| University | `BEB / SDS / BKB` | B bookshelf, E electronic component, S stone bricks, D diamond, K crafting table | High prestige/science generation is gated by books, electronics, and one diamond. |
| Temple | `QGQ / SBS / ODO` | Q quartz block, G gold block, S stone bricks, B bookshelf, O obsidian, D diamond | The strongest configured prestige generator uses late-Nether materials and valuable blocks. |
| Statue | ` Q  / QEQ / SGS` | Q quartz block, E emerald, S stone bricks, G gold block | A smaller passive prestige source is cheaper than major machines but remains an investment. |

All recipe leaves are obtainable vanilla materials or existing craftable Xenofactions components. The dependency graph is one-way (vanilla materials → components → infrastructure), with no recipe output required to make itself.

## Deliberate exclusions

The complete registered-content audit excluded the following rather than generating recipes from registry names:

* **Internal/generated blocks:** `clowder_cap`, blast-door dummy, seal hatch, crop blocks, and multiblock internals are placed or managed by code and are not player crafting outputs.
* **Admin/economy infrastructure:** Market is unbreakable and backs the operator-managed stock/shop economy; the debug block, debug item, administration wands, out-of-bounds wand, and internal capsule remain non-craftable.
* **Legacy claim presentation:** the Big Flag and cap belong to legacy/generated claim representation; current city founding uses the named City Center. The faction banner already has a recipe and is cosmetic rather than infrastructure.
* **Incomplete or disconnected machines:** radar/legacy defense, seals and blast door, hydro core, net, coal generator, battery, windmill, waterwheel, diesel generator, alloy machine, and registered-but-disabled machine/weapon fields were not promoted merely because a class, renderer, or registry entry exists. Some retain isolated mechanics, but their broader power/resource chains are incomplete or legacy.
* **Decorative/building blocks:** mud, rope, temporary/asphalt blocks, Wall Art, HESCO/palisade/wall variants, Berlin wall, and similar props are decorative, generated, independently sourced, or outside the core faction loop. Existing recipes (for example barricades) remain untouched.
* **Legacy content families:** missiles, naval/railgun charges, vehicle/gun parts and kits, nuclear materials, canisters/fluids, armor, grenades, food jokes, cassettes, multipliers, repair parts, and other old HFR items are incomplete, externally dependent, debug-oriented, or unrelated to established Xenofactions faction gameplay. Existing recipes and machine outputs remain unchanged.
* **Runtime currencies and outputs:** prestige is data rather than an item. Science, cogs, scrolls, tax/coins, flour, coal-mine workforce/supplies, and other machine products are generated/consumed by their established systems; direct crafting would bypass those systems. Rice/food and already-craftable supplies retain their old acquisition paths.

Disabling the toggle only omits the eleven recipes above and returns `/c claim` to its prior free named-token behavior. It never gates blocks/items, changes registry names/IDs, removes pre-existing recipes, or changes placed tile entities and saved data.

The Federal Reserve is legacy-only and intentionally has no recipe. See [gameplay permissions and automation](gameplay-permissions-and-automation.md) for its compatibility treatment, hopper foundations, and processor audit.
