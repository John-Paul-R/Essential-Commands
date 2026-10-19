# Player Profiles

Profiles contain per-player settings that players can set themselves that
affect how Essential Commands works for them, overriding the server-configured
defaults.

The following player profile settings allow tweaking how chat messages from
Essential Commands are presented:

- `/essentialcommands profile set formattingDefault <value>`
- `/essentialcommands profile set formattingAccent <value>`
- `/essentialcommands profile set printTeleportCoordinates <true/false>`

## Teleport Requests

`allowTeleportRequests` controls whether other players can send you teleport
requests (`/tpa` and `/tpahere`). It defaults to `on`.

- `on`: requests are delivered as normal.
- `off`: requests are auto-denied. You get a short notice in chat that someone
  tried, and the sender is told you saw it.
- `off-silent`: requests are auto-denied, and nothing appears in your chat. The
  sender is told you did not see it. Use this to stop request spam.

Set it with `/essentialcommands profile set allowTeleportRequests <on/off/off-silent>`,
or with the `/tptoggle` shorthand:

- `/tptoggle`: switch between `on` and `off` (switches to `on` from `off-silent`)
- `/tptoggle on`, `/tptoggle off`, `/tptoggle off-silent`: set it directly

Requests that are already pending when you turn this off are not affected.
