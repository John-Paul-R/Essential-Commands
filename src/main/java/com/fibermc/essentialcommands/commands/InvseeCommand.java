package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.text.ECText;

import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class InvseeCommand implements Command<ServerCommandSource> {
    public static class InvseeScreenHandler extends GenericContainerScreenHandler {
        final ServerPlayerEntity executor;
        public final ServerPlayerEntity target;

        private static final ItemStack notAllowed = new ItemStack(Items.BARRIER);
        static {
            notAllowed.setCustomName(ECText.getInstance().getText("invsee.not_available", TextFormatType.Empty));
        }

        InvseeScreenHandler(int syncId, ServerPlayerEntity executor, ServerPlayerEntity target) {
            super(ScreenHandlerType.GENERIC_9X5, syncId, executor.getInventory(),
                new SimpleInventory(45) {
                    @Override
                    public ItemStack getStack(int slot) {
                        if (slot < 41) {
                            return target.getInventory().getStack(slot);
                        } else {
                            return notAllowed.copy();
                        }
                    }

                    @Override
                    public void setStack(int slot, ItemStack stack) {
                        if (slot < 41) {
                            target.getInventory().setStack(slot, stack);
                        }
                    }

                    @Override
                    public ItemStack removeStack(int slot) {
                        if (slot < 41) {
                            return target.getInventory().removeStack(slot);
                        }
                        return notAllowed.copy();
                    }

                    @Override
                    public ItemStack removeStack(int slot, int amount) {
                        if (slot < 41) {
                            return target.getInventory().removeStack(slot, amount);
                        }
                        return notAllowed.copy();
                    }
                }, 5);
            this.executor = executor;
            this.target = target;
            for (int i = 41; i < 45; i++) {
                // prevent the player from interacting with the not available slots
                Slot slot = getSlot(i);
                slot = new Slot(slot.inventory, slot.getIndex(), slot.x, slot.y) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakePartial(PlayerEntity player) {
                        return false;
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return false;
                    }
                };
                slots.set(i, slot);
            }
        }
    }
    public static Map<ServerPlayerEntity, InvseeScreenHandler> invseeHandlers = new HashMap<>();
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
        ServerPlayerEntity executor = context.getSource().getPlayer();
        assert executor != null;
        executor.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return ECText.getInstance().getText("invsee.name", TextFormatType.Empty, target.getName());
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                InvseeScreenHandler handler = new InvseeScreenHandler(syncId, executor, target);
                invseeHandlers.put(target, handler);
                return handler;
            }
        });
        return 0;
    }
}
