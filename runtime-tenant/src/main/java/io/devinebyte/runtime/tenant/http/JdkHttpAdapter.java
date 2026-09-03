package io.devinebyte.runtime.tenant.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.orchestration.runtime.api.RuntimeApiServer;
import io.devinebyte.runtime.tenant.TenantRuntime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class JdkHttpAdapter {
    private final HttpServer server;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final Map<String, TenantRuntime> runtimes = new ConcurrentHashMap<>();

    public JdkHttpAdapter(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/api/", new ApiHandler());
        this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void registerTenant(TenantRuntime runtime) {
        String tenantId = runtime.context().tenantId();
        System.out.println("[DBRT] Registering tenant: " + tenantId);
        this.runtimes.put(tenantId, runtime);
    }

    public void start() {
        server.start();
        System.out.println("[DBRT] HTTP Server listening on http://localhost:" + server.getAddress().getPort());
    }

    class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String uriPath = exchange.getRequestURI().getPath();

                // 1. GLOBAL SYSTEM HEALTH PROBE
                if ("/api/system/health".equals(uriPath)) {
                    String method = exchange.getRequestMethod().toUpperCase();
                    if ("GET".equals(method)) {
                        send(exchange, 200, Map.of(
                            "status", "UP",
                            "timestamp", java.time.Instant.now().toString(),
                            "active_tenants", runtimes.size()
                        ));
                    } else {
                        send(exchange, 405, Map.of("error", "Method Not Allowed"));
                    }
                    return;
                }

                // 2. TENANT-SPECIFIC CONTRACT ROUTING
                String[] parts = uriPath.split("/");

                if (parts.length < 3) {
                    send(exchange, 404, Map.of("error", "Invalid path structure. Use /api/{tenantId}/..."));
                    return;
                }

                String tenantId = parts[2];

                TenantRuntime runtime = runtimes.get(tenantId);
                if (runtime == null) {
                    System.out.println("[DBRT] [404] Tenant '" + tenantId + "' requested but not found. Available: " + runtimes.keySet());
                    send(exchange, 404, Map.of(
                        "error", "Tenant runtime not found or active: " + tenantId,
                        "available_tenants", runtimes.keySet()
                    ));
                    return;
                }

                String internalPath = "/";
                if (parts.length > 3) {
                    internalPath = "/" + String.join("/", java.util.Arrays.copyOfRange(parts, 3, parts.length));
                }

                String method = exchange.getRequestMethod().toUpperCase();
                Object body = null;

                // FIX: Parse raw URL query string for GET parameters
                if ("GET".equals(method)) {
                    String query = exchange.getRequestURI().getRawQuery();
                    if (query != null && !query.isEmpty()) {
                        Map<String, String> queryMap = new HashMap<>();
                        String[] pairs = query.split("&");
                        for (String pair : pairs) {
                            int idx = pair.indexOf("=");
                            if (idx > 0) {
                                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                                queryMap.put(key, value);
                            }
                        }
                        body = queryMap; // Pass query map directly into the engine's body channel
                    }
                } else {
                    // Standard POST JSON handler remains unchanged
                    try (InputStream is = exchange.getRequestBody()) {
                        byte[] bytes = is.readAllBytes();
                        if (bytes.length > 0) {
                            body = mapper.readValue(bytes, Object.class);
                        }
                    }
                }

                TenantContext ctx = runtime.context();
                String principal = "anonymous";
                DiagnosticCollector diag = new DiagnosticCollector();

                RuntimeApiServer apiServer = runtime.orchestration().apiServer();
                Object result = apiServer.handle(ctx, principal, method, internalPath, body, diag);

                send(exchange, 200, result);

            } catch (SecurityException e) {
                send(exchange, 403, Map.of("error", e.getMessage()));
            } catch (IllegalArgumentException e) {
                // Return proper 400 Bad Request payloads for validation/missing parameter blocks
                send(exchange, 400, Map.of("error", e.getMessage()));
            } catch (java.util.NoSuchElementException e) {
                // FIX: Map engine element absence directly to a true HTTP 404 Not Found response code!
                send(exchange, 404, Map.of("error", e.getMessage()));
            } catch (IllegalStateException e) {
                // Return proper 409 Conflict payloads for unique constraints
                int status = e.getMessage().contains("409") ? 409 : 500;
                send(exchange, status, Map.of("error", e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                send(exchange, 500, Map.of("error", "Internal execution fault: " + e.getMessage()));
            } finally {
                exchange.close();
            }
        }
    }

    private void send(HttpExchange exchange, int status, Object obj) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(obj);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}

