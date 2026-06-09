# Getting Started with Xenofactions

This guide explains the basic gameplay loop for a new player on a Xenofactions server.

## 1. Join or create a faction

Create a faction:

```text
/c create <faction name>
```

Join an existing faction:

```text
/c apply <faction name>
```

Faction officers/leaders can review and accept applicants:

```text
/c applicants
/c accept <player>
/c deny <player>
```

## 2. Claim your first city

Stand where you want the city center and run:

```text
/c claim <city name>
```

City rules are configurable by the server. By default, city radii are tiered from 2 to 6 chunks, city centers must be spaced apart, and city upgrades cost prestige.

Upgrade a city while standing in it:

```text
/c city upgrade
```

Name a claim:

```text
/c nameclaim <claim name>
```

## 3. Earn and spend prestige

Prestige is the faction economy. It is used for upkeep, war, warps, and city growth.

Useful commands:

```text
/c balance
/c deposit <amount>
/c setwarp <name>
/c warp <name>
/c warps
```

`/c withdraw` currently exists in command parsing but is disabled.

## 4. Manage faction identity

Leaders can set presentation and messaging:

```text
/c color <hexadecimal>
/c motd <message>
/c flag <flag>
/c flag seturl <https image URL>
/c flag clear
/c flag reload
```

Use `/c listflags` to browse built-in flags. Imported image flags must pass the server's custom-flag configuration.

## 5. Diplomacy and war

Common diplomacy commands:

```text
/c ally <faction>
/c acceptally <faction>
/c unally <faction>
/c declarewar <faction>
/c ceasefire <faction>
/c acceptceasefire <faction>
/c peace <faction> {city}
/c acceptpeace <faction>
/c surrender <faction>
/c acceptsurrender <faction>
/c defendally <ally>
```

War declarations may be globally disabled by admins, and many war actions have configurable cooldowns and online-player checks.

## 6. Chat and maps

Faction chat:

```text
/cc <message>
/cc faction <message>
/cc alliance <message>
/cc public
```

Claim map item:

```text
/xmap
```

## Tips

- Use `/c help 1` through `/c help 6` in game for command pages.
- Faction names with spaces are stored with underscores in some command paths; when a command fails with a spaced name, try underscores.
- Ask server staff before using war, surrender, city transfer, or custom image features; many are tuned by server policy.
