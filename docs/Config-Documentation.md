
# Config Documentation

The config file can be found at `config/EssentialCommands.properties`

## Options

*Options marked with ~~strikethrough~~ are not yet fully implemented.*

*Note: Enabling/Disabling commands requires a server restart. `/essentialcommands config reload` is not sufficient.*

| Config Var                                             | Default Value              | Acceptable Values           |
|--------------------------------------------------------|----------------------------|-----------------------------|
| ~~teleport_cooldown~~                                  | 1.0                        | double (seconds)            |
| afk_prefix                                             | "[AFK] " (gray)            | MinecraftText               |
| allow_back_on_death                                    | false                      | boolean                     |
| allow_teleport_between_dimensions                      | true                       | boolean                     |
| auto_afk_enabled                                       | true                       | boolean                     |
| auto_afk_time                                          | PT15M                      | duration                    |
| broadcast_to_ops                                       | false                      | boolean                     |
| check_for_updates                                      | true                       | boolean                     |
| enable_afk                                             | true                       | boolean                     |
| enable_anvil                                           | false                      | boolean                     |
| enable_back                                            | true                       | boolean                     |
| enable_enderchest                                      | true                       | boolean                     |
| enable_experimental_essentialsx_converter              | false                      | boolean                     |
| enable_fly                                             | true                       | boolean                     |
| enable_gametime                                        | true                       | boolean                     |
| enable_home                                            | true                       | boolean                     |
| enable_invuln                                          | true                       | boolean                     |
| enable_motd                                            | false                      | boolean                     |
| enable_nick                                            | true                       | boolean                     |
| enable_rtp                                             | true                       | boolean                     |
| enable_sleep                                           | false                      | boolean                     |
| enable_spawn                                           | true                       | boolean                     |
| enable_tpa                                             | true                       | boolean                     |
| enable_warp                                            | true                       | boolean                     |
| enable_wastebin                                        | true                       | boolean                     |
| enable_workbench                                       | true                       | boolean                     |
| excluded_top_level_commands                            | []                         | list of command names       |
| formatting_accent                                      | light_purple               | Formatting Code, Style JSON |
| formatting_default                                     | gold                       | Formatting Code, Style JSON |
| formatting_error                                       | red                        | Formatting Code, Style JSON |
| grant_lowest_numeric_by_default                        | true                       | boolean                     |
| home_limit ([Read More][home-limit])                   | [1, 2, 5]                  | list of integers            |
| invuln_while_afk                                       | false                      | boolean                     |
| language                                               | en_us                      | (see language ids below)    |
| motd                                                   | *A welcome message*        | String (Text)               |
| nick_reveal_on_hover                                   | true                       | boolean                     |
| nickname_above_head                                    | false                      | boolean                     |
| nickname_max_length                                    | 32                         | integer                     |
| nickname_prefix                                        | {"text":"~","color":"red"} | MinecraftText               |
| nicknames_in_player_list                               | true                       | boolean                     |
| ops_bypass_teleport_rules                              | true                       | boolean                     |
| persist_back_location                                  | false                      | boolean                     |
| recheck_player_ability_permissions_on_dimension_change | false                      | boolean                     |
| register_top_level_commands                            | true                       | boolean                     |
| respawn_at_ec_spawn                                    | Never                      | RespawnCondition Expression |
| rtp_center                                             | Spawn                      | RtpCenter (`Spawn`, `5,10`) |
| rtp_cooldown                                           | 30                         | integer                     |
| rtp_enabled_worlds                                     | overworld                  | world name (ex `the_nether`)|
| rtp_max_attempts                                       | 15                         | integer                     |
| rtp_min_radius                                         | `rtp_radius` (1000)        | integer (`<= rtp_radius`)   |
| rtp_radius                                             | 1000                       | integer                     |
| sleep_invuln                                           | false                      | boolean                     |
| sleep_near_monsters                                    | false                      | boolean                     |
| teleport_delay                                         | 0.0                        | double (seconds)            |
| teleport_interrupt_on_damaged                          | true                       | boolean                     |
| teleport_request_duration                              | 60                         | integer (seconds)           |
| use_permissions_api                                    | false                      | boolean                     |

*Note: if `use_permissions_api` is set to true, OPs are treated as having all permissions (thus making the `ops_bypass_teleport_rules` config option do nothing).*

## Types

### Boolean

`true` or `false`

### Integer

Positive or negative whole number. \
Negative values generally disable their respecitve property.

Examples: `1`, `20`, `-3`

### Double

Positive or negative floating point number (can have decimals). \
Negative values generally disable their respecitve property.

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

### Expression

An expression is a special type of config option value. It can be used to
represent multiple conditions at once.

For example, normally, `RespawnCondition` can only have a single value. If you
want to match players with `NoBed`, you can't also match all players in the
`SameWorld` as the spawn. With expressions, however, this is quite simple. The
config value to do just that is `NoBed OR SameWorld`.

In short, Expressions allow you to represent multiple conditions at once, joined
by either `OR` or `AND`. Grouping with parentheses also works.

## Languages

List of supported language ids (use these in the "language" config option):

- en_us
- pt_br (Courtesy of AnonymozzY on CF)
- ru_ru (Courtesy of @oldBrowze)
- zh_cn (Courtesy of @MikhailTapio, @deluxghost, @Leo204_LKY)

[home-limit]: Home-Limit
