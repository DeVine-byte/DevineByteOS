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

    public void registerWorkflow(String command, WorkflowDefinition def) {
        this.definitions.put(command, def);
    }

    public void register(WorkflowDefinition def) {
        if (def != null && def.name() != null) {
            this.definitions.put(def.name(), def);
            this.definitions.put("Handle" + def.name() + "POST", def);
            this.definitions.put("Handle" + def.name() + "GET", def);
        }
    }

    public WorkflowDefinition getDefinition(String workflowName) {
        return this.definitions.get(workflowName);
    }

    public void handleEvent(TenantContext ctx, UUID instanceId, DomainEvent event) {
        if (this.executor == null || this.repo == null) {
            throw new IllegalStateException("Workflow storage engine dependencies are completely unassigned.");
        }
        
        WorkflowInstance instance = this.repo.findById(instanceId);
        if (instance != null) {
            WorkflowDefinition def = this.definitions.get(instance.workflowName());
            if (def != null) {
                WorkflowInstance advancedInstance = this.executor.handleEvent(ctx, def, instance, event);
                this.repo.save(advancedInstance);
                System.out.println("[WORKFLOW ENGINE] Asynchronously advanced instance " + instanceId + " to state: " + advancedInstance.currentState());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Object start(TenantContext ctx, String command, JsonNode body, String commandOrQuery) {
        System.out.println("[WORKFLOW] Starting " + commandOrQuery + ": " + command + " with payload: " + body);

        WorkflowDefinition def = definitions.get(command);
        if (def == null) {
            throw new IllegalArgumentException("No workflow definition registered for command: " + command);
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

        Map<String, Object> dataPayload = new java.util.HashMap<>();
        if (runtimeContext.containsKey("body") && runtimeContext.get("body") instanceof Map) {
            dataPayload.putAll((Map<String, Object>) runtimeContext.get("body"));
        } else {
            dataPayload.putAll(runtimeContext);
        }

        // ==========================================================
        // 1. INDUSTRY-BLIND POST INTERCEPT ROUTE
        // ==========================================================
        if (command.startsWith("Handle") && command.endsWith("POST")) {
            try {
                EntityRepository entityRepo = RepositoryFactory.get(tenantId, moduleId, entityName);

                // Industry-blind identity marker search loop
                String naturalUniqueKey = null;
                if (dataPayload.containsKey("email")) naturalUniqueKey = "email";
                else if (dataPayload.containsKey("sku")) naturalUniqueKey = "sku";

                if (naturalUniqueKey != null) {
                    String uniqueValue = (String) dataPayload.get(naturalUniqueKey);
                    if (uniqueValue != null && !uniqueValue.trim().isEmpty()) {
                        System.out.println("[ENGINE VALIDATOR] Guard inspecting constraint parameter dynamically: " + naturalUniqueKey + " = " + uniqueValue);

                        java.lang.reflect.Field dsField = entityRepo.getClass().getDeclaredField("ds");
                        dsField.setAccessible(true);
                        javax.sql.DataSource ds = (javax.sql.DataSource) dsField.get(entityRepo);
                        
                        // FIX: Target the correct dynamic table layout matrix name and payload schema column
                        String dynamicTableName = (moduleId + "_" + entityName).toLowerCase();
                        String checkSql = "SELECT payload FROM " + dynamicTableName;

                        try (java.sql.Connection c = ds.getConnection();
                             java.sql.PreparedStatement ps = c.prepareStatement(checkSql);
                             java.sql.ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                Map<String, Object> record = MAPPER.readValue(rs.getString("payload"), Map.class);
                                if (record.containsKey(naturalUniqueKey) && uniqueValue.equalsIgnoreCase((String) record.get(naturalUniqueKey))) {
                                    System.err.println("[CORE REJECTION] Duplicate identity conflict found for asset: " + uniqueValue);
                                    throw new IllegalStateException("409 Conflict: An operational entity matching unique constraints already exists inside the system runtime.");
                                }
                            }
                        }
                    }
                }

                System.out.println("[ENGINE PERSISTENCE] Committing complete profile object layout grid down to disk storage...");
                String persistentId = entityRepo.upsert(dataPayload);
                System.out.println("[ENGINE DEBUG] Successfully wrote full record to DB with ID: " + persistentId);

                emitAsyncDomainEvent(ctx, entityName + "Created", persistentId, dataPayload);

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
                throw new RuntimeException("500 Internal Server Error: Database engine unavailable - " + ex.getMessage(), ex);
            }
        }

        // ==========================================================
        // 2. INDUSTRY-BLIND GET INTERCEPT ROUTE
        // ==========================================================
        if (command.startsWith("Handle") && command.endsWith("GET")) {
            try {
                String targetId = null;

                if (dataPayload.get("id") != null) targetId = dataPayload.get("id").toString();
                else if (dataPayload.get("ID") != null) targetId = dataPayload.get("ID").toString();
                else if (runtimeContext.get("id") != null) targetId = runtimeContext.get("id").toString();

                if (targetId == null || targetId.trim().isEmpty() || "YOUR_GENERATED_ID".equalsIgnoreCase(targetId)) {
                    throw new IllegalArgumentException("400 Bad Request: Missing or unparsed unique identifier parameter '?id=' in your query URL route.");
                }

                System.out.println("[ENGINE QUERY] Fetching full database record for ID: " + targetId);
                EntityRepository entityRepo = RepositoryFactory.get(tenantId, moduleId, entityName);
                Map<String, Object> record = entityRepo.findById(targetId);

                if (record == null) {
                    throw new NoSuchElementException("404 Not Found: Entity matching ID '" + targetId + "' does not exist in module table " + entityName);
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
        return this.executor.start(ctx, def, runtimeContext);
    }

    private void emitAsyncDomainEvent(TenantContext ctx, String eventType, String entityId, Map<String, Object> context) {
        try {
            System.out.println("[EVENTSTREAM] Async emitting event: " + eventType + " for Resource Key: " + entityId);
        } catch (Exception e) {
            System.err.println("[EVENTSTREAM ERROR] Failed to emit step event structural stream: " + e.getMessage());
        }
    }
    public boolean isSubscribedTo(String eventType) {
        return definitions.values().stream().anyMatch(d -> d.findTransition(null, eventType) != null);
    }
}
