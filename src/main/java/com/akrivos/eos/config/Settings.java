package com.akrivos.eos.config;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An Enum singleton used for application's settings.
 */
public enum Settings {
    INSTANCE;

    public static final SettingKey<String> SERVER_ADDRESS
            = new SettingKey<String>("server.address", "0.0.0.0");

    public static final SettingKey<Integer> SERVER_PORT
            = new SettingKey<Integer>("server.port", 8080);

    public static final SettingKey<String> SERVER_ROOT
            = new SettingKey<String>("server.root", "~/www");

    private static final Logger log = Logger.getLogger(Settings.class);
    private final Map<String, String> map;

    /**
     * Initialises the singleton instance of the Settings enum.
     */
    private Settings() {
        map = new HashMap<String, String>();
    }

    /**
     * Returns the value for a setting as a String. If the key is found, it
     * returns the value, otherwise, it returns the setting's default value.
     *
     * @param settingKey the setting's key.
     * @return if found, it returns the value as String, otherwise,
     *         it returns the setting's default value.
     */
    public String getValueFor(SettingKey<String> settingKey) {
        String value = map.get(settingKey.getKey());
        if (value != null)
            return value;

        if (log.isTraceEnabled()) {
            log.trace("There is no setting for " + settingKey.getKey()
                    + ". Returning default value: "
                    + settingKey.getDefaultValue());
        }

        return settingKey.getDefaultValue();
    }

    /**
     * Returns the value for a setting as an int. If the key is found, it
     * returns the value, otherwise, it returns the setting's default value.
     *
     * @param settingKey the setting's key.
     * @return if found, it returns the value as int, otherwise,
     *         it returns the setting's default value.
     */
    public int getValueAsIntegerFor(SettingKey<Integer> settingKey) {
        try {
            String value = map.get(settingKey.getKey());
            if (value != null)
                return Integer.parseInt(value);

            if (log.isTraceEnabled()) {
                log.trace("There is no setting for " + settingKey.getKey()
                        + ". Returning default value: "
                        + settingKey.getDefaultValue());
            }

            return settingKey.getDefaultValue();
        } catch (NumberFormatException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid setting for " + settingKey.getKey()
                        + ". Returning default value: "
                        + settingKey.getDefaultValue());
            }
            return settingKey.getDefaultValue();
        }
    }

    /**
     * Loads the configuration from a properties file.
     *
     * @param propertiesFile the properties file path.
     */
    public void loadSettings(String propertiesFile) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propertiesFile));

            // load all the settings from the file and put them in the map
            for (Object key : props.keySet()) {
                final String keyStr = String.valueOf(key);
                map.put(keyStr, props.getProperty(keyStr));
            }

        } catch (IOException ex) {
            log.error("Could not load configuration from properties file: "
                    + propertiesFile + ". Using default server configuration");
        }
    }
}