
# Permissions Documentation

## Overview

All commands and sub-commands have their own permissions node in the form:

`essentialcommands.<command>.<subcommand>`

Grant access to all subcommands using wildcards, like so:

`essentialcommands.<command>.*`

*Note: The config option `use permissions api` must be set to `true` for permissions to have an effect.*

## Command Permission Nodes

| Command                                        | Permission                                    | Description                                                                                                  |
|------------------------------------------------|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| /tpa \<player>                                 | `essentialcommands.tpa`                       | Request to teleport to a player.                                                                             |
| /tpahere \<player>                             | `essentialcommands.tpahere`                   | Request that a player teleports to you.                                                                      |
| /tpaccept \<player>                            | `essentialcommands.tpaccept`                  | Accept player's teleport request.                                                                            |
| /tpdeny \<player>                              | `essentialcommands.tpdeny`                    | Deny Player's teleport request.                                                                              |
| /home set \<home_name>                         | `essentialcommands.home.set`                  | Set a personal home location.                                                                                |
| /home tp \<home_name>                          | `essentialcommands.home.tp`                   | Teleport to your home.                                                                                       |
| /home delete \<home_name>                      | `essentialcommands.home.delete`               | Delete your home.                                                                                            |
| /home list                                     | `essentialcommands.home.tp`                   | List your homes. (click one to teleport)                                                                     |
| /warp set \<warp_name>                         | `essentialcommands.warp.set`                  | Set a server-wide warp location.                                                                             |
| /home tp_other \<player_name> \<home_name>     | `essentialcommands.home_tp_others`            | Teleport to the specified home belonging to the specified player. Generally admin only.                      |
| /home tp_offline \<player_name> \<home_name>   | `essentialcommands.home_tp_others`            | Teleport to the specified home belonging to the specified player, who may be offline. Generally admin only.  |
| /home list_offline \<player_name> \<home_name> | `essentialcommands.home_tp_others`            | List the homes of the specified player, who may be offline. Generally admin only.                            |
| /home overwritehome \<home_name>               | `essentialcommands.home.set`                  | Set a personal home location, overwriting any existing home with the same name.                              |
| /warp tp \<warp_name>                          | `essentialcommands.warp.tp`                   | Teleport to a warp. (grants access to command)                                                               |
| /warp tp \<warp_name>                          | `essentialcommands.warp.tp_named.<warp_name>` | Teleport to the specified warp. (grants access to specific warp)                                             |
| /warp delete \<warp_name>                      | `essentialcommands.warp.delete`               | Delete a warp.                                                                                               |
| /warp list                                     | `essentialcommands.warp.tp`                   | List available warps. (click one to teleport)                                                                |
| /back                                          | `essentialcommands.back`                      | Teleport to your previous location.                                                                          |
| /spawn tp \|\| /spawn                          | `essentialcommands.spawn.tp`                  | Teleport to the server spawn.                                                                                |
| /spawn set                                     | `essentialcommands.spawn.set`                 | Set the server spawn.                                                                                        |
| /nickname set \<nickname>                      | `essentialcommands.nickname.self`             | Set your own nickname to specified MinecraftText.                                                            |
| /nickname set \<target-player> \<nickname>     | `essentialcommands.nickname.others`           | Set target player's nickname to specified MinecraftText.                                                     |
| /nickname clear                                | `essentialcommands.nickname.self`             | Clear your own nickname.                                                                                     |
| /nickname clear \<target-player>               | `essentialcommands.nickname.others`           | Clear target player's nickname.                                                                              |
| /nickname reveal \<player-nickname>            | `essentialcommands.nickname.reveal`           | Get list of players with the provided nickname (String, case-insensitive).                                   |
| /randomteleport \|\| /rtp                      | `essentialcommands.randomteleport`            | Teleport to a random location a preset (in config) distance from the spawn.                                  |
| /fly                                           | `essentialcommands.fly.self`                  | Toggle ability to fly for self.                                                                              |
| /fly \<target-player>                          | `essentialcommands.fly.others`                | Toggle ability to fly for target player.                                                                     |
| /fly speed \<fly_speed>                        | `essentialcommands.fly.self`                  | Change flight speed for self.                                                                                |
| /fly speed reset                               | `essentialcommands.fly.self`                  | Reset flight speed to default for self.                                                                      |
| /fly speed \<target-player> \<fly_speed>       | `essentialcommands.fly.others`                | Change flight speed for target player.                                                                       |
| /fly speed \<target-player> reset              | `essentialcommands.fly.others`                | Reset flight speed to default for target player.                                                             |
| /workbench                                     | `essentialcommands.workbench`                 | Open a workbench (crafting table) screen.                                                                    |
| /grindstone                                    | `essentialcommands.workbench`                 | Open a grindstone screen.                                                                                    |
| /stonecutter                                   | `essentialcommands.workbench`                 | Open a stonecutter screen.                                                                                   |
| /anvil                                         | `essentialcommands.anvil`                     | Open an anvil screen.                                                                                        |
| /enderchest                                    | `essentialcommands.enderchest`                | Open your enderchest screen.                                                                                 |
| /wastebin                                      | `essentialcommands.wastebin`                  | Open an inventory screen that will delete all items placed in it when closed.                                |
| /invuln                                        | `essentialcommands.invuln.self`               | Make yourself invulnerable (cannot take damage).                                                             |
| /invuln \<target-player>                       | `essentialcommands.invuln.others`             | Make the target player invulnerable.                                                                         |
| /top                                           | `essentialcommands.top`                       | Teleport to the top of the highest block at your x,z position.                                               |
| /gametime                                      | `essentialcommands.gametime`                  | Get the current in-game time.                                                                                |
| /day                                           | `essentialcommands.day`                       | Advance the time to the beginning of the next day, if it is nighttime.                                       |
| /night                                         | `essentialcommands.night`                     | Advance the time to nighttime, if it is daytime.                                                             |
| /afk                                           | `essentialcommands.afk`                       | Mark yourself as afk until you interact or use `/afk` again. Grants invuln if `invuln_while_afk` is enabled. |
| /bed                                           | `essentialcommands.bed`                       | Teleport yourself to you vanilla bed spawn / spawnpoint.                                                     |
| /sleep                                         | `essentialcommands.sleep`                     | Start sleeping, no matter where you are!                                                                     |
| /lastPos \<target-player>                      | `essentialcommands.admin.lasPos`              | Get the last possition of the specified (possibly offline) player.                                           |
| /rules                                         | `essentialcommands.rules`                     | Print the rules to the chat for self                                                                         |
| /rules reload                                  | `essentialcommands.rules_reload`              | Reload the rules from the rules file.                                                                        |
| /feed                                          | `essentialcommands.feed.self`                 | Fill your hunger bar & clear exhaustion.                                                                     |
| /feed \<target-player>                         | `essentialcommands.feed.others`               | Fill the target player's hunger bar & clear exhaustion.                                                      |
| /heal                                          | `essentialcommands.heal.self`                 | Fill your health.                                                                                            |
| /heal \<target-player>                         | `essentialcommands.heal.others`               | Fill the target player's health.                                                                             |
| /repair                                        | `essentialcommands.repair`                    | Repair held item.                                                                                            |
| /suicide                                       | `essentialcommands.suicide`                   | Die.
| /extinguish                                    | `essentialcommands.extinguish.self`           | Stop burning on self.                                                                                        |
| /extinguish \<target-player>                   | `essentialcommands.extinguish.others`         | Stop burning on terget player.                                                                               |
| N/A                                            | `essentialcommands.nickname.style.color`      | Allows setting colorful nicknames.                                                                           |
| N/A                                            | `essentialcommands.nickname.style.fancy`      | Allows setting nicknames that have special formatting (italic, bold, etc.)                                   |
| N/A                                            | `essentialcommands.nickname.style.hover`      | Allows setting nicknames that show text on hover.                                                            |
| N/A                                            | `essentialcommands.nickname.style.click`      | Allows setting nicknames that execute an action on click.                                                    |
| /essentialcommands config reload               | `essentialcommands.config.reload`             | Reload essentialcommands config.                                                                             |

## Rules/Config Bypass Permissions

These permissions allow players to bypass rules defined in the [Essential Commands config](Config-Documentation).

Permission | Description
-----------|------------
`essentialcommands.bypass.teleport_delay` | Ignore `teleport_delay`.
`essentialcommands.bypass.allow_teleport_between_dimensions` | Ignore `allow_teleport_between_dimensions`.
`essentialcommands.bypass.teleport_interrupt_on_damaged` | Ignore `teleport_interrupt_on_damaged`.
`essentialcommands.bypass.teleport_interrupt_on_move` | Ignore `teleport_interrupt_on_move`.

## Types

### MinecraftText

Essentially, any value that works for `/tellraw`'s message field. (JSON text or string enclosed by quotes)

You can use a tellraw generator like [MinecraftJson](https://www.minecraftjson.com/) to create this JSON text with a graphical interface and preview.

Examples: `"Alexandra"`, `{"text":"Alex","color":"green","bold":true}`
