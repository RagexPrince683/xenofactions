# Building a bundled Earth release

Real archives are intentionally absent from Git. Supply both:

```bat
gradlew jarWithEarthMaps ^
  -PearthCleanMap="C:/EarthMaps/earth-16k-clean.xfmap" ^
  -PearthPopulatedMap="C:/EarthMaps/earth-16k-populated.xfmap"
```

The expected artifact is
`build/libs/Xenofactions-2.1.7-with-earthmaps.jar`. The task fails if either pack
is missing, malformed, misidentified, not format 1 / Minecraft 1.7.10, or lacks
the profile/template. It generates `builtin-earthmaps.json` with pack ID,
resource path, byte size, SHA-256, and display order.

Confirm packaging with:

```sh
jar tf build/libs/Xenofactions-*-with-earthmaps.jar | grep assets/hfr/earthmaps/bundled
```

Both `.xfmap` resources and `builtin-earthmaps.json` must appear. Ordinary
`gradlew build` needs no maps and leaves bundled choices unavailable. Set
`-PincludeBundledEarthMaps=true` only when intentionally putting both maps in the
normal artifact.

Troubleshooting: a bad hash requires replacing/rebuilding the archive; missing
mods require installing every manifest content mod; extraction failures should
leave no final save; missing in-bounds chunks indicate an incomplete/corrupt
export. Never bypass populated-pack registry and required-mod testing.
