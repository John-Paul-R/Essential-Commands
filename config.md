
# Config Documentation

## Options

*Options marked with ~~strikethrough~~ are not yet fully implemented.*

*Note: Enabling/Disabling commands requires a server restart. `/essentialcommands config reload` is not sufficient.*

Config Var | Default Value | Acceptable Values
---|---|---
formatting_default                 | gold           | Formatting Code, Style JSON
formatting_accent                  | light_purple   | Formatting Code, Style JSON
formatting_error                   | red            | Formatting Code, Style JSON
enable_back                        | true           | boolean
enable_home                        | true           | boolean
enable_spawn                       | true           | boolean
enable_tpa                         | true           | boolean
enable_warp                        | true           | boolean
enable_nick                        | true           | boolean
home_limit                         | 1              | integer
~~teleport_cooldown~~              | 1.0            | double (seconds)
teleport_delay                     | 0.0            | double (seconds)
allow_back_on_death                | false          | boolean
teleport_request_duration          | 60             | integer (seconds)
use_permissions_api                | false          | boolean
check_for_updates                  | true           | boolean
teleport_interrupt_on_damaged      | true           | boolean
allow_teleport_between_dimensions  | true           | boolean
ops_bypass_teleport_rules          | true           | boolean
nicknames_in_player_list           | true           | boolean

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
