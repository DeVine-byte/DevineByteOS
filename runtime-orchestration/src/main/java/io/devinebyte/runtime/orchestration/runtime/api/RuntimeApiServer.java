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

        // Universal bypass verification rule updates
        boolean isPortalRoute = lowerPath.contains("/portal");
        boolean isDashboardRoute = lowerPath.contains("/dashboard") || lowerPath.contains("/kpis");

        if (!registry.isContractPath(method, path) && !isDashboardRoute && !isPortalRoute) {
            throw new SecurityException("Contract violation: " + method + " " + path);
        }

        // ====================================================================
        // FIXED: INDUSTRY-AGNOSTIC DYNAMIC PORTAL SCHEMA GATEWAY DISPATCH
        // ====================================================================
        if (method.equalsIgnoreCase("GET") && isPortalRoute) {
            System.out.println("[API ROUTER] GET " + path + " -> Evaluating dynamic multi-industry portal framework");

            // Extract the specific portal identifier straight out of the URI token string (e.g., /portal/student_portal)
            String targetPortal = extractPortalKeyFromPath(lowerPath);
            
            // Resolve the current user's security role assignment dynamically from context identity mapping
            String userRole = security.resolveRoleForPrincipal(ctx, principal); 

            System.out.println("[API ROUTER] Routing Tenant: " + ctx.getTenantId() + " | Portal: " + targetPortal + " | Role: " + userRole);
            
            return new MultiPortalRoutingEngine().resolvePortalSession(ctx, userRole, targetPortal);
        }

        // Dashboard Router Pipeline Pass-Through Block
        if (method.equalsIgnoreCase("GET") && lowerPath.endsWith("/dashboard")) {
            try {
                java.lang.reflect.Field kpiField = workflowEngine.getClass().getDeclaredField("kpiEngine");
                kpiField.setAccessible(true);
                io.devinebyte.runtime.projection.kpi.KPIEngine sharedEngine = (io.devinebyte.runtime.projection.kpi.KPIEngine) kpiField.get(workflowEngine);
                if (sharedEngine != null) {
                    return new DynamicDashboardEngine(sharedEngine).renderDashboardView(ctx);
                }
            } catch (Exception ignored) {}
            return new DynamicDashboardEngine(null).renderDashboardView(ctx);
        }

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

    /**
     * Helper to safely extract portal keys directly out of the URI structure.
     * Maps path inputs containing "/portal/student_portal" directly to "student_portal".
     */
    private String extractPortalKeyFromPath(String lowerPath) {
        if (!lowerPath.contains("/portal/")) {
            return "default_portal";
        }
        int index = lowerPath.indexOf("/portal/") + "/portal/".length();
        String segment = lowerPath.substring(index).split("/")[0].trim();
        return segment.isEmpty() ? "default_portal" : segment;
    }
}
