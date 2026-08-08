# Command Reference

Commands below were verified from source files in `src/main/java/com/hfr/command` and registration in `MainRegistry.serverStarting`.

## Permissions

Minecraft Forge 1.7.10 commands use permission levels when `getRequiredPermissionLevel()` is implemented.

- **Player commands:** permission level 0 or explicit `canCommandSenderUseCommand=true`.
- **Moderator/admin commands:** usually permission level 2 or 3, or vanilla `CommandBase` defaults when no override is present.
- `/xclowder`, `/xmarket`, `/xplayer`, `/xshop`, and `/xcum` use permission level 3.
- `/invsee` uses permission level 2.

## Factions: `/clowder` and `/c`

Usage: `/clowder help`; aliases: `/clowder`, `/c`; permission level: 0.

### Basic information

| Command | Purpose |
| --- | --- |
| `/c help {page}` | Show in-game help pages. |
| `/c create <name>` | Create a faction. Blocked only while a faction-creation cooldown is active. |
| `/c disband <faction name>` | Leader-only regular disband. Requires the exact faction name as confirmation, warns members, then blocks the leader from creating factions for 7 real days and other current/offline members for 3 real days. Applications and joining remain allowed. |
| `/c info {faction}` | Show your faction or another faction. |
| `/c list` | List factions. |
| `/c comrades` | List faction members. |
| `/c alliance`, `/c allies`, or `/c allylist` | Show allied factions and pending alliance offers. |
| `/c balance` | Show faction prestige/balance. |

### Membership

| Command | Purpose |
| --- | --- |
| `/c apply <faction>` | Apply to join a faction. |
| `/c applicants` | List applications. |
| `/c accept <player>` | Accept an application. |
| `/c deny <player>` | Deny an application. |
| `/c leave` | Leave your faction. |
| `/c kick <player>` | Kick a member. |
| `/c promote <player>` | Promote a member to officer. |
| `/c demote <player>` | Demote an officer. |
| `/c owner <player>` | Transfer ownership. |

### Identity and flags

| Command | Purpose |
| --- | --- |
| `/c rename <name>` | Rename your faction. |
| `/c color <hexadecimal>` | Set faction color. |
| `/c motd <message>` | Set faction message of the day. |
| `/c listflags {page}` | List built-in flags. |
| `/c flag <flag>` | Set a built-in faction flag. |
| `/c flag seturl <https URL>` | Import a custom flag image if enabled and allowed. |
| `/c flag clear` | Clear custom flag metadata. |
| `/c flag reload` | Reload a cached custom flag image. |

### Claims, cities, and travel

| Command | Purpose |
| --- | --- |
| `/c claim <city name>` | Claim/found a city at your location. |
| `/c city upgrade` | Upgrade the city where you are standing. |
| `/c city cancelmove` | Leader-only: cancel the pending relocation without changing the City Center or its claims. |
| `/c city recovermove` | Leader-only: issue a replacement token for the active server-authorized relocation. |
| `/c nameclaim <name>` | Name the current claim. |
| `/c sethome` | Set faction home. |
| `/c home` | Teleport to faction home. |
| `/c setallywarp` | Set ally warp point. |
| `/c allywarp <faction>` | Warp to an allied faction's ally warp. |
| `/c setwarp <name>` or `/c addwarp <name>` | Create a named faction warp. |
| `/c delwarp <name>` | Delete a faction warp. |
| `/c warp <name>` | Use a faction warp. |
| `/c warps` | List faction warps. |
| `/c gracebuild` | Use faction build grace when enabled. |

### Economy

| Command | Purpose |
| --- | --- |
| `/c deposit <amount>` | Deposit prestige/resources into the faction system. |
| `/c withdraw <amount>` | Parsed but currently disabled in source. |

### Diplomacy and war

| Command | Purpose |
| --- | --- |
| `/c ally <faction>` or `/c befriend <faction>` | Request an alliance. |
| `/c acceptally <faction>` or `/c acceptfriend <faction>` | Accept an alliance request. |
| `/c unally <faction>` or `/c unfriend <faction>` | Break an alliance. |
| `/c enemy <faction>` | Leader-only one-way enemy marker. It is separate from wars, costs no Prestige, and enemy kills grant faction Prestige. |
| `/c unenemy <faction>` | Leader-only enemy removal request. The enemy relation remains active for 72 real hours before expiring. |
| `/c declarewar <faction>` | Declare war when war mode/rules allow. |
| `/c peace <faction> {city}` | Request peace, optionally including a city transfer. |
| `/c acceptpeace <faction>` | Accept peace. |
| `/c ceasefire <faction>` | Request a ceasefire. |
| `/c acceptceasefire <faction>` | Accept a ceasefire. |
| `/c surrender <faction>` | Offer surrender. |
| `/c acceptsurrender <faction>` | Accept surrender. |
| `/c defendally <ally>` | Join an ally's defense when enabled. |
| `/c retreat` | Retreat from conflict context. |

## Faction administration: `/xclowder` and `/xc`

Usage: `/xclowder help`; aliases: `/xclowder`, `/xc`; permission level: 3.

| Command | Purpose |
| --- | --- |
| `/xc forcejoin <faction>` or `/xc fj <faction>` | Force the sender into a faction. |
| `/xc forcekick <player>` or `/xc fk <player>` | Force-kick a player from their faction. |
| `/xc forcedisband <faction>` or `/xc fd <faction>` | Force-disband a faction. |
| `/xc forcerename <name>` or `/xc fr <name>` | Force-rename the sender's faction. |
| `/xc hijack` | Override faction leadership. |
| `/xc deletedata` | Delete all clowder/faction data. Use with extreme caution. |
| `/xc disband <faction>` | Admin disband path with confirmation behavior. |
| `/xc rename <name>` | Admin rename path. |
| `/xc setclaim <wild/safe/war> <s/c> <radius>` or `/xc sc ...` | Set wilderness/safezone/warzone claims in square (`s`) or circle (`c`) radius. |
| `/xc addprestige <faction> <amount>` or `/xc ap ...` | Add or subtract prestige. |
| `/xc newplayerprotection` | Toggle starter protection if the module is enabled. |
| `/xc resetnewplayerprotection` | Reset starter protection timers. |
| `/xc endnewplayerprotection` | End starter protection timers. |
| `/xc resetbuildgrace <faction>` or `/xc rbg <faction>` | Reset faction build grace. |
| `/xc endbuildgrace <faction>` or `/xc ebg <faction>` | End faction build grace. |
| `/xc clearcreationcooldown <player-or-uuid>` or `/xc resetcreationcooldown ...` | Clear one online or offline player creation cooldown. UUIDs, live/cached profiles, and stored names are supported; ambiguity fails closed. |
| `/xc warenable` | Enable war declarations. |
| `/xc wardisable` | Disable war declarations and clear active wars. |
| `/xc skipwarcooldowns` | Toggle global war cooldown bypass. |
| `/xc skipwarcooldown` | Clear all faction war cooldown timers. |
| `/xc ignorewarcooldowncheck` | Toggle cooldown check bypass. |
| `/xc ignorewaronlinecheck` | Toggle online-member check bypass. |
| `/xc ignorewarstatecheck` | Toggle at-war state check bypass. |
| `/xc enablelegacywar` | Toggle legacy war mode, bypassing modern checks. |

The `/xc factiontimeoutcreationreset <playername>` command also requires permission level 3. It resets only that player's faction creation cooldown, accepts online players and offline players known to the server profile cache or cooldown store, and can be run by an admin player or the server console.

## Faction chat: `/cc`

Usage: `/cc <message>`; permission level: 0.

| Command | Purpose |
| --- | --- |
| `/cc <message>` | Send faction chat message. |
| `/cc f` or `/cc faction` | Use faction channel. |
| `/cc a` or `/cc alliance` | Use alliance channel. |
| `/cc p` or `/cc public` | Return to public chat. |
| `/cc m` or `/cc mute` | Mute faction chat behavior. |
| `/cc u` or `/cc unmute` | Unmute. |

## Persistent command data

`clowder_faction_creation_cooldowns.json` stores UUID-keyed faction creation cooldown expirations plus last-known player names and fallback name entries for unresolved offline members. Expired entries are ignored/cleaned on load or lookup.

## Utility and moderation commands

| Command | Permission | Purpose |
| --- | --- | --- |
| `/xignore <player>` | Player | Toggle ignoring a player. |
| `/xmute <player> <seconds\|perm> [reason]` | Admin/moderator default | Mute a player. |
| `/xunmute <player>` | Admin/moderator default | Unmute a player. |
| `/invsee <player> [armorslots]` | Level 2 | View another player's inventory or armor slots. |
| `/xmap` | Player | Give yourself a claim map item. |
| `/xflags` | Player | Open/list faction flag functionality. |
| `/xmulti` | Player | Utility command registered for all players. |
| `/xdebug <param>` | Player | Debug command registered for all players; server policy should control use. |
| `/c stonedrops [page]` | Player | Read-only list of custom stone drops, including chance, raw chance, item registry name, metadata, stack amount, and Y range. All players and the server console can use this command. |
| `/stonedrop <rarity> [minY] [maxY]` | Admin/moderator default | Administrator command to add held item/block as a custom stone drop. |
| `/stonedrop list` | Admin/moderator default | Administrator command to list configured custom stone drops. |
| `/stonedrop remove <index>` | Admin/moderator default | Administrator command to remove a custom stone drop. |
| `/xshop add <shop>` | Level 3 | Add a shop offer using hotbar slots: slot 1 sold item, next three slots currency. |
| `/xshop delete <shop/index>` | Level 3 | Delete a shop offer. |
| `/xmarket setstock ...` | Level 3 | Admin stock market control. |
| `/xmarket triggerstock ...` | Level 3 | Trigger stock movement/events. |
| `/xmarket setshares ...` | Level 3 | Set player stock shares. |
| `/xmarket getshares ...` | Level 3 | Inspect player stock shares. |
| `/xplayer <mode> [player]` | Level 3 | Player administration utility. |

## Optional TDM: `/tdm`

Registered only when `enableTDM=true`.

Common subcommands parsed by source include:

```text
/tdm help
/tdm maps
/tdm kits
/tdm menu
/tdm vote
/tdm team
/tdm switchteam
/tdm setteam
/tdm add
/tdm remove
/tdm addspawn
/tdm clear
/tdm save
/tdm toggle
/tdm friendlyfire
/tdm autobalance
/tdm forcevote
/tdm forcemapvote
```

Server staff should validate map and kit setup in game because the exact argument requirements depend on TDM manager data.

## Registered-but-not-currently-registered commands

The source contains `CommandOrewand`, `CommandXCum`, and `CommandXCustomImage`, but `MainRegistry.serverStarting` does not register them in the current code path. They are intentionally not documented as usable commands.
