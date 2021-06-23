
# Essential Commands

Essential Commands is a Minecraft (Fabric) mod that adds several simple commands to the game. \
All commands are [configurable](https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation) and toggleable. \
The mod is purely serverside, and is not required on the client. (but it does work on singleplayer worlds).

## Requires

[![fabric permissions api](https://img.shields.io/github/v/tag/lucko/fabric-permissions-api?label=fabric-permissions-api&style=for-the-badge)](https://github.com/lucko/fabric-permissions-api/releases)

## Downloads

[![Modrinth Downloads](https://img.shields.io/badge/dynamic/json?color=5da545&label=modrinth&prefix=downloads%20&query=downloads&url=https://api.modrinth.com/api/v1/mod/essential-commands&style=flat&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMSAxMSIgd2lkdGg9IjE0LjY2NyIgaGVpZ2h0PSIxNC42NjciICB4bWxuczp2PSJodHRwczovL3ZlY3RhLmlvL25hbm8iPjxkZWZzPjxjbGlwUGF0aCBpZD0iQSI+PHBhdGggZD0iTTAgMGgxMXYxMUgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjxnIGNsaXAtcGF0aD0idXJsKCNBKSI+PHBhdGggZD0iTTEuMzA5IDcuODU3YTQuNjQgNC42NCAwIDAgMS0uNDYxLTEuMDYzSDBDLjU5MSA5LjIwNiAyLjc5NiAxMSA1LjQyMiAxMWMxLjk4MSAwIDMuNzIyLTEuMDIgNC43MTEtMi41NTZoMGwtLjc1LS4zNDVjLS44NTQgMS4yNjEtMi4zMSAyLjA5Mi0zLjk2MSAyLjA5MmE0Ljc4IDQuNzggMCAwIDEtMy4wMDUtMS4wNTVsMS44MDktMS40NzQuOTg0Ljg0NyAxLjkwNS0xLjAwM0w4LjE3NCA1LjgybC0uMzg0LS43ODYtMS4xMTYuNjM1LS41MTYuNjk0LS42MjYuMjM2LS44NzMtLjM4N2gwbC0uMjEzLS45MS4zNTUtLjU2Ljc4Ny0uMzcuODQ1LS45NTktLjcwMi0uNTEtMS44NzQuNzEzLTEuMzYyIDEuNjUxLjY0NSAxLjA5OC0xLjgzMSAxLjQ5MnptOS42MTQtMS40NEE1LjQ0IDUuNDQgMCAwIDAgMTEgNS41QzExIDIuNDY0IDguNTAxIDAgNS40MjIgMCAyLjc5NiAwIC41OTEgMS43OTQgMCA0LjIwNmguODQ4QzEuNDE5IDIuMjQ1IDMuMjUyLjgwOSA1LjQyMi44MDljMi42MjYgMCA0Ljc1OCAyLjEwMiA0Ljc1OCA0LjY5MSAwIC4xOS0uMDEyLjM3Ni0uMDM0LjU2bC43NzcuMzU3aDB6IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGZpbGw9IiM1ZGE0MjYiLz48L2c+PC9zdmc+)](https://modrinth.com/mod/essential-commands)\
[![Curseforge Downloads](https://img.shields.io/badge/dynamic/json?color=f16436&label=CurseForge&prefix=downloads%20&query=downloadCount&url=https://addons-ecs.forgesvc.net/api/v2/addon/475964&logo=CurseForge)](https://www.curseforge.com/minecraft/mc-mods/essential-commands)\
[![GitHub Downloads (all releases)](https://img.shields.io/github/downloads/John-Paul-R/Essential-Commands/total?style=flat&amp;label=GitHub&amp;prefix=downloads%20&amp;color=4078c0&amp;logo=github)](https://github.com/John-Paul-R/Essential-Commands/releases)

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

### Config

  - /essentialcommands config reload

Questions? Contact me in [my Discord server](https://discord.jpcode.dev/).