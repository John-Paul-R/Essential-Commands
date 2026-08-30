package com.fibermc.essentialcommands.mixin;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fibermc.essentialcommands.commands.NicknameTargetResolver;
import com.fibermc.essentialcommands.types.NicknameCommandArgMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.arguments.EntityArgument;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

/**
 * Adds nickname suggestions to all EntityArgument-based commands
 * (e.g. /tp, /give) when global nickname arguments are enabled.
 */
@Mixin(EntityArgument.class)
public class EntityArgumentNicknameSuggestionMixin {

    @Inject(method = "listSuggestions", at = @At("RETURN"), cancellable = true)
    private void ec$addNicknameSuggestions(
        CommandContext<?> context,
        SuggestionsBuilder builder,
        CallbackInfoReturnable<CompletableFuture<Suggestions>> cir
    ) {
        if (CONFIG.NICKNAMES_AS_COMMAND_ARG != NicknameCommandArgMode.Everywhere) {
            return;
        }

        SuggestionsBuilder nicknameBuilder = builder.createOffset(builder.getStart());
        NicknameTargetResolver.addNicknameSuggestions(nicknameBuilder);
        Suggestions nicknameSuggestions = nicknameBuilder.build();

        if (!nicknameSuggestions.getList().isEmpty()) {
            cir.setReturnValue(
                cir.getReturnValue().thenApply(vanilla ->
                    Suggestions.merge(builder.getInput(), List.of(vanilla, nicknameSuggestions))
                )
            );
        }
    }
}
