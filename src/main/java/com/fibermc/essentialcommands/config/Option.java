package com.fibermc.essentialcommands.config;

import com.fibermc.essentialcommands.events.OptionChangedCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Properties;

public class Option<T> {

    private final String key;
    private final T defaultValue;
    private final ValueParser<T> parser;
    private T value;

    public final Event<OptionChangedCallback<T>> CHANGE_EVENT = EventFactory.createArrayBacked(OptionChangedCallback.class,
        (listeners) -> (newValue) -> {
            for (OptionChangedCallback<T> event : listeners) {
                event.onOptionChanged(newValue);
            }
        }
    );

    public Option(String key, T defaultValue, ValueParser<T> parser) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    public Option<T> loadAndSave(Properties props) {
        this.loadFrom(props);
        this.saveIfAbsent(props);
        return this;
    }

    public Option<T> loadFrom(Properties props) {
        T prevValue = this.value;
        this.value = parser.parseValue(String.valueOf(props.getOrDefault(this.key, String.valueOf(this.defaultValue))));
        if (!this.value.equals(prevValue)) {
            CHANGE_EVENT.invoker().onOptionChanged(this.value);
        }
        return this;
    }

    public void saveIfAbsent(Properties props) {
        props.putIfAbsent(this.key, String.valueOf(this.value));
    }

    public T getValue() {
        if (value != null)
            return value;
        return defaultValue;
    }
}
