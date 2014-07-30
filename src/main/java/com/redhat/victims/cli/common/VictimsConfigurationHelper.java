package com.redhat.victims.cli.common;

import java.util.HashMap;
import java.util.Set;

import com.redhat.victims.VictimsConfig;
import com.redhat.victims.VictimsConfig.Key;

public class VictimsConfigurationHelper {

    protected static final Set<String> keys = VictimsConfig.DEFAULT_PROPS
            .keySet();
    protected static final HashMap<String, String> options = new HashMap<String, String>();
    protected static final HashMap<String, String> descriptions = new HashMap<String, String>();

    protected static String makeOption(String key) {
        return String.format("--%s", key.replace(".", "-"));
    }
    
    static {
        for (String key : keys) {
            options.put(makeOption(key), key);
        }

        descriptions.put(Key.HOME,
                "set the directory where victims data should be stored");
        descriptions.put(Key.DB_USER,
                "set the user to connect to the victims database");
        descriptions.put(Key.DB_PASS,
                "set the password to use to connect to the victims database");
        descriptions.put(Key.DB_URL, 
                "the jdbc url connection string to use with the victims database");
        descriptions.put(Key.DB_DRIVER,
                "the jdbc driver to use when connecting to the victims database");
        descriptions.put(Key.URI,
                "the uri to use to synchronize with the victims database");
        descriptions.put(Key.ENTRY,
                "the uri path to use when connecting to the victims service");
    }

    public static Set<String> getKeys() {
        return keys;
    }

    public static Set<String> getOptions() {
        return options.keySet();
    }

    public static String getOptionFromKey(String key) {
        if (options.containsValue(key)) {
            return makeOption(key);
        }
        return null;
    }

    public static String getKeyFromOption(String option) {
        return options.get(option);
    }

    public static String getDescriptionFromKey(String key) {
        if (descriptions.containsKey(key)) {
            return descriptions.get(key);
        }
        return "";
    }

}
