package io.devinebyte.runtime.workflow.diagnostics;

public final class WorkflowDiagnostics {
    public static final String DBWF001 = "Missing /workflows/compiled_state_machines.json";
    public static final String DBWF002 = "Failed to parse compiled_state_machines.json";
    public static final String DBWF003 = "Workflow not found in manifest";
    public static final String DBWF004 = "Module workflow is disabled";
    private WorkflowDiagnostics() {}
}
