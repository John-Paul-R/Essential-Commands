## Essential Commands `v0.42.0` (mc 26.2)

**Features**

- Allow commands to target players by nicknames (#389) (by @ImTau)
    - New config option: `nicknames_as_command_arg` with the possible values `Everywhere`, `EssentialCommandsOnly`, `Never`
- New config option, `nicknames_must_be_unique`, to force unique nicknames

Fixes:

- night/day commands incorrectly upgraded (@arnokeesman)
- fix EssentialsX home conversion breaking for players online during the conversion & more (#399) (by @ImTau) 

Language updates:

- add Polish (pl_pl) localization

--- --- ---

## Essential Commands `v0.41.0` (mc 26.2)

- fix: default permissions under some conditions (#393) by @eclipseisoffline
- fix: translations in list commands (#394) by @arnokeesman
- lang: add Swedish localization (#395) by dotar

--- --- ---

## Essential Commands `v0.40.0-beta1` (mc 26.2.0 snapshot 2)

- upgrade to Minecraft 26.2.0 (snapshot 2) (#391) by @eclipseisoffline
- fix server placeholders (#388) by @arnokeesman 
- resolve unexpected CR box characters in /rules output (#388) by @tiehu
- fix permissions system not handling "Permissions Only, not OP" correctly (#380)
- migrate to Server Translations for langfiles (#384)
- fix server placeholders in MOTD by @arnokeesman
- use modrinth for update checking, enabling per-mc-version accuracy (#12)

--- --- ---

## Essential Commands `v0.39.0` (mc 26.1.1)

Note well that Minecraft has a new versioning scheme. This version of Essential
Commands supports Minecraft 26.1.1, the version following 1.21.11.

This upgrade includes:

- QOL improvements to /home and /warp, allowing `/home <name>` instead of requiring `/home tp <name>`, by @HongMeiIing (#358)
- fix RtpCenter configuration not accepting negative coords by @PalorderSoftWorksOfficial (#373)
- supporting the new non-obfuscated mojang source, by @arnokeesman (#361)
- force-disable the sleep command due to an exploitable bug (until I can find a solution)

--- --- ---

## Essential Commands `v0.38.6` (mc 1.21.9)

- upgrade to Minecraft 1.21.9

--- --- ---

## Essential Commands `v0.38.5` (mc 1.21.8)

- assorted internal dependency and tooling upgrades

--- --- ---

## Essential Commands `v0.38.4` (mc 1.21.8)

- fix playerdata saving mistake that could cause homes to not save under certian conditions
- assorted dependency upgrades to improve mod compatability


--- --- ---

## 0.39.0-mc26.1.1

## Essential Commands `v0.39.0` (mc 26.1.1)

Note well that Minecraft has a new versioning scheme. This version of Essential
Commands supports Minecraft 26.1.1, the version following 1.21.11.

This upgrade includes:

- QOL improvements to /home and /warp, allowing `/home <name>` instead of requiring `/home tp <name>`, by @HongMeiIing (#358)
- fix RtpCenter configuration not accepting negative coords by @PalorderSoftWorksOfficial (#373)
- supporting the new non-obfuscated mojang source, by @arnokeesman (#361)
- force-disable the sleep command due to an exploitable bug (until I can find a solution)

--- --- ---

## 0.38.6-mc1.21.11

## Essential Commands `v0.38.6` (mc 1.21.9)

- upgrade to Minecraft 1.21.9

--- --- ---

## 0.38.6-mc1.21.9

## Essential Commands `v0.38.6` (mc 1.21.9)

- upgrade to Minecraft 1.21.9

--- --- ---

## 0.38.5-mc1.21.8

## Essential Commands `v0.38.5` (mc 1.21.8)

- assorted internal dependency and tooling upgrades

--- --- ---

## 0.38.3-mc1.21.7

## Essential Commands `v0.38.3` (mc 1.21.7)

- fix playerdata saving mistake that could cause homes to not save under certian conditions

--- --- ---

## 0.38.2-mc1.21.7

## Essential Commands `v0.38.2` (mc 1.21.7)

- fix parsing mistake that caused EC player data from some older versions to be lost
  - the offending versions have either been taken down or been marked with large warnings 

--- --- ---

## BROKEN DO NOT USE  0.38.1-mc1.21.7

> [!CAUTION]
> this version contains a bug that will lead to loss of data (like player homes) if loading up worlds from some versions back.
> Use a version without this warning instead if you are upgrading an existing server.
> If you are using EC for the first time, this version is fine.

## Essential Commands `v0.38.1` (mc 1.21.7)

- fix reading certain old EssentialCommands `world_data.dat` formats causing a crash on startup 

--- --- ---

## BROKEN DO NOT USE 0.38.0-mc1.21.7

> [!CAUTION]
> this version contains a bug that will lead to loss of data (like player homes) if loading up worlds from some versions back.
> Use a version without this warning instead if you are upgrading an existing server.
> If you are using EC for the first time, this version is fine.

## Essential Commands `v0.38.0` (mc 1.21.7)

- fix some world data and player data read/write bugs from latest 0.37.4
- fix text formatting regression from 0.37.4 (my... version numbering was really illegal here tbh)

--- --- ---

## BROKEN DO NOT USE 0.37.4-mc1.21.7

> [!CAUTION]
> this version contains a bug that will lead to loss of data (like player homes) if loading up worlds from some versions back.
> Use a version without this warning instead if you are upgrading an existing server.
> If you are using EC for the first time, this version is fine.

## Essential Commands `v0.37.4` (mc 1.21.7)

- update to minecraft 1.21.7
- internal updates to use newer TestParserAPI methods for Essential Commands messages

--- --- ---

## 0.37.4-mc1.21.6

## Essential Commands `v0.37.4` (mc 1.21.6)

- update to minecraft 1.21.6
- feat: don't broadcast afk messages if vanished

--- --- ---

## 0.37.3-mc1.21.5

## Essential Commands `v0.37.3` (mc 1.21.5)

fixes:

- throw an error on startup if `rtp_min_radius` is greater than `rtp_radius`
- deserializing 'null' nickname (somehow?) causes an "invalid player data" error
  for the connecting user

translations:

- updates to `zh_cn` by @MineYuanlu

--- --- ---

## 0.37.2-beta-mc1.21.5

## Essential Commands `v0.37.2-beta` (mc 1.21.5)

> [!WARNING]
> This is a beta release. I _**STRONGLY**_ recommend backing up at least the `world` folder of your server before experimenting with this.
>
> The MC crew changed how a lot of NBT stuff works, which affects how Essential Commands stores information. Still attempting to verify there is no data loss.
> 
> If anyone can send me a message on the [dev room discord](https://discord.jpcode.dev) with whether things do or do not work smoothly for you, I'd appreciate it!

- Spanish Translations by @KelviNosse
- Update to mv 1.21.5 in part by @KrisTheCanadian

--- --- ---

## 0.37.0-mc1.21.4

## Essential Commands `v0.37.0` (mc 1.21.4)

- add "TELEPORT_FOLLOWERS" feature, causing (non-sitting) pets within a configured radius to be teleported with the player (for teleports performed by Essential Commands) by @KrisTheCanadian!
- Update officially to 1.21.4 by @KrisTheCanadian
- pull in a fix for the Fabric permissions API

--- --- ---

## 0.36.1-mc1.21.3

## Essential Commands `v0.36.1` (mc 1.21.3)

- Change bundled `fabric-permissions-api` version to `0.3.3` to benefit from
bugfixes

--- --- ---

## 0.36.0-mc1.21.3

## Essential Commands `v0.36.0` (mc 1.21.3)

- update to mc1.21.2/3 by @arnokeesman
- feat: add config option `PRINT_TELEPORT_COORDINATES` (default value for the profile setting)
- fixes to traditional Chinese (`zh_tw`) localization (#290) by @notlin4
- fix "teleport interrupt on move" message (#290) by @notlin4
- fix keeping flight ability after respawn (#307) by @arnokeesman
- minor improvements to certain teleport errors clarity

--- --- ---

## 0.35.3-mc1.20.1

## Essential Commands `v0.35.3` (mc 1.20.1)

- fix: When creating Taterzens fake player, PlayerDataManager.getByUuid NRE (#279)
- French (fr_fr) translations, by  @MysthZero (#288)
- fix: broken "EC spawn" behavior on first connect (by @arnokeesman in #289)

--- --- ---

## 0.35.2-mc1.21

## Essential Commands `v0.35.2` (mc 1.21.0)

A couple of rapid fire releases! (including patch notes from `v0.35.1`, since these were so close together)

### `v0.35.2`

- Safer `/bed` command teleportation, courtesy of @LittleCircleOO in #285

### `v0.35.1`

- First release with mc 1.21 support! (Big thanks to @petersv5's help in #283)
- Add Chinese (zh_tw) translations, by @yichifauzi (#282)
- (re)fix: platform-specific line separators for /rules
- Additional zh_cn translations by @Silverteal in #270

--- --- ---

## 0.35.2-mc1.20.1

## Essential Commands `v0.35.2` (mc 1.20.1)

A couple of rapid fire releases! (including patch notes from `v0.35.1`, since these were so close together)

### `v0.35.2`

- Safer `/bed` command teleportation, courtesy of @LittleCircleOO in #285

### `v0.35.1`

- Add Chinese (zh_tw) translations, by @yichifauzi (#282)
- (re)fix: platform-specific line separators for /rules
- Additional zh_cn translations by @Silverteal in #270

--- --- ---

## 0.35.1-mc1.21

## Essential Commands `v0.35.1` (mc 1.21.0)

- First release with mc 1.21 support! (Big thanks to @petersv5's help in #283)
- Add Chinese (zh_tw) translations, by @yichifauzi (#282)
- (re)fix: platform-specific line separators for /rules
- Additional zh_cn translations by @Silverteal in #270

--- --- ---

## 0.35.0-mc1.20.4


## Essential Commands `v0.35.0` (mc 1.20.4)

- Additional Chinese translations, by @LittleCircleOO (#267)
- add experimental utility command: `/essentialcommands deleteAllPlayerData`
  - requires permission: op level 4
  - Addressing the use case in #242

Much of the version updates to mc 1.20.4 handled by @arnokeesman.

--- --- ---

## 0.35.0-mc1.20.1


## Essential Commands `v0.35.0` (mc 1.20.1)

- Additional Chinese translations, by @LittleCircleOO (#267)
- add experimental utility command: `/essentialcommands deleteAllPlayerData`
  - requires permission: op level 4
  - Addressing the use case in #242

also, all the utility commands from #248 (by @arnokeesman)

Backport to mc 1.20.1 by @arnokeesman.

--- --- ---

## 0.34.0-mc1.19.4


## Essential Commands `v0.34.0` (mc 1.19.4)

### New Commands

All of these courtesy of @arnokeesman

feed, heal, near, repair, night, suicide, extinguish, flyspeed

`/fly speed [reset|0-5]`


<details><summary><code>/feed [player]</code></summary>

```
enable_feed: true

essentialcommands.feed.self 2
essentialcommands.feed.others 2
```

</details>

<details><summary><code>/heal [player]</code></summary>

```
enable_heal: true

essentialcommands.heal.self 2
essentialcommands.heal.others 2
```

</details>

<details><summary><code>/near [range] [player]</code></summary>

```
enable_near: true
near_command_default_range: 200
near_command_max_range: 200

essentialcommands.near.self 2
essentialcommands.near.others 2
```

</details>

<details><summary><code>/repair [player]</code></summary>

```
enable_repair: true

essentialcommands.repair.self 2
essentialcommands.repair.others 2
```

</details>

<details><summary><code>/night</code></summary>

```
enable_night: true

essentialcommands.night 2
```

</details>

<details><summary><code>/suicide</code></summary>

```
enable_suicide: true

essentialcommands.suicide 0
```

</details>

<details><summary><code>/ext(inguish) [player]</code></summary>

```
enable_extinguish: true

essentialcommands.extinguish.self 2
essentialcommands.extinguish.others 2
```

</details>

### RTP support in the Nether!

Config option:

- `rtp_enabled_worlds` - a list of world registry keys to allow `/rtp` in
- Example: `rtp_enabled_worlds=[overworld,nether]`
- This config option _can_ be reloaded with `/essentialcommands config reload`

new permission:

- `essentialcommands.bypass.randomteleport_cooldown`, requires OP 4 if not using a permissions mod

lang file change:

- `cmd.rtp.error.no_spawn_set` -> `cmd.rtp.error.world_not_enabled`
    - Old `en_us` text: "Not in Overworld."
    - New `en_us` text: "RTP is not enabled in the world '${0}'"

### Additional Features

- allow `/tpahere` to multiple players at once (#238)

### Fixes

- fix auto `/tpaccept`/`/tpdeny` (no target player)
- fix rare crash. (264e86ffe7097741b504391680a9c5e4fb939e78)
- fix: allow reading UTF-16 rules files
- fix: warp suggestions not perms filtered (#235)

[//]: # (---)

[//]: # (This version is available for: `1.20`, `1.19.4`, `1.19.2`, `1.18.2`, `1.17.1`)

[//]: # (&#40;make sure you grab the jar with the matching version in the name&#41;)

--- --- ---

## 0.34.0-mc1.20.2


## Essential Commands `v0.34.0` (mc 1.20.2)

### New Commands

All of these courtesy of @arnokeesman

feed, heal, near, repair, night, suicide, extinguish, flyspeed

`/fly speed [reset|0-5]`


<details><summary><code>/feed [player]</code></summary>

```
enable_feed: true

essentialcommands.feed.self 2
essentialcommands.feed.others 2
```

</details>

<details><summary><code>/heal [player]</code></summary>

```
enable_heal: true

essentialcommands.heal.self 2
essentialcommands.heal.others 2
```

</details>

<details><summary><code>/near [range] [player]</code></summary>

```
enable_near: true
near_command_default_range: 200
near_command_max_range: 200

essentialcommands.near.self 2
essentialcommands.near.others 2
```

</details>

<details><summary><code>/repair [player]</code></summary>

```
enable_repair: true

essentialcommands.repair.self 2
essentialcommands.repair.others 2
```

</details>

<details><summary><code>/night</code></summary>

```
enable_night: true

essentialcommands.night 2
```

</details>

<details><summary><code>/suicide</code></summary>

```
enable_suicide: true

essentialcommands.suicide 0
```

</details>

<details><summary><code>/ext(inguish) [player]</code></summary>

```
enable_extinguish: true

essentialcommands.extinguish.self 2
essentialcommands.extinguish.others 2
```

</details>

### RTP support in the Nether!

Config option:

- `rtp_enabled_worlds` - a list of world registry keys to allow `/rtp` in
- Example: `rtp_enabled_worlds=[overworld,nether]`
- This config option _can_ be reloaded with `/essentialcommands config reload`

new permission:

- `essentialcommands.bypass.randomteleport_cooldown`, requires OP 4 if not using a permissions mod

lang file change:

- `cmd.rtp.error.no_spawn_set` -> `cmd.rtp.error.world_not_enabled`
    - Old `en_us` text: "Not in Overworld."
    - New `en_us` text: "RTP is not enabled in the world '${0}'"

### Additional Features

- allow `/tpahere` to multiple players at once (#238)

### Fixes

- fix auto `/tpaccept`/`/tpdeny` (no target player)
- fix rare crash. (264e86ffe7097741b504391680a9c5e4fb939e78)
- fix: allow reading UTF-16 rules files
- fix: warp suggestions not perms filtered (#235)

[//]: # (---)

[//]: # (This version is available for: `1.20`, `1.19.4`, `1.19.2`, `1.18.2`, `1.17.1`)

[//]: # (&#40;make sure you grab the jar with the matching version in the name&#41;)

--- --- ---

## 0.33.2-mc1.20


## Essential Commands `v0.33.2` (mc 1.20)

### Fixes

- continuation of the fix in EC 0.33.1 around PlayerData being incorrectly reloaded from disk on respawn.

--- --- ---

## 0.33.2-mc1.19.4


## Essential Commands `v0.33.2` (mc 1.19.4)

### Fixes

- continuation of the fix in EC 0.33.1 around PlayerData being incorrectly reloaded from disk on respawn.

--- --- ---

## 0.33.2-mc1.19.2


## Essential Commands `v0.33.2` (mc 1.19.2)

### Fixes

- continuation of the fix in EC 0.33.1 around PlayerData being incorrectly reloaded from disk on respawn.

--- --- ---

## 0.33.2-mc1.18.2


## Essential Commands `v0.33.2` (mc 1.18.2)

### Fixes

- continuation of the fix in EC 0.33.1 around PlayerData being incorrectly reloaded from disk on respawn.

--- --- ---

## 0.33.2-mc1.17.1


## Essential Commands `v0.33.2` (mc 1.17.1)

### Fixes

- continuation of the fix in EC 0.33.1 around PlayerData being incorrectly reloaded from disk on respawn.

--- --- ---

## 0.33.1-mc1.19.4


## Essential Commands `v0.33.1` (1.19.4)

### Fixes

A massive thanks to @arnokeesman for diving into the code and discovering the root cause of this issue. This one threw me through a loop.


fix: `PlayerData` lost on respawn if EC spawn not set

- Resolves #211 (`/back` to death points sometimes does not work)
- Resolves #214 (Active players are sometimes put into AFK state.)

--- --- ---

## 0.33.1-mc1.19.2


## Essential Commands `v0.33.1` (1.19.2)

### Fixes

A massive thanks to @arnokeesman for diving into the code and discovering the root cause of this issue. This one threw me through a loop.


fix: `PlayerData` lost on respawn if EC spawn not set

- Resolves #211 (`/back` to death points sometimes does not work)
- Resolves #214 (Active players are sometimes put into AFK state.)

--- --- ---

## 0.33.1-mc1.18.2


## Essential Commands `v0.33.1` (1.18.2)

### Fixes

A massive thanks to @arnokeesman for diving into the code and discovering the root cause of this issue. This one threw me through a loop.


fix: `PlayerData` lost on respawn if EC spawn not set

- Resolves #211 (`/back` to death points sometimes does not work)
- Resolves #214 (Active players are sometimes put into AFK state.)

--- --- ---

## 0.33.1-mc1.17.1


## Essential Commands `v0.33.1` (1.17.1)

### Fixes

A massive thanks to @arnokeesman for diving into the code and discovering the root cause of this issue. This one threw me through a loop.


fix: `PlayerData` lost on respawn if EC spawn not set

- Resolves #211 (`/back` to death points sometimes does not work)
- Resolves #214 (Active players are sometimes put into AFK state.)

--- --- ---

## 0.33.0-mc1.19.4


## Essential Commands `v0.33.0` (1.19.4)

### New Features

- New [Expressions](https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation#expression) config option type.
- `respawn_at_ec_spawn` enhancements:
  - Add `NoBed` respawn condition option (If a player does not have a bed spawn, they will respawn at the EC spawn)
  - Use the new Expression config option type, allowing values in the form: `NoBed AND SameWorld`
- Log an error on startup if `excluded_top_level_commands` is malformed (#203, by @arnokeesman)
- Add `nickname_above_head` feature and config option. (Max 16 chars, no color) (`1.19.4+`)

### Translations updates

- `ru_ru` (Updated - by @skvoryanich)
- `nl_nl` (Created - by @arnokeesman)
- `ko_kr` (Created - by @CrushedKingoros)

### Fixes

- Fix exception being thrown with no meaningful in-game feedback when attempting to RTP without a spawn having been set. (#197, by @arnokeesman)
- config array parsing: Don't consider `[]` & similar to be `[""]`
- Some styling fixups for teleport destination names in chat feedback. Some  
  destinations that were previously ambiguous will now use more apecific names. (#198, by @arnokeesman)
- account for different line separators in rules file (fixes `CR` characters being rendered in chat in some scenarios) (#206, by @arnokeesman)

--- --- ---

## 0.33.0-mc1.19.2


## Essential Commands `v0.33.0` (1.19.2)

### New Features

- New [Expressions](https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation#expression) config option type.
- `respawn_at_ec_spawn` enhancements:
  - Add `NoBed` respawn condition option (If a player does not have a bed spawn, they will respawn at the EC spawn)
  - Use the new Expression config option type, allowing values in the form: `NoBed AND SameWorld`
- Log an error on startup if `excluded_top_level_commands` is malformed (#203, by @arnokeesman)
- Add `nickname_above_head` feature and config option. (Max 16 chars, no color) (`1.19.4+`)

### Translations updates

- `ru_ru` (Updated - by @skvoryanich)
- `nl_nl` (Created - by @arnokeesman)
- `ko_kr` (Created - by @CrushedKingoros)

### Fixes

- Fix exception being thrown with no meaningful in-game feedback when attempting to RTP without a spawn having been set. (#197, by @arnokeesman)
- config array parsing: Don't consider `[]` & similar to be `[""]`
- Some styling fixups for teleport destination names in chat feedback. Some  
  destinations that were previously ambiguous will now use more apecific names. (#198, by @arnokeesman)
- account for different line separators in rules file (fixes `CR` characters being rendered in chat in some scenarios) (#206, by @arnokeesman)

--- --- ---

## 0.33.0-mc1.18.2


## Essential Commands `v0.33.0` (1.18.2)

### New Features

- New [Expressions](https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation#expression) config option type.
- `respawn_at_ec_spawn` enhancements:
  - Add `NoBed` respawn condition option (If a player does not have a bed spawn, they will respawn at the EC spawn)
  - Use the new Expression config option type, allowing values in the form: `NoBed AND SameWorld`
- Log an error on startup if `excluded_top_level_commands` is malformed (#203, by @arnokeesman)
- Add `nickname_above_head` feature and config option. (Max 16 chars, no color) (`1.19.4+`)

### Translations updates

- `ru_ru` (Updated - by @skvoryanich)
- `nl_nl` (Created - by @arnokeesman)
- `ko_kr` (Created - by @CrushedKingoros)

### Fixes

- Fix exception being thrown with no meaningful in-game feedback when attempting to RTP without a spawn having been set. (#197, by @arnokeesman)
- config array parsing: Don't consider `[]` & similar to be `[""]`
- Some styling fixups for teleport destination names in chat feedback. Some  
  destinations that were previously ambiguous will now use more apecific names. (#198, by @arnokeesman)
- account for different line separators in rules file (fixes `CR` characters being rendered in chat in some scenarios) (#206, by @arnokeesman)

--- --- ---

## 0.33.0-mc1.17.1


## Essential Commands `v0.33.0` (1.17.1)

### New Features

- New [Expressions](https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation#expression) config option type.
- `respawn_at_ec_spawn` enhancements:
  - Add `NoBed` respawn condition option (If a player does not have a bed spawn, they will respawn at the EC spawn)
  - Use the new Expression config option type, allowing values in the form: `NoBed AND SameWorld`
- Log an error on startup if `excluded_top_level_commands` is malformed (#203, by @arnokeesman)
- Add `nickname_above_head` feature and config option. (Max 16 chars, no color) (`1.19.4+`)

### Translations updates

- `ru_ru` (Updated - by @skvoryanich)
- `nl_nl` (Created - by @arnokeesman)
- `ko_kr` (Created - by @CrushedKingoros)

### Fixes

- Fix exception being thrown with no meaningful in-game feedback when attempting to RTP without a spawn having been set. (#197, by @arnokeesman)
- config array parsing: Don't consider `[]` & similar to be `[""]`
- Some styling fixups for teleport destination names in chat feedback. Some  
  destinations that were previously ambiguous will now use more apecific names. (#198, by @arnokeesman)
- account for different line separators in rules file (fixes `CR` characters being rendered in chat in some scenarios) (#206, by @arnokeesman)

--- --- ---

## 0.32.0-mc1.19.4


## Essential Commands `v0.32.0` (mc 1.19.4)

New Features

- Persistent `/back` locations, by @justinbchen
    - Config Option: `persist_back_location`, default `false`

Fixes, minor:

- Fix a bug that could cause some unrelated commands to become non-functional when certain `ENABLE_CommandName` flags were false in the config.
- More accurate RTP calculation time logging

--- --- ---

## 0.32.0-mc1.19.3


## Essential Commands `v0.32.0` (mc 1.19.3)

New Features

- Persistent `/back` locations, by @justinbchen
    - Config Option: `persist_back_location`, default `false`

Fixes, minor:

- Fix a bug that could cause some unrelated commands to become non-functional when certain `ENABLE_CommandName` flags were false in the config.
- More accurate RTP calculation time logging

--- --- ---

## 0.32.0-mc1.18.2


## Essential Commands `v0.32.0` (mc 1.18.2)

New Features

- Persistent `/back` locations, by @justinbchen
    - Config Option: `persist_back_location`, default `false`

Fixes, minor:

- Fix a bug that could cause some unrelated commands to become non-functional when certain `ENABLE_CommandName` flags were false in the config.
- More accurate RTP calculation time logging

--- --- ---

## 0.32.0-mc1.17.1


## Essential Commands `v0.32.0` (mc 1.17.1)

New Features

- Persistent `/back` locations, by @justinbchen
  - Config Option: `persist_back_location`, default `false`

Fixes, minor:

- Fix a bug that could cause some unrelated commands to become non-functional when certain `ENABLE_CommandName` flags were false in the config.
- More accurate RTP calculation time logging

--- --- ---

## 0.31.1-mc1.17.1


## Essential Commands `v0.31.1` (mc 1.17.1)

<p>
This is, in theory, a full backport of EssentialCommands 0.31.1 to Minecraft
1.17.1. This is the first major backport since ~0.17, so there may be
newly-introduced bugs. For this reason, this release has been marked as a beta.
</p>

Changes

- Fix lang usage for RTP's "spawn not set" error message.

--- --- ---

## 0.31.1-mc1.19.3


## Essential Commands `v0.31.1` (mc 1.19.3)

[Commit Log](https://github.com/John-Paul-R/Essential-Commands/compare/e85441a02d056af53f7b2d295f248c5ff110642f...99c3625e45e9428c5a6db7efa15f1fbdbe94469f)

Changes

- Fix lang usage for RTP's "spawn not set" error message.

--- --- ---

## 0.31.1-mc1.18.2


## Essential Commands `v0.31.1` (mc 1.18.2)

<p>
This is, in theory, a full backport of EssentialCommands 0.31.1 to Minecraft
1.18.2. This is the first major backport since ~0.24.x, so there may be
newly-introduced bugs. For this reason, this release has been marked as a beta.
</p>

Changes

- Fix lang usage for RTP's "spawn not set" error message.

--- --- ---

## 0.31.0-mc1.19.3


## Essential Commands `v0.31.0` (mc 1.19.3)

[Commit Log](https://github.com/John-Paul-R/Essential-Commands/compare/c07b2339b2e34cefa3785063d8b354c5e7fe32e8...e85441a02d056af53f7b2d295f248c5ff110642f)

Changes

- (#169) Fix bug that caused AFK to not be cancelled on move in certain configurations. (@Luungooo)
- German localization (@Luungooo)
- EssentialsX warp converter (@disymayufei) (#172, #157)
- Bugfixes to the EssentialsX homes converter. Now supports Nether and End in most circumstances.

--- --- ---

## 0.30.2-mc1.19.3


## Essential Commands `v0.30.2` (mc 1.19.3)

[Commit Log](https://github.com/John-Paul-R/Essential-Commands/compare/d2f5f86041efc11bdf07db5b9f11d5dd3728fcfd...ba9aa4500e8d2e1923c909a506c3b5a027292f69)

Changes

- add `pt_br` translations, courtesy of AnonymozzY on CF
- fix crash on first startup (Missing EssentialCommands.properties) (This would resolve itself on second run, but...)
- fix: `continue` queuedTeleport loop on "moved" interrupt
  - Previously, if a player hit the "teleport interrupt on move" distance threshold on the same tick that the teleport would execute, crash ensued. (_very_ tiny chance of happening, but possible)

--- --- ---

## 0.30.1-mc1.19.3


## Essential Commands `v0.30.1` (mc 1.19.3)

[Commit Log](https://github.com/John-Paul-R/Essential-Commands/compare/cd12dd795bfb8b4265cca1ce966d5a52dd71327b...1493fad4dab49a01493c7fa0e452a0a9dafe4d61)

- Update to 1.19.3 courtesy of @LagPixelLOL (#160)
- Add `bed` command - allows player to teleport to their `spawnpoint`
  - config: `enable_bed` (default `false`)
  - permission: `essentialcommands.bed`
- Add config option `recheck_player_ability_permissions_on_dimension_change` (default `false`). If true, on world change, a player with `fly` or `invuln` enabled, but without the appropriate permission in the new world, will lose the ability upon arrival.
  - This is primarily to better support per-world permissions. 
- Fix bug that always allowed ops to bypass teleport_interrupt_on_move, regardless of whether they had the `bypass.teleport_interrupt_on_move` permission.
- Fixes to `rules` command feedback courtesy of @arnokeesman
- Don't write to the `config/EssentialCommands.properties` file (updating the timestamp), unless there are meaningful changes to be made to the file (#153)
- EssentialsXParser bugfixes (it's still super buggy!)
  - Further improvements potentially coming next release, in large part thanks to @disymayufei (#157)

--- --- ---

## 0.30.0-mc1.19.2


## Essential Commands `v0.30.0` (mc 1.19.x)

- Add ability to use `selector` text tag and Placeholders in nicknames. (permissions-gated)
- Add `/rules`. (Configurable via `config/essential_commands/rules.txt`)
- Fix broken EssentialsXParser (but is still highly experimental)

--- --- ---

## 0.30.0-mc1.19


## Essential Commands `v0.30.0` (mc 1.19)

- Add ability to use `selector` text tag and Placeholders in nicknames. (permissions-gated)
- Add `/rules`. (Configurable via `config/essential_commands/rules.txt`)
- Fix broken EssentialsXParser (but is still highly experimental)

--- --- ---

## 0.29.0-mc1.19.2


## Essential Commands `v0.29.0` (mc 1.19.2)

- Show home/warp name in teleport completion message.
- (#142) Add config option `respawn_at_ec_spawn` for respawning at `spawn`.
  available options:
  - `Always`
  - `SameWorld`
  - `Never`

(this release previously did not include the feature addressing #142, but `0.29.0-mc1.19` did. That has since been rectified. (the current jar attached to this release _does_ include the stated feature))

--- --- ---

## 0.29.0-mc1.19


## Essential Commands `v0.29.0` (mc 1.19)

- Show home/warp name in teleport completion message.
- (#142) Add config option `respawn_at_ec_spawn` for respawning at `spawn`.
  available options:
  - `Always`
  - `SameWorld`
  - `Never`

--- --- ---

## 0.24.5-mc1.18.1


## Essential Commands `v0.24.5` (mc 1.18.1)

EC [`0.24.5-mc1.18.2`](https://github.com/John-Paul-R/Essential-Commands/releases/tag/0.24.5-mc1.18.2) backported to mc1.18.1

--- --- ---

## 0.28.1-mc1.19.2


## Essential Commands `v0.28.1` (mc 1.19.2)

Fix bug that caused `essentialcommands config reload` to fail if run in server console.

--- --- ---

## 0.28.1-mc1.19


## Essential Commands `v0.28.1` (mc 1.19)

Fix bug that caused `essentialcommands config reload` to fail if run in server console.

--- --- ---

## 0.28.0-mc1.19.1


## Essential Commands `v0.28.0` (mc 1.19.1)

First mc1.19.1 release.

Changes:

- Add "formattingDefault" and "formattingAccent" player profile options (`/essentialcommands profile`)

Minor/internal
- Text/msg system rework to accomodate per-player chat styling
- Fix some text styling & formatting bugs
- Internal cleanup of the project, hopefully making things a bit easier for newcomers to reason about.

--- --- ---

## 0.28.0-mc1.19


## Essential Commands `v0.28.0` (mc 1.19)

Changes:

- Add "formattingDefault" and "formattingAccent" player profile options (`/essentialcommands profile`)

Minor/internal
- Text/msg system rework to accomodate per-player chat styling
- Fix some text styling & formatting bugs
- Internal cleanup of the project, hopefully making things a bit easier for newcomers to reason about.

--- --- ---

## 0.27.1-beta-mc1.19


## Essential Commands `v0.27.0-beta` (mc 1.19)

Changes:

- revert player data dir name from `modplayerData` to `modplayerdata` (accidental change)

--- --- ---

## 0.27.0-beta-mc1.19


## Essential Commands `v0.27.0-beta` (mc 1.19)

### CAUTION

This is a beta release and may be unstable. Make sure you have a backup of your world before using.

Changes:

- Add player profiles (`/essentialcommands profile {get/set} <optionKey> <optionValue>`)
  - Right now, the only option is `printTeleportCoordinates` (Resolving #131)
- Fix a text parsing error that could cause unstyled text to be rendered with
  "formatting_default" instead.
- Added config options, allowing easier CommandAliases redirects
   (This resolves #50 & #47 - CommandAliases configs for Essentials-style home/warp syntax  can be found in #50's comments.) 
  - `register_top_level_commands`, default `true`
  - `excluded_top_level_commands`, default `[]`
- Add `/warp tp_other <playerName> <warpName>` (Resolving #100)

Internal changes:
- Fixup a bunch of ECText internal logic.
- Clean up a bunch of PlayerDataFactory logic
- Use `ec$` to namespace all mixin-added fields and methods
- Add tests! Particularly around text parsing and related utilities.
- Make building the project a less headache-inducing process for those without my exact local setup.

--- --- ---

## 0.26.3-mc1.19


## Essential Commands `v0.26.3` (mc 1.19)

Changes:

- Fix bug introduced in `0.26.2` that caused the leading segment of text before a placeholder to be omitted.
- Refactor "Workbench"-style commands. (There should be no functional differences)

--- --- ---

## 0.26.2-mc1.19


## Essential Commands `v0.26.2` (mc 1.19)

Changes:

- Fix a `zh_cn` placeholder, courtesy of @deluxghost
- Update Permissions API to 0.2-SNAPSHOT, the same version `LuckPerms-Fabric-5.4.35` expects
- Update PlaceholderAPI to 2.0.0-beta.7+1.19, the same version `styled-chat-1.3.3+1.19` expects
- Add config option `enable_day` (default `true`)

--- --- ---

## 0.26.1-mc1.19


## Essential Commands `v0.26.1` (mc 1.19)

Changes:

- Fix bug that prevented `/afk` from properly ending timeout-induced afk state.
- Update `zh_cn` lang file, courtesy of @Leo204-LKY
- Fix `/home list_offline`, which was entirely broken, and some additional fixes to `/home tp_offline`

--- --- ---

## 0.26.0-beta-mc1.19


## Essential Commands `v0.26.0-beta` (mc 1.19)

Changes:

- Add "auto afk" feature - Automatically mark players as AFK after a certain period has elapsed.
  - if a player has been marked AFK in this manner (not with the command), they will be excluded
    from the sleep percentage calculation.
- Add option `teleport_interrupt_on_move`, `teleport_interrupt_on_move_max_blocks`
  - Related permission: `teleport_interrupt_on_move`
- Fix `[AFK]` prefix not updating in player list
- Various lang fixes
- Rework Config & Text tech

--- --- ---

## 0.25.0-mc1.19


## Essential Commands `v0.25.0` (mc 1.19)

Changes:

- Add `/afk` command
  - Config Options:
    - `enable_afk` (default true)
    - `afk_prefix` (for chat)
    - `invuln_while_afk` (default false) - If set to true, using `/afk` while in combat is disallowed.
- Fix broken `getNickname` null check.

--- --- ---

## 0.24.5-mc1.18.2


## Essential Commands `v0.24.5` (mc 1.18.2)

Changes:

- Fix broken getNickname null check.

--- --- ---

## 0.24.4-mc1.19


## Essential Commands `v0.24.4` (mc 1.19)

Changes:

- Persist RTP cooldown as `timeUsedRtpEpochMs`, fixing bug that could cause RTP cooldowns to be far to long upon server restart.
- Revamp ECText, using Placeholders to enable interpolation. (#113)
- Fix #111
- Fix #112
- Fix #108

--- --- ---

## 0.24.4-mc1.18.2


## Essential Commands `v0.24.4` (mc 1.18.2)

Changes:

- Persist RTP cooldown as `timeUsedRtpEpochMs`, fixing bug that could cause RTP cooldowns to be far to long upon server restart.
- Fix #111
- Fix #112

--- --- ---

## 0.24.3-mc1.19


## Essential Commands `v0.24.3` (mc 1.19)

Changes:

- Update `zh_cn` translations, courtesy of @Leo204-LKY in #107
- Fix #106 

--- --- ---

## 0.24.3-mc1.18.2


## Essential Commands `v0.24.3` (mc 1.18.2)

Changes:

- Update `zh_cn` translations, courtesy of @Leo204-LKY in #107
- Fix #106 

--- --- ---

## 0.24.2-mc1.19


## Essential Commands `v0.24.2` (mc 1.19)

Changes:

- Fix ECText issue that could prevent EC translations from being initialized correctly. (#105, courtesy of @YanWQ-monad)

--- --- ---

## 0.24.1-mc1.19


## Essential Commands `v0.24.1` (mc 1.19)

Changes:

- Fix bug that caused MOTD to fail to parse TextPlaceholder styles (e.g.
  `<blue>Hello</blue>` would render as raw, uncolored text)

--- --- ---

## 0.24.0-beta-mc1.19


## Essential Commands `v0.24.0-beta` (mc 1.19)

Note: this is a beta release for mc 1.19. Expect some bugs. A more complete release should be out within a week or so. 

Changes:

- Give `stonecutter` and `grindstone` their own perm nodes. (breaking change to permissions for these commands)

--- --- ---

## 0.23.0-mc1.18.2


## Essential Commands `v0.23.0` (mc 1.18.x)

Changes:

- Add `rtp_min_radius` config option. Accepts integer values. Defaults to the value of `rtp_radius` (1000).

--- --- ---

## 0.22.0-mc1.18.2


## Essential Commands `v0.22.0` (mc 1.18.x)

Changes:

- Add `/day`, a command that lets you advance the time to the start of the next
  day, assuming is it not already daytime.

--- --- ---

## 0.21.1-mc1.18.2


## Essential Commands `v0.21.1` (mc 1.18.x)

Changes:

- Fix various bugs affecting RTP.
- Allow EC commands to be executed by command blocks.
- Hook (vanilla) `/teleport` previous location for `/back`.

--- --- ---

## 0.21.0-mc1.18.2


## Essential Commands `v0.21.0` (mc 1.18.2)

Changes:

- Add `/home tp_other`, enabling teleporting to another (presently online) player's home. (intended for admin use) 
  - The permission node is `essentialcommands.home_tp_others`. Requires OP by default.

--- --- ---

## 0.20.4-mc1.18.2


## Essential Commands `v0.20.4` (mc 1.18.2)

Changes:

- _Actually_ Fix RTP on 1.18.2 servers (infinite loop bad!)

--- --- ---

## 0.20.3-mc1.18.2


## Essential Commands `v0.20.3` (mc 1.18.2)

Changes:

- Fix RTP on 1.18.2 servers
- Fix bug that caused a server crash if both `spawn` and `tpa` were disabled.
- Use 1.18.2 mappings & newer Fabric API

--- --- ---

## 0.20.2-mc1.18.1


## Essential Commands `v0.20.2` (mc 1.18.1)

Changes:

  - Fix items potentially getting deleted in Anvil, Grindstone, and Stonecutter commands (#86).
  - Fix display text for all "bench" commands, using Mincrafts built-in translations.

--- --- ---

## 0.20.1-mc1.18.1


## Essential Commands `v0.20.1` (mc 1.18.1)

Changes:

  - Change the way EC grants all perms to OPs.
    - Old version was causing broad mod compatability issues. Investigated and Resolved by @braunly in #94.

--- --- ---

## 0.20.0-mc1.18.1


## Essential Commands `v0.20.0` (mc 1.18.1)

Changes:

  - RTP performance enchancements (Thanks @Wesley1808!) (https://github.com/Wesley1808/ServerCore/issues/16)
  - Russian (ru_ru) translations (thanks **oldbrowze#9618** on Discord!)
  - Add workbench-style commands:
    - `/stonecutter`
    - `/grindstone`
    - `/wastebin`
    - `/anvil`
  - Optional computed permissions for warps. (e.g. `essentialcommands.warp.tp_named.{warp_name}`)

--- --- ---

## 0.19.0-mc1.18.1


## Essential Commands `v0.19.0` (mc 1.18.1)

Changes:

- Add `essential_commands:nickname` placeholder. ([PlacehodlerAPI](https://github.com/Patbox/TextPlaceholderAPI))
- Send motd after player join message. (Should fix a bug where motd would not include role styles from LuckPerms)
- Add Invuln Command (Basically the old `/god`).
- Add Gametime Command (displays in-game time in 24-"hour" format, and tick-time on hover).

--- --- ---

## 0.18.2-mc1.18.1


## Essential Commands `v0.18.2` (mc 1.18.1)

Changes:
  - A mod compatability fix for [StyledChat](https://github.com/Patbox/StyledChat) by [Patbox](https://github.com/Patbox).
    - Fixes #83, a bug where EssentialCommands nicknames would completely overwrite StyledChat's `displayName` setting. 

--- --- ---

## 0.18.1-mc1.18.1


## Essential Commands `v0.18.1` (mc 1.18.1)

A small update to support Minecraft `1.18.1`

Other changes:
  - RTP has been disabled in the nether, as it allowed players to get on
top of the uppermost bedrock layer.
  - Don't allow spamming same target with tp requests.
  - Add tpcancel as per #64. Better backend system for ending tp requests.
  - Fix bug that could prevent config from being generated in some scenarios.

--- --- ---

## 0.17.1-mc1.18-rc3


## Essential Commands `v0.17.1` (mc 1.18-rc3) (1.18 compatible)

A tentative update for Minecraft `1.18-rc3` & Java 17.

--- --- ---

## 0.17.0-mc1.17.1


## Essential Commands `v0.17.0` (mc 1.17.x)

A bunch of community-submitted improvements for this release!

### New

  - [Chinese (zh_cn) localization][zh_cn-file], by [MikhailTapio](https://github.com/MikhailTapio)
  - Feature: Message of the Day (MOTD), by [SonarBeserk](https://github.com/SonarBeserk)

### Bugfixes

(both by [SonarBeserk](https://github.com/SonarBeserk))

  - Change NicknameClearCommand to use the name of target player for feedback (instead of sender player)
  - Correctly display config via `/essentialcommands config display`

[zh_cn-file]: https://github.com/John-Paul-R/Essential-Commands/commit/759791240f244ebdd95e75971a1a5222af4d4f59

--- --- ---

## 0.17.0-mc1.16.5


## Essential Commands `v0.17.0` (mc 1.16.5)

Direct backport of `0.17.0-mc1.17.1`

This version of Essential Commands has incompatibilities with certain older versions of Java.
If the server fails to start, consider updating your Java installation (16 recommended).

## Important Note

EssentialCommands for mc 1.16.5 requires version `0.11.7` (or earlier) of the Fabric Loader. It will *not* work on Fabric Loader v0.12.x.

Note that the Fabric loader is *not* the Fabric API. [Get the Fabric Loader Installer here.](https://fabricmc.net/use/installer/)

--- --- ---

## 0.16.2-mc1.17.1


**Essential Commands** **v0.16.2** (mc `1.17.x`)

**Bugfix:**
  - Changed `/fly` to use PAL, improving compatability with other mods.
  - `/fly` should now always persist until it is manually disabled.
  - Flight can no longer be disabled in creative mode.
  - Using an elytra should no longer grant the wearer the ability to creative-fly.
  - Switching from creative to survival no longer leaves flight enabled, unless the player has `/fly` enabled.

--- --- ---

## 0.16.1-mc1.17


**Essential Commands** **v0.16.1** (mc `1.17.x`)

**Bugfix:**
  - Fix bug that caused `/fly` with the `persist` flag set to true
    to not actually persist when using `/execute in <dimension> run tp <player> <destination>`

--- --- ---

## 0.16.1-mc1.16.5


**Essential Commands** **v0.16.1** (mc `1.16.5`)

Direct backport of `0.16.1-mc1.17`

This version of Essential Commands has incompatabilities with certain older versions of Java.
If the server fails to start, consider updating your Java installation (16 recommended).

--- --- ---

## 0.16.0-mc1.17


**Essential Commands** **v0.16.0** (mc `1.17.x`)

**New:**
- Add command /top
    - Allows teleporting to the world surface (highest non-air block at your current position)
    - Config opt: `enable_top`
    - Permission: `essentialcommands.top
- Add command /essentialcommands config display
    - Shows currently loaded config values.
    - Also, accepts optional argument `config_property`.
      If this arg is provided, only the value of that config option will be returned.
- Add tpahere permission, separate from tpa.
- Add option to allow the effects of `/fly` to persist between deaths, relogs, dimensions, etc.

**Bugfixes:**
- Fix Option change event not firing on option init.
- Fix crash-inducing Java version parsing bug that occured for some early-access Java versions.

--- --- ---

## 0.16.0-mc1.16.5


**Essential Commands** **v0.16.0** (mc `1.16.5`)

Direct backport of `0.16.0-mc1.17`

This version of Essential Commands has incompatabilities with certain older versions of Java.
If the server fails to start, consider updating your Java installation (16 recommended).

--- --- ---

## 0.15.0-4-mc1.16.5


**Essential Commands** **v0.15.0-4** (mc `1.16.5`)

Fix memory leak that occured on some Linux machines.

Crimes against Java were committed for this release.

--- --- ---

## 0.15.0-mc1.17


**Essential Commands** **v0.15.0** (mc `1.17.x`)

New:
- Support multiple home limits via permissions, instead of one global limit in config.
- Command shortcuts for `tpaccept`, `tpdeny`, and `home`.
  (No longer requires specification of home/player name if only one option exists)


--- --- ---

## 0.15.0-mc1.16.5


**Essential Commands** **v0.15.0** (mc 1.16.5)

Direct backport of `0.15.0-mc1.17`

This version of Essential Commands has incompatabilities with certain older versions of Java.
If the server fails to start, consider updating your Java installation.

--- --- ---

## 0.14.0-mc1.17


**Essential Commands** **v0.14.0** (`1.17.x` & `1.16.5`)

New:
- Added highly experimental EssentialsX homes converter to aid in porting spigot servers to Fabric.
- Added Clickable Accept/Reject chat buttons for TPA requests.
- Added command `/tpahere`

Fixed Bugs:
- `home/warp list` commands fail with no feedback in the case whre no homes/warps are set.
- Teleport requests can be accepted multiple times if the user sends the command multiple times in a single tick. #48
- NPE bug in `/nickname reveal <nickname>` that cause the command to just... never work (idk how I missed this lol).
- Change defaultrequirelevel of ec & wb from 2 to 0. (Previously, this prevented non-opped players from using wb & ec
  on servers not using permissions)

Change to version naming:
For a while now, every version of Essential Commands has been labeled as a "beta".
This will no longer be the case. Starting with this version, published builds will be marked as releases.
This does not reflect any change in stability.

--- --- ---

## 0.14.0-mc1.16.5



**Essential Commands** **v0.14.0** (mc 1.16.5)

Direct backport of `0.14.0-mc1.17`

This version of Essential Commands has incompatabilities with certain older versions of Java.
If the server fails to start, consider updating your Java installation.

--- --- ---

## 0.13.4-beta+1-mc1.16.5


**Essential Commands** **v0.13.4+1** (1.16.5)

1.16.5-specific patch to fix subcommand ambiguity bug caused by old brigaider parsing.
(Originally fixed by https://github.com/Mojang/brigadier/commit/242de3fe7322372c15f388da6353c2c72f733306)

Particularly affected `/nickname set <player> <nickname>`


--- --- ---

## 0.13.4-beta-mc1.17


**Essential Commands** **v0.13.4** (mc 1.17.x)

Changes
  - Fixed bug that caused (uncolored) nicknames to override team colors by default.
  - Update playerlist every 5s (from 30s).


--- --- ---

## 0.13.4-beta-mc1.16.5


**Essential Commands** **v0.13.4** (mc 1.16.5)

Direct backport of `0.13.4-beta-1.17`

--- --- ---

## 0.13.3-beta-mc1.17


**Essential Commands** **v0.13.3**

A lot of bugfixes and prep for translations support.


Changes
  - Show only commands/subcommands that the player has permission to use in autocomplete. 
    (Root nodes require perms for at least 1 subcommand)
  - No longer allow overwriting via `/home set` or `/warp set` (require explicit deletion)
  - Use new lang file for all command feedback and other user-facing text. This will soon enable translations support.
  - Fix bug that made console unable to set player nicknames.
  - More descriptive tpaccept and tpdeny error in the case where there is no pending request from specified player.
  - Gracefully handle error for rtp when spawn is not set.

Minor
  - Save playerdata more frequently for nickname changes.
  - Add build timestamps for GH Actions builds.
  - Home/Warp storage rework.


--- --- ---

## 0.13.2-beta-mc1.17


**Essential Commands** **v0.13.2**

Fix broken default nickname prefix.

--- --- ---

## 0.13.1-beta-mc1.17


**Essential Commands** **v0.13.1**

Small bugfix:
  - **Bug:** cleared nicknames never update in PlayerList.
  - **Fix:** cleared nicknames are now properly synced with clients PlayerLists. (in 30second intervals)

--- --- ---

## 0.13.0-beta-mc1.17


**Essential Commands** **v0.13.0**

This version is for 1.17 only.
Working on a way to bring these features to 1.16.5.

### New Commands:

Command | Permission | Description
---|---|---
/fly                    | `essentialcommands.fly.self`      | Toggle ability to fly for self.
/fly \<target-player>   | `essentialcommands.fly.others`    | Toggle ability to fly for target player.
/workbench              | `essentialcommands.workbench`     | Open a crafting table screen.
/enderchest             | `essentialcommands.enderchest`    | Open your enderchest screen.

All of these have toggles in the config file, of course.

--- --- ---

## 0.12.0-beta-mc1.17


**Essential Commands** **v0.12.0**

This version is for 1.17 only.
Working on a way to bring these features to 1.16.5.

New Features
  - Homes and Warps listed with their respective `list` commands
    (i.e. `/home list`) can now be clicked to teleport to them.
  - Player nicknames show real name on hover.
    - +Config opt `nick_reveal_on_hover` to toggle this. Default `true`.
  - Add support for FabricPlaceholderAPI for all Text fields and JiJ said API.

Other changes
  - Latest unstable builds now available through GitHub Actions.

--- --- ---

## 0.11.1-beta-mc1.17


**Essential Commands v0.11.1**

RTP bugfixes and minor enhancements.

**Changelog:**

- Fix broken `/rtp` alias (You can use /rtp now instead of the full /randomteleport!)
- Run RTP location calculation in its own thread. (long-ish task)
- Switch to sendFeedbcak over sendSystemMessage where possible.
- Add config opt `broadcast_to_ops`.
    - Default `false`. (No change)
    - Allows logging Essentials Commands when enabled. (similar to vanilla commands)

--- --- ---

## 0.11.1-beta-mc1.16


`1.16.5` port of `0.11.1-beta-mc1.17`.

--- --- ---

## 0.11.0-beta-mc1.17


**Essential Commands** **v0.11.0** - Random Teleports!

**Changelog:**

Add /randomteleport (rtp) command.

Config options:
- `enable_rtp` default `true`
- `rtp_radius` default `1000`
- `rtp_cooldown` default `30` (seconds)
- `rtp_max_attempts` default `15`

Permissions:
- `essentialcommands.randomteleport`

Minor changes:
- Fix float formatting for /spawn set chat feedback.
- JiJ permissions api.

--- --- ---

## 0.11.0-beta-mc1.16


`1.16.5` port of `0.11.0-beta-mc1.17`.

--- --- ---

## 0.10.0-beta-mc1.17


**Essential Commands** **v0.10.0**

**Changelog:**

Nickname Improvements
- No longer allow players to add click events to their nicknames by default. (oops)
  (Now requires a permission.)
- Add permissions for `color`, `fancy` styling (italic, bold, etc.), `click` events,
  and `hover` events in nicknames.
  (All able to be assigned separately)

/back Improvements
- `/back` integration with most non-Essential-Commands teleport sources.
- Fix bug that caused `/back` to lose your previous location upon respawn.

--- --- ---

## 0.10.0-beta-mc1.16

`1.16.5` port of `0.10.0-beta-mc1.17`.

--- --- ---

## 0.9.1-beta-mc1.17


**Essential Commands** **v0.9.1**

List all the things, also bugfix.

Changelog:

0.9.0
- Add `list` subcommands to `/warp` and `/home`.

0.9.1
- Fix crash that occurred with some mods when Essential Commands was installed
  on the client.

--- --- ---

## 0.9.1-beta-mc1.16

`1.16.5` port of `0.9.1-beta-mc1.17`.

--- --- ---

## 0.8.0-beta-mc1.17

**Essential Commands** **v0.8.0** - Nickname Improvements

Changelog:

  - Correctly grant OPs all permissions.
    - This means that the `ops_bypass_teleport_rules` config only has an effect if `use_permissions_api` is set to `false`.
  - Display nicknames in player list (TAB). Can be disabled by `nicknames_in_player_list` config option.
  - Add customizable nickname prefix to designate when users are using a nickname. Add `nickname_prefix` config option.
  - Add command `/nickname reveal <nickname>`. Shows list of players matching the provided nickname.
    - Requires the permission `essentialcommands.nickname.reveal`.

--- --- ---

## 0.8.0-beta-mc1.16


Minecraft `1.16.5` port of Essential Commands version `0.8.0-beta-mc1.17`.

--- --- ---

## 0.7.3-beta-mc1.17

**Essential Commands** **v0.7.3** - Bugfixes for my bugfixes

Fixed some bugs introduced by v0.7.1.
Improved compatability with other mods (Origins, mostly).

--- --- ---

## 0.7.3-beta-mc1.16

`1.16.5` port of `0.7.3-beta-mc1.17`.

--- --- ---

## 0.7.1-beta-mc1.17

**Essential Commands** **v0.7.1** - Bugfix update.

Changelog:

- Provide friendly err msg and file path if WorldManager fails to load from file.
- Autosave PlayData when the vanilla server saves player data.
- Fully migrate to PlayerEntityAccess instead of PlayerDataManager HashMap
- No longer register commands if the command is disabled in the config (instead of registering a "Disabled command msg").
- Fix bug in update checker that caused 1.16 versions to always appear out-of-date if a 1.17 version of the same release existed.

--- --- ---

## 0.7.1-beta-mc1.16

`1.16` port of `0.7.1-beta-mc1.17`.

--- --- ---

## 0.7.0-beta-mc1.17

0.7.0-mc1.17 - The Nickname Update

New Features:
- New command, `/nickname`!
    - Config option: `enable_nick`, default true.
    - Relevant Perms:
        - `essentialcommands.nickname.self`
        - `essentialcommands.nickname.others`

Bugfixes & minor features:
- Log link to download new version if out of date.
- Add support for being used as a maven dependency via GitHub packages.
- Store PlayerData on PlayerEntity (Faster than previous HashMap\<UUID, PlayerData>)

--- --- ---

## 0.7.0-beta-mc1.16

Direct backport of **0.7.0-mc1.17** for **1.16.5**.

--- --- ---

## 0.6.0-mc1.17


Rework config to use Map instead of list of entries internally. (it was bad).

New config option: `ops_bypass_teleport_rules`

New permissions:
  - `essentialcommands.bypass.teleport_delay`
  - `essentialcommands.bypass.allow_teleport_between_dimensions`
  - `essentialcommands.bypass.teleport_interrupt_on_damaged`

Improve config error handling and parsing.
  - Give informational and descriptive console logs of errors found when parsing config. 
    (Fails gracefully, but loudly/clearly.)

--- --- ---

## 0.6.0-mc1.16


Port `0.6.0-mc1.17` to 1.16

--- --- ---

## 0.5.1-beta-mc1.17


Add config option `allow_teleport_between_dimensions`.
  - Bypassed by ops.
  - Default true.
  - Setting to false disallows teleporting between dimensions via EssentialCommands commands.

--- --- ---

## 0.5.0-mc1.17


New Features:
  - Add option to interrupt teleports on damage taken.
  - Implemented `allow_back_on_death` config option. (default `false`)
  - Self-Update checker & `check_for_updates` config option. (default `true`)
  - Add option to interrupt teleports on damage taken.
    - New config option `teleport_interrupt_on_damaged`, default `true`.
  - Add hot-reload config command: `/essentialcommands config reload`. Requires permission `essentialcommands.config.reload`.
  - Add ability to style command feedback using Style json, instead of just a single formatting code.
    - Ex: `{"bold"\:"true", "color":"light_purple"}`
    - Old format still works (Ex: `light_purple`)

Minor/Bugfix:
  - Changed tpa/tpaccept/tpdeny permissions nodes to match permissions.md.
  - Bumped fabric loader/api & yarn mappings versions

--- --- ---

## 0.5.0-mc1.16


Port to `1.16.5` from `1.17`.

--- --- ---

## 0.4.1-mc1.17

New Features:
  - Added /spawn command. (Finally!)
  - Implement teleport_delay config option. Improve teleport messages. 
  - Add /essentialcommands for easy listing of EC commands.

Minor improvements:
  - Don't create/init managers if their relevant features are disabled. (So disabled features won't have any impact on performance, ideally.)
  - Add permissions docs (permissions.md) (GitHub repo)

--- --- ---

## 0.3.1b-mc1.17

Singletonified most-all manager classes.

This was done to fix bug where loaded warps persist between different singleplayer worlds opened in the same game instance. ('twas a mess)

Other small changes:
  - Sort properties in config file.
  - Remove salmon.png


--- --- ---

## 0.3.0b-mc1.17

Added permissions API support!
(and fixed major Config bug that caused changes to be lost upon server restart. Oop)

Bugfixes/Minor improvements:
- Add 'use_permissions_api' config option. Default false.
- Add "could not be deleted" error message for '/warp delete' if warp does not exist.

--- --- ---

## 0.2.1-mc1.17

Fix bug that caused warps to fail to save.

--- --- ---

## 0.2.0-mc1.17

Add /warp command.

--- --- ---

## 0.1.3-mc1.17

Update to 1.17 with all the existing features! (in theory)

--- --- ---
