package io.devinebyte.runtime.workflow.model;

import io.devinebyte.compiler.workflow.model.ExecutableStateMachine;
import io.devinebyte.compiler.workflow.model.State;
import io.devinebyte.compiler.workflow.model.Transition;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record WorkflowDefinition(
    String name,
    String version,
    Map<String, State> statesByName,
    String initialState
) {
    public static WorkflowDefinition from(ExecutableStateMachine machine) {
        Map<String, State> states = machine.states().stream()
            .collect(Collectors.toMap(State::name, Function.identity()));
        String initial = machine.states().stream()
            .filter(State::isInitial)
            .map(State::name)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No initial state for " + machine.workflowName()));
        return new WorkflowDefinition(machine.workflowName(), machine.version(), states, initial);
    }

    public Transition findTransition(String currentState, String eventType) {
        State state = statesByName.get(currentState);
        if (state == null) return null;
        return state.transitions().stream()
            .filter(t -> t.triggerEvent().equals(eventType))
            .findFirst()
            .orElse(null);
    }
}
