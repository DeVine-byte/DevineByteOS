package io.devinebyte.runtime.observability;

public final class RuntimeMetrics {
    public static final String BOOT_DURATION = "dbos_boot_duration_seconds";
    public static final String MODULE_COUNT = "dbos_module_count";
    public static final String EVENT_PROCESSED_TOTAL = "dbos_event_processed_total";
    public static final String EVENT_FAILED_TOTAL = "dbos_event_failed_total";
    public static final String EVENT_PROCESSING_DURATION = "dbos_event_processing_duration_seconds";
    public static final String WORKFLOW_STARTED_TOTAL = "dbos_workflow_started_total";
    public static final String WORKFLOW_COMPLETED_TOTAL = "dbos_workflow_completed_total";
    public static final String WORKFLOW_FAILED_TOTAL = "dbos_workflow_failed_total";
    public static final String PROJECTION_PROCESSED_TOTAL = "dbos_projection_processed_total";
    public static final String PROJECTION_FAILED_TOTAL = "dbos_projection_failed_total";
    public static final String RUNTIME_UPTIME = "dbos_runtime_uptime_seconds";
    
    private RuntimeMetrics() {}
}
