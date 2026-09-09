
package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.handler.NotificationCenterHandler;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.EventMetadata;
import io.devinebyte.runtime.projection.kpi.KPIEngine;
import io.devinebyte.runtime.repository.EntityRepository;
import io.devinebyte.runtime.repository.RepositoryFactory;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
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
            definitions.put("Handle" + def.name() + "PUT", def);
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
                command != null
                        ? command.trim().toLowerCase()
                        : "";

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

        Map<String, Object> dataPayload =
                new HashMap<>();

        if (runtimeContext.get("body") instanceof Map) {
            dataPayload.putAll(
                    (Map<String, Object>) runtimeContext.get("body"));
        } else {
            dataPayload.putAll(runtimeContext);
        }

        String tenantId = ctx.tenantId();

        String entityName =
                command
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

        // ==========================================================
        // POST INTERCEPT ROUTE
        // ==========================================================
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
                            String.valueOf(
                                    dataPayload.get(naturalUniqueKey));

                    if (!uniqueValue.trim().isEmpty()) {

                        System.out.println(
                                "[ENGINE VALIDATOR] Guard inspecting "
                                        + "constraint parameter dynamically: "
                                        + naturalUniqueKey
                                        + " = "
                                        + uniqueValue);

                        Field dsField =
                                entityRepo.getClass()
                                        .getDeclaredField("ds");

                        dsField.setAccessible(true);

                        DataSource ds =
                                (DataSource) dsField.get(entityRepo);

                        String table =
                                (moduleId + "_" + entityName)
                                        .toLowerCase();

                        String checkSql =
                                "SELECT payload FROM " + table;

                        try (
                                Connection c = ds.getConnection();
                                PreparedStatement ps =
                                        c.prepareStatement(checkSql);
                                ResultSet rs =
                                        ps.executeQuery()) {

                            while (rs.next()) {
                                Map<String, Object> record =
                                        MAPPER.readValue(
                                                rs.getString("payload"),
                                                Map.class);

                                if (record.containsKey(naturalUniqueKey)
                                        && uniqueValue.equalsIgnoreCase(
                                                String.valueOf(
                                                        record.get(
                                                                naturalUniqueKey)))) {

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

                if (kpiEngine != null) {
                    kpiEngine.recordMetric(
                            tenantId,
                            entityName + "Created_COUNT",
                            1.0);

                    if (dataPayload.containsKey("status")
                            && dataPayload.get("status") != null) {

                        String statusVal =
                                String.valueOf(
                                        dataPayload.get("status"))
                                        .trim();

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
                // ==========================================================
                System.out.println(
                        "[EVENTSTREAM] Async emitting event: "
                                + entityName
                                + "Created for Resource Key: "
                                + persistentId);

                try {
                    ObjectNode eventPayload =
                            MAPPER.createObjectNode();

                    for (Map.Entry<String, Object> entry :
                            dataPayload.entrySet()) {

                        if (entry.getValue() != null) {
                            eventPayload.put(
                                    entry.getKey(),
                                    entry.getValue().toString());
                        }
                    }

                    eventPayload.put("id", persistentId);

                    EventMetadata meta =
                            new EventMetadata(
                                    UUID.randomUUID(),
                                    null,
                                    moduleId,
                                    Instant.now(),
                                    Map.of(
                                            "tenantId",
                                            tenantId,
                                            "source",
                                            "workflow-engine"));

                    // FIXED:
                    // If the incoming payload contains an existing record ID,
                    // treat this as a lifecycle status/update transaction.
                    boolean isStatusUpdate =
                            dataPayload.containsKey("id")
                                    || (
                                    dataPayload.containsKey("status")
                                            && !"SCHEDULED"
                                            .equalsIgnoreCase(
                                                    eventPayload
                                                            .get("status")
                                                            .asText())
                            );

                    String derivedEventType =
                            isStatusUpdate
                                    ? entityName + "Updated"
                                    : entityName + "Created";

                    System.out.println(
                            "[NOTIFICATION DESCRIPTOR] Inferred Event Type "
                                    + "Channel => "
                                    + derivedEventType);

                    DomainEvent domainEvent =
                            new DomainEvent(
                                    derivedEventType,
                                    "1.0",
                                    eventPayload,
                                    meta);

                    NotificationCenterHandler directAlertHandler =
                            new NotificationCenterHandler(
                                    domainEvent.type());

                    directAlertHandler.handle(
                            ctx,
                            domainEvent);

                    // ======================================================
                    // OPERATIONAL POLICY ENGINE ROUTING
                    // ======================================================
                            
                try {
                    OperationalPolicyEngine policyEngine = new OperationalPolicyEngine();
                    policyEngine.evaluateEvent(ctx, domainEvent, this.kpiEngine != null ? this.kpiEngine.getTenantMetricsGrid(tenantId) : null);
                } catch (Exception ignored) {}

                } catch (Exception e) {
                    System.err.println(
                            "[EVENTSTREAM ERROR] Direct bypass notification "
                                    + "dispatch failed: "
                                    + e.getMessage());
                }

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

        // ==========================================================
        // ADDED: INDUSTRY-BLIND PUT INTERCEPT ROUTE
        // ==========================================================
        if (command.startsWith("Handle")
                && command.endsWith("PUT")) {

            try {
                EntityRepository entityRepo =
                        RepositoryFactory.get(
                                tenantId,
                                moduleId,
                                entityName);

                String targetId = null;

                if (dataPayload.get("id") != null) {
                    targetId =
                            dataPayload.get("id").toString();

                } else if (runtimeContext.get("id") != null) {
                    targetId =
                            runtimeContext.get("id").toString();
                }

                if (targetId == null
                        || targetId.trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "400 Bad Request: Missing entity record "
                                    + "tracking identifier 'id' for PUT updates.");
                }

                System.out.println(
                        "[ENGINE UPDATE] Merging incoming payload "
                                + "modifications onto record ID: "
                                + targetId);

                Map<String, Object> existingRecord =
                        entityRepo.findById(targetId);

                if (existingRecord == null) {
                    throw new NoSuchElementException(
                            "404 Not Found: Cannot perform update. "
                                    + "Entity matching ID '"
                                    + targetId
                                    + "' does not exist.");
                }

                // Merge incoming modifications.
                existingRecord.putAll(dataPayload);

                // Keep the original tracking identifier locked.
                existingRecord.put(
                        "id",
                        targetId);

                System.out.println(
                        "[ENGINE UPDATE] Persisting merged record "
                                + "layout for ID: "
                                + targetId);

                String persistentId =
                        entityRepo.upsert(existingRecord);

                // ==========================================================
                // TELEMETRY STREAM FEED
                // ==========================================================
                if (kpiEngine != null) {

                    kpiEngine.recordMetric(
                            tenantId,
                            entityName + "Updated_COUNT",
                            1.0);

                    if (dataPayload.containsKey("status")
                            && dataPayload.get("status") != null) {

                        String statusVal =
                                String.valueOf(
                                        dataPayload.get("status"))
                                        .trim();

                        kpiEngine.recordMetric(
                                tenantId,
                                entityName + "_"
                                        + statusVal
                                        + "_COUNT",
                                1.0);
                    }
                }

                System.out.println(
                        "[EVENTSTREAM] Async emitting update event: "
                                + entityName
                                + "Updated for Key: "
                                + persistentId);

                // ==========================================================
                // DIRECT UPDATE EVENT ROUTER
                // ==========================================================
                try {
                    ObjectNode eventPayload =
                            MAPPER.createObjectNode();

                    for (Map.Entry<String, Object> entry :
                            existingRecord.entrySet()) {

                        if (entry.getValue() != null) {
                            eventPayload.put(
                                    entry.getKey(),
                                    entry.getValue().toString());
                        }
                    }

                    eventPayload.put(
                            "id",
                            persistentId);

                    EventMetadata meta =
                            new EventMetadata(
                                    UUID.randomUUID(),
                                    null,
                                    moduleId,
                                    Instant.now(),
                                    Map.of(
                                            "tenantId",
                                            tenantId,
                                            "source",
                                            "workflow-engine-update"));

                    DomainEvent domainEvent =
                            new DomainEvent(
                                    entityName + "Updated",
                                    "1.0",
                                    eventPayload,
                                    meta);

                    System.out.println(
                            "[NOTIFICATION DESCRIPTOR] Inferred Event Type "
                                    + "Channel => "
                                    + domainEvent.type());

                    NotificationCenterHandler directAlertHandler =
                            new NotificationCenterHandler(
                                    domainEvent.type());

                    directAlertHandler.handle(
                            ctx,
                            domainEvent);

                    // ======================================================
                    // OPERATIONAL POLICY ENGINE ROUTING
                    // ======================================================
                    try {
                        OperationalPolicyEngine policyEngine =
                                new OperationalPolicyEngine();

                        policyEngine.evaluateEvent(
                                ctx,
                                domainEvent,
                                kpiEngine != null
                                        ? kpiEngine.getTenantMetricsGrid(
                                                tenantId)
                                        : null);

                    } catch (Exception ignored) {
                    }

                } catch (Exception e) {
                    System.err.println(
                            "[EVENTSTREAM ERROR] Direct bypass update "
                                    + "notification dispatch failed: "
                                    + e.getMessage());
                }

                existingRecord.put(
                        "status",
                        "SUCCESS");

                return existingRecord;

            } catch (NoSuchElementException
                    | IllegalArgumentException ex) {

                throw ex;

            } catch (Exception ex) {

                System.err.println(
                        "[CORE CRASH] PUT database transaction failed: "
                                + ex.getMessage());

                throw new RuntimeException(
                        "500 Internal Server Error: "
                                + ex.getMessage(),
                        ex);
            }
        }

        // ==========================================================
        // HARDENED GET INTERCEPT ROUTE
        // ==========================================================
        if (command.startsWith("Handle")
                && command.endsWith("GET")) {

            try {
                String targetId = null;

                if (dataPayload.get("id") != null) {
                    targetId =
                            dataPayload.get("id").toString();

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

            } catch (NoSuchElementException
                    | IllegalArgumentException ex) {

                throw ex;

            } catch (Exception ex) {

                throw new RuntimeException(
                        "500 Internal Server Error: Database query failed.",
                        ex);
            }
        }

        // ==========================================================
        // STANDARD WORKFLOW EXECUTION
        // ==========================================================
        WorkflowDefinition def =
                definitions.get(command);

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
                        d -> d.findTransition(
                                null,
                                eventType) != null);
    }
}

