//package com.fibermc.essentialcommands;
//
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.screen.ingame.ContainerScreen54;
//import net.minecraft.container.ContainerType;
//import net.minecraft.container.GenericContainer;
//import net.minecraft.inventory.BasicInventory;
//import net.minecraft.inventory.Inventory;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.text.LiteralText;
//
//public class TradeUI {
//
//
//    public void showScreens(ServerPlayerEntity player1, ServerPlayerEntity player2) {
//        MinecraftClient.getInstance().openScreen(getPlayerUI(player1, player2));
//    }
//
//    private Screen getPlayerUI(ServerPlayerEntity player1, ServerPlayerEntity player2) {
//        ContainerScreen54 p1UI = new ContainerScreen54(getUIContainer(player1, player2), player1.inventory, new LiteralText("Trade with " + player2.getName()) );
//
//        return p1UI;
//    }
//
//    private GenericContainer getUIContainer(ServerPlayerEntity thisPlayer, ServerPlayerEntity otherPlayer) {
//        Inventory containerInv = new BasicInventory();
//        final int rows = 6;
//        for (int i=5; i < 9*rows; i+=9) {
//            containerInv.setInvStack(i, new ItemStack(Items.LIME_STAINED_GLASS_PANE));
//        }
//        GenericContainer out = new GenericContainer(ContainerType.GENERIC_9X6, 0, thisPlayer.inventory, containerInv, rows);
//
//        return out;
//    }
//}
//
