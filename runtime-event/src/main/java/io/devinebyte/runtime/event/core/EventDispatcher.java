package io.devinebyte.runtime.event.core;                        

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.handler.HandlerRegistry;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public final class EventDispatcher {
    private final HandlerRegistry registry;
    private final ModuleIsolationGuard guard;

    @Inject
    public EventDispatcher(HandlerRegistry registry, ModuleIsolationGuard guard) {
        this.registry = registry;                                        
        this.guard = guard;
    }

    public void dispatch(TenantContext ctx, DomainEvent event) {
        if (event == null || event.metadata() == null) {
            return;
        }

        // Extract the explicit tracking metadata sourceModule that emitted the event payload
        String sourceModule = event.metadata().sourceModule();
        if (sourceModule == null || sourceModule.isBlank()) {
            sourceModule = "RUNTIME"; // Global engine core fallback mapping boundary
        }

        final String finalSourceModule = sourceModule;

        registry.getHandlers(event.type())
            .stream()
            .filter(h -> {
                try {                                                                
                    // FIXED: Fulfill strict Item 5 cross-module dependency mapping checks to stop telemetry leaks
                    guard.assertAccessPermitted(
                        ctx, 
                        finalSourceModule, 
                        h.moduleId(), 
                        "subscribe:" + event.type()
                    );
                    return true;
                } catch (Exception e) {
                    // Suppress rogue handler execution routes if authorization checks fail
                    return false; 
                }
            })
            .forEach(h -> h.handle(ctx, event));
    }
}

