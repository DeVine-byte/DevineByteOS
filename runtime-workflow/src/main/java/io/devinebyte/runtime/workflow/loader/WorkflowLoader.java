package io.devinebyte.runtime.workflow.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.compiler.workflow.model.ExecutableStateMachine;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record WorkflowLoader(ObjectMapper mapper) {
    public WorkflowLoader() { this(new ObjectMapper()); }

    public WorkflowLoadResult load(FileSystem fs, TenantContext ctx, DiagnosticCollector diagnostics) {
        Path path = fs.getPath("workflows", "compiled_state_machines.json");
        if (!Files.exists(path)) {
            diagnostics.error("DBWF001", "Missing required file: /workflows/compiled_state_machines.json", ctx.tenantId());
            return new WorkflowLoadResult(Map.of(), diagnostics);
        }
        try (InputStream in = Files.newInputStream(path)) {
            List<ExecutableStateMachine> machines = mapper.readValue(in, new TypeReference<>() {});
            Map<String, ExecutableStateMachine> byName = machines.stream()
                .collect(Collectors.toMap(ExecutableStateMachine::workflowName, Function.identity()));
            return new WorkflowLoadResult(byName, diagnostics);
        } catch (Exception e) {
            diagnostics.error("DBWF002", "Failed to parse compiled_state_machines.json: " + e.getMessage(), ctx.tenantId());
            return new WorkflowLoadResult(Map.of(), diagnostics);
        }
    }
}
