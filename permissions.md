
# Permissions Documentation

## Overview

All commands and sub-commands have their own permissions node in the form:

`essentialcommands.<command>.<subcommand>`

Grant access to all subcommands using wildcards, like so:

`essentialcommands.<command>.*`

## Command Permission Nodes

Command | Permission
--------|-----------
/tpa \<player>        |   `essentialcommands.tpa`
/tpaccept \<player>   |   `essentialcommands.tpaccept`
/tpdeny \<player>     |   `essentialcommands.tpdeny`
/home set \<home_name>    |   `essentialcommands.home.set`
/home tp \<home_name>     |   `essentialcommands.home.tp`
/home delete \<home_name> |   `essentialcommands.home.delete`
/warp set \<warp_name>      |   `essentialcommands.warp.set`
/warp tp \<warp_name>       |   `essentialcommands.warp.tp`
/warp delete \<warp_name>   |   `essentialcommands.warp.delete`
/back     |   `essentialcommands.back`
/spawn tp \|\| /spawn     |   `essentialcommands.spawn.tp`
/spawn set              |   `essentialcommands.spawn.set`
