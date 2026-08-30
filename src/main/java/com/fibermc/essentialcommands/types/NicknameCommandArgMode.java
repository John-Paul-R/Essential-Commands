package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.mixin.EntityArgumentNicknameSuggestionMixin;
import com.fibermc.essentialcommands.mixin.EntitySelectorNicknameMixin;

public enum NicknameCommandArgMode {
    /** Nicknames resolve ({@link EntitySelectorNicknameMixin}) and suggest
     *  ({@link EntityArgumentNicknameSuggestionMixin}) in all commands via mixin. */
    Everywhere,
    /** Nicknames resolve and suggest only in Essential Commands. */
    EssentialCommandsOnly,
    /** Vanilla behavior unchanged -- no nickname resolution. */
    Never
}
