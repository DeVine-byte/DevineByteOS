package io.devinebyte.runtime.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.event.diagnostics.EventDiagnostics;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.storage.FileEventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventReplayTest {

    @Test
    void replay_afterAppend_returnsAllEventsInOrder(@TempDir Path tmp) {
        TenantContext ctx = new TenantContext("acme", TenantLifecycle.ACTIVE, Set.of("SALES"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DiagnosticCollector collector = new DiagnosticCollector();
        EventDiagnostics diagnostics = new EventDiagnostics(collector);

        FileEventStore eventStore = new FileEventStore(tmp, mapper, diagnostics); // FIXED ORDER

        // 1. Append 3 events
        for(int i=0; i<3; i++) {
            ObjectNode payload = mapper.createObjectNode().put("seq", i);
            eventStore.append(ctx, DomainEvent.create(ctx, "OrderCreated", "1.0", payload));
        }

        // 2. Replay from 0
        List<DomainEvent> replayed = new ArrayList<>();
        eventStore.replay(ctx, 0, replayed::add);

        // 3. Assert: Same 3 events, same order
        assertEquals(3, replayed.size());
        assertEquals(0, replayed.get(0).payload().get("seq").asInt());
        assertEquals(2, replayed.get(2).payload().get("seq").asInt());
    }
}
