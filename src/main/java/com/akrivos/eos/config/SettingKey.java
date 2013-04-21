package com.akrivos.eos.config;

/**
 * A representation of a setting key along with its default value.
 *
 * @param <T> the type of the value.
 */
public class SettingKey<T> {
    private final String key;
    private final T defaultValue;

    /**
     * Initialises a new instance of the SettingKey class.
     *
     * @param key          the actual key.
     * @param defaultValue the default value.
     */
    public SettingKey(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    /**
     * Accessor for the setting's default value.
     *
     * @return the setting's default value.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Accessor for the setting's actual key.
     *
     * @return the setting's actual key.
     */
    public String getKey() {
        return key;
    }
}