
<div align="center">

<!-- <img alt="Example Icon" src="src/main/resources/assets/essential_commands/icon.jpg" width="128"> -->

# Essential Commands

Configurable, permissions-backed utility commands for Fabric servers.

[![Release](https://img.shields.io/github/v/release/John-Paul-R/essential-commands?style=for-the-badge&include_prereleases&sort=semver)][releases]
[![Available For](https://img.shields.io/badge/dynamic/json?label=Available%20For&style=for-the-badge&color=34aa2f&query=$[:]&url=https%3A%2F%2Fwww.jpcode.dev%2Fessentialcommands%2Fsupported_mc_versions.json)][modrinth:files]

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/essential-commands?color=00AF5C&label=modrinth&style=for-the-badge&logo=modrinth)][modrinth:files]
[![Curseforge Downloads](https://img.shields.io/badge/dynamic/json?color=f16436&style=for-the-badge&label=CurseForge&query=downloadCount&url=https://www.fibermc.com/api/v1.0/ForeignMods/475964&logo=CurseForge)][curseforge:files]
[![GitHub Downloads (all releases)](https://img.shields.io/github/downloads/John-Paul-R/Essential-Commands/total?style=for-the-badge&amp;label=GitHub&amp;prefix=downloads%20&amp;color=4078c0&amp;logo=github)][releases]

</div>

## Description

Essential Commands is a Minecraft (Fabric) mod that adds several simple commands to the game. \
All commands are [configurable][config-docs] and toggleable. \
The mod is purely serverside, and is not required on the client. (but it does work on singleplayer worlds).

## Permissions

Essential Commands supports permissions mods like LuckPerms!
All commands and sub-commands have their own permissions node in the form:

`essentialcommands.<command>.<subcommand>`

*Note: The config option `use_permissions_api` must be set to `true` for permissions to have an effect.* ([Config Docs](https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation)) ([Permissions Docs](https://github.com/John-Paul-R/Essential-Commands/wiki/List-of-Commands-&-Permissions))

## Commands

All of these commands support automatic tab completion using Minecraft's new commands system.

See the [full List of Commands & Permissions](https://github.com/John-Paul-R/Essential-Commands/wiki/List-of-Commands-&-Permissions).

### Spawn

  - /spawn
  - /spawn set

### Teleport requests

  - /tpa \<target-player>
  - /tpahere \<target-player>
  - /tpaccept \<target-player>
  - /tpdeny \<target-player>

### Player Homes

  - /home set \<home-name>
  - /home tp \<home-name>
  - /home delete \<home-name>
  - /home list

### Warps

  - /warp set \<warp-name>
  - /warp tp \<warp-name>
  - /warp delete \<warp-name>
  - /warp list

### Back

  - /back

Want to teleport to where you died? Enable `allow_back_on_death` in the [config][config-docs].

### Nickname

  - /nickname set \<nickname>
  - /nickname clear
  - /nickname reveal \<nickname>

### Random Teleport (/wild equivalent)

  - /randomteleport
  - /rtp

### Workbench

  - /workbench
  - /anvil
  - /enderchest
  - /stonecutter
  - /grindstone
  - /wastebin

### Kitchen sink

  - /afk
  - /fly
  - /fly \<target-player>
  - /invuln
  - /invuln \<target-player>
  - /top
  - /day
  - /gametime

### Config

  - /essentialcommands config reload

## License

Essential Commands is open-sourced software licenced under the [MIT license][license].

## Discord

Questions? Contact me in [my Discord server][discord].

[curseforge:files]: https://www.curseforge.com/minecraft/mc-mods/essential-commands/files
[modrinth:files]: https://modrinth.com/mod/essential-commands/versions
[releases]: https://github.com/John-Paul-R/essential-commands/releases
[license]: LICENSE
[discord]: https://discord.jpcode.dev/
[config-docs]: https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation
