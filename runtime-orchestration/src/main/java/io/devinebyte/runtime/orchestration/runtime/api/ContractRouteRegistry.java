package io.devinebyte.runtime.orchestration.runtime.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContractRouteRegistry {
    private final Map<String, Route> ROUTES = new ConcurrentHashMap<>(); // no longer static
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ContractRouteRegistry(FileSystem fs) { // ADD constructor
        Path schemaPath = fs.getPath("/contracts/APISchema.json");
        if (!Files.exists(schemaPath)) {
            throw new IllegalStateException("APISchema.json not found in dbpkg at /contracts/");
        }
        try (InputStream is = Files.newInputStream(schemaPath)) {
            JsonNode root = MAPPER.readTree(is);

            for (JsonNode contract : root) {
                String module = contract.get("module").asText();
                JsonNode paths = contract.get("paths");
                for (JsonNode n : paths) {
                    String key = n.get("method").asText() + ":" + n.get("path").asText();
                    ROUTES.put(key, new Route(
                        n.get("handler").asText(),
                        n.get("commandOrQuery").asText(),
                        n.get("requiredPermission").asText(),
                        module
                    ));
                }
            }
            System.out.println("[DBRT] Loaded " + ROUTES.size() + " routes from APISchema.json");
        } catch (Exception e) { throw new RuntimeException("Failed to load APISchema.json", e); }
    }

    public boolean isContractPath(String method, String path) { // no longer static
        return ROUTES.containsKey(method + ":" + path);
    }

    public String getCommand(String method, String path) {
        Route r = ROUTES.get(method + ":" + path);
        return r == null ? null : r.handler();
    }

    public String getCommandOrQuery(String method, String path) {
        Route r = ROUTES.get(method + ":" + path);
        return r == null ? null : r.commandOrQuery();
    }

    private record Route(String handler, String commandOrQuery, String requiredPermission, String module) {}
}
