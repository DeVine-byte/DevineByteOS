package io.devinebyte.runtime.event.handler;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class HandlerRegistry {
    private final Map<String, List<EventHandler>> handlers = new ConcurrentHashMap<>();

    public void register(EventHandler handler) {
        handlers.computeIfAbsent(handler.eventType(), k -> new java.util.ArrayList<>()).add(handler);
    }

    public List<EventHandler> getHandlers(String eventType) {
        return handlers.getOrDefault(eventType, List.of());
    }
}
