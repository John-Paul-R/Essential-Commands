package com.fibermc.essentialcommands.mixin;

import com.mojang.brigadier.tree.CommandNode;
import groovyjarjarasm.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(CommandNode.class)
public class CommandNodeMixin<S> {

    /**
     * Removes sorting of child nodes in CommandNode.
     * This sorting sorting that broke ability to deal with
     * command node ambiguity via registration order.
     * (essentially a fix from brigadier 1.0.18
     *
     * @param commandNode
     */
    @Redirect(
            method = "addChild",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/mojang/brigadier/tree/CommandNode;children:Ljava/util/Map;",
                    opcode = Opcodes.PUTFIELD,
                    remap = false
            ),
            remap = false
    )
    private void dontResetChildren(CommandNode commandNode, Map<String, CommandNode> value) {}

}
