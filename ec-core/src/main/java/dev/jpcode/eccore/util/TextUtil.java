package dev.jpcode.eccore.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.gson.JsonParser;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;

import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;

public final class TextUtil {
    private TextUtil() {}

    public static MutableComponent concat(Component... arr) {
        MutableComponent out = Component.empty();
        for (Component text : arr) {
            out.append(text);
        }
        return out;
    }

    public static MutableComponent deepCopy(Component text) {
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
     * Joins the elements of the provided array into a single Text
     * containing the provided list of elements.
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
    public static MutableComponent join(Component[] array, Component separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static MutableComponent join(Collection<Component> textCollection, Component separator) {
        if (textCollection == null) {
            return null;
        }
        return join(textCollection.toArray(new Component[0]), separator, 0, textCollection.size());
    }

    public static MutableComponent join(Collection<String> stringCollection, Component separator, Style stringsFormatting) {
        if (stringCollection == null) {
            return null;
        }
        return join(
            stringCollection.stream().map(str -> Component.literal(str).setStyle(stringsFormatting)).toArray(Component[]::new),
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

    public static MutableComponent join(Component[] array, Component separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return null;
        }
        MutableComponent buf = Component.empty();
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

    public static MutableComponent spaceBetween(Component[] array, int totalWidth, int padding) {
        int totalTextSize = 0;
        for (Component txt : array) {
            String str = txt.getString();
            totalTextSize += str.length();
        }

        // No room for spacing
        if (totalTextSize > totalWidth) {
            return concat(array);
        }

        MutableComponent outText = Component.empty();
        String lrPadStr = " ".repeat(padding);
        String spaceStr = " ".repeat((totalWidth - padding * 2 - totalTextSize) / (array.length - 1));
        outText.append(Component.literal(lrPadStr));

        for (int i = 0; i < array.length; i++) {
            outText.append(array[i]);
            if (i != array.length - 1) {
                outText.append(Component.literal(spaceStr));
            }
        }

        outText.append(Component.literal(lrPadStr));

        return outText;
    }

    public static MutableComponent clickableTeleport(MutableComponent originalText, String destinationName, String commandBaseString) {
        String teleportCommand = String.format("%s %s", commandBaseString, destinationName);

        Style outStyle = originalText.getStyle()
            .withClickEvent(new ClickEvent.RunCommand(teleportCommand))
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to teleport to "
                + destinationName
                + ".")));

        return originalText.setStyle(outStyle);
    }

    public static String toJsonString(Component text) {
        return ComponentSerialization.CODEC
            .encodeStart(JsonOps.INSTANCE, text)
            .getOrThrow()
            .toString();
    }

    private static final Collection<StringToTextParser> TEXT_PARSERS = new ArrayList<>();

    /**
     * Parsers should be registered in order of most-restrictive to least restrictive.
     */
    public static void registerTextParser(StringToTextParser parser) {
        TEXT_PARSERS.add(parser);
    }

    static {
        registerTextParser(str -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(str)).getOrThrow());
        registerTextParser(str -> TagParser.DEFAULT.parseComponent(str, ParserContext.of()));
    }

    public static Component parseText(String textStr) {
        Component outText = null;
        for (StringToTextParser parser : TEXT_PARSERS) {
            try {
                outText = parser.parseText(textStr);
            } catch (Exception e) {
                // ign
            }

            if (outText != null) {
                return outText;
            }
        }

        throw new RuntimeException(String.format("Failed to parse string '%s' as MinecraftText using any parsing strategy", textStr));
    }

    public static Collector<Component, MutableComponent, MutableComponent> collect() {
        return new Collector<>() {
            @Override
            public Supplier<MutableComponent> supplier() {
                return Component::empty;
            }

            @Override
            public BiConsumer<MutableComponent, Component> accumulator() {
                return MutableComponent::append;
            }

            @Override
            public BinaryOperator<MutableComponent> combiner() {
                return (r1, r2) -> {
                    r1.append(r2);
                    return r1;
                };
            }

            @Override
            public Function<MutableComponent, MutableComponent> finisher() {
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
    public static List<Component> flattenRoot(Component text) {
        var siblings = text.getSiblings();
        if (text.getContents().equals(PlainTextContents.EMPTY) && siblings.size() == 1) {
            return siblings;
        } else if (siblings.size() == 0) {
            return List.of(text);
        }

        List<Component> content = new ArrayList<>(siblings.size() + 1);
        if (!text.getContents().equals(PlainTextContents.EMPTY)) {
            content.add(text.plainCopy().setStyle(text.getStyle()));
        }
        content.addAll(siblings);

        return content;
    }
}
