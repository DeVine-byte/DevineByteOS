package io.devinebyte.runtime.projection.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.projection.kpi.KPIEngine;
import jakarta.inject.Singleton;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DynamicDashboardEngine {

    private final KPIEngine kpiEngine;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DynamicDashboardEngine(KPIEngine kpiEngine) {
        this.kpiEngine = kpiEngine;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> renderDashboardView(TenantContext ctx) {
        Map<String, Object> renderedResponse = new HashMap<>();
        String tenantId = ctx.tenantId();
        
        String relativeTarget = "data/tenants/" + tenantId + "/projections/dashboard_definitions.json";
        
        // Scan multiple execution directory levels to find the configurations sheet
        File configFile = new File(relativeTarget);
        
        if (!configFile.exists()) {
            configFile = new File("../" + relativeTarget);
        }
        if (!configFile.exists()) {
            configFile = new File("../../" + relativeTarget);
        }
        if (!configFile.exists()) {
            configFile = new File("/data/data/com.termux/files/home/DevineByteOS/" + relativeTarget);
        }

        try {
            if (!configFile.exists()) {
                System.err.println("[DASHBOARD ENGINE] Missing file template looking at: " + configFile.getAbsolutePath());
                renderedResponse.put("portalName", "Default System Portal Gateway");
                renderedResponse.put("widgets", new java.util.ArrayList<>());
                renderedResponse.put("error", "No projection mapping found for tenant: " + tenantId);
                return renderedResponse;
            }

            // 1. Hydrate UI blueprint structure from dynamic definitions file
            String jsonContent = new String(Files.readAllBytes(configFile.toPath()));
            JsonNode schemaRoot = MAPPER.readTree(jsonContent);

            renderedResponse.put("portalName", schemaRoot.get("portalName").asText());
            renderedResponse.put("refreshRateSeconds", schemaRoot.get("refreshRateSeconds").asInt());
            renderedResponse.put("layoutGrid", MAPPER.convertValue(schemaRoot.get("layoutGrid"), Map.class));

            // 2. Fetch computed live operational values from the KPI engine sub-tier
            Map<String, Double> engineMetrics = kpiEngine != null 
                ? kpiEngine.getTenantMetricsGrid(tenantId) 
                : new HashMap<>();
            
            // 3. Bind live numbers directly into matching configuration blocks
            JsonNode widgetsNode = schemaRoot.get("widgets");
            java.util.List<Map<String, Object>> hydratedWidgets = new java.util.ArrayList<>();

            if (widgetsNode != null && widgetsNode.isArray()) {
                for (JsonNode widget : widgetsNode) {
                    Map<String, Object> widgetMap = MAPPER.convertValue(widget, Map.class);
                    Map<String, Object> dataSource = (Map<String, Object>) widgetMap.get("dataSource");

                    if (dataSource != null && dataSource.containsKey("kpiName")) {
                        String targetKPI = (String) dataSource.get("kpiName");
                        widgetMap.put("liveValue", engineMetrics.getOrDefault(targetKPI, 0.0));
                    } else {
                        widgetMap.put("liveValue", 0.0);
                    }
                    hydratedWidgets.add(widgetMap);
                }
            }
            
            renderedResponse.put("widgets", hydratedWidgets);
            renderedResponse.put("status", "HEALTHY");

        } catch (Exception e) {
            System.err.println("[DASHBOARD ENGINE CRASH] Failed rendering dynamic UI view: " + e.getMessage());
            renderedResponse.put("error", "500 Internal Projection Compilation Error");
            renderedResponse.put("details", e.getMessage());
        }

        return renderedResponse;
    }
}

