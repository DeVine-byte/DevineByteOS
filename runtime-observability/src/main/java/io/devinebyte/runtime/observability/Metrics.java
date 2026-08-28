package io.devinebyte.runtime.observability;

import java.util.Map;

public interface Metrics {
    void counter(String name, long delta, Map<String, String> tags);
    void gauge(String name, double value, Map<String, String> tags);
    void timer(String name, double durationSeconds, Map<String, String> tags);
}

