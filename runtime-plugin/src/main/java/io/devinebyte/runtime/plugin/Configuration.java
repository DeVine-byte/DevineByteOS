package io.devinebyte.runtime.plugin;

import java.util.Optional;

public interface Configuration {
    Optional<String> get(String key);
    String getOrDefault(String key, String defaultValue);
    boolean getBoolean(String key, boolean defaultValue);
    int getInt(String key, int defaultValue);
}

