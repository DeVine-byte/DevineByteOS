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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventStoreTest {

    @Test
    void append_createsTenantFile(@TempDir Path tmp) {
        TenantContext ctx = new TenantContext("acme", TenantLifecycle.ACTIVE, Set.of("SALES"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DiagnosticCollector collector = new DiagnosticCollector();
        EventDiagnostics diagnostics = new EventDiagnostics(collector);

        FileEventStore eventStore = new FileEventStore(tmp, mapper, diagnostics); // FIXED ORDER

        ObjectNode payload = mapper.createObjectNode();
        payload.put("orderId", UUID.randomUUID().toString());
        DomainEvent e1 = DomainEvent.create(ctx, "OrderCreated", "1.0", payload);

        eventStore.append(ctx, e1);

        assertTrue(Files.exists(tmp.resolve("acme/events.log")), "events.log should be created for tenant");
    }
}
