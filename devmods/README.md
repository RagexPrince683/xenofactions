# External development mods

Put normal Forge 1.7.10 release JARs in this directory. `runClient` uses
RetroFuturaGradle to remap their production SRG references to the stable-12 MCP
names used by the development workspace.

If a mod is already an MCP-mapped development JAR (usually named `-dev.jar`),
put it in `devmods/deobf/` instead. Those files are loaded directly and are not
remapped. Never put the same mod in both locations.
