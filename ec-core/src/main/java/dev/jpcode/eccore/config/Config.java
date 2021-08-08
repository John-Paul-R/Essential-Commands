package dev.jpcode.eccore.config;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Config {
    static final Logger LOGGER = LogManager.getLogger("ec-core-config");

    protected SortedProperties props;
    private final Path configPath;
    private final String displayName;
    private final String documentationLink;

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
            boolean fileAlreadyExisted = !inFile.createNewFile();
            if (fileAlreadyExisted) {
                props.load(new FileReader(inFile));
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load preferences.");
        }
        initProperties();
        storeProperties();
    }

    private void initProperties() {
        Class<? extends Config> cls = this.getClass();
        // Cursed reflection reloading of all properties.
        Arrays.stream(cls.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(ConfigOption.class))
            .forEach(field -> {
                try {
                    ((Option<?>) field.get(this)).loadAndSave(props);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
    }

    public void storeProperties() {
        try {
            File outFile = configPath.toFile();
            FileWriter writer = new FileWriter(outFile);

            props.storeSorted(writer, new StringBuilder(80)
                .append(displayName)
                .append("\n")
                .append("Config Documentation: ")
                .append(documentationLink)
                .toString()
            );
        } catch (IOException e) {
            LOGGER.warn("Failed to store preferences to disk.");
        }

    }

    static final Style DEFAULT_STYLE = Style.EMPTY.withFormatting(Formatting.GOLD);
    static final Style ACCENT_STYLE = Style.EMPTY.withFormatting(Formatting.GREEN);

    public @NotNull Text stateAsText() {
        LiteralText result = new LiteralText("");
        String newLine = "\n";//System.getProperty("line.separator");

        result.append(new LiteralText(displayName + " {").setStyle(DEFAULT_STYLE));
        result.append(newLine);
        LiteralText propsText = new LiteralText("");
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
        result.append(new LiteralText("}").setStyle(ACCENT_STYLE));

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
        return new LiteralText("")
            .append(new LiteralText(field.getName() + ": ").setStyle(DEFAULT_STYLE))
            .append(new LiteralText(field.get(this.getClass()).toString()));
    }

    public @Nullable MutableText getFieldValueAsText(String fieldName) throws NoSuchFieldException {
        try {
            return fieldAsText(this.getClass().getField(fieldName));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
