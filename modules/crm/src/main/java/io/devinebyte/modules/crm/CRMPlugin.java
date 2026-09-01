package io.devinebyte.modules.crm;

import io.devinebyte.runtime.plugin.*;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class CRMPlugin implements RuntimePlugin {
    private PluginContext ctx;
    private WorkflowEngine workflowEngine;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override 
    public PluginDescriptor descriptor() {
        return new PluginDescriptor("crm", "1.0.0", "io.devinebyte.modules.crm.CRMPlugin", "1.0.0", "CRM");
    }

    @Override 
    public void initialize(PluginContext context, DiagnosticCollector d) {
        this.ctx = context;
        this.workflowEngine = ctx.services().getService(WorkflowEngine.class);

        // Register the workflow that APISchema.json points to
        workflowEngine.registerWorkflow("HandleCustomerPOST", this::handleCreateCustomer);
        workflowEngine.registerWorkflow("HandleCustomerGET", this::handleGetCustomers);
        
        ctx.logger().info("[CRM] Workflows registered");
    }

    private Map<String, Object> handleCreateCustomer(TenantContext tenant, Map<String, Object> cmd) {
        String id = UUID.randomUUID().toString();
        cmd.put("id", id);
        cmd.put("createdAt", Instant.now().toString());

        // Event-First: No DB save. RuntimeServices has no repo
        ObjectNode payload = mapper.valueToTree(Map.of(
            "customerId", id,
            "name", cmd.get("name"),
            "email", cmd.get("email")
        ));
        var event = DomainEvent.create(tenant, "crm", "CustomerCreated", "1.0", payload);
        ctx.eventBus().publish(tenant, event);

        ctx.logger().info("[CRM] Published CustomerCreated: {}", id);
        return cmd;
    }

    private Map<String, Object> handleGetCustomers(TenantContext tenant, Map<String, Object> query) {
        return Map.of("customers", java.util.List.of()); // empty for v0.1
    }

    @Override public void start(DiagnosticCollector d) {}
    @Override public void stop(DiagnosticCollector d) {}
    @Override public void shutdown(DiagnosticCollector d) {}
}
