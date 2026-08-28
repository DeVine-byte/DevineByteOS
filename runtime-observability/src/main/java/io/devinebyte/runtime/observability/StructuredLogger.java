package io.devinebyte.runtime.observability;

import java.util.Map;

public interface StructuredLogger {
    void log(String level, String code, String message, Map<String, Object> context);
    
    default void info(String code, String message) {
        log("INFO", code, message, Map.of());
    }
    
    default void warn(String code, String message) {
        log("WARN", code, message, Map.of());
    }
    
    default void error(String code, String message) {
        log("ERROR", code, message, Map.of());
    }
}

