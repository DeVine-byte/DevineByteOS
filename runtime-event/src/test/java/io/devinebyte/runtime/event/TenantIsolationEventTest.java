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

class TenantIsolationEventTest {

    @Test
    void tenantA_cannotReadTenantB_Events(@TempDir Path tmp) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DiagnosticCollector collector = new DiagnosticCollector();
        EventDiagnostics diagnostics = new EventDiagnostics(collector);

        FileEventStore eventStore = new FileEventStore(tmp, mapper, diagnostics); // FIXED ORDER

        TenantContext acme = new TenantContext("acme", TenantLifecycle.ACTIVE, Set.of("SALES"));
        TenantContext beta = new TenantContext("beta", TenantLifecycle.ACTIVE, Set.of("SALES"));

        ObjectNode payload = mapper.createObjectNode().put("secret", "acme-data");
        eventStore.append(acme, DomainEvent.create(acme, "OrderCreated", "1.0", payload));

        // Beta tries to replay - should get 0 events because different file
        List<DomainEvent> betaStream = new ArrayList<>();
        eventStore.replay(beta, 0, betaStream::add);

        assertEquals(0, betaStream.size(), "Tenant isolation: beta should not see acme events");
    }
}
