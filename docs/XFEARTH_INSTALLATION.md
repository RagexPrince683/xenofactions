# XenoFactions Earth installation

## Native external-raster earth2000 generation

XenoFactions can now generate the clean 1:2000 overworld without WorldPainter. The five source PNGs remain external: they are neither copied into `src/main/resources` nor included in the normal JAR. Put an exported source package at `xenofactions/earthmaps/sources/earth2000/` under the server root, or set `earthSourceDirectory` to another source-root directory. The package must contain `HeightMap20k.png`, `BiomeMap20k.png`, `WaterMap20k.png`, `Ice20k.png`, `globecover20k.png`, and `xenoearth-source.json`.

Create the package from a MinecraftEarthMapXF checkout with:

```sh
python tools/build_xfearth_source.py --profile earth2000 --images images --output generated/earth2000-source
```

The exporter validates each PNG's 21,504 x 10,752 dimensions and records its SHA-256. On the server, `/xc earth source status` shows the configured location and `/xc earth source verify earth2000` checks the complete manifest, exact profile geometry, dimensions, and every hash before generation is allowed.

Run the pregenerator from a temporary, non-Earth bootstrap server as a level-4 operator:

```text
/xc earth pregen start earth2000
/xc earth pregen status
/xc earth pregen pause
/xc earth pregen resume
/xc earth pregen cancel
```

Generation is a conservative single-writer background job, not a server-tick task. It visits regions deterministically from `r.-21.-11.mca` through `r.20.10.mca`, clips the edge regions, and writes all 903,168 chunks (1,344 by 672). Raster access uses a configurable bounded LRU of 512 x 512 source tiles. Clean generation includes height/bathymetry, water, biome IDs, GlobCover surfaces, rivers, ice, bedrock, filler and stone; population, vegetation, caves, ores, lava and structures remain disabled.

Output first goes to `earth2000.xfearth-generating`. Its ownership marker and `xfearth-pregen.json` record the manifest hash, generator version, current/completed regions, chunk totals and timestamps. Pause/cancel/interruption never marks it complete. Resume rejects changed sources or generator versions. XF never deletes this partial directory and never overwrites an existing `earth2000` world. After generation, XF verifies every in-bounds chunk, writes `xenoearth-profile.json`, a 1.7.10 `level.dat`, and installation metadata, removes the ownership marker, and atomically renames the directory to `earth2000`. Set `level-name=earth2000` and `level-type=earthmap`, then restart the server.

This staged workflow deliberately avoids trying to create 903,168 chunks during Forge's overworld construction. Previously Singleplayer provisioned a selected `.xfmap` before starting its integrated server, while a dedicated server had no equivalent pre-start UI and entered the strict missing-profile/missing-chunk path. The bootstrap-server workflow supplies that missing server-side provisioning step safely. Existing `.xfmap` installation remains supported and unchanged.

For existing map packs, `earthmap` remains a guard around an **already pregenerated** Minecraft 1.7.10 Anvil world. A WorldPainter `.world` project is not a Minecraft save. Existing exported chunks continue to be loaded by Minecraft's Anvil loader; the provider only handles absent chunks and prevents vanilla terrain appearing around or inside the map.

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

The default `FAIL` policy deliberately stops access when an expected in-bounds chunk is absent. `VOID` is an explicit diagnostic escape hatch, not a repair. `/xc earth check <chunkX> <chunkZ>` requires integer chunk coordinates. Region-file existence reported by the command does not prove that a particular chunk is present in that region.

Production save data, `.world` projects, and `.mca` files are deployed separately and must not be committed to Xenofactions or bundled in its jar.

## New map-pack workflow

The supported `earthmap` Singleplayer installer, `.xfmap` security model, local-pack
workflow, and bundled release procedure are documented in
[XFEARTH_WORLD_TYPE.md](XFEARTH_WORLD_TYPE.md),
[XFEARTH_MAP_PACK_FORMAT.md](XFEARTH_MAP_PACK_FORMAT.md), and
[XFEARTH_BUNDLED_RELEASE.md](XFEARTH_BUNDLED_RELEASE.md). The older manual
WorldPainter adoption notes above remain useful only for pre-pack saves.
