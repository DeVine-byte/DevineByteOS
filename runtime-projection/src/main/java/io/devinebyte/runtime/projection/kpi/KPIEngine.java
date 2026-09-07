package io.devinebyte.runtime.projection.kpi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import jakarta.inject.Singleton;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class KPIEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CONFIG_PATH = "data/tenants/%s/dashboard_config.json";

    // In-memory counter matrix tracking telemetry logs: TenantId -> (Event_Metric -> Count)
    private final Map<String, Map<String, Double>> registry = new ConcurrentHashMap<>();

    /**
     * Increments or records metric telemetry occurrences based on published events.
     */
    public void recordMetric(String tenantId, String metricKey, double increment) {
        this.registry.computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>());
        Map<String, Double> tenantMetrics = this.registry.get(tenantId);
        tenantMetrics.put(metricKey, tenantMetrics.getOrDefault(metricKey, 0.0) + increment);
        
        System.out.println("[KPI ENGINE] Tracked event telemetry: " + metricKey + " += " + increment);
    }

    /**
     * Resolves and returns the fully evaluated real-time metrics grid for a specific tenant.
     * The Dashboard Engine invokes this exact method to read and populate your widgets!
     */
    public Map<String, Double> getTenantMetricsGrid(String tenantId) {
        Map<String, Double> grid = new HashMap<>();
        
        // 1. Seed with raw collected operational counts
        Map<String, Double> rawCounters = this.registry.getOrDefault(tenantId, new HashMap<>());
        grid.putAll(rawCounters);

        // 2. Read the dynamic client KPI rules matrix from disk storage
        String targetConfig = String.format(CONFIG_PATH, tenantId);
        File file = new File(targetConfig);
        
        if (!file.exists()) {
            return grid; // Fallback gracefully if config is not seeded yet
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(targetConfig)));
            JsonNode root = MAPPER.readTree(content);
            JsonNode calculations = root.get("kpiCalculations");

            if (calculations != null && calculations.isArray()) {
                for (JsonNode calc : calculations) {
                    String name = calc.get("name").asText();
                    String operation = calc.get("operation").asText();
                    String numeratorKey = calc.get("numerator").asText();
                    String denominatorKey = calc.get("denominator").asText();

                    double num = grid.getOrDefault(numeratorKey, 0.0);
                    double den = grid.getOrDefault(denominatorKey, 0.0);
                    double result = 0.0;

                    if ("RATIO".equalsIgnoreCase(operation)) {
                        if (den != 0.0) {
                            result = num / den;
                        }
                    } else if ("SUM".equalsIgnoreCase(operation)) {
                        result = num + den;
                    }

                    grid.put(name, result);
                }
            }
        } catch (Exception e) {
            System.err.println("[KPI ENGINE CRASH] Evaluation failed for tenant " + tenantId + ": " + e.getMessage());
        }

        return grid;
    }
}

