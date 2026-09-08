package io.devinebyte.runtime.orchestration.runtime.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import jakarta.inject.Singleton;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Singleton
public final class MultiPortalRoutingEngine {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PORTAL_CONFIG_PATH = "data/tenants/%s/portals_config.json";

    public Map<String, Object> resolvePortalSession(TenantContext ctx, String userRole, String targetPortalKey) {
        Map<String, Object> response = new HashMap<>();
        String tenantId = ctx.tenantId();
        String relPath = String.format(PORTAL_CONFIG_PATH, tenantId);

        // Multi-Path Resilient Scan Logic
        File configFile = new File(relPath);
        if (!configFile.exists()) configFile = new File("../" + relPath);
        if (!configFile.exists()) configFile = new File("../../" + relPath);
        if (!configFile.exists()) configFile = new File("/data/data/com.termux/files/home/DevineByteOS/" + relPath);

        if (!configFile.exists()) {
            response.put("error", "404 Not Found");
            response.put("details", "No custom portal schema matrix provisioned for tenant: " + tenantId);
            return response;
        }

        try {
            JsonNode root = MAPPER.readTree(Files.readAllBytes(configFile.toPath()));
            JsonNode portals = root.get("availablePortals");

            if (portals == null || !portals.has(targetPortalKey)) {
                response.put("error", "403 Forbidden");
                response.put("details", "Requested portal structure does not exist within the client footprint mapping layout.");
                return response;
            }

            JsonNode targetPortal = portals.get(targetPortalKey);
            JsonNode allowedRolesNode = targetPortal.get("allowedRoles");

            // Role-Security Verification Evaluation Loop
            boolean isAuthorized = false;
            if (allowedRolesNode != null && allowedRolesNode.isArray()) {
                for (JsonNode role : allowedRolesNode) {
                    if (role.asText().equalsIgnoreCase(userRole)) {
                        isAuthorized = true;
                        break;
                    }
                }
            }

            if (!isAuthorized) {
                response.put("status", "ACCESS_DENIED");
                response.put("message", "Security Exception: Role profile assignment [" + userRole + "] lacks authorization bounds to view portal: " + targetPortalKey);
                return response;
            }

            // Hydrate dynamic portal metadata context
            response.put("status", "AUTHORIZED");
            response.put("portalTitle", targetPortal.get("portalTitle").asText());
            response.put("navigation", MAPPER.convertValue(targetPortal.get("navigationMenu"), java.util.List.class));
            response.put("actionsGrid", MAPPER.convertValue(targetPortal.get("allowedActions"), java.util.List.class));
            response.put("industryContext", root.get("industryProfile").asText());

        } catch (Exception e) {
            response.put("error", "500 Internal Error");
            response.put("details", e.getMessage());
        }

        return response;
    }
}

