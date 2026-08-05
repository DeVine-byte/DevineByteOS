package io.devinebyte.runtime.workflow.loader;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.compiler.workflow.model.ExecutableStateMachine;
import java.util.Map;

public record WorkflowLoadResult(
    Map<String, ExecutableStateMachine> workflows, 
    DiagnosticCollector diagnostics
) {}
