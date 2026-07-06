package org.devinebyte.compiler.blueprint.model;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public class PolicyDefinition {

    private String name;

    private String expression;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

}
