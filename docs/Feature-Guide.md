# Feature Guide

This guide provides a comprehensive overview of Essential Commands features, organized by functionality rather than by commands or configuration options alone.

## Teleportation Features

Essential Commands offers a few different commands that perform teleports. The following config options apply to all of them:

- `teleport_delay` - Wait time before teleportation occurs (seconds) - Default: `0.0`
- `teleport_interrupt_on_damaged` - Whether taking damage cancels teleportation - Default: `true`
- `teleport_interrupt_on_move` - Whether movement cancels teleportation - Default: `false`
- `teleport_interrupt_on_move_max_blocks` - Maximum blocks movement allowed before teleport is canceled - Default: `3.0`
- `allow_teleport_between_dimensions` - Allow teleporting across dimensions - Default: `true`
- `teleport_with_followers` - Whether followers teleport with the player - Default: `false`
- `teleport_with_followers_radius` - Maximum radius to look for followers - Default: `100.0`
- `print_teleport_coordinates` - Whether to show teleport coordinates in command chat feedback - Default: `true`
    - see also the related [player profile](#player-profiles) `printTeleportCoordinates`

Some of the restrictions imposed by some of the above options can be bypassed by select players when using a permissions mod, with the following permissions:

- `essentialcommands.bypass.teleport_delay` - Ignore teleport delay
- `essentialcommands.bypass.allow_teleport_between_dimensions` - Ignore dimension restrictions
- `essentialcommands.bypass.teleport_interrupt_on_damaged` - Ignore damage interruption
- `essentialcommands.bypass.teleport_interrupt_on_move` - Ignore movement interruption

Alternatively, if not using a permissions mod, ops can be allowed to bypass _all_ teleport rules with the following config option:

- `ops_bypass_teleport_rules` - Whether server operators bypass teleport restrictions - Default: `true`

### Player-to-Player Teleportation (`/tpa`)
Essential Commands provides a request-based teleportation system that allows any player to teleport to any other player, (or have someone else teleport to them), as long as the other player say 'yes' to the request.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Request to teleport to another player | `/tpa <player>` | `essentialcommands.tpa` |
| Request player to teleport to you | `/tpahere <player>` | `essentialcommands.tpahere` |
| Accept teleport request | `/tpaccept <player>` | `essentialcommands.tpaccept` |
| Deny teleport request | `/tpdeny <player>` | `essentialcommands.tpdeny` |
| Cancel teleport request | `/tpcancel <player>` | Same as original teleport |

**Related Config Options:**
- `teleport_request_duration` - How long teleport requests remain valid (seconds) - Default: `60`

### Player Homes (`/home`)

A `home` is a location that player has saved and given a name with `/home set <home_name>`, and can be returned to with `/home tp <home_name>`. Only the player that created the home can see or teleport to it.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Set a home | `/home set <n>` | `essentialcommands.home.set` |
| Overwrite existing home | `/home overwritehome <n>` | `essentialcommands.home.set` |
| Teleport to home | `/home tp <n>` | `essentialcommands.home.tp` |
| List homes | `/home list` | `essentialcommands.home.tp` |
| Delete home | `/home delete <n>` | `essentialcommands.home.delete` |
| Teleport to another player's home | `/home tp_other <player> <n>` | `essentialcommands.home_tp_others` |
| List offline player's homes | `/home list_offline <player>` | `essentialcommands.home_tp_others` |

**Related Config Options:**
- `home_limit` - Maximum number of homes a player can have (see [Home Limit](Home-Limit) for details) - Default: `[1, 2, 5]`
- `grant_lowest_numeric_by_default` - Give non-permission players access to the minimum home limit - Default: `true`

### Server Warps (`/warp`)
Warps are server-wide teleport locations accessible to all players with permissions.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Create a warp | `/warp set <n>` | `essentialcommands.warp.set` |
| Teleport to warp | `/warp tp <n>` | `essentialcommands.warp.tp` or<br>`essentialcommands.warp.tp_named.<warp_name>` |
| List available warps | `/warp list` | `essentialcommands.warp.tp` |
| Delete a warp | `/warp delete <n>` | `essentialcommands.warp.delete` |

**Related Config Options:**
- `enable_warp` - Enables/disables warp functionality - Default: `true`

### Server Spawn
Server-wide spawn point management.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Teleport to spawn | `/spawn` or `/spawn tp` | `essentialcommands.spawn.tp` |
| Set server spawn | `/spawn set` | `essentialcommands.spawn.set` |

**Related Config Options:**
- `enable_spawn` - Enables/disables spawn commands - Default: `true`
- `respawn_at_ec_spawn` - Controls when players respawn at the EC spawn point. Accepts these values as an [Expression](Config-Documentation.md#expression) - Default: `Never`
  - `Never` - Players never respawn at EC spawn (default)
  - `Always` - Players always respawn at EC spawn
  - `NoBed` - Players respawn at EC spawn only when they don't have a bed
  - `SameWorld` - Players respawn at EC spawn if they're in the same world as the spawn
  - `FirstJoin` - Players respawn at EC spawn only on their first join
  
  You can combine these using logical operators, for example: `NoBed OR SameWorld` will use the EC spawn if either condition is true.

### Other Teleportation Features

#### Back Command
Return to your previous location.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Return to previous location | `/back` | `essentialcommands.back` |

**Related Config Options:**
- `enable_back` - Enables/disables back command - Default: `true`
- `allow_back_on_death` - Whether `/back` allows players to jump back to their death location - Default: `false`
- `persist_back_location` - Whether back location persists across server restarts - Default: `false`

#### Random Teleport
Teleport to a random location in the world.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Teleport to random location | `/rtp` or `/randomteleport` | `essentialcommands.randomteleport` |

**Related Config Options:**
- `enable_rtp` - Enables/disables random teleport - Default: `true`
- `rtp_center` - Where to center the region of available locations - Default: `Spawn`
  - Also accepts comma-separated coordinates in `x,z` format, e.g. `500,1000`
- `rtp_radius` - Maximum distance for random teleport - Default: `1000`
- `rtp_min_radius` - Minimum distance for random teleport - Default: Same as `rtp_radius` (`1000`)
- `rtp_cooldown` - Cooldown between random teleports (seconds) - Default: `30`
- `rtp_max_attempts` - Maximum tries to find valid location - Default: `15`
- `rtp_enabled_worlds` - Worlds where RTP is enabled - Default: `overworld`
  - valid values are the registry keys of worlds that exist on your server. In vanilla, that's: `minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`
  - any number of entries may be provided, separated by commas, e.g. `overworld,the_nether,the_end`
  - milelage may vary in The End, as the RTP algorithm currently struggles to quickly find safe locations in void-heavy worlds

#### Bed Command
Teleport to your bed or spawnpoint.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Teleport to your bed or spawnpoint | `/bed` | `essentialcommands.bed` |

**Related Config Options:**
- `enable_bed` - Enables/disables bed command - Default: `false`

#### Top Command
Teleport to the highest block at your current position.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Teleport to highest block | `/top` | `essentialcommands.top` |

**Related Config Options:**
- `enable_top` - Enables/disables top command - Default: `true`

## Player Abilities, Status, and Display

### Flight
Control player flight abilities.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Toggle flight for yourself | `/fly` | `essentialcommands.fly.self` |
| Toggle flight for others | `/fly <player>` | `essentialcommands.fly.others` |
| Set flight speed | `/fly speed <speed>` | `essentialcommands.fly.self` |
| Set others' flight speed | `/fly speed <player> <speed>` | `essentialcommands.fly.others` |
| Reset flight speed | `/fly speed reset` | `essentialcommands.fly.self` |
| Reset others' flight speed | `/fly speed <player> reset` | `essentialcommands.fly.others` |

**Related Config Options:**
- `enable_fly` - Enables/disables flight commands - Default: `true`
- `fly_max_speed` - Maximum allowed flight speed - Default: `5`

### Invulnerability
Make players immune to damage.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Toggle invulnerability for self | `/invuln` | `essentialcommands.invuln.self` |
| Toggle invulnerability for others | `/invuln <player>` | `essentialcommands.invuln.others` |

**Related Config Options:**
- `enable_invuln` - Enables/disables invulnerability commands - Default: `true`

### AFK (Away From Keyboard)
Mark players as AFK with optional automatic detection.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Toggle AFK status | `/afk` | `essentialcommands.afk` |

**Related Config Options:**
- `enable_afk` - Enables/disables AFK functionality - Default: `true`
- `auto_afk_enabled` - Enables automatic AFK detection - Default: `true`
- `auto_afk_time` - Time before player is marked AFK automatically - Default: `PT15M` (15 minutes)
- `afk_prefix` - Text prefix shown for AFK players - Default: `[AFK]` (gray)
- `invuln_while_afk` - Whether AFK players are invulnerable - Default: `false`

### Nicknames
Customize player display names.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Set your nickname | `/nickname set <nickname>` | `essentialcommands.nickname.self` |
| Set other's nickname | `/nickname set <player> <nickname>` | `essentialcommands.nickname.others` |
| Clear your nickname | `/nickname clear` | `essentialcommands.nickname.self` |
| Clear other's nickname | `/nickname clear <player>` | `essentialcommands.nickname.others` |
| Find player by nickname | `/nickname reveal <nickname>` | `essentialcommands.nickname.reveal` |

**Additional Permissions:**
- `essentialcommands.nickname.style.color` - Use colored nicknames
- `essentialcommands.nickname.style.fancy` - Use formatted nicknames (bold, italic)
- `essentialcommands.nickname.style.hover` - Use hover effects on nicknames
- `essentialcommands.nickname.style.click` - Use click actions on nicknames

**Related Config Options:**
- `enable_nick` - Enables/disables nickname functionality - Default: `true`
- `nickname_prefix` - Prefix shown before nicknames - Default: `~` (red)
- `nickname_max_length` - Maximum nickname length - Default: `32`
- `nick_reveal_on_hover` - Show real name on nickname hover - Default: `true`
- `nickname_above_head` - Show nickname above player's head - Default: `false`
- `nicknames_in_player_list` - Show nicknames in tab list - Default: `true`

## Utility Commands

### Player-Focused
Commands to help with player survival needs.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Fill hunger | `/feed` | `essentialcommands.feed.self` |
| Fill other's hunger | `/feed <player>` | `essentialcommands.feed.others` |
| Heal yourself | `/heal` | `essentialcommands.heal.self` |
| Heal other player | `/heal <player>` | `essentialcommands.heal.others` |
| Extinguish fire on self | `/extinguish` | `essentialcommands.extinguish.self` |
| Extinguish fire on others | `/extinguish <player>` | `essentialcommands.extinguish.others` |
| Repair held item | `/repair` | `essentialcommands.repair` |
| Die | `/suicide` | `essentialcommands.suicide` |

**Related Config Options:**
- `enable_feed` - Enables/disables feed command - Default: `true`
- `enable_heal` - Enables/disables heal command - Default: `true`
- `enable_extinguish` - Enables/disables extinguish command - Default: `true`
- `enable_repair` - Enables/disables repair command - Default: `true`
- `enable_suicide` - Enables/disables suicide command - Default: `true`

## Sleep Command

Sleep anywhere, without a bed!

| Feature | Commands | Permissions |
|---------|----------|------------|
| Bedless sleeping | `/sleep` | `essentialcommands.sleep` |

**Related Config Options:**
- `enable_sleep` - Enables/disables sleep command - Default: `false`
- `sleep_invuln` - Whether players are invulnerable while sleeping - Default: `false`
- `sleep_near_monsters` - Whether players can sleep with monsters nearby - Default: `false`

### World Commands
Commands to interact with the world.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Get current game time | `/gametime` | `essentialcommands.gametime` |
| Skip to day | `/day` | `essentialcommands.day` |
| Skip to night | `/night` | `essentialcommands.night` |
| Find nearby players | `/near` | `essentialcommands.near` |

**Related Config Options:**
- `enable_gametime` - Enables/disables gametime command - Default: `true`
- `enable_day` - Enables/disables day command - Default: `true`
- `enable_night` - Enables/disables night command - Default: `true`
- `enable_near` - Enables/disables near command - Default: `true`
- `near_command_default_radius` - Default search radius for near command - Default: `200`
- `near_command_max_radius` - Maximum search radius for near command - Default: `200`

### Workbenches
Access crafting and other stations anywhere.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Crafting table | `/workbench` | `essentialcommands.workbench` |
| Grindstone | `/grindstone` | `essentialcommands.workbench` |
| Stonecutter | `/stonecutter` | `essentialcommands.workbench` |
| Anvil | `/anvil` | `essentialcommands.anvil` |
| Enderchest | `/enderchest` | `essentialcommands.enderchest` |
| A 'chest' that deletes items | `/wastebin` | `essentialcommands.wastebin` |

**Related Config Options:**
- `enable_workbench` - Enables/disables workbench commands - Default: `true`
- `enable_anvil` - Enables/disables anvil command - Default: `false`
- `enable_enderchest` - Enables/disables enderchest command - Default: `true`
- `enable_wastebin` - Enables/disables wastebin command - Default: `true`

## Server Information

### Rules Management
Display and manage server rules.

| Feature | Commands | Permissions |
|---------|----------|------------|
| View rules | `/rules` | `essentialcommands.rules` |
| Reload rules file | `/rules reload` | `essentialcommands.rules_reload` |

**Related Config Options:**
- `enable_rules` - Enables/disables rules command - Default: `true`

### MOTD (Message of the Day)
Server welcome message.

| Feature | Commands | Permissions |
|---------|----------|------------|
| View MOTD | `/motd` | - |

**Related Config Options:**
- `enable_motd` - Enables/disables MOTD command - Default: `false`
- `motd` - The message shown to players - Default: `<yellow>Welcome to our server <blue>%player:displayname%</blue>!\nPlease read the rules.</yellow>`

## Player Profiles
Players can customize their personal Essential Commands experience through profiles.

| Feature | Commands | Example |
|---------|----------|---------|
| Set text formatting | `/essentialcommands profile set formattingDefault <value>` | `/ec profile set formattingDefault gold` |
| Set accent formatting | `/essentialcommands profile set formattingAccent <value>` | `/ec profile set formattingAccent light_purple` |
| Toggle teleport coordinates | `/essentialcommands profile set printTeleportCoordinates <true/false>` | `/ec profile set printTeleportCoordinates true` |

**Note:** Changes to profiles only affect the player who makes them and persist across server restarts.

## Admin Commands

### Last Position
This admin command allows tracking a player's most recent position, even if they're offline.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Get player's last position | `/lastpos <player>` | `essentialcommands.admin.lastPos` |

### Clear Player Data
This command is extremely powerful and should be restricted to server administrators only. It completely erases all data about all players stored by Essential Commands.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Erase all player data | `/clearplayerdata` | OP level 4 only |

**Related Config Options:**
- `enable_delete_all_player_data` - Enables/disables clear player data command - Default: `true`

**Warning:** This command will remove all homes, nicknames, and other player-specific data from Essential Commands. This action cannot be undone.

### Config Management

| Feature | Commands | Permissions |
|---------|----------|------------|
| Reload config | `/essentialcommands config reload` | `essentialcommands.config.reload` |

## Permissions System

By default, Essential Commands grants all players access to some reasonable commands, with access to the potentially destructive or powerful ones limited to different levels of OP.

For more control, Essential Commands also supports the Fabric permissions API, which allows choosing exactly which features each player (or group of players) have access to, via a permissions mod like [LuckPerms][luck-perms] and [PlayerRoles][player-roles]. To use the permissions system, enable it in the config by settings `use_permissions_api` to `true`.

| Config Option | Description | Default |
|---------------|-------------|---------|
| `use_permissions_api` | Enable permissions-based access control | `false` |
| `recheck_player_ability_permissions_on_dimension_change` | Re-verify player ability permissions when changing dimensions | `false` |

When permissions are enabled:
- Each command has its own permission node
- Numeric features (like home limits) use tiered permission nodes
- Operators have all permissions by default

For more detailed information on specific commands and permissions, see [List of Commands & Permissions](List-of-Commands-&-Permissions).

## Visual Customization

| Config Option | Description | Default |
|---------------|-------------|---------|
| `formatting_default` | Default text formatting | `gold` |
| `formatting_accent` | Accent text formatting | `light_purple` |
| `formatting_error` | Error text formatting | `red` |

## Miscellaneous Options

| Config Option | Description | Default |
|---------------|-------------|---------|
| `check_for_updates` | Whether to check for mod updates | `true` |
| `broadcast_to_ops` | Whether to broadcast certain actions to operators | `false` |
| `register_top_level_commands` | Register commands at top level (e.g., `/home` vs. `/ec home`) | `true` |
| `excluded_top_level_commands` | List of commands to exclude from top-level registration | `[]` |
| `language` | Default language for mod messages | `en_us` |

[Read more about languages and translations](Language).

[luck-perms]: https://luckperms.net/wiki/Usage
[player-roles]: https://github.com/NucleoidMC/player-roles
