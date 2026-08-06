# Earth Map world type

## Singleplayer

Choose **Singleplayer → Create New World → More World Options → World Type:
Earth Map → Customize**, select a pack, and create the world. The clean pack is
the default. The create button is intercepted only for `earthmap`; installation
runs on a worker thread and the integrated server starts only after the temporary
save is verified and finalized.

**Clean** is a lightweight 1:16000, 2688×1344 export with primarily solid stone
underground and no Minecraft population. **Populated** is heavier and
modpack-dependent: it retains ores, pools, deposits, and compatible mod plants.
Both are pregenerated and runtime population is always disabled.

> **Do not distribute the populated map until its required-mod list and Forge
> registry preservation have been tested in a clean installation of the intended
> modpack.**

The installed `level.dat` is based on the template, not a new vanilla tag tree.
Consequently top-level Forge/FML registry tags survive. The installer removes
`Data.Player`, patches `LevelName`, `RandomSeed`, `GameType`, `hardcore`,
`allowCommands`, `MapFeatures`, `generatorName`, `generatorVersion`,
`generatorOptions`, and `bonusChest`, and normalizes time/weather metadata.

Inside profile bounds an absent chunk raises a detailed corruption exception.
Outside bounds the fallback contains bedrock at Y=0 and air above. It never
populates, decorates, creates structures, or delegates to vanilla generation.
`PROFILE` border mode applies the authoritative dimension-0 profile bounds;
`CONFIG` leaves configured border values intact, and `OFF` disables Earth-specific
border adoption. Nether and End are untouched.

## Dedicated servers

Install/extract and verify a pack while the world is offline, then set
`level-type=earthmap`. Use `/xfearth status`, `/xfearth packs`, or
`/xfearth verify <pack-id>` for administration; verification never downloads or
extracts a live world. Clients receive ordinary chunks and never need or receive
the archive.

## Manual runtime plan

With real exports available, run `runClient`, create each variant, verify the
progress screen remains responsive, terrain identities are correct, no edge
terrain or second population appears, and reopen each save. For populated, use
the intended full modpack and repeat after removing one required mod; creation
must be blocked with no partial save. These tests cannot be completed without the
owner's binary exports.
