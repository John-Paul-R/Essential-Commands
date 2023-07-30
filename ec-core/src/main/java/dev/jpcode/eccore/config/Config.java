package dev.jpcode.eccore.config;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class Config<T extends Config<T>> {
    static final Logger LOGGER = LogManager.getLogger("ec-core-config");

    protected SortedProperties props;
    private final Path configPath;
    private final String displayName;
    private final String documentationLink;

    private @Nullable String existingPropsStr;
    private String getNonCommentsLines(String propsContent) {
        return propsContent.lines()
            .skip(displayName.lines().count() + 2)
            .collect(Collectors.joining("\n"));
    }

    public Config(Path savePath, String displayName, String documentationLink) {
        this.configPath = savePath;
        this.displayName = displayName;
        this.documentationLink = documentationLink;
        initFieldStorage();
    }

    public void loadOrCreateProperties() {
        props = new SortedProperties();
        File inFile = configPath.toFile();

        try {
            inFile.getParentFile().mkdirs();
            boolean fileAlreadyExisted = !inFile.createNewFile();
            if (fileAlreadyExisted) {
                var stringWriter = new StringWriter();
                new FileReader(inFile).transferTo(stringWriter);
                existingPropsStr = stringWriter.toString();

                props.load(new FileReader(inFile));
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load preferences.");
            LOGGER.error(e.getMessage());
        }
        initProperties();
        storeProperties();
        configLoadHandlers.forEach(consumer -> consumer.accept((T) this));
    }

    private void initProperties() {
        // Cursed reflection reloading of all properties.
        getAllOptions()
            .forEach(opt -> opt.loadAndSave(props));
    }

    private List<? extends Option<?>> options;

    private List<? extends Option<?>> getOptions() {
        if (this.options != null) {
            return this.options;
        }

        Class<?> cls = this.getClass();
        return this.options = Arrays.stream(cls.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(ConfigOption.class))
            .map(field -> {
                try {
                    return ((Option<?>) field.get(this));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .toList();
    }

    public Stream<? extends Option<?>> getAllOptions() {
        return Stream.concat(getOptions().stream(), getSectionOptions());
    }

    private Stream<? extends Option<?>> getSectionOptions() {
        Class<?> cls = this.getClass();
        return Arrays.stream(cls.getDeclaredFields())
            .filter(field -> ConfigSectionSkeleton.class.isAssignableFrom(field.getType()))
            .flatMap(field -> {
                try {
                    return ((ConfigSectionSkeleton) field.get(this)).getOptions().stream();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public void storeProperties() {
        try {
            var propsComments = displayName + "\n" + "Config Documentation: " + documentationLink;

            var stringWriter = new StringWriter();
            props.storeSorted(stringWriter, propsComments);
            stringWriter.close();

            var strinifiedProps = stringWriter.toString();
            var stringifiedPropsNoComments = getNonCommentsLines(strinifiedProps);
            if (existingPropsStr == null
                || !stringifiedPropsNoComments.equals(getNonCommentsLines(existingPropsStr)))
            {
                File outFile = configPath.toFile();
                FileWriter writer = new FileWriter(outFile);
                props.storeSorted(writer, propsComments);
                existingPropsStr = strinifiedProps;
            }

        } catch (IOException e) {
            LOGGER.warn("Failed to store preferences to disk.");
            LOGGER.error(e.getMessage());
        }

    }

    static final Style DEFAULT_STYLE = Style.EMPTY.withFormatting(Formatting.GOLD);
    static final Style ACCENT_STYLE = Style.EMPTY.withFormatting(Formatting.GREEN);

    public @NotNull Text stateAsText() {
        var result = Text.empty();
        String newLine = "\n";//System.getProperty("line.separator");

        result.append(Text.literal(displayName + " {").setStyle(DEFAULT_STYLE));
        result.append(newLine);
        var propsText = Text.empty();
        result.append(propsText);

        //print field names paired with their values
        for (Field field : publicFields) {
            try {
                if (Modifier.isPublic(field.getModifiers())) {
                    propsText.append(fieldAsText(field).append(newLine));
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        result.append(Text.literal("}").setStyle(ACCENT_STYLE));

        return result;

    }

    private List<String> publicFieldNames;
    private List<Field> publicFields;

    private void initFieldStorage() {
        Class<? extends Config> cls = this.getClass();
        publicFieldNames = Arrays.stream(cls.getDeclaredFields())
            .filter(field -> Modifier.isPublic(field.getModifiers()))
            .map(Field::getName)
            .sorted()
            .collect(Collectors.toList());
        publicFields = Arrays.stream(cls.getDeclaredFields())
            .filter(field -> Modifier.isPublic(field.getModifiers()))
            .sorted(Comparator.comparing(Field::getName))
            .collect(Collectors.toList());

    }

    public List<String> getPublicFieldNames() {
        return publicFieldNames;
    }

    private MutableText fieldAsText(Field field) throws IllegalAccessException {
        var value = (Option<?>) field.get(this);
        return Text.empty()
            .append(Text.literal(field.getName() + ": ").setStyle(DEFAULT_STYLE))
            .append(Text.literal(value.getValue().toString()));
    }

    public @Nullable MutableText getFieldValueAsText(String fieldName) throws NoSuchFieldException {
        try {
            return fieldAsText(this.getClass().getField(fieldName));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final List<Consumer<T>> configLoadHandlers = new ArrayList<>();

    public void registerLoadHandler(Consumer<T> handler) {
        configLoadHandlers.add(handler);
    }
}
