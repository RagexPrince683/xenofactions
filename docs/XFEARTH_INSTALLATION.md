# XenoFactions Earth installation (Phase 1)

`xf_earth` is a guard around an **already pregenerated** Minecraft 1.7.10 Anvil world. It does not generate Earth, read heightmaps, or read WorldPainter `.world` projects. A `.world` project is not a Minecraft save: WorldPainter must export it to Anvil first. Existing exported chunks continue to be loaded by Minecraft's Anvil loader; the provider only handles absent chunks and prevents vanilla terrain appearing around or inside the map.

## Smoke-map adoption

1. Run the WorldPainter smoke profile and export it as a Minecraft 1.7.10 Anvil save.
2. Copy `xenoearth-profile-smoke.json` into the exported save root as `xenoearth-profile.json`.
3. With an NBT editor, set these values in the exported `level.dat`:
   ```text
   Data.generatorName=xf_earth
   Data.generatorVersion=0
   Data.generatorOptions=
   ```
4. On a dedicated server, set `level-type=xf_earth` in `server.properties`.
5. Copy the save into the directory selected by the server's `level-name`.
6. Back up the world before starting it.
7. Start the server and run `/xc earth status` as an operator.
8. Test the smoke map before preview or production.
9. Never start a production Earth world with an empty or incomplete `region` directory.

The default `FAIL` policy deliberately stops access when an expected in-bounds chunk is absent. `VOID` is an explicit diagnostic escape hatch, not a repair. Region-file existence reported by `/xc earth check` does not prove that a particular chunk is present in that region.

Production save data, `.world` projects, and `.mca` files are deployed separately and must not be committed to Xenofactions or bundled in its jar.

## New map-pack workflow

The supported `earthmap` Singleplayer installer, `.xfmap` security model, local-pack
workflow, and bundled release procedure are documented in
[XFEARTH_WORLD_TYPE.md](XFEARTH_WORLD_TYPE.md),
[XFEARTH_MAP_PACK_FORMAT.md](XFEARTH_MAP_PACK_FORMAT.md), and
[XFEARTH_BUNDLED_RELEASE.md](XFEARTH_BUNDLED_RELEASE.md). The older manual
WorldPainter adoption notes above remain useful only for pre-pack saves.
