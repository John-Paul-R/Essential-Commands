package dev.jpcode.eccore.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.gson.JsonParseException;
import eu.pb4.placeholders.api.TextParserUtils;
import org.apache.logging.log4j.Level;

import net.minecraft.text.*;

import dev.jpcode.eccore.ECCore;

public final class TextUtil {
    private TextUtil() {}

    public static MutableText concat(Text... arr) {
        MutableText out = Text.empty();
        for (Text text : arr) {
            out.append(text);
        }
        return out;
    }

    public static MutableText deepCopy(Text text) {
        if (text.getSiblings().isEmpty()) {
            return text.copy();
        }

        var siblings = text.getSiblings();
        var newSiblings = siblings.stream()
            .map(TextUtil::deepCopy)
            .toList();
        siblings.clear();
        siblings.addAll(newSiblings);
        return text.copy();
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
     * @param array     the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, <code>null</code> if null array input
     * @since 2.0
     */
    public static MutableText join(Text[] array, Text separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static MutableText join(Collection<Text> textCollection, Text separator) {
        if (textCollection == null) {
            return null;
        }
        return join(textCollection.toArray(new Text[0]), separator, 0, textCollection.size());
    }

    public static MutableText join(Collection<String> stringCollection, Text separator, Style stringsFormatting) {
        if (stringCollection == null) {
            return null;
        }
        return join(
            stringCollection.stream().map(str -> Text.literal(str).setStyle(stringsFormatting)).toArray(Text[]::new),
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

    public static MutableText join(Text[] array, Text separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return null;
        }
        MutableText buf = Text.empty();
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

    public static MutableText spaceBetween(Text[] array, int totalWidth, int padding) {
        int totalTextSize = 0;
        for (Text txt : array) {
            String str = txt.getString();
            totalTextSize += str.length();
        }

        // No room for spacing
        if (totalTextSize > totalWidth) {
            return concat(array);
        }

        MutableText outText = Text.empty();
        String lrPadStr = " ".repeat(padding);
        String spaceStr = " ".repeat((totalWidth - padding * 2 - totalTextSize) / (array.length - 1));
        outText.append(Text.literal(lrPadStr));

        for (int i = 0; i < array.length; i++) {
            outText.append(array[i]);
            if (i != array.length - 1) {
                outText.append(Text.literal(spaceStr));
            }
        }

        outText.append(Text.literal(lrPadStr));

        return outText;
    }

    public static MutableText clickableTeleport(MutableText originalText, String destinationName, String commandBaseString) {
        String teleportCommand = String.format("%s %s", commandBaseString, destinationName);

        Style outStyle = originalText.getStyle()
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to teleport to "
                + destinationName
                + ".")));

        return originalText.setStyle(outStyle);
    }

    private static final Collection<StringToTextParser> TEXT_PARSERS = new ArrayList<>();

    /**
     * Parsers should be registered in order of most-restrictive to least restrictive.
     */
    public static void registerTextParser(StringToTextParser parser) {
        TEXT_PARSERS.add(parser);
    }

    static {
        registerTextParser(Text.Serialization::fromJson);
        int javaVersion = Util.getJavaVersion();
        if (javaVersion >= 16) {
            ECCore.LOGGER.log(Level.INFO, String.format(
                "Detected Java version %d. Enabling Java %d features.",
                javaVersion,
                16
            ));
            registerTextParser(TextParserUtils::formatText);
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
        for (StringToTextParser parser : TEXT_PARSERS) {
            try {
                outText = parser.parseText(textStr);
            } catch (JsonParseException e) {
                // ign
            }

            if (outText != null) {
                return outText;
            }
        }

        throw new RuntimeException(String.format("Failed to parse string '%s' as MinecraftText using any parsing strategy", textStr));
    }

    public static Collector<Text, MutableText, MutableText> collect() {
        return new Collector<>() {
            @Override
            public Supplier<MutableText> supplier() {
                return Text::empty;
            }

            @Override
            public BiConsumer<MutableText, Text> accumulator() {
                return MutableText::append;
            }

            @Override
            public BinaryOperator<MutableText> combiner() {
                return (r1, r2) -> {
                    r1.append(r2);
                    return r1;
                };
            }

            @Override
            public Function<MutableText, MutableText> finisher() {
                return (a) -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

    /**
     * indempotent
     *
     * @return flattened text
     */
    public static List<Text> flattenRoot(Text text) {
        var siblings = text.getSiblings();
        if (text.getContent().equals(PlainTextContent.EMPTY) && siblings.size() == 1) {
            return siblings;
        } else if (siblings.size() == 0) {
            return List.of(text);
        }

        List<Text> content = new ArrayList<>(siblings.size() + 1);
        if (!text.getContent().equals(PlainTextContent.EMPTY)) {
            content.add(text.copyContentOnly().setStyle(text.getStyle()));
        }
        content.addAll(siblings);

        return content;
    }
}
