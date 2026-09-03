package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.repository.EntityRepository;
import io.devinebyte.runtime.repository.RepositoryFactory;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class WorkflowEngine {
    private WorkflowInstanceRepository repo;
    private WorkflowExecutor executor;
    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public WorkflowEngine(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    public void wireDependencies(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    @SuppressWarnings("unchecked")
    public Object start(TenantContext ctx, String command, JsonNode body, String commandOrQuery) {
        System.out.println("[WORKFLOW] Starting " + commandOrQuery + ": " + command + " with payload: " + body);

        WorkflowDefinition def = definitions.get(command);
        if (def == null) {
            throw new IllegalArgumentException("No workflow definition for: " + command);
        }

        Map<String, Object> input = MAPPER.convertValue(body, Map.class);
        Map<String, Object> runtimeContext = input != null ? new java.util.HashMap<>(input) : new java.util.HashMap<>();

        String tenantId = ctx.tenantId();
        String moduleId = def.moduleId();

        String entityName = command
                .replace("Handle", "")
                .replace("POST", "")
                .replace("GET", "")
                .replace("PUT", "");

        // ==========================================================
        // 1. HARDENED POST INTERCEPT ROUTE (WITH 409 DUPLICATE CHECK)
        // ==========================================================
        if (command.startsWith("Handle") && command.endsWith("POST")) {
            try {
                EntityRepository entityRepo = RepositoryFactory.get(tenantId, moduleId, entityName);

                Map<String, Object> dataPayload = new java.util.HashMap<>();
                if (runtimeContext.containsKey("body")) {
                    Object nestedBody = runtimeContext.get("body");
                    if (nestedBody instanceof Map) {
                        dataPayload.putAll((Map<String, Object>) nestedBody);
                    }
                }
                if (dataPayload.isEmpty()) {
                    dataPayload.putAll(runtimeContext);
                }

                String email = (String) dataPayload.get("email");
                if (email != null && !email.trim().isEmpty()) {
                    System.out.println("[ENGINE VALIDATOR] Guard checking unique constraint bounds for email: " + email);

                    java.lang.reflect.Field dsField = entityRepo.getClass().getDeclaredField("ds");
                    dsField.setAccessible(true);
                    javax.sql.DataSource ds = (javax.sql.DataSource) dsField.get(entityRepo);

                    String checkSql = "SELECT payload FROM " + (moduleId + "_" + entityName).toLowerCase();

                    try (java.sql.Connection c = ds.getConnection();
                         java.sql.PreparedStatement ps = c.prepareStatement(checkSql);
                         java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> record = MAPPER.readValue(rs.getString("payload"), Map.class);
                            if (email.equalsIgnoreCase((String) record.get("email"))) {
                                System.err.println("[CORE REJECTION] Duplicate identity target encountered: " + email);
                                throw new IllegalStateException(
                                        "409 Conflict: An operational entity with the unique email '"
                                                + email + "' already exists inside the engine system."
                                );
                            }
                        }
                    }
                }

                System.out.println("[ENGINE PERSISTENCE] Committing complete profile object layout grid down to disk storage...");
                String persistentId = entityRepo.upsert(dataPayload);

                System.out.println("[ENGINE DEBUG] Successfully wrote full record to DB with ID: " + persistentId);

                Map<String, Object> savedRecord = entityRepo.findById(persistentId);
                if (savedRecord != null) {
                    savedRecord.put("status", "SUCCESS");
                    return savedRecord;
                }

                dataPayload.put("id", persistentId);
                dataPayload.put("status", "SUCCESS");
                return dataPayload;

            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ex) {
                System.err.println("[CORE CRASH] Automatic structural transaction failed: " + ex.getMessage());
                throw new RuntimeException(
                        "500 Internal Server Error: Database engine unavailable - " + ex.getMessage(), ex
                );
            }
        }

        // ==========================================================
        // 2. HARDENED GET INTERCEPT ROUTE (STRICT FILTER PATTERN)
        // ==========================================================
        if (command.startsWith("Handle") && command.endsWith("GET")) {
            try {
                String targetId = null;

                if (runtimeContext.get("id") != null) {
                    targetId = runtimeContext.get("id").toString();
                } else if (runtimeContext.get("ID") != null) {
                    targetId = runtimeContext.get("ID").toString();
                }

                if (targetId == null
                        || targetId.trim().isEmpty()
                        || "YOUR_GENERATED_ID".equalsIgnoreCase(targetId)) {
                    throw new IllegalArgumentException(
                            "400 Bad Request: Missing or unparsed unique identifier parameter '?id=' in your query URL route."
                    );
                }

                System.out.println("[ENGINE QUERY] Fetching full database record for ID: " + targetId);
                EntityRepository entityRepo = RepositoryFactory.get(tenantId, moduleId, entityName);
                Map<String, Object> record = entityRepo.findById(targetId);

                if (record == null) {
                    throw new NoSuchElementException(
                            "404 Not Found: Entity matching ID '" + targetId + "' does not exist in module table " + entityName
                    );
                }

                return record;

            } catch (IllegalArgumentException | NoSuchElementException | IllegalStateException ex) {
                throw ex;
            } catch (Exception ex) {
                System.err.println("[CORE CRASH] Automatic structural query failed: " + ex.getMessage());
                throw new RuntimeException("500 Internal Server Error: Database transaction failed.", ex);
            }
        }

        if (this.executor == null) {
            throw new IllegalStateException("State machine executor reference is completely unassigned.");
        }
        return this.executor.start(ctx, def, input);
    }

    public boolean isSubscribedTo(String eventType) {
        return definitions.values().stream()
            .anyMatch(d -> d.findTransition(null, eventType) != null);
    }

    public WorkflowDefinition getDefinition(String workflowName) {
        return definitions.get(workflowName);
    }

    public void handleEvent(TenantContext ctx, UUID instanceId, DomainEvent event) {
        if (repo == null || executor == null) return;
        WorkflowInstance instance = repo.load(ctx, instanceId);
        if (instance == null) return;
        WorkflowDefinition def = definitions.get(instance.workflowName());
        if (def == null) return;
        WorkflowInstance newInstance = executor.handleEvent(ctx, def, instance, event);
        repo.save(newInstance);
    }

    public void register(WorkflowDefinition def) {
        definitions.put(def.name(), def);
    }
}

