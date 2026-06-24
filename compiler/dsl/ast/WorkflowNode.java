package compiler.dsl.ast;

import java.util.List;

public class WorkflowNode implements AstNode {

    private final String name;
    private final List<String> states;
    private final List<String> transitions;

    public WorkflowNode(String name,
                        List<String> states,
                        List<String> transitions) {
        this.name = name;
        this.states = states;
        this.transitions = transitions;
    }

    public String getName() {
        return name;
    }

    public List<String> getStates() {
        return states;
    }

    public List<String> getTransitions() {
        return transitions;
    }
}
