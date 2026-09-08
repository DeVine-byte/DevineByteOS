package io.devinebyte.modules.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import jakarta.inject.Singleton;

import java.io.File;
import java.nio.file.Files;

@Singleton
public final class OperationalPolicyEngine {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String POLICY_PATH = "data/tenants/%s/operational_policies.json";

    public void evaluateEvent(TenantContext ctx, DomainEvent event, Map<String, Double> liveMetrics) {
        String tenantId = ctx.tenantId();
        String targetEvent = event.type();
        String relPath = String.format(POLICY_PATH, tenantId);

        File file = new File(relPath);
        if (!file.exists()) file = new File("../" + relPath);
        if (!file.exists()) file = new File("../../" + relPath);
        if (!file.exists()) file = new File("/data/data/com.termux/files/home/DevineByteOS/" + relPath);

        if (!file.exists()) return;

        try {
            JsonNode root = MAPPER.readTree(Files.readAllBytes(file.toPath()));
            JsonNode triggers = root.get("reactiveTriggers");
            JsonNode warnings = root.get("thresholdWarnings");

            // Mechanism 1: Smooth Operations Reactive Prompts
            if (triggers != null && triggers.isArray()) {
                JsonNode payload = (JsonNode) event.payload();
                for (JsonNode trigger : triggers) {
                    if (trigger.get("onEvent").asText().equalsIgnoreCase(targetEvent)) {
                        String field = trigger.get("conditionField").asText();
                        String expectedValue = trigger.get("conditionValue").asText();

                        if (payload.has(field) && payload.get(field).asText().equalsIgnoreCase(expectedValue)) {
                            System.out.println(String.format(
                                "\n[🤖 POLICY ENGINE SYSTEM PROMPT] [%s] Match found! Action Target -> %s\n     Instruction: %s",
                                tenantId.toUpperCase(), trigger.get("autoPromptAction").asText(), trigger.get("uiInstruction").asText()
                            ));
                        }
                    }
                }
            }

            // Mechanism 2: Threshold Inventory/Safety Warnings
            if (warnings != null && warnings.isArray() && liveMetrics != null) {
                for (JsonNode warning : warnings) {
                    String telemetryKey = warning.get("monitoredTelemetry").asText();
                    double floor = warning.get("criticalFloor").asDouble();
                    double currentLiveValue = liveMetrics.getOrDefault(telemetryKey, 0.0);

                    if (currentLiveValue < floor && liveMetrics.containsKey(telemetryKey)) {
                        System.out.println(String.format(
                            "\n[⚠️ AUTOMATION WARNING] [%s] %s (Threshold Floor: %s, Current Active: %s)",
                            tenantId.toUpperCase(), warning.get("warningMessage").asText(), floor, currentLiveValue
                        ));
                    }
                }
            }

        } catch (Exception ignored) {}
    }
}

