package com.fibermc.essentialcommands;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class NicknameText extends LiteralText {

    public NicknameText(String string) {
        super(string);
    }


    // Returns true if they have the permissions for this nickname
    private static boolean checkClickEventPerms(Text text, ServerCommandSource source) {
        // If the nickname has not click event, return true
        // if it DOES have a clickEvent, return true if they have the clickEvent permission...
        return text.getStyle().getClickEvent() == null || ECPerms.check(source, ECPerms.Registry.nickname_style_clickEvent);
    }
    public static boolean checkPerms(Text parentText, ServerCommandSource source) {
        if (parentText == null) {
            return true;
        }
        boolean hasRequiredPerms = checkClickEventPerms(parentText, source);
        for (Text text : parentText.getSiblings()) {
            checkPerms(text, source);

            if (checkClickEventPerms(text, source)) {
                hasRequiredPerms = false;
                break;
            }
        }
        return hasRequiredPerms;
    }

    public boolean checkPerms(ServerPlayerEntity playerEntity) {
        return checkPerms(this, playerEntity.getCommandSource());
    }
}
