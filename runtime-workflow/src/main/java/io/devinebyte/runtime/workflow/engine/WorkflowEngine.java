package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.projection.kpi.KPIEngine;
import io.devinebyte.runtime.repository.EntityRepository;
import io.devinebyte.runtime.repository.RepositoryFactory;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class WorkflowEngine {
    private WorkflowInstanceRepository repo;
    private WorkflowExecutor executor;
    private KPIEngine kpiEngine;

    private final Map<String, WorkflowDefinition> definitions =
            new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public WorkflowEngine(
            WorkflowInstanceRepository repo,
            WorkflowExecutor executor,
            KPIEngine kpiEngine) {
        this.repo = repo;
        this.executor = executor;
        this.kpiEngine = kpiEngine;
    }

    public void wireDependencies(
            WorkflowInstanceRepository repo,
            WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    public void wireKPIEngine(KPIEngine kpiEngine) {
        this.kpiEngine = kpiEngine;
    }

    public void registerWorkflow(
            String command,
            WorkflowDefinition def) {
        definitions.put(command, def);
    }

    public void register(WorkflowDefinition def) {
        if (def != null && def.name() != null) {
            definitions.put(def.name(), def);
            definitions.put("Handle" + def.name() + "POST", def);
            definitions.put("Handle" + def.name() + "GET", def);
        }
    }

    public WorkflowDefinition getDefinition(String workflowName) {
        return definitions.get(workflowName);
    }

    public void handleEvent(
            TenantContext ctx,
            UUID instanceId,
            DomainEvent event) {
        if (executor == null || repo == null) {
            throw new IllegalStateException(
                    "Workflow storage engine dependencies are completely unassigned.");
        }

        WorkflowInstance instance = repo.findById(instanceId);

        if (instance == null) {
            return;
        }

        WorkflowDefinition def =
                definitions.get(instance.workflowName());

        if (def == null) {
            return;
        }

        WorkflowInstance advanced =
                executor.handleEvent(ctx, def, instance, event);

        repo.save(advanced);

        System.out.println(
                "[WORKFLOW ENGINE] Asynchronously advanced instance "
                        + instanceId
                        + " to state: "
                        + advanced.currentState());
    }

    @SuppressWarnings("unchecked")
    public Object start(
            TenantContext ctx,
            String command,
            JsonNode body,
            String commandOrQuery) {

        System.out.println(
                "[WORKFLOW] Starting "
                        + commandOrQuery
                        + ": "
                        + command
                        + " with payload: "
                        + body);

        String normalizedCmd =
                command != null ? command.trim().toLowerCase() : "";

        if (normalizedCmd.contains("fetchdashboardmetrics")
                || normalizedCmd.contains("dashboardkpis")
                || normalizedCmd.contains("dashboard/kpis")
                || normalizedCmd.endsWith("dashboardget")
                || normalizedCmd.endsWith("kpisget")) {

            if (kpiEngine == null) {
                throw new IllegalStateException(
                        "Dashboard KPI engine dependency is completely unassigned.");
            }

            System.out.println(
                    "[ENGINE QUERY] Dispatching real-time agnostic KPI "
                            + "matrix for tenant: "
                            + ctx.tenantId());

            return kpiEngine.getTenantMetricsGrid(ctx.tenantId());
        }

        Map<String, Object> input =
                MAPPER.convertValue(body, Map.class);

        Map<String, Object> runtimeContext =
                input != null
                        ? new HashMap<>(input)
                        : new HashMap<>();

        Map<String, Object> dataPayload = new HashMap<>();

        if (runtimeContext.get("body") instanceof Map) {
            dataPayload.putAll(
                    (Map<String, Object>) runtimeContext.get("body"));
        } else {
            dataPayload.putAll(runtimeContext);
        }

        String tenantId = ctx.tenantId();

        String entityName = command
                .replace("Handle", "")
                .replace("POST", "")
                .replace("GET", "")
                .replace("PUT", "");

        String moduleId = "crm";
        String lowerEntity = entityName.toLowerCase();

        if (lowerEntity.contains("appointment")
                || lowerEntity.contains("invoice")) {
            moduleId = "sales";
        } else if (lowerEntity.contains("stock")
                || lowerEntity.contains("prescription")) {
            moduleId = "inventory";
        } else if (lowerEntity.contains("payment")
                || lowerEntity.contains("insurance")) {
            moduleId = "finance";
        }

        if (command.startsWith("Handle")
                && command.endsWith("POST")) {
            try {
                EntityRepository entityRepo =
                        RepositoryFactory.get(
                                tenantId,
                                moduleId,
                                entityName);

                String naturalUniqueKey = null;

                if (dataPayload.containsKey("email")) {
                    naturalUniqueKey = "email";
                } else if (dataPayload.containsKey("sku")) {
                    naturalUniqueKey = "sku";
                }

                if (naturalUniqueKey != null) {
                    String uniqueValue =
                            (String) dataPayload.get(naturalUniqueKey);

                    if (uniqueValue != null
                            && !uniqueValue.trim().isEmpty()) {

                        System.out.println(
                                "[ENGINE VALIDATOR] Guard inspecting "
                                        + "constraint parameter dynamically: "
                                        + naturalUniqueKey
                                        + " = "
                                        + uniqueValue);

                        // NOTE: This reflection will fail on Termux. Recommend removing later
                        java.lang.reflect.Field dsField =
                                entityRepo.getClass()
                                        .getDeclaredField("ds");

                        dsField.setAccessible(true);

                        javax.sql.DataSource ds =
                                (javax.sql.DataSource) dsField.get(entityRepo);

                        String table =
                                (moduleId + "_" + entityName).toLowerCase();

                        String checkSql =
                                "SELECT payload FROM " + table;

                        try (
                                java.sql.Connection c =
                                        ds.getConnection();
                                java.sql.PreparedStatement ps =
                                        c.prepareStatement(checkSql);
                                java.sql.ResultSet rs =
                                        ps.executeQuery()) {

                            while (rs.next()) {
                                Map<String, Object> record =
                                        MAPPER.readValue(
                                                rs.getString("payload"),
                                                Map.class);

                                if (record.containsKey(naturalUniqueKey)
                                        && uniqueValue.equalsIgnoreCase(
                                                (String) record.get(
                                                        naturalUniqueKey))) {

                                    System.err.println(
                                            "[CORE REJECTION] Duplicate "
                                                    + "identity conflict found "
                                                    + "for asset: "
                                                    + uniqueValue);

                                    throw new IllegalStateException(
                                            "409 Conflict: An operational "
                                                    + "entity matching unique "
                                                    + "constraints already "
                                                    + "exists inside the system "
                                                    + "runtime.");
                                }
                            }
                        }
                    }
                }

                System.out.println(
                        "[ENGINE PERSISTENCE] Committing complete profile "
                                + "object layout grid down to disk storage...");

                String persistentId =
                        entityRepo.upsert(dataPayload);

                System.out.println(
                        "[ENGINE DEBUG] Successfully wrote full record "
                                + "to DB with ID: "
                                + persistentId);

                // KPI METRIC RECORDING
                if (kpiEngine != null) {
                    kpiEngine.recordMetric(
                            tenantId,
                            entityName + "Created_COUNT",
                            1.0);

                    if (dataPayload.containsKey("status")
                            && dataPayload.get("status") != null) {

                        String statusVal =
                                String.valueOf(
                                        dataPayload.get("status")).trim();

                        kpiEngine.recordMetric(
                                tenantId,
                                entityName + "Updated_COUNT",
                                1.0);

                        kpiEngine.recordMetric(
                                tenantId,
                                entityName + "_"
                                        + statusVal
                                        + "_COUNT",
                                1.0);
                    }
                }

                // ==========================================================
                // FIXED: ROBUST EVENT DERIVATION AND VERBOSE DEBUGGING
                // REPLACED NOTIFICATION ENGINE BLOCK
                // ==========================================================
                System.out.println("[EVENTSTREAM] Async emitting event: " + entityName + "Created for Resource Key: " + persistentId);

                try {
                    com.fasterxml.jackson.databind.node.ObjectNode eventPayload = MAPPER.createObjectNode();
                    for (Map.Entry<String, Object> entry : dataPayload.entrySet()) {
                        if (entry.getValue() != null) {
                            eventPayload.put(entry.getKey(), entry.getValue().toString());
                        }
                    }
                    eventPayload.put("id", persistentId);

                    io.devinebyte.runtime.event.model.EventMetadata meta = new io.devinebyte.runtime.event.model.EventMetadata(
                        java.util.UUID.randomUUID(), null, moduleId, java.time.Instant.now(),
                        java.util.Map.of("tenantId", tenantId, "source", "workflow-engine")
                    );

                    // FIXED: Simple, iron-clad logic. If the input contains a record 'id', it IS a lifecycle status update transaction request!
                    boolean isStatusUpdate = dataPayload.containsKey("id") || (dataPayload.containsKey("status") && !"SCHEDULED".equalsIgnoreCase(eventPayload.get("status").asText()));
                    String derivedEventType = isStatusUpdate ? entityName + "Updated" : entityName + "Created";

                    System.out.println("[NOTIFICATION DESCRIPTOR] Inferred Event Type Channel => " + derivedEventType);

                    io.devinebyte.runtime.event.model.DomainEvent domainEvent = new io.devinebyte.runtime.event.model.DomainEvent(
                        derivedEventType, "1.0", eventPayload, meta
                    );

                    io.devinebyte.runtime.event.handler.NotificationCenterHandler directAlertHandler = 
                        new io.devinebyte.runtime.event.handler.NotificationCenterHandler(domainEvent.type());
                    
                    directAlertHandler.handle(ctx, domainEvent);

                } catch (Exception e) {
                    System.err.println("[EVENTSTREAM ERROR] Direct bypass notification dispatch failed: " + e.getMessage());
                }
                // ==========================================================

                Map<String, Object> savedRecord =
                        entityRepo.findById(persistentId);

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
                System.err.println(
                        "[CORE CRASH] Automatic structural transaction "
                                + "failed: "
                                + ex.getMessage());

                throw new RuntimeException(
                        "500 Internal Server Error: Database engine "
                                + "unavailable - "
                                + ex.getMessage(),
                        ex);
            }
        }

        if (command.startsWith("Handle")
                && command.endsWith("GET")) {
            try {
                String targetId = null;

                if (dataPayload.get("id") != null) {
                    targetId = dataPayload.get("id").toString();
                } else if (runtimeContext.get("id") != null) {
                    targetId =
                            runtimeContext.get("id").toString();
                }

                if (targetId == null
                        || targetId.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "400 Bad Request: Missing unique identifier "
                                    + "parameter 'id'.");
                }

                System.out.println(
                        "[ENGINE QUERY] Fetching full database record "
                                + "for ID: "
                                + targetId);

                EntityRepository entityRepo =
                        RepositoryFactory.get(
                                tenantId,
                                moduleId,
                                entityName);

                Map<String, Object> record =
                        entityRepo.findById(targetId);

                if (record == null) {
                    throw new NoSuchElementException(
                            "404 Not Found: Entity matching ID '"
                                    + targetId
                                    + "' does not exist.");
                }

                return record;

            } catch (Exception ex) {
                throw new RuntimeException(
                        "500 Internal Server Error: Database query failed.",
                        ex);
            }
        }

        WorkflowDefinition def = definitions.get(command);

        if (def == null) {
            throw new IllegalArgumentException(
                    "No workflow definition registered for command: "
                            + command);
        }

        if (executor == null) {
            throw new IllegalStateException(
                    "State machine executor reference is completely "
                            + "unassigned.");
        }

        return executor.start(
                ctx,
                def,
                runtimeContext);
    }

    public boolean isSubscribedTo(String eventType) {
        return definitions.values()
                .stream()
                .anyMatch(
                        d -> d.findTransition(null, eventType) != null);
    }
}
