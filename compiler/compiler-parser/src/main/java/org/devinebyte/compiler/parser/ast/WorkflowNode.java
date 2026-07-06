package org.devinebyte.compiler.parser.ast;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public class WorkflowNode extends AstNode {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
