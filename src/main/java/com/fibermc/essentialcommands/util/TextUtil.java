package com.fibermc.essentialcommands.util;

import net.minecraft.text.*;

import java.util.Collection;

public class TextUtil {


/**
  * <p>Joins the elements of the provided array into a single Text
  * containing the provided list of elements.</p>
  *
  * <p>No delimiter is added before or after the list.
  * Null objects or empty strings within the array are represented by
  * empty strings.</p>
  *
  * <pre>
  * StringUtils.join(null, *)               = null
  * StringUtils.join([], *)                 = ""
  * StringUtils.join([null], *)             = ""
  * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
  * StringUtils.join(["a", "b", "c"], null) = "abc"
  * StringUtils.join([null, "", "a"], ';')  = ";;a"
  * </pre>
  *
  * @param array  the array of values to join together, may be null
  * @param separator  the separator character to use
  * @return the joined String, <code>null</code> if null array input
  * @since 2.0
  */
    public static Text join(Text[] array, Text separator) {
        if (array == null) {
                return null;
            }
        return join(array, separator, 0, array.length);
    }
    public static Text join(Collection<Text> textCollection, Text separator) {
        if (textCollection == null) {
            return null;
        }
        return join(textCollection.toArray(new Text[0]), separator, 0, textCollection.size());
    }
    public static Text joinStrings(Collection<String> stringCollection, Text separator, Style stringsFormatting) {
        if (stringCollection == null) {
            return null;
        }
        return join(
            stringCollection.stream().map(str -> new LiteralText(str).setStyle(stringsFormatting)).toArray(Text[]::new),
            separator, 0, stringCollection.size()
        );
    }
    public static Text join(Text[] array, Text separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return null;
        }
        MutableText buf = new LiteralText("");
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf;
    }

    public static Text clickableTeleport(MutableText originalText, String destinationName, String commandBaseString) {
        String teleportCommand = String.format("%s %s", commandBaseString, destinationName);

        Style outStyle = originalText.getStyle()
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to teleport to " + destinationName +".")));

        return originalText.setStyle(outStyle);
    }

}
