package io.devinebyte.compiler.core.context;

import io.devinebyte.compiler.core.diagnostics.DiagnosticCollector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompilationContext {
    private final TenantContext tenant;
    private final DiagnosticCollector diagnostics;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public CompilationContext(TenantContext tenant, DiagnosticCollector diagnostics) {
        this.tenant = tenant;
        this.diagnostics = diagnostics;
    }

    public TenantContext tenant() { 
        return tenant; 
    }

    public DiagnosticCollector diagnostics() { 
        return diagnostics; 
    }

    public <T> void put(String key, T value) { 
        attributes.put(key, value); 
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { 
        return (T) attributes.get(key); 
    }
}
