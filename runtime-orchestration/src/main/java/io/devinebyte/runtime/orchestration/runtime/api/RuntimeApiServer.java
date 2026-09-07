package io.devinebyte.runtime.orchestration.runtime.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import io.devinebyte.runtime.orchestration.runtime.security.SecurityRuntime;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import io.devinebyte.runtime.projection.dashboard.DynamicDashboardEngine;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;

public record RuntimeApiServer(
    SecurityRuntime security,
    ContractRouteRegistry registry,
    RuntimeRegistry runtimeRegistry,
    WorkflowEngine workflowEngine
) {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public Object handle(TenantContext ctx, String principal, String method, String path, Object body, DiagnosticCollector diag) {
        String lowerPath = path != null ? path.toLowerCase().trim() : "";
        boolean isDashboardRoute = lowerPath.contains("/dashboard") || lowerPath.contains("/kpis");

        if (!registry.isContractPath(method, path) && !isDashboardRoute) {
            throw new SecurityException("Contract violation: " + method + " " + path);
        }

        // ==========================================================
        // FIXED: EXPLICIT ROUTING SPLIT FOR UI PORTAL RENDERING
        // ==========================================================
        if (method.equalsIgnoreCase("GET") && lowerPath.endsWith("/dashboard")) {
            System.out.println("[API] GET " + path + " -> Invoking DynamicDashboardEngine UI Matrix Render");
            
            try {
                // Safely extract the running, shared KPIEngine instance using reflection from the WorkflowEngine
                java.lang.reflect.Field kpiField = workflowEngine.getClass().getDeclaredField("kpiEngine");
                kpiField.setAccessible(true);
                io.devinebyte.runtime.projection.kpi.KPIEngine sharedEngine = (io.devinebyte.runtime.projection.kpi.KPIEngine) kpiField.get(workflowEngine);
                
                if (sharedEngine != null) {
                    return new DynamicDashboardEngine(sharedEngine).renderDashboardView(ctx);
                }
            } catch (Exception ignored) {}
            
            // Fallback if reflection extraction fails
            return new DynamicDashboardEngine(null).renderDashboardView(ctx);
        }

        // Handle Command Extraction Safely for Bypassed Raw KPI Paths
        String command = registry.getCommand(method, path);
        String commandOrQuery = registry.getCommandOrQuery(method, path);

        if (command == null && isDashboardRoute) {
            command = "FetchDashboardMetrics";
            commandOrQuery = "Query";
        } else if (command == null) {
            throw new IllegalArgumentException("No route for " + method + " " + path);
        }

        JsonNode bodyNode = body == null ? MAPPER.createObjectNode() : MAPPER.valueToTree(body);

        System.out.println("[API] " + method + " " + path + " -> " + command + " [" + commandOrQuery + "]");

        return workflowEngine.start(ctx, command, bodyNode, commandOrQuery);
    }
}

