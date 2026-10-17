Fixes:

- Fix OPs being unable to set styled nicknames for other players when
  `use_permissions_api` is `false` (#300). Nickname style permissions are now
  checked against the player running the command.
    - New config option, `nickname_style_perms_check_target` (default `false`),
      restores the old behavior of checking the target player.
