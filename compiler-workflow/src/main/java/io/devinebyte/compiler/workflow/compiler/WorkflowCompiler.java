package io.devinebyte.compiler.workflow.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;      
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.blueprint.model.EntityIR;          
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
        List<ExecutableStateMachine> machines = new ArrayList<>();

        // 1. Process explicitly defined workflows from the DSL (e.g., FulfillOrder)
        if (ir.workflows() != null) {
            for (WorkflowIR w : ir.workflows()) {
                // Find the module that owns this explicit workflow by checking module definitions
                String owningModuleId = "runtime";
                if (ir.modules() != null) {
                    for (ModuleIR module : ir.modules()) {
                        if (module.workflows() != null && module.workflows().stream().anyMatch(wf -> wf.name().equals(w.name()))) {
                            owningModuleId = module.id();
                            break;
                        }
                    }
                }
                machines.add(toStateMachine(w, owningModuleId, ir.version()));
            }
        }

        // 2. AUTO-GENERATION: Convert entities inside enabled modules to implicit CRUD endpoints
        if (ir.modules() != null) {
            for (ModuleIR module : ir.modules()) {
                if (module.entities() == null) continue;

                for (EntityIR entity : module.entities()) {
                    // Generate implicit Write Pipeline
                    String postWfName = "Handle" + entity.name() + "POST";
                    if (ir.workflows() == null || ir.workflows().stream().noneMatch(w -> w.name().equals(postWfName))) {
                        machines.add(createImplicitCrudStateMachine(postWfName, module.id(), ir.version(), entity));
                    }

                    // ADDED FOR PHASE 1.2: Generate implicit Read Pipeline
                    String getWfName = "Handle" + entity.name() + "GET";
                    if (ir.workflows() == null || ir.workflows().stream().noneMatch(w -> w.name().equals(getWfName))) {
                        machines.add(createImplicitGetStateMachine(getWfName, module.id(), ir.version()));
                    }
                }
            }
        }

        context.diagnostics().addInfo("WORKFLOW", "Compiling " + machines.size() + " workflows for tenant " + tenant.tenantId());

        try {
            Path outDir = Path.of("build/workflows");
            Files.createDirectories(outDir);

            for (ExecutableStateMachine machine : machines) {
                String name = machine.workflowName();
                Path wfFile = outDir.resolve(name + ".json");
                mapper.writeValue(wfFile.toFile(), machine);
            }

            mapper.writeValue(outDir.resolve("compiled_state_machines.json").toFile(), machines);
            context.diagnostics().addInfo("WORKFLOW", "Wrote " + machines.size() + " state machines to build/workflows/");
        } catch (Exception e) {
            context.diagnostics().addError("WORKFLOW_IO", "Failed to write workflows: " + e.getMessage());
        }
    }

    private ExecutableStateMachine toStateMachine(WorkflowIR w, String moduleId, String version) {
        List<State> states = new ArrayList<>();
        List<String> steps = w.steps();

        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                String stepName = steps.get(i);
                boolean isInitial = (i == 0);
                boolean isFinal = (i == steps.size() - 1);
                
                String nextState = isFinal ? "END" : steps.get(i + 1);
                List<Transition> transitions = List.of(
                    new Transition(stepName + "Completed", nextState, "builtin:step:" + stepName)
                );
                
                states.add(new State(stepName, isInitial, isFinal, transitions));
            }
        }
        return new ExecutableStateMachine(w.name(), moduleId, version, states);
    }

    private ExecutableStateMachine createImplicitCrudStateMachine(String name, String moduleId, String version, EntityIR entity) {
        List<State> states = new ArrayList<>();
        states.add(new State(
            "UpsertEntity",
            true, // initial
            true, // final
            List.of(new Transition(name + "Completed", "END", "builtin:repository:upsert"))
        ));
        return new ExecutableStateMachine(name, moduleId, version, states);
    }

    private ExecutableStateMachine createImplicitGetStateMachine(String name, String moduleId, String version) {
        List<State> states = new ArrayList<>();
        states.add(new State(
            "FetchEntity",
            true, // initial state
            false, // not final
            List.of(
                new Transition(name + "FetchSuccess", "END", "builtin:repository:find"),
                new Transition(name + "FetchFailed", "FailExecution", "builtin:error:500")
            )
        ));
        states.add(new State("FailExecution", false, true, List.of()));
        return new ExecutableStateMachine(name, moduleId, version, states);
    }
}

