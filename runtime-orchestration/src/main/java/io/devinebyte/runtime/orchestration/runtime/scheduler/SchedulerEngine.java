package io.devinebyte.runtime.orchestration.runtime.scheduler;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.event.model.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class SchedulerEngine {
    private final EventBus eventBus;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ObjectMapper mapper = new ObjectMapper();

    public SchedulerEngine(EventBus eventBus) { this.eventBus = eventBus; }

    public void schedule(TenantContext ctx, String eventType, Duration delay) {
        executor.schedule(() -> {
            DomainEvent event = DomainEvent.create(ctx, eventType, "1.0", mapper.createObjectNode());
            eventBus.publish(ctx, event); // Event Sourced Only
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
}
