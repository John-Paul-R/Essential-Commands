package dev.jpcode.eccore.config;

import java.util.Arrays;
import java.util.List;

public abstract class ConfigSectionSkeleton {
    public final String name;
    private List<? extends Option<?>> options;

    protected ConfigSectionSkeleton(String name) {
        this.name = name;

    }

    public List<? extends Option<?>> getOptions() {
        if (options != null) {
            return options;
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
}
