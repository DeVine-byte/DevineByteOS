package io.devinebyte.compiler.core.bus;

import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Singleton
public class CompilerEventBus {
    private final ConcurrentHashMap<String, Consumer<Object>> listeners = new ConcurrentHashMap<>();

    public void subscribe(String eventType, Consumer<Object> handler) {
        listeners.put(eventType, handler);
    }
    
    public void publish(String eventType, Object payload) {
        var handler = listeners.get(eventType);
        if (handler != null) {
            handler.accept(payload);
        }
    }
}
