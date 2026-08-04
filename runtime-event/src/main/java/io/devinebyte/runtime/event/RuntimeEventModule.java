package io.devinebyte.runtime.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.devinebyte.runtime.event.core.*;
import io.devinebyte.runtime.event.diagnostics.EventDiagnostics;
import io.devinebyte.runtime.event.handler.HandlerRegistry;
import io.devinebyte.runtime.event.storage.FileEventStore;
import io.devinebyte.runtime.event.storage.TenantBasePath;
import io.devinebyte.runtime.module.ModuleIsolationGuard; 
import io.devinebyte.runtime.module.ModuleRegistry;
import jakarta.inject.Singleton;
import java.nio.file.Path;

public final class RuntimeEventModule extends AbstractModule {
    private final Path basePath;
    private final ModuleRegistry registry;

    public RuntimeEventModule(Path basePath, ModuleRegistry registry) {
        this.basePath = basePath;
        this.registry = registry;
    }

    @Override
    protected void configure() {
        bind(Path.class).annotatedWith(TenantBasePath.class).toInstance(basePath);
        bind(EventStore.class).to(FileEventStore.class).in(Singleton.class);
        bind(ModuleRegistry.class).toInstance(registry); // use the one from bootstrap

        bind(HandlerRegistry.class).in(Singleton.class);
        bind(EventDiagnostics.class).in(Singleton.class);
        bind(EventDispatcher.class).in(Singleton.class);
        bind(EventReplayer.class).in(Singleton.class);
        bind(EventBus.class).in(Singleton.class);
        bind(ModuleIsolationGuard.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
