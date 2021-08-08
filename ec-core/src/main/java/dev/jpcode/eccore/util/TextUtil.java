package dev.jpcode.eccore.util;

import com.google.gson.JsonParseException;
import dev.jpcode.eccore.ECCore;
import eu.pb4.placeholders.TextParser;
import net.minecraft.text.*;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Collection;

public class TextUtil {


    public static MutableText concat(Text... arr) {
        MutableText out = new LiteralText("");
        for (Text text : arr) {
            out.append(text);
        }
        return out;
    }
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
    public static Text join(Collection<String> stringCollection, Text separator, Style stringsFormatting) {
        if (stringCollection == null) {
            return null;
        }
        return join(
            stringCollection.stream().map(str -> new LiteralText(str).setStyle(stringsFormatting)).toArray(Text[]::new),
            separator, 0, stringCollection.size()
        );
    }

    public static String joinStrings(Collection<String> stringCollection, String separator) {
        if (stringCollection == null) {
            return null;
        }
        return joinStrings(
                stringCollection.toArray(String[]::new),
                separator, 0, stringCollection.size()
        );
    }

    public static String joinStrings(String[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
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

    public static Text spaceBetween(Text[] array, int totalWidth, int padding) {
        int totalTextSize = 0;
        ArrayList<String> strings = new ArrayList<>(array.length);
        for (Text txt : array) {
            String str = txt.getString();
            strings.add(str);
            totalTextSize += str.length();
        }

        // No room for spacing
        if (totalTextSize > totalWidth) {
            return concat(array);
        }

        MutableText outText = new LiteralText("");//new ArrayList<>(strings.size() * 2 - 1);
        String lrPadStr = " ".repeat(padding);
        String spaceStr = " ".repeat((totalWidth - padding * 2 - totalTextSize) / (array.length - 1));
        outText.append(new LiteralText(lrPadStr));

        for (int i = 0; i < array.length; i++) {
            outText.append(array[i]);
            if (i != array.length - 1)
                outText.append(new LiteralText(spaceStr));
        }

        outText.append(new LiteralText(lrPadStr));

        return outText;
    }

    public static Text clickableTeleport(MutableText originalText, String destinationName, String commandBaseString) {
        String teleportCommand = String.format("%s %s", commandBaseString, destinationName);

        Style outStyle = originalText.getStyle()
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to teleport to " + destinationName +".")));

        return originalText.setStyle(outStyle);
    }


    private static final Collection<StringToTextParser> textParsers = new ArrayList<>();
    /**
     * Parsers should be registered in order of most-restrictive to least restrictive.
     */
    public static void registerTextParser(StringToTextParser parser) {
        textParsers.add(parser);
    }

    static {
        registerTextParser(Text.Serializer::fromJson);
        int javaVersion = Util.getJavaVersion();
        if (javaVersion >= 16) {
            ECCore.LOGGER.log(Level.INFO, String.format(
                "Detected Java version %d. Enabling Java %d features.",
                javaVersion,
                16
            ));
            registerTextParser(TextParser::parse);
        } else {
            ECCore.LOGGER.log(Level.WARN, String.format(
                "Detected Java version %d. Some features require Java %d. Some text formatting features will be disabled.",
                javaVersion,
                16
            ));
        }
    }
    public static Text parseText(String textStr) {
        Text outText = null;
        for (StringToTextParser parser : textParsers) {
            try {
                outText = parser.parseText(textStr);
            } catch (JsonParseException e) {
                ECCore.LOGGER.log(Level.INFO, String.format("Failed to parse string '%s' as MinecraftText, trying Fabric Placeholder API...", textStr));
            }

            if (outText != null)
                break;
        }
        return outText;
    }
}
