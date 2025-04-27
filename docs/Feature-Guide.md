# Feature Guide

This guide provides a comprehensive overview of Essential Commands features, organized by functionality rather than by commands or configuration options alone.

## Teleportation Features

### Player-to-Player Teleportation
Essential Commands provides a request-based teleportation system that respects player consent.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Request to teleport to another player | `/tpa <player>` | `essentialcommands.tpa` |
| Request player to teleport to you | `/tpahere <player>` | `essentialcommands.tpahere` |
| Accept teleport request | `/tpaccept <player>` | `essentialcommands.tpaccept` |
| Deny teleport request | `/tpdeny <player>` | `essentialcommands.tpdeny` |
| Cancel teleport request | `/tpcancel <player>` | Same as original teleport |

**Related Config Options:**
- `teleport_delay` - Wait time before teleportation occurs (seconds)
- `teleport_interrupt_on_damaged` - Whether taking damage cancels teleportation
- `teleport_interrupt_on_move` - Whether movement cancels teleportation
- `allow_teleport_between_dimensions` - Allow teleporting across dimensions
- `teleport_request_duration` - How long teleport requests remain valid (seconds)

**Bypass Permissions:**
- `essentialcommands.bypass.teleport_delay` - Ignore teleport delay
- `essentialcommands.bypass.allow_teleport_between_dimensions` - Ignore dimension restrictions
- `essentialcommands.bypass.teleport_interrupt_on_damaged` - Ignore damage interruption
- `essentialcommands.bypass.teleport_interrupt_on_move` - Ignore movement interruption

### Home System
The home system allows players to save and teleport to personal locations.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Set a home | `/home set <name>` | `essentialcommands.home.set` |
| Overwrite existing home | `/home overwritehome <name>` | `essentialcommands.home.set` |
| Teleport to home | `/home tp <name>` | `essentialcommands.home.tp` |
| List homes | `/home list` | `essentialcommands.home.tp` |
| Delete home | `/home delete <name>` | `essentialcommands.home.delete` |
| Teleport to another player's home | `/home tp_other <player> <name>` | `essentialcommands.home_tp_others` |
| List offline player's homes | `/home list_offline <player>` | `essentialcommands.home_tp_others` |

**Related Config Options:**
- `home_limit` - Maximum number of homes a player can have (see [Home Limit](Home-Limit) for details)
- `grant_lowest_numeric_by_default` - Give non-permission players access to the minimum home limit

### Warps
Warps are server-wide teleport locations accessible to all players with permissions.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Create a warp | `/warp set <name>` | `essentialcommands.warp.set` |
| Teleport to warp | `/warp tp <name>` | `essentialcommands.warp.tp` or<br>`essentialcommands.warp.tp_named.<warp_name>` |
| List available warps | `/warp list` | `essentialcommands.warp.tp` |
| Delete a warp | `/warp delete <name>` | `essentialcommands.warp.delete` |

**Related Config Options:**
- `enable_warp` - Enables/disables warp functionality

### Spawn Management
Server-wide spawn point management.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Teleport to spawn | `/spawn` or `/spawn tp` | `essentialcommands.spawn.tp` |
| Set server spawn | `/spawn set` | `essentialcommands.spawn.set` |

**Related Config Options:**
- `enable_spawn` - Enables/disables spawn commands
- `respawn_at_ec_spawn` - Controls when players respawn at the EC spawn point. Accepts these values as an [Expression](Config-Documentation.md#expression):
  - `Never` - Players never respawn at EC spawn (default)
  - `Always` - Players always respawn at EC spawn
  - `NoBed` - Players respawn at EC spawn only when they don't have a bed
  - `SameWorld` - Players respawn at EC spawn if they're in the same world as the spawn
  - `FirstJoin` - Players respawn at EC spawn only on their first join
  
  You can combine these using logical operators, for example: `NoBed OR SameWorld` will use the EC spawn if either condition is true.

### Other Teleportation
Additional teleportation options.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Return to previous location | `/back` | `essentialcommands.back` |
| Teleport to random location | `/rtp` or `/randomteleport` | `essentialcommands.randomteleport` |
| Teleport to your bed | `/bed` | `essentialcommands.bed` |
| Teleport to highest block | `/top` | `essentialcommands.top` |

**Related Config Options:**
- `enable_back` - Enables/disables back command
- `allow_back_on_death` - Whether `/back` works after death
- `persist_back_location` - Whether back location persists across server restarts
- `enable_rtp` - Enables/disables random teleport
- `rtp_radius` - Maximum distance for random teleport
- `rtp_min_radius` - Minimum distance for random teleport
- `rtp_cooldown` - Cooldown between random teleports
- `rtp_max_attempts` - Maximum tries to find valid location
- `rtp_enabled_worlds` - Worlds where RTP is enabled

## Player Abilities & Status

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
- `enable_fly` - Enables/disables flight commands

### Invulnerability
Make players immune to damage.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Toggle invulnerability for self | `/invuln` | `essentialcommands.invuln.self` |
| Toggle invulnerability for others | `/invuln <player>` | `essentialcommands.invuln.others` |

**Related Config Options:**
- `enable_invuln` - Enables/disables invulnerability commands

### AFK (Away From Keyboard)
Mark players as AFK with optional automatic detection.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Toggle AFK status | `/afk` | `essentialcommands.afk` |

**Related Config Options:**
- `enable_afk` - Enables/disables AFK functionality
- `auto_afk_enabled` - Enables automatic AFK detection
- `auto_afk_time` - Time before player is marked AFK automatically
- `afk_prefix` - Text prefix shown for AFK players
- `invuln_while_afk` - Whether AFK players are invulnerable
- `afk_prefix` - Prefix shown for AFK players

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
- `enable_nick` - Enables/disables nickname functionality
- `nickname_prefix` - Prefix shown before nicknames
- `nickname_max_length` - Maximum nickname length
- `nick_reveal_on_hover` - Show real name on nickname hover
- `nickname_above_head` - Show nickname above player's head
- `nicknames_in_player_list` - Show nicknames in tab list

## Utility Commands

### Player Needs
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
| End your life | `/suicide` | `essentialcommands.suicide` |

### World Commands
Commands to interact with the world.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Get current game time | `/gametime` | `essentialcommands.gametime` |
| Skip to day | `/day` | `essentialcommands.day` |
| Skip to night | `/night` | `essentialcommands.night` |
| Find nearby players | `/near` | `essentialcommands.near` |

**Related Config Options:**
- `enable_gametime` - Enables/disables gametime command

### Workbenches
Access crafting and other stations anywhere.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Crafting table | `/workbench` | `essentialcommands.workbench` |
| Grindstone | `/grindstone` | `essentialcommands.workbench` |
| Stonecutter | `/stonecutter` | `essentialcommands.workbench` |
| Anvil | `/anvil` | `essentialcommands.anvil` |
| Enderchest | `/enderchest` | `essentialcommands.enderchest` |
| Waste disposal | `/wastebin` | `essentialcommands.wastebin` |

**Related Config Options:**
- `enable_workbench` - Enables/disables workbench commands
- `enable_anvil` - Enables/disables anvil command
- `enable_enderchest` - Enables/disables enderchest command
- `enable_wastebin` - Enables/disables wastebin command

### Player Trading
Direct trading between players.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Trade with player | `/trade <player>` | `essentialcommands.trade` |

## Server Information

### Rules Management
Display and manage server rules.

| Feature | Commands | Permissions |
|---------|----------|------------|
| View rules | `/rules` | `essentialcommands.rules` |
| Reload rules file | `/rules reload` | `essentialcommands.rules_reload` |

### MOTD (Message of the Day)
Server welcome message.

| Feature | Commands | Permissions |
|---------|----------|------------|
| View MOTD | `/motd` | - |

**Related Config Options:**
- `enable_motd` - Enables/disables MOTD command
- `motd` - The message shown to players

## Sleep Command

| Feature | Commands | Permissions |
|---------|----------|------------|
| Bedless sleeping | `/sleep` | `essentialcommands.sleep` |

**Related Config Options:**
- `enable_sleep` - Enables/disables sleep command
- `sleep_invuln` - Whether players are invulnerable while sleeping
- `sleep_near_monsters` - Whether players can sleep with monsters nearby

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
| Get player's last position | `/lastpos <player>` | `essentialcommands.admin.lasPos` |

### Clear Player Data
This command is extremely powerful and should be restricted to server administrators only. It completely erases all data about all players stored by Essential Commands.

| Feature | Commands | Permissions |
|---------|----------|------------|
| Erase all player data | `/clearplayerdata` | OP level 4 only |

**Warning:** This command will remove all homes, nicknames, and other player-specific data from Essential Commands. This action cannot be undone.

### Config Management

| Feature | Commands | Permissions |
|---------|----------|------------|
| Reload config | `/essentialcommands config reload` | `essentialcommands.config.reload` |

## Permissions System

By default, Essential Commands grants all players access to some reasonable commands, with access to the potentially destructive or powerful ones limited to different levels of OP.

For more control, Essential Commands also supports the Fabric permissions API, which allows choosing exactly which features each player (or group of players) have access to, via a permissions mod like [LuckPerms][luck-perms] and [PlayerRoles][player-roles]. To use the permissions system, enable it in the config by settings `use_permissions_api` to `true`.

| Config Option | Description |
|---------------|-------------|
| `use_permissions_api` | Enable permissions-based access control |
| `ops_bypass_teleport_rules` | Whether server operators bypass teleport restrictions |

When permissions are enabled:
- Each command has its own permission node
- Numeric features (like home limits) use tiered permission nodes
- Operators have all permissions by default

For more detailed information on specific commands and permissions, see [List of Commands & Permissions](List-of-Commands-&-Permissions).

## Visual Customization

| Config Option | Description |
|---------------|-------------|
| `formatting_default` | Default text formatting |
| `formatting_accent` | Accent text formatting |
| `formatting_error` | Error text formatting |

[luck-perms]: https://luckperms.net/wiki/Usage
[player-roles]: https://github.com/NucleoidMC/player-roles
