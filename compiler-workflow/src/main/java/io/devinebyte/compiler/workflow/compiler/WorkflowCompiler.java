package io.devinebyte.compiler.workflow.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.WorkflowIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.workflow.model.ExecutableStateMachine;
import io.devinebyte.compiler.workflow.model.State;
import io.devinebyte.compiler.workflow.model.Transition;
import jakarta.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class WorkflowCompiler {

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void compile(TenantContext tenant, CompilationContext context, BlueprintIR ir) {
        context.diagnostics().addInfo("WORKFLOW", "Compiling " + ir.workflows().size() + " workflows for tenant " + tenant.tenantId());
        
        List<ExecutableStateMachine> machines = new ArrayList<>();
        for (WorkflowIR w : ir.workflows()) {
            machines.add(toStateMachine(w, ir.version()));
        }

        try {
            Path outDir = Path.of("build/workflows");
            Files.createDirectories(outDir);
            mapper.writeValue(outDir.resolve("compiled_state_machines.json").toFile(), machines);
            context.diagnostics().addInfo("WORKFLOW", "Wrote " + machines.size() + " state machines to build/workflows/");
        } catch (Exception e) {
            context.diagnostics().addError("WORKFLOW_IO", "Failed to write workflows: " + e.getMessage());
        }
    }
    
    private ExecutableStateMachine toStateMachine(WorkflowIR w, String version) {
        List<State> states = new ArrayList<>();
        List<String> steps = w.steps(); // steps are just command names for now
        
        for (int i = 0; i < steps.size(); i++) {
            String stepName = steps.get(i);
            String nextState = (i + 1 < steps.size()) ? steps.get(i + 1) : "END";
            
            State state = new State(
                stepName,
                i == 0,
                i == steps.size() - 1,
                List.of(new Transition(stepName + "Completed", nextState, stepName))
            );
            states.add(state);
        }
        
        if (states.isEmpty()) {
            states.add(new State("IDLE", true, true, List.of()));
        }
        
        return new ExecutableStateMachine(w.name(), version, states);
    }
}
