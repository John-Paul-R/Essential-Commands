package dev.jpcode.eccore.config;

import java.util.Properties;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class Option<T> {

    private final String key;
    private final T defaultValue;
    private final ValueParser<T> parser;
    private final StringSerializer<T> serializer;
    private T value;

    public final Event<Consumer<T>> changeEvent = EventFactory.createArrayBacked(Consumer.class,
        (listeners) -> (newValue) -> {
            for (Consumer<T> event : listeners) {
                event.accept(newValue);
            }
        }
    );

    public Option(String key, T defaultValue, ValueParser<T> parser, StringSerializer<T> serializer) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.parser = parser;
        this.serializer = serializer;
    }

    public Option(String key, T defaultValue, ValueParser<T> parser) {
        this(key, defaultValue, parser, String::valueOf);
    }

    public Option<T> loadAndSave(Properties props) {
        this.loadFrom(props);
        this.saveIfAbsent(props);
        return this;
    }

    public Option<T> loadFrom(Properties props) {
        T prevValue = this.value;
        this.value = parser.parseValue(String.valueOf(props.getOrDefault(this.key, serializer.serialize(this.defaultValue))));
        if (!this.value.equals(prevValue)) {
            changeEvent.invoker().accept(this.value);
        }
        return this;
    }

    public void saveIfAbsent(Properties props) {
        props.putIfAbsent(this.key, serializer.serialize(this.value));
    }

    public T getValue() {
        if (value != null)
            return value;
        return defaultValue;
    }
}
