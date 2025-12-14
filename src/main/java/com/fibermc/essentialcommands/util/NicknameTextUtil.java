package com.fibermc.essentialcommands.util;

import com.fibermc.essentialcommands.ECPerms;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class NicknameTextUtil {
    private NicknameTextUtil() {}

    private static final class NickPerms {
        public final boolean color;
        public final boolean fancy;
        public final boolean hover;
        public final boolean click;

        private NickPerms(CommandSourceStack source) {
            this.color = ECPerms.check(source, ECPerms.Registry.nickname_style_color);
            this.fancy = ECPerms.check(source, ECPerms.Registry.nickname_style_fancy);
            this.hover = ECPerms.check(source, ECPerms.Registry.nickname_style_hover);
            this.click = ECPerms.check(source, ECPerms.Registry.nickname_style_click);
        }
    }

    public static boolean areAllTrue(boolean... array) {
        for (boolean b : array) if (!b) return false;
        return true;
    }

    // Returns true if they have the permissions for this nickname
    private static boolean hasPermissionForTextFragment(Component text, NickPerms sourcePerms) {
        Style style = text.getStyle();
        // If the nickname has no click event, return true
        // if it DOES have a clickEvent, return true if they have the clickEvent permission...
        return areAllTrue(
            (sourcePerms.color || (style.getColor() == null)),
            (sourcePerms.fancy
                || !(style.isBold()
                    || style.isItalic()
                    || style.isObfuscated()
                    || style.isStrikethrough()
                    || style.isUnderlined())
                || !style.getFont().equals(Identifier.parse("minecraft:default"))),
            (sourcePerms.click || (style.getClickEvent() == null)),
            (sourcePerms.hover || (style.getHoverEvent() == null))
        );
    }

    public static boolean checkPerms(Component parentText, NickPerms sourcePerms) {
        if (parentText == null) {
            return true;
        }
        boolean hasRequiredPerms = hasPermissionForTextFragment(parentText, sourcePerms);
        for (Component text : parentText.getSiblings()) {
            if (!checkPerms(text, sourcePerms)) {
                hasRequiredPerms = false;
                break;
            }

            if (!hasPermissionForTextFragment(text, sourcePerms)) {
                hasRequiredPerms = false;
                break;
            }
        }
        return hasRequiredPerms;
    }

    public static boolean checkPerms(Component parentText, CommandSourceStack source) {
        return checkPerms(parentText, new NickPerms(source));
    }
}
