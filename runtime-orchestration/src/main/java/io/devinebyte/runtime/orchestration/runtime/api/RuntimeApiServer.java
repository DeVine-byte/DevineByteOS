package io.devinebyte.runtime.orchestration.runtime.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import io.devinebyte.runtime.orchestration.runtime.security.SecurityRuntime;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;

public record RuntimeApiServer(
    SecurityRuntime security,
    ContractRouteRegistry registry,
    RuntimeRegistry runtimeRegistry,
    WorkflowEngine workflowEngine
) {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules(); // FIXED

    public Object handle(TenantContext ctx, String principal, String method, String path, Object body, DiagnosticCollector diag) {
        if (!registry.isContractPath(method, path)) {
            // FIXED: Closed string quote right after colon
            throw new SecurityException("Contract violation: " + method + " " + path);
        }

        String command = registry.getCommand(method, path);
        String commandOrQuery = registry.getCommandOrQuery(method, path);

        if (command == null) {
            throw new IllegalArgumentException("No route for " + method + " " + path);
        }

        JsonNode bodyNode = body == null ? MAPPER.createObjectNode() : MAPPER.valueToTree(body);

        System.out.println("[API] " + method + " " + path + " -> " + command + " [" + commandOrQuery + "]");

        return workflowEngine.start(ctx, command, bodyNode, commandOrQuery);
    }
}
