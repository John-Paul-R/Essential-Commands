
# Config Documentation

The config file can be found at `config/EssentialCommands.properties`

## Options

*Options marked with ~~strikethrough~~ are not yet fully implemented.*

*Note: Enabling/Disabling commands requires a server restart. `/essentialcommands config reload` is not sufficient.*

| Config Var                                             | Default Value              | Acceptable Values            |
|--------------------------------------------------------|----------------------------|------------------------------|
| afk_prefix                                             | "[AFK] " (gray)            | MinecraftText                |
| allow_back_on_death                                    | false                      | boolean                      |
| allow_teleport_between_dimensions                      | true                       | boolean                      |
| auto_afk_enabled                                       | true                       | boolean                      |
| auto_afk_time                                          | PT15M                      | duration                     |
| broadcast_to_ops                                       | false                      | boolean                      |
| check_for_updates                                      | true                       | boolean                      |
| enable_afk                                             | true                       | boolean                      |
| enable_anvil                                           | false                      | boolean                      |
| enable_back                                            | true                       | boolean                      |
| enable_bed                                             | false                      | boolean                      |
| enable_day                                             | true                       | boolean                      |
| enable_delete_all_player_data                          | true                       | boolean                      |
| enable_enderchest                                      | true                       | boolean                      |
| enable_experimental_essentialsx_converter              | false                      | boolean                      |
| enable_extinguish                                      | true                       | boolean                      |
| enable_feed                                            | true                       | boolean                      |
| enable_fly                                             | true                       | boolean                      |
| enable_gametime                                        | true                       | boolean                      |
| enable_heal                                            | true                       | boolean                      |
| enable_home                                            | true                       | boolean                      |
| enable_invuln                                          | true                       | boolean                      |
| enable_motd                                            | false                      | boolean                      |
| enable_near                                            | true                       | boolean                      |
| enable_nick                                            | true                       | boolean                      |
| enable_night                                           | true                       | boolean                      |
| enable_repair                                          | true                       | boolean                      |
| enable_rtp                                             | true                       | boolean                      |
| enable_rules                                           | true                       | boolean                      |
| enable_sleep                                           | false                      | boolean                      |
| enable_spawn                                           | true                       | boolean                      |
| enable_suicide                                         | true                       | boolean                      |
| enable_top                                             | true                       | boolean                      |
| enable_tpa                                             | true                       | boolean                      |
| enable_warp                                            | true                       | boolean                      |
| enable_wastebin                                        | true                       | boolean                      |
| enable_workbench                                       | true                       | boolean                      |
| excluded_top_level_commands                            | []                         | list of command names        |
| fly_max_speed                                          | 5                          | integer                      |
| formatting_accent                                      | light_purple               | Formatting Code, Style JSON  |
| formatting_default                                     | gold                       | Formatting Code, Style JSON  |
| formatting_error                                       | red                        | Formatting Code, Style JSON  |
| grant_lowest_numeric_by_default                        | true                       | boolean                      |
| home_limit ([Read More][home-limit])                   | [1, 2, 5]                  | list of integers             |
| invuln_while_afk                                       | false                      | boolean                      |
| language                                               | en_us                      | (see language ids below)     |
| motd                                                   | *A welcome message*        | String (Text)                |
| near_command_default_radius                            | 200                        | integer                      |
| near_command_max_radius                                | 200                        | integer                      |
| nick_reveal_on_hover                                   | true                       | boolean                      |
| nickname_above_head                                    | false                      | boolean                      |
| nickname_max_length                                    | 32                         | integer                      |
| nickname_prefix                                        | {"text":"~","color":"red"} | MinecraftText                |
| nicknames_in_player_list                               | true                       | boolean                      |
| ops_bypass_teleport_rules                              | true                       | boolean                      |
| persist_back_location                                  | false                      | boolean                      |
| print_teleport_coordinates                             | true                       | boolean                      |
| recheck_player_ability_permissions_on_dimension_change | false                      | boolean                      |
| register_top_level_commands                            | true                       | boolean                      |
| respawn_at_ec_spawn                                    | Never                      | RespawnCondition Expression  |
| rtp_center                                             | Spawn                      | RtpCenter (`Spawn`, `5,10`)  |
| rtp_cooldown                                           | 30                         | integer                      |
| rtp_enabled_worlds                                     | overworld                  | world name (ex `the_nether`) |
| rtp_max_attempts                                       | 15                         | integer                      |
| rtp_min_radius                                         | auto-matches `rtp_radius`  | integer (`<= rtp_radius`)    |
| rtp_radius                                             | 1000                       | integer                      |
| sleep_invuln                                           | false                      | boolean                      |
| sleep_near_monsters                                    | false                      | boolean                      |
| ~~teleport_cooldown~~                                  | 1.0                        | double (seconds)             |
| teleport_delay                                         | 0.0                        | double (seconds)             |
| teleport_interrupt_on_damaged                          | true                       | boolean                      |
| teleport_interrupt_on_move                             | false                      | boolean                      |
| teleport_interrupt_on_move_max_blocks                  | 3.0                        | double (blocks)              |
| teleport_request_duration                              | 60                         | integer (seconds)            |
| teleport_with_followers                                | false                      | boolean                      |
| teleport_with_followers_radius                         | 100.0                      | double (blocks)              |
| use_permissions_api                                    | false                      | boolean                      |

*Note: if `use_permissions_api` is set to true, OPs are treated as having all permissions (thus making the `ops_bypass_teleport_rules` config option do nothing).*

## Types

### Boolean

`true` or `false`

### Integer

Positive or negative whole number. \
Negative values generally disable their respective property.

Examples: `1`, `20`, `-3`

### Double

Positive or negative floating point number (can have decimals). \
Negative values generally disable their respective property.

Examples: `1.0`, `20.5`, `-3.125`

### Formatting Code

See ["Formatting codes"](https://minecraft.fandom.com/wiki/Formatting_codes) on the minecraft wiki.

Example: `light_purple`, `gold`

### Style JSON

See [Raw JSON text format](https://minecraft.fandom.com/wiki/Raw_JSON_text_format#Java_Edition) on the minecraft wiki.

Example: `{"italic":"true", "color":"light_purple"}`

### MinecraftText

Essentially, any value that works for `/tellraw`'s message field. (JSON text or string enclosed by quotes)

You can use a tellraw generator like [MinecraftJson](https://www.minecraftjson.com/) to create this JSON text with a graphical interface and preview.

Examples: `"Alexandra"`, `{"text":"Alex","color":"green","bold":true}`

### `RespawnCondition`

Valid values:

- Never
- Always
- NoBed
- SameWorld
- FirstJoin

### Expression

An expression is a special type of config option value. It can be used to
represent multiple conditions at once.

For example, normally, `RespawnCondition` can only have a single value. If you
want to match players with `NoBed`, you can't also match all players in the
`SameWorld` as the spawn. With expressions, however, this is quite simple. The
config value to do just that is `NoBed OR SameWorld`.

In short, Expressions allow you to represent multiple conditions at once, joined
by either `OR` or `AND`. Grouping with parentheses also works.

## Numeric Permissions

Several config options (like `home_limit`) use a tiered numeric permission
system when `use_permissions_api=true`. These accept a comma-separated list of
integers; each value generates a corresponding permission node (e.g.
`essentialcommands.home.limit.5`).

### `grant_lowest_numeric_by_default`

When enabled (the default), players without any explicit tiered numeric
permission are treated as having the *lowest* value in the list, rather than
`0`. Setting this to `false` means a player with no such permission gets no
access at all (for example, no homes).

See [Home Limit](Home-Limit) for a more complete example.

## Languages

List of supported language ids (use these in the "language" config option):

- de_de (German)
- en_us (English, US)
- es_es (Spanish, Spain)
- fr_fr (French, France)
- ko_kr (Korean)
- nl_nl (Dutch)
- pt_br (Portuguese, Brazil -- courtesy of AnonymozzY on CF)
- ru_ru (Russian -- courtesy of @oldBrowze)
- zh_cn (Chinese, Simplified -- courtesy of @MikhailTapio, @deluxghost, @Leo204_LKY)
- zh_tw (Chinese, Traditional)

See also the [Language](Language) page.

[home-limit]: Home-Limit
