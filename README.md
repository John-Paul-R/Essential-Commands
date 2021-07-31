
<div align="center">

<!-- <img alt="Example Icon" src="src/main/resources/assets/essential_commands/icon.jpg" width="128"> -->

# Essential Commands

Configurable, permissions-backed utility commands for Fabric servers.

[![Release](https://img.shields.io/github/v/release/John-Paul-R/essential-commands?style=for-the-badge&include_prereleases&sort=semver)][releases]
[![Available For](https://img.shields.io/badge/dynamic/json?label=Available%20For&style=for-the-badge&color=34aa2f&query=$[:]&url=https%3A%2F%2Fwww.jpcode.dev%2Fessentialcommands%2Fsupported_mc_versions.json)][modrinth:files]

[![Modrinth Downloads](https://img.shields.io/badge/dynamic/json?color=5da545&label=modrinth&query=downloads&url=https://api.modrinth.com/api/v1/mod/essential-commands&style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMSAxMSIgd2lkdGg9IjE0LjY2NyIgaGVpZ2h0PSIxNC42NjciICB4bWxuczp2PSJodHRwczovL3ZlY3RhLmlvL25hbm8iPjxkZWZzPjxjbGlwUGF0aCBpZD0iQSI+PHBhdGggZD0iTTAgMGgxMXYxMUgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjxnIGNsaXAtcGF0aD0idXJsKCNBKSI+PHBhdGggZD0iTTEuMzA5IDcuODU3YTQuNjQgNC42NCAwIDAgMS0uNDYxLTEuMDYzSDBDLjU5MSA5LjIwNiAyLjc5NiAxMSA1LjQyMiAxMWMxLjk4MSAwIDMuNzIyLTEuMDIgNC43MTEtMi41NTZoMGwtLjc1LS4zNDVjLS44NTQgMS4yNjEtMi4zMSAyLjA5Mi0zLjk2MSAyLjA5MmE0Ljc4IDQuNzggMCAwIDEtMy4wMDUtMS4wNTVsMS44MDktMS40NzQuOTg0Ljg0NyAxLjkwNS0xLjAwM0w4LjE3NCA1LjgybC0uMzg0LS43ODYtMS4xMTYuNjM1LS41MTYuNjk0LS42MjYuMjM2LS44NzMtLjM4N2gwbC0uMjEzLS45MS4zNTUtLjU2Ljc4Ny0uMzcuODQ1LS45NTktLjcwMi0uNTEtMS44NzQuNzEzLTEuMzYyIDEuNjUxLjY0NSAxLjA5OC0xLjgzMSAxLjQ5MnptOS42MTQtMS40NEE1LjQ0IDUuNDQgMCAwIDAgMTEgNS41QzExIDIuNDY0IDguNTAxIDAgNS40MjIgMCAyLjc5NiAwIC41OTEgMS43OTQgMCA0LjIwNmguODQ4QzEuNDE5IDIuMjQ1IDMuMjUyLjgwOSA1LjQyMi44MDljMi42MjYgMCA0Ljc1OCAyLjEwMiA0Ljc1OCA0LjY5MSAwIC4xOS0uMDEyLjM3Ni0uMDM0LjU2bC43NzcuMzU3aDB6IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGZpbGw9IiM1ZGE0MjYiLz48L2c+PC9zdmc+)][modrinth:files]
[![Curseforge Downloads](https://img.shields.io/badge/dynamic/json?color=f16436&style=for-the-badge&label=CurseForge&query=downloadCount&url=https://addons-ecs.forgesvc.net/api/v2/addon/475964&logo=CurseForge)][curseforge:files]
[![GitHub Downloads (all releases)](https://img.shields.io/github/downloads/John-Paul-R/Essential-Commands/total?style=for-the-badge&amp;label=GitHub&amp;prefix=downloads%20&amp;color=4078c0&amp;logo=github)][releases]

</div>

## Description

Essential Commands is a Minecraft (Fabric) mod that adds several simple commands to the game. \
All commands are [configurable](https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation) and toggleable. \
The mod is purely serverside, and is not required on the client. (but it does work on singleplayer worlds).

## Permissions

Essential Commands supports permissions mods like LuckPerms!
All commands and sub-commands have their own permissions node in the form:

`essentialcommands.<command>.<subcommand>`

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

### Warps

  - /warp set \<warp-name>
  - /warp tp \<warp-name>
  - /warp delete \<warp-name>

### Back

  - /back

### Nickname

  - /nickname set \<nickname>
  - /nickname clear
  - /nickname reveal \<nickname>

### Random Teleport (/wild equivalent)

  - /randomteleport
  - /rtp

### Kitchen sink

  - /fly
  - /fly \<target-player>
  - /workbench
  - /enderchest

### Config

  - /essentialcommands config reload

## License

Essential Commands is open-sourced software licenced under the [MIT license][license].

## Discord

Questions? Contact me in [my Discord server][discord].

[curseforge:files]: https://www.curseforge.com/minecraft/mc-mods/essential-commands/files/all
[modrinth:files]: https://modrinth.com/mod/essential-commands/versions
[releases]: https://github.com/John-Paul-R/essential-commands/releases
[license]: LICENSE
[discord]: https://discord.jpcode.dev/
