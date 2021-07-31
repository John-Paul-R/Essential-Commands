package com.fibermc.essentialcommands.events;

public interface OptionChangedCallback<T> {

    void onOptionChanged(T newValue);
}
