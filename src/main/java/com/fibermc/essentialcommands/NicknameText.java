package com.fibermc.essentialcommands;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NicknameText extends LiteralText {

    public NicknameText(String string) {
        super(string);
    }

    private static class NickPerms {
        public final boolean color;
        public final boolean fancy;
        public final boolean hover;
        public final boolean click;

        private NickPerms(ServerCommandSource source) {
            this.color = ECPerms.check(source, ECPerms.Registry.nickname_style_color);
            this.fancy = ECPerms.check(source, ECPerms.Registry.nickname_style_fancy);
            this.hover = ECPerms.check(source, ECPerms.Registry.nickname_style_hover);
            this.click = ECPerms.check(source, ECPerms.Registry.nickname_style_click);
        }
    }

    public static boolean areAllTrue(boolean... array)
    {
        for(boolean b : array) if(!b) return false;
        return true;
    }

    // Returns true if they have the permissions for this nickname
    private static boolean hasPermissionForTextFragment(Text text, NickPerms sourcePerms) {
        Style style = text.getStyle();
        // If the nickname has not click event, return true
        // if it DOES have a clickEvent, return true if they have the clickEvent permission...
        return areAllTrue(
            (sourcePerms.color || (style.getColor() == null)),
            (sourcePerms.fancy || !(style.isBold() || style.isItalic() || style.isObfuscated() || style.isStrikethrough() || style.isUnderlined()) || !style.getFont().equals(new Identifier("minecraft:default"))),
            (sourcePerms.click || (style.getClickEvent() == null)),
            (sourcePerms.hover || (style.getHoverEvent() == null))
        );
    }

    public static boolean checkPerms(Text parentText, NickPerms sourcePerms) {
        if (parentText == null) {
            return true;
        }
        boolean hasRequiredPerms = hasPermissionForTextFragment(parentText, sourcePerms);
        for (Text text : parentText.getSiblings()) {
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

    public static boolean checkPerms(Text parentText, ServerCommandSource source) {
        return checkPerms(parentText, new NickPerms(source));
    }
    public boolean checkPerms(ServerPlayerEntity playerEntity) {
        return checkPerms(this, playerEntity.getCommandSource());
    }
}
