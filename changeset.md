Features:

- Add `/tptoggle [on/off/off-silent]` (permission `essentialcommands.tptoggle`)
  to control whether other players can send you teleport requests. Backed by
  the new `allowTeleportRequests` player profile option.
    - `off` auto-denies requests and tells you someone tried.
    - `off-silent` auto-denies requests without notifying you.

Fixes:

- Fix OPs being unable to set styled nicknames for other players when
  `use_permissions_api` is `false` (#300). Nickname style permissions are now
  checked against the player running the command.
    - New config option, `nickname_style_perms_check_target` (default `false`),
      restores the old behavior of checking the target player.

- Fix `/warp`, `/warp <name>`, `/warp tp <name>` and warp tab completion throwing
  `IdentifierException` when a warp's permission node is not a valid `Identifier`
  (for example names with uppercase letters such as `Pyramid`, which older versions
  accepted). Invalid nodes now fall back to the lower-cased node, then to the
  `essentialcommands.warp.tp_named` parent node, instead of failing the command.
