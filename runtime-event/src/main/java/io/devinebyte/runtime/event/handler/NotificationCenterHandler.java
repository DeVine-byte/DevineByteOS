package io.devinebyte.runtime.event.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class NotificationCenterHandler implements EventHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final String targetEventType;

    public NotificationCenterHandler(String targetEventType) {
        this.targetEventType = targetEventType;
    }

    @Override
    public String moduleId() {
        return "sales";
    }

    @Override
    public String eventType() {
        return this.targetEventType;
    }

    @Override
    public void handle(TenantContext ctx, DomainEvent event) {
        String tenantId = ctx.tenantId();
        String targetEvent = event.type();
        
        String relTemplates = "data/tenants/" + tenantId + "/notification_templates.json";
        String relIntegrations = "data/tenants/" + tenantId + "/integrations.json";

        // Multi-Path Fallback Scanner Strategy
        File templateFile = new File(relTemplates);
        if (!templateFile.exists()) templateFile = new File("../" + relTemplates);
        if (!templateFile.exists()) templateFile = new File("../../" + relTemplates);
        if (!templateFile.exists()) templateFile = new File("/data/data/com.termux/files/home/DevineByteOS/" + relTemplates);

        File integrationFile = new File(relIntegrations);
        if (!integrationFile.exists()) integrationFile = new File("../" + relIntegrations);
        if (!integrationFile.exists()) integrationFile = new File("../../" + relIntegrations);
        if (!integrationFile.exists()) integrationFile = new File("/data/data/com.termux/files/home/DevineByteOS/" + relIntegrations);

        if (!templateFile.exists() || !integrationFile.exists()) {
            System.err.println("[NOTIFICATION WARN] Configuration files missing on disk context. Checked paths for templates: " + templateFile.getAbsolutePath());
            return;
        }

        try {
            JsonNode templateRoot = MAPPER.readTree(Files.readAllBytes(templateFile.toPath()));
            JsonNode integrationRoot = MAPPER.readTree(Files.readAllBytes(integrationFile.toPath()));
            
            JsonNode templates = templateRoot.get("templates");
            JsonNode channels = integrationRoot.get("channels");

            if (templates == null || !templates.isArray() || channels == null) {
                System.err.println("[NOTIFICATION WARN] Invalid JSON layout formats inside configurations.");
                return;
            }

            JsonNode payloadNode = (JsonNode) event.payload();
            Map<String, String> contextTokens = new HashMap<>();
            
            java.util.Iterator<String> fields = payloadNode.fieldNames();
            while (fields.hasNext()) {
                String key = fields.next();
                contextTokens.put(key, payloadNode.get(key).asText());
            }

            for (JsonNode templateNode : templates) {
                String trigger = templateNode.get("eventTrigger").asText();
                
                if (trigger.equalsIgnoreCase(targetEvent)) {
                    String channelRef = templateNode.get("channelRef").asText();
                    String rawDestination = templateNode.get("destinationAddress").asText();
                    String messageBody = templateNode.get("messageTemplate").asText();

                    JsonNode channelConfig = channels.get(channelRef);
                    if (channelConfig == null) {
                        System.err.println("[NOTIFICATION WARN] Missing integration profile for alias ref: " + channelRef);
                        continue;
                    }

                    String providerType = channelConfig.get("provider").asText();

                    String finalDestination = rawDestination;
                    if (rawDestination.startsWith("{") && rawDestination.endsWith("}")) {
                        String cleanKey = rawDestination.replace("{", "").replace("}", "");
                        finalDestination = contextTokens.getOrDefault(cleanKey, "patient-fallback@stjude.org");
                    }

                    String finalMessage = messageBody;
                    for (Map.Entry<String, String> entry : contextTokens.entrySet()) {
                        finalMessage = finalMessage.replace("{" + entry.getKey() + "}", entry.getValue());
                    }

                    System.out.println(String.format(
                        "\n[NOTIFICATION OS ENGINE] [%s] Processing event: %s -> Route Channel: %s",
                        tenantId.toUpperCase(), targetEvent, providerType
                    ));
                    System.out.println(String.format(
                        "    ==> Dispatch Target: %s", finalDestination
                    ));
                    System.out.println("    ==> Payload Stream Content:\n" + finalMessage + "\n");
                }
            }
        } catch (Exception e) {
            System.err.println("[NOTIFICATION CRASH] Fatal exception in execution handler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

